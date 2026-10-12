package za.co.lab.dao;

import org.junit.Before;
import org.junit.Test;

import za.co.lab.model.Pathologist;
import za.co.lab.model.Patient;
import za.co.lab.util.DBUtil;

import java.sql.Connection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the DAO operations used by laboratory result capture.
 *
 * Required read-only database fixtures:
 *
 * Patient 1 / P001 -> Pathologist 5 / PATH001 Patient 2 / P002 -> Pathologist 6
 * / PATH002 Patient 3 / P003 -> Pathologist 7 / PATH003
 *
 * All three pathologists must be active.
 *
 * These tests do not insert, update or delete database records.
 *
 * @author USER
 */
public class ResultCaptureDAORegressionTest {

    private static final String EXPECTED_DATABASE
            = "lab_alert_db";

    private static final int PATIENT_1_ID = 1;
    private static final int PATIENT_2_ID = 2;
    private static final int PATIENT_3_ID = 3;

    private static final String PATIENT_1_CODE
            = "P001";

    private static final String PATIENT_2_CODE
            = "P002";

    private static final String PATIENT_3_CODE
            = "P003";

    private static final int PATHOLOGIST_1_ID = 5;
    private static final int PATHOLOGIST_2_ID = 6;
    private static final int PATHOLOGIST_3_ID = 7;

    private static final String PATHOLOGIST_1_CODE
            = "PATH001";

    private static final String PATHOLOGIST_2_CODE
            = "PATH002";

    private static final String PATHOLOGIST_3_CODE
            = "PATH003";

    private static final int PATHOLOGIST_1_USER_ID = 4;
    private static final int PATHOLOGIST_2_USER_ID = 5;
    private static final int PATHOLOGIST_3_USER_ID = 6;

    private static final int UNKNOWN_PATIENT_ID
            = Integer.MAX_VALUE;

    private static final int UNKNOWN_PATHOLOGIST_ID
            = Integer.MAX_VALUE - 1;

    private static final int UNKNOWN_USER_ID
            = Integer.MAX_VALUE - 2;

    private PatientDAO patientDAO;
    private PathologistDAO pathologistDAO;

    /**
     * Enables direct JDBC test mode and creates fresh DAO instances before each
     * test.
     */
    @Before
    public void setUp() {

        /*
         * JUnit executes outside GlassFish. DBUtil must therefore
         * use its direct JDBC test connection instead of JNDI.
         */
        System.setProperty(
                "labalert.testMode",
                "true"
        );

        patientDAO
                = new PatientDAO();

        pathologistDAO
                = new PathologistDAO();
    }

    /**
     * Confirms that the DAO tests use the expected database.
     *
     * @throws Exception when the connection cannot be inspected
     */
    @Test
    public void daoTestsShouldUseExpectedDatabase()
            throws Exception {

        try (Connection connection
                = DBUtil.getConnection()) {

            assertNotNull(
                    "The test connection must not be null.",
                    connection
            );

            assertFalse(
                    "The test connection must remain open.",
                    connection.isClosed()
            );

            assertEquals(
                    "The DAO tests must use lab_alert_db.",
                    EXPECTED_DATABASE,
                    connection.getCatalog()
            );

            System.out.println(
                    "DAO regression database: "
                    + connection.getCatalog()
            );

            System.out.println(
                    "DAO regression user: "
                    + connection.getMetaData()
                            .getUserName()
            );
        }
    }

    /**
     * Confirms that findAll() returns the expected patient fixtures.
     */
    @Test
    public void findAllShouldReturnExpectedPatients() {

        List<Patient> patients
                = patientDAO.findAll();

        assertNotNull(
                "PatientDAO.findAll() must not return null.",
                patients
        );

        assertTrue(
                "At least three patient fixtures must exist.",
                patients.size() >= 3
        );

        assertTrue(
                "P001 must appear in the complete patient list.",
                containsPatientCode(
                        patients,
                        PATIENT_1_CODE
                )
        );

        assertTrue(
                "P002 must appear in the complete patient list.",
                containsPatientCode(
                        patients,
                        PATIENT_2_CODE
                )
        );

        assertTrue(
                "P003 must appear in the complete patient list.",
                containsPatientCode(
                        patients,
                        PATIENT_3_CODE
                )
        );
    }

