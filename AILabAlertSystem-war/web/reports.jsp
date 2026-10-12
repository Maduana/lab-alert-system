<%--
    Document   : reports
    Purpose    : Secure alert report preview and export page
    Author     : USER
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="includes/header.jspf" %>

<c:url var="reportsUrl" value="/reports" />
<c:url var="reportExportUrl" value="/report-export" />
<c:url var="dashboardUrl" value="/dashboard" />

<%-- =====================================================
     PAGE HEADING
===================================================== --%>

<div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
    <div>
        <h2 class="fw-bold mb-1">
            Alert Reporting Module
        </h2>
        <p class="text-muted mb-0">
            Preview and export authorised laboratory alert records.
        </p>
    </div>

    <%-- FIXED: Added missing <a href="..."> wrapper --%>
    <a href="${dashboardUrl}" class="btn btn-outline-secondary">
        Back to Dashboard
    </a>
</div>

<%-- =====================================================
     ROLE AND REPORT SCOPE
===================================================== --%>

<div class="card app-card mb-4">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
                <div class="text-muted small">Report scope</div>
                <div class="fw-bold"><c:out value="${reportScope}" /></div>
            </div>

            <div>
                <div class="text-muted small">Generated for</div>
                <div class="fw-bold">
                    <c:choose>
                        <c:when test="${not empty reportUser.fullName}">
                            <c:out value="${reportUser.fullName}" />
                        </c:when>
                        <c:otherwise>
                            <c:out value="${reportUser.username}" />
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div>
                <div class="text-muted small">Current role</div>
                <span class="badge bg-primary"><c:out value="${reportUser.role}" /></span>
            </div>

            <div>
                <div class="text-muted small">Matching records</div>
                <div class="fs-4 fw-bold text-primary"><c:out value="${recordCount}" /></div>
            </div>
        </div>
    </div>
</div>

<c:if test="${isAdministrator}">
    <div class="alert alert-info" role="alert">
        <strong>Administrator report scope:</strong>
        This report includes all alerts authorised for administrator monitoring.
    </div>
</c:if>

<c:if test="${isPathologist}">
    <div class="alert alert-info" role="alert">
        <strong>Pathologist report scope:</strong>
        This report contains only alerts for patients assigned to your pathologist profile.
    </div>
</c:if>

<%-- =====================================================
     SUCCESS AND ERROR MESSAGES
===================================================== --%>

<c:if test="${not empty requestScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <c:out value="${requestScope.error}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>

<c:if test="${not empty param.msg}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <c:out value="${param.msg}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>

<c:if test="${not empty param.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <c:out value="${param.error}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>

<%-- =====================================================
     REPORT FILTERS
===================================================== --%>

