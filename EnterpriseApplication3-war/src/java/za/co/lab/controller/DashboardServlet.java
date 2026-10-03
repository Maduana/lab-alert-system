package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.dao.LabResultDAO;
import za.co.lab.dao.PatientDAO;
import za.co.lab.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final AlertDAO alertDAO = new AlertDAO();
    private final PatientDAO patientDAO = new PatientDAO();

   @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    User user = (User) req.getSession().getAttribute("loggedInUser");

    if (user == null) {
        resp.sendRedirect(req.getContextPath()
                + "/index.jsp?error=Please login first");
        return;
    }

    if ("PATHOLOGIST".equalsIgnoreCase(user.getRole())
            && user.getPathologistId() != null) {

        int pathologistId = user.getPathologistId();

        var latestResults =
                labResultDAO.findLatestResultsByPathologistId(pathologistId);

        var alerts =
                alertDAO.findAllByPathologistId(pathologistId);

        var patients =
                patientDAO.findByPathologistId(pathologistId);

        req.setAttribute("latestResults", latestResults);
        req.setAttribute("alerts", alerts);
        req.setAttribute("patients", patients);

        req.setAttribute("totalPatients", patients.size());
        req.setAttribute("totalResults", latestResults.size());
        req.setAttribute("totalAlerts", alerts.size());

        req.setAttribute(
                "criticalAlerts",
                alertDAO.countPendingCriticalByPathologistId(pathologistId));

        req.setAttribute(
                "pendingCriticalCount",
                alertDAO.countPendingCriticalByPathologistId(pathologistId));

        req.setAttribute("pathologistView", true);

    } else {

        var latestResults = labResultDAO.findLatestResults();
        var alerts = alertDAO.findAll();
        var patients = patientDAO.findAll();

        req.setAttribute("latestResults", latestResults);
        req.setAttribute("alerts", alerts);
        req.setAttribute("patients", patients);

        req.setAttribute("totalPatients", patients.size());
        req.setAttribute("totalResults", latestResults.size());
        req.setAttribute("totalAlerts", alerts.size());

        req.setAttribute(
                "criticalAlerts",
                alertDAO.countPendingCritical());

        req.setAttribute(
                "pendingCriticalCount",
                alertDAO.countPendingCritical());

        req.setAttribute("pathologistView", false);
    }

    req.getRequestDispatcher("/dashboard.jsp")
            .forward(req, resp);
}
}