<%-- 
    Document   : alerts
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
     URLS
     ============================================================ --%>

<c:url var="dashboardUrl"
       value="/dashboard" />

<c:url var="alertActionUrl"
       value="/alert-action" />

<c:url var="alertReminderUrl"
       value="/alert-reminder" />

<%-- ============================================================
     ROLE FLAGS
     ============================================================ --%>

<c:set var="currentRole"
       value="${fn:toUpperCase(
                fn:trim(
                sessionScope.loggedInUser.role
                )
                )}" />

<c:set var="isPathologist"
       value="${currentRole eq 'PATHOLOGIST'}" />

<c:set var="isAdministrator"
       value="${currentRole eq 'ADMIN'
                or currentRole eq 'ADMINISTRATOR'
                or currentRole eq
                'LABORATORY_ADMINISTRATOR'}" />

<%-- ============================================================
     SUCCESS AND ERROR MESSAGES
     ============================================================ --%>

<c:if test="${not empty param.msg}">

    <div class="alert alert-success
         alert-dismissible fade show"
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

    <div class="alert alert-danger
         alert-dismissible fade show"
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
     PAGE HEADER WITH BACK BUTTON
     ============================================================ --%>

<div class="d-flex justify-content-between
     align-items-center flex-wrap gap-3 mb-3">

    <div>

        <h3 class="fw-bold mb-0">
            All Alerts
        </h3>

        <div class="text-muted small">

            <c:choose>

                <c:when test="${isPathologist}">
                    Assigned alert history, deadlines and
                    acknowledgement status
                </c:when>

                <c:when test="${isAdministrator}">
                    System-wide alert monitoring, escalation and
                    reminder management
                </c:when>

                <c:otherwise>
                    Alert history and acknowledgement status
                </c:otherwise>

            </c:choose>

        </div>

    </div>

    <a href="${dashboardUrl}"
       class="btn btn-outline-secondary">
        Back to Dashboard
    </a>

</div>

<%-- ============================================================
     ROLE INFORMATION
     ============================================================ --%>

<c:if test="${isAdministrator}">

    <div class="alert alert-info"
         role="alert">

        <strong>Administrator view:</strong>

        Administrators may monitor deadlines and send controlled
        reminders for overdue alerts. Alert acknowledgement must
        be completed by the assigned Pathologist.

        <div class="small mt-1">
            A maximum of three reminders may be sent, with a
            15-minute cooldown between reminders.
        </div>

    </div>

</c:if>

<c:if test="${isPathologist}">

    <div class="alert alert-info"
         role="alert">

        <strong>Pathologist view:</strong>

        Only alerts belonging to assigned patients are shown.
        Open alerts may be acknowledged after professional review.

    </div>

</c:if>

<%-- ============================================================
     REVIEW STATUS LEGEND
     ============================================================ --%>

<div class="card app-card mb-3">

    <div class="card-body py-3">

        <div class="d-flex flex-wrap align-items-center gap-2">

            <span class="small fw-semibold me-1">
                Review status:
            </span>

            <span class="badge bg-success">
                ON TIME
            </span>

            <span class="badge bg-warning text-dark">
                DUE SOON
            </span>

            <span class="badge bg-danger">
                OVERDUE
            </span>

            <span class="badge bg-primary">
                ACKNOWLEDGED ON TIME
            </span>

            <span class="badge bg-secondary">
                ACKNOWLEDGED LATE
            </span>

        </div>

    </div>

</div>

<%-- ============================================================
     ALERT REGISTER
     ============================================================ --%>

