package za.co.lab.dao;

import za.co.lab.model.Patient;
import za.co.lab.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY patient_code";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient p = mapPatient(rs);
                patients.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return patients;
    }

    public List<Patient> findByPathologistId(int pathologistId) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE pathologist_id = ? ORDER BY patient_code";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pathologistId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Patient p = mapPatient(rs);
                patients.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return patients;
    }

    private Patient mapPatient(ResultSet rs) throws Exception {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setDateOfBirth(String.valueOf(rs.getDate("date_of_birth")));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));

        int pid = rs.getInt("pathologist_id");
        if (!rs.wasNull()) {
            p.setPathologistId(pid);
        }

        return p;
    }
}