package za.co.lab.controller;

import za.co.lab.dao.AlertDAO;
import za.co.lab.dao.LabResultDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.LabResult;
import za.co.lab.model.User;
import za.co.lab.util.EmailLinkUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/email-view")
public class EmailAlertViewServlet extends HttpServlet {

    private final AlertDAO alertDAO = new AlertDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            int alertId = Integer.parseInt(req.getParameter("alertId"));
            long exp = Long.parseLong(req.getParameter("exp"));
            String sig = req.getParameter("sig");

            if (!EmailLinkUtil.isValid(alertId, "view", exp, sig)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired email link.");
                return;
            }

            User user = (User) req.getSession().getAttribute("loggedInUser");

            if (user == null) {
                req.getSession().setAttribute("postLoginRedirect", req.getRequestURI() + "?" + req.getQueryString());
                resp.sendRedirect(req.getContextPath() + "/index.jsp?error=Please login to view alert");
                return;
            }

            if ("PATHOLOGIST".equalsIgnoreCase(user.getRole())) {
                if (user.getPathologistId() == null || !alertDAO.isOwnedByPathologist(alertId, user.getPathologistId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not allowed to view this alert.");
                    return;
                }
            }

            Alert alert = alertDAO.findById(alertId);
            LabResult result = labResultDAO.findById(alert.getResultId());

            String ackSig = EmailLinkUtil.sign(alertId, "ack", exp);

            req.setAttribute("alert", alert);
            req.setAttribute("result", result);
            req.setAttribute("ackUrl", req.getContextPath() + "/email-acknowledge");
            req.setAttribute("exp", exp);
            req.setAttribute("sig", ackSig);

            req.getRequestDispatcher("/email-alert-view.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
        }
    }
}