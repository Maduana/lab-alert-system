
package za.co.lab.controller;

import za.co.lab.model.User;
import za.co.lab.service.AuditService;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session != null) {
            User user = (User) session.getAttribute("loggedInUser");

            if (user != null) {
                // ✅ AUDIT: logout
                AuditService.log(
                    user,
                    "LOGOUT",
                    "USER",
                    user.getId(),
                    "User logged out"
                );
            }

            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath()
            + "/index.jsp?msg=Logged out successfully");
    }
}
