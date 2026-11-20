package za.co.lab.dao;

import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides database operations for laboratory alerts.
 *
 * Review deadlines:
 *
 * CRITICAL alerts -> 15 minutes WARNING alerts -> 60 minutes
 *
 * @author USER
 */
public class AlertDAO {

    private static final Logger LOGGER
            = Logger.getLogger(AlertDAO.class.getName());

    private static final int CRITICAL_REVIEW_MINUTES = 15;
    private static final int WARNING_REVIEW_MINUTES = 60;
    private static final int DUE_SOON_MINUTES = 5;
    private static final int MAX_REMINDERS = 3;
    private static final int REMINDER_COOLDOWN_MINUTES = 15;

    /**
     * Creates a new alert.
     *
     * Every alert receives a review deadline based on severity.
     *
     * @param alert alert to insert
     * @return generated alert ID, or -1 when insertion fails
     */
    public int insert(Alert alert) {

        if (!isValidAlertForInsert(alert)) {

            LOGGER.log(
                    Level.WARNING,
                    "Alert insertion rejected because required "
                    + "information was missing or invalid."
            );

            return -1;
        }

        String sql
                = "INSERT INTO alerts "
                + "(result_id, patient_id, severity, alert_message, "
                + "notification_status, acknowledged, created_at, "
                + "review_due_at, escalated, reminder_count, "
                + "last_reminder_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime createdAt
                = LocalDateTime.now();

        LocalDateTime reviewDueAt
                = calculateReviewDueAt(
                        createdAt,
                        alert.getSeverity()
                );

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(
                    1,
                    alert.getResultId()
            );

            statement.setInt(
                    2,
                    alert.getPatientId()
            );

            statement.setString(
                    3,
                    alert.getSeverity().trim().toUpperCase()
            );

            statement.setString(
                    4,
                    alert.getAlertMessage().trim()
            );

            statement.setString(
                    5,
                    normalizeNotificationStatus(
                            alert.getNotificationStatus()
                    )
            );

            statement.setBoolean(
                    6,
                    alert.isAcknowledged()
            );

            statement.setTimestamp(
                    7,
                    Timestamp.valueOf(createdAt)
            );

            statement.setTimestamp(
                    8,
                    Timestamp.valueOf(reviewDueAt)
            );

            statement.setBoolean(
                    9,
                    false
            );

            statement.setInt(
                    10,
                    0
            );

            statement.setTimestamp(
                    11,
                    null
            );
            int rowsInserted
                    = statement.executeUpdate();

            if (rowsInserted != 1) {

                LOGGER.log(
                        Level.WARNING,
                        "Alert insertion affected {0} rows.",
                        rowsInserted
                );

                return -1;
            }

            try (ResultSet generatedKeys
                    = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to insert alert.",
                    exception
            );
        }

