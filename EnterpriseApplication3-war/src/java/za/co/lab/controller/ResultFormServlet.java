package za.co.lab.controller;

import za.co.lab.dao.PathologistDAO;
import za.co.lab.model.Pathologist;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;

import java.io.IOException;
import java.net.URLEncoder;
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
 * Displays the secure laboratory result-capture form.
 *
 * Only administrators may access this workflow.
 *
 * The servlet loads active pathologists for the first dropdown.
 * Assigned patients are loaded separately through the
 * patients-by-pathologist endpoint.
 *
 * @author USER
 */
@WebServlet("/result-form")
public class ResultFormServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    ResultFormServlet.class.getName()
            );

    private final PathologistDAO pathologistDAO =
            new PathologistDAO();

    /**
     * Displays the laboratory result-capture form.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException when forwarding fails
     * @throws IOException when redirecting or forwarding fails
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        configurePageResponse(response);

        /*
         * Require an existing authenticated session.
         */
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            redirectToLogin(
                    request,
                    response
            );

            return;
        }

        User loggedInUser =
                (User) session.getAttribute(
                        "loggedInUser"
                );

        if (loggedInUser == null) {
            redirectToLogin(
                    request,
                    response
            );

            return;
        }

        String role =
                normalizeRole(
                        loggedInUser.getRole()
                );

        /*
         * The application currently defines ADMIN and PATHOLOGIST.
         * Only ADMIN may capture laboratory results.
         */
        if (!"ADMIN".equals(role)) {

            logAuditSafely(
                    loggedInUser,
                    "UNAUTHORIZED_RESULT_FORM_ACCESS",
                    "A non-administrator attempted to access "
                    + "the laboratory result-capture form."
            );

            showAccessDenied(
                    request,
                    response,
                    "Only administrators may capture "
                    + "laboratory results."
            );

            return;
        }

        List<Pathologist> pathologists =
                loadActivePathologists(request);

        request.setAttribute(
                "pathologists",
                pathologists
        );

        /*
         * Patients are deliberately not loaded here.
         *
         * Patients are loaded after the administrator selects an
         * active pathologist.
         */
        request.setAttribute(
                "patients",
                Collections.emptyList()
        );

        /*
         * Preserve optional values supplied by another workflow.
         *
         * The normal validation-error workflow forwards from
         * ResultSubmitServlet and supplies these attributes directly.
         */
        preserveOptionalFormValues(request);

        request.getRequestDispatcher(
                "/result-form.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Prevents POST requests to the form-display endpoint.
     *
     * Result submission must use /submit-result.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException servlet exception
     * @throws IOException input/output exception
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        configurePageResponse(response);

        response.setHeader(
                "Allow",
                "GET"
        );

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Submit laboratory results through "
                + "the result submission endpoint."
        );
    }

    /**
     * Loads active pathologists for the result-capture form.
     *
     * @param request HTTP request
     * @return active pathologists or an empty list
     */
    private List<Pathologist> loadActivePathologists(
            HttpServletRequest request) {

        try {
            List<Pathologist> pathologists =
                    pathologistDAO.findAllActive();

            if (pathologists == null) {
                return Collections.emptyList();
            }

            return pathologists;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load active pathologists "
                    + "for the result-capture form.",
                    exception
            );

            request.setAttribute(
                    "error",
                    "The active pathologists could not be loaded. "
                    + "Please try again."
            );

            return Collections.emptyList();
        }
    }

    /**
     * Preserves optional query values when the display endpoint is
     * reached from another application flow.
     *
     * @param request HTTP request
     */
    private void preserveOptionalFormValues(
            HttpServletRequest request) {

        copyParameterIfAttributeMissing(
                request,
                "pathologistId",
                "formPathologistId"
        );

        copyParameterIfAttributeMissing(
                request,
                "patientId",
                "formPatientId"
        );

        copyParameterIfAttributeMissing(
                request,
                "testName",
                "formTestName"
        );

        copyParameterIfAttributeMissing(
                request,
                "resultValue",
                "formResultValue"
        );

        copyParameterIfAttributeMissing(
                request,
                "unit",
                "formUnit"
        );

        copyParameterIfAttributeMissing(
                request,
                "normalMin",
                "formNormalMin"
        );

        copyParameterIfAttributeMissing(
                request,
                "normalMax",
                "formNormalMax"
        );
    }

    /**
     * Copies a request parameter to a request attribute only when
     * the attribute has not already been supplied.
     *
     * @param request HTTP request
     * @param parameterName request parameter name
     * @param attributeName target request attribute name
     */
    private void copyParameterIfAttributeMissing(
            HttpServletRequest request,
            String parameterName,
            String attributeName) {

        if (request.getAttribute(attributeName) != null) {
            return;
        }

        String value =
                request.getParameter(parameterName);

        if (value == null) {
            return;
        }

        request.setAttribute(
                attributeName,
                value.trim()
        );
    }

    /**
     * Adds security and cache-control headers to the form page.
     *
     * @param response HTTP response
     */
    private void configurePageResponse(
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
     * Redirects an unauthenticated user to the login page.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException when redirecting fails
     */
    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/index.jsp?error="
                + encode(
                        "Please login as an administrator "
                        + "to capture laboratory results."
                )
        );
    }

    /**
     * Displays the context-aware HTTP 403 access-denied page.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param message user-facing error message
     * @throws ServletException when forwarding fails
     * @throws IOException when forwarding fails
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

        /*
         * This explanation is displayed in the
         * "Why am I seeing this?" section.
         */
        request.setAttribute(
                "accessReason",
                "Laboratory result capture is restricted to "
                + "administrators. Pathologists may review assigned "
                + "patients, results and alerts, acknowledge assigned "
                + "alerts, and generate authorised reports."
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
     * Records an audit event without allowing an audit failure to
     * interrupt the result-form workflow.
     *
     * @param user authenticated user
     * @param action audit action
     * @param details audit details
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
                    "Unable to record the result-form audit event.",
                    exception
            );
        }
    }

    /**
     * Normalises a user role.
     *
     * @param role user role
     * @return uppercase role or an empty string
     */
    private String normalizeRole(String role) {

        return role == null
                ? ""
                : role.trim()
                        .toUpperCase(Locale.ROOT);
    }

    /**
     * URL-encodes a value using UTF-8.
     *
     * @param value source value
     * @return encoded value
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