    /**
     * Confirms that findById() maps the P001 fixture correctly.
     */
    @Test
    public void findByIdShouldMapPatientCorrectly() {

        Patient patient
                = patientDAO.findById(
                        PATIENT_1_ID
                );

        assertNotNull(
                "Patient 1 must be returned.",
                patient
        );

        assertEquals(
                "Patient ID must match.",
                PATIENT_1_ID,
                patient.getId()
        );

        assertEquals(
                "Patient code must match.",
                PATIENT_1_CODE,
                patient.getPatientCode()
        );

        assertNotNull(
                "Patient 1 must have a pathologist assignment.",
                patient.getPathologistId()
        );

        assertEquals(
                "Patient 1 must belong to pathologist 5.",
                PATHOLOGIST_1_ID,
                patient.getPathologistId()
                        .intValue()
        );

        assertNotNull(
                "Patient first name must be mapped.",
                patient.getFirstName()
        );

        assertFalse(
                "Patient first name must not be blank.",
                patient.getFirstName()
                        .isBlank()
        );

        assertNotNull(
                "Patient last name must be mapped.",
                patient.getLastName()
        );

        assertFalse(
                "Patient last name must not be blank.",
                patient.getLastName()
                        .isBlank()
        );
    }

    /**
     * Confirms that findById() returns null for invalid identifiers.
     */
    @Test
    public void findByIdShouldRejectInvalidIdentifiers() {

        assertNull(
                "Patient ID zero must return null.",
                patientDAO.findById(0)
        );

        assertNull(
                "A negative patient ID must return null.",
                patientDAO.findById(-1)
        );

        assertNull(
                "An unknown patient ID must return null.",
                patientDAO.findById(
                        UNKNOWN_PATIENT_ID
                )
        );
    }

    /**
     * Confirms that existsById() identifies existing and unknown patients
     * correctly.
     */
    @Test
    public void existsByIdShouldValidatePatients() {

        assertTrue(
                "Patient 1 must exist.",
                patientDAO.existsById(
                        PATIENT_1_ID
                )
        );

        assertTrue(
                "Patient 2 must exist.",
                patientDAO.existsById(
                        PATIENT_2_ID
                )
        );

        assertTrue(
                "Patient 3 must exist.",
                patientDAO.existsById(
                        PATIENT_3_ID
                )
        );

        assertFalse(
                "Patient ID zero must not exist.",
                patientDAO.existsById(0)
        );

        assertFalse(
                "A negative patient ID must not exist.",
                patientDAO.existsById(-1)
        );

        assertFalse(
                "An unknown patient ID must not exist.",
                patientDAO.existsById(
                        UNKNOWN_PATIENT_ID
                )
        );
    }

    /**
     * Confirms that findByPathologistId() returns only patients assigned to the
     * requested pathologist.
     */
    @Test
    public void findByPathologistIdShouldReturnAssignedPatients() {

        verifyPatientList(
                patientDAO.findByPathologistId(
                        PATHOLOGIST_1_ID
                ),
                PATHOLOGIST_1_ID,
                PATIENT_1_ID,
                PATIENT_1_CODE
        );

        verifyPatientList(
                patientDAO.findByPathologistId(
                        PATHOLOGIST_2_ID
                ),
                PATHOLOGIST_2_ID,
                PATIENT_2_ID,
                PATIENT_2_CODE
        );

        verifyPatientList(
                patientDAO.findByPathologistId(
                        PATHOLOGIST_3_ID
                ),
                PATHOLOGIST_3_ID,
                PATIENT_3_ID,
                PATIENT_3_CODE
        );
    }

    /**
     * Confirms that the descriptive alias returns the same records as
     * findByPathologistId().
     */
    @Test
    public void findAllByPathologistIdShouldMatchOriginalMethod() {

        List<Patient> originalPatients
                = patientDAO.findByPathologistId(
                        PATHOLOGIST_1_ID
                );

        List<Patient> aliasPatients
                = patientDAO.findAllByPathologistId(
                        PATHOLOGIST_1_ID
                );

        assertNotNull(
                "The original patient list must not be null.",
                originalPatients
        );

        assertNotNull(
                "The alias patient list must not be null.",
                aliasPatients
        );

        assertEquals(
                "Both methods must return the same number "
                + "of patients.",
                originalPatients.size(),
                aliasPatients.size()
        );

        assertTrue(
                "Both methods must include P001.",
                containsPatientCode(
                        originalPatients,
                        PATIENT_1_CODE
                )
                && containsPatientCode(
                        aliasPatients,
                        PATIENT_1_CODE
                )
        );
    }

