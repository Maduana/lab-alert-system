package za.co.lab.dao;

import za.co.lab.model.User;
import za.co.lab.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT u.*, p.id AS pathologist_id "
                + "FROM users u "
                + "LEFT JOIN pathologists p ON p.user_id = u.id "
                + "WHERE u.username = ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));

                int pid = rs.getInt("pathologist_id");
                if (!rs.wasNull()) {
                    user.setPathologistId(pid);
                }

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}