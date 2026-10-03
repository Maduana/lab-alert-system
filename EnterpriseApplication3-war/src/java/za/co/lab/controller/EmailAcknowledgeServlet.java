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

    private static final Logger LOGGER
            = Logger.getLogger(EmailAcknowledgeServlet.class.getName());

    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        int alertId;
        long expiry;
        String signature;

        /*
         * Validate the submitted request parameters.
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
         * Validate the signed acknowledgement link.
         */
        if (!EmailLinkUtil.isValid(
                alertId,
                "ack",
                expiry,
                signature)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "The acknowledgement link is invalid or expired."
            );
            return;
        }

        /*
         * Require an existing authenticated session.
         */
        HttpSession session = request.getSession(false);

        if (session == null) {
            redirectToLogin(
                    request,
                    response,
                    alertId,
                    expiry
            );
            return;
        }

        User loggedInUser
                = (User) session.getAttribute("loggedInUser");

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
         * Only Pathologists may acknowledge laboratory alerts.
         * Administrators and Laboratory Personnel may monitor alerts
         * but may not acknowledge them.
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

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only the assigned pathologist may acknowledge this alert."
            );
            return;
        }

        /*
         * The user account must be linked to a Pathologist profile.
         */
        if (loggedInUser.getPathologistId() == null
                || loggedInUser.getPathologistId() <= 0) {

            AuditService.log(
                    loggedInUser,
                    "ALERT_ACKNOWLEDGEMENT_REJECTED",
                    "ALERT",
                    alertId,
                    "The Pathologist account is not linked "
                    + "to a valid Pathologist profile."
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "The logged-in account is not linked "
                    + "to a valid pathologist profile."
            );
            return;
        }

        /*
         * Use the ownership-aware DAO method.
         *
         * This method should acknowledge the alert only when the
         * patient is assigned to the logged-in Pathologist.
         */
        boolean acknowledged;

        try {
            acknowledged
                    = alertDAO.acknowledgeByPathologist(
                            alertId,
                            loggedInUser.getPathologistId(),
                            loggedInUser.getFullName()
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to acknowledge alert ID " + alertId,
                    exception
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The alert could not be acknowledged."
            );
            return;
        }

        /*
         * A false result means that the alert does not exist,
         * is already acknowledged, or is not assigned to the
         * logged-in Pathologist.
         */
        if (!acknowledged) {

            AuditService.log(
                    loggedInUser,
                    "ALERT_ACKNOWLEDGEMENT_REJECTED",
                    "ALERT",
                    alertId,
                    "Alert acknowledgement was rejected because "
                    + "the alert was already acknowledged or was not "
                    + "assigned to the logged-in Pathologist."
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not allowed to acknowledge this alert, "
                    + "or the alert has already been acknowledged."
            );
            return;
        }

        /*
         * Record successful acknowledgement.
         */
        AuditService.log(
                loggedInUser,
                "ALERT_ACKNOWLEDGED",
                "ALERT",
                alertId,
                "Alert acknowledged by assigned pathologist. "
                + "pathologistId="
                + loggedInUser.getPathologistId()
        );

        /*
         * Create a new signed view token and redirect back to the
         * secure alert-review page.
         */
        String viewSignature
                = EmailLinkUtil.sign(
                        alertId,
                        "view",
                        expiry
                );

        String redirectUrl
                = request.getContextPath()
                + "/email-view"
                + "?alertId=" + alertId
                + "&exp=" + expiry
                + "&sig=" + encode(viewSignature)
                + "&msg=" + encode(
                        "Alert acknowledged successfully."
                );

        response.sendRedirect(redirectUrl);
    }

    /**
     * Redirects an unauthenticated user to the login page and stores the secure
     * review page as the post-login destination.
     */
    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            int alertId,
            long expiry)
            throws IOException {

        String viewSignature
                = EmailLinkUtil.sign(
                        alertId,
                        "view",
                        expiry
                );

        String postLoginRedirect
                = request.getContextPath()
                + "/email-view"
                + "?alertId=" + alertId
                + "&exp=" + expiry
                + "&sig=" + encode(viewSignature);

        HttpSession loginSession
                = request.getSession(true);

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
     * URL-encodes redirect parameters using UTF-8.
     */
    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}
