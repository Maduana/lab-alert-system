package za.co.lab.dao;

import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertDAO {

    private static final Logger LOGGER =
            Logger.getLogger(AlertDAO.class.getName());

    /**
     * Creates a new alert.
     *
     * @param alert alert to insert
     * @return generated alert ID, or -1 when insertion fails
     */
    public int insert(Alert alert) {

        String sql =
                "INSERT INTO alerts "
                + "(result_id, patient_id, severity, alert_message, "
                + "notification_status, acknowledged) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, alert.getResultId());
            statement.setInt(2, alert.getPatientId());
            statement.setString(3, alert.getSeverity());
            statement.setString(4, alert.getAlertMessage());
            statement.setString(5, alert.getNotificationStatus());
            statement.setBoolean(6, alert.isAcknowledged());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted != 1) {
                return -1;
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

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
     * Updates only the email notification status.
     *
     * Typical values:
     * EMAIL_SENT
     * EMAIL_FAILED
     * PENDING
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
                || status == null
                || status.isBlank()) {

            return false;
        }

        String sql =
                "UPDATE alerts "
                + "SET notification_status = ? "
                + "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status.trim());
            statement.setInt(2, alertId);

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to update notification status "
                    + "for alert ID " + alertId,
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

        String sql =
                "SELECT COUNT(*) "
                + "FROM alerts "
                + "WHERE result_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, resultId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to check whether an alert exists "
                    + "for result ID " + resultId,
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

        String sql =
                "SELECT a.*, "
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

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, alertId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAlert(resultSet);
                }
            }

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to find alert ID " + alertId,
                    exception
            );
        }

        return null;
    }

    /**
     * Returns all alerts for administrator monitoring.
     *
     * @return alert list
     */
    public List<Alert> findAll() {

        List<Alert> alerts = new ArrayList<>();

        String sql =
                "SELECT a.*, "
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
                + "ORDER BY a.created_at DESC, a.id DESC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                alerts.add(mapAlert(resultSet));
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

        List<Alert> alerts = new ArrayList<>();

        if (pathologistId <= 0) {
            return alerts;
        }

        String sql =
                "SELECT a.*, "
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
                + "ORDER BY a.created_at DESC, a.id DESC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    alerts.add(mapAlert(resultSet));
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
     * Returns alerts matching the supplied report filters.
     *
     * This method is intended for administrators because it does
     * not limit results to a particular pathologist.
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
     * Returns alerts matching the report filters, limited to patients
     * assigned to the authenticated pathologist.
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
     * When pathologistId is null, all matching alerts are returned.
     * When pathologistId is supplied, only alerts assigned to that
     * pathologist are returned.
     *
     * @param filter report filters
     * @param pathologistId optional pathologist identifier
     * @return filtered alert list
     */
    private List<Alert> findForReportInternal(
            AlertReportFilter filter,
            Integer pathologistId) {

        List<Alert> alerts = new ArrayList<>();

        AlertReportFilter safeFilter =
                filter == null
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

        StringBuilder sql = new StringBuilder();

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

        List<Object> parameters = new ArrayList<>();

        /*
         * The pathologist ID must come from the authenticated User
         * object in the servlet, never from a request parameter.
         */
        if (pathologistId != null) {
            sql.append(
                    "AND p.pathologist_id = ? "
            );

            parameters.add(pathologistId);
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
         * End date is inclusive.
         *
         * The query compares created_at with the start of the day
         * after the selected end date.
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

        /*
         * ACKNOWLEDGED means acknowledged = TRUE.
         * PENDING means acknowledged = FALSE.
         */
        if (safeFilter.isAcknowledgedOnly()) {
            sql.append(
                    "AND a.acknowledged = TRUE "
            );

        } else if (safeFilter.isPendingOnly()) {
            sql.append(
                    "AND a.acknowledged = FALSE "
            );
        }

        /*
         * Patient code supports a case-insensitive partial match.
         */
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

        /*
         * Test name uses a case-insensitive exact match.
         */
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

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql.toString()
                     )) {

            bindReportParameters(
                    statement,
                    parameters
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    alerts.add(mapAlert(resultSet));
                }
            }

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve filtered alert report records.",
                    exception
            );
        }

        return alerts;
    }

    /**
     * Binds the dynamically collected report parameters to the
     * prepared statement.
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

            Object parameter =
                    parameters.get(index);

            int position =
                    index + 1;

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

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM alerts "
                + "WHERE severity = 'CRITICAL' "
                + "AND acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

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

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "WHERE p.pathologist_id = ? "
                + "AND a.severity = 'CRITICAL' "
                + "AND a.acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to count pending critical alerts "
                    + "for pathologist ID " + pathologistId,
                    exception
            );
        }

        return 0;
    }

    /**
     * General acknowledgement method.
     *
     * This method preserves notification_status.
     *
     * Prefer acknowledgeByPathologist() for acknowledgements
     * initiated by a pathologist.
     *
     * @param alertId alert identifier
     * @param acknowledgedBy acknowledging user
     * @return true when acknowledgement succeeds
     */
    public boolean acknowledge(
            int alertId,
            String acknowledgedBy) {

        if (alertId <= 0
                || acknowledgedBy == null
                || acknowledgedBy.isBlank()) {

            return false;
        }

        String sql =
                "UPDATE alerts "
                + "SET acknowledged = TRUE, "
                + "acknowledged_by = ?, "
                + "acknowledged_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? "
                + "AND acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    acknowledgedBy.trim()
            );

            statement.setInt(2, alertId);

            return statement.executeUpdate() == 1;

        } catch (Exception exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to acknowledge alert ID " + alertId,
                    exception
            );
        }

        return false;
    }

    /**
     * Acknowledges an alert only when:
     *
     * 1. The alert exists.
     * 2. The alert has not already been acknowledged.
     * 3. The patient is assigned to the supplied pathologist.
     * 4. The pathologist profile is active.
     *
     * The notification status remains unchanged.
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
                || pathologistName == null
                || pathologistName.isBlank()) {

            return false;
        }

        String sql =
                "UPDATE alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN pathologists ph "
                + "ON ph.id = p.pathologist_id "
                + "SET a.acknowledged = TRUE, "
                + "a.acknowledged_by_pathologist_id = ?, "
                + "a.acknowledged_by = ?, "
                + "a.acknowledged_at = CURRENT_TIMESTAMP "
                + "WHERE a.id = ? "
                + "AND a.acknowledged = FALSE "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            statement.setString(
                    2,
                    pathologistName.trim()
            );

            statement.setInt(3, alertId);
            statement.setInt(4, pathologistId);

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
     * Checks whether an alert belongs to a patient assigned to the
     * supplied active pathologist.
     *
     * @param alertId alert identifier
     * @param pathologistId pathologist identifier
     * @return true when ownership is valid
     */
    public boolean isOwnedByPathologist(
            int alertId,
            int pathologistId) {

        if (alertId <= 0 || pathologistId <= 0) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON a.patient_id = p.id "
                + "JOIN pathologists ph "
                + "ON ph.id = p.pathologist_id "
                + "WHERE a.id = ? "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, alertId);
            statement.setInt(2, pathologistId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

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
     * The query must include:
     * patient_code
     * patient_name
     * test_name
     * result_value
     *
     * @param resultSet current query row
     * @return populated alert
     * @throws Exception when a column cannot be read
     */
    private Alert mapAlert(ResultSet resultSet)
            throws Exception {

        Alert alert = new Alert();

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
                resultSet.getString("notification_status")
        );

        alert.setAcknowledged(
                resultSet.getBoolean("acknowledged")
        );

        int acknowledgedPathologistId =
                resultSet.getInt(
                        "acknowledged_by_pathologist_id"
                );

        if (!resultSet.wasNull()) {
            alert.setAcknowledgedByPathologistId(
                    acknowledgedPathologistId
            );
        }

        alert.setAcknowledgedBy(
                resultSet.getString("acknowledged_by")
        );

        alert.setAcknowledgedAt(
                resultSet.getTimestamp("acknowledged_at")
        );

        alert.setCreatedAt(
                resultSet.getTimestamp("created_at")
        );

        return alert;
    }
}