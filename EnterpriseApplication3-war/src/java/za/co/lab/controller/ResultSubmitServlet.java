package za.co.lab.controller;

import za.co.lab.dao.PathologistDAO;
import za.co.lab.dao.PatientDAO;
import za.co.lab.model.LabResult;
import za.co.lab.model.Patient;
import za.co.lab.model.User;
import za.co.lab.service.AlertService;
import za.co.lab.service.AuditService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
 * Validates, evaluates and saves submitted laboratory results.
 *
 * Only administrators may capture laboratory results.
 *
 * The servlet validates that:
 *
 * 1. The selected pathologist exists and is active. 2. The selected patient
 * exists. 3. The patient is assigned to the selected pathologist. 4. The test
 * and numerical values are valid.
 *
 * @author USER
 */
@WebServlet("/submit-result")
public class ResultSubmitServlet extends HttpServlet {

    private static final Logger LOGGER
            = Logger.getLogger(
                    ResultSubmitServlet.class.getName()
            );

    private static final double MAX_SUPPORTED_VALUE
            = 1_000_000.0;

    /**
     * These values must match the options in result-form.jsp.
     */
    private static final Set<String> SUPPORTED_TESTS
            = Set.of(
                    "Glucose",
                    "Potassium",
                    "Hemoglobin",
                    "WBC",
                    "Platelets"
            );

    private final AlertService alertService
            = new AlertService();

    private final PatientDAO patientDAO
            = new PatientDAO();

    private final PathologistDAO pathologistDAO
            = new PathologistDAO();

    /**
     * Processes a submitted laboratory result.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException when forwarding fails
     * @throws IOException when redirecting or forwarding fails
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        configureResponse(response);

        /*
         * Require an existing authenticated session.
         */
        HttpSession session
                = request.getSession(false);

        if (session == null) {

            redirectToLogin(
                    request,
                    response
            );

            return;
        }

        User loggedInUser
                = (User) session.getAttribute(
                        "loggedInUser"
                );

        if (loggedInUser == null) {

            redirectToLogin(
                    request,
                    response
            );

            return;
        }

        /*
         * Only administrators may capture laboratory results.
         */
        String role
                = normalizeRole(
                        loggedInUser.getRole()
                );

        if (!"ADMIN".equals(role)) {

            logAuditSafely(
                    loggedInUser,
                    "UNAUTHORIZED_ACCESS",
                    "Pathologist or other unauthorised user attempted "
                    + "to capture a laboratory result."
            );

            showAccessDenied(
                    request,
                    response,
                    "Only administrators may capture "
                    + "laboratory results."
            );

            return;
        }

        /*
         * Read and trim all submitted values.
         */
        String pathologistIdText
                = trim(
                        request.getParameter(
                                "pathologistId"
                        )
                );

        String patientIdText
                = trim(
                        request.getParameter(
                                "patientId"
                        )
                );

        String testName
                = trim(
                        request.getParameter(
                                "testName"
                        )
                );

        String resultValueText
                = trim(
                        request.getParameter(
                                "resultValue"
                        )
                );

        String unit
                = trim(
                        request.getParameter(
                                "unit"
                        )
                );

        String normalMinText
                = trim(
                        request.getParameter(
                                "normalMin"
                        )
                );

        String normalMaxText
                = trim(
                        request.getParameter(
                                "normalMax"
                        )
                );

        /*
         * Preserve submitted values before performing validation.
         */
        preserveSubmittedValues(
                request,
                pathologistIdText,
                patientIdText,
                testName,
                resultValueText,
                unit,
                normalMinText,
                normalMaxText
        );

        /*
         * Validate all required fields before parsing them.
         */
        String requiredFieldError
                = validateRequiredFields(
                        pathologistIdText,
                        patientIdText,
                        testName,
                        resultValueText,
                        unit,
                        normalMinText,
                        normalMaxText
                );

        if (requiredFieldError != null) {

            returnToForm(
                    request,
                    response,
                    requiredFieldError
            );

            return;
        }

        int pathologistId;
        int patientId;

        double resultValue;
        double normalMin;
        double normalMax;

