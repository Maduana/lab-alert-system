<%-- 
    Document   : ccess-denied
    Created on : 03 Oct 2026, 21:01:15
    Author     : USER
--%>

<%--
    Document : access-denied
    Purpose  : Friendly access-denied page
    Author   : USER
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="includes/header.jspf" %>

<div class="access-denied-wrapper">
    <div class="access-denied-card">
        <div class="access-denied-icon" aria-hidden="true">
            &#128274;
        </div>

        <div class="access-denied-code">
            403
        </div>

        <h1 class="access-denied-title">
            Access Denied
        </h1>

        <p class="access-denied-message">
        <c:choose>
            <c:when test="${not empty requestScope.errorMessage}">
                <c:out value="${requestScope.errorMessage}" />
            </c:when>
            <c:otherwise>
                Only the assigned pathologist may acknowledge this alert.
            </c:otherwise>
        </c:choose>
        </p>

        <div class="access-denied-info">
            <strong>Why am I seeing this?</strong>
            <p class="mb-0 mt-2">
                Alert acknowledgement is restricted to the active pathologist assigned to the patient.
                Administrators and laboratory personnel may monitor the alert, but cannot acknowledge it.
            </p>
        </div>

        <div class="access-denied-actions">
            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
                Return to Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/alerts" class="btn btn-outline-secondary ms-2">
                View All Alerts
            </a>
        </div>

        <c:if test="${not empty sessionScope.loggedInUser}">
            <div class="access-denied-user mt-4">
                Signed in as: 
                <strong>
                    <c:out value="${sessionScope.loggedInUser.fullName}" />
                </strong>
                <c:if test="${not empty sessionScope.loggedInUser.role}">
                    <span class="badge bg-secondary ms-1">
                        <c:out value="${sessionScope.loggedInUser.role}" />
                    </span>
                </c:if>
            </div>
        </c:if>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>