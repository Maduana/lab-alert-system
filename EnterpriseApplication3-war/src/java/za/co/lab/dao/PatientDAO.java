package za.co.lab.dao;

import za.co.lab.model.Patient;
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
 * Provides database operations for patient records.
 *
 * @author USER
 */
public class PatientDAO {

    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

    /**
     * Returns all patients ordered by patient code.
     *
     * This method is suitable for administrator monitoring views.
     *
     * @return all patients
     */
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();

        String sql = "SELECT "
                + "p.id, "
                + "p.patient_code, "
                + "p.first_name, "
                + "p.last_name, "
                + "p.date_of_birth, "
                + "p.gender, "
                + "p.phone, "
                + "p.pathologist_id "
                + "FROM patients p "
                + "ORDER BY p.patient_code ASC, p.id ASC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve all patients.", exception);
        }

        return patients;
    }

    /**
     * Returns a patient by the patient-table identifier.
     *
     * @param patientId patient identifier
     * @return patient, or null when not found
     */
    public Patient findById(int patientId) {
        if (patientId <= 0) {
            return null;
        }

        String sql = "SELECT "
                + "p.id, "
                + "p.patient_code, "
                + "p.first_name, "
                + "p.last_name, "
                + "p.date_of_birth, "
                + "p.gender, "
                + "p.phone, "
                + "p.pathologist_id "
                + "FROM patients p "
                + "WHERE p.id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPatient(resultSet);
                }
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve patient ID " + patientId, exception);
        }

        return null;
    }

    /**
     * Returns all patients assigned to the supplied pathologist.
     *
     * The method name is retained for compatibility with existing dashboard and servlet code.
     * Only patients assigned to an active pathologist are returned.
     *
     * @param pathologistId pathologist identifier
     * @return assigned patients
     */
    public List<Patient> findByPathologistId(int pathologistId) {
        List<Patient> patients = new ArrayList<>();

        if (pathologistId <= 0) {
            return patients;
        }

        String sql = "SELECT "
                + "p.id, "
                + "p.patient_code, "
                + "p.first_name, "
                + "p.last_name, "
                + "p.date_of_birth, "
                + "p.gender, "
                + "p.phone, "
                + "p.pathologist_id "
                + "FROM patients p "
                + "JOIN pathologists ph ON ph.id = p.pathologist_id "
                + "WHERE p.pathologist_id = ? "
                + "AND ph.active = TRUE "
                + "ORDER BY p.patient_code ASC, p.id ASC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    patients.add(mapPatient(resultSet));
                }
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve patients for pathologist ID " + pathologistId, exception);
        }

        return patients;
    }

    /**
     * Alias for findByPathologistId().
     *
     * This descriptive name is useful in the new result-capture workflow while preserving existing method calls.
     *
     * @param pathologistId pathologist identifier
     * @return assigned patients
     */
    public List<Patient> findAllByPathologistId(int pathologistId) {
        return findByPathologistId(pathologistId);
    }

    /**
     * Checks whether a patient exists.
     *
     * @param patientId patient identifier
     * @return true when the patient exists
     */
    public boolean existsById(int patientId) {
        if (patientId <= 0) {
            return false;
        }

        String sql = "SELECT COUNT(*) AS total FROM patients WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("total") == 1;
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to validate patient ID " + patientId, exception);
        }

        return false;
    }

    /**
     * Checks whether the selected patient is assigned to the selected active pathologist.
     *
     * This method must be called by ResultSubmitServlet even when the patient dropdown is filtered with JavaScript.
     * It prevents a manually modified request from submitting a mismatched patient and pathologist.
     *
     * @param patientId patient identifier
     * @param pathologistId pathologist identifier
     * @return true when the assignment exists and the pathologist is active
     */
    public boolean isAssignedToPathologist(int patientId, int pathologistId) {
        if (patientId <= 0 || pathologistId <= 0) {
            return false;
        }

        String sql = "SELECT COUNT(*) AS total "
                + "FROM patients p "
                + "JOIN pathologists ph ON ph.id = p.pathologist_id "
                + "WHERE p.id = ? "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);
            statement.setInt(2, pathologistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("total") == 1;
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                    "Unable to verify patient ID " + patientId + " assignment to pathologist ID " + pathologistId,
                    exception);
        }

        return false;
    }

    /**
     * Finds a patient only when the patient is assigned to the supplied active pathologist.
     *
     * This method combines patient retrieval and assignment enforcement in one database query.
     *
     * @param patientId patient identifier
     * @param pathologistId pathologist identifier
     * @return assigned patient, or null when the assignment is invalid
     */
    public Patient findByIdAndPathologistId(int patientId, int pathologistId) {
        if (patientId <= 0 || pathologistId <= 0) {
            return null;
        }

        String sql = "SELECT "
                + "p.id, "
                + "p.patient_code, "
                + "p.first_name, "
                + "p.last_name, "
                + "p.date_of_birth, "
                + "p.gender, "
                + "p.phone, "
                + "p.pathologist_id "
                + "FROM patients p "
                + "JOIN pathologists ph ON ph.id = p.pathologist_id "
                + "WHERE p.id = ? "
                + "AND p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);
            statement.setInt(2, pathologistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPatient(resultSet);
                }
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                    "Unable to retrieve patient ID " + patientId + " for pathologist ID " + pathologistId,
                    exception);
        }

        return null;
    }

    /**
     * Returns the number of patients assigned to an active pathologist.
     *
     * This method can be used for dashboard metrics or form validation.
     *
     * @param pathologistId pathologist identifier
     * @return assigned-patient count
     */
    public int countByPathologistId(int pathologistId) {
        if (pathologistId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS total "
                + "FROM patients p "
                + "JOIN pathologists ph ON ph.id = p.pathologist_id "
                + "WHERE p.pathologist_id = ? "
                + "AND ph.active = TRUE";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, pathologistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to count patients for pathologist ID " + pathologistId, exception);
        }

        return 0;
    }

    /**
     * Maps one database row to a Patient object.
     *
     * Every query using this method must return:
     * <ul>
     *   <li>id</li>
     *   <li>patient_code</li>
     *   <li>first_name</li>
     *   <li>last_name</li>
     *   <li>date_of_birth</li>
     *   <li>gender</li>
     *   <li>phone</li>
     *   <li>pathologist_id</li>
     * </ul>
     *
     * @param resultSet current database row
     * @return populated patient
     * @throws SQLException when a column cannot be read
     */
    private Patient mapPatient(ResultSet resultSet) throws SQLException {
        Patient patient = new Patient();

        patient.setId(resultSet.getInt("id"));
        patient.setPatientCode(resultSet.getString("patient_code"));
        patient.setFirstName(resultSet.getString("first_name"));
        patient.setLastName(resultSet.getString("last_name"));

        java.sql.Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth == null) {
            patient.setDateOfBirth(null);
        } else {
            patient.setDateOfBirth(dateOfBirth.toString());
        }

        patient.setGender(resultSet.getString("gender"));
        patient.setPhone(resultSet.getString("phone"));

        int pathologistId = resultSet.getInt("pathologist_id");
        if (!resultSet.wasNull()) {
            patient.setPathologistId(pathologistId);
        }

        return patient;
    }
}