package za.co.lab.model;

import java.sql.Timestamp;

public class Alert {
    private int id;
    private int resultId;
    private int patientId;
    private String patientCode;
    private String patientName;
    private String testName;
    private double resultValue;
    private String severity;
    private String alertMessage;
    private String notificationStatus;
    private boolean acknowledged;
    private Integer acknowledgedByPathologistId;
    private String acknowledgedBy;
    private Timestamp acknowledgedAt;
    private Timestamp createdAt;

    public Alert() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public double getResultValue() { return resultValue; }
    public void setResultValue(double resultValue) { this.resultValue = resultValue; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getNotificationStatus() { return notificationStatus; }
    public void setNotificationStatus(String notificationStatus) { this.notificationStatus = notificationStatus; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public Integer getAcknowledgedByPathologistId() { return acknowledgedByPathologistId; }
    public void setAcknowledgedByPathologistId(Integer acknowledgedByPathologistId) { this.acknowledgedByPathologistId = acknowledgedByPathologistId; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public Timestamp getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Timestamp acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
