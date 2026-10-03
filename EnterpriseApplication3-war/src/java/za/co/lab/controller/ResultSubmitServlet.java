package za.co.lab.controller;

import za.co.lab.dao.PatientDAO;
import za.co.lab.model.LabResult;
import za.co.lab.model.Patient;
import za.co.lab.model.User;
import za.co.lab.service.AlertService;
import za.co.lab.service.AuditService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

@WebServlet("/submit-result")
public class ResultSubmitServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ResultSubmitServlet.class.getName());

    private static final double MAX_SUPPORTED_VALUE = 1_000_000.0;

    /*
     * These values must match the options in result-form.jsp.
     */
    private static final Set<String> SUPPORTED_TESTS = Set.of(
            "Glucose",
            "Potassium",
            "Hemoglobin",
            "WBC",
            "Platelets"
    );

    private final AlertService alertService = new AlertService();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        /*
         * Check the existing authenticated session without creating
         * a new session for an unauthenticated request.
         */
        HttpSession session = request.getSession(false);

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

        /*
         * Pathologists may review and acknowledge assigned alerts,
         * but may not capture laboratory results.
         */
        if ("PATHOLOGIST".equalsIgnoreCase(loggedInUser.getRole())) {

            AuditService.log(
                    loggedInUser,
                    "UNAUTHORIZED_ACCESS",
                    "LAB_RESULT",
                    null,
                    "Pathologist attempted to capture a laboratory result."
            );

            redirectToDashboard(
                    request,
                    response,
                    "error",
                    "Pathologists may not capture laboratory results."
            );

            return;
        }

        /*
         * Read and trim all submitted values.
         */
        String patientIdText =
                trim(request.getParameter("patientId"));

        String testName =
                trim(request.getParameter("testName"));

        String resultValueText =
                trim(request.getParameter("resultValue"));

        String unit =
                trim(request.getParameter("unit"));

        String normalMinText =
                trim(request.getParameter("normalMin"));

        String normalMaxText =
                trim(request.getParameter("normalMax"));

        /*
         * Preserve values so that the form can display them again
         * when validation fails.
         */
        preserveSubmittedValues(
                request,
                patientIdText,
                testName,
                resultValueText,
                unit,
                normalMinText,
                normalMaxText
        );

        /*
         * Validate required fields before parsing.
         */
        String requiredFieldError = validateRequiredFields(
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

        int patientId;
        double resultValue;
        double normalMin;
        double normalMax;

        /*
         * Safely parse submitted numerical values.
         */
        try {
            patientId = Integer.parseInt(patientIdText);
            resultValue = Double.parseDouble(resultValueText);
            normalMin = Double.parseDouble(normalMinText);
            normalMax = Double.parseDouble(normalMaxText);

        } catch (NumberFormatException exception) {

            returnToForm(
                    request,
                    response,
                    "Patient, result and reference-range values must be valid numbers."
            );

            return;
        }

        /*
         * Validate the patient identifier.
         */
        if (patientId <= 0) {
            returnToForm(
                    request,
                    response,
                    "Select a valid patient."
            );
            return;
        }

        /*
         * Confirm that the selected patient exists.
         *
         * This uses findAll() because that method is already known
         * to exist in the current PatientDAO implementation.
         */
        Patient selectedPatient;

        try {
            selectedPatient = findPatient(patientId);

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to validate the selected patient.",
                    exception
            );

            returnToForm(
                    request,
                    response,
                    "The selected patient could not be validated."
            );

            return;
        }

        if (selectedPatient == null) {
            returnToForm(
                    request,
                    response,
                    "The selected patient does not exist."
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
         * Reject special floating-point values such as NaN
         * and infinity.
         */
        if (!Double.isFinite(resultValue)
                || !Double.isFinite(normalMin)
                || !Double.isFinite(normalMax)) {

            returnToForm(
                    request,
                    response,
                    "Result and reference-range values must be finite numbers."
            );

            return;
        }

        /*
         * Laboratory values and reference ranges may not be negative
         * in the current prototype.
         */
        if (resultValue < 0) {
            returnToForm(
                    request,
                    response,
                    "Result value cannot be negative."
            );
            return;
        }

        if (normalMin < 0 || normalMax < 0) {
            returnToForm(
                    request,
                    response,
                    "Reference-range values cannot be negative."
            );
            return;
        }

        /*
         * Validate the relationship between minimum and maximum.
         */
        if (normalMin >= normalMax) {
            returnToForm(
                    request,
                    response,
                    "Normal minimum must be lower than normal maximum."
            );
            return;
        }

        /*
         * Apply a general upper bound to prevent unrealistic or
         * deliberately excessive values from being processed.
         */
        if (resultValue > MAX_SUPPORTED_VALUE
                || normalMin > MAX_SUPPORTED_VALUE
                || normalMax > MAX_SUPPORTED_VALUE) {

            returnToForm(
                    request,
                    response,
                    "One or more numerical values exceed the supported limit."
            );

            return;
        }

        /*
         * Validate the unit.
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
                    "The unit of measurement may not exceed 30 characters."
            );
            return;
        }

        /*
         * Build the validated laboratory-result object.
         */
        LabResult result = new LabResult();

        result.setPatientId(patientId);
        result.setTestName(testName);
        result.setResultValue(resultValue);
        result.setUnit(unit);
        result.setNormalMin(normalMin);
        result.setNormalMax(normalMax);

        try {
            /*
             * Existing core workflow:
             *
             * 1. Retrieve previous laboratory result.
             * 2. Evaluate the submitted result through the Rule Engine.
             * 3. Save the evaluated laboratory result.
             * 4. Generate an alert where required.
             * 5. Send the pathologist email where required.
             * 6. Update notification status.
             *
             * These operations are coordinated by AlertService.
             */
            String message = alertService.processResult(result);

            /*
             * Record successful result capture.
             */
            AuditService.log(
                    loggedInUser,
                    "RESULT_CAPTURED",
                    "LAB_RESULT",
                    null,
                    "Result captured for patientId="
                    + result.getPatientId()
                    + ", testName="
                    + result.getTestName()
                    + ", resultValue="
                    + result.getResultValue()
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
                    "Laboratory result processing failed for patient ID "
                    + patientId,
                    exception
            );

            /*
             * Record the failed processing attempt without exposing
             * technical details to the user.
             */
            AuditService.log(
                    loggedInUser,
                    "RESULT_CAPTURE_FAILED",
                    "LAB_RESULT",
                    null,
                    "Result processing failed for patientId="
                    + patientId
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
     * Finds a patient from the existing PatientDAO result.
     *
     * @param patientId patient identifier
     * @return matching patient or null
     */
    private Patient findPatient(int patientId) {

        List<Patient> patients = patientDAO.findAll();

        if (patients == null) {
            return null;
        }

        for (Patient patient : patients) {
            if (patient != null && patient.getId() == patientId) {
                return patient;
            }
        }

        return null;
    }

    /**
     * Validates that required request values were supplied.
     *
     * @return validation message or null when valid
     */
    private String validateRequiredFields(
            String patientId,
            String testName,
            String resultValue,
            String unit,
            String normalMin,
            String normalMax) {

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
     * Preserves submitted values following validation failure.
     */
    private void preserveSubmittedValues(
            HttpServletRequest request,
            String patientId,
            String testName,
            String resultValue,
            String unit,
            String normalMin,
            String normalMax) {

        request.setAttribute("formPatientId", patientId);
        request.setAttribute("formTestName", testName);
        request.setAttribute("formResultValue", resultValue);
        request.setAttribute("formUnit", unit);
        request.setAttribute("formNormalMin", normalMin);
        request.setAttribute("formNormalMax", normalMax);
    }

    /**
     * Forwards the user back to the form with a validation message
     * and a refreshed patient list.
     */
    private void returnToForm(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("error", errorMessage);

        try {
            request.setAttribute(
                    "patients",
                    patientDAO.findAll()
            );

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to reload patients after validation failure.",
                    exception
            );

            request.setAttribute(
                    "patients",
                    List.of()
            );
        }

        request.getRequestDispatcher("/result-form.jsp")
                .forward(request, response);
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
                + encode("Please login first.")
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
     * Trims a request value and converts null to an empty string.
     */
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * URL-encodes redirect messages using UTF-8.
     */
    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}