<%--
    Document   : dashboard
    Created on : 18 Apr 2026
    Updated on : 12 Nov 2026
    Author     : USER
--%>

<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<%@ include file="includes/header.jspf" %>

<%-- ============================================================
     AUTOMATIC REFRESH
     ============================================================ --%>

<div class="text-muted small mb-1">
    Operational metrics refresh automatically every 60 seconds.
</div>

<script>
    window.setInterval(function () {
        window.location.reload();
    }, 60000);
</script>

<%-- ============================================================
     STATUS MESSAGES
     ============================================================ --%>

<c:if test="${not empty param.msg}">
    <div class="alert alert-success alert-dismissible fade show"
         role="alert">

        <c:out value="${param.msg}" />

        <button type="button"
                class="btn-close"
                data-bs-dismiss="alert"
                aria-label="Close">
        </button>
    </div>
</c:if>

<c:if test="${not empty param.error}">
    <div class="alert alert-danger alert-dismissible fade show"
         role="alert">

        <c:out value="${param.error}" />

        <button type="button"
                class="btn-close"
                data-bs-dismiss="alert"
                aria-label="Close">
        </button>
    </div>
</c:if>

<%-- ============================================================
     HEADING AND ACTIONS
     ============================================================ --%>

<div class="d-flex justify-content-between
            align-items-center flex-wrap gap-3 mb-3">

    <div>
        <h3 class="fw-bold mb-0">
            Monitoring Dashboard
        </h3>

        <div class="text-muted small">
            <c:choose>
                <c:when test="${pathologistView}">
                    Pathologist view &mdash; showing only assigned
                    patients, results and alerts
                </c:when>

                <c:otherwise>
                    Administrator view &mdash; showing all patients,
                    results and alerts
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="d-flex align-items-center flex-wrap gap-2">

        <c:if test="${not pathologistView}">
            <a href="${pageContext.request.contextPath}/result-form"
               class="btn btn-primary">

                Capture New Result
            </a>
        </c:if>

        <c:if test="${sessionScope.loggedInUser.role eq 'ADMIN'
                      or sessionScope.loggedInUser.role eq
                         'ADMINISTRATOR'
                      or sessionScope.loggedInUser.role eq
                         'LABORATORY_ADMINISTRATOR'
                      or sessionScope.loggedInUser.role eq
                         'PATHOLOGIST'}">

            <a href="${pageContext.request.contextPath}/reports"
               class="btn btn-success">

                Reports
            </a>
        </c:if>

    </div>
</div>

<%-- ============================================================
     PRIMARY KPIs
     ============================================================ --%>

<div class="row g-3 mb-3">

    <div class="col-sm-6 col-xl-3">
        <div class="metric-card metric-blue h-100">

            <h6>Patients</h6>

            <h2>
                <c:out value="${totalPatients}"
                       default="0" />
            </h2>

            <div class="small opacity-75">
                <c:choose>
                    <c:when test="${pathologistView}">
                        Assigned patients
                    </c:when>

                    <c:otherwise>
                        Registered patients
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="metric-card metric-green h-100">

            <h6>Recent Results</h6>

            <h2>
                <c:out value="${totalResults}"
                       default="0" />
            </h2>

            <div class="small opacity-75">
                Latest laboratory records
            </div>

        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="metric-card metric-red h-100">

            <h6>Pending Critical</h6>

            <h2>
                <c:out value="${pendingCriticalCount}"
                       default="0" />
            </h2>

            <div class="small opacity-75">
                Awaiting professional review
            </div>

        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="metric-card metric-purple h-100">

            <h6>Total Alerts</h6>

            <h2>
                <c:out value="${totalAlerts}"
                       default="0" />
            </h2>

            <div class="small opacity-75">
                Warning and critical alerts
            </div>

        </div>
    </div>

</div>

<%-- ============================================================
     OPERATIONAL KPIs
     ============================================================ --%>

