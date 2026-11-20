package za.co.lab.service;

import za.co.lab.ai.RuleEngine;
import za.co.lab.dao.PathologistDAO;
import za.co.lab.model.Alert;
import za.co.lab.model.LabResult;
import za.co.lab.model.Pathologist;
import za.co.lab.util.EmailLinkUtil;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

/**
 * Sends formatted laboratory-alert emails to the Pathologist
 * assigned to the affected patient.
 *
 * @author USER
 */
public class EmailService {

    private static final Logger LOGGER =
            Logger.getLogger(
                    EmailService.class.getName()
            );

    private static final String SENDER_EMAIL =
            "labresultsalert@gmail.com";

    private static final String SENDER_NAME =
            "AI Lab Alert System";

    private static final String MAIL_SESSION_JNDI =
            "java:comp/env/mail/LabAlertMail";

    /*
     * The application base URL may be supplied using:
     *
     * -Dlabalert.baseUrl=http://localhost:8080/AILabAlertSystem
     *
     * or:
     *
     * LABALERT_BASE_URL
     *
     * The GlassFish JVM option takes precedence over this fallback.
     */
    private static final String BASE_URL_PROPERTY =
            "labalert.baseUrl";

    private static final String BASE_URL_ENVIRONMENT =
            "LABALERT_BASE_URL";

    private static final String DEFAULT_BASE_URL =
            "http://localhost:8080/AILabAlertSystem";

    private final PathologistDAO pathologistDAO =
            new PathologistDAO();

