/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.co.lab.model;

/**
 *
 * @author USER
 */
import java.time.LocalDate;

/**
 * Stores filter criteria used by the alert reporting module.
 *
 * The same filter object is used by:
 *
 * 1. The report preview. 2. The PDF export. 3. The Excel export. 4.
 * Administrator-wide reports. 5. Pathologist-assigned-patient reports.
 *
 * Empty text values mean that the corresponding filter is not active.
 *
 * @author USER
 */
public class AlertReportFilter {

    private LocalDate startDate;
    private LocalDate endDate;

    private String severity;
    private String notificationStatus;
    private String acknowledgementStatus;
    private String patientCode;
    private String testName;

    /**
     * Creates an empty report filter.
     */
    public AlertReportFilter() {

        this.startDate = null;
        this.endDate = null;

        this.severity = "";
        this.notificationStatus = "";
        this.acknowledgementStatus = "";
        this.patientCode = "";
        this.testName = "";
    }

    /**
     * Returns the inclusive report start date.
     *
     * @return start date, or null when no start date was selected
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the inclusive report start date.
     *
     * @param startDate report start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the inclusive report end date.
     *
     * @return end date, or null when no end date was selected
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the inclusive report end date.
     *
     * @param endDate report end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the selected severity.
     *
     * Supported values are normally:
     *
     * CRITICAL WARNING
     *
     * @return severity or an empty string
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Sets the selected severity.
     *
     * @param severity alert severity
     */
    public void setSeverity(String severity) {
        this.severity = clean(severity);
    }

    /**
     * Returns the selected email notification status.
     *
     * Supported values may include:
     *
     * PENDING EMAIL_SENT EMAIL_FAILED SENT SIMULATED_SENT
     *
     * @return notification status or an empty string
     */
    public String getNotificationStatus() {
        return notificationStatus;
    }

    /**
     * Sets the email notification status.
     *
     * @param notificationStatus notification status
     */
    public void setNotificationStatus(
            String notificationStatus) {

        this.notificationStatus
                = clean(notificationStatus);
    }

    /**
     * Returns the selected acknowledgement state.
     *
     * Supported values are:
     *
     * ACKNOWLEDGED PENDING
     *
     * @return acknowledgement state or an empty string
     */
    public String getAcknowledgementStatus() {
        return acknowledgementStatus;
    }

    /**
     * Sets the acknowledgement state.
     *
     * @param acknowledgementStatus acknowledgement state
     */
    public void setAcknowledgementStatus(
            String acknowledgementStatus) {

        this.acknowledgementStatus
                = clean(acknowledgementStatus);
    }

    /**
     * Returns the patient-code search value.
     *
     * @return patient code or an empty string
     */
    public String getPatientCode() {
        return patientCode;
    }

    /**
     * Sets the patient-code search value.
     *
     * @param patientCode patient code
     */
    public void setPatientCode(String patientCode) {
        this.patientCode = clean(patientCode);
    }

    /**
     * Returns the selected laboratory-test name.
     *
     * @return test name or an empty string
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Sets the laboratory-test name.
     *
     * @param testName test name
     */
    public void setTestName(String testName) {
        this.testName = clean(testName);
    }

    /**
     * Checks whether a start date was selected.
     *
     * @return true when a start date exists
     */
    public boolean hasStartDate() {
        return startDate != null;
    }

    /**
     * Checks whether an end date was selected.
     *
     * @return true when an end date exists
     */
    public boolean hasEndDate() {
        return endDate != null;
    }

    /**
     * Checks whether a severity filter is active.
     *
     * @return true when a severity value exists
     */
    public boolean hasSeverity() {
        return hasText(severity);
    }

    /**
     * Checks whether an email-notification filter is active.
     *
     * @return true when a notification status exists
     */
    public boolean hasNotificationStatus() {
        return hasText(notificationStatus);
    }

    /**
     * Checks whether an acknowledgement filter is active.
     *
     * @return true when an acknowledgement state exists
     */
    public boolean hasAcknowledgementStatus() {
        return hasText(acknowledgementStatus);
    }

    /**
     * Checks whether a patient-code filter is active.
     *
     * @return true when a patient code exists
     */
    public boolean hasPatientCode() {
        return hasText(patientCode);
    }

