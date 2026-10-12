/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.co.lab.dao;

/**
 *
 * @author USER
 */
import org.junit.Before;
import org.junit.Test;

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
 * Regression tests for patient/pathologist ownership enforcement during
 * laboratory result capture.
 *
 * Required database fixtures:
 *
 * Patient 1 / P001 -> Pathologist 5 / PATH001 Patient 2 / P002 -> Pathologist 6
 * / PATH002 Patient 3 / P003 -> Pathologist 7 / PATH003
 *
 * All three pathologists must be active.
 *
 * These tests are read-only. They do not create, update or delete patients,
 * pathologists, results, alerts or audit records.
 *
 * @author USER
 */
public class ResultCaptureSecurityRegressionTest {

    private static final String EXPECTED_DATABASE
            = "lab_alert_db";

    private static final int PATIENT_1_ID = 1;
    private static final int PATIENT_2_ID = 2;
    private static final int PATIENT_3_ID = 3;

    private static final int PATHOLOGIST_1_ID = 5;
    private static final int PATHOLOGIST_2_ID = 6;
    private static final int PATHOLOGIST_3_ID = 7;

    private static final int UNKNOWN_PATIENT_ID
            = Integer.MAX_VALUE;

    private static final int UNKNOWN_PATHOLOGIST_ID
            = Integer.MAX_VALUE - 1;

    private PatientDAO patientDAO;
    private PathologistDAO pathologistDAO;

    /**
     * Enables direct JDBC test mode and creates fresh DAO objects before every
     * test.
     */
    @Before
    public void setUp() {

        /*
         * JUnit runs outside GlassFish, so DBUtil must use its
         * direct JDBC test connection instead of JNDI.
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
     * Confirms that the tests are connected to the expected schema.
     *
     * @throws Exception when the database connection cannot be read
     */
    @Test
    public void testDatabaseConnectionShouldUseExpectedSchema()
            throws Exception {

        try (Connection connection
                = DBUtil.getConnection()) {

            assertNotNull(
                    "The test database connection must not be null.",
                    connection
            );

            assertFalse(
                    "The test database connection must remain open.",
                    connection.isClosed()
            );

            System.out.println(
                    "Regression-test JDBC URL: "
                    + connection.getMetaData().getURL()
            );

            System.out.println(
                    "Regression-test database user: "
                    + connection.getMetaData().getUserName()
            );

            System.out.println(
                    "Regression-test database catalog: "
                    + connection.getCatalog()
            );

            assertEquals(
                    "The regression suite must use lab_alert_db.",
                    EXPECTED_DATABASE,
                    connection.getCatalog()
            );
        }
    }

    /**
     * Confirms that the three expected patient fixtures exist and have the
     * expected patient codes and pathologist assignments.
     */
    @Test
    public void patientFixturesShouldExist() {

        Patient patient1
                = patientDAO.findById(
                        PATIENT_1_ID
                );

        Patient patient2
                = patientDAO.findById(
                        PATIENT_2_ID
                );

        Patient patient3
                = patientDAO.findById(
                        PATIENT_3_ID
                );

        assertNotNull(
                "Patient 1 must exist.",
                patient1
        );

        assertNotNull(
                "Patient 2 must exist.",
                patient2
        );

        assertNotNull(
                "Patient 3 must exist.",
                patient3
        );

        assertEquals(
                "Patient 1 must be P001.",
                "P001",
                patient1.getPatientCode()
        );

        assertEquals(
                "Patient 2 must be P002.",
                "P002",
                patient2.getPatientCode()
        );

        assertEquals(
                "Patient 3 must be P003.",
                "P003",
                patient3.getPatientCode()
        );

        assertNotNull(
                "P001 must have an assigned pathologist.",
                patient1.getPathologistId()
        );

        assertNotNull(
                "P002 must have an assigned pathologist.",
                patient2.getPathologistId()
        );

        assertNotNull(
                "P003 must have an assigned pathologist.",
                patient3.getPathologistId()
        );

        assertEquals(
                "P001 must be assigned to pathologist 5.",
                PATHOLOGIST_1_ID,
                patient1.getPathologistId().intValue()
        );

        assertEquals(
                "P002 must be assigned to pathologist 6.",
                PATHOLOGIST_2_ID,
                patient2.getPathologistId().intValue()
        );

        assertEquals(
                "P003 must be assigned to pathologist 7.",
                PATHOLOGIST_3_ID,
                patient3.getPathologistId().intValue()
        );
    }

    /**
     * Confirms that the three selected pathologists exist and are active.
     */
    @Test
    public void activePathologistFixturesShouldExist() {

        assertTrue(
                "PATH001 should exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_1_ID
                )
        );

        assertTrue(
                "PATH002 should exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_2_ID
                )
        );

