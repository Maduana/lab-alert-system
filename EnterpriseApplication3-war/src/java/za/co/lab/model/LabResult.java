package za.co.lab.model;

import java.sql.Timestamp;

public class LabResult {
    private int id;
    private int patientId;
    private String patientCode;
    private String patientName;
    private String testName;
    private double resultValue;
    private String unit;
    private double normalMin;
    private double normalMax;
    private String resultStatus;
    private double aiScore;
    private String resultComment;
    private Timestamp capturedAt;

    public LabResult() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getNormalMin() { return normalMin; }
    public void setNormalMin(double normalMin) { this.normalMin = normalMin; }

    public double getNormalMax() { return normalMax; }
    public void setNormalMax(double normalMax) { this.normalMax = normalMax; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public double getAiScore() { return aiScore; }
    public void setAiScore(double aiScore) { this.aiScore = aiScore; }

    public String getResultComment() { return resultComment; }
    public void setResultComment(String resultComment) { this.resultComment = resultComment; }

    public Timestamp getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Timestamp capturedAt) { this.capturedAt = capturedAt; }
}
