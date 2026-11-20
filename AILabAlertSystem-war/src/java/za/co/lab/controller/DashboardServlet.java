package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.dao.LabResultDAO;
import za.co.lab.dao.PatientDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.User;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Loads the role-aware AILabAlertSystem dashboard.
 *
 * Administrators receive system-wide operational metrics.
 * Pathologists receive metrics restricted to assigned patients.
 *
 * @author USER
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DashboardServlet.class.getName()
            );

    private static final long DUE_SOON_MILLISECONDS =
            5L * 60L * 1000L;

    private final LabResultDAO labResultDAO =
            new LabResultDAO();

    private final AlertDAO alertDAO =
            new AlertDAO();

    private final PatientDAO patientDAO =
            new PatientDAO();

    /**
     * Loads the dashboard according to the authenticated user's role.
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

        /*
         * Prevent sensitive dashboard information from being stored
         * in the browser cache.
         */
        setNoCacheHeaders(response);

        HttpSession session =
                request.getSession(false);

        User user =
                session == null
                        ? null
                        : (User) session.getAttribute(
                                "loggedInUser"
                        );

        if (user == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/index.jsp?error=Please+login+first"
            );

            return;
        }

        String role =
                safeText(
                        user.getRole()
                );

        try {
            /*
             * Synchronise overdue-alert flags before retrieving
             * dashboard records and calculating KPI values.
             */
            alertDAO.synchroniseEscalationFlags();

            if ("PATHOLOGIST".equalsIgnoreCase(role)) {

                loadPathologistDashboard(
                        request,
                        response,
                        user
                );

            } else if ("ADMIN".equalsIgnoreCase(role)
                    || "ADMINISTRATOR".equalsIgnoreCase(role)
                    || "LABORATORY_ADMINISTRATOR"
                            .equalsIgnoreCase(role)) {

                loadAdministratorDashboard(
                        request
                );

            } else {

                LOGGER.log(
                        Level.WARNING,
                        "Dashboard access rejected for unsupported "
                        + "role: {0}",
                        role
                );

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Your account is not authorised to access "
                        + "this dashboard."
                );

                return;
            }

            request.getRequestDispatcher(
                    "/dashboard.jsp"
            ).forward(
                    request,
                    response
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load dashboard for user role "
                    + role,
                    exception
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The dashboard could not be loaded."
            );
        }
    }

    /**
     * Loads system-wide dashboard information for an administrator.
     *
     * @param request HTTP request
     */
    private void loadAdministratorDashboard(
            HttpServletRequest request) {

        var latestResults =
                labResultDAO.findLatestResults();

        List<Alert> alerts =
                alertDAO.findAll();

        var patients =
                patientDAO.findAll();

        request.setAttribute(
                "latestResults",
                latestResults
        );

        request.setAttribute(
                "alerts",
                alerts
        );

        request.setAttribute(
                "patients",
                patients
        );

        request.setAttribute(
                "totalPatients",
                patients.size()
        );

        request.setAttribute(
                "totalResults",
                latestResults.size()
        );

        request.setAttribute(
                "totalAlerts",
                alerts.size()
        );

        int pendingCriticalCount =
                alertDAO.countPendingCritical();

        request.setAttribute(
                "criticalAlerts",
                pendingCriticalCount
        );

        request.setAttribute(
                "pendingCriticalCount",
                pendingCriticalCount
        );

        /*
         * System-wide operational KPIs.
         */
        request.setAttribute(
                "overdueAlerts",
                alertDAO.countOverdueAlerts()
        );

        request.setAttribute(
                "dueSoonAlerts",
                alertDAO.countDueSoonAlerts()
        );

        request.setAttribute(
                "acknowledgementRate",
                alertDAO.getAcknowledgementRatePercentage()
        );

        request.setAttribute(
                "averageAcknowledgementMinutes",
                alertDAO.getAverageAcknowledgementMinutes()
        );

        request.setAttribute(
                "emailSuccessRate",
                alertDAO.getEmailSuccessRatePercentage()
        );

        /*
         * Additional dashboard values calculated from the loaded
         * alert list.
         */
        request.setAttribute(
                "acknowledgedAlerts",
                countAcknowledgedAlerts(alerts)
        );

        request.setAttribute(
                "awaitingReviewAlerts",
                countAwaitingReviewAlerts(alerts)
        );

        request.setAttribute(
                "escalatedAlerts",
                countEscalatedAlerts(alerts)
        );

        request.setAttribute(
                "pathologistView",
                false
        );
    }

    /**
     * Loads assignment-scoped dashboard information for a
     * Pathologist.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param user authenticated user
     * @throws IOException when the error response cannot be written
     */
    private void loadPathologistDashboard(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws IOException {

        Integer pathologistIdValue =
                user.getPathologistId();

        if (pathologistIdValue == null
                || pathologistIdValue <= 0) {

            LOGGER.log(
                    Level.WARNING,
                    "Pathologist dashboard access rejected because "
                    + "the authenticated user has no valid "
                    + "Pathologist profile."
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "No active Pathologist profile is linked to "
                    + "this account."
            );

            /*
             * Throwing a controlled exception would result in a
             * second error response. Return through a marker
             * exception instead.
             */
            throw new DashboardAccessException();
        }

        int pathologistId =
                pathologistIdValue;

        var latestResults =
                labResultDAO
                        .findLatestResultsByPathologistId(
                                pathologistId
                        );

        List<Alert> alerts =
                alertDAO.findAllByPathologistId(
                        pathologistId
                );

        var patients =
                patientDAO.findByPathologistId(
                        pathologistId
                );

        request.setAttribute(
                "latestResults",
                latestResults
        );

        request.setAttribute(
                "alerts",
                alerts
        );

        request.setAttribute(
                "patients",
                patients
        );

        request.setAttribute(
                "totalPatients",
                patients.size()
        );

        request.setAttribute(
                "totalResults",
                latestResults.size()
        );

        request.setAttribute(
                "totalAlerts",
                alerts.size()
        );

        int pendingCriticalCount =
                alertDAO
                        .countPendingCriticalByPathologistId(
                                pathologistId
                        );

        request.setAttribute(
                "criticalAlerts",
                pendingCriticalCount
        );

        request.setAttribute(
                "pendingCriticalCount",
                pendingCriticalCount
        );

        /*
         * Pathologist KPI values are calculated only from alerts
         * belonging to assigned patients. This prevents disclosure
         * of system-wide operational information.
         */
        request.setAttribute(
                "overdueAlerts",
                countOverdueAlerts(alerts)
        );

        request.setAttribute(
                "dueSoonAlerts",
                countDueSoonAlerts(alerts)
        );

        request.setAttribute(
                "acknowledgementRate",
                calculateAcknowledgementRate(alerts)
        );

        request.setAttribute(
                "averageAcknowledgementMinutes",
                calculateAverageAcknowledgementMinutes(
                        alerts
                )
        );

        request.setAttribute(
                "emailSuccessRate",
                calculateEmailSuccessRate(alerts)
        );

        request.setAttribute(
                "acknowledgedAlerts",
                countAcknowledgedAlerts(alerts)
        );

        request.setAttribute(
                "awaitingReviewAlerts",
                countAwaitingReviewAlerts(alerts)
        );

        request.setAttribute(
                "escalatedAlerts",
                countEscalatedAlerts(alerts)
        );

        request.setAttribute(
                "pathologistView",
                true
        );
    }

    /**
     * Counts overdue alerts from an authorised alert list.
     *
     * @param alerts authorised alert list
     * @return overdue-alert count
     */
    private int countOverdueAlerts(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0;
        }

        long now =
                System.currentTimeMillis();

        int count =
                0;

        for (Alert alert : alerts) {

            if (alert != null
                    && !alert.isAcknowledged()
                    && alert.getReviewDueAt() != null
                    && alert.getReviewDueAt().getTime() < now) {

                count++;
            }
        }

        return count;
    }

    /**
     * Counts open alerts due for review within five minutes.
     *
     * @param alerts authorised alert list
     * @return due-soon count
     */
    private int countDueSoonAlerts(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0;
        }

        long now =
                System.currentTimeMillis();

        long dueSoonLimit =
                now + DUE_SOON_MILLISECONDS;

        int count =
                0;

        for (Alert alert : alerts) {

            if (alert == null
                    || alert.isAcknowledged()
                    || alert.getReviewDueAt() == null) {

                continue;
            }

            long deadline =
                    alert.getReviewDueAt().getTime();

            if (deadline >= now
                    && deadline <= dueSoonLimit) {

                count++;
            }
        }

        return count;
    }

    /**
     * Counts acknowledged alerts.
     *
     * @param alerts authorised alert list
     * @return acknowledged-alert count
     */
    private int countAcknowledgedAlerts(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0;
        }

        int count =
                0;

        for (Alert alert : alerts) {

            if (alert != null
                    && alert.isAcknowledged()) {

                count++;
            }
        }

        return count;
    }

    /**
     * Counts alerts awaiting professional review.
     *
     * @param alerts authorised alert list
     * @return awaiting-review count
     */
    private int countAwaitingReviewAlerts(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0;
        }

        int count =
                0;

        for (Alert alert : alerts) {

            if (alert != null
                    && !alert.isAcknowledged()) {

                count++;
            }
        }

        return count;
    }

    /**
     * Counts alerts that have an active escalation flag.
     *
     * @param alerts authorised alert list
     * @return escalated-alert count
     */
    private int countEscalatedAlerts(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0;
        }

        int count =
                0;

        for (Alert alert : alerts) {

            if (alert != null
                    && alert.isEscalated()
                    && !alert.isAcknowledged()) {

                count++;
            }
        }

        return count;
    }

    /**
     * Calculates acknowledged alerts as a percentage of all
     * authorised alerts.
     *
     * @param alerts authorised alert list
     * @return acknowledgement percentage rounded to one decimal
     */
    private double calculateAcknowledgementRate(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0.0;
        }

        int acknowledged =
                countAcknowledgedAlerts(alerts);

        return roundToOneDecimal(
                100.0 * acknowledged
                / alerts.size()
        );
    }

    /**
     * Calculates average time from alert creation to
     * acknowledgement.
     *
     * Historical records marked acknowledged without an
     * acknowledgement timestamp are excluded.
     *
     * @param alerts authorised alert list
     * @return average minutes rounded to one decimal
     */
    private double calculateAverageAcknowledgementMinutes(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0.0;
        }

        double totalMinutes =
                0.0;

        int validRecordCount =
                0;

        for (Alert alert : alerts) {

            if (alert == null
                    || alert.getCreatedAt() == null
                    || alert.getAcknowledgedAt() == null) {

                continue;
            }

            long difference =
                    alert.getAcknowledgedAt().getTime()
                    - alert.getCreatedAt().getTime();

            if (difference < 0) {

                /*
                 * Invalid historical data is ignored rather than
                 * producing a negative review-time KPI.
                 */
                continue;
            }

            totalMinutes +=
                    difference / (60.0 * 1000.0);

            validRecordCount++;
        }

        if (validRecordCount == 0) {

            return 0.0;
        }

        return roundToOneDecimal(
                totalMinutes / validRecordCount
        );
    }

    /**
     * Calculates successful email deliveries as a percentage of
     * completed email attempts.
     *
     * PENDING and unknown values are excluded from the denominator.
     *
     * @param alerts authorised alert list
     * @return email success percentage rounded to one decimal
     */
    private double calculateEmailSuccessRate(
            List<Alert> alerts) {

        if (alerts == null
                || alerts.isEmpty()) {

            return 0.0;
        }

        int sent =
                0;

        int failed =
                0;

        for (Alert alert : alerts) {

            if (alert == null
                    || isBlank(
                            alert.getNotificationStatus()
                    )) {

                continue;
            }

            String status =
                    alert.getNotificationStatus()
                            .trim();

            if ("EMAIL_SENT".equalsIgnoreCase(status)) {

                sent++;

            } else if ("EMAIL_FAILED"
                    .equalsIgnoreCase(status)) {

                failed++;
            }
        }

        int completedAttempts =
                sent + failed;

        if (completedAttempts == 0) {

            return 0.0;
        }

        return roundToOneDecimal(
                100.0 * sent
                / completedAttempts
        );
    }

    /**
     * Rounds a number to one decimal place.
     *
     * @param value numeric value
     * @return rounded value
     */
    private double roundToOneDecimal(
            double value) {

        return Math.round(
                value * 10.0
        ) / 10.0;
    }

    /**
     * Prevents the dashboard from being cached after logout.
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

    /**
     * Checks whether text is null or blank.
     *
     * @param value text value
     * @return true when missing
     */
    private String safeText(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Checks whether text is null or blank.
     *
     * @param value text value
     * @return true when missing
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }

    /**
     * Internal marker exception used after a forbidden Pathologist
     * dashboard response has already been sent.
     */
    private static class DashboardAccessException
            extends RuntimeException {

        private static final long serialVersionUID =
                1L;
    }
}