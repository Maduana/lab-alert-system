package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;
import za.co.lab.service.EmailService;

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

/**
 * Allows an authorised Administrator to resend an email reminder
 * for an overdue, unacknowledged laboratory alert.
 *
 * Reminder eligibility is enforced by AlertDAO:
 *
 * 1. The alert must be overdue.
 * 2. The alert must remain unacknowledged.
 * 3. The maximum reminder count must not be reached.
 * 4. The reminder cooldown period must have passed.
 *
 * @author USER
 */
@WebServlet("/alert-reminder")
public class AlertReminderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(
                    AlertReminderServlet.class.getName()
            );

    private final AlertDAO alertDAO =
            new AlertDAO();

    private final EmailService emailService =
            new EmailService();

    /**
     * Processes an Administrator reminder request.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException when request processing fails
     * @throws IOException when the response cannot be written
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setNoCacheHeaders(response);

        HttpSession session =
                request.getSession(false);

        User user =
                session == null
                        ? null
                        : (User) session.getAttribute(
                                "loggedInUser"
                        );

        /*
         * Authentication check.
         */
        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication is required."
            );

            return;
        }

        /*
         * Only an Administrator may resend an alert reminder.
         */
        if (!isAdministrator(user)) {

            safeAuditLog(
                    user,
                    "UNAUTHORIZED_ALERT_REMINDER",
                    "ALERT",
                    null,
                    "A non-administrator attempted to resend "
                    + "an overdue alert reminder."
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only an Administrator may resend alert "
                    + "reminders."
            );

            return;
        }

        /*
         * Validate the submitted alert identifier.
         */
        int alertId =
                parsePositiveInteger(
                        request.getParameter(
                                "alertId"
                        )
                );

        if (alertId <= 0) {

            safeAuditLog(
                    user,
                    "ALERT_REMINDER_REJECTED",
                    "ALERT",
                    null,
                    "The reminder request contained an invalid "
                    + "alert identifier."
            );

            redirectToAlerts(
                    request,
                    response,
                    "error",
                    "Invalid alert identifier."
            );

            return;
        }

        /*
         * AlertDAO verifies that the alert:
         *
         * - exists;
         * - is overdue;
         * - remains unacknowledged;
         * - is below the maximum reminder limit; and
         * - is outside the reminder cooldown period.
         */
        Alert alert =
                alertDAO.findEligibleReminderAlert(
                        alertId
                );

        if (alert == null) {

            safeAuditLog(
                    user,
                    "ALERT_REMINDER_REJECTED",
                    "ALERT",
                    alertId,
                    "The alert was not eligible for another "
                    + "reminder."
            );

            redirectToAlerts(
                    request,
                    response,
                    "error",
                    "The alert is not eligible for a reminder. "
                    + "It may already be acknowledged, not yet "
                    + "overdue, within the cooldown period, or "
                    + "at the maximum reminder limit."
            );

            return;
        }

        /*
         * Send the reminder before updating reminder tracking.
         *
         * A failed email delivery must not increase the reminder
         * count.
         */
        boolean sent;

        try {
            sent =
                    emailService.sendOverdueReminder(
                            alert
                    );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unexpected reminder-email failure for alert ID "
                    + alertId,
                    exception
            );

            sent =
                    false;
        }

        if (!sent) {

            safeAuditLog(
                    user,
                    "ALERT_REMINDER_FAILED",
                    "ALERT",
                    alertId,
                    "The overdue alert reminder email could not "
                    + "be delivered."
            );

            redirectToAlerts(
                    request,
                    response,
                    "error",
                    "The reminder email could not be sent. "
                    + "Please check the SMTP configuration and "
                    + "try again."
            );

            return;
        }

        /*
         * Record the successfully delivered reminder.
         *
         * AlertDAO repeats the reminder eligibility checks. This
         * reduces rapid or duplicate reminder updates.
         */
        boolean recorded =
                alertDAO.recordReminderSent(
                        alertId
                );

        if (!recorded) {

            LOGGER.log(
                    Level.WARNING,
                    "Reminder email was sent for alert ID {0}, "
                    + "but reminder tracking could not be updated.",
                    alertId
            );

            safeAuditLog(
                    user,
                    "ALERT_REMINDER_RECORD_FAILED",
                    "ALERT",
                    alertId,
                    "The reminder email was sent, but reminder "
                    + "tracking could not be updated."
            );

            redirectToAlerts(
                    request,
                    response,
                    "error",
                    "The reminder email was sent, but its tracking "
                    + "record could not be updated. Do not resend "
                    + "immediately; check the audit log first."
            );

            return;
        }

        /*
         * Record the successful reminder action.
         */
        safeAuditLog(
                user,
                "ALERT_REMINDER_SENT",
                "ALERT",
                alertId,
                "An overdue alert reminder was sent successfully."
        );

        redirectToAlerts(
                request,
                response,
                "msg",
                "Reminder email sent successfully for alert #"
                + alertId
                + "."
        );
    }

    /**
     * Rejects direct GET requests.
     *
     * Reminder delivery changes application state and must
     * therefore use POST.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException when the response cannot be written
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        setNoCacheHeaders(response);

        response.setHeader(
                "Allow",
                "POST"
        );

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Use POST for alert-reminder requests."
        );
    }

    /**
     * Checks whether the authenticated user has an Administrator
     * role.
     *
     * @param user authenticated user
     * @return true when Administrator access is permitted
     */
    private boolean isAdministrator(
            User user) {

        if (user == null
                || user.getRole() == null) {

            return false;
        }

        String role =
                user.getRole().trim();

        return "ADMIN".equalsIgnoreCase(role)
                || "ADMINISTRATOR".equalsIgnoreCase(role)
                || "LABORATORY_ADMINISTRATOR"
                        .equalsIgnoreCase(role);
    }

    /**
     * Parses a positive integer request parameter.
     *
     * @param value submitted request value
     * @return positive integer, or -1 when invalid
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
     * Redirects to the alerts page with a safely encoded message.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param parameterName msg or error
     * @param message display message
     * @throws IOException when redirection fails
     */
    private void redirectToAlerts(
            HttpServletRequest request,
            HttpServletResponse response,
            String parameterName,
            String message)
            throws IOException {

        String safeParameterName =
                "msg".equals(parameterName)
                        ? "msg"
                        : "error";

        String encodedMessage =
                URLEncoder.encode(
                        message == null
                                ? ""
                                : message,
                        StandardCharsets.UTF_8
                );

        response.sendRedirect(
                request.getContextPath()
                + "/alerts?"
                + safeParameterName
                + "="
                + encodedMessage
        );
    }

    /**
     * Records an audit event without allowing an audit failure to
     * interrupt the reminder workflow.
     *
     * @param user authenticated user
     * @param action audit action
     * @param entityType affected entity type
     * @param entityId affected alert identifier
     * @param details audit details
     */
    private void safeAuditLog(
            User user,
            String action,
            String entityType,
            Integer entityId,
            String details) {

        try {
            AuditService.log(
                    user,
                    action,
                    entityType,
                    entityId,
                    details
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record audit event "
                    + action
                    + " for alert ID "
                    + entityId,
                    exception
            );
        }
    }

    /**
     * Prevents sensitive action responses from being cached.
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