package za.co.lab.service;

import za.co.lab.ai.RuleEngine;
import za.co.lab.dao.PathologistDAO;
import za.co.lab.model.LabResult;
import za.co.lab.model.Pathologist;
import za.co.lab.util.EmailLinkUtil;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.naming.InitialContext;

public class EmailService {

    private static final String SENDER_EMAIL = "labresultsalert@gmail.com";
    private static final String SENDER_NAME = "Lab Alert System";

    private static final String BASE_URL =
        "http://localhost:8080/LabAlertSystemWeb";

    private final PathologistDAO pathologistDAO = new PathologistDAO();

    public boolean sendAlertEmail(
            LabResult result,
            RuleEngine.EvaluationResult eval,
            int alertId) {

        try {
            Pathologist pathologist =
                pathologistDAO.findByPatientId(result.getPatientId());

            if (pathologist == null || pathologist.getEmail() == null) {
                AuditService.log(
                    null,
                    "EMAIL_FAILED",
                    "ALERT",
                    alertId,
                    "No pathologist email for patientId=" + result.getPatientId()
                );
                return false;
            }

            String recipientEmail = pathologist.getEmail();

            InitialContext ctx = new InitialContext();
            Session session =
                (Session) ctx.lookup("java:comp/env/mail/LabAlertMail");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipientEmail));

            message.setSubject("[LAB ALERT] "
                + eval.getSeverity() + " - "
                + result.getTestName());

            message.setContent(
                buildHtmlEmail(result, eval, alertId),
                "text/html; charset=UTF-8");

            Transport.send(message);

            // ✅ AUDIT: email sent
            AuditService.log(
                null,
                "EMAIL_SENT",
                "ALERT",
                alertId,
                "Email sent to " + recipientEmail
            );

            return true;

        } catch (Exception e) {

            // ✅ AUDIT: email failed
            AuditService.log(
                null,
                "EMAIL_FAILED",
                "ALERT",
                alertId,
                e.getMessage()
            );

            e.printStackTrace();
            return false;
        }
    }

    private String buildHtmlEmail(
            LabResult result,
            RuleEngine.EvaluationResult eval,
            int alertId) {

        long exp = EmailLinkUtil.generateExpiry();
        String sig = EmailLinkUtil.sign(alertId, "view", exp);

        String link = BASE_URL
            + "/email-view?alertId=" + alertId
            + "&exp=" + exp
            + "&sig=" + sig;

        return "<html><body>"
            + "<h3>Lab Alert</h3>"
            + "<p><b>Test:</b> " + result.getTestName() + "</p>"
            + "<p><b>Result:</b> " + result.getResultValue()
            + " " + result.getUnit() + "</p>"
            + "<p><b>Comment:</b> " + eval.getComment() + "</p>"
            + "<a href='" + link + "'>View Alert</a>"
            + "</body></html>";
    }
}