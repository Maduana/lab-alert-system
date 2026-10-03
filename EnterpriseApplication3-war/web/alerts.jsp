<%--
    Document   : alerts
    Created on : 18 Apr 2026
    Author     : USER
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="includes/header.jspf" %>

<c:url var="dashboardUrl" value="/dashboard" />
<c:url var="alertActionUrl" value="/alert-action" />

<%-- Determine the current user's role --%>
<c:set var="isPathologist"
       value="${sessionScope.loggedInUser.role eq 'PATHOLOGIST'}" />

<c:set var="isAdministrator"
       value="${sessionScope.loggedInUser.role eq 'ADMIN'}" />

<%-- =====================================================
     SUCCESS AND ERROR MESSAGES
===================================================== --%>

<c:if test="${not empty param.msg}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <c:out value="${param.msg}" />
        <button type="button"
                class="btn-close"
                data-bs-dismiss="alert"
                aria-label="Close">
        </button>
    </div>
</c:if>

<c:if test="${not empty param.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <c:out value="${param.error}" />
        <button type="button"
                class="btn-close"
                data-bs-dismiss="alert"
                aria-label="Close">
        </button>
    </div>
</c:if>

<%-- =====================================================
     PAGE HEADING
===================================================== --%>

<div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
    <div>
        <h3 class="fw-bold mb-0">
            All Alerts
        </h3>
        <div class="text-muted small">
            <c:choose>
                <c:when test="${isPathologist}">
                    Assigned alert history and acknowledgement status
                </c:when>
                <c:otherwise>
                    Alert history and acknowledgement status
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <a href="${dashboardUrl}" class="btn btn-outline-secondary btn-sm">
        Back to Dashboard
    </a>
</div>

<%-- Role information --%>

<c:if test="${isAdministrator}">
    <div class="alert alert-info" role="alert">
        <strong>Administrator view:</strong>
        Administrators may monitor alert and notification statuses.
        Alert acknowledgement must be completed by the assigned pathologist.
    </div>
</c:if>

<c:if test="${isPathologist}">
    <div class="alert alert-info" role="alert">
        <strong>Pathologist view:</strong>
        Only assigned, unacknowledged alerts may be acknowledged.
    </div>
</c:if>

<%-- =====================================================
     ALERT TABLE
===================================================== --%>

<div class="card app-card">
    <div class="card-body table-responsive">
        <c:choose>
            <c:when test="${empty alerts}">
                <div class="alert alert-info mb-0">
                    No alerts are available.
                </div>
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
                            <th>Message</th>
                            <th>Email Status</th>
                            <th>Acknowledged By</th>
                            <th>Acknowledged At</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${alerts}" var="a">
                            <tr>
                                <%-- Created date --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty a.createdAt}">
                                            <c:out value="${a.createdAt}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <%-- Patient --%>
                                <td>
                                    <strong>
                                        <c:out value="${a.patientCode}" />
                                    </strong>
                                    <br />
                                    <small class="text-muted">
                                        <c:out value="${a.patientName}" />
                                    </small>
                                </td>

                                <%-- Test --%>
                                <td>
                                    <c:out value="${a.testName}" />
                                </td>

                                <%-- Result --%>
                                <td>
                                    <c:out value="${a.resultValue}" />
                                </td>

                                <%-- Severity --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.severity eq 'CRITICAL'}">
                                            <span class="badge bg-danger">
                                                CRITICAL
                                            </span>
                                        </c:when>
                                        <c:when test="${a.severity eq 'WARNING'}">
                                            <span class="badge bg-warning text-dark">
                                                WARNING
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">
                                                <c:out value="${a.severity}" />
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <%-- Alert message --%>
                                <td class="alert-message-cell">
                                    <c:out value="${a.alertMessage}" />
                                </td>

                                <%-- Email status --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.notificationStatus eq 'EMAIL_SENT'}">
                                            <span class="badge bg-info text-dark">
                                                EMAIL SENT
                                            </span>
                                        </c:when>
                                        <c:when test="${a.notificationStatus eq 'EMAIL_FAILED'}">
                                            <span class="badge bg-danger">
                                                EMAIL FAILED
                                            </span>
                                        </c:when>
                                        <c:when test="${a.notificationStatus eq 'PENDING'}">
                                            <span class="badge bg-secondary">
                                                PENDING
                                            </span>
                                        </c:when>
                                        <c:when test="${a.notificationStatus eq 'SENT'}">
                                            <span class="badge bg-info text-dark">
                                                SENT
                                            </span>
                                        </c:when>
                                        <c:when test="${a.notificationStatus eq 'SIMULATED_SENT'}">
                                            <span class="badge bg-info text-dark">
                                                SIMULATED SENT
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">
                                                <c:choose>
                                                    <c:when test="${not empty a.notificationStatus}">
                                                        <c:out value="${a.notificationStatus}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        NOT SENT
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <%-- Acknowledged by --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty a.acknowledgedBy}">
                                            <c:out value="${a.acknowledgedBy}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <%-- Acknowledged at --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty a.acknowledgedAt}">
                                            <c:out value="${a.acknowledgedAt}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <%-- Role-controlled action --%>
                                <td>
                                    <c:choose>
                                        <%-- Already acknowledged --%>
                                        <c:when test="${a.acknowledged}">
                                            <span class="badge bg-success">
                                                ACKNOWLEDGED
                                            </span>
                                        </c:when>

                                        <%-- Only pathologists receive the form --%>
                                        <c:when test="${isPathologist}">
                                            <form action="${alertActionUrl}" method="post">
                                                <%-- Include CSRF token when available --%>
                                                <c:if test="${not empty requestScope._csrf}">
                                                    <input type="hidden"
                                                           name="_csrf"
                                                           value="${requestScope._csrf}" />
                                                </c:if>

                                                <input type="hidden"
                                                       name="alertId"
                                                       value="${a.id}" />

                                                <input type="hidden"
                                                       name="action"
                                                       value="acknowledge" />

                                                <button type="submit"
                                                        class="btn btn-sm btn-outline-primary">
                                                    Acknowledge
                                                </button>
                                            </form>
                                        </c:when>

                                        <%-- Administrators and other roles cannot acknowledge --%>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark border">
                                                Awaiting Pathologist Review
                                            </span>
                                        </c:otherwise>
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

<%@ include file="includes/footer.jspf" %>