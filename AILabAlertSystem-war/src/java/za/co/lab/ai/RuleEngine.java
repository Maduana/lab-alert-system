package za.co.lab.ai;

import za.co.lab.model.Alert;
import za.co.lab.model.LabResult;

public class RuleEngine {

    public static class EvaluationResult {
        private String status;
        private String severity;
        private double aiScore;
        private String comment;
        private boolean requiresAlert;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public double getAiScore() { return aiScore; }
        public void setAiScore(double aiScore) { this.aiScore = aiScore; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public boolean isRequiresAlert() { return requiresAlert; }
        public void setRequiresAlert(boolean requiresAlert) { this.requiresAlert = requiresAlert; }
    }

    public EvaluationResult evaluate(LabResult current, Double previousValue) {
        EvaluationResult out = new EvaluationResult();

        double value = current.getResultValue();
        String test = current.getTestName().trim().toLowerCase();

        boolean outOfRange = value < current.getNormalMin() || value > current.getNormalMax();

        out.setStatus(outOfRange ? "ABNORMAL" : "NORMAL");
        out.setSeverity(outOfRange ? "WARNING" : "INFO");
        out.setAiScore(outOfRange ? 0.65 : 0.10);
        out.setComment(outOfRange ? "Outside normal reference range" : "Within normal reference range");
        out.setRequiresAlert(outOfRange);

        if ("glucose".equals(test)) {
            if (value < 3.0 || value > 11.1) {
                setCritical(out, "Critical glucose level detected");
            }
        } else if ("potassium".equals(test)) {
            if (value < 2.8 || value > 6.0) {
                setCritical(out, "Critical potassium level detected");
            }
        } else if ("hemoglobin".equals(test)) {
            if (value < 7.0) {
                setCritical(out, "Critical hemoglobin level detected");
            }
        } else if ("wbc".equals(test) || "white blood cells".equals(test)) {
            if (value < 2.0 || value > 25.0) {
                setCritical(out, "Critical white blood cell count detected");
            }
        } else if ("platelets".equals(test)) {
            if (value < 50.0) {
                setCritical(out, "Critical platelet count detected");
            }
        }

        if (previousValue != null && previousValue > 0) {
            double changePct = Math.abs((value - previousValue) / previousValue) * 100.0;
            if (changePct >= 30.0) {
                out.setAiScore(Math.min(0.99, out.getAiScore() + 0.20));
                out.setComment(out.getComment() + "; Significant historical change (" + String.format("%.1f", changePct) + "%)");
                out.setRequiresAlert(true);

                if (!"CRITICAL".equals(out.getSeverity())) {
                    out.setSeverity("WARNING");
                    out.setStatus("ABNORMAL");
                }
            }
        }

        return out;
    }

    private void setCritical(EvaluationResult out, String message) {
        out.setStatus("CRITICAL");
        out.setSeverity("CRITICAL");
        out.setAiScore(0.95);
        out.setComment(message);
        out.setRequiresAlert(true);
    }

    public Alert buildAlert(int resultId, int patientId, LabResult result, EvaluationResult evaluation) {
        Alert alert = new Alert();
        alert.setResultId(resultId);
        alert.setPatientId(patientId);
        alert.setSeverity(evaluation.getSeverity());
        alert.setAlertMessage(
                result.getTestName() + " = " + result.getResultValue() + " " + result.getUnit()
                + " | " + evaluation.getComment()
        );
        alert.setNotificationStatus("PENDING");
        alert.setAcknowledged(false);
        return alert;
    }
}