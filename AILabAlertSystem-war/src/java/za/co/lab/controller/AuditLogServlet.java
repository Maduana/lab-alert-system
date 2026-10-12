package za.co.lab.controller;

import za.co.lab.dao.AuditLogDAO;
import za.co.lab.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/audit-logs")
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("loggedInUser");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=Please login first");
            return;
        }

        // ✅ ADMIN ONLY
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Access denied");
            return;
        }

        int limit = 200;
        String limitParam = req.getParameter("limit");
        if (limitParam != null) {
            try {
                limit = Integer.parseInt(limitParam);
            } catch (NumberFormatException ignored) {}
        }

        req.setAttribute("logs", auditLogDAO.findLatest(limit));
        req.setAttribute("limit", limit);

        req.getRequestDispatcher("/audit-logs.jsp").forward(req, resp);
    }
}