<div class="row g-3 mb-4">

    <%-- Overdue --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-danger">

            <div class="card-body">

                <div class="d-flex justify-content-between
                            align-items-start gap-2">

                    <div>
                        <div class="text-muted small fw-semibold">
                            Overdue Alerts
                        </div>

                        <div class="fs-2 fw-bold text-danger mt-1">
                            <c:out value="${overdueAlerts}"
                                   default="0" />
                        </div>
                    </div>

                    <span class="badge bg-danger">
                        OVERDUE
                    </span>

                </div>

                <div class="small text-muted mt-2">
                    Past the review deadline
                </div>

            </div>
        </div>
    </div>

    <%-- Due soon --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-warning">

            <div class="card-body">

                <div class="d-flex justify-content-between
                            align-items-start gap-2">

                    <div>
                        <div class="text-muted small fw-semibold">
                            Due Soon
                        </div>

                        <div class="fs-2 fw-bold text-warning mt-1">
                            <c:out value="${dueSoonAlerts}"
                                   default="0" />
                        </div>
                    </div>

                    <span class="badge bg-warning text-dark">
                        5 MIN
                    </span>

                </div>

                <div class="small text-muted mt-2">
                    Due within five minutes
                </div>

            </div>
        </div>
    </div>

    <%-- Acknowledgement rate --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-success">

            <div class="card-body">

                <div class="text-muted small fw-semibold">
                    Acknowledgement Rate
                </div>

                <div class="fs-2 fw-bold text-success mt-1">
                    <c:out value="${acknowledgementRate}"
                           default="0.0" />%
                </div>

                <div class="small text-muted mt-2">
                    Alerts reviewed
                </div>

            </div>
        </div>
    </div>

    <%-- Average review time --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-primary">

            <div class="card-body">

                <div class="text-muted small fw-semibold">
                    Average Review Time
                </div>

                <div class="fs-2 fw-bold text-primary mt-1">

                    <c:out value="${averageAcknowledgementMinutes}"
                           default="0.0" />

                    <span class="fs-6">
                        min
                    </span>

                </div>

                <div class="small text-muted mt-2">
                    Creation to acknowledgement
                </div>

            </div>
        </div>
    </div>

    <%-- Email success rate --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-info">

            <div class="card-body">

                <div class="text-muted small fw-semibold">
                    Email Success Rate
                </div>

                <div class="fs-2 fw-bold text-info mt-1">
                    <c:out value="${emailSuccessRate}"
                           default="0.0" />%
                </div>

                <div class="small text-muted mt-2">
                    Successful delivery attempts
                </div>

            </div>
        </div>
    </div>

    <%-- Escalated alerts --%>

    <div class="col-sm-6 col-md-4 col-xl-2">
        <div class="card app-card dashboard-kpi-card
                    h-100 border-dark">

            <div class="card-body">

                <div class="text-muted small fw-semibold">
                    Escalated Alerts
                </div>

                <div class="fs-2 fw-bold text-dark mt-1">
                    <c:out value="${escalatedAlerts}"
                           default="0" />
                </div>

                <div class="small text-muted mt-2">
                    Active overdue escalations
                </div>

            </div>
        </div>
    </div>

</div>

<%-- ============================================================
     REVIEW SUMMARY
     ============================================================ --%>

<div class="row g-3 mb-4">

    <div class="col-md-6">
        <div class="card app-card h-100">

            <div class="card-body d-flex
                        justify-content-between
                        align-items-center">

                <div>
                    <div class="text-muted small fw-semibold">
                        Acknowledged Alerts
                    </div>

                    <div class="fs-3 fw-bold text-success">
                        <c:out value="${acknowledgedAlerts}"
                               default="0" />
                    </div>
                </div>

                <span class="badge bg-success fs-6">
                    REVIEWED
                </span>

            </div>
        </div>
    </div>

    <div class="col-md-6">
        <div class="card app-card h-100">

            <div class="card-body d-flex
                        justify-content-between
                        align-items-center">

                <div>
                    <div class="text-muted small fw-semibold">
                        Awaiting Review
                    </div>

                    <div class="fs-3 fw-bold text-warning">
                        <c:out value="${awaitingReviewAlerts}"
                               default="0" />
                    </div>
                </div>

                <span class="badge bg-warning text-dark fs-6">
                    OPEN
                </span>

            </div>
        </div>
    </div>

</div>