<div class="alerts-page">

    <div class="card app-card">

        <div class="card-header d-flex
             justify-content-between
             align-items-center
             flex-wrap gap-2">

            <span class="fw-semibold">
                Alert Register
            </span>

            <span class="badge bg-secondary">
                <c:out value="${fn:length(alerts)}"
                       default="0" />
            </span>

        </div>

        <div class="card-body p-0">

            <c:choose>

                <c:when test="${empty alerts}">

                    <div class="alert alert-info m-3">
                        No alerts are available.
                    </div>

                </c:when>

                <c:otherwise>

                    <%-- Wrapper uses safe auto-scroll fallback if screens are exceptionally narrow --%>
                    <div class="alert-register-scroll">

                        <table class="table table-striped
                               table-hover align-middle
                               alert-register-table">

                            <%-- Percentage-based colgroup ensuring columns fit on screen --%>
                            <colgroup>
                                <col style="width: 4%;">   <%-- Alert ID --%>
                                <col style="width: 9%;">   <%-- Created --%>
                                <col style="width: 9%;">   <%-- Patient --%>
                                <col style="width: 6%;">   <%-- Test --%>
                                <col style="width: 5%;">   <%-- Result --%>
                                <col style="width: 7%;">   <%-- Severity --%>
                                <col style="width: 14%;">  <%-- Message --%>
                                <col style="width: 7%;">   <%-- Email Status --%>
                                <col style="width: 9%;">   <%-- Review Due --%>
                                <col style="width: 9%;">   <%-- Review Status --%>
                                <col style="width: 6%;">   <%-- Reminders --%>
                                <col style="width: 7%;">   <%-- Acknowledged By --%>
                                <col style="width: 9%;">   <%-- Acknowledged At --%>
                                <col style="width: 9%;">   <%-- Action (Sticky) --%>
                            </colgroup>

                            <thead>

                                <tr>

                                    <th scope="col" class="alert-id-cell">Alert</th>
                                    <th scope="col" class="date-cell">Created</th>
                                    <th scope="col" class="patient-cell">Patient</th>
                                    <th scope="col" class="test-cell">Test</th>
                                    <th scope="col" class="result-cell">Result</th>
                                    <th scope="col" class="severity-cell">Severity</th>
                                    <th scope="col" class="alert-message-cell">Message</th>
                                    <th scope="col" class="email-status-cell">Email</th>
                                    <th scope="col" class="date-cell">Review Due</th>
                                    <th scope="col" class="status-cell">Review Status</th>
                                    <th scope="col" class="reminder-cell">Reminders</th>
                                    <th scope="col" class="acknowledged-by-cell">Acknowledged By</th>
                                    <th scope="col" class="acknowledged-at-cell">Acknowledged At</th>
                                    <th scope="col" class="action-cell sticky-action">Action</th>

                                </tr>

                            </thead>

                            <tbody>

                                <c:forEach items="${alerts}"
                                           var="a">

                                    <tr class="${a.escalated
                                                 and not a.acknowledged
                                                 ? 'table-danger'
                                                 : ''}">

                                        <%-- Alert identifier --%>
                                        <td class="alert-id-cell">
                                            <strong>
                                                #<c:out value="${a.id}" />
                                            </strong>
                                        </td>

                                        <%-- Created timestamp (stacked date/time) --%>
                                        <td class="date-cell">
                                            <c:choose>
                                                <c:when test="${not empty a.createdAt}">
                                                    <c:set var="createdParts" value="${fn:split(a.createdAt, ' ')}" />
                                                    <span class="d-block fw-semibold"><c:out value="${createdParts[0]}" /></span>
                                                    <c:if test="${fn:length(createdParts) gt 1}">
                                                        <span class="d-block text-muted" style="font-size: 0.65rem;"><c:out value="${fn:substring(createdParts[1], 0, 8)}" /></span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Patient --%>
                                        <td class="patient-cell">
                                            <strong class="d-block text-truncate">
                                                <c:out value="${a.patientCode}" default="-" />
                                            </strong>
                                            <small class="text-muted d-block text-truncate">
                                                <c:out value="${a.patientName}" default="-" />
                                            </small>
                                        </td>

                                        <%-- Laboratory test --%>
                                        <td class="test-cell">
                                            <span class="text-truncate d-block">
                                                <c:out value="${a.testName}" default="-" />
                                            </span>
                                        </td>

                                        <%-- Result value --%>
                                        <td class="result-cell">
                                            <c:out value="${a.resultValue}" />
                                        </td>

                                        <%-- Severity --%>
                                        <td class="severity-cell">
                                            <c:choose>
                                                <c:when test="${a.severity eq 'CRITICAL'}">
                                                    <span class="badge bg-danger">CRITICAL</span>
                                                </c:when>
                                                <c:when test="${a.severity eq 'WARNING'}">
                                                    <span class="badge bg-warning text-dark">WARNING</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">
                                                        <c:out value="${a.severity}" default="UNKNOWN" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Alert message (Clamped with tooltip title) --%>
                                        <td class="alert-message-cell" title="${fn:escapeXml(a.alertMessage)}">
                                            <div class="message-clamp">
                                                <c:out value="${a.alertMessage}" default="No alert message." />
                                            </div>
                                        </td>

                                        <%-- Email notification status --%>
                                        <td class="email-status-cell">
                                            <c:choose>
                                                <c:when test="${a.notificationStatus eq 'EMAIL_SENT' or a.notificationStatus eq 'SENT'}">
                                                    <span class="badge bg-info text-dark">SENT</span>
                                                </c:when>
                                                <c:when test="${a.notificationStatus eq 'EMAIL_FAILED'}">
                                                    <span class="badge bg-danger">FAILED</span>
                                                </c:when>
                                                <c:when test="${a.notificationStatus eq 'SIMULATED_SENT'}">
                                                    <span class="badge bg-info text-dark">SIMULATED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">
                                                        <c:out value="${a.notificationStatus}" default="PENDING" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Review deadline (stacked) --%>
                                        <td class="date-cell">
                                            <c:choose>
                                                <c:when test="${not empty a.reviewDueAt}">
                                                    <c:set var="dueParts" value="${fn:split(a.reviewDueAt, ' ')}" />
                                                    <span class="d-block fw-semibold"><c:out value="${dueParts[0]}" /></span>
                                                    <c:if test="${fn:length(dueParts) gt 1}">
                                                        <span class="d-block text-muted" style="font-size: 0.65rem;"><c:out value="${fn:substring(dueParts[1], 0, 8)}" /></span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">Not set</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Review status --%>
                                        <td class="status-cell">
                                            <c:choose>
                                                <c:when test="${a.acknowledged and a.reviewStatus eq 'ACKNOWLEDGED ON TIME'}">
                                                    <span class="badge bg-primary">ACK. ON TIME</span>
                                                </c:when>
                                                <c:when test="${a.acknowledged and a.reviewStatus eq 'ACKNOWLEDGED LATE'}">
                                                    <span class="badge bg-secondary">ACK. LATE</span>
                                                </c:when>
                                                <c:when test="${a.acknowledged}">
                                                    <span class="badge bg-success">ACKNOWLEDGED</span>
                                                </c:when>
                                                <c:when test="${a.escalated or a.reviewStatus eq 'OVERDUE'}">
                                                    <span class="badge bg-danger">OVERDUE</span>
                                                    <c:if test="${a.escalated}">
                                                        <br><small class="text-danger fw-semibold">Escalated</small>
                                                    </c:if>
                                                </c:when>
                                                <c:when test="${a.reviewStatus eq 'DUE SOON'}">
                                                    <span class="badge bg-warning text-dark">DUE SOON</span>
                                                </c:when>
                                                <c:when test="${a.reviewStatus eq 'DEADLINE NOT CONFIGURED'}">
                                                    <span class="badge bg-secondary">NO DEADLINE</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success">ON TIME</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Reminder tracking --%>
                                        <td class="reminder-cell">
                                            <span class="badge ${a.reminderCount ge 3 ? 'bg-danger' : (a.reminderCount gt 0 ? 'bg-warning text-dark' : 'bg-secondary')}">
                                                <c:out value="${a.reminderCount}" default="0" /> / 3
                                            </span>
                                        </td>

                                        <%-- Acknowledged by --%>
                                        <td class="acknowledged-by-cell">
                                            <span class="text-truncate d-block">
                                                <c:out value="${a.acknowledgedBy}" default="-" />
                                            </span>
                                        </td>

                                        <%-- Acknowledged timestamp (stacked) --%>
                                        <td class="acknowledged-at-cell">
                                            <c:choose>
                                                <c:when test="${not empty a.acknowledgedAt}">
                                                    <c:set var="ackParts" value="${fn:split(a.acknowledgedAt, ' ')}" />
                                                    <span class="d-block"><c:out value="${ackParts[0]}" /></span>
                                                    <c:if test="${fn:length(ackParts) gt 1}">
                                                        <span class="d-block text-muted" style="font-size: 0.65rem;"><c:out value="${fn:substring(ackParts[1], 0, 8)}" /></span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Role-controlled action (Sticky) --%>
                                        <td class="action-cell sticky-action text-end">
                                            <c:choose>
                                                <c:when test="${a.acknowledged}">
                                                    <span class="badge bg-success">REVIEWED</span>
                                                </c:when>
                                                <c:when test="${isPathologist}">
                                                    <form action="${alertActionUrl}" method="post" class="d-inline">
                                                        <c:if test="${not empty requestScope._csrf}">
                                                            <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope._csrf)}">
                                                        </c:if>
                                                        <input type="hidden" name="alertId" value="${fn:escapeXml(a.id)}">
                                                        <input type="hidden" name="action" value="acknowledge">
                                                        <button type="submit" class="btn btn-xs btn-outline-primary">
                                                            Acknowledge
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:when test="${isAdministrator and a.reminderEligible}">
                                                    <form action="${alertReminderUrl}" method="post" class="d-inline">
                                                        <c:if test="${not empty requestScope._csrf}">
                                                            <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope._csrf)}">
                                                        </c:if>
                                                        <input type="hidden" name="alertId" value="${fn:escapeXml(a.id)}">
                                                        <button type="submit" class="btn btn-xs btn-outline-danger">
                                                            Remind
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted small">Monitor</span>
                                                </c:otherwise>
                                            </c:choose>
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

<%@ include file="includes/footer.jspf" %>