    /**
     * Checks whether a test-name filter is active.
     *
     * @return true when a test name exists
     */
    public boolean hasTestName() {
        return hasText(testName);
    }

    /**
     * Checks whether the selected date range is invalid.
     *
     * The range is invalid only when both dates are present and the start date
     * is later than the end date.
     *
     * @return true when the date range is invalid
     */
    public boolean hasInvalidDateRange() {

        return startDate != null
                && endDate != null
                && startDate.isAfter(endDate);
    }

    /**
     * Checks whether at least one report filter is active.
     *
     * @return true when one or more filters are active
     */
    public boolean hasAnyFilter() {

        return hasStartDate()
                || hasEndDate()
                || hasSeverity()
                || hasNotificationStatus()
                || hasAcknowledgementStatus()
                || hasPatientCode()
                || hasTestName();
    }

    /**
     * Checks whether the report should contain only acknowledged alerts.
     *
     * @return true when acknowledgement status is ACKNOWLEDGED
     */
    public boolean isAcknowledgedOnly() {

        return "ACKNOWLEDGED".equalsIgnoreCase(
                acknowledgementStatus
        );
    }

    /**
     * Checks whether the report should contain only pending alerts.
     *
     * PENDING means that the alert has not yet been acknowledged.
     *
     * @return true when acknowledgement status is PENDING
     */
    public boolean isPendingOnly() {

        return "PENDING".equalsIgnoreCase(
                acknowledgementStatus
        );
    }

    /**
     * Returns a readable start-date value for exported reports.
     *
     * @return selected start date or "All dates"
     */
    public String getStartDateDisplayValue() {

        return startDate == null
                ? "All dates"
                : startDate.toString();
    }

    /**
     * Returns a readable end-date value for exported reports.
     *
     * @return selected end date or "All dates"
     */
    public String getEndDateDisplayValue() {

        return endDate == null
                ? "All dates"
                : endDate.toString();
    }

    /**
     * Returns a readable severity value.
     *
     * @return selected severity or "All"
     */
    public String getSeverityDisplayValue() {

        return displayValue(severity);
    }

    /**
     * Returns a readable notification-status value.
     *
     * @return selected notification status or "All"
     */
    public String getNotificationStatusDisplayValue() {

        return displayValue(notificationStatus);
    }

    /**
     * Returns a readable acknowledgement-status value.
     *
     * @return selected acknowledgement status or "All"
     */
    public String getAcknowledgementStatusDisplayValue() {

        return displayValue(acknowledgementStatus);
    }

    /**
     * Returns a readable patient-code value.
     *
     * @return selected patient code or "All"
     */
    public String getPatientCodeDisplayValue() {

        return displayValue(patientCode);
    }

    /**
     * Returns a readable test-name value.
     *
     * @return selected test name or "All"
     */
    public String getTestNameDisplayValue() {

        return displayValue(testName);
    }

    /**
     * Clears every report filter.
     */
    public void clear() {

        this.startDate = null;
        this.endDate = null;

        this.severity = "";
        this.notificationStatus = "";
        this.acknowledgementStatus = "";
        this.patientCode = "";
        this.testName = "";
    }

    /**
     * Trims a text value and converts null to an empty string.
     *
     * @param value source value
     * @return cleaned value
     */
    private String clean(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Checks whether a value contains non-whitespace text.
     *
     * @param value source value
     * @return true when the value contains text
     */
    private boolean hasText(String value) {

        return value != null
                && !value.isBlank();
    }

    /**
     * Converts an empty filter value to a readable report label.
     *
     * @param value filter value
     * @return filter value or "All"
     */
    private String displayValue(String value) {

        return hasText(value)
                ? value.trim()
                : "All";
    }

    /**
     * Returns a readable representation for logging and debugging.
     *
     * @return filter description
     */
    @Override
    public String toString() {

        return "AlertReportFilter{"
                + "startDate=" + startDate
                + ", endDate=" + endDate
                + ", severity='" + severity + '\''
                + ", notificationStatus='"
                + notificationStatus + '\''
                + ", acknowledgementStatus='"
                + acknowledgementStatus + '\''
                + ", patientCode='" + patientCode + '\''
                + ", testName='" + testName + '\''
                + '}';
    }
}
