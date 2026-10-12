package za.co.lab.dao;

import org.junit.BeforeClass;

import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AlertDAORegressionTest {

    private AlertDAO alertDAO;

    private int testAlertId;
    private int assignedPathologistId;

    /*
     * Original values are stored so the test can restore the
     * selected alert after execution.
     */
    private String originalNotificationStatus;
    private boolean originalAcknowledged;
    private String originalAcknowledgedBy;
    private Timestamp originalAcknowledgedAt;
    private Integer originalAcknowledgedByPathologistId;

    @BeforeClass
    public static void configureTestDatabase() {

        System.setProperty(
                "labalert.testMode",
                "true"
        );

        System.setProperty(
                "labalert.testDbUrl",
                "jdbc:mysql://localhost:3306/lab_alert_db"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Africa/Johannesburg"
        );

        System.setProperty(
                "labalert.testDbUser",
                "root"
        );

        System.setProperty(
                "labalert.testDbPassword",
                "Labsystem@6724"
        );
    }

    @Before
    public void setUp() throws SQLException {

        alertDAO = new AlertDAO();

        /*
         * Find the newest unacknowledged alert that:
         *
         * 1. Has an EMAIL_SENT notification status.
         * 2. Belongs to a patient assigned to a pathologist.
         * 3. Is assigned to an active pathologist.
         *
         * This test should run against a development or test database.
         */
        String sql
                = "SELECT "
                + "a.id AS alert_id, "
                + "a.notification_status, "
                + "a.acknowledged, "
                + "a.acknowledged_by, "
                + "a.acknowledged_at, "
                + "a.acknowledged_by_pathologist_id, "
                + "p.pathologist_id "
                + "FROM alerts a "
                + "JOIN patients p "
                + "ON p.id = a.patient_id "
                + "JOIN pathologists ph "
                + "ON ph.id = p.pathologist_id "
                + "WHERE a.notification_status = 'EMAIL_SENT' "
                + "AND a.acknowledged = FALSE "
                + "AND ph.active = TRUE "
                + "ORDER BY a.id DESC "
                + "LIMIT 1";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            assertTrue(
                    "The test database must contain at least one "
                    + "unacknowledged EMAIL_SENT alert assigned "
                    + "to an active pathologist.",
                    resultSet.next()
            );

            testAlertId
                    = resultSet.getInt("alert_id");

            assignedPathologistId
                    = resultSet.getInt("pathologist_id");

            originalNotificationStatus
                    = resultSet.getString(
                            "notification_status"
                    );

            originalAcknowledged
                    = resultSet.getBoolean(
                            "acknowledged"
                    );

            originalAcknowledgedBy
                    = resultSet.getString(
                            "acknowledged_by"
                    );

            originalAcknowledgedAt
                    = resultSet.getTimestamp(
                            "acknowledged_at"
                    );

            originalAcknowledgedByPathologistId
                    = resultSet.getObject(
                            "acknowledged_by_pathologist_id",
                            Integer.class
                    );
        }
    }

    @After
    public void tearDown() throws SQLException {

        /*
         * Restore the selected alert to its original state.
         *
         * This prevents the regression test from permanently
         * modifying development data.
         */
        if (testAlertId <= 0) {
            return;
        }

        String sql
                = "UPDATE alerts "
                + "SET notification_status = ?, "
                + "acknowledged = ?, "
                + "acknowledged_by = ?, "
                + "acknowledged_at = ?, "
                + "acknowledged_by_pathologist_id = ? "
                + "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    originalNotificationStatus
            );

            statement.setBoolean(
                    2,
                    originalAcknowledged
            );

            if (originalAcknowledgedBy == null) {
                statement.setNull(
                        3,
                        Types.VARCHAR
                );
            } else {
                statement.setString(
                        3,
                        originalAcknowledgedBy
                );
            }

            if (originalAcknowledgedAt == null) {
                statement.setNull(
                        4,
                        Types.TIMESTAMP
                );
            } else {
                statement.setTimestamp(
                        4,
                        originalAcknowledgedAt
                );
            }

            if (originalAcknowledgedByPathologistId == null) {
                statement.setNull(
                        5,
                        Types.INTEGER
                );
            } else {
                statement.setInt(
                        5,
                        originalAcknowledgedByPathologistId
                );
            }

            statement.setInt(
                    6,
                    testAlertId
            );

            statement.executeUpdate();
        }
    }

    @Test
    public void acknowledgementMustPreserveEmailSentStatus()
            throws SQLException {

        /*
         * Act:
         * Acknowledge the alert using the pathologist assigned
         * to the related patient.
         */
        boolean updated
                = alertDAO.acknowledgeByPathologist(
                        testAlertId,
                        assignedPathologistId,
                        "Regression Test Pathologist"
                );

        assertTrue(
                "The assigned pathologist should be able "
                + "to acknowledge the alert.",
                updated
        );

        AlertState state
                = findAlertState(testAlertId);

        assertNotNull(
                "The alert should still exist after acknowledgement.",
                state
        );

        /*
         * Main regression assertion.
         *
         * The test fails if acknowledgement changes
         * notification_status from EMAIL_SENT to ACKNOWLEDGED.
         */
        assertEquals(
                "Acknowledging an alert must preserve "
                + "notification_status = EMAIL_SENT.",
                "EMAIL_SENT",
                state.notificationStatus
        );

        assertTrue(
                "The alert should be marked as acknowledged.",
                state.acknowledged
        );

        assertEquals(
                "The acknowledging pathologist name should be stored.",
                "Regression Test Pathologist",
                state.acknowledgedBy
        );

        assertEquals(
                "The assigned pathologist ID should be stored.",
                Integer.valueOf(assignedPathologistId),
                state.acknowledgedByPathologistId
        );

        assertNotNull(
                "The acknowledgement timestamp should be stored.",
                state.acknowledgedAt
        );
    }

    /**
     * Reads the current state of an alert after acknowledgement.
     *
     * @param alertId alert identifier
     * @return alert state, or null when the alert does not exist
     * @throws SQLException when the query fails
     */
    private AlertState findAlertState(int alertId)
            throws SQLException {

        String sql
                = "SELECT "
                + "notification_status, "
                + "acknowledged, "
                + "acknowledged_by, "
                + "acknowledged_at, "
                + "acknowledged_by_pathologist_id "
                + "FROM alerts "
                + "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, alertId);

            try (ResultSet resultSet
                    = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                AlertState state
                        = new AlertState();

                state.notificationStatus
                        = resultSet.getString(
                                "notification_status"
                        );

                state.acknowledged
                        = resultSet.getBoolean(
                                "acknowledged"
                        );

                state.acknowledgedBy
                        = resultSet.getString(
                                "acknowledged_by"
                        );

                state.acknowledgedAt
                        = resultSet.getTimestamp(
                                "acknowledged_at"
                        );

                state.acknowledgedByPathologistId
                        = resultSet.getObject(
                                "acknowledged_by_pathologist_id",
                                Integer.class
                        );

                return state;
            }
        }
    }

    /**
     * Simple internal object used to hold the alert state read from the
     * database.
     */
    private static class AlertState {

        private String notificationStatus;
        private boolean acknowledged;
        private String acknowledgedBy;
        private Timestamp acknowledgedAt;
        private Integer acknowledgedByPathologistId;
    }
}
