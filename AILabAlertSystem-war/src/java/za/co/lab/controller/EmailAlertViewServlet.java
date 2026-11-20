package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.dao.LabResultDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.LabResult;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;
import za.co.lab.util.EmailLinkUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Displays an alert reached through a signed, time-limited email
 * link.
 *
 * The Servlet:
 *
 * 1. Validates the alert ID, expiry value, and signature.
 * 2. Redirects unauthenticated users to login.
 * 3. Restricts Pathologists to assigned patients.
 * 4. Allows Administrators to monitor an alert.
 * 5. Generates a separate signed acknowledgement action.
 *
 * @author USER
 */
@WebServlet(name = "EmailAlertViewServlet",
        urlPatterns = {"/email-view"})
public class EmailAlertViewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(
                    EmailAlertViewServlet.class.getName()
            );

    private final AlertDAO alertDAO =
            new AlertDAO();

    private final LabResultDAO labResultDAO =
            new LabResultDAO();

    /**
     * Handles a signed alert-review link.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException when forwarding fails
     * @throws IOException when the response cannot be written
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setNoCacheHeaders(response);

        /*
         * Parse and validate required request parameters.
         */
        int alertId =
                parsePositiveInteger(
                        request.getParameter("alertId")
                );

        long expiry =
                parsePositiveLong(
                        request.getParameter("exp")
                );

        String signature =
                trimToNull(
                        request.getParameter("sig")
                );

        if (alertId <= 0
                || expiry <= 0
                || signature == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "The alert-review link is incomplete or invalid."
            );

            return;
        }

        /*
         * Reject an expired link separately so the response clearly
         * distinguishes expiry from tampering.
         */
        if (expiry < System.currentTimeMillis()) {

            LOGGER.log(
                    Level.WARNING,
                    "Expired email-review link rejected for alert "
                    + "ID {0}.",
                    alertId
            );

            response.sendError(
                    HttpServletResponse.SC_GONE,
                    "This alert-review link has expired."
            );

            return;
        }

        /*
         * Validate the signature before exposing alert information.
         */
        boolean validSignature;

        try {
            validSignature =
                    EmailLinkUtil.isValid(
                            alertId,
                            "view",
                            expiry,
                            signature
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to validate the signed review link for "
                    + "alert ID "
                    + alertId,
                    exception
            );

            validSignature =
                    false;
        }

        if (!validSignature) {

            LOGGER.log(
                    Level.WARNING,
                    "Invalid or modified email-review link rejected "
                    + "for alert ID {0}.",
                    alertId
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "This alert-review link is invalid or has been "
                    + "modified."
            );

            return;
        }

        /*
         * Confirm that the signed alert still exists before login
         * redirection or result retrieval.
         */
        Alert alert =
                alertDAO.findById(
                        alertId
                );

        if (alert == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The requested alert could not be found."
            );

            return;
        }

        /*
         * Retrieve the current authenticated session without
         * creating a new session unnecessarily.
         */
        HttpSession session =
                request.getSession(false);

        User user =
                session == null
                        ? null
                        : (User) session.getAttribute(
                                "loggedInUser"
                        );

        if (user == null) {

            /*
             * A session is required to preserve the validated
             * destination through login.
             */
            HttpSession loginSession =
                    request.getSession(true);

            loginSession.setAttribute(
                    "postLoginRedirect",
                    buildCurrentRelativeUrl(request)
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/index.jsp?error="
                    + "Please+login+to+view+the+alert"
            );

            return;
        }

        String role =
                user.getRole() == null
                        ? ""
                        : user.getRole().trim();

        boolean pathologist =
                "PATHOLOGIST".equalsIgnoreCase(role);

        boolean administrator =
                "ADMIN".equalsIgnoreCase(role)
                || "ADMINISTRATOR".equalsIgnoreCase(role)
                || "LABORATORY_ADMINISTRATOR"
                        .equalsIgnoreCase(role);

        /*
         * Reject unsupported roles.
         */
        if (!pathologist && !administrator) {

            AuditService.log(
                    user,
                    "EMAIL_ALERT_VIEW_DENIED",
                    "ALERT",
                    alertId,
                    "The authenticated role was not authorised "
                    + "to view an email alert."
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Your account is not authorised to view this "
                    + "alert."
            );

            return;
        }

        /*
         * A Pathologist may view only an alert belonging to an
         * assigned patient.
         */
        if (pathologist) {

            Integer pathologistId =
                    user.getPathologistId();

            if (pathologistId == null
                    || pathologistId <= 0
                    || !alertDAO.isOwnedByPathologist(
                            alertId,
                            pathologistId
                    )) {

                AuditService.log(
                        user,
                        "EMAIL_ALERT_VIEW_DENIED",
                        "ALERT",
                        alertId,
                        "The Pathologist attempted to view an alert "
                        + "outside the assigned patient scope."
                );

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You are not authorised to view this alert."
                );

                return;
            }
        }

        LabResult result =
                labResultDAO.findById(
                        alert.getResultId()
                );

        if (result == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The laboratory result linked to this alert "
                    + "could not be found."
            );

            return;
        }

        /*
         * Generate a separate acknowledgement signature. The
         * acknowledgement signature binds the same alert ID and
         * expiry value to the "ack" action.
         */
        String acknowledgementSignature;

        try {
            acknowledgementSignature =
                    EmailLinkUtil.sign(
                            alertId,
                            "ack",
                            expiry
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to generate acknowledgement signature "
                    + "for alert ID "
                    + alertId,
                    exception
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The alert acknowledgement action could not be "
                    + "prepared."
            );

            return;
        }

        /*
         * Supply the review information to the JSP.
         */
        request.setAttribute(
                "alert",
                alert
        );

        request.setAttribute(
                "result",
                result
        );

        request.setAttribute(
                "ackUrl",
                request.getContextPath()
                + "/email-acknowledge"
        );

        request.setAttribute(
                "exp",
                expiry
        );

        request.setAttribute(
                "sig",
                acknowledgementSignature
        );

        AuditService.log(
                user,
                "EMAIL_ALERT_VIEWED",
                "ALERT",
                alertId,
                "The signed email alert-review page was opened."
        );

        request.getRequestDispatcher(
                "/email-alert-view.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Rejects unsupported POST requests.
     *
     * Viewing a signed alert is a read-only GET operation.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException when the response cannot be written
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        setNoCacheHeaders(response);

        response.setHeader(
                "Allow",
                "GET"
        );

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Use GET to open an alert-review link."
        );
    }

    /**
     * Builds the relative signed URL that should be restored after
     * successful login.
     *
     * @param request HTTP request
     * @return context-relative URL with its query string
     */
    private String buildCurrentRelativeUrl(
            HttpServletRequest request) {

        StringBuilder destination =
                new StringBuilder(
                        request.getRequestURI()
                );

        String queryString =
                request.getQueryString();

        if (queryString != null
                && !queryString.isBlank()) {

            destination.append("?")
                    .append(queryString);
        }

        return destination.toString();
    }

    /**
     * Parses a positive integer.
     *
     * @param value request value
     * @return positive integer, or -1
     */
    private int parsePositiveInteger(
            String value) {

        if (value == null
                || value.isBlank()) {

            return -1;
        }

        try {
            int parsedValue =
                    Integer.parseInt(
                            value.trim()
                    );

            return parsedValue > 0
                    ? parsedValue
                    : -1;

        } catch (NumberFormatException exception) {

            return -1;
        }
    }

    /**
     * Parses a positive long value.
     *
     * @param value request value
     * @return positive long, or -1
     */
    private long parsePositiveLong(
            String value) {

        if (value == null
                || value.isBlank()) {

            return -1L;
        }

        try {
            long parsedValue =
                    Long.parseLong(
                            value.trim()
                    );

            return parsedValue > 0
                    ? parsedValue
                    : -1L;

        } catch (NumberFormatException exception) {

            return -1L;
        }
    }

    /**
     * Returns trimmed text or null.
     *
     * @param value source text
     * @return trimmed text or null
     */
    private String trimToNull(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    /**
     * Prevents secure review content from being cached.
     *
     * @param response HTTP response
     */
    private void setNoCacheHeaders(
            HttpServletResponse response) {

        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );
    }
}