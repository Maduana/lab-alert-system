package za.co.lab.dao;

import za.co.lab.model.LabResult;
import za.co.lab.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LabResultDAO {

    public int insert(LabResult result) {
        String sql = "INSERT INTO lab_results "
                + "(patient_id, test_name, result_value, unit, normal_min, normal_max, result_status, ai_score, result_comment) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, result.getPatientId());
            ps.setString(2, result.getTestName());
            ps.setDouble(3, result.getResultValue());
            ps.setString(4, result.getUnit());
            ps.setDouble(5, result.getNormalMin());
            ps.setDouble(6, result.getNormalMax());
            ps.setString(7, result.getResultStatus());
            ps.setDouble(8, result.getAiScore());
            ps.setString(9, result.getResultComment());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public Double findPreviousValue(int patientId, String testName) {
        String sql = "SELECT result_value FROM lab_results "
                + "WHERE patient_id = ? AND test_name = ? "
                + "ORDER BY captured_at DESC LIMIT 1";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setString(2, testName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("result_value");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public LabResult findById(int resultId) {
        String sql = "SELECT lr.*, p.patient_code, CONCAT(p.first_name, ' ', p.last_name) AS patient_name "
                + "FROM lab_results lr "
                + "JOIN patients p ON lr.patient_id = p.id "
                + "WHERE lr.id = ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resultId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LabResult r = new LabResult();
                r.setId(rs.getInt("id"));
                r.setPatientId(rs.getInt("patient_id"));
                r.setPatientCode(rs.getString("patient_code"));
                r.setPatientName(rs.getString("patient_name"));
                r.setTestName(rs.getString("test_name"));
                r.setResultValue(rs.getDouble("result_value"));
                r.setUnit(rs.getString("unit"));
                r.setNormalMin(rs.getDouble("normal_min"));
                r.setNormalMax(rs.getDouble("normal_max"));
                r.setResultStatus(rs.getString("result_status"));
                r.setAiScore(rs.getDouble("ai_score"));
                r.setResultComment(rs.getString("result_comment"));
                r.setCapturedAt(rs.getTimestamp("captured_at"));
                return r;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<LabResult> findLatestResults() {
        List<LabResult> results = new ArrayList<>();

        String sql = "SELECT lr.*, p.patient_code, CONCAT(p.first_name, ' ', p.last_name) AS patient_name "
                + "FROM lab_results lr "
                + "JOIN patients p ON lr.patient_id = p.id "
                + "ORDER BY lr.captured_at DESC LIMIT 20";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LabResult r = new LabResult();
                r.setId(rs.getInt("id"));
                r.setPatientId(rs.getInt("patient_id"));
                r.setPatientCode(rs.getString("patient_code"));
                r.setPatientName(rs.getString("patient_name"));
                r.setTestName(rs.getString("test_name"));
                r.setResultValue(rs.getDouble("result_value"));
                r.setUnit(rs.getString("unit"));
                r.setNormalMin(rs.getDouble("normal_min"));
                r.setNormalMax(rs.getDouble("normal_max"));
                r.setResultStatus(rs.getString("result_status"));
                r.setAiScore(rs.getDouble("ai_score"));
                r.setResultComment(rs.getString("result_comment"));
                r.setCapturedAt(rs.getTimestamp("captured_at"));
                results.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<LabResult> findLatestResultsByPathologistId(int pathologistId) {
        List<LabResult> results = new ArrayList<>();

        String sql = "SELECT lr.*, p.patient_code, CONCAT(p.first_name, ' ', p.last_name) AS patient_name "
                + "FROM lab_results lr "
                + "JOIN patients p ON lr.patient_id = p.id "
                + "WHERE p.pathologist_id = ? "
                + "ORDER BY lr.captured_at DESC";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pathologistId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LabResult r = new LabResult();
                r.setId(rs.getInt("id"));
                r.setPatientId(rs.getInt("patient_id"));
                r.setPatientCode(rs.getString("patient_code"));
                r.setPatientName(rs.getString("patient_name"));
                r.setTestName(rs.getString("test_name"));
                r.setResultValue(rs.getDouble("result_value"));
                r.setUnit(rs.getString("unit"));
                r.setNormalMin(rs.getDouble("normal_min"));
                r.setNormalMax(rs.getDouble("normal_max"));
                r.setResultStatus(rs.getString("result_status"));
                r.setAiScore(rs.getDouble("ai_score"));
                r.setResultComment(rs.getString("result_comment"));
                r.setCapturedAt(rs.getTimestamp("captured_at"));
                results.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}