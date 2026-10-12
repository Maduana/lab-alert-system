package za.co.lab.dao;

import za.co.lab.model.Pathologist;
import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides database operations for pathologist records.
 *
 * @author USER
 */
public class PathologistDAO {

    private static final Logger LOGGER =
            Logger.getLogger(
                    PathologistDAO.class.getName()
            );

    /**
     * Returns all active pathologists for administrator selection.
     *
     * Results are ordered alphabetically by the user's full name.
     *
     * @return active pathologist list
     */
    public List<Pathologist> findAllActive() {

        List<Pathologist> pathologists =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "p.id, "
                + "p.user_id, "
                + "p.employee_code, "
                + "p.registration_no, "
                + "p.email, "
                + "p.phone, "
                + "p.specialty, "
                + "p.active, "
                + "u.full_name "
                + "FROM pathologists p "
                + "JOIN users u "
                + "ON u.id = p.user_id "
                + "WHERE p.active = TRUE "
                + "ORDER BY u.full_name ASC, p.id ASC";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                pathologists.add(
                        mapPathologist(resultSet)
                );
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve active pathologists.",
                    exception
            );
        }

        return pathologists;
    }

    /**
     * Checks whether a pathologist exists and is active.
     *
     * @param pathologistId pathologist identifier
     * @return true when the pathologist exists and is active
     */
    public boolean existsActive(int pathologistId) {

        if (pathologistId <= 0) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM pathologists "
                + "WHERE id = ? "
                + "AND active = TRUE";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    pathologistId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getInt("total") == 1;
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to validate active pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return false;
    }

    /**
     * Finds an active pathologist by the pathologist table ID.
     *
     * @param pathologistId pathologist identifier
     * @return active pathologist, or null when not found
     */
    public Pathologist findActiveById(int pathologistId) {

        if (pathologistId <= 0) {
            return null;
        }

        String sql =
                "SELECT "
                + "p.id, "
                + "p.user_id, "
                + "p.employee_code, "
                + "p.registration_no, "
                + "p.email, "
                + "p.phone, "
                + "p.specialty, "
                + "p.active, "
                + "u.full_name "
                + "FROM pathologists p "
                + "JOIN users u "
                + "ON u.id = p.user_id "
                + "WHERE p.id = ? "
                + "AND p.active = TRUE";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    pathologistId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPathologist(resultSet);
                }
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to find active pathologist ID "
                    + pathologistId,
                    exception
            );
        }

        return null;
    }

    /**
     * Finds the pathologist assigned to a patient.
     *
     * The method returns null when the patient has no assigned
     * pathologist or when the pathologist record does not exist.
     *
     * This method does not require the pathologist to be active,
     * because existing historical records may still need to display
     * the assigned pathologist.
     *
     * @param patientId patient identifier
     * @return assigned pathologist, or null when not found
     */
    public Pathologist findByPatientId(int patientId) {

        if (patientId <= 0) {
            return null;
        }

        String sql =
                "SELECT "
                + "p.id, "
                + "p.user_id, "
                + "p.employee_code, "
                + "p.registration_no, "
                + "p.email, "
                + "p.phone, "
                + "p.specialty, "
                + "p.active, "
                + "u.full_name "
                + "FROM pathologists p "
                + "JOIN patients pt "
                + "ON pt.pathologist_id = p.id "
                + "JOIN users u "
                + "ON u.id = p.user_id "
                + "WHERE pt.id = ?";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    patientId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPathologist(resultSet);
                }
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve the pathologist assigned "
                    + "to patient ID "
                    + patientId,
                    exception
            );
        }

        return null;
    }

    /**
     * Finds a pathologist profile using the linked user ID.
     *
     * This method does not require an active record because login
     * and administrative workflows may need to distinguish between
     * a missing profile and an inactive profile.
     *
     * @param userId user identifier
     * @return linked pathologist, or null when not found
     */
    public Pathologist findByUserId(int userId) {

        if (userId <= 0) {
            return null;
        }

        String sql =
                "SELECT "
                + "p.id, "
                + "p.user_id, "
                + "p.employee_code, "
                + "p.registration_no, "
                + "p.email, "
                + "p.phone, "
                + "p.specialty, "
                + "p.active, "
                + "u.full_name "
                + "FROM pathologists p "
                + "JOIN users u "
                + "ON u.id = p.user_id "
                + "WHERE p.user_id = ?";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPathologist(resultSet);
                }
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve pathologist profile "
                    + "for user ID "
                    + userId,
                    exception
            );
        }

        return null;
    }

    /**
     * Finds an active pathologist profile using the linked user ID.
     *
     * This is useful when an operation requires both a valid profile
     * and active status.
     *
     * @param userId user identifier
     * @return active linked pathologist, or null when not found
     */
    public Pathologist findActiveByUserId(int userId) {

        if (userId <= 0) {
            return null;
        }

        String sql =
                "SELECT "
                + "p.id, "
                + "p.user_id, "
                + "p.employee_code, "
                + "p.registration_no, "
                + "p.email, "
                + "p.phone, "
                + "p.specialty, "
                + "p.active, "
                + "u.full_name "
                + "FROM pathologists p "
                + "JOIN users u "
                + "ON u.id = p.user_id "
                + "WHERE p.user_id = ? "
                + "AND p.active = TRUE";

        try (Connection connection =
                     DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPathologist(resultSet);
                }
            }

        } catch (SQLException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve active pathologist profile "
                    + "for user ID "
                    + userId,
                    exception
            );
        }

        return null;
    }

    /**
     * Maps a joined database result to a Pathologist object.
     *
     * Every query using this mapper must return:
     *
     * id
     * user_id
     * employee_code
     * registration_no
     * email
     * phone
     * specialty
     * active
     * full_name
     *
     * @param resultSet current database row
     * @return populated pathologist object
     * @throws SQLException when a value cannot be read
     */
    private Pathologist mapPathologist(
            ResultSet resultSet)
            throws SQLException {

        Pathologist pathologist =
                new Pathologist();

        pathologist.setId(
                resultSet.getInt("id")
        );

        pathologist.setUserId(
                resultSet.getInt("user_id")
        );

        pathologist.setEmployeeCode(
                resultSet.getString("employee_code")
        );

        pathologist.setRegistrationNo(
                resultSet.getString("registration_no")
        );

        pathologist.setEmail(
                resultSet.getString("email")
        );

        pathologist.setPhone(
                resultSet.getString("phone")
        );

        pathologist.setSpecialty(
                resultSet.getString("specialty")
        );

        pathologist.setActive(
                resultSet.getBoolean("active")
        );

        pathologist.setFullName(
                resultSet.getString("full_name")
        );

        return pathologist;
    }
}