        /*
         * Parse submitted identifiers and numerical values.
         */
        try {
            pathologistId
                    = parsePositiveInteger(
                            pathologistIdText,
                            "Select a valid pathologist."
                    );

            patientId
                    = parsePositiveInteger(
                            patientIdText,
                            "Select a valid patient."
                    );

            resultValue
                    = Double.parseDouble(
                            resultValueText
                    );

            normalMin
                    = Double.parseDouble(
                            normalMinText
                    );

            normalMax
                    = Double.parseDouble(
                            normalMaxText
                    );

        } catch (IllegalArgumentException exception) {

            returnToForm(
                    request,
                    response,
                    exception.getMessage()
            );

            return;
        }

        /*
         * Confirm that the selected pathologist exists and is active.
         */
        boolean activePathologist;

        try {
            activePathologist
                    = pathologistDAO.existsActive(
                            pathologistId
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to validate pathologist ID "
                    + pathologistId,
                    exception
            );

            returnToForm(
                    request,
                    response,
                    "The selected pathologist could not be verified."
            );

            return;
        }

        if (!activePathologist) {

            logAuditSafely(
                    loggedInUser,
                    "RESULT_SUBMISSION_REJECTED",
                    "Invalid or inactive pathologist selected. "
                    + "pathologistId="
                    + pathologistId
            );

            returnToForm(
                    request,
                    response,
                    "Select a valid active pathologist."
            );

            return;
        }

        /*
         * Confirm that the patient belongs to the selected active
         * pathologist.
         *
         * This check must be performed server-side even though the
         * patient dropdown is filtered in the browser.
         */
        boolean validAssignment;

        try {
            validAssignment
                    = patientDAO.isAssignedToPathologist(
                            patientId,
                            pathologistId
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to validate patient/pathologist assignment. "
                    + "patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId,
                    exception
            );

            returnToForm(
                    request,
                    response,
                    "The patient assignment could not be verified."
            );

            return;
        }

        if (!validAssignment) {

            logAuditSafely(
                    loggedInUser,
                    "RESULT_SUBMISSION_REJECTED",
                    "Patient/pathologist assignment mismatch. "
                    + "patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId
            );

            returnToForm(
                    request,
                    response,
                    "The selected patient is not assigned "
                    + "to the selected pathologist."
            );

            return;
        }

        /*
         * Retrieve the patient using the ownership-aware DAO method.
         */
        Patient selectedPatient;

        try {
            selectedPatient
                    = patientDAO.findByIdAndPathologistId(
                            patientId,
                            pathologistId
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve the verified patient. "
                    + "patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId,
                    exception
            );

            returnToForm(
                    request,
                    response,
                    "The selected patient could not be verified."
            );

            return;
        }

        if (selectedPatient == null) {

            logAuditSafely(
                    loggedInUser,
                    "RESULT_SUBMISSION_REJECTED",
                    "The verified patient record could not be retrieved. "
                    + "patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId
            );

            returnToForm(
                    request,
                    response,
                    "The selected patient could not be verified."
            );

            return;
        }

        /*
         * Validate the selected laboratory test.
         */
        if (!SUPPORTED_TESTS.contains(testName)) {

            returnToForm(
                    request,
                    response,
                    "Select a supported laboratory test."
            );

            return;
        }

        /*
         * Reject NaN and infinite values.
         */
        if (!Double.isFinite(resultValue)
                || !Double.isFinite(normalMin)
                || !Double.isFinite(normalMax)) {

            returnToForm(
                    request,
                    response,
                    "Result and reference-range values "
                    + "must be finite numbers."
            );

            return;
        }

        /*
         * Reject negative values.
         */
        if (resultValue < 0) {

            returnToForm(
                    request,
                    response,
                    "Result value cannot be negative."
            );

            return;
        }

        if (normalMin < 0
                || normalMax < 0) {

            returnToForm(
                    request,
                    response,
                    "Reference-range values cannot be negative."
            );

            return;
        }

        /*
         * Validate the reference range.
         */
        if (normalMin >= normalMax) {

            returnToForm(
                    request,
                    response,
                    "Normal minimum must be lower "
                    + "than normal maximum."
            );

            return;
        }

