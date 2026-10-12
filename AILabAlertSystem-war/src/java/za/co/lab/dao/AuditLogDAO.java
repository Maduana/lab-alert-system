package za.co.lab.dao;

import za.co.lab.model.AuditLog;
import za.co.lab.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void log(AuditLog log) {

        String sql = "INSERT INTO audit_logs "
                + "(user_id, username, role, action_type, entity_type, entity_id, action_details) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, log.getUserId());
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getRole());
            ps.setString(4, log.getActionType());
            ps.setString(5, log.getEntityType());
            ps.setObject(6, log.getEntityId());
            ps.setString(7, log.getActionDetails());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AuditLog> findLatest(int limit) {

        List<AuditLog> logs = new ArrayList<>();

        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUserId((Integer) rs.getObject("user_id"));
                log.setUsername(rs.getString("username"));
                log.setRole(rs.getString("role"));
                log.setActionType(rs.getString("action_type"));
                log.setEntityType(rs.getString("entity_type"));
                log.setEntityId((Integer) rs.getObject("entity_id"));
                log.setActionDetails(rs.getString("action_details"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                logs.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }
}