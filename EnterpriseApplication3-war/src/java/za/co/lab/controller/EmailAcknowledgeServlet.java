package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;
import za.co.lab.util.EmailLinkUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/email-acknowledge")
public class EmailAcknowledgeServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    EmailAcknowledgeServlet.class.getName()
            );

    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        int alertId;
        long expiry;
        String signature;

        /*
         * Read and validate the signed-link parameters.
         */
        try {
            alertId = Integer.parseInt(
                    request.getParameter("alertId")
            );

            expiry = Long.parseLong(
                    request.getParameter("exp")
            );

            signature = request.getParameter("sig");

        } catch (NumberFormatException exception) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid acknowledgement request."
            );

            return;
        }

        /*
         * Reject missing or invalid parameter values.
         */
        if (alertId <= 0
                || expiry <= 0
                || signature == null
                || signature.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid acknowledgement request."
            );

            return;
        }

        /*
         * Verify that the acknowledgement link is authentic
         * and has not expired.
         */
        if (!EmailLinkUtil.isValid(
                alertId,
                "ack",
                expiry,
                signature)) {

            showAccessDenied(
                    request,
                    response,
                    "The acknowledgement link is invalid or has expired."
            );

            return;
        }

        /*
         * Require an existing authenticated session.
         */
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            redirectToLogin(
                    request,
                    response,
                    alertId,
                    expiry
            );

            return;
        }

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectToLogin(
                    request,
                    response,
                    alertId,
                    expiry
            );

            return;
        }

        /*
         * Only pathologists may acknowledge laboratory alerts.
         * Administrators and laboratory personnel may monitor
         * alerts but may not acknowledge them.
         */
        if (!"PATHOLOGIST".equalsIgnoreCase(
                loggedInUser.getRole())) {

            AuditService.log(
                    loggedInUser,
                    "UNAUTHORIZED_ALERT_ACKNOWLEDGEMENT",
                    "ALERT",
                    alertId,
                    "A non-pathologist attempted to acknowledge alert "
                    + alertId
                    + " through a signed email link."
            );

            showAccessDenied(
                    request,
                    response,
                    "Only the assigned pathologist may acknowledge "
                    + "this alert."
            );

            return;
        }

        /*
         * The authenticated user must be linked to a valid
         * pathologist profile.
         */
        if (loggedInUser.getPathologistId() == null
                || loggedInUser.getPathologistId() <= 0) {

            AuditService.log(
                    loggedInUser,
                    "ALERT_ACKNOWLEDGEMENT_REJECTED",
                    "ALERT",
                    alertId,
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
         * Ensure a usable pathologist display name is available.
         */
        String pathologistName =
                loggedInUser.getFullName();

        if (pathologistName == null
                || pathologistName.isBlank()) {

            pathologistName =
                    loggedInUser.getUsername();
        }

        /*
         * Acknowledge the alert through the ownership-aware
         * DAO method.
         *
         * The DAO verifies:
         *
         * 1. The alert exists.
         * 2. The alert is not already acknowledged.
         * 3. The patient is assigned to this pathologist.
         * 4. The pathologist profile is active.
         *
         * The DAO must preserve notification_status.
         */
        boolean acknowledged;

        try {
            acknowledged =
                    alertDAO.acknowledgeByPathologist(
                            alertId,
                            loggedInUser.getPathologistId(),
                            pathologistName
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to acknowledge alert ID "
                    + alertId,
                    exception
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The alert could not be acknowledged."
            );

            return;
        }

        /*
         * A false result means the alert:
         *
         * - does not exist;
         * - is already acknowledged;
         * - is not assigned to this pathologist; or
         * - belongs to an inactive pathologist profile.
         */
        if (!acknowledged) {

            AuditService.log(
                    loggedInUser,
                    "ALERT_ACKNOWLEDGEMENT_REJECTED",
                    "ALERT",
                    alertId,
                    "The alert was already acknowledged, "
                    + "was not assigned to the logged-in pathologist, "
                    + "or the pathologist profile is inactive."
            );

            showAccessDenied(
                    request,
                    response,
                    "This alert is not assigned to your pathologist "
                    + "profile, or it has already been acknowledged."
            );

            return;
        }

        /*
         * Record the successful acknowledgement in the audit log.
         */
        AuditService.log(
                loggedInUser,
                "ALERT_ACKNOWLEDGED",
                "ALERT",
                alertId,
                "Alert acknowledged by the assigned pathologist. "
                + "pathologistId="
                + loggedInUser.getPathologistId()
        );

        /*
         * Generate a signed view token and return the user to
         * the secure alert-review page.
         */
        String viewSignature =
                EmailLinkUtil.sign(
                        alertId,
                        "view",
                        expiry
                );

        String redirectUrl =
                request.getContextPath()
                + "/email-view"
                + "?alertId="
                + alertId
                + "&exp="
                + expiry
                + "&sig="
                + encode(viewSignature)
                + "&msg="
                + encode(
                        "Alert acknowledged successfully."
                );

        response.sendRedirect(redirectUrl);
    }

    /**
     * Redirects an unauthenticated user to the login page.
     *
     * The signed alert-review URL is stored in the session so
     * the application can return the user to the alert after
     * successful authentication.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param alertId alert identifier
     * @param expiry signed-link expiry value
     * @throws IOException when redirecting fails
     */
    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            int alertId,
            long expiry)
            throws IOException {

        String viewSignature =
                EmailLinkUtil.sign(
                        alertId,
                        "view",
                        expiry
                );

        String postLoginRedirect =
                request.getContextPath()
                + "/email-view"
                + "?alertId="
                + alertId
                + "&exp="
                + expiry
                + "&sig="
                + encode(viewSignature);

        HttpSession loginSession =
                request.getSession(true);

        loginSession.setAttribute(
                "postLoginRedirect",
                postLoginRedirect
        );

        response.sendRedirect(
                request.getContextPath()
                + "/index.jsp?error="
                + encode(
                        "Please login as the assigned pathologist."
                )
        );
    }

    /**
     * Displays the branded access-denied page while preserving
     * the correct HTTP 403 response status.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param message user-friendly access-denied message
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
     * URL-encodes a value using UTF-8.
     *
     * @param value value to encode
     * @return encoded value
     */
    private String encode(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}