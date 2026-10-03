package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/alerts")
public class AlertsServlet extends HttpServlet {

    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("loggedInUser");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=Please login first");
            return;
        }

        if ("PATHOLOGIST".equalsIgnoreCase(user.getRole()) && user.getPathologistId() != null) {
            req.setAttribute("alerts", alertDAO.findAllByPathologistId(user.getPathologistId()));
        } else {
            req.setAttribute("alerts", alertDAO.findAll());
        }

        req.getRequestDispatcher("/alerts.jsp").forward(req, resp);
    }
}