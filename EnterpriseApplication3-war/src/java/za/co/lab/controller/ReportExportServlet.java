/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.model.User;
import za.co.lab.service.AlertExcelReportService;
import za.co.lab.service.AlertPdfReportService;
import za.co.lab.service.AuditService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Generates authorised PDF and Excel alert-report downloads.
 *
 * Report scope is derived from the authenticated user. The servlet never
 * accepts a role, user ID or pathologist ID from the browser.
 *
 * @author USER
 */
@WebServlet("/report-export")
public class ReportExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ReportExportServlet.class.getName());

    private static final String PDF_FORMAT = "pdf";
    private static final String EXCEL_FORMAT = "xlsx";

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final int MAX_PATIENT_CODE_LENGTH = 30;

    private static final Set<String> ALLOWED_SEVERITIES = Set.of("CRITICAL", "WARNING");
    private static final Set<String> ALLOWED_NOTIFICATION_STATUSES = Set.of(
            "PENDING", "EMAIL_SENT", "EMAIL_FAILED", "SENT", "SIMULATED_SENT"
    );
    private static final Set<String> ALLOWED_ACKNOWLEDGEMENT_STATUSES = Set.of("ACKNOWLEDGED", "PENDING");
    private static final Set<String> ALLOWED_TEST_NAMES = Set.of("Glucose", "Potassium", "Hemoglobin", "WBC", "Platelets");

    private final AlertDAO alertDAO = new AlertDAO();
    private final AlertPdfReportService pdfReportService = new AlertPdfReportService();
    private final AlertExcelReportService excelReportService = new AlertExcelReportService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        /*
         * Require an existing authenticated session.
         */
        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectToLogin(request, response);
            return;
        }

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectToLogin(request, response);
            return;
        }

        String role = normalizeUpper(loggedInUser.getRole());
        boolean isAdministrator = "ADMIN".equals(role);
        boolean isPathologist = "PATHOLOGIST".equals(role);

        /*
         * Only administrators and pathologists may export reports.
         */
        if (!isAdministrator && !isPathologist) {
            logAuditSafely(
                    loggedInUser,
                    "UNAUTHORIZED_REPORT_EXPORT",
                    null,
                    "A user without report-export permission attempted to export an alert report."
            );
            showAccessDenied(
                    request,
                    response,
                    "You are not authorised to export laboratory alert reports."
            );
            return;
        }

        /*
         * A pathologist account must be linked to a valid profile.
         */
        if (isPathologist && (loggedInUser.getPathologistId() == null || loggedInUser.getPathologistId() <= 0)) {
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_REJECTED",
                    null,
                    "The pathologist account is not linked to a valid pathologist profile."
            );
            showAccessDenied(
                    request,
                    response,
                    "The logged-in account is not linked to a valid pathologist profile."
            );
            return;
        }

        /*
         * Validate the requested export format.
         */
        String format = normalizeLower(request.getParameter("format"));
        if (!PDF_FORMAT.equals(format) && !EXCEL_FORMAT.equals(format)) {
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_REJECTED",
                    null,
                    "An unsupported report-export format was requested."
            );
            redirectToReportsWithError(request, response, "Select a supported report export format.");
            return;
        }

        /*
         * Rebuild and revalidate every filter server-side.
         */
        AlertReportFilter filter = new AlertReportFilter();
        try {
            populateFilter(request, filter);
        } catch (IllegalArgumentException exception) {
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_REJECTED",
                    null,
                    "Report export validation failed: " + exception.getMessage()
            );
            redirectToReportsWithError(request, response, exception.getMessage());
            return;
        }

        if (filter.hasInvalidDateRange()) {
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_REJECTED",
                    null,
                    "The report start date was later than the report end date."
            );
            redirectToReportsWithError(request, response, "The start date may not be later than the end date.");
            return;
        }

        List<Alert> alerts;
        String reportScope;
        try {
            if (isPathologist) {
                /*
                 * The pathologist identifier comes only from the authenticated session user.
                 */
                alerts = alertDAO.findForReportByPathologistId(filter, loggedInUser.getPathologistId());
                reportScope = "Assigned Patients";
            } else {
                alerts = alertDAO.findForReport(filter);
                reportScope = "All Authorised Alerts";
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve alert records for report export.", exception);
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_FAILED",
                    null,
                    "The authorised report records could not be retrieved."
            );
            redirectToReportsWithError(request, response, "The report records could not be retrieved.");
            return;
        }

        /*
         * Generate the complete document in memory before setting response headers.
         *
         * This prevents a partially generated PDF or workbook from being returned if document generation fails.
         */
        byte[] reportBytes;
        try {
            reportBytes = generateReport(format, alerts, filter, loggedInUser, reportScope);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Unable to generate the " + format + " alert report.", exception);
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_FAILED",
                    null,
                    "Alert report generation failed. format=" + format
            );
            redirectToReportsWithError(request, response, "The report file could not be generated.");
            return;
        }

        if (reportBytes == null || reportBytes.length == 0) {
            logAuditSafely(
                    loggedInUser,
                    "REPORT_EXPORT_FAILED",
                    null,
                    "The generated report file was empty. format=" + format
            );
            redirectToReportsWithError(request, response, "The generated report file was empty.");
            return;
        }

        String filename = createFilename(format);

        /*
         * Set secure download response headers.
         */
        response.reset();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(PDF_FORMAT.equals(format) ? PDF_CONTENT_TYPE : EXCEL_CONTENT_TYPE);
        response.setContentLength(reportBytes.length);

        String encodedFilename = encode(filename);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename
        );
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(reportBytes);
            outputStream.flush();
        }

        logAuditSafely(
                loggedInUser,
                "REPORT_EXPORTED",
                null,
                "Alert report exported. format=" + format.toUpperCase(Locale.ROOT)
                + ", recordCount=" + alerts.size()
                + ", scope=" + reportScope
        );
    }

    /**
     * Prevents direct GET requests from generating exports.
     *
     * The reports page submits exports using POST.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Report exports must be requested from the reporting page."
        );
    }

    /**
     * Generates a PDF or Excel file in memory.
     */
    private byte[] generateReport(
            String format,
            List<Alert> alerts,
            AlertReportFilter filter,
            User user,
            String reportScope) throws IOException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (PDF_FORMAT.equals(format)) {
                pdfReportService.writeReport(outputStream, alerts, filter, user, reportScope);
            } else if (EXCEL_FORMAT.equals(format)) {
                excelReportService.writeReport(outputStream, alerts, filter, user, reportScope);
            } else {
                throw new IllegalArgumentException("Unsupported export format.");
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Reads and validates supported report filters.
     */
    private void populateFilter(HttpServletRequest request, AlertReportFilter filter) {
        LocalDate startDate = parseDate(request.getParameter("startDate"), "start date");
        LocalDate endDate = parseDate(request.getParameter("endDate"), "end date");

        String severity = normalizeUpper(request.getParameter("severity"));
        String notificationStatus = normalizeUpper(request.getParameter("notificationStatus"));
        String acknowledgementStatus = normalizeUpper(request.getParameter("acknowledgementStatus"));
        String patientCode = clean(request.getParameter("patientCode"));
        String testName = clean(request.getParameter("testName"));

        validateOptionalValue(severity, ALLOWED_SEVERITIES, "severity");
        validateOptionalValue(notificationStatus, ALLOWED_NOTIFICATION_STATUSES, "notification status");
        validateOptionalValue(acknowledgementStatus, ALLOWED_ACKNOWLEDGEMENT_STATUSES, "acknowledgement status");
        validateOptionalValue(testName, ALLOWED_TEST_NAMES, "test name");

        if (patientCode.length() > MAX_PATIENT_CODE_LENGTH) {
            throw new IllegalArgumentException(
                    "The patient code search may not exceed " + MAX_PATIENT_CODE_LENGTH + " characters."
            );
        }

        if (!patientCode.isEmpty() && !patientCode.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "The patient code may contain only letters, numbers, hyphens and underscores."
            );
        }

        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSeverity(severity);
        filter.setNotificationStatus(notificationStatus);
        filter.setAcknowledgementStatus(acknowledgementStatus);
        filter.setPatientCode(patientCode);
        filter.setTestName(testName);
    }

    /**
     * Parses an optional ISO date.
     */
    private LocalDate parseDate(String value, String label) {
        String cleanedValue = clean(value);
        if (cleanedValue.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(cleanedValue);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("The " + label + " is invalid. Use yyyy-MM-dd.");
        }
    }

    /**
     * Validates an optional value against a fixed allow-list.
     */
    private void validateOptionalValue(String value, Set<String> allowedValues, String label) {
        if (value == null || value.isBlank()) {
            return;
        }

        boolean allowed = allowedValues.stream()
                .anyMatch(allowedValue -> allowedValue.equalsIgnoreCase(value));

        if (!allowed) {
            throw new IllegalArgumentException("The selected " + label + " is not supported.");
        }
    }

    /**
     * Creates a safe report filename.
     */
    private String createFilename(String format) {
        String date = LocalDate.now().toString();
        if (PDF_FORMAT.equals(format)) {
            return "Lab_Alert_Report_" + date + ".pdf";
        }
        return "Lab_Alert_Report_" + date + ".xlsx";
    }

    /**
     * Redirects unauthenticated users to login.
     */
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/index.jsp?error=" + encode("Please login to export reports."));
    }

    /**
     * Redirects back to the report page with a safe error message.
     */
    private void redirectToReportsWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/reports?error=" + encode(message));
    }

    /**
     * Displays the branded HTTP 403 page.
     */
    private void showAccessDenied(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/access-denied.jsp").forward(request, response);
    }

    /**
     * Writes an audit event without allowing audit failure to interrupt the
     * export workflow.
     */
    private void logAuditSafely(User user, String action, Integer entityId, String details) {
        try {
            AuditService.log(user, action, "REPORT", entityId, details);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Unable to record the report audit event.", exception);
        }
    }

    /**
     * Trims a value and converts null to an empty string.
     */
    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Normalises text to uppercase.
     */
    private String normalizeUpper(String value) {
        return clean(value).toUpperCase(Locale.ROOT);
    }

    /**
     * Normalises text to lowercase.
     */
    private String normalizeLower(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    /**
     * URL-encodes redirect values using UTF-8.
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