    /**
     * Sends the initial alert email to the Pathologist assigned to
     * the affected patient.
     *
     * @param result evaluated laboratory result
     * @param evaluation Rule Engine evaluation
     * @param alertId stored alert identifier
     * @return true when email delivery succeeds
     */
    public boolean sendAlertEmail(
            LabResult result,
            RuleEngine.EvaluationResult evaluation,
            int alertId) {

        if (result == null
                || evaluation == null
                || alertId <= 0) {

            logEmailFailure(
                    alertId,
                    "Email data was incomplete."
            );

            return false;
        }

        try {
            Pathologist pathologist =
                    pathologistDAO.findByPatientId(
                            result.getPatientId()
                    );

            if (pathologist == null
                    || isBlank(pathologist.getEmail())) {

                logEmailFailure(
                        alertId,
                        "No Pathologist email address is configured "
                        + "for patientId="
                        + result.getPatientId()
                );

                return false;
            }

            String recipientEmail =
                    pathologist.getEmail().trim();

            Session session =
                    lookupMailSession();

            MimeMessage message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            SENDER_EMAIL,
                            SENDER_NAME,
                            StandardCharsets.UTF_8.name()
                    )
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            recipientEmail,
                            false
                    )
            );

            message.setSubject(
                    buildSubject(
                            result,
                            evaluation
                    ),
                    StandardCharsets.UTF_8.name()
            );

            message.setSentDate(
                    new Date()
            );

            message.setContent(
                    buildHtmlEmail(
                            result,
                            evaluation,
                            alertId
                    ),
                    "text/html; charset=UTF-8"
            );

            if (isCritical(evaluation)) {

                applyHighPriorityHeaders(
                        message
                );
            }

            Transport.send(
                    message
            );

            logEmailSuccess(
                    alertId
            );

            return true;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Email delivery failed for alert ID "
                    + alertId,
                    exception
            );

            logEmailFailure(
                    alertId,
                    "Alert email delivery failed: "
                    + exception.getClass().getSimpleName()
            );

            return false;
        }
    }

    /**
     * Sends an overdue reminder to the Pathologist assigned to the
     * alert's patient.
     *
     * A fresh signed review link is generated for every reminder.
     * Reminder database tracking is performed separately by
     * AlertDAO after this method returns true.
     *
     * @param alert overdue, unacknowledged alert
     * @return true only when delivery succeeds
     */
    public boolean sendOverdueReminder(
            Alert alert) {

        if (!isValidOverdueAlert(alert)) {

            LOGGER.log(
                    Level.WARNING,
                    "Reminder email rejected because alert {0} "
                    + "is invalid, acknowledged, or not overdue.",
                    alert == null
                            ? null
                            : alert.getId()
            );

            return false;
        }

        try {
            Pathologist pathologist =
                    pathologistDAO.findByPatientId(
                            alert.getPatientId()
                    );

            if (pathologist == null
                    || isBlank(pathologist.getEmail())) {

                LOGGER.log(
                        Level.WARNING,
                        "No Pathologist email address is configured "
                        + "for overdue alert ID {0}.",
                        alert.getId()
                );

                logReminderFailure(
                        alert.getId(),
                        "No assigned Pathologist email address "
                        + "was available."
                );

                return false;
            }

            String recipientEmail =
                    pathologist.getEmail().trim();

            long expiry =
                    EmailLinkUtil.generateExpiry();

            String signature =
                    EmailLinkUtil.sign(
                            alert.getId(),
                            "view",
                            expiry
                    );

            String reviewUrl =
                    buildReviewUrl(
                            alert.getId(),
                            expiry,
                            signature
                    );

            Session session =
                    lookupMailSession();

            MimeMessage message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            SENDER_EMAIL,
                            SENDER_NAME,
                            StandardCharsets.UTF_8.name()
                    )
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            recipientEmail,
                            false
                    )
            );

            message.setSubject(
                    buildReminderSubject(alert),
                    StandardCharsets.UTF_8.name()
            );

            message.setSentDate(
                    new Date()
            );

            message.setContent(
                    buildOverdueReminderHtml(
                            alert,
                            reviewUrl
                    ),
                    "text/html; charset=UTF-8"
            );

            applyHighPriorityHeaders(
                    message
            );

            Transport.send(
                    message
            );

            LOGGER.log(
                    Level.INFO,
                    "Overdue reminder email sent for alert ID {0}.",
                    alert.getId()
            );

            logReminderSuccess(
                    alert.getId()
            );

            return true;

        } catch (Exception exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to send overdue reminder for alert ID "
                    + alert.getId(),
                    exception
            );

            logReminderFailure(
                    alert.getId(),
                    "Reminder delivery failed: "
                    + exception.getClass().getSimpleName()
            );

            return false;
        }
    }

    /**
     * Looks up the Jakarta Mail session configured in GlassFish.
     *
     * @return configured mail session
     * @throws Exception when JNDI lookup fails
     */
    private Session lookupMailSession()
            throws Exception {

        InitialContext context =
                new InitialContext();

        return (Session) context.lookup(
                MAIL_SESSION_JNDI
        );
    }

    /**
     * Adds high-priority email headers.
     *
     * @param message email message
     * @throws Exception when a header cannot be added
     */
    private void applyHighPriorityHeaders(
            MimeMessage message)
            throws Exception {

        message.setHeader(
                "X-Priority",
                "1"
        );

        message.setHeader(
                "Importance",
                "high"
        );
    }

    /**
     * Validates an overdue reminder candidate.
     *
     * @param alert alert to validate
     * @return true when reminder delivery may be attempted
     */
    private boolean isValidOverdueAlert(
            Alert alert) {

        return alert != null
                && alert.getId() > 0
                && alert.getPatientId() > 0
                && !alert.isAcknowledged()
                && alert.getReviewDueAt() != null
                && alert.getReviewDueAt().getTime()
                < System.currentTimeMillis();
    }

    /**
     * Builds the initial alert subject.
     *
     * @param result laboratory result
     * @param evaluation Rule Engine evaluation
     * @return email subject
     */
    private String buildSubject(
            LabResult result,
            RuleEngine.EvaluationResult evaluation) {

        String severity =
                safeText(
                        evaluation.getSeverity()
                ).toUpperCase(Locale.ROOT);

        String testName =
                safeText(
                        result.getTestName()
                );

        return "[AI LAB ALERT] "
                + severity
                + " - "
                + testName;
    }

    /**
     * Builds the overdue reminder subject.
     *
     * @param alert overdue alert
     * @return reminder subject
     */
    private String buildReminderSubject(
            Alert alert) {

        String severity =
                safeText(
                        alert.getSeverity()
                ).toUpperCase(Locale.ROOT);

        String testName =
                safeText(
                        alert.getTestName()
                );

        return "[OVERDUE LAB ALERT REMINDER] "
                + severity
                + " - "
                + testName
                + " - Alert #"
                + alert.getId();
    }

    /**
     * Builds the initial formatted alert email.
     *
     * @param result laboratory result
     * @param evaluation Rule Engine evaluation
     * @param alertId alert identifier
     * @return HTML email
     */
    private String buildHtmlEmail(
            LabResult result,
            RuleEngine.EvaluationResult evaluation,
            int alertId) {

        long expiry =
                EmailLinkUtil.generateExpiry();

        String signature =
                EmailLinkUtil.sign(
                        alertId,
                        "view",
                        expiry
                );

        String reviewUrl =
                buildReviewUrl(
                        alertId,
                        expiry,
                        signature
                );

        String severity =
                safeText(
                        evaluation.getSeverity()
                ).toUpperCase(Locale.ROOT);

        String testName =
                safeText(
                        result.getTestName()
                );

        String unit =
                safeText(
                        result.getUnit()
                );

        String comment =
                safeText(
                        evaluation.getComment()
                );

        String displayedResult =
                formatNumber(
                        result.getResultValue()
                )
                + (unit.isBlank()
                ? ""
                : " " + unit);

        boolean critical =
                isCritical(evaluation);

        String severityColour =
                critical
                        ? "#dc3545"
                        : "#f0ad00";

        String severityTextColour =
                critical
                        ? "#ffffff"
                        : "#212529";

        String heading =
                critical
                        ? "Critical laboratory result"
                        : "Laboratory result requires review";

        String safeUrl =
                escapeHtml(reviewUrl);

        StringBuilder html =
                new StringBuilder(6000);

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append(
                "<meta name=\"viewport\" "
                + "content=\"width=device-width, initial-scale=1.0\">"
        );
        html.append("</head>");

        html.append(
                "<body style=\"margin:0;padding:0;"
                + "background-color:#f3f6fa;"
                + "font-family:Arial,Helvetica,sans-serif;"
                + "color:#1f2937;\">"
        );

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f3f6fa;"
                + "padding:24px 12px;\">"
        );

        html.append("<tr><td align=\"center\">");

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:620px;"
                + "background-color:#ffffff;"
                + "border:1px solid #dfe6ee;"
                + "border-radius:12px;"
                + "overflow:hidden;\">"
        );

        html.append("<tr>");
        html.append(
                "<td style=\"background-color:#2f3b78;"
                + "padding:24px 28px;\">"
        );

        html.append(
                "<div style=\"font-size:22px;font-weight:700;"
                + "color:#ffffff;\">"
        );
        html.append("AI Lab Alert System");
        html.append("</div>");

        html.append(
                "<div style=\"font-size:13px;color:#dce8f5;"
                + "margin-top:7px;\">"
        );
        html.append("Secure laboratory result notification");
        html.append("</div>");

        html.append("</td>");
        html.append("</tr>");

        html.append("<tr>");
        html.append("<td style=\"padding:28px;\">");

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\">"
        );

        html.append("<tr>");

        html.append(
                "<td style=\"font-size:21px;font-weight:700;"
                + "color:#111827;padding-bottom:14px;\">"
        );
        html.append(
                escapeHtml(heading)
        );
        html.append("</td>");

        html.append(
                "<td align=\"right\" valign=\"top\" "
                + "style=\"padding-bottom:14px;\">"
        );

        html.append(
                "<span style=\"display:inline-block;"
                + "background-color:"
        );
        html.append(severityColour);
        html.append(";color:");
        html.append(severityTextColour);
        html.append(
                ";font-size:12px;font-weight:700;"
                + "letter-spacing:0.5px;"
                + "padding:8px 13px;"
                + "border-radius:999px;\">"
        );
        html.append(
                escapeHtml(severity)
        );
        html.append("</span>");

        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append(
                "<p style=\"margin:4px 0 22px;"
                + "font-size:14px;line-height:1.6;"
                + "color:#4b5563;\">"
        );
        html.append(
                "A laboratory result has been flagged by the "
                + "AI-assisted Rule Engine and requires authorised "
                + "review."
        );
        html.append("</p>");

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"border:1px solid #e5e7eb;"
                + "border-radius:8px;overflow:hidden;\">"
        );

        html.append(
                buildDetailRow(
                        "Alert reference",
                        String.valueOf(alertId),
                        "#f8fafc"
                )
        );

        html.append(
                buildDetailRow(
                        "Laboratory test",
                        testName,
                        "#ffffff"
                )
        );

        html.append(
                buildDetailRow(
                        "Result",
                        displayedResult,
                        "#f8fafc"
                )
        );

        html.append(
                buildDetailRow(
                        "Severity",
                        severity,
                        "#ffffff"
                )
        );

        html.append("</table>");

        html.append(
                "<div style=\"margin-top:20px;padding:16px;"
                + "background-color:#fff7ed;"
                + "border-left:4px solid "
        );
        html.append(severityColour);
        html.append(";border-radius:6px;\">");

        html.append(
                "<div style=\"font-size:12px;font-weight:700;"
                + "text-transform:uppercase;"
                + "letter-spacing:0.4px;"
                + "color:#7c2d12;margin-bottom:6px;\">"
        );
        html.append("Alert explanation");
        html.append("</div>");

        html.append(
                "<div style=\"font-size:14px;"
                + "line-height:1.55;color:#3f3f46;\">"
        );
        html.append(
                escapeHtml(comment)
        );
        html.append("</div>");

        html.append("</div>");

        html.append(
                "<table role=\"presentation\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"margin:26px 0 15px;\">"
        );

        html.append("<tr>");

        html.append(
                "<td align=\"center\" "
                + "style=\"background-color:#5b3df5;"
                + "border-radius:7px;\">"
        );

        html.append(
                "<a href=\""
                + safeUrl
                + "\" style=\"display:inline-block;"
                + "padding:13px 24px;"
                + "color:#ffffff;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "font-weight:700;"
                + "border-radius:7px;\">"
        );
        html.append("Review Alert");
        html.append("</a>");

        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append(
                "<p style=\"margin:0;font-size:12px;"
                + "line-height:1.5;color:#6b7280;\">"
        );
        html.append(
                "For security, this review link is signed and "
                + "time limited."
        );
        html.append("</p>");

        html.append(
                "<p style=\"margin:17px 0 0;"
                + "font-size:12px;line-height:1.55;"
                + "color:#6b7280;overflow-wrap:anywhere;\">"
        );

        html.append(
                "If the button does not work, copy and paste "
                + "this address into the browser:<br>"
        );

        html.append(
                "<a href=\""
                + safeUrl
                + "\" style=\"color:#0d6efd;"
                + "text-decoration:underline;"
                + "word-break:break-all;\">"
        );
        html.append(safeUrl);
        html.append("</a>");

        html.append("</p>");

        html.append("</td>");
        html.append("</tr>");

        appendEmailFooter(
                html
        );

        html.append("</table>");
        html.append("</td></tr>");
        html.append("</table>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Builds the overdue reminder email.
     *
     * @param alert overdue alert
     * @param reviewUrl freshly signed review URL
     * @return reminder email HTML
     */
    private String buildOverdueReminderHtml(
            Alert alert,
            String reviewUrl) {

        String severity =
                safeText(
                        alert.getSeverity()
                ).toUpperCase(Locale.ROOT);

        String testName =
                safeText(
                        alert.getTestName()
                );

        String patientCode =
                safeText(
                        alert.getPatientCode()
                );

        String alertMessage =
                safeText(
                        alert.getAlertMessage()
                );

        String displayedResult =
                formatNumber(
                        alert.getResultValue()
                );

        String reviewDue =
                alert.getReviewDueAt() == null
                        ? "Not available"
                        : alert.getReviewDueAt().toString();

        String safeUrl =
                escapeHtml(reviewUrl);

        int reminderNumber =
                alert.getReminderCount() + 1;

        StringBuilder html =
                new StringBuilder(5000);

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append(
                "<meta name=\"viewport\" "
                + "content=\"width=device-width, initial-scale=1.0\">"
        );
        html.append("</head>");

        html.append(
                "<body style=\"margin:0;padding:0;"
                + "background-color:#f3f6fa;"
                + "font-family:Arial,Helvetica,sans-serif;"
                + "color:#1f2937;\">"
        );

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f3f6fa;"
                + "padding:24px 12px;\">"
        );

        html.append("<tr><td align=\"center\">");

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:620px;"
                + "background-color:#ffffff;"
                + "border:1px solid #e5e7eb;"
                + "border-radius:12px;"
                + "overflow:hidden;\">"
        );

        html.append("<tr>");

        html.append(
                "<td style=\"background-color:#991b1b;"
                + "padding:24px 28px;color:#ffffff;\">"
        );

        html.append(
                "<div style=\"font-size:22px;font-weight:700;\">"
                + "Overdue Laboratory Alert"
                + "</div>"
        );

        html.append(
                "<div style=\"font-size:13px;"
                + "margin-top:7px;color:#fee2e2;\">"
                + "Reminder "
                + reminderNumber
                + " of 3"
                + "</div>"
        );

        html.append("</td>");
        html.append("</tr>");

        html.append("<tr>");
        html.append("<td style=\"padding:28px;\">");

        html.append(
                "<span style=\"display:inline-block;"
                + "background-color:#dc2626;"
                + "color:#ffffff;"
                + "font-size:12px;"
                + "font-weight:700;"
                + "padding:8px 13px;"
                + "border-radius:999px;\">"
                + "OVERDUE"
                + "</span>"
        );

        html.append(
                "<p style=\"font-size:15px;"
                + "line-height:1.6;color:#374151;\">"
                + "This laboratory alert remains unacknowledged "
                + "and has passed its configured review deadline."
                + "</p>"
        );

        html.append(
                "<table role=\"presentation\" width=\"100%\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"border:1px solid #e5e7eb;"
                + "border-radius:8px;overflow:hidden;\">"
        );

        html.append(
                buildDetailRow(
                        "Alert reference",
                        String.valueOf(alert.getId()),
                        "#f8fafc"
                )
        );

        html.append(
                buildDetailRow(
                        "Patient reference",
                        patientCode,
                        "#ffffff"
                )
        );

        html.append(
                buildDetailRow(
                        "Laboratory test",
                        testName,
                        "#f8fafc"
                )
        );

        html.append(
                buildDetailRow(
                        "Result",
                        displayedResult,
                        "#ffffff"
                )
        );

        html.append(
                buildDetailRow(
                        "Severity",
                        severity,
                        "#f8fafc"
                )
        );

        html.append(
                buildDetailRow(
                        "Review deadline",
                        reviewDue,
                        "#ffffff"
                )
        );

        html.append("</table>");

        html.append(
                "<div style=\"margin-top:20px;padding:16px;"
                + "background-color:#fef2f2;"
                + "border-left:4px solid #dc2626;"
                + "border-radius:6px;\">"
        );

        html.append(
                "<div style=\"font-size:12px;font-weight:700;"
                + "text-transform:uppercase;"
                + "letter-spacing:0.4px;"
                + "color:#991b1b;margin-bottom:6px;\">"
                + "Alert explanation"
                + "</div>"
        );

        html.append(
                "<div style=\"font-size:14px;"
                + "line-height:1.55;color:#3f3f46;\">"
                + escapeHtml(alertMessage)
                + "</div>"
        );

        html.append("</div>");

        html.append(
                "<table role=\"presentation\" "
                + "cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"margin:26px 0 15px;\">"
        );

        html.append("<tr>");

        html.append(
                "<td align=\"center\" "
                + "style=\"background-color:#dc2626;"
                + "border-radius:7px;\">"
        );

        html.append(
                "<a href=\""
                + safeUrl
                + "\" style=\"display:inline-block;"
                + "padding:13px 24px;"
                + "color:#ffffff;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "font-weight:700;"
                + "border-radius:7px;\">"
                + "Review Overdue Alert"
                + "</a>"
        );

        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append(
                "<p style=\"margin:0;font-size:12px;"
                + "line-height:1.5;color:#6b7280;\">"
                + "For security, this link is signed and time limited."
                + "</p>"
        );

        html.append(
                "<p style=\"margin:17px 0 0;"
                + "font-size:12px;line-height:1.55;"
                + "color:#6b7280;overflow-wrap:anywhere;\">"
                + "If the button does not work, copy and paste "
                + "this address into the browser:<br>"
                + "<a href=\""
                + safeUrl
                + "\" style=\"color:#0d6efd;"
                + "text-decoration:underline;"
                + "word-break:break-all;\">"
                + safeUrl
                + "</a>"
                + "</p>"
        );

        html.append("</td>");
        html.append("</tr>");

        appendEmailFooter(
                html
        );

        html.append("</table>");
        html.append("</td></tr>");
        html.append("</table>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Appends the standard confidentiality footer.
     *
     * @param html email builder
     */
    private void appendEmailFooter(
            StringBuilder html) {

        html.append("<tr>");

        html.append(
                "<td style=\"background-color:#f8fafc;"
                + "border-top:1px solid #e5e7eb;"
                + "padding:20px 28px;\">"
        );

        html.append(
                "<p style=\"margin:0 0 8px;"
                + "font-size:12px;line-height:1.5;"
                + "color:#6b7280;\">"
        );

        html.append(
                "<strong>Confidentiality notice:</strong> "
                + "This notification is intended for an authorised "
                + "recipient. Do not forward or disclose it."
        );

        html.append("</p>");

        html.append(
                "<p style=\"margin:0;font-size:12px;"
                + "line-height:1.5;color:#6b7280;\">"
        );

        html.append(
                "This classification provides decision-support "
                + "information and does not replace professional "
                + "clinical judgement."
        );

        html.append("</p>");

        html.append("</td>");
        html.append("</tr>");
    }

    /**
     * Creates one label-and-value email row.
     *
     * @param label row label
     * @param value row value
     * @param backgroundColour row background
     * @return HTML row
     */
    private String buildDetailRow(
            String label,
            String value,
            String backgroundColour) {

        return "<tr>"
                + "<td width=\"38%\" "
                + "style=\"padding:13px 15px;"
                + "background-color:"
                + backgroundColour
                + ";border-bottom:1px solid #e5e7eb;"
                + "font-size:13px;font-weight:700;"
                + "color:#374151;\">"
                + escapeHtml(label)
                + "</td>"
                + "<td style=\"padding:13px 15px;"
                + "background-color:"
                + backgroundColour
                + ";border-bottom:1px solid #e5e7eb;"
                + "font-size:14px;color:#111827;\">"
                + escapeHtml(value)
                + "</td>"
                + "</tr>";
    }

    /**
     * Builds a signed alert-review URL.
     *
     * @param alertId alert identifier
     * @param expiry link expiry
     * @param signature link signature
     * @return signed review URL
     */
    private String buildReviewUrl(
            int alertId,
            long expiry,
            String signature) {

        return getBaseUrl()
                + "/email-view"
                + "?alertId="
                + alertId
                + "&exp="
                + expiry
                + "&sig="
                + URLEncoder.encode(
                        safeText(signature),
                        StandardCharsets.UTF_8
                );
    }

    /**
     * Returns the configured application base URL.
     *
     * @return base URL without a trailing slash
     */
    private String getBaseUrl() {

        String baseUrl =
                System.getProperty(
                        BASE_URL_PROPERTY
                );

        if (isBlank(baseUrl)) {

            baseUrl =
                    System.getenv(
                            BASE_URL_ENVIRONMENT
                    );
        }

        if (isBlank(baseUrl)) {

            baseUrl =
                    DEFAULT_BASE_URL;
        }

        baseUrl =
                baseUrl.trim();

        while (baseUrl.endsWith("/")) {

            baseUrl =
                    baseUrl.substring(
                            0,
                            baseUrl.length() - 1
                    );
        }

        return baseUrl;
    }

    /**
     * Determines whether an evaluation is critical.
     *
     * @param evaluation Rule Engine evaluation
     * @return true when critical
     */
    private boolean isCritical(
            RuleEngine.EvaluationResult evaluation) {

        return evaluation != null
                && "CRITICAL".equalsIgnoreCase(
                        safeText(
                                evaluation.getSeverity()
                        )
                );
    }

    /**
     * Formats a result value to two decimal places.
     *
     * @param value numeric value
     * @return formatted value
     */
    private String formatNumber(
            double value) {

        if (!Double.isFinite(value)) {

            return "Not available";
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    /**
     * Escapes dynamic values before insertion into HTML.
     *
     * @param value source value
     * @return escaped HTML
     */
    private String escapeHtml(
            String value) {

        if (value == null) {

            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Converts null text to an empty trimmed value.
     *
     * @param value source value
     * @return safe text
     */
    private String safeText(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Checks whether text is missing.
     *
     * @param value source value
     * @return true when missing
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }

    /**
     * Records successful initial email delivery.
     *
     * @param alertId alert identifier
     */
    private void logEmailSuccess(
            int alertId) {

        try {
            AuditService.log(
                    null,
                    "EMAIL_SENT",
                    "ALERT",
                    alertId,
                    "Alert email sent successfully."
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record EMAIL_SENT for alert ID "
                    + alertId,
                    exception
            );
        }
    }

    /**
     * Records failed initial email delivery.
     *
     * @param alertId alert identifier
     * @param details failure details
     */
    private void logEmailFailure(
            int alertId,
            String details) {

        try {
            AuditService.log(
                    null,
                    "EMAIL_FAILED",
                    "ALERT",
                    alertId > 0
                            ? alertId
                            : null,
                    safeAuditDetails(details)
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record EMAIL_FAILED.",
                    exception
            );
        }
    }

    /**
     * Records successful reminder delivery.
     *
     * @param alertId alert identifier
     */
    private void logReminderSuccess(
            int alertId) {

        try {
            AuditService.log(
                    null,
                    "ALERT_REMINDER_EMAIL_SENT",
                    "ALERT",
                    alertId,
                    "Overdue alert reminder email sent successfully."
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record reminder email success for "
                    + "alert ID "
                    + alertId,
                    exception
            );
        }
    }

    /**
     * Records failed reminder delivery.
     *
     * @param alertId alert identifier
     * @param details failure details
     */
    private void logReminderFailure(
            int alertId,
            String details) {

        try {
            AuditService.log(
                    null,
                    "ALERT_REMINDER_EMAIL_FAILED",
                    "ALERT",
                    alertId > 0
                            ? alertId
                            : null,
                    safeAuditDetails(details)
            );

        } catch (Exception exception) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to record reminder email failure.",
                    exception
            );
        }
    }

    /**
     * Limits audit text and prevents blank failure messages.
     *
     * @param details audit details
     * @return safe audit details
     */
    private String safeAuditDetails(
            String details) {

        if (isBlank(details)) {

            return "Alert email delivery failed.";
        }

        String safeDetails =
                details.trim();

        if (safeDetails.length() > 250) {

            safeDetails =
                    safeDetails.substring(
                            0,
                            250
                    );
        }

        return safeDetails;
    }
}