<div class="card app-card mb-4">
    <div class="card-header">Report Filters</div>
    <div class="card-body">

        <%-- FIXED: Added missing <form> tag with proper action and id --%>
        <form id="reportFilterForm" action="${reportsUrl}" method="get" novalidate>
            <div class="row g-3">
                <div class="col-md-6 col-xl-3">
                    <label for="startDate" class="form-label">Start Date</label>
                    <input id="startDate" name="startDate" type="date" class="form-control" value="${fn:escapeXml(selectedStartDate)}" />
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="endDate" class="form-label">End Date</label>
                    <input id="endDate" name="endDate" type="date" class="form-control" value="${fn:escapeXml(selectedEndDate)}" />
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="severity" class="form-label">Severity</label>
                    <select id="severity" name="severity" class="form-select">
                        <option value="" ${empty selectedSeverity ? 'selected' : ''}>All severities</option>
                        <option value="CRITICAL" ${selectedSeverity eq 'CRITICAL' ? 'selected' : ''}>Critical</option>
                        <option value="WARNING" ${selectedSeverity eq 'WARNING' ? 'selected' : ''}>Warning</option>
                    </select>
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="notificationStatus" class="form-label">Email Status</label>
                    <select id="notificationStatus" name="notificationStatus" class="form-select">
                        <option value="" ${empty selectedNotificationStatus ? 'selected' : ''}>All email statuses</option>
                        <option value="PENDING" ${selectedNotificationStatus eq 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="EMAIL_SENT" ${selectedNotificationStatus eq 'EMAIL_SENT' ? 'selected' : ''}>Email Sent</option>
                        <option value="EMAIL_FAILED" ${selectedNotificationStatus eq 'EMAIL_FAILED' ? 'selected' : ''}>Email Failed</option>
                        <option value="SENT" ${selectedNotificationStatus eq 'SENT' ? 'selected' : ''}>Sent</option>
                        <option value="SIMULATED_SENT" ${selectedNotificationStatus eq 'SIMULATED_SENT' ? 'selected' : ''}>Simulated Sent</option>
                    </select>
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="acknowledgementStatus" class="form-label">Acknowledgement</label>
                    <select id="acknowledgementStatus" name="acknowledgementStatus" class="form-select">
                        <option value="" ${empty selectedAcknowledgementStatus ? 'selected' : ''}>All acknowledgement states</option>
                        <option value="ACKNOWLEDGED" ${selectedAcknowledgementStatus eq 'ACKNOWLEDGED' ? 'selected' : ''}>Acknowledged</option>
                        <option value="PENDING" ${selectedAcknowledgementStatus eq 'PENDING' ? 'selected' : ''}>Pending Review</option>
                    </select>
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="patientCode" class="form-label">Patient Code</label>
                    <input id="patientCode" name="patientCode" type="text" class="form-control" value="${fn:escapeXml(selectedPatientCode)}" maxlength="30" pattern="[A-Za-z0-9_-]*" placeholder="Example: P003" />
                    <div class="form-text">Letters, numbers, hyphens and underscores only.</div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <label for="testName" class="form-label">Test Name</label>
                    <select id="testName" name="testName" class="form-select">
                        <option value="" ${empty selectedTestName ? 'selected' : ''}>All laboratory tests</option>
                        <option value="Glucose" ${selectedTestName eq 'Glucose' ? 'selected' : ''}>Glucose</option>
                        <option value="Potassium" ${selectedTestName eq 'Potassium' ? 'selected' : ''}>Potassium</option>
                        <option value="Hemoglobin" ${selectedTestName eq 'Hemoglobin' ? 'selected' : ''}>Hemoglobin</option>
                        <option value="WBC" ${selectedTestName eq 'WBC' ? 'selected' : ''}>White Blood Cell Count</option>
                        <option value="Platelets" ${selectedTestName eq 'Platelets' ? 'selected' : ''}>Platelets</option>
                    </select>
                </div>

                <div class="col-md-6 col-xl-3 d-flex align-items-end gap-2">
                    <button type="submit" class="btn btn-primary flex-grow-1">Apply Filters</button>

                    <%-- FIXED: Added missing <a href="..."> wrapper --%>
                    <a href="${reportsUrl}" class="btn btn-outline-secondary">Clear</a>
                </div>
            </div>
        </form>

    </div>
</div>

<%-- =====================================================
     EXPORT CONTROLS
===================================================== --%>

<div class="card app-card mb-4">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
                <h5 class="mb-1">Export Report</h5>
                <p class="text-muted mb-0">Export the currently selected and authorised alert records.</p>
            </div>

            <div class="d-flex flex-wrap gap-2">

                <%-- FIXED: Added missing PDF <form> tag --%>
                <form action="${reportExportUrl}" method="post" class="d-inline">
                    <c:if test="${not empty requestScope._csrf}">
                        <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope._csrf)}" />
                    </c:if>
                    <input type="hidden" name="format" value="pdf" />
                    <input type="hidden" name="startDate" value="${fn:escapeXml(selectedStartDate)}" />
                    <input type="hidden" name="endDate" value="${fn:escapeXml(selectedEndDate)}" />
                    <input type="hidden" name="severity" value="${fn:escapeXml(selectedSeverity)}" />
                    <input type="hidden" name="notificationStatus" value="${fn:escapeXml(selectedNotificationStatus)}" />
                    <input type="hidden" name="acknowledgementStatus" value="${fn:escapeXml(selectedAcknowledgementStatus)}" />
                    <input type="hidden" name="patientCode" value="${fn:escapeXml(selectedPatientCode)}" />
                    <input type="hidden" name="testName" value="${fn:escapeXml(selectedTestName)}" />

                    <button type="submit" class="btn btn-danger" ${empty alerts ? 'disabled' : ''}>
                        Export PDF
                    </button>
                </form>

                <%-- FIXED: Added missing Excel <form> tag --%>
                <form action="${reportExportUrl}" method="post" class="d-inline">
                    <c:if test="${not empty requestScope._csrf}">
                        <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope._csrf)}" />
                    </c:if>
                    <input type="hidden" name="format" value="xlsx" />
                    <input type="hidden" name="startDate" value="${fn:escapeXml(selectedStartDate)}" />
                    <input type="hidden" name="endDate" value="${fn:escapeXml(selectedEndDate)}" />
                    <input type="hidden" name="severity" value="${fn:escapeXml(selectedSeverity)}" />
                    <input type="hidden" name="notificationStatus" value="${fn:escapeXml(selectedNotificationStatus)}" />
                    <input type="hidden" name="acknowledgementStatus" value="${fn:escapeXml(selectedAcknowledgementStatus)}" />
                    <input type="hidden" name="patientCode" value="${fn:escapeXml(selectedPatientCode)}" />
                    <input type="hidden" name="testName" value="${fn:escapeXml(selectedTestName)}" />

                    <button type="submit" class="btn btn-success" ${empty alerts ? 'disabled' : ''}>
                        Export Excel
                    </button>
                </form>

            </div>
        </div>
    </div>
