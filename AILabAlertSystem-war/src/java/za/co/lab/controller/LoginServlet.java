package za.co.lab.controller;

import za.co.lab.model.User;
import za.co.lab.service.AuthService;
import za.co.lab.service.AuditService;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = authService.login(username, password);

        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("loggedInUser", user);

            // ✅ AUDIT: successful login
            AuditService.log(
                user,
                "LOGIN",
                "USER",
                user.getId(),
                "User logged in successfully"
            );

            String redirect = (String) session.getAttribute("postLoginRedirect");
            if (redirect != null && !redirect.trim().isEmpty()) {
                session.removeAttribute("postLoginRedirect");
                resp.sendRedirect(redirect);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } else {

            // ✅ AUDIT: failed login
            AuditService.log(
                null,
                "LOGIN_FAILED",
                "USER",
                null,
                "Failed login attempt for username=" + username
            );

            resp.sendRedirect(req.getContextPath()
                + "/index.jsp?error=Invalid username or password");
        }
    }
}