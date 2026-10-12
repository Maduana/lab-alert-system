package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.model.User;
import za.co.lab.service.AuditService;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/alert-action")
public class AlertActionServlet extends HttpServlet {

    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("loggedInUser");
        int alertId = Integer.parseInt(req.getParameter("alertId"));

        boolean ok =
            alertDAO.acknowledgeByPathologist(
                alertId,
                user.getPathologistId(),
                user.getFullName());

        if (ok) {
            AuditService.log(
                user,
                "ALERT_ACKNOWLEDGED",
                "ALERT",
                alertId,
                "Alert acknowledged by pathologist"
            );
        } else {
            AuditService.log(
                user,
                "UNAUTHORIZED_ACK_ATTEMPT",
                "ALERT",
                alertId,
                "Attempted to acknowledge unassigned alert"
            );
        }

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}