</div>

<%-- =====================================================
     REPORT PREVIEW
===================================================== --%>

<div class="card app-card">
    <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <span>Alert Report Preview</span>
        <span class="badge bg-primary"><c:out value="${recordCount}" /> records</span>
    </div>

    <div class="card-body table-responsive">
        <c:choose>
            <c:when test="${empty alerts}">
                <div class="alert alert-info mb-0">No alerts match the selected report filters.</div>
            </c:when>
            <c:otherwise>
                <table class="table table-striped table-hover align-middle">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Patient</th>
                            <th>Test</th>
                            <th>Result</th>
                            <th>Severity</th>
                            <th>Alert Message</th>
                            <th>Email Status</th>
                            <th>Acknowledgement</th>
                            <th>Acknowledged By</th>
                            <th>Acknowledged At</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${alerts}" var="alert">
                            <tr>
                                <td class="text-nowrap">
                                    <c:choose>
                                        <c:when test="${not empty alert.createdAt}"><c:out value="${alert.createdAt}" /></c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <strong><c:out value="${alert.patientCode}" /></strong><br />
                                    <small class="text-muted"><c:out value="${alert.patientName}" /></small>
                                </td>
                                <td><c:out value="${alert.testName}" /></td>
                                <td><c:out value="${alert.resultValue}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${alert.severity eq 'CRITICAL'}">
                                            <span class="badge bg-danger">CRITICAL</span>
                                        </c:when>
                                        <c:when test="${alert.severity eq 'WARNING'}">
                                            <span class="badge bg-warning text-dark">WARNING</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary"><c:out value="${alert.severity}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${alert.alertMessage}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${alert.notificationStatus eq 'EMAIL_SENT'}">
                                            <span class="badge bg-info text-dark">EMAIL SENT</span>
                                        </c:when>
                                        <c:when test="${alert.notificationStatus eq 'EMAIL_FAILED'}">
                                            <span class="badge bg-danger">EMAIL FAILED</span>
                                        </c:when>
                                        <c:when test="${alert.notificationStatus eq 'PENDING'}">
                                            <span class="badge bg-secondary">PENDING</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary"><c:out value="${alert.notificationStatus}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${alert.acknowledged}">
                                            <span class="badge bg-success">ACKNOWLEDGED</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-warning text-dark">PENDING REVIEW</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty alert.acknowledgedBy}"><c:out value="${alert.acknowledgedBy}" /></c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-nowrap">
                                    <c:choose>
                                        <c:when test="${not empty alert.acknowledgedAt}"><c:out value="${alert.acknowledgedAt}" /></c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%-- =====================================================
     CLIENT-SIDE DATE VALIDATION
===================================================== --%>

<script>
    (() => {
        "use strict";

        const filterForm = document.getElementById("reportFilterForm");
        const startDate = document.getElementById("startDate");
        const endDate = document.getElementById("endDate");

        if (!filterForm || !startDate || !endDate) {
            return;
        }

        function validateDateRange() {
            if (startDate.value === "" || endDate.value === "") {
                startDate.setCustomValidity("");
                endDate.setCustomValidity("");
                return true;
            }

            const valid = startDate.value <= endDate.value;

            startDate.setCustomValidity(valid ? "" : "Start date may not be later than end date.");
            endDate.setCustomValidity(valid ? "" : "End date may not be earlier than start date.");

            return valid;
        }

        startDate.addEventListener("change", validateDateRange);
        endDate.addEventListener("change", validateDateRange);

        filterForm.addEventListener("submit", event => {
            if (!validateDateRange() || !filterForm.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                filterForm.classList.add("was-validated");
            }
        });
    })();
</script>

<%@ include file="includes/footer.jspf" %>