        return -1;
    }

    /**
     * Calculates a review deadline from the alert creation time.
     *
     * @param createdAt creation time
     * @param severity alert severity
     * @return review deadline
     */
    private LocalDateTime calculateReviewDueAt(
            LocalDateTime createdAt,
            String severity) {

        if ("CRITICAL".equalsIgnoreCase(severity)) {

            return createdAt.plusMinutes(
                    CRITICAL_REVIEW_MINUTES
            );
        }

        return createdAt.plusMinutes(
                WARNING_REVIEW_MINUTES
        );
    }

    /**
     * Validates alert information required for insertion.
     *
     * @param alert alert being inserted
     * @return true when valid
     */
    private boolean isValidAlertForInsert(Alert alert) {

        if (alert == null
                || alert.getResultId() <= 0
                || alert.getPatientId() <= 0
                || isBlank(alert.getSeverity())
                || isBlank(alert.getAlertMessage())) {

            return false;
        }

        String severity
                = alert.getSeverity().trim();

        return "CRITICAL".equalsIgnoreCase(severity)
                || "WARNING".equalsIgnoreCase(severity);
    }

    /**
     * Returns a valid notification status.
     *
     * @param status supplied notification status
     * @return notification status
     */
    private String normalizeNotificationStatus(
            String status) {

        if (isBlank(status)) {
            return "PENDING";
        }

        return status.trim().toUpperCase();
    }

    /**
     * Updates only the email notification status.
     *
     * Typical values: EMAIL_SENT EMAIL_FAILED PENDING
     *
     * This method must not be used for acknowledgement status.
     *
     * @param alertId alert identifier
     * @param status notification status
     * @return true when one row was updated
     */
    public boolean updateNotificationStatus(
            int alertId,
            String status) {

        if (alertId <= 0
                || isBlank(status)) {

            return false;
        }

        String sql
                = "UPDATE alerts "
                + "SET notification_status = ? "
                + "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.trim().toUpperCase()
            );

            statement.setInt(
                    2,
                    alertId
            );

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to update notification status "
                    + "for alert ID "
                    + alertId,
                    exception
            );
        }

        return false;
    }

    /**
     * Checks whether an alert already exists for a result.
     *
     * @param resultId laboratory result identifier
     * @return true when an alert exists
     */
    public boolean existsByResultId(int resultId) {

        if (resultId <= 0) {
            return false;
        }

        String sql
                = "SELECT COUNT(*) "
                + "FROM alerts "
                + "WHERE result_id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    resultId
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to check whether an alert exists "
                    + "for result ID "
                    + resultId,
                    exception
            );
        }

        return false;
    }

    /**
     * Finds one alert and its related patient and result information.
     *
     * @param alertId alert identifier
     * @return alert, or null when not found
     */
    public Alert findById(int alertId) {

        if (alertId <= 0) {
            return null;
        }

        String sql
                = "SELECT a.*, "
                + "p.patient_code, "
                + "CONCAT(p.first_name, ' ', p.last_name) "
                + "AS patient_name, "
                + "lr.test_name, "
                + "lr.result_value "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN lab_results lr "
                + "ON a.result_id = lr.id "
                + "WHERE a.id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    alertId
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAlert(resultSet);
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to find alert ID "
                    + alertId,
                    exception
            );
        }

        return null;
    }

    /**
     * Finds an overdue, unacknowledged alert that may receive another reminder.
     *
     * @param alertId alert identifier
     * @return eligible alert, or null
     */
    public Alert findEligibleReminderAlert(
            int alertId) {

        if (alertId <= 0) {
            return null;
        }

        String sql
                = "SELECT a.*, "
                + "p.patient_code, "
                + "CONCAT(p.first_name, ' ', p.last_name) "
                + "AS patient_name, "
                + "lr.test_name, "
                + "lr.result_value "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON p.id = a.patient_id "
                + "JOIN lab_results lr "
                + "ON lr.id = a.result_id "
                + "WHERE a.id = ? "
                + "AND a.acknowledged = FALSE "
                + "AND a.review_due_at IS NOT NULL "
                + "AND a.review_due_at < CURRENT_TIMESTAMP "
                + "AND a.reminder_count < ? "
                + "AND ("
                + "a.last_reminder_at IS NULL "
                + "OR TIMESTAMPADD("
                + "MINUTE, ?, a.last_reminder_at"
                + ") <= CURRENT_TIMESTAMP"
                + ")";

        try (Connection connection
                = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    alertId
            );

            statement.setInt(
                    2,
                    MAX_REMINDERS
            );

            statement.setInt(
                    3,
                    REMINDER_COOLDOWN_MINUTES
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAlert(resultSet);
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve reminder-eligible alert ID "
                    + alertId,
                    exception
            );
        }

        return null;
    }

    /**
     * Records a successfully delivered overdue-alert reminder.
     *
     * The original notification status remains unchanged because it describes
     * the first alert email.
     *
     * @param alertId alert identifier
     * @return true when reminder tracking was updated
     */
    public boolean recordReminderSent(
            int alertId) {

        if (alertId <= 0) {
            return false;
        }

        String sql
                = "UPDATE alerts "
                + "SET reminder_count = reminder_count + 1, "
                + "last_reminder_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? "
                + "AND acknowledged = FALSE "
                + "AND review_due_at IS NOT NULL "
                + "AND review_due_at < CURRENT_TIMESTAMP "
                + "AND reminder_count < ? "
                + "AND ("
                + "last_reminder_at IS NULL "
                + "OR TIMESTAMPADD("
                + "MINUTE, ?, last_reminder_at"
                + ") <= CURRENT_TIMESTAMP"
                + ")";

        try (Connection connection
                = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    alertId
            );

            statement.setInt(
                    2,
                    MAX_REMINDERS
            );

            statement.setInt(
                    3,
                    REMINDER_COOLDOWN_MINUTES
            );

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to record reminder for alert ID "
                    + alertId,
                    exception
            );
        }

        return false;
    }

    /**
     * Returns the reminder eligibility reason for an alert.
     *
     * @param alert alert to evaluate
     * @return eligibility status
     */
    public String getReminderEligibilityStatus(
            Alert alert) {

        if (alert == null) {
            return "ALERT_NOT_FOUND";
        }

        if (alert.isAcknowledged()) {
            return "ALREADY_ACKNOWLEDGED";
        }

        if (alert.getReviewDueAt() == null) {
            return "NO_REVIEW_DEADLINE";
        }

        long now
                = System.currentTimeMillis();

        if (alert.getReviewDueAt().getTime() >= now) {
            return "NOT_OVERDUE";
        }

        if (alert.getReminderCount() >= MAX_REMINDERS) {
            return "MAXIMUM_REACHED";
        }

        if (alert.getLastReminderAt() != null) {

            long cooldownMilliseconds
                    = REMINDER_COOLDOWN_MINUTES
                    * 60L
                    * 1000L;

            long nextAllowedTime
                    = alert.getLastReminderAt().getTime()
                    + cooldownMilliseconds;

            if (nextAllowedTime > now) {
                return "COOLDOWN_ACTIVE";
            }
        }

        return "ELIGIBLE";
    }

    /**
     * Returns all alerts for administrator monitoring.
     *
     * Open overdue alerts are placed first, followed by open critical alerts,
     * other open alerts, and acknowledged alerts.
     *
     * @return alert list
     */
    public List<Alert> findAll() {

        List<Alert> alerts
                = new ArrayList<>();

        String sql
                = "SELECT a.*, "
                + "p.patient_code, "
                + "CONCAT(p.first_name, ' ', p.last_name) "
                + "AS patient_name, "
                + "lr.test_name, "
                + "lr.result_value "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN lab_results lr "
                + "ON a.result_id = lr.id "
                + "ORDER BY "
                + "CASE "
                + "WHEN a.acknowledged = FALSE "
                + "AND a.review_due_at IS NOT NULL "
                + "AND a.review_due_at < NOW() THEN 1 "
                + "WHEN a.acknowledged = FALSE "
                + "AND a.severity = 'CRITICAL' THEN 2 "
                + "WHEN a.acknowledged = FALSE THEN 3 "
                + "ELSE 4 "
                + "END, "
                + "a.created_at DESC, "
                + "a.id DESC";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            while (resultSet.next()) {
                alerts.add(
                        mapAlert(resultSet)
                );
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve all alerts.",
                    exception
            );
        }

        return alerts;
    }

    /**
     * Returns alerts for patients assigned to a pathologist.
     *
     * @param pathologistId pathologist identifier
     * @return assigned alert list
     */
    public List<Alert> findAllByPathologistId(
            int pathologistId) {

        List<Alert> alerts
                = new ArrayList<>();

        if (pathologistId <= 0) {
            return alerts;
        }

        String sql
                = "SELECT a.*, "
                + "p.patient_code, "
                + "CONCAT(p.first_name, ' ', p.last_name) "
                + "AS patient_name, "
                + "lr.test_name, "
                + "lr.result_value "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN lab_results lr "
                + "ON a.result_id = lr.id "
                + "WHERE p.pathologist_id = ? "
                + "ORDER BY "
                + "CASE "
                + "WHEN a.acknowledged = FALSE "
                + "AND a.review_due_at IS NOT NULL "
                + "AND a.review_due_at < NOW() THEN 1 "
                + "WHEN a.acknowledged = FALSE "
                + "AND a.severity = 'CRITICAL' THEN 2 "
                + "WHEN a.acknowledged = FALSE THEN 3 "
                + "ELSE 4 "
                + "END, "
                + "a.created_at DESC, "
                + "a.id DESC";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    pathologistId
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {
                    alerts.add(
                            mapAlert(resultSet)
                    );
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve alerts for pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return alerts;
    }

    /**
     * Returns alerts matching supplied report filters.
     *
     * This method is intended for administrators because it does not limit
     * results to a particular pathologist.
     *
     * @param filter report filters
     * @return filtered alert list
     */
    public List<Alert> findForReport(
            AlertReportFilter filter) {

        return findForReportInternal(
                filter,
                null
        );
    }

    /**
     * Returns alerts matching the report filters, limited to patients assigned
     * to the authenticated pathologist.
     *
     * @param filter report filters
     * @param pathologistId authenticated pathologist identifier
     * @return filtered assigned alert list
     */
    public List<Alert> findForReportByPathologistId(
            AlertReportFilter filter,
            int pathologistId) {

        if (pathologistId <= 0) {
            return new ArrayList<>();
        }

        return findForReportInternal(
                filter,
                pathologistId
        );
    }

    /**
     * Executes the shared role-aware reporting query.
     *
     * When pathologistId is null, all matching alerts are returned. When
     * supplied, only alerts assigned to that pathologist are returned.
     *
     * @param filter report filters
     * @param pathologistId optional pathologist identifier
     * @return filtered alert list
     */
    private List<Alert> findForReportInternal(
            AlertReportFilter filter,
            Integer pathologistId) {

        List<Alert> alerts
                = new ArrayList<>();

        AlertReportFilter safeFilter
                = filter == null
                        ? new AlertReportFilter()
                        : filter;

        if (safeFilter.hasInvalidDateRange()) {

            LOGGER.log(
                    Level.WARNING,
                    "Report query rejected because the start date "
                    + "is later than the end date."
            );

            return alerts;
        }

        StringBuilder sql
                = new StringBuilder();

        sql.append(
                "SELECT a.*, "
                + "p.patient_code, "
                + "CONCAT(p.first_name, ' ', p.last_name) "
                + "AS patient_name, "
                + "lr.test_name, "
                + "lr.result_value "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON p.id = a.patient_id "
                + "JOIN lab_results lr "
                + "ON lr.id = a.result_id "
                + "WHERE 1 = 1 "
        );

        List<Object> parameters
                = new ArrayList<>();

        /*
         * The Pathologist ID must originate from the authenticated
         * User object, never from a request parameter.
         */
        if (pathologistId != null) {

            sql.append(
                    "AND p.pathologist_id = ? "
            );

            parameters.add(
                    pathologistId
            );
        }

        /*
         * Start date is inclusive from 00:00:00.
         */
        if (safeFilter.hasStartDate()) {

            sql.append(
                    "AND a.created_at >= ? "
            );

            parameters.add(
                    Timestamp.valueOf(
                            safeFilter.getStartDate()
                                    .atStartOfDay()
                    )
            );
        }

        /*
         * End date is inclusive by comparing against the start
         * of the following day.
         */
        if (safeFilter.hasEndDate()) {

            sql.append(
                    "AND a.created_at < ? "
            );

            parameters.add(
                    Timestamp.valueOf(
                            safeFilter.getEndDate()
                                    .plusDays(1)
                                    .atStartOfDay()
                    )
            );
        }

        if (safeFilter.hasSeverity()) {

            sql.append(
                    "AND UPPER(a.severity) = UPPER(?) "
            );

            parameters.add(
                    safeFilter.getSeverity()
            );
        }

        if (safeFilter.hasNotificationStatus()) {

            sql.append(
                    "AND UPPER(a.notification_status) = UPPER(?) "
            );

            parameters.add(
                    safeFilter.getNotificationStatus()
            );
        }

        if (safeFilter.isAcknowledgedOnly()) {

            sql.append(
                    "AND a.acknowledged = TRUE "
            );

        } else if (safeFilter.isPendingOnly()) {

            sql.append(
                    "AND a.acknowledged = FALSE "
            );
        }

        if (safeFilter.hasPatientCode()) {

            sql.append(
                    "AND UPPER(p.patient_code) LIKE UPPER(?) "
            );

            parameters.add(
                    "%"
                    + safeFilter.getPatientCode()
                    + "%"
            );
        }

        if (safeFilter.hasTestName()) {

            sql.append(
                    "AND UPPER(lr.test_name) = UPPER(?) "
            );

            parameters.add(
                    safeFilter.getTestName()
            );
        }

        sql.append(
                "ORDER BY a.created_at DESC, a.id DESC"
        );

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(
                        sql.toString()
                )) {

            bindReportParameters(
                    statement,
                    parameters
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {
                    alerts.add(
                            mapAlert(resultSet)
                    );
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve filtered alert report "
                    + "records.",
                    exception
            );
        }

        return alerts;
    }

    /**
     * Binds dynamically collected report parameters.
     *
     * @param statement prepared statement
     * @param parameters filter parameters
     * @throws Exception when parameter binding fails
     */
    private void bindReportParameters(
            PreparedStatement statement,
            List<Object> parameters)
            throws Exception {

        for (int index = 0;
                index < parameters.size();
                index++) {

            Object parameter
                    = parameters.get(index);

            int position
                    = index + 1;

            if (parameter instanceof Integer) {

                statement.setInt(
                        position,
                        (Integer) parameter
                );

            } else if (parameter instanceof Timestamp) {

                statement.setTimestamp(
                        position,
                        (Timestamp) parameter
                );

            } else {

                statement.setString(
                        position,
                        String.valueOf(parameter)
                );
            }
        }
    }

    /**
     * Counts all unacknowledged critical alerts.
     *
     * @return pending critical alert count
     */
    public int countPendingCritical() {

        String sql
                = "SELECT COUNT(*) AS total "
                + "FROM alerts "
                + "WHERE severity = 'CRITICAL' "
                + "AND acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to count pending critical alerts.",
                    exception
            );
        }

        return 0;
    }

    /**
     * Counts pending critical alerts for a pathologist.
     *
     * @param pathologistId pathologist identifier
     * @return pending critical alert count
     */
    public int countPendingCriticalByPathologistId(
            int pathologistId) {

        if (pathologistId <= 0) {
            return 0;
        }

        String sql
                = "SELECT COUNT(*) AS total "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "WHERE p.pathologist_id = ? "
                + "AND a.severity = 'CRITICAL' "
                + "AND a.acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    pathologistId
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to count pending critical alerts "
                    + "for pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return 0;
    }

    /**
     * Counts currently overdue unacknowledged alerts.
     *
     * @return overdue alert count
     */
    public int countOverdueAlerts() {

        String sql
                = "SELECT COUNT(*) AS total "
                + "FROM alerts "
                + "WHERE acknowledged = FALSE "
                + "AND review_due_at IS NOT NULL "
                + "AND review_due_at < NOW()";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to count overdue alerts.",
                    exception
            );
        }

        return 0;
    }

    /**
     * Counts open alerts due within the next five minutes.
     *
     * @return due-soon alert count
     */
    public int countDueSoonAlerts() {

        String sql
                = "SELECT COUNT(*) AS total "
                + "FROM alerts "
                + "WHERE acknowledged = FALSE "
                + "AND review_due_at IS NOT NULL "
                + "AND review_due_at >= NOW() "
                + "AND review_due_at <= "
                + "DATE_ADD(NOW(), TIMESTAMPADD())";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    DUE_SOON_MINUTES
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to count due-soon alerts.",
                    exception
            );
        }

        return 0;
    }

    /**
     * Calculates the acknowledgement rate across all alerts.
     *
     * @return acknowledgement percentage
     */
    public double getAcknowledgementRatePercentage() {

        String sql
                = "SELECT ROUND("
                + "100.0 * SUM("
                + "CASE WHEN acknowledged = TRUE "
                + "THEN 1 ELSE 0 END"
                + ") / NULLIF(COUNT(*), 0), 1"
                + ") AS rate "
                + "FROM alerts";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble("rate");
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to calculate acknowledgement rate.",
                    exception
            );
        }

        return 0.0;
    }

    /**
     * Calculates average acknowledgement time.
     *
     * Incomplete historical records with no acknowledged_at value are excluded.
     *
     * @return average acknowledgement time in minutes
     */
    public double getAverageAcknowledgementMinutes() {

        String sql
                = "SELECT ROUND("
                + "AVG(TIMESTAMPDIFF("
                + "MINUTE, created_at, acknowledged_at"
                + ")), 1"
                + ") AS average_minutes "
                + "FROM alerts "
                + "WHERE acknowledged = TRUE "
                + "AND acknowledged_at IS NOT NULL";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(
                        "average_minutes"
                );
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to calculate average acknowledgement "
                    + "time.",
                    exception
            );
        }

        return 0.0;
    }

    /**
     * Calculates email-delivery success rate.
     *
     * @return email success percentage
     */
    public double getEmailSuccessRatePercentage() {

        String sql
                = "SELECT ROUND("
                + "100.0 * SUM("
                + "CASE WHEN notification_status = 'EMAIL_SENT' "
                + "THEN 1 ELSE 0 END"
                + ") / NULLIF(SUM("
                + "CASE WHEN notification_status IN "
                + "('EMAIL_SENT', 'EMAIL_FAILED') "
                + "THEN 1 ELSE 0 END"
                + "), 0), 1"
                + ") AS rate "
                + "FROM alerts";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble("rate");
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to calculate email success rate.",
                    exception
            );
        }

        return 0.0;
    }

    /**
     * Synchronises escalation flags with the current time.
     *
     * Open overdue alerts are escalated. Acknowledged and open non-overdue
     * alerts are not escalated.
     *
     * @return number of rows affected
     */
    public int synchroniseEscalationFlags() {

        String sql
                = "UPDATE alerts "
                + "SET escalated = CASE "
                + "WHEN acknowledged = FALSE "
                + "AND review_due_at IS NOT NULL "
                + "AND review_due_at < NOW() "
                + "THEN TRUE "
                + "ELSE FALSE "
                + "END "
                + "WHERE id > 0";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to synchronise escalation flags.",
                    exception
            );
        }

        return 0;
    }

    /**
     * General acknowledgement method.
     *
     * This method preserves notification_status and clears active escalation.
     *
     * @param alertId alert identifier
     * @param acknowledgedBy acknowledging user
     * @return true when acknowledgement succeeds
     */
    public boolean acknowledge(
            int alertId,
            String acknowledgedBy) {

        if (alertId <= 0
                || isBlank(acknowledgedBy)) {

            return false;
        }

        String sql
                = "UPDATE alerts "
                + "SET acknowledged = TRUE, "
                + "acknowledged_by = ?, "
                + "acknowledged_at = CURRENT_TIMESTAMP, "
                + "escalated = FALSE "
                + "WHERE id = ? "
                + "AND acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    acknowledgedBy.trim()
            );

            statement.setInt(
                    2,
                    alertId
            );

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to acknowledge alert ID "
                    + alertId,
                    exception
            );
        }

        return false;
    }

    /**
     * Acknowledges an alert only when the patient belongs to the supplied
     * active pathologist.
     *
     * Notification status is preserved and escalation is cleared.
     *
     * @param alertId alert identifier
     * @param pathologistId pathologist identifier
     * @param pathologistName pathologist display name
     * @return true when acknowledgement succeeds
     */
    public boolean acknowledgeByPathologist(
            int alertId,
            int pathologistId,
            String pathologistName) {

        if (alertId <= 0
                || pathologistId <= 0
                || isBlank(pathologistName)) {

            return false;
        }

        String sql
                = "UPDATE alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN pathologists ph "
                + "ON ph.id = p.pathologist_id "
                + "SET a.acknowledged = TRUE, "
                + "a.acknowledged_by_pathologist_id = ?, "
                + "a.acknowledged_by = ?, "
                + "a.acknowledged_at = CURRENT_TIMESTAMP, "
                + "a.escalated = FALSE "
                + "WHERE a.id = ? "
                + "AND a.acknowledged = FALSE "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    pathologistId
            );

            statement.setString(
                    2,
                    pathologistName.trim()
            );

            statement.setInt(
                    3,
                    alertId
            );

            statement.setInt(
                    4,
                    pathologistId
            );

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to acknowledge alert ID "
                    + alertId
                    + " for pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return false;
    }

    /**
     * Checks whether an alert belongs to a patient assigned to the supplied
     * active pathologist.
     *
     * @param alertId alert identifier
     * @param pathologistId pathologist identifier
     * @return true when ownership is valid
     */
    public boolean isOwnedByPathologist(
            int alertId,
            int pathologistId) {

        if (alertId <= 0
                || pathologistId <= 0) {

            return false;
        }

        String sql
                = "SELECT COUNT(*) "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN pathologists ph "
                + "ON ph.id = p.pathologist_id "
                + "WHERE a.id = ? "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    alertId
            );

            statement.setInt(
                    2,
                    pathologistId
            );

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to verify ownership for alert ID "
                    + alertId
                    + " and pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return false;
    }

    /**
     * Maps one joined query row to an Alert object.
     *
     * Required joined aliases: patient_code patient_name test_name result_value
     *
     * @param resultSet current query row
     * @return populated alert
     * @throws Exception when a column cannot be read
     */
    private Alert mapAlert(ResultSet resultSet)
            throws Exception {

        Alert alert
                = new Alert();

        alert.setId(
                resultSet.getInt("id")
        );

        alert.setResultId(
                resultSet.getInt("result_id")
        );

        alert.setPatientId(
                resultSet.getInt("patient_id")
        );

        alert.setPatientCode(
                resultSet.getString("patient_code")
        );

        alert.setPatientName(
                resultSet.getString("patient_name")
        );

        alert.setTestName(
                resultSet.getString("test_name")
        );

        alert.setResultValue(
                resultSet.getDouble("result_value")
        );

        alert.setSeverity(
                resultSet.getString("severity")
        );

        alert.setAlertMessage(
                resultSet.getString("alert_message")
        );

        alert.setNotificationStatus(
                resultSet.getString(
                        "notification_status"
                )
        );

        alert.setAcknowledged(
                resultSet.getBoolean(
                        "acknowledged"
                )
        );

        int acknowledgedPathologistId
                = resultSet.getInt(
                        "acknowledged_by_pathologist_id"
                );

        if (!resultSet.wasNull()) {

            alert.setAcknowledgedByPathologistId(
                    acknowledgedPathologistId
            );
        }

        alert.setAcknowledgedBy(
                resultSet.getString(
                        "acknowledged_by"
                )
        );

        alert.setAcknowledgedAt(
                resultSet.getTimestamp(
                        "acknowledged_at"
                )
        );

        alert.setCreatedAt(
                resultSet.getTimestamp(
                        "created_at"
                )
        );

        alert.setReviewDueAt(
                resultSet.getTimestamp(
                        "review_due_at"
                )
        );

        alert.setEscalated(
                resultSet.getBoolean(
                        "escalated"
                )
        );

        alert.setReminderCount(
                resultSet.getInt(
                        "reminder_count"
                )
        );

        alert.setLastReminderAt(
                resultSet.getTimestamp(
                        "last_reminder_at"
                )
        );

        return alert;
    }

    /**
     * Checks whether text is null or blank.
     *
     * @param value text value
     * @return true when missing
     */
    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }
}
