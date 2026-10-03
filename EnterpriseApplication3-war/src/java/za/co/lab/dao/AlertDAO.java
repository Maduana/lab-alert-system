package za.co.lab.dao;

import za.co.lab.model.Alert;
import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertDAO {

    private static final Logger LOGGER
            = Logger.getLogger(AlertDAO.class.getName());

    /**
     * Creates a new alert.
     *
     * @param alert alert to insert
     * @return generated alert ID, or -1 when insertion fails
     */
    public int insert(Alert alert) {

        String sql
                = "INSERT INTO alerts "
                + "(result_id, patient_id, severity, alert_message, "
                + "notification_status, acknowledged) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(
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
     * Updates only the email-notification status.
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
                || status == null
                || status.isBlank()) {

            return false;
        }

        String sql
                = "UPDATE alerts "
                + "SET notification_status = ? "
                + "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

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
     * @param resultId laboratory-result identifier
     * @return true when an alert exists
     */
    public boolean existsByResultId(int resultId) {

        String sql
                = "SELECT COUNT(*) "
                + "FROM alerts "
                + "WHERE result_id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, resultId);

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

            statement.setInt(1, alertId);

            try (ResultSet resultSet
                    = statement.executeQuery()) {

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
                + "ORDER BY a.created_at DESC";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

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
     * Returns alerts for patients assigned to a particular pathologist.
     *
     * @param pathologistId pathologist identifier
     * @return assigned-alert list
     */
    public List<Alert> findAllByPathologistId(
            int pathologistId) {

        List<Alert> alerts = new ArrayList<>();

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
                + "ORDER BY a.created_at DESC";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            try (ResultSet resultSet
                    = statement.executeQuery()) {

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
     * Counts all unacknowledged critical alerts.
     *
     * @return pending critical-alert count
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
     * Counts pending critical alerts belonging to patients assigned to a
     * particular pathologist.
     *
     * @param pathologistId pathologist identifier
     * @return pending critical-alert count
     */
    public int countPendingCriticalByPathologistId(
            int pathologistId) {

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

            statement.setInt(1, pathologistId);

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
     * Prefer acknowledgeByPathologist() for user-triggered acknowledgement
     * because that method verifies ownership.
     *
     * @param alertId alert identifier
     * @param acknowledgedBy acknowledging user name
     * @return true when one unacknowledged row was updated
     */
    public boolean acknowledge(
            int alertId,
            String acknowledgedBy) {

        String sql
                = "UPDATE alerts "
                + "SET acknowledged = TRUE, "
                + "acknowledged_by = ?, "
                + "acknowledged_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? "
                + "AND acknowledged = FALSE";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, acknowledgedBy);
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
     * 1. The alert exists. 2. The alert has not already been acknowledged. 3.
     * The patient is assigned to the supplied pathologist. 4. The supplied
     * pathologist profile is active.
     *
     * The method deliberately leaves notification_status unchanged. EMAIL_SENT
     * therefore remains EMAIL_SENT after acknowledgement.
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

        String sql
                = "UPDATE alerts a "
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

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);
            statement.setString(2, pathologistName);
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

            statement.setInt(1, alertId);
            statement.setInt(2, pathologistId);

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
     * Maps an alert query row to an Alert object.
     *
     * The query must include patient_code, patient_name, test_name and
     * result_value.
     */
    private Alert mapAlert(ResultSet resultSet)
            throws Exception {

        Alert alert = new Alert();

        alert.setId(resultSet.getInt("id"));
        alert.setResultId(resultSet.getInt("result_id"));
        alert.setPatientId(resultSet.getInt("patient_id"));
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