        /*
         * Apply a general upper bound.
         */
        if (resultValue > MAX_SUPPORTED_VALUE
                || normalMin > MAX_SUPPORTED_VALUE
                || normalMax > MAX_SUPPORTED_VALUE) {

            returnToForm(
                    request,
                    response,
                    "One or more numerical values exceed "
                    + "the supported limit."
            );

            return;
        }

        /*
         * Validate the measurement unit.
         */
        if (unit.isBlank()) {

            returnToForm(
                    request,
                    response,
                    "Enter the unit of measurement."
            );

            return;
        }

        if (unit.length() > 30) {

            returnToForm(
                    request,
                    response,
                    "The unit of measurement may not exceed "
                    + "30 characters."
            );

            return;
        }

        /*
         * Build the validated laboratory-result object.
         *
         * Pathologist ownership is determined through the selected
         * patient's assignment. The result does not independently
         * override that assignment.
         */
        LabResult result
                = new LabResult();

        result.setPatientId(patientId);
        result.setTestName(testName);
        result.setResultValue(resultValue);
        result.setUnit(unit);
        result.setNormalMin(normalMin);
        result.setNormalMax(normalMax);

        try {
            /*
             * Existing workflow coordinated by AlertService:
             *
             * 1. Retrieve the previous laboratory result.
             * 2. Evaluate the submitted result.
             * 3. Save the evaluated result.
             * 4. Generate an alert where required.
             * 5. Notify the patient's assigned pathologist.
             * 6. Update notification status.
             */
            String message
                    = alertService.processResult(result);

            logAuditSafely(
                    loggedInUser,
                    "RESULT_CAPTURED",
                    "Result captured for patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId
                    + ", testName="
                    + testName
                    + ", resultValue="
                    + resultValue
            );

            redirectToDashboard(
                    request,
                    response,
                    "msg",
                    message
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Laboratory result processing failed. "
                    + "patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId,
                    exception
            );

            logAuditSafely(
                    loggedInUser,
                    "RESULT_CAPTURE_FAILED",
                    "Result processing failed for patientId="
                    + patientId
                    + ", pathologistId="
                    + pathologistId
                    + ", testName="
                    + testName
            );

            returnToForm(
                    request,
                    response,
                    "The laboratory result could not be processed. "
                    + "Please review the information and try again."
            );
        }
    }

    /**
     * Rejects GET requests to the result-submission endpoint.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException servlet exception
     * @throws IOException input/output exception
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader(
                "Allow",
                "POST"
        );

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Laboratory results must be submitted "
                + "from the result-capture form."
        );
    }

    /**
     * Validates required request fields.
     *
     * @return validation message, or null when all required fields have values
     */
    private String validateRequiredFields(
            String pathologistId,
            String patientId,
            String testName,
            String resultValue,
            String unit,
            String normalMin,
            String normalMax) {

        if (pathologistId.isBlank()) {
            return "Select a pathologist.";
        }

        if (patientId.isBlank()) {
            return "Select a patient.";
        }

        if (testName.isBlank()) {
            return "Select a laboratory test.";
        }

        if (resultValue.isBlank()) {
            return "Enter the laboratory result value.";
        }

        if (unit.isBlank()) {
            return "Enter the unit of measurement.";
        }

        if (normalMin.isBlank()) {
            return "Enter the normal minimum value.";
        }

        if (normalMax.isBlank()) {
            return "Enter the normal maximum value.";
        }

        return null;
    }

    /**
     * Parses and validates a positive integer.
     *
     * @param value submitted value
     * @param errorMessage validation error message
     * @return positive integer
     */
    private int parsePositiveInteger(
            String value,
            String errorMessage) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        try {
            int parsedValue
                    = Integer.parseInt(
                            value.trim()
                    );

            if (parsedValue <= 0) {

                throw new IllegalArgumentException(
                        errorMessage
                );
            }

            return parsedValue;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    /**
     * Preserves submitted values following a validation failure.
     */
    private void preserveSubmittedValues(
            HttpServletRequest request,
            String pathologistId,
            String patientId,
            String testName,
            String resultValue,
            String unit,
            String normalMin,
            String normalMax) {

        request.setAttribute(
                "formPathologistId",
                pathologistId
        );

        request.setAttribute(
                "formPatientId",
                patientId
        );

        request.setAttribute(
                "formTestName",
                testName
        );

        request.setAttribute(
                "formResultValue",
                resultValue
        );

        request.setAttribute(
                "formUnit",
                unit
        );

        request.setAttribute(
                "formNormalMin",
                normalMin
        );

        request.setAttribute(
                "formNormalMax",
                normalMax
        );
    }

    /**
     * Returns the user to the result form while preserving submitted values and
     * reloading active pathologists.
     */
    private void returnToForm(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage)
            throws ServletException, IOException {

        request.setAttribute(
                "error",
                errorMessage
        );

        /*
         * Preserve values if this method is called before the normal
         * preservation block has run.
         */
        setAttributeIfMissing(
                request,
                "formPathologistId",
                request.getParameter("pathologistId")
        );

        setAttributeIfMissing(
                request,
                "formPatientId",
                request.getParameter("patientId")
        );

        setAttributeIfMissing(
                request,
                "formTestName",
                request.getParameter("testName")
        );

        setAttributeIfMissing(
                request,
                "formResultValue",
                request.getParameter("resultValue")
        );

        setAttributeIfMissing(
                request,
                "formUnit",
                request.getParameter("unit")
        );

        setAttributeIfMissing(
                request,
                "formNormalMin",
                request.getParameter("normalMin")
        );

        setAttributeIfMissing(
                request,
                "formNormalMax",
                request.getParameter("normalMax")
        );

        try {
            List<?> pathologists
                    = pathologistDAO.findAllActive();

            request.setAttribute(
                    "pathologists",
                    pathologists == null
                            ? Collections.emptyList()
                            : pathologists
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to reload active pathologists "
                    + "after result validation failure.",
                    exception
            );

            request.setAttribute(
                    "pathologists",
                    Collections.emptyList()
            );
        }

        /*
         * Patients remain dynamically loaded according to the
         * preserved pathologist selection.
         */
        request.setAttribute(
                "patients",
                Collections.emptyList()
        );

        request.getRequestDispatcher(
                "/result-form.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Sets an attribute only when the attribute is not already present.
     */
    private void setAttributeIfMissing(
            HttpServletRequest request,
            String attributeName,
            String value) {

        if (request.getAttribute(attributeName) != null) {
            return;
        }

        request.setAttribute(
                attributeName,
                trim(value)
        );
    }

    /**
     * Displays the branded HTTP 403 page.
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
     * Redirects an unauthenticated user to login.
     */
    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/index.jsp?error="
                + encode(
                        "Please login first."
                )
        );
    }

    /**
     * Redirects to the dashboard with a safely encoded message.
     */
    private void redirectToDashboard(
            HttpServletRequest request,
            HttpServletResponse response,
            String parameterName,
            String message)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/dashboard?"
                + parameterName
                + "="
                + encode(message)
        );
    }

    /**
     * Records an audit event without allowing an audit failure to interrupt the
     * result-capture workflow.
     */
    private void logAuditSafely(
            User user,
            String action,
            String details) {

        try {
            AuditService.log(
                    user,
                    action,
                    "LAB_RESULT",
                    null,
                    details
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record the result-capture audit event.",
                    exception
            );
        }
    }

    /**
     * Applies secure response headers.
     */
    private void configureResponse(
            HttpServletResponse response) {

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate, "
                + "max-age=0, private"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0L
        );

        response.setHeader(
                "X-Content-Type-Options",
                "nosniff"
        );
    }

    /**
     * Normalises a user role.
     */
    private String normalizeRole(String role) {

        return role == null
                ? ""
                : role.trim()
                        .toUpperCase(Locale.ROOT);
    }

    /**
     * Trims a request value and converts null to an empty string.
     */
    private String trim(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * URL-encodes redirect messages using UTF-8.
     */
    private String encode(String value) {

        return URLEncoder.encode(
                value == null
                        ? ""
                        : value,
                StandardCharsets.UTF_8
        );
    }
}
