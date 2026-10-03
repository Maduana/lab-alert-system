package za.co.lab.dao;

import za.co.lab.model.Pathologist;
import za.co.lab.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PathologistDAO {

    public Pathologist findByPatientId(int patientId) {
        String sql = "SELECT p.*, u.full_name "
                + "FROM pathologists p "
                + "JOIN patients pt ON pt.pathologist_id = p.id "
                + "JOIN users u ON p.user_id = u.id "
                + "WHERE pt.id = ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Pathologist p = new Pathologist();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setEmployeeCode(rs.getString("employee_code"));
                p.setRegistrationNo(rs.getString("registration_no"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setSpecialty(rs.getString("specialty"));
                p.setActive(rs.getBoolean("active"));
                p.setFullName(rs.getString("full_name"));
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Pathologist findByUserId(int userId) {
        String sql = "SELECT p.*, u.full_name "
                + "FROM pathologists p "
                + "JOIN users u ON p.user_id = u.id "
                + "WHERE p.user_id = ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Pathologist p = new Pathologist();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setEmployeeCode(rs.getString("employee_code"));
                p.setRegistrationNo(rs.getString("registration_no"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setSpecialty(rs.getString("specialty"));
                p.setActive(rs.getBoolean("active"));
                p.setFullName(rs.getString("full_name"));
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}