<%-- ============================================================
     RESULTS AND ALERTS
     ============================================================ --%>

<div class="row g-4 align-items-start">

    <%-- Latest laboratory results --%>

    <div class="col-xl-7">
        <div class="card app-card">

            <div class="card-header d-flex
                        justify-content-between
                        align-items-center">

                <span>
                    Latest Laboratory Results
                </span>

                <span class="badge bg-secondary">
                    <c:out value="${totalResults}"
                           default="0" />
                </span>

            </div>

            <div class="card-body p-0">

                <c:choose>

                    <c:when test="${empty latestResults}">
                        <div class="alert alert-info m-3">
                            No laboratory results are available.
                        </div>
                    </c:when>

                    <c:otherwise>

                        <div class="dashboard-results-wrapper">

                            <table class="table table-hover
                                          align-middle
                                          dashboard-results-table">

                                <thead>
                                    <tr>
                                        <th scope="col">Time</th>
                                        <th scope="col">Patient</th>
                                        <th scope="col">Test</th>
                                        <th scope="col">Value</th>
                                        <th scope="col">Status</th>
                                        <th scope="col">AI Score</th>
                                    </tr>
                                </thead>

                                <tbody>

                                    <c:forEach items="${latestResults}"
                                               var="r">

                                        <tr>

                                            <td class="text-nowrap">
                                                <c:out value="${r.capturedAt}" />
                                            </td>

                                            <td>
                                                <strong>
                                                    <c:out value="${r.patientCode}" />
                                                </strong>

                                                <br>

                                                <small class="text-muted">
                                                    <c:out value="${r.patientName}" />
                                                </small>
                                            </td>

                                            <td>
                                                <c:out value="${r.testName}" />
                                            </td>

                                            <td class="text-nowrap">

                                                <c:out value="${r.resultValue}" />

                                                <c:if test="${not empty r.unit}">
                                                    <c:out value="${r.unit}" />
                                                </c:if>

                                            </td>

                                            <td>
                                                <c:choose>

                                                    <c:when test="${r.resultStatus
                                                                    eq 'CRITICAL'}">

                                                        <span class="badge bg-danger">
                                                            CRITICAL
                                                        </span>

                                                    </c:when>

                                                    <c:when test="${r.resultStatus
                                                                    eq 'ABNORMAL'}">

                                                        <span class="badge
                                                                     bg-warning
                                                                     text-dark">

                                                            ABNORMAL
                                                        </span>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <span class="badge bg-success">

                                                            <c:out value="${r.resultStatus}"
                                                                   default="NORMAL" />

                                                        </span>

                                                    </c:otherwise>

                                                </c:choose>
                                            </td>

                                            <td>
                                                <c:out value="${r.aiScore}"
                                                       default="0.0" />
                                            </td>

                                        </tr>

                                    </c:forEach>

                                </tbody>

                            </table>

                        </div>

                    </c:otherwise>

                </c:choose>

            </div>
        </div>
    </div>

    <%-- Priority alerts --%>

    <div class="col-xl-5">
        <div class="card app-card">

            <div class="card-header d-flex
                        justify-content-between
                        align-items-center
                        flex-wrap gap-2">

                <div class="d-flex align-items-center gap-2">

                    <span class="fw-bold">
                        Priority Alerts
                    </span>

                    <span class="badge bg-danger">
                        <c:out value="${awaitingReviewAlerts}"
                               default="0" />
                    </span>

                </div>

                <a href="${pageContext.request.contextPath}/alerts"
                   class="text-decoration-none">

                    View Full List
                </a>

            </div>

            <div class="card-body dashboard-alert-feed">

                <c:choose>

                    <c:when test="${empty alerts}">
                        <div class="alert alert-success mb-0">
                            No alerts are available.
                        </div>
                    </c:when>

                    <c:otherwise>

                        <c:forEach items="${alerts}"
                                   var="a"
                                   begin="0"
                                   end="4">

                            <div class="alert-card-item
                                        ${a.severity eq 'CRITICAL'
                                            ? 'critical'
                                            : 'warning'}
                                        mb-3">

                                <%-- Patient and severity --%>

                                <div class="d-flex
                                            justify-content-between
                                            align-items-start
                                            gap-2">

                                    <div class="fw-bold">

                                        <c:out value="${a.patientCode}" />
                                        -
                                        <c:out value="${a.patientName}" />

                                    </div>

                                    <span class="badge
                                                 ${a.severity eq 'CRITICAL'
                                                     ? 'bg-danger'
                                                     : 'bg-warning text-dark'}">

                                        <c:out value="${a.severity}" />

                                    </span>

                                </div>

                                <%-- Test and result --%>

                                <div class="mt-1">

                                    <strong>
                                        <c:out value="${a.testName}" />
                                    </strong>

                                    :

                                    <c:out value="${a.resultValue}" />

                                </div>

                                <%-- Message --%>

                                <small class="text-muted d-block mt-1">
                                    <c:out value="${a.alertMessage}" />
                                </small>

                                <%-- Review deadline --%>

                                <div class="small mt-2">

                                    <span class="text-muted">
                                        Review due:
                                    </span>

                                    <strong>
                                        <c:choose>

                                            <c:when test="${not empty
                                                            a.reviewDueAt}">

                                                <c:out value="${a.reviewDueAt}" />

                                            </c:when>

                                            <c:otherwise>
                                                Not configured
                                            </c:otherwise>

                                        </c:choose>
                                    </strong>

                                </div>

                                <%-- Notification and review status --%>

                                <div class="mt-2 d-flex
                                            justify-content-between
                                            align-items-center
                                            flex-wrap gap-2">

                                    <div class="d-flex flex-wrap gap-2">

                                        <span class="badge bg-dark">
                                            <c:out value="${a.notificationStatus}"
                                                   default="PENDING" />
                                        </span>

                                        <c:choose>

                                            <c:when test="${a.acknowledged}">

                                                <span class="badge bg-success">
                                                    ACKNOWLEDGED
                                                </span>

                                            </c:when>

                                            <c:when test="${a.escalated
                                                            or a.reviewStatus
                                                            eq 'OVERDUE'}">

                                                <span class="badge bg-danger">
                                                    OVERDUE
                                                </span>

                                            </c:when>

                                            <c:when test="${a.reviewStatus
                                                            eq 'DUE SOON'}">

                                                <span class="badge
                                                             bg-warning
                                                             text-dark">

                                                    DUE SOON
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="badge bg-success">
                                                    ON TIME
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                    <%-- Role-based action --%>

                                    <c:choose>

                                        <c:when test="${a.acknowledged}">

                                            <span class="badge bg-success">
                                                REVIEWED
                                            </span>

                                        </c:when>

                                        <c:when test="${sessionScope.loggedInUser.role
                                                        eq 'PATHOLOGIST'}">

                                            <form action="${pageContext.request.contextPath}/alert-action"
                                                  method="post"
                                                  class="d-inline">

                                                <c:if test="${not empty
                                                              requestScope._csrf}">

                                                    <input type="hidden"
                                                           name="_csrf"
                                                           value="${fn:escapeXml(
                                                               requestScope._csrf
                                                           )}">

                                                </c:if>

                                                <input type="hidden"
                                                       name="alertId"
                                                       value="${fn:escapeXml(
                                                           a.id
                                                       )}">

                                                <input type="hidden"
                                                       name="action"
                                                       value="acknowledge">

                                                <button type="submit"
                                                        class="btn btn-sm
                                                               btn-outline-primary">

                                                    Acknowledge
                                                </button>

                                            </form>

                                        </c:when>

                                        <c:otherwise>

                                            <span class="badge
                                                         bg-light
                                                         text-dark
                                                         border">

                                                Awaiting Pathologist Review
                                            </span>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                                <%-- Acknowledgement details --%>

                                <c:if test="${a.acknowledged}">

                                    <div class="small text-muted mt-2">

                                        Acknowledged by:

                                        <strong>
                                            <c:out value="${empty
                                                            a.acknowledgedBy
                                                            ? 'System User'
                                                            : a.acknowledgedBy}" />
                                        </strong>

                                        <c:if test="${not empty
                                                      a.acknowledgedAt}">

                                            on
                                            <c:out value="${a.acknowledgedAt}" />

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