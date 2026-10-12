/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ReportServlet.class.getName());

    private static final Set<String> ALLOWED_SEVERITIES =
            Set.of(
                    "CRITICAL",
                    "WARNING"
            );

    private static final Set<String> ALLOWED_NOTIFICATION_STATUSES =
            Set.of(
                    "PENDING",
                    "EMAIL_SENT",
                    "EMAIL_FAILED",
                    "SENT",
                    "SIMULATED_SENT"
            );

    private static final Set<String> ALLOWED_ACKNOWLEDGEMENT_STATUSES =
            Set.of(
                    "ACKNOWLEDGED",
                    "PENDING"
            );

    private static final Set<String> ALLOWED_TEST_NAMES =
            Set.of(
                    "Glucose",
                    "Potassium",
                    "Hemoglobin",
                    "WBC",
                    "Platelets"
            );

    private static final int MAX_PATIENT_CODE_LENGTH = 30;

    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        /*
         * Use an existing session only.
         *
         * An unauthenticated request must not create a new session
         * merely by attempting to access the reporting module.
         */
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            redirectToLogin(request, response);
            return;
        }

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectToLogin(request, response);
            return;
        }

        String role =
                normalizeUpper(loggedInUser.getRole());

        boolean isAdministrator =
                "ADMIN".equals(role);

        boolean isPathologist =
                "PATHOLOGIST".equals(role);

        /*
         * Only administrators and pathologists may access the
         * reporting module.
         *
         * Laboratory personnel may capture results but may not
         * export alert-monitoring reports.
         */
        if (!isAdministrator && !isPathologist) {

            AuditService.log(
                    loggedInUser,
                    "UNAUTHORIZED_REPORT_ACCESS",
                    "REPORT",
                    null,
                    "A user without report permission attempted "
                    + "to access the alert reporting module."
            );

            showAccessDenied(
                    request,
                    response,
                    "You are not authorised to access "
                    + "the alert reporting module."
            );

            return;
        }

        /*
         * A pathologist account must be connected to an active
         * pathologist profile before assigned-alert reports can
         * be generated.
         */
        if (isPathologist
                && (loggedInUser.getPathologistId() == null
                || loggedInUser.getPathologistId() <= 0)) {

            AuditService.log(
                    loggedInUser,
                    "REPORT_ACCESS_REJECTED",
                    "REPORT",
                    null,
                    "The pathologist account is not linked "
                    + "to a valid pathologist profile."
            );

            showAccessDenied(
                    request,
                    response,
                    "The logged-in account is not linked "
                    + "to a valid pathologist profile."
            );

            return;
        }

        /*
         * Build the report filter exclusively from permitted
         * request parameters.
         *
         * No user ID, role or pathologist ID is accepted from
         * the browser.
         */
        AlertReportFilter filter =
                new AlertReportFilter();

        try {
            populateFilter(
                    request,
                    filter
            );

        } catch (IllegalArgumentException exception) {

            preserveFilterValues(
                    request,
                    filter
            );

            request.setAttribute(
                    "error",
                    exception.getMessage()
            );

            request.setAttribute(
                    "alerts",
                    Collections.emptyList()
            );

            setPageAttributes(
                    request,
                    loggedInUser,
                    isAdministrator,
                    isPathologist,
                    0
            );

            request.getRequestDispatcher("/reports.jsp")
                    .forward(request, response);

            return;
        }

        if (filter.hasInvalidDateRange()) {

            preserveFilterValues(
                    request,
                    filter
            );

            request.setAttribute(
                    "error",
                    "The start date may not be later "
                    + "than the end date."
            );

            request.setAttribute(
                    "alerts",
                    Collections.emptyList()
            );

            setPageAttributes(
                    request,
                    loggedInUser,
                    isAdministrator,
                    isPathologist,
                    0
            );

            request.getRequestDispatcher("/reports.jsp")
                    .forward(request, response);

            return;
        }

        List<Alert> alerts;

        try {
            if (isPathologist) {

                /*
                 * Security boundary:
                 *
                 * The pathologist identifier is obtained from the
                 * authenticated User object, never from a request
                 * parameter.
                 */
                alerts =
                        alertDAO.findForReportByPathologistId(
                                filter,
                                loggedInUser.getPathologistId()
                        );

            } else {

                alerts =
                        alertDAO.findForReport(filter);
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to generate the alert report preview.",
                    exception
            );

            request.setAttribute(
                    "error",
                    "The report could not be generated. "
                    + "Please try again."
            );

            alerts =
                    Collections.emptyList();
        }

        preserveFilterValues(
                request,
                filter
        );

        request.setAttribute(
                "filter",
                filter
        );

        request.setAttribute(
                "alerts",
                alerts
        );

        setPageAttributes(
                request,
                loggedInUser,
                isAdministrator,
                isPathologist,
                alerts.size()
        );

        /*
         * Record the report preview event.
         *
         * No confidential row data is written to the audit detail.
         */
        AuditService.log(
                loggedInUser,
                "REPORT_PREVIEWED",
                "REPORT",
                null,
                "Alert report preview generated. "
                + "recordCount="
                + alerts.size()
                + ", scope="
                + (isPathologist
                        ? "ASSIGNED_PATIENTS"
                        : "ALL_AUTHORISED_ALERTS")
        );

        request.getRequestDispatcher("/reports.jsp")
                .forward(request, response);
    }

    /**
     * Supports submitted filter forms that use method="post".
     *
     * Report preview remains read-only. Both GET and POST requests
     * pass through the same authentication, role and validation
     * checks.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }

    /**
     * Reads and validates all supported report filters.
     *
     * Empty values represent "all" and are accepted.
     *
     * @param request HTTP request
     * @param filter report filter to populate
     */
    private void populateFilter(
            HttpServletRequest request,
            AlertReportFilter filter) {

        LocalDate startDate =
                parseDate(
                        request.getParameter("startDate"),
                        "start date"
                );

        LocalDate endDate =
                parseDate(
                        request.getParameter("endDate"),
                        "end date"
                );

        String severity =
                normalizeUpper(
                        request.getParameter("severity")
                );

        String notificationStatus =
                normalizeUpper(
                        request.getParameter(
                                "notificationStatus"
                        )
                );

        String acknowledgementStatus =
                normalizeUpper(
                        request.getParameter(
                                "acknowledgementStatus"
                        )
                );

        String patientCode =
                clean(
                        request.getParameter("patientCode")
                );

        String testName =
                clean(
                        request.getParameter("testName")
                );

        validateOptionalValue(
                severity,
                ALLOWED_SEVERITIES,
                "severity"
        );

        validateOptionalValue(
                notificationStatus,
                ALLOWED_NOTIFICATION_STATUSES,
                "notification status"
        );

        validateOptionalValue(
                acknowledgementStatus,
                ALLOWED_ACKNOWLEDGEMENT_STATUSES,
                "acknowledgement status"
        );

        validateOptionalValue(
                testName,
                ALLOWED_TEST_NAMES,
                "test name"
        );

        if (patientCode.length()
                > MAX_PATIENT_CODE_LENGTH) {

            throw new IllegalArgumentException(
                    "The patient code search may not exceed "
                    + MAX_PATIENT_CODE_LENGTH
                    + " characters."
            );
        }

        if (!patientCode.isEmpty()
                && !patientCode.matches("[A-Za-z0-9_-]+")) {

            throw new IllegalArgumentException(
                    "The patient code may contain only letters, "
                    + "numbers, hyphens and underscores."
            );
        }

        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSeverity(severity);
        filter.setNotificationStatus(notificationStatus);
        filter.setAcknowledgementStatus(
                acknowledgementStatus
        );
        filter.setPatientCode(patientCode);
        filter.setTestName(testName);
    }

    /**
     * Parses an optional ISO date in yyyy-MM-dd format.
     *
     * HTML date fields submit values in this format.
     *
     * @param value submitted date
     * @param label field label used in validation messages
     * @return parsed date or null when blank
     */
    private LocalDate parseDate(
            String value,
            String label) {

        String cleanedValue =
                clean(value);

        if (cleanedValue.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(cleanedValue);

        } catch (DateTimeParseException exception) {

            throw new IllegalArgumentException(
                    "The "
                    + label
                    + " is invalid. Use the format yyyy-MM-dd."
            );
        }
    }

    /**
     * Validates one optional value against a fixed allow-list.
     *
     * Blank values are accepted because blank means no filter.
     *
     * @param value submitted value
     * @param allowedValues allowed values
     * @param label field label
     */
    private void validateOptionalValue(
            String value,
            Set<String> allowedValues,
            String label) {

        if (value == null || value.isBlank()) {
            return;
        }

        boolean allowed =
                allowedValues.stream()
                        .anyMatch(
                                allowedValue ->
                                        allowedValue.equalsIgnoreCase(
                                                value
                                        )
                        );

        if (!allowed) {
            throw new IllegalArgumentException(
                    "The selected "
                    + label
                    + " is not supported."
            );
        }
    }

    /**
     * Makes filter values available to reports.jsp so that the
     * selected controls remain populated after filtering or after
     * a validation error.
     */
    private void preserveFilterValues(
            HttpServletRequest request,
            AlertReportFilter filter) {

        request.setAttribute(
                "selectedStartDate",
                filter.getStartDate() == null
                        ? ""
                        : filter.getStartDate().toString()
        );

        request.setAttribute(
                "selectedEndDate",
                filter.getEndDate() == null
                        ? ""
                        : filter.getEndDate().toString()
        );

        request.setAttribute(
                "selectedSeverity",
                filter.getSeverity()
        );

        request.setAttribute(
                "selectedNotificationStatus",
                filter.getNotificationStatus()
        );

        request.setAttribute(
                "selectedAcknowledgementStatus",
                filter.getAcknowledgementStatus()
        );

        request.setAttribute(
                "selectedPatientCode",
                filter.getPatientCode()
        );

        request.setAttribute(
                "selectedTestName",
                filter.getTestName()
        );
    }

    /**
     * Adds common report-page attributes.
     */
    private void setPageAttributes(
            HttpServletRequest request,
            User loggedInUser,
            boolean isAdministrator,
            boolean isPathologist,
            int recordCount) {

        request.setAttribute(
                "isAdministrator",
                isAdministrator
        );

        request.setAttribute(
                "isPathologist",
                isPathologist
        );

        request.setAttribute(
                "recordCount",
                recordCount
        );

        request.setAttribute(
                "reportScope",
                isPathologist
                        ? "Assigned Patients"
                        : "All Authorised Alerts"
        );

        request.setAttribute(
                "reportUser",
                loggedInUser
        );
    }

    /**
     * Redirects an unauthenticated user to the login page.
     */
    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/index.jsp?error="
                + "Please+login+to+access+reports"
        );
    }

    /**
     * Displays the branded application access-denied page.
     */
    private void showAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute(
                "errorMessage",
                message
        );

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        request.getRequestDispatcher(
                "/access-denied.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Trims a value and converts null to an empty string.
     */
    private String clean(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Converts a value to uppercase after trimming it.
     */
    private String normalizeUpper(String value) {

        return clean(value).toUpperCase();
    }
}