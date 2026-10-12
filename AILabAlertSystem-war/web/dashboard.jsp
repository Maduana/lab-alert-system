<%-- 
    Document   : dashboard
    Created on : 18 Apr 2026, 06:39:09
    Author     : USER
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="includes/header.jspf" %>

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

<%-- Metric Summary Cards --%>
<div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="metric-card metric-blue">
            <h6>Patients</h6>
            <h2><c:out value="${patients.size()}" default="0" /></h2>
        </div>
    </div>

    <div class="col-md-4">
        <div class="metric-card metric-green">
            <h6>Recent Results</h6>
            <h2><c:out value="${latestResults.size()}" default="0" /></h2>
        </div>
    </div>

    <div class="col-md-4">
        <div class="metric-card metric-red">
            <h6>Pending Critical Alerts</h6>
            <h2><c:out value="${pendingCriticalCount}" default="0" /></h2>
        </div>
    </div>
</div>

<%-- Dashboard Heading & Action Controls --%>
<div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
    <div>
        <h3 class="fw-bold mb-0">
            Monitoring Dashboard
        </h3>
        <div class="text-muted small">
            <c:choose>
                <c:when test="${pathologistView}">
                    Pathologist view — showing only assigned patients, results and alerts
                </c:when>
                <c:otherwise>
                    Administrator view — showing all patients, results and alerts
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="d-flex align-items-center flex-wrap gap-2">
        <%-- Administrators and laboratory personnel may capture results --%>
        <c:if test="${not pathologistView}">
            <a href="${pageContext.request.contextPath}/result-form" class="btn btn-primary">
                Capture New Result
            </a>
        </c:if>

        <%-- Only administrators and pathologists may access reports --%>
        <c:if test="${sessionScope.loggedInUser.role eq 'ADMIN' or sessionScope.loggedInUser.role eq 'PATHOLOGIST'}">
            <a href="${pageContext.request.contextPath}/reports" class="btn btn-success">
                Reports
            </a>
        </c:if>
    </div>
</div>

<div class="row g-4">
    <!-- Latest Results Table -->
    <div class="col-lg-7">
        <div class="card app-card">
            <div class="card-header">
                Latest Laboratory Results
            </div>
            <div class="card-body table-responsive">
                <c:choose>
                    <c:when test="${empty latestResults}">
                        <div class="alert alert-info mb-0">
                            No laboratory results available.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="table table-hover align-middle">
                            <thead>
                                <tr>
                                    <th>Time</th>
                                    <th>Patient</th>
                                    <th>Test</th>
                                    <th>Value</th>
                                    <th>Status</th>
                                    <th>AI Score</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${latestResults}" var="r">
                                    <tr>
                                        <td><c:out value="${r.capturedAt}" /></td>
                                        <td>
                                            <strong><c:out value="${r.patientCode}" /></strong><br>
                                            <small class="text-muted"><c:out value="${r.patientName}" /></small>
                                        </td>
                                        <td><c:out value="${r.testName}" /></td>
                                        <td><c:out value="${r.resultValue}" /> <c:out value="${r.unit}" /></td>
                                        <td>
                                            <span class="badge ${r.resultStatus == 'CRITICAL' ? 'bg-danger' : (r.resultStatus == 'ABNORMAL' ? 'bg-warning text-dark' : 'bg-success')}">
                                                <c:out value="${r.resultStatus}" />
                                            </span>
                                        </td>
                                        <td><c:out value="${r.aiScore}" /></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Recent Alerts Card Feed -->
    <div class="col-lg-5">
        <div class="card app-card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Alerts</span>
                <a href="${pageContext.request.contextPath}/alerts" class="text-decoration-none">View Full List</a>
            </div>

            <div class="card-body">
                <c:choose>
                    <c:when test="${empty alerts}">
                        <div class="alert alert-success mb-0">
                            No alerts available.
                        </div>
                    </c:when>

                    <c:otherwise>
                        <c:forEach items="${alerts}" var="a" begin="0" end="4">
                            <div class="alert-card-item ${a.severity == 'CRITICAL' ? 'critical' : 'warning'} mb-3">
                                <div class="fw-bold">
                                    <c:out value="${a.patientCode}" /> - <c:out value="${a.patientName}" />
                                </div>
                                <div><c:out value="${a.testName}" />: <c:out value="${a.resultValue}" /></div>
                                <small class="text-muted"><c:out value="${a.alertMessage}" /></small>

                                <div class="mt-2 d-flex justify-content-between align-items-center flex-wrap gap-2">
                                    <div>
                                        <span class="badge bg-dark"><c:out value="${a.notificationStatus}" /></span>
                                    </div>

                                    <c:choose>
                                        <%-- Alert has already been acknowledged --%>
                                        <c:when test="${a.acknowledged}">
                                            <span class="badge bg-success ms-1">
                                                ACKNOWLEDGED
                                            </span>
                                        </c:when>

                                        <%-- Only Pathologists may acknowledge an alert --%>
                                        <c:when test="${sessionScope.loggedInUser.role eq 'PATHOLOGIST'}">
                                            <form action="${pageContext.request.contextPath}/alert-action" method="post" class="d-inline">
                                                <c:if test="${not empty requestScope._csrf}">
                                                    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope._csrf)}">
                                                </c:if>

                                                <input type="hidden" name="alertId" value="${fn:escapeXml(a.id)}">
                                                <input type="hidden" name="action" value="acknowledge">

                                                <button type="submit" class="btn btn-sm btn-outline-primary">
                                                    Acknowledge
                                                </button>
                                            </form>
                                        </c:when>

                                        <%-- Administrators and Laboratory Personnel only monitor --%>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark border">
                                                Awaiting Pathologist Review
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <c:if test="${a.acknowledged}">
                                    <div class="mt-2 small text-muted">
                                        Acknowledged by:
                                        <strong><c:out value="${empty a.acknowledgedBy ? 'System User' : a.acknowledgedBy}" /></strong>
                                        <c:if test="${not empty a.acknowledgedAt}">
                                            on <c:out value="${a.acknowledgedAt}" />
                                        </c:if>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>