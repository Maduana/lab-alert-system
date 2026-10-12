/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.co.lab.controller;

import za.co.lab.dao.PathologistDAO;
import za.co.lab.dao.PatientDAO;
import za.co.lab.model.Patient;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Returns patients assigned to a selected active pathologist.
 *
 * The endpoint is used by the result-capture form to populate the patient
 * dropdown after an authorised user selects a pathologist.
 *
 * The response contains only:
 *
 * 1. Patient ID 2. Patient code 3. Patient display name
 *
 * @author USER
 */
@WebServlet("/patients-by-pathologist")
public class PatientsByPathologistServlet extends HttpServlet {

    private static final Logger LOGGER
            = Logger.getLogger(
                    PatientsByPathologistServlet.class.getName()
            );

    private final PatientDAO patientDAO
            = new PatientDAO();

    private final PathologistDAO pathologistDAO
            = new PathologistDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        configureJsonResponse(response);

        /*
         * Require an existing authenticated session.
         */
        HttpSession session
                = request.getSession(false);

        if (session == null) {
            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication is required."
            );

            return;
        }

        User loggedInUser
                = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication is required."
            );

            return;
        }

        String role
                = normaliseRole(
                        loggedInUser.getRole()
                );

        /*
         * Only roles permitted to capture results may use this
         * patient-lookup endpoint.
         *
         * Adjust LAB_PERSONNEL below if your application uses a
         * different role value, such as LABORATORY_PERSONNEL.
         */
        boolean isAdministrator
                = "ADMIN".equals(role);

        if (!isAdministrator) {

            logAuditSafely(
                    loggedInUser,
                    "UNAUTHORIZED_PATIENT_LOOKUP",
                    "A non-administrator attempted to retrieve "
                    + "patients by pathologist."
            );

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators may retrieve patients "
                    + "for laboratory result capture."
            );

            return;
        }

        int pathologistId;

        try {
            pathologistId
                    = parsePositiveInteger(
                            request.getParameter(
                                    "pathologistId"
                            )
                    );

        } catch (IllegalArgumentException exception) {

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );

            return;
        }

        /*
         * Reject missing, unknown, or inactive pathologists.
         */
        if (!pathologistDAO.existsActive(
                pathologistId)) {

            logAuditSafely(
                    loggedInUser,
                    "PATIENT_LOOKUP_REJECTED",
                    "Patient lookup rejected because pathologist ID "
                    + pathologistId
                    + " does not exist or is inactive."
            );

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "The selected pathologist does not exist "
                    + "or is inactive."
            );

            return;
        }

        List<Patient> patients;

        try {
            patients
                    = patientDAO.findAllByPathologistId(
                            pathologistId
                    );

            if (patients == null) {
                patients
                        = Collections.emptyList();
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve patients for pathologist ID "
                    + pathologistId,
                    exception
            );

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The assigned patients could not be retrieved."
            );

            return;
        }

        writeSuccessResponse(
                response,
                patients
        );
    }

    /**
     * Rejects POST requests because this endpoint provides a read-only lookup.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        configureJsonResponse(response);

        writeErrorResponse(
                response,
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "This patient lookup supports GET requests only."
        );
    }

    /**
     * Configures the response as non-cacheable UTF-8 JSON.
     */
    private void configureJsonResponse(
            HttpServletResponse response) {

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                "application/json"
        );

        /*
         * Patient lookup responses should not be stored by the
         * browser, proxies, or shared caches.
         */
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
     * Parses a positive integer parameter.
     *
     * @param value submitted parameter
     * @return positive integer
     */
    private int parsePositiveInteger(
            String value) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Select a pathologist."
            );
        }

        try {
            int parsedValue
                    = Integer.parseInt(
                            value.trim()
                    );

            if (parsedValue <= 0) {
                throw new IllegalArgumentException(
                        "Select a valid pathologist."
                );
            }

            return parsedValue;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "The selected pathologist is invalid."
            );
        }
    }

    /**
     * Writes a successful JSON response containing the assigned patients.
     *
     * Example:
     *
     * {
     * "success": true, "count": 2, "patients": [ { "id": 1, "patientCode":
     * "P001", "displayName": "P001 - Lebo Mokoena" } ] }
     */
    private void writeSuccessResponse(
            HttpServletResponse response,
            List<Patient> patients)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        StringBuilder json
                = new StringBuilder();

        json.append("{");
        json.append("\"success\":true,");
        json.append("\"count\":");
        json.append(patients.size());
        json.append(",");
        json.append("\"patients\":[");

        for (int index = 0;
                index < patients.size();
                index++) {

            Patient patient
                    = patients.get(index);

            if (patient == null) {
                continue;
            }

            if (index > 0) {
                json.append(",");
            }

            String patientCode
                    = safeText(
                            patient.getPatientCode()
                    );

            String patientName
                    = buildPatientName(patient);

            String displayName;

            if (patientCode.isEmpty()) {
                displayName = patientName;

            } else if (patientName.isEmpty()) {
                displayName = patientCode;

            } else {
                displayName
                        = patientCode
                        + " - "
                        + patientName;
            }

            json.append("{");

            json.append("\"id\":");
            json.append(patient.getId());
            json.append(",");

            json.append("\"patientCode\":\"");
            json.append(
                    escapeJson(patientCode)
            );
            json.append("\",");

            json.append("\"displayName\":\"");
            json.append(
                    escapeJson(displayName)
            );
            json.append("\"");

            json.append("}");
        }

        json.append("]");
        json.append("}");

        try (PrintWriter writer
                = response.getWriter()) {

            writer.write(
                    json.toString()
            );

            writer.flush();
        }
    }

    /**
     * Writes a consistent JSON error response.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        String json
                = "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(message)
                + "\""
                + "}";

        try (PrintWriter writer
                = response.getWriter()) {

            writer.write(json);
            writer.flush();
        }
    }

    /**
     * Builds a patient's display name.
     */
    private String buildPatientName(
            Patient patient) {

        if (patient == null) {
            return "";
        }

        String firstName
                = safeText(
                        patient.getFirstName()
                );

        String lastName
                = safeText(
                        patient.getLastName()
                );

        return (firstName
                + " "
                + lastName).trim();
    }

    /**
     * Escapes a value for safe JSON output.
     */
    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        StringBuilder escaped
                = new StringBuilder();

        for (int index = 0;
                index < value.length();
                index++) {

            char character
                    = value.charAt(index);

            switch (character) {

                case '"':
                    escaped.append("\\\"");
                    break;

                case '\\':
                    escaped.append("\\\\");
                    break;

                case '\b':
                    escaped.append("\\b");
                    break;

                case '\f':
                    escaped.append("\\f");
                    break;

                case '\n':
                    escaped.append("\\n");
                    break;

                case '\r':
                    escaped.append("\\r");
                    break;

                case '\t':
                    escaped.append("\\t");
                    break;

                case '<':
                    escaped.append("\\u003C");
                    break;

                case '>':
                    escaped.append("\\u003E");
                    break;

                case '&':
                    escaped.append("\\u0026");
                    break;

                default:

                    if (character < 32) {

                        escaped.append(
                                String.format(
                                        Locale.ROOT,
                                        "\\u%04x",
                                        (int) character
                                )
                        );

                    } else {
                        escaped.append(character);
                    }

                    break;
            }
        }

        return escaped.toString();
    }

    /**
     * Converts a nullable text value to safe trimmed text.
     */
    private String safeText(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Normalises the application role.
     */
    private String normaliseRole(
            String role) {

        return safeText(role)
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Records a security audit event without interrupting the lookup response
     * if audit logging itself fails.
     */
    private void logAuditSafely(
            User user,
            String action,
            String details) {

        try {
            AuditService.log(
                    user,
                    action,
                    "PATIENT_LOOKUP",
                    null,
                    details
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record patient-lookup audit event.",
                    exception
            );
        }
    }
}
