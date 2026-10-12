package za.co.lab.service;

import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.model.User;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Generates secure PDF exports for the alert reporting module.
 *
 * The service receives alert records that have already been authorised and
 * filtered by the controller and DAO layers.
 *
 * @author USER
 */
public class AlertPdfReportService {

    private static final DateTimeFormatter GENERATED_DATE_FORMAT
            = DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy HH:mm:ss",
                    Locale.ENGLISH
            );

    private static final DateTimeFormatter RECORD_DATE_FORMAT
            = DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.ENGLISH
            );

    private static final Color PRIMARY_COLOUR
            = new Color(91, 53, 245);

    private static final Color PRIMARY_DARK_COLOUR
            = new Color(49, 46, 129);

    private static final Color HEADER_BACKGROUND
            = new Color(91, 53, 245);

    private static final Color LIGHT_PURPLE_BACKGROUND
            = new Color(248, 247, 255);

    private static final Color LIGHT_GREY_BACKGROUND
            = new Color(248, 250, 252);

    private static final Color BORDER_COLOUR
            = new Color(213, 217, 226);

    private static final Color CRITICAL_COLOUR
            = new Color(201, 61, 50);

    private static final Color WARNING_COLOUR
            = new Color(217, 119, 6);

    private static final Color SUCCESS_COLOUR
            = new Color(46, 157, 66);

    private static final Color FAILURE_COLOUR
            = new Color(210, 60, 76);

    private static final Color MUTED_TEXT_COLOUR
            = new Color(75, 85, 99);

    private static final float DOCUMENT_MARGIN = 28.0f;

    /**
     * Generates the alert monitoring PDF report.
     *
     * @param outputStream response output stream
     * @param alerts authorised alert records
     * @param filter applied filters
     * @param generatedBy authenticated user
     * @param reportScope authorised report scope
     * @throws IOException when the PDF cannot be generated
     */
    public void writeReport(
            OutputStream outputStream,
            List<Alert> alerts,
            AlertReportFilter filter,
            User generatedBy,
            String reportScope)
            throws IOException {

        if (outputStream == null) {
            throw new IllegalArgumentException(
                    "The PDF output stream is required."
            );
        }

        List<Alert> safeAlerts;

        if (alerts == null) {
            safeAlerts = Collections.emptyList();
        } else {
            safeAlerts = alerts;
        }

        AlertReportFilter safeFilter;

        if (filter == null) {
            safeFilter = new AlertReportFilter();
        } else {
            safeFilter = filter;
        }

        String safeScope;

        if (hasText(reportScope)) {
            safeScope = reportScope.trim();
        } else {
            safeScope = "Authorised Alerts";
        }

        Document document
                = new Document(
                        PageSize.A4.rotate(),
                        DOCUMENT_MARGIN,
                        DOCUMENT_MARGIN,
                        38.0f,
                        38.0f
                );

        try {
            PdfWriter writer
                    = PdfWriter.getInstance(
                            document,
                            outputStream
                    );

            writer.setPageEvent(
                    new ReportPageEvent()
            );

            document.addTitle(
                    "Lab Alert System - Alert Monitoring Report"
            );

            document.addSubject(
                    "Authorised laboratory alert monitoring report"
            );

            document.addCreator(
                    "Lab Alert System"
            );

            document.addAuthor(
                    resolveUserName(generatedBy)
            );

            document.open();

            addReportHeading(document);

            addReportInformation(
                    document,
                    generatedBy,
                    safeScope,
                    safeAlerts.size()
            );

            addAppliedFilters(
                    document,
                    safeFilter
            );

            addSummary(
                    document,
                    safeAlerts
            );

            addAlertTable(
                    document,
                    safeAlerts
            );

            addConfidentialityNotice(document);

        } catch (DocumentException exception) {
            throw new IOException(
                    "Unable to generate the PDF alert report.",
                    exception
            );

        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void addReportHeading(Document document)
            throws DocumentException {

        Font systemTitleFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10,
                        PRIMARY_COLOUR
                );

        Font reportTitleFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        20,
                        Color.BLACK
                );

        Font subtitleFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        9,
                        MUTED_TEXT_COLOUR
                );

        Paragraph systemTitle
                = new Paragraph(
                        "LAB ALERT SYSTEM",
                        systemTitleFont
                );

        systemTitle.setSpacingAfter(3.0f);

        Paragraph reportTitle
                = new Paragraph(
                        "Alert Monitoring Report",
                        reportTitleFont
                );

        reportTitle.setSpacingAfter(4.0f);

        Paragraph subtitle
                = new Paragraph(
                        "AI-Based Laboratory Test Result "
                        + "Flagging and Alert System",
                        subtitleFont
                );

        subtitle.setSpacingAfter(15.0f);

        document.add(systemTitle);
        document.add(reportTitle);
        document.add(subtitle);
    }

    private void addReportInformation(
            Document document,
            User generatedBy,
            String reportScope,
            int recordCount)
            throws DocumentException {

        Font labelFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        8,
                        PRIMARY_DARK_COLOUR
                );

        Font valueFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8,
                        Color.BLACK
                );

        PdfPTable table
                = new PdfPTable(4);

        table.setWidthPercentage(100.0f);

        table.setWidths(
                new float[]{
                    1.0f,
                    1.2f,
                    1.1f,
                    0.8f
                }
        );

        addInformationCell(
                table,
                "Generated On",
                LocalDateTime.now()
                        .format(GENERATED_DATE_FORMAT),
                labelFont,
                valueFont
        );

        addInformationCell(
                table,
                "Generated By",
                resolveUserName(generatedBy),
                labelFont,
                valueFont
        );

        addInformationCell(
                table,
                "Report Scope",
                reportScope,
                labelFont,
                valueFont
        );

        addInformationCell(
                table,
                "Matching Records",
                String.valueOf(recordCount),
                labelFont,
                valueFont
        );

        table.setSpacingAfter(12.0f);

        document.add(table);
    }

    private void addInformationCell(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont) {

        Paragraph content
                = new Paragraph();

        content.add(
                new Phrase(
                        label + "\n",
                        labelFont
                )
        );

        content.add(
                new Phrase(
                        safeText(value),
                        valueFont
                )
        );

        PdfPCell cell
                = new PdfPCell(content);

        cell.setBackgroundColor(
                LIGHT_PURPLE_BACKGROUND
        );

        cell.setBorderColor(BORDER_COLOUR);
        cell.setPadding(7.0f);

        table.addCell(cell);
    }

    private void addAppliedFilters(
            Document document,
            AlertReportFilter filter)
            throws DocumentException {

        Font headingFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10,
                        PRIMARY_DARK_COLOUR
                );

        Font valueFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8,
                        Color.BLACK
                );

        Paragraph heading
                = new Paragraph(
                        "Applied Filters",
                        headingFont
                );

        heading.setSpacingAfter(5.0f);

        document.add(heading);

        PdfPTable table
                = new PdfPTable(7);

        table.setWidthPercentage(100.0f);

        addFilterCell(
                table,
                "Start Date",
                filter.getStartDateDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "End Date",
                filter.getEndDateDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "Severity",
                filter.getSeverityDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "Email Status",
                filter.getNotificationStatusDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "Acknowledgement",
                filter.getAcknowledgementStatusDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "Patient Code",
                filter.getPatientCodeDisplayValue(),
                valueFont
        );

        addFilterCell(
                table,
                "Test Name",
                filter.getTestNameDisplayValue(),
                valueFont
        );

        table.setSpacingAfter(12.0f);

        document.add(table);
    }

    private void addFilterCell(
            PdfPTable table,
            String label,
            String value,
            Font valueFont) {

        Font labelFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        7,
                        MUTED_TEXT_COLOUR
                );

        Paragraph content
                = new Paragraph();

        content.add(
                new Phrase(
                        label + "\n",
                        labelFont
                )
        );

        content.add(
                new Phrase(
                        safeText(value),
                        valueFont
                )
        );

        PdfPCell cell
                = new PdfPCell(content);

        cell.setPadding(5.5f);
        cell.setBorderColor(BORDER_COLOUR);
        cell.setBackgroundColor(
                LIGHT_GREY_BACKGROUND
        );

        table.addCell(cell);
    }

    private void addSummary(
            Document document,
            List<Alert> alerts)
            throws DocumentException {

        int criticalCount = 0;
        int warningCount = 0;
        int emailSentCount = 0;
        int emailFailedCount = 0;
        int acknowledgedCount = 0;
        int pendingCount = 0;

        for (Alert alert : alerts) {

            if (alert == null) {
                continue;
            }

            if ("CRITICAL".equalsIgnoreCase(
                    alert.getSeverity())) {

                criticalCount++;

            } else if ("WARNING".equalsIgnoreCase(
                    alert.getSeverity())) {

                warningCount++;
            }

            if ("EMAIL_SENT".equalsIgnoreCase(
                    alert.getNotificationStatus())) {

                emailSentCount++;

            } else if ("EMAIL_FAILED".equalsIgnoreCase(
                    alert.getNotificationStatus())) {

                emailFailedCount++;
            }

            if (alert.isAcknowledged()) {
                acknowledgedCount++;
            } else {
                pendingCount++;
            }
        }

        Font headingFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10,
                        PRIMARY_DARK_COLOUR
                );

        Paragraph heading
                = new Paragraph(
                        "Report Summary",
                        headingFont
                );

        heading.setSpacingAfter(5.0f);

        document.add(heading);

        PdfPTable summaryTable
                = new PdfPTable(7);

        summaryTable.setWidthPercentage(100.0f);

        addSummaryCell(
                summaryTable,
                "Total Alerts",
                alerts.size(),
                PRIMARY_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Critical",
                criticalCount,
                CRITICAL_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Warning",
                warningCount,
                WARNING_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Email Sent",
                emailSentCount,
                SUCCESS_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Email Failed",
                emailFailedCount,
                FAILURE_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Acknowledged",
                acknowledgedCount,
                SUCCESS_COLOUR
        );

        addSummaryCell(
                summaryTable,
                "Pending Review",
                pendingCount,
                WARNING_COLOUR
        );

        summaryTable.setSpacingAfter(14.0f);

        document.add(summaryTable);
    }

    private void addSummaryCell(
            PdfPTable table,
            String label,
            int value,
            Color valueColour) {

        Font valueFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        13,
                        valueColour
                );

        Font labelFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        7,
                        MUTED_TEXT_COLOUR
                );

        Paragraph content
                = new Paragraph();

        content.setAlignment(
                Element.ALIGN_CENTER
        );

        content.add(
                new Phrase(
                        String.valueOf(value) + "\n",
                        valueFont
                )
        );

        content.add(
                new Phrase(
                        label,
                        labelFont
                )
        );

        PdfPCell cell
                = new PdfPCell(content);

        cell.setPadding(7.0f);
        cell.setBorderColor(BORDER_COLOUR);
        cell.setBackgroundColor(Color.WHITE);

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        table.addCell(cell);
    }

    private void addAlertTable(
            Document document,
            List<Alert> alerts)
            throws DocumentException {

        Font headingFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10,
                        PRIMARY_DARK_COLOUR
                );

        Paragraph heading
                = new Paragraph(
                        "Alert Details",
                        headingFont
                );

        heading.setSpacingAfter(5.0f);

        document.add(heading);

        if (alerts.isEmpty()) {

            Font emptyFont
                    = FontFactory.getFont(
                            FontFactory.HELVETICA,
                            9,
                            MUTED_TEXT_COLOUR
                    );

            Paragraph emptyMessage
                    = new Paragraph(
                            "No alerts match the selected report filters.",
                            emptyFont
                    );

            emptyMessage.setSpacingAfter(15.0f);

            document.add(emptyMessage);
            return;
        }

        PdfPTable table
                = new PdfPTable(10);

        table.setWidthPercentage(100.0f);
        table.setHeaderRows(1);
        table.setSplitRows(true);
        table.setSplitLate(false);

        table.setWidths(
                new float[]{
                    1.35f,
                    1.35f,
                    0.95f,
                    0.70f,
                    0.90f,
                    2.20f,
                    1.05f,
                    1.15f,
                    1.15f,
                    1.25f
                }
        );

        addHeaderCell(table, "Date");
        addHeaderCell(table, "Patient");
        addHeaderCell(table, "Test");
        addHeaderCell(table, "Result");
        addHeaderCell(table, "Severity");
        addHeaderCell(table, "Alert Message");
        addHeaderCell(table, "Email Status");
        addHeaderCell(table, "Acknowledgement");
        addHeaderCell(table, "Acknowledged By");
        addHeaderCell(table, "Acknowledged At");

        int rowIndex = 0;

        for (Alert alert : alerts) {

            if (alert == null) {
                continue;
            }

            Color rowBackground
                    = rowIndex % 2 == 0
                            ? Color.WHITE
                            : LIGHT_GREY_BACKGROUND;

            addBodyCell(
                    table,
                    formatTimestamp(
                            alert.getCreatedAt()
                    ),
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            addBodyCell(
                    table,
                    formatPatient(alert),
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            addBodyCell(
                    table,
                    alert.getTestName(),
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            addBodyCell(
                    table,
                    formatResultValue(
                            alert.getResultValue()
                    ),
                    rowBackground,
                    Element.ALIGN_RIGHT
            );

            addStatusCell(
                    table,
                    alert.getSeverity(),
                    rowBackground,
                    getSeverityColour(
                            alert.getSeverity()
                    )
            );

            addBodyCell(
                    table,
                    alert.getAlertMessage(),
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            addStatusCell(
                    table,
                    alert.getNotificationStatus(),
                    rowBackground,
                    getNotificationColour(
                            alert.getNotificationStatus()
                    )
            );

            addStatusCell(
                    table,
                    alert.isAcknowledged()
                    ? "ACKNOWLEDGED"
                    : "PENDING REVIEW",
                    rowBackground,
                    alert.isAcknowledged()
                    ? SUCCESS_COLOUR
                    : WARNING_COLOUR
            );

            addBodyCell(
                    table,
                    hasText(alert.getAcknowledgedBy())
                    ? alert.getAcknowledgedBy()
                    : "-",
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            addBodyCell(
                    table,
                    formatTimestamp(
                            alert.getAcknowledgedAt()
                    ),
                    rowBackground,
                    Element.ALIGN_LEFT
            );

            rowIndex++;
        }

        document.add(table);
    }

    private void addHeaderCell(
            PdfPTable table,
            String text) {

        Font font
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        7,
                        Color.WHITE
                );

        PdfPCell cell
                = new PdfPCell(
                        new Phrase(
                                text,
                                font
                        )
                );

        cell.setBackgroundColor(
                HEADER_BACKGROUND
        );

        cell.setBorderColor(
                HEADER_BACKGROUND
        );

        cell.setPadding(5.0f);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }

    private void addBodyCell(
            PdfPTable table,
            String text,
            Color background,
            int alignment) {

        Font font
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        6.7f,
                        Color.BLACK
                );

        PdfPCell cell
                = new PdfPCell(
                        new Phrase(
                                safeText(text),
                                font
                        )
                );

        cell.setBackgroundColor(background);
        cell.setBorderColor(BORDER_COLOUR);
        cell.setPadding(4.5f);
        cell.setHorizontalAlignment(alignment);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }

    private void addStatusCell(
            PdfPTable table,
            String text,
            Color background,
            Color textColour) {

        Font font
                = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        6.5f,
                        textColour
                );

        PdfPCell cell
                = new PdfPCell(
                        new Phrase(
                                safeText(text),
                                font
                        )
                );

        cell.setBackgroundColor(background);
        cell.setBorderColor(BORDER_COLOUR);
        cell.setPadding(4.5f);

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }

    private void addConfidentialityNotice(
            Document document)
            throws DocumentException {

        Font font
                = FontFactory.getFont(
                        FontFactory.HELVETICA_OBLIQUE,
                        7,
                        MUTED_TEXT_COLOUR
                );

        Paragraph notice
                = new Paragraph(
                        "Confidentiality notice: This report contains "
                        + "sensitive laboratory alert information and may "
                        + "only be accessed, stored and distributed by "
                        + "authorised personnel.",
                        font
                );

        notice.setSpacingBefore(12.0f);

        notice.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(notice);
    }

    private String resolveUserName(User user) {

        if (user == null) {
            return "Unknown User";
        }

        if (hasText(user.getFullName())) {
            return user.getFullName().trim();
        }

        if (hasText(user.getUsername())) {
            return user.getUsername().trim();
        }

        return "Unknown User";
    }

    private String formatPatient(Alert alert) {

        String patientCode
                = safeText(alert.getPatientCode());

        String patientName
                = safeText(alert.getPatientName());

        if ("-".equals(patientName)) {
            return patientCode;
        }

        return patientCode
                + "\n"
                + patientName;
    }

    private String formatResultValue(double value) {

        if (!Double.isFinite(value)) {
            return "-";
        }

        if (value == Math.rint(value)) {
            return String.format(
                    Locale.ROOT,
                    "%.0f",
                    value
            );
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        ).replaceAll(
                "0+$",
                ""
        ).replaceAll(
                "\\.$",
                ""
        );
    }

    private String formatTimestamp(
            Timestamp timestamp) {

        if (timestamp == null) {
            return "-";
        }

        return timestamp
                .toLocalDateTime()
                .format(RECORD_DATE_FORMAT);
    }

    private Color getSeverityColour(
            String severity) {

        if ("CRITICAL".equalsIgnoreCase(severity)) {
            return CRITICAL_COLOUR;
        }

        if ("WARNING".equalsIgnoreCase(severity)) {
            return WARNING_COLOUR;
        }

        return MUTED_TEXT_COLOUR;
    }

    private Color getNotificationColour(
            String notificationStatus) {

        if ("EMAIL_SENT".equalsIgnoreCase(
                notificationStatus)
                || "SENT".equalsIgnoreCase(
                        notificationStatus)
                || "SIMULATED_SENT".equalsIgnoreCase(
                        notificationStatus)) {

            return SUCCESS_COLOUR;
        }

        if ("EMAIL_FAILED".equalsIgnoreCase(
                notificationStatus)) {

            return FAILURE_COLOUR;
        }

        return WARNING_COLOUR;
    }

    private String safeText(String value) {

        return hasText(value)
                ? value.trim()
                : "-";
    }

    private boolean hasText(String value) {

        return value != null
                && !value.isBlank();
    }

    private static class ReportPageEvent
            extends PdfPageEventHelper {

        private final Font footerFont
                = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        7,
                        MUTED_TEXT_COLOUR
                );

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document) {

            Rectangle pageSize
                    = document.getPageSize();

            Phrase leftFooter
                    = new Phrase(
                            "Lab Alert System - Confidential",
                            footerFont
                    );

            Phrase rightFooter
                    = new Phrase(
                            "Page "
                            + writer.getPageNumber(),
                            footerFont
                    );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    leftFooter,
                    document.left(),
                    pageSize.getBottom() + 17.0f,
                    0.0f
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    rightFooter,
                    document.right(),
                    pageSize.getBottom() + 17.0f,
                    0.0f
            );
        }
    }
}
