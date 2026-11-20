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
    private Timestamp reviewDueAt;
    private boolean escalated;
    private int reminderCount;
    private Timestamp lastReminderAt;

    public Alert() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public double getResultValue() {
        return resultValue;
    }

    public void setResultValue(double resultValue) {
        this.resultValue = resultValue;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Integer getAcknowledgedByPathologistId() {
        return acknowledgedByPathologistId;
    }

    public void setAcknowledgedByPathologistId(Integer acknowledgedByPathologistId) {
        this.acknowledgedByPathologistId = acknowledgedByPathologistId;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public Timestamp getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Timestamp acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the deadline by which the alert should be reviewed.
     *
     * @return review deadline
     */
    public Timestamp getReviewDueAt() {

        return reviewDueAt;
    }

    /**
     * Sets the deadline by which the alert should be reviewed.
     *
     * @param reviewDueAt review deadline
     */
    public void setReviewDueAt(
            Timestamp reviewDueAt) {

        this.reviewDueAt
                = reviewDueAt;
    }

    /**
     * Indicates whether the alert has been escalated.
     *
     * @return true when escalated
     */
    public boolean isEscalated() {

        return escalated;
    }

    /**
     * Sets the escalation state.
     *
     * @param escalated escalation state
     */
    public void setEscalated(
            boolean escalated) {

        this.escalated
                = escalated;
    }

    public int getReminderCount() {
        return reminderCount;
    }

    public void setReminderCount(
            int reminderCount) {

        this.reminderCount
                = reminderCount;
    }

    public Timestamp getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(
            Timestamp lastReminderAt) {

        this.lastReminderAt
                = lastReminderAt;
    }

    /**
     * Indicates whether the alert may receive another reminder.
     *
     * An alert is eligible when it is overdue, unacknowledged, below the
     * reminder limit, and outside the fifteen-minute cooldown.
     *
     * @return true when a reminder may be sent
     */
    /**
     * Indicates whether an Administrator may send another reminder.
     *
     * Eligibility requires:
     *
     * 1. The alert remains unacknowledged. 2. The review deadline has passed.
     * 3. Fewer than three reminders have been sent. 4. The fifteen-minute
     * cooldown has passed.
     *
     * @return true when another reminder may be sent
     */
    public boolean isReminderEligible() {

        if (acknowledged
                || reviewDueAt == null
                || reminderCount >= 3) {

            return false;
        }

        long now
                = System.currentTimeMillis();

        /*
     * The reminder action is only available after the review
     * deadline has passed.
         */
        if (reviewDueAt.getTime() >= now) {
            return false;
        }

        /*
     * No previous reminder means the first reminder is available.
         */
        if (lastReminderAt == null) {
            return true;
        }

        long cooldownMilliseconds
                = 15L * 60L * 1000L;

        return now - lastReminderAt.getTime()
                >= cooldownMilliseconds;
    }

    /**
     * Returns the alert's current review status.
     *
     * @return review-status label
     */
    public String getReviewStatus() {

        /*
     * An acknowledged alert can be classified as reviewed on time,
     * reviewed late, or acknowledged with an unavailable timestamp.
         */
        if (acknowledged) {

            if (acknowledgedAt == null) {
                return "ACKNOWLEDGED - TIME UNKNOWN";
            }

            if (reviewDueAt != null
                    && acknowledgedAt.after(reviewDueAt)) {

                return "ACKNOWLEDGED LATE";
            }

            return "ACKNOWLEDGED ON TIME";
        }

        /*
     * An open alert with no deadline cannot be classified against
     * the review target.
         */
        if (reviewDueAt == null) {
            return "DEADLINE NOT CONFIGURED";
        }

        long now
                = System.currentTimeMillis();

        long deadline
                = reviewDueAt.getTime();

        /*
     * The deadline has passed and the alert remains open.
         */
        if (deadline < now) {
            return "OVERDUE";
        }

        /*
     * The alert enters Due Soon during the final five minutes of
     * its review period.
         */
        long dueSoonWindow
                = 5L * 60L * 1000L;

        if (deadline - now <= dueSoonWindow) {
            return "DUE SOON";
        }

        return "ON TIME";
    }
}