    /**
     * Confirms that invalid pathologist identifiers produce empty patient
     * lists.
     */
    @Test
    public void patientLookupShouldReturnEmptyListForInvalidPathologist() {

        List<Patient> zeroIdPatients
                = patientDAO.findAllByPathologistId(0);

        List<Patient> negativeIdPatients
                = patientDAO.findAllByPathologistId(-1);

        List<Patient> unknownIdPatients
                = patientDAO.findAllByPathologistId(
                        UNKNOWN_PATHOLOGIST_ID
                );

        assertNotNull(
                "Zero-ID lookup must return an empty list, not null.",
                zeroIdPatients
        );

        assertNotNull(
                "Negative-ID lookup must return an empty list, "
                + "not null.",
                negativeIdPatients
        );

        assertNotNull(
                "Unknown-ID lookup must return an empty list, "
                + "not null.",
                unknownIdPatients
        );

        assertTrue(
                "Zero-ID lookup must return no patients.",
                zeroIdPatients.isEmpty()
        );

        assertTrue(
                "Negative-ID lookup must return no patients.",
                negativeIdPatients.isEmpty()
        );

        assertTrue(
                "Unknown-ID lookup must return no patients.",
                unknownIdPatients.isEmpty()
        );
    }

    /**
     * Confirms that countByPathologistId() returns a value consistent with the
     * assigned-patient list.
     */
    @Test
    public void countByPathologistIdShouldMatchPatientList() {

        verifyPatientCount(
                PATHOLOGIST_1_ID
        );

        verifyPatientCount(
                PATHOLOGIST_2_ID
        );

        verifyPatientCount(
                PATHOLOGIST_3_ID
        );

        assertEquals(
                "Invalid pathologist ID zero must have no patients.",
                0,
                patientDAO.countByPathologistId(0)
        );

        assertEquals(
                "An unknown pathologist must have no patients.",
                0,
                patientDAO.countByPathologistId(
                        UNKNOWN_PATHOLOGIST_ID
                )
        );
    }

    /**
     * Confirms that findAllActive() returns all required active pathologist
     * fixtures.
     */
    @Test
    public void findAllActiveShouldReturnExpectedPathologists() {

        List<Pathologist> pathologists
                = pathologistDAO.findAllActive();

        assertNotNull(
                "PathologistDAO.findAllActive() must not return null.",
                pathologists
        );

        assertTrue(
                "At least three active pathologists must exist.",
                pathologists.size() >= 3
        );

        assertTrue(
                "PATH001 must appear in the active-pathologist list.",
                containsEmployeeCode(
                        pathologists,
                        PATHOLOGIST_1_CODE
                )
        );

        assertTrue(
                "PATH002 must appear in the active-pathologist list.",
                containsEmployeeCode(
                        pathologists,
                        PATHOLOGIST_2_CODE
                )
        );

        assertTrue(
                "PATH003 must appear in the active-pathologist list.",
                containsEmployeeCode(
                        pathologists,
                        PATHOLOGIST_3_CODE
                )
        );

        for (Pathologist pathologist : pathologists) {

            assertNotNull(
                    "The active-pathologist list must not contain "
                    + "null records.",
                    pathologist
            );

            assertTrue(
                    "Every record returned by findAllActive() "
                    + "must be active.",
                    pathologist.isActive()
            );
        }
    }

    /**
     * Confirms that existsActive() identifies valid and invalid pathologist
     * identifiers.
     */
    @Test
    public void existsActiveShouldValidatePathologists() {

        assertTrue(
                "Pathologist 5 must exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_1_ID
                )
        );

        assertTrue(
                "Pathologist 6 must exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_2_ID
                )
        );

        assertTrue(
                "Pathologist 7 must exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_3_ID
                )
        );

        assertFalse(
                "Pathologist ID zero must be rejected.",
                pathologistDAO.existsActive(0)
        );

        assertFalse(
                "A negative pathologist ID must be rejected.",
                pathologistDAO.existsActive(-1)
        );

        assertFalse(
                "An unknown pathologist ID must be rejected.",
                pathologistDAO.existsActive(
                        UNKNOWN_PATHOLOGIST_ID
                )
        );
    }

    /**
     * Confirms that findActiveById() maps a pathologist fixture and rejects
     * invalid identifiers.
     */
    @Test
    public void findActiveByIdShouldMapPathologistCorrectly() {

        Pathologist pathologist
                = pathologistDAO.findActiveById(
                        PATHOLOGIST_1_ID
                );

        assertNotNull(
                "Pathologist 5 must be returned.",
                pathologist
        );

        assertEquals(
                "Pathologist table ID must match.",
                PATHOLOGIST_1_ID,
                pathologist.getId()
        );

        assertEquals(
                "Employee code must match.",
                PATHOLOGIST_1_CODE,
                pathologist.getEmployeeCode()
        );

        assertEquals(
                "Linked user ID must match.",
                PATHOLOGIST_1_USER_ID,
                pathologist.getUserId()
        );

        assertTrue(
                "The returned pathologist must be active.",
                pathologist.isActive()
        );

        assertNotNull(
                "The pathologist full name must be mapped.",
                pathologist.getFullName()
        );

        assertFalse(
                "The pathologist full name must not be blank.",
                pathologist.getFullName()
                        .isBlank()
        );

        assertNull(
                "Pathologist ID zero must return null.",
                pathologistDAO.findActiveById(0)
        );

        assertNull(
                "An unknown pathologist must return null.",
                pathologistDAO.findActiveById(
                        UNKNOWN_PATHOLOGIST_ID
                )
        );
    }

    /**
     * Confirms that findByUserId() returns linked pathologist profiles.
     */
    @Test
    public void findByUserIdShouldReturnLinkedProfile() {

        Pathologist pathologist1
                = pathologistDAO.findByUserId(
                        PATHOLOGIST_1_USER_ID
                );

        Pathologist pathologist2
                = pathologistDAO.findByUserId(
                        PATHOLOGIST_2_USER_ID
                );

        Pathologist pathologist3
                = pathologistDAO.findByUserId(
                        PATHOLOGIST_3_USER_ID
                );

        assertNotNull(
                "User 4 must have a linked pathologist profile.",
                pathologist1
        );

        assertNotNull(
                "User 5 must have a linked pathologist profile.",
                pathologist2
        );

        assertNotNull(
                "User 6 must have a linked pathologist profile.",
                pathologist3
        );

        assertEquals(
                PATHOLOGIST_1_CODE,
                pathologist1.getEmployeeCode()
        );

        assertEquals(
                PATHOLOGIST_2_CODE,
                pathologist2.getEmployeeCode()
        );

        assertEquals(
                PATHOLOGIST_3_CODE,
                pathologist3.getEmployeeCode()
        );

        assertNull(
                "User ID zero must return null.",
                pathologistDAO.findByUserId(0)
        );

        assertNull(
                "An unknown user must return null.",
                pathologistDAO.findByUserId(
                        UNKNOWN_USER_ID
                )
        );
    }

    /**
     * Confirms that findActiveByUserId() returns active linked profiles.
     */
    @Test
    public void findActiveByUserIdShouldReturnActiveProfile() {

        Pathologist pathologist
                = pathologistDAO.findActiveByUserId(
                        PATHOLOGIST_1_USER_ID
                );

        assertNotNull(
                "User 4 must have an active pathologist profile.",
                pathologist
        );

        assertEquals(
                PATHOLOGIST_1_ID,
                pathologist.getId()
        );

        assertTrue(
                "The returned profile must be active.",
                pathologist.isActive()
        );

        assertNull(
                "An unknown user must not have an active profile.",
                pathologistDAO.findActiveByUserId(
                        UNKNOWN_USER_ID
                )
        );
    }

    /**
     * Confirms that findByPatientId() returns the pathologist assigned to each
     * patient.
     */
    @Test
    public void findByPatientIdShouldReturnAssignedPathologist() {

        Pathologist patient1Pathologist
                = pathologistDAO.findByPatientId(
                        PATIENT_1_ID
                );

        Pathologist patient2Pathologist
                = pathologistDAO.findByPatientId(
                        PATIENT_2_ID
                );

        Pathologist patient3Pathologist
                = pathologistDAO.findByPatientId(
                        PATIENT_3_ID
                );

        assertNotNull(
                "P001 must have an assigned pathologist.",
                patient1Pathologist
        );

        assertNotNull(
                "P002 must have an assigned pathologist.",
                patient2Pathologist
        );

        assertNotNull(
                "P003 must have an assigned pathologist.",
                patient3Pathologist
        );

        assertEquals(
                PATHOLOGIST_1_ID,
                patient1Pathologist.getId()
        );

        assertEquals(
                PATHOLOGIST_2_ID,
                patient2Pathologist.getId()
        );

        assertEquals(
                PATHOLOGIST_3_ID,
                patient3Pathologist.getId()
        );

        assertEquals(
                PATHOLOGIST_1_CODE,
                patient1Pathologist.getEmployeeCode()
        );

        assertEquals(
                PATHOLOGIST_2_CODE,
                patient2Pathologist.getEmployeeCode()
        );

        assertEquals(
                PATHOLOGIST_3_CODE,
                patient3Pathologist.getEmployeeCode()
        );

        assertNull(
                "Patient ID zero must return no pathologist.",
                pathologistDAO.findByPatientId(0)
        );

        assertNull(
                "An unknown patient must return no pathologist.",
                pathologistDAO.findByPatientId(
                        UNKNOWN_PATIENT_ID
                )
        );
    }

    /**
     * Checks whether a patient list contains the expected code.
     *
     * @param patients patient list
     * @param patientCode expected patient code
     * @return true when the patient code is present
     */
    private boolean containsPatientCode(
            List<Patient> patients,
            String patientCode) {

        if (patients == null
                || patientCode == null) {

            return false;
        }

        for (Patient patient : patients) {

            if (patient != null
                    && patientCode.equals(
                            patient.getPatientCode()
                    )) {

                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a pathologist list contains the expected employee code.
     *
     * @param pathologists pathologist list
     * @param employeeCode expected employee code
     * @return true when the employee code is present
     */
    private boolean containsEmployeeCode(
            List<Pathologist> pathologists,
            String employeeCode) {

        if (pathologists == null
                || employeeCode == null) {

            return false;
        }

        for (Pathologist pathologist : pathologists) {

            if (pathologist != null
                    && employeeCode.equals(
                            pathologist.getEmployeeCode()
                    )) {

                return true;
            }
        }

        return false;
    }

    /**
     * Verifies a pathologist-filtered patient list.
     *
     * @param patients patient list
     * @param expectedPathologistId expected pathologist ID
     * @param expectedPatientId expected patient ID
     * @param expectedPatientCode expected patient code
     */
    private void verifyPatientList(
            List<Patient> patients,
            int expectedPathologistId,
            int expectedPatientId,
            String expectedPatientCode) {

        assertNotNull(
                "The patient list must not be null.",
                patients
        );

        assertFalse(
                "The patient list must not be empty.",
                patients.isEmpty()
        );

        boolean expectedPatientFound
                = false;

        for (Patient patient : patients) {

            assertNotNull(
                    "The patient list must not contain null records.",
                    patient
            );

            assertNotNull(
                    "Each patient must have a pathologist ID.",
                    patient.getPathologistId()
            );

            assertEquals(
                    "Every returned patient must belong to "
                    + "the requested pathologist.",
                    expectedPathologistId,
                    patient.getPathologistId()
                            .intValue()
            );

            if (patient.getId() == expectedPatientId) {

                expectedPatientFound
                        = true;

                assertEquals(
                        "The expected patient code must match.",
                        expectedPatientCode,
                        patient.getPatientCode()
                );
            }
        }

        assertTrue(
                "The expected assigned patient must appear "
                + "in the result.",
                expectedPatientFound
        );
    }

    /**
     * Verifies that the DAO count matches the number of assigned patients
     * returned by the list query.
     *
     * @param pathologistId pathologist identifier
     */
    private void verifyPatientCount(
            int pathologistId) {

        List<Patient> patients
                = patientDAO.findAllByPathologistId(
                        pathologistId
                );

        assertNotNull(
                "The assigned-patient list must not be null.",
                patients
        );

        assertEquals(
                "The count must match the assigned-patient list.",
                patients.size(),
                patientDAO.countByPathologistId(
                        pathologistId
                )
        );
    }
}
