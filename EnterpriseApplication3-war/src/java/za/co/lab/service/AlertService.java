package za.co.lab.service;

import za.co.lab.ai.RuleEngine;
import za.co.lab.dao.AlertDAO;
import za.co.lab.dao.LabResultDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.LabResult;

public class AlertService {

    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final AlertDAO alertDAO = new AlertDAO();
    private final RuleEngine engine = new RuleEngine();
    private final EmailService emailService = new EmailService();

    public String processResult(LabResult result) {

        Double previousValue =
            labResultDAO.findPreviousValue(result.getPatientId(), result.getTestName());

        RuleEngine.EvaluationResult eval =
            engine.evaluate(result, previousValue);

        result.setResultStatus(eval.getStatus());
        result.setAiScore(eval.getAiScore());
        result.setResultComment(eval.getComment());

        int resultId = labResultDAO.insert(result);

        if (resultId <= 0) {
            return "Failed to save result.";
        }

        if (eval.isRequiresAlert() && !alertDAO.existsByResultId(resultId)) {

            Alert alert =
                engine.buildAlert(resultId, result.getPatientId(), result, eval);

            int alertId = alertDAO.insert(alert);

            // ✅ AUDIT: alert created
            AuditService.log(
                null,
                "ALERT_CREATED",
                "ALERT",
                alertId,
                "Alert generated for patientId=" + result.getPatientId()
            );

            boolean emailSent =
                emailService.sendAlertEmail(result, eval, alertId);

            alertDAO.updateNotificationStatus(
                alertId, emailSent ? "EMAIL_SENT" : "EMAIL_FAILED");

            return emailSent
                ? "Result saved, alert generated, and email sent successfully."
                : "Result saved, alert generated, but email sending failed.";
        }

        return "Result saved successfully.";
    }
}
