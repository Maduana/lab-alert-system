package za.co.lab.service;

import za.co.lab.model.Alert;
import za.co.lab.model.AlertReportFilter;
import za.co.lab.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates secure Excel exports for the alert reporting module.
 *
 * The service receives alert records that have already been authorised and
 * filtered by the controller and DAO layers.
 *
 * The workbook contains:
 *
 * 1. Alerts 2. Summary 3. Report Information
 *
 * @author USER
 */
public class AlertExcelReportService {

    private static final DateTimeFormatter GENERATED_DATE_FORMAT
            = DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy HH:mm:ss",
                    Locale.ENGLISH
            );

    private static final String ALERTS_SHEET_NAME
            = "Alerts";

    private static final String SUMMARY_SHEET_NAME
            = "Summary";

    private static final String INFORMATION_SHEET_NAME
            = "Report Information";

    private static final int ALERT_COLUMN_COUNT = 11;

    /**
     * Generates the alert monitoring Excel workbook.
     *
     * @param outputStream response output stream
     * @param alerts authorised alert records
     * @param filter applied report filters
     * @param generatedBy authenticated user
     * @param reportScope authorised report scope
     * @throws IOException when the workbook cannot be generated
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
                    "The Excel output stream is required."
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

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            WorkbookStyles styles
                    = createWorkbookStyles(workbook);

            createAlertsSheet(
                    workbook,
                    safeAlerts,
                    styles
            );

            createSummarySheet(
                    workbook,
                    safeAlerts,
                    styles
            );

            createReportInformationSheet(
                    workbook,
                    safeAlerts,
                    safeFilter,
                    generatedBy,
                    safeScope,
                    styles
            );

            workbook.setActiveSheet(0);
            workbook.write(outputStream);
            outputStream.flush();
        }
    }

    /**
     * Creates the detailed alert-record worksheet.
     */
    private void createAlertsSheet(
            Workbook workbook,
            List<Alert> alerts,
            WorkbookStyles styles) {

        Sheet sheet
                = workbook.createSheet(
                        ALERTS_SHEET_NAME
                );

        createAlertsTitleRow(
                sheet,
                styles
        );

        createAlertsHeaderRow(
                sheet,
                styles
        );

        int rowIndex = 2;

        for (Alert alert : alerts) {

            if (alert == null) {
                continue;
            }

            Row row
                    = sheet.createRow(rowIndex);

            row.setHeightInPoints(34.0f);

            boolean alternateRow
                    = rowIndex % 2 != 0;

            CellStyle textStyle
                    = alternateRow
                            ? styles.alternateTextStyle
                            : styles.bodyTextStyle;

            CellStyle numericStyle
                    = alternateRow
                            ? styles.alternateNumericStyle
                            : styles.bodyNumericStyle;

            CellStyle dateStyle
                    = alternateRow
                            ? styles.alternateDateStyle
                            : styles.bodyDateStyle;

            createDateCell(
                    row,
                    0,
                    alert.getCreatedAt(),
                    dateStyle
            );

            createStringCell(
                    row,
                    1,
                    alert.getPatientCode(),
                    textStyle
            );

            createStringCell(
                    row,
                    2,
                    alert.getPatientName(),
                    textStyle
            );

            createStringCell(
                    row,
                    3,
                    alert.getTestName(),
                    textStyle
            );

            createNumericCell(
                    row,
                    4,
                    alert.getResultValue(),
                    numericStyle
            );

            createStringCell(
                    row,
                    5,
                    alert.getSeverity(),
                    severityStyle(
                            alert.getSeverity(),
                            styles
                    )
            );

            createStringCell(
                    row,
                    6,
                    alert.getAlertMessage(),
                    textStyle
            );

            createStringCell(
                    row,
                    7,
                    alert.getNotificationStatus(),
                    notificationStyle(
                            alert.getNotificationStatus(),
                            styles
                    )
            );

            createStringCell(
                    row,
                    8,
                    alert.isAcknowledged()
                    ? "ACKNOWLEDGED"
                    : "PENDING REVIEW",
                    alert.isAcknowledged()
                    ? styles.successStatusStyle
                    : styles.warningStatusStyle
            );

            createStringCell(
                    row,
                    9,
                    hasText(alert.getAcknowledgedBy())
                    ? alert.getAcknowledgedBy()
                    : "-",
                    textStyle
            );

            createDateCell(
                    row,
                    10,
                    alert.getAcknowledgedAt(),
                    dateStyle
            );

            rowIndex++;
        }

        if (rowIndex == 2) {

            Row emptyRow
                    = sheet.createRow(2);

            Cell emptyCell
                    = emptyRow.createCell(0);

            emptyCell.setCellValue(
                    "No alerts match the selected report filters."
            );

            emptyCell.setCellStyle(
                    styles.emptyMessageStyle
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            2,
                            2,
                            0,
                            ALERT_COLUMN_COUNT - 1
                    )
            );

        } else {

            sheet.setAutoFilter(
                    new CellRangeAddress(
                            1,
                            rowIndex - 1,
                            0,
                            ALERT_COLUMN_COUNT - 1
                    )
            );
        }

        sheet.createFreezePane(0, 2);
        sheet.setDisplayGridlines(false);

        configureAlertColumnWidths(sheet);
    }

    /**
     * Creates the title row on the Alerts worksheet.
     */
    private void createAlertsTitleRow(
            Sheet sheet,
            WorkbookStyles styles) {

        Row titleRow
                = sheet.createRow(0);

        titleRow.setHeightInPoints(28.0f);

        Cell titleCell
                = titleRow.createCell(0);

        titleCell.setCellValue(
                "Lab Alert System - Alert Monitoring Report"
        );

        titleCell.setCellStyle(
                styles.titleStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        ALERT_COLUMN_COUNT - 1
                )
        );
    }

    /**
     * Creates the Alerts worksheet heading row.
     */
    private void createAlertsHeaderRow(
            Sheet sheet,
            WorkbookStyles styles) {

        String[] headers = {
            "Date",
            "Patient Code",
            "Patient Name",
            "Test",
            "Result",
            "Severity",
            "Alert Message",
            "Email Status",
            "Acknowledgement",
            "Acknowledged By",
            "Acknowledged At"
        };

        Row headerRow
                = sheet.createRow(1);

        headerRow.setHeightInPoints(26.0f);

        for (int column = 0;
                column < headers.length;
                column++) {

            Cell cell
                    = headerRow.createCell(column);

            cell.setCellValue(
                    headers[column]
            );

            cell.setCellStyle(
                    styles.headerStyle
            );
        }
    }

    /**
     * Configures readable alert worksheet column widths.
     */
    private void configureAlertColumnWidths(
            Sheet sheet) {

        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(2, 24 * 256);
        sheet.setColumnWidth(3, 20 * 256);
        sheet.setColumnWidth(4, 12 * 256);
        sheet.setColumnWidth(5, 14 * 256);
        sheet.setColumnWidth(6, 46 * 256);
        sheet.setColumnWidth(7, 18 * 256);
        sheet.setColumnWidth(8, 20 * 256);
        sheet.setColumnWidth(9, 24 * 256);
        sheet.setColumnWidth(10, 20 * 256);
    }

    /**
     * Creates the report Summary worksheet.
     */
    private void createSummarySheet(
            Workbook workbook,
            List<Alert> alerts,
            WorkbookStyles styles) {

        Sheet sheet
                = workbook.createSheet(
                        SUMMARY_SHEET_NAME
                );

        sheet.setDisplayGridlines(false);

        Row titleRow
                = sheet.createRow(0);

        titleRow.setHeightInPoints(28.0f);

        Cell titleCell
                = titleRow.createCell(0);

        titleCell.setCellValue(
                "Alert Report Summary"
        );

        titleCell.setCellStyle(
                styles.titleStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        2
                )
        );

        Row headerRow
                = sheet.createRow(2);

        createStringCell(
                headerRow,
                0,
                "Metric",
                styles.headerStyle
        );

        createStringCell(
                headerRow,
                1,
                "Count",
                styles.headerStyle
        );

        createStringCell(
                headerRow,
                2,
                "Description",
                styles.headerStyle
        );

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

        int rowIndex = 3;

        createSummaryRow(
                sheet,
                rowIndex++,
                "Total Alerts",
                alerts.size(),
                "Total authorised alerts matching the report filters.",
                styles.primarySummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex++,
                "Critical Alerts",
                criticalCount,
                "Alerts classified as critical.",
                styles.criticalSummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex++,
                "Warning Alerts",
                warningCount,
                "Alerts classified as warnings.",
                styles.warningSummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex++,
                "Email Sent",
                emailSentCount,
                "Alerts for which email delivery succeeded.",
                styles.successSummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex++,
                "Email Failed",
                emailFailedCount,
                "Alerts for which email delivery failed.",
                styles.failureSummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex++,
                "Acknowledged",
                acknowledgedCount,
                "Alerts acknowledged by an assigned pathologist.",
                styles.successSummaryStyle,
                styles
        );

        createSummaryRow(
                sheet,
                rowIndex,
                "Pending Review",
                pendingCount,
                "Alerts awaiting pathologist review.",
                styles.warningSummaryStyle,
                styles
        );

        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 55 * 256);

        sheet.createFreezePane(0, 3);
    }

    /**
     * Creates one Summary worksheet row.
     */
    private void createSummaryRow(
            Sheet sheet,
            int rowIndex,
            String metric,
            int value,
            String description,
            CellStyle valueStyle,
            WorkbookStyles styles) {

        Row row
                = sheet.createRow(rowIndex);

        row.setHeightInPoints(24.0f);

        createStringCell(
                row,
                0,
                metric,
                styles.summaryLabelStyle
        );

        createNumericCell(
                row,
                1,
                value,
                valueStyle
        );

        createStringCell(
                row,
                2,
                description,
                styles.bodyTextStyle
        );
    }

    /**
     * Creates the Report Information worksheet.
     */
    private void createReportInformationSheet(
            Workbook workbook,
            List<Alert> alerts,
            AlertReportFilter filter,
            User generatedBy,
            String reportScope,
            WorkbookStyles styles) {

        Sheet sheet
                = workbook.createSheet(
                        INFORMATION_SHEET_NAME
                );

        sheet.setDisplayGridlines(false);

        Row titleRow
                = sheet.createRow(0);

        titleRow.setHeightInPoints(28.0f);

        Cell titleCell
                = titleRow.createCell(0);

        titleCell.setCellValue(
                "Alert Report Information"
        );

        titleCell.setCellStyle(
                styles.titleStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        2
                )
        );

        Row headerRow
                = sheet.createRow(2);

        createStringCell(
                headerRow,
                0,
                "Property",
                styles.headerStyle
        );

        createStringCell(
                headerRow,
                1,
                "Value",
                styles.headerStyle
        );

        createStringCell(
                headerRow,
                2,
                "Notes",
                styles.headerStyle
        );

        int rowIndex = 3;

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Generated On",
                LocalDateTime.now()
                        .format(GENERATED_DATE_FORMAT),
                "Date and time when the workbook was generated.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Generated By",
                resolveUserName(generatedBy),
                "Authenticated account that generated the report.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "User Role",
                generatedBy == null
                        ? "Unknown"
                        : safeText(
                                generatedBy.getRole()
                        ),
                "Role used to determine report access.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Report Scope",
                reportScope,
                "Authorised record scope applied by the server.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Record Count",
                String.valueOf(alerts.size()),
                "Number of alert records included.",
                styles
        );

        rowIndex++;

        Row filtersHeadingRow
                = sheet.createRow(rowIndex++);

        Cell filtersHeadingCell
                = filtersHeadingRow.createCell(0);

        filtersHeadingCell.setCellValue(
                "Applied Filters"
        );

        filtersHeadingCell.setCellStyle(
                styles.sectionHeadingStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        filtersHeadingRow.getRowNum(),
                        filtersHeadingRow.getRowNum(),
                        0,
                        2
                )
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Start Date",
                filter.getStartDateDisplayValue(),
                "Inclusive start date.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "End Date",
                filter.getEndDateDisplayValue(),
                "Inclusive end date.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Severity",
                filter.getSeverityDisplayValue(),
                "Selected severity filter.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Email Status",
                filter.getNotificationStatusDisplayValue(),
                "Selected email notification status.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Acknowledgement",
                filter.getAcknowledgementStatusDisplayValue(),
                "Selected acknowledgement state.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Patient Code",
                filter.getPatientCodeDisplayValue(),
                "Selected patient-code search value.",
                styles
        );

        rowIndex = createInformationRow(
                sheet,
                rowIndex,
                "Test Name",
                filter.getTestNameDisplayValue(),
                "Selected laboratory-test filter.",
                styles
        );

        rowIndex++;

        Row noticeRow
                = sheet.createRow(rowIndex);

        noticeRow.setHeightInPoints(48.0f);

        Cell noticeCell
                = noticeRow.createCell(0);

        noticeCell.setCellValue(
                "Confidentiality notice: This workbook contains "
                + "sensitive laboratory alert information. "
                + "The workbook may only be accessed, stored and "
                + "distributed by authorised personnel."
        );

        noticeCell.setCellStyle(
                styles.confidentialityStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        0,
                        2
                )
        );

        sheet.setColumnWidth(0, 25 * 256);
        sheet.setColumnWidth(1, 32 * 256);
        sheet.setColumnWidth(2, 62 * 256);

        sheet.createFreezePane(0, 3);
    }

    /**
     * Creates one Report Information row.
     *
     * @return next available row index
     */
    private int createInformationRow(
            Sheet sheet,
            int rowIndex,
            String property,
            String value,
            String notes,
            WorkbookStyles styles) {

        Row row
                = sheet.createRow(rowIndex);

        row.setHeightInPoints(23.0f);

        createStringCell(
                row,
                0,
                property,
                styles.informationLabelStyle
        );

        createStringCell(
                row,
                1,
                value,
                styles.bodyTextStyle
        );

        createStringCell(
                row,
                2,
                notes,
                styles.bodyTextStyle
        );

        return rowIndex + 1;
    }

    /**
     * Creates all reusable workbook styles.
     */
    private WorkbookStyles createWorkbookStyles(
            Workbook workbook) {

        WorkbookStyles styles
                = new WorkbookStyles();

        styles.titleStyle
                = createTitleStyle(workbook);

        styles.headerStyle
                = createHeaderStyle(workbook);

        styles.bodyTextStyle
                = createBodyStyle(
                        workbook,
                        HorizontalAlignment.LEFT,
                        false
                );

        styles.bodyNumericStyle
                = createNumericStyle(
                        workbook,
                        false
                );

        styles.bodyDateStyle
                = createDateStyle(
                        workbook,
                        false
                );

        styles.alternateTextStyle
                = createBodyStyle(
                        workbook,
                        HorizontalAlignment.LEFT,
                        true
                );

        styles.alternateNumericStyle
                = createNumericStyle(
                        workbook,
                        true
                );

        styles.alternateDateStyle
                = createDateStyle(
                        workbook,
                        true
                );

        styles.criticalStatusStyle
                = createStatusStyle(
                        workbook,
                        IndexedColors.RED,
                        IndexedColors.WHITE
                );

        styles.warningStatusStyle
                = createStatusStyle(
                        workbook,
                        IndexedColors.GOLD,
                        IndexedColors.BLACK
                );

        styles.successStatusStyle
                = createStatusStyle(
                        workbook,
                        IndexedColors.GREEN,
                        IndexedColors.WHITE
                );

        styles.failureStatusStyle
                = createStatusStyle(
                        workbook,
                        IndexedColors.DARK_RED,
                        IndexedColors.WHITE
                );

        styles.pendingStatusStyle
                = createStatusStyle(
                        workbook,
                        IndexedColors.GREY_40_PERCENT,
                        IndexedColors.WHITE
                );

        styles.primarySummaryStyle
                = createSummaryValueStyle(
                        workbook,
                        IndexedColors.VIOLET
                );

        styles.criticalSummaryStyle
                = createSummaryValueStyle(
                        workbook,
                        IndexedColors.RED
                );

        styles.warningSummaryStyle
                = createSummaryValueStyle(
                        workbook,
                        IndexedColors.ORANGE
                );

        styles.successSummaryStyle
                = createSummaryValueStyle(
                        workbook,
                        IndexedColors.GREEN
                );

        styles.failureSummaryStyle
                = createSummaryValueStyle(
                        workbook,
                        IndexedColors.DARK_RED
                );

        styles.summaryLabelStyle
                = createLabelStyle(workbook);

        styles.informationLabelStyle
                = createLabelStyle(workbook);

        styles.sectionHeadingStyle
                = createSectionHeadingStyle(workbook);

        styles.confidentialityStyle
                = createConfidentialityStyle(workbook);

        styles.emptyMessageStyle
                = createEmptyMessageStyle(workbook);

        return styles;
    }

    /**
     * Creates the workbook title style.
     */
    private CellStyle createTitleStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setFontHeightInPoints(
                (short) 16
        );

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.VIOLET.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        setThinBorders(style);

        return style;
    }

    /**
     * Creates the table heading style.
     */
    private CellStyle createHeaderStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.VIOLET.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        setThinBorders(style);

        return style;
    }

    /**
     * Creates a normal body-cell style.
     */
    private CellStyle createBodyStyle(
            Workbook workbook,
            HorizontalAlignment alignment,
            boolean alternateRow) {

        CellStyle style
                = workbook.createCellStyle();

        style.setAlignment(alignment);

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        if (alternateRow) {

            style.setFillForegroundColor(
                    IndexedColors.GREY_25_PERCENT.getIndex()
            );

            style.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
        }

        setThinBorders(style);

        return style;
    }

    /**
     * Creates a numeric-result style.
     */
    private CellStyle createNumericStyle(
            Workbook workbook,
            boolean alternateRow) {

        CellStyle style
                = createBodyStyle(
                        workbook,
                        HorizontalAlignment.RIGHT,
                        alternateRow
                );

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("0.00")
        );

        return style;
    }

    /**
     * Creates a timestamp style.
     */
    private CellStyle createDateStyle(
            Workbook workbook,
            boolean alternateRow) {

        CellStyle style
                = createBodyStyle(
                        workbook,
                        HorizontalAlignment.LEFT,
                        alternateRow
                );

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat(
                                "yyyy-mm-dd hh:mm:ss"
                        )
        );

        return style;
    }

    /**
     * Creates a coloured status-cell style.
     */
    private CellStyle createStatusStyle(
            Workbook workbook,
            IndexedColors background,
            IndexedColors foreground) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setColor(
                foreground.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                background.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        setThinBorders(style);

        return style;
    }

    /**
     * Creates a highlighted summary-number style.
     */
    private CellStyle createSummaryValueStyle(
            Workbook workbook,
            IndexedColors colour) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setFontHeightInPoints(
                (short) 13
        );

        font.setColor(
                colour.getIndex()
        );

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        setThinBorders(style);

        return style;
    }

    /**
     * Creates a label-cell style.
     */
    private CellStyle createLabelStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors.VIOLET.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        setThinBorders(style);

        return style;
    }

    /**
     * Creates an information-section heading style.
     */
    private CellStyle createSectionHeadingStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setBold(true);

        font.setFontHeightInPoints(
                (short) 12
        );

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.VIOLET.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        setThinBorders(style);

        return style;
    }

    /**
     * Creates the confidentiality-notice style.
     */
    private CellStyle createConfidentialityStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setItalic(true);

        font.setColor(
                IndexedColors.DARK_RED.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.LIGHT_YELLOW.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setWrapText(true);

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        setThinBorders(style);

        return style;
    }

    /**
     * Creates the empty-report message style.
     */
    private CellStyle createEmptyMessageStyle(
            Workbook workbook) {

        CellStyle style
                = workbook.createCellStyle();

        Font font
                = workbook.createFont();

        font.setItalic(true);

        font.setColor(
                IndexedColors.GREY_80_PERCENT.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        setThinBorders(style);

        return style;
    }

    /**
     * Selects the appropriate severity style.
     */
    private CellStyle severityStyle(
            String severity,
            WorkbookStyles styles) {

        if ("CRITICAL".equalsIgnoreCase(
                severity)) {

            return styles.criticalStatusStyle;
        }

        if ("WARNING".equalsIgnoreCase(
                severity)) {

            return styles.warningStatusStyle;
        }

        return styles.pendingStatusStyle;
    }

    /**
     * Selects the appropriate notification-status style.
     */
    private CellStyle notificationStyle(
            String notificationStatus,
            WorkbookStyles styles) {

        if ("EMAIL_SENT".equalsIgnoreCase(
                notificationStatus)
                || "SENT".equalsIgnoreCase(
                        notificationStatus)
                || "SIMULATED_SENT".equalsIgnoreCase(
                        notificationStatus)) {

            return styles.successStatusStyle;
        }

        if ("EMAIL_FAILED".equalsIgnoreCase(
                notificationStatus)) {

            return styles.failureStatusStyle;
        }

        return styles.pendingStatusStyle;
    }

    /**
     * Creates a safely formatted string cell.
     */
    private void createStringCell(
            Row row,
            int column,
            String value,
            CellStyle style) {

        Cell cell
                = row.createCell(column);

        cell.setCellValue(
                safeExcelText(value)
        );

        cell.setCellStyle(style);
    }

    /**
     * Creates a numeric cell.
     */
    private void createNumericCell(
            Row row,
            int column,
            double value,
            CellStyle style) {

        Cell cell
                = row.createCell(column);

        if (Double.isFinite(value)) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("-");
        }

        cell.setCellStyle(style);
    }

    /**
     * Creates a date cell from a SQL timestamp.
     */
    private void createDateCell(
            Row row,
            int column,
            Timestamp timestamp,
            CellStyle style) {

        Cell cell
                = row.createCell(column);

        if (timestamp == null) {

            cell.setCellValue("-");

        } else {

            Date date
                    = new Date(
                            timestamp.getTime()
                    );

            cell.setCellValue(date);
        }

        cell.setCellStyle(style);
    }

    /**
     * Applies thin borders to a cell style.
     */
    private void setThinBorders(
            CellStyle style) {

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        short borderColour
                = IndexedColors.GREY_40_PERCENT.getIndex();

        style.setTopBorderColor(borderColour);
        style.setBottomBorderColor(borderColour);
        style.setLeftBorderColor(borderColour);
        style.setRightBorderColor(borderColour);
    }

    /**
     * Resolves the authenticated user's display name.
     */
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

    /**
     * Protects exported text from spreadsheet formula interpretation.
     */
    private String safeExcelText(String value) {

        String cleanedValue
                = safeText(value);

        if ("-".equals(cleanedValue)) {
            return cleanedValue;
        }

        if (cleanedValue.startsWith("=")
                || cleanedValue.startsWith("+")
                || cleanedValue.startsWith("-")
                || cleanedValue.startsWith("@")) {

            return "'"
                    + cleanedValue;
        }

        return cleanedValue;
    }

    /**
     * Returns safe printable text.
     */
    private String safeText(String value) {

        return hasText(value)
                ? value.trim()
                : "-";
    }

    /**
     * Checks whether a string contains text.
     */
    private boolean hasText(String value) {

        return value != null
                && !value.isBlank();
    }

    /**
     * Stores workbook styles shared by all worksheets.
     */
    private static class WorkbookStyles {

        private CellStyle titleStyle;
        private CellStyle headerStyle;

        private CellStyle bodyTextStyle;
        private CellStyle bodyNumericStyle;
        private CellStyle bodyDateStyle;

        private CellStyle alternateTextStyle;
        private CellStyle alternateNumericStyle;
        private CellStyle alternateDateStyle;

        private CellStyle criticalStatusStyle;
        private CellStyle warningStatusStyle;
        private CellStyle successStatusStyle;
        private CellStyle failureStatusStyle;
        private CellStyle pendingStatusStyle;

        private CellStyle primarySummaryStyle;
        private CellStyle criticalSummaryStyle;
        private CellStyle warningSummaryStyle;
        private CellStyle successSummaryStyle;
        private CellStyle failureSummaryStyle;

        private CellStyle summaryLabelStyle;
        private CellStyle informationLabelStyle;
        private CellStyle sectionHeadingStyle;
        private CellStyle confidentialityStyle;
        private CellStyle emptyMessageStyle;
    }
}
