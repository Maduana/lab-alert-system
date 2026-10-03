package za.co.lab.controller;

import za.co.lab.dao.PatientDAO;
import za.co.lab.model.User;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/result-form")
public class ResultFormServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ResultFormServlet.class.getName());

    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Do not create a new session when checking authentication.
         * If no session exists, the user must log in.
         */
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/index.jsp?error=Please login first"
            );
            return;
        }

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/index.jsp?error=Please login first"
            );
            return;
        }

        String role = loggedInUser.getRole();

        if (role == null || role.isBlank()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/dashboard?error=Your account has no valid role"
            );
            return;
        }

        /*
         * Pathologists review and acknowledge alerts.
         * Pathologists may not capture laboratory results.
         */
        if ("PATHOLOGIST".equalsIgnoreCase(role)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/dashboard?error="
                    + "Pathologists may only view and acknowledge alerts"
            );
            return;
        }

        /*
         * Only Administrator and Laboratory Personnel roles
         * may access the result-capture form.
         */
        boolean authorised =
                "ADMIN".equalsIgnoreCase(role)
                || "LABORATORY".equalsIgnoreCase(role)
                || "LABORATORY_PERSONNEL".equalsIgnoreCase(role);

        if (!authorised) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorised to capture laboratory results."
            );
            return;
        }

        try {
            /*
             * Load the patients used by the dropdown in result-form.jsp.
             */
            request.setAttribute(
                    "patients",
                    patientDAO.findAll()
            );

            /*
             * Forward rather than redirect so that the patient list
             * remains available as a request attribute.
             */
            request.getRequestDispatcher("/result-form.jsp")
                    .forward(request, response);

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load the laboratory result form.",
                    exception
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The laboratory result form could not be loaded."
            );
        }
    }
}