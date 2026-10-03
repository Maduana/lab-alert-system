package za.co.lab.service;

import za.co.lab.dao.UserDAO;
import za.co.lab.model.User;
import za.co.lab.security.PasswordUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String rawPassword) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null;
        }

        String enteredHash = PasswordUtil.sha256(rawPassword);

        if (enteredHash.equals(user.getPasswordHash())) {
            return user;
        }

        return null;
    }
}