        assertTrue(
                "PATH003 should exist and be active.",
                pathologistDAO.existsActive(
                        PATHOLOGIST_3_ID
                )
        );
    }

    /**
     * Confirms that every valid patient/pathologist assignment is accepted.
     */
    @Test
    public void validPatientAssignmentsShouldBeAccepted() {

        assertTrue(
                "Patient 1 should be assigned to pathologist 5.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertTrue(
                "Patient 2 should be assigned to pathologist 6.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_2_ID,
                        PATHOLOGIST_2_ID
                )
        );

        assertTrue(
                "Patient 3 should be assigned to pathologist 7.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_3_ID,
                        PATHOLOGIST_3_ID
                )
        );
    }

    /**
     * Confirms that patients cannot be submitted under a different pathologist.
     */
    @Test
    public void mismatchedPatientAssignmentsShouldBeRejected() {

        assertFalse(
                "Patient 2 must not be accepted for pathologist 5.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_2_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertFalse(
                "Patient 3 must not be accepted for pathologist 6.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_3_ID,
                        PATHOLOGIST_2_ID
                )
        );

        assertFalse(
                "Patient 1 must not be accepted for pathologist 7.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        PATHOLOGIST_3_ID
                )
        );

        assertFalse(
                "Patient 3 must not be accepted for pathologist 5.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_3_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertFalse(
                "Patient 1 must not be accepted for pathologist 6.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        PATHOLOGIST_2_ID
                )
        );

        assertFalse(
                "Patient 2 must not be accepted for pathologist 7.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_2_ID,
                        PATHOLOGIST_3_ID
                )
        );
    }

    /**
     * Confirms that zero and negative patient identifiers are rejected.
     */
    @Test
    public void nonPositivePatientIdentifiersShouldBeRejected() {

        assertFalse(
                "Patient ID zero must be rejected.",
                patientDAO.isAssignedToPathologist(
                        0,
                        PATHOLOGIST_1_ID
                )
        );

        assertFalse(
                "Negative patient IDs must be rejected.",
                patientDAO.isAssignedToPathologist(
                        -1,
                        PATHOLOGIST_1_ID
                )
        );

        assertNull(
                "Patient ID zero must not return a patient.",
                patientDAO.findByIdAndPathologistId(
                        0,
                        PATHOLOGIST_1_ID
                )
        );

        assertNull(
                "A negative patient ID must not return a patient.",
                patientDAO.findByIdAndPathologistId(
                        -1,
                        PATHOLOGIST_1_ID
                )
        );
    }

    /**
     * Confirms that zero and negative pathologist identifiers are rejected.
     */
    @Test
    public void nonPositivePathologistIdentifiersShouldBeRejected() {

        assertFalse(
                "Pathologist ID zero must be rejected.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        0
                )
        );

        assertFalse(
                "Negative pathologist IDs must be rejected.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        -1
                )
        );

        assertFalse(
                "PathologistDAO must reject ID zero.",
                pathologistDAO.existsActive(0)
        );

        assertFalse(
                "PathologistDAO must reject negative IDs.",
                pathologistDAO.existsActive(-1)
        );

        assertNull(
                "Pathologist ID zero must not return a patient.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_1_ID,
                        0
                )
        );
    }

    /**
     * Confirms that unknown patient and pathologist identifiers are rejected.
     */
    @Test
    public void unknownIdentifiersShouldBeRejected() {

        assertFalse(
                "An unknown patient must not pass validation.",
                patientDAO.isAssignedToPathologist(
                        UNKNOWN_PATIENT_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertFalse(
                "An unknown pathologist must not pass validation.",
                patientDAO.isAssignedToPathologist(
                        PATIENT_1_ID,
                        UNKNOWN_PATHOLOGIST_ID
                )
        );

        assertFalse(
                "An unknown pathologist must not be active.",
                pathologistDAO.existsActive(
                        UNKNOWN_PATHOLOGIST_ID
                )
        );

        assertNull(
                "An unknown patient must not be returned.",
                patientDAO.findByIdAndPathologistId(
                        UNKNOWN_PATIENT_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertNull(
                "A patient must not be returned for an unknown "
                + "pathologist.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_1_ID,
                        UNKNOWN_PATHOLOGIST_ID
                )
        );
    }

    /**
     * Confirms that ownership-aware retrieval returns the correct patient for
     * each valid assignment.
     */
    @Test
    public void ownershipAwareLookupShouldReturnAssignedPatient() {

        Patient patient1
                = patientDAO.findByIdAndPathologistId(
                        PATIENT_1_ID,
                        PATHOLOGIST_1_ID
                );

        Patient patient2
                = patientDAO.findByIdAndPathologistId(
                        PATIENT_2_ID,
                        PATHOLOGIST_2_ID
                );

        Patient patient3
                = patientDAO.findByIdAndPathologistId(
                        PATIENT_3_ID,
                        PATHOLOGIST_3_ID
                );

        assertNotNull(
                "Patient 1 should be returned for pathologist 5.",
                patient1
        );

        assertNotNull(
                "Patient 2 should be returned for pathologist 6.",
                patient2
        );

        assertNotNull(
                "Patient 3 should be returned for pathologist 7.",
                patient3
        );

        assertEquals(
                "P001",
                patient1.getPatientCode()
        );

        assertEquals(
                "P002",
                patient2.getPatientCode()
        );

        assertEquals(
                "P003",
                patient3.getPatientCode()
        );

        assertNotNull(
                "P001 must have a pathologist ID.",
                patient1.getPathologistId()
        );

        assertNotNull(
                "P002 must have a pathologist ID.",
                patient2.getPathologistId()
        );

        assertNotNull(
                "P003 must have a pathologist ID.",
                patient3.getPathologistId()
        );

        assertEquals(
                PATHOLOGIST_1_ID,
                patient1.getPathologistId().intValue()
        );

        assertEquals(
                PATHOLOGIST_2_ID,
                patient2.getPathologistId().intValue()
        );

        assertEquals(
                PATHOLOGIST_3_ID,
                patient3.getPathologistId().intValue()
        );
    }

    /**
     * Confirms that ownership-aware retrieval does not expose a patient
     * belonging to a different pathologist.
     */
    @Test
    public void ownershipAwareLookupShouldNotLeakMismatchedPatient() {

        assertNull(
                "Pathologist 5 must not retrieve patient 2.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_2_ID,
                        PATHOLOGIST_1_ID
                )
        );

        assertNull(
                "Pathologist 6 must not retrieve patient 3.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_3_ID,
                        PATHOLOGIST_2_ID
                )
        );

        assertNull(
                "Pathologist 7 must not retrieve patient 1.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_1_ID,
                        PATHOLOGIST_3_ID
                )
        );

        assertNull(
                "Pathologist 5 must not retrieve patient 3.",
                patientDAO.findByIdAndPathologistId(
                        PATIENT_3_ID,
                        PATHOLOGIST_1_ID
                )
        );
    }

    /**
     * Confirms that each pathologist-specific list contains only patients
     * assigned to that pathologist.
     */
    @Test
    public void pathologistPatientListsShouldBeIsolated() {

        verifyIsolatedPatientList(
                PATHOLOGIST_1_ID,
                PATIENT_1_ID,
                "P001"
        );

        verifyIsolatedPatientList(
                PATHOLOGIST_2_ID,
                PATIENT_2_ID,
                "P002"
        );

        verifyIsolatedPatientList(
                PATHOLOGIST_3_ID,
                PATIENT_3_ID,
                "P003"
        );
    }

    /**
     * Verifies one pathologist's patient list.
     *
     * @param pathologistId expected pathologist identifier
     * @param expectedPatientId expected patient identifier
     * @param expectedPatientCode expected patient code
     */
    private void verifyIsolatedPatientList(
            int pathologistId,
            int expectedPatientId,
            String expectedPatientCode) {

        List<Patient> patients
                = patientDAO.findAllByPathologistId(
                        pathologistId
                );

        assertNotNull(
                "The assigned-patient list must not be null.",
                patients
        );

        assertFalse(
                "The assigned-patient list must not be empty.",
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
                    "Every patient must have a pathologist ID.",
                    patient.getPathologistId()
            );

            assertEquals(
                    "Every returned patient must belong to the "
                    + "requested pathologist.",
                    pathologistId,
                    patient.getPathologistId().intValue()
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
                "The expected patient must appear in the "
                + "pathologist-specific patient list.",
                expectedPatientFound
        );
    }
}
