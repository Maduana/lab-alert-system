<%--
    Document : access-denied
    Purpose  : Friendly and context-aware access-denied page
    Author   : USER
--%>

<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="includes/header.jspf" %>

<c:url var="dashboardUrl" value="/dashboard" />
<c:url var="alertsUrl" value="/alerts" />
<c:url var="reportsUrl" value="/reports" />

<div class="access-denied-wrapper">

    <div class="access-denied-card">

        <div class="access-denied-icon"
             aria-hidden="true">
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
                    You do not have permission to access this resource.
                </c:otherwise>

            </c:choose>

        </p>

        <div class="access-denied-info">

            <strong>
                Why am I seeing this?
            </strong>

            <p class="mb-0 mt-2">

                <c:choose>

                    <c:when test="${not empty requestScope.accessReason}">
                        <c:out value="${requestScope.accessReason}" />
                    </c:when>

                    <c:otherwise>
                        This action is restricted according to your
                        authenticated role and assigned permissions.
                        Return to the dashboard to continue with the
                        functions available to your account.
                    </c:otherwise>

                </c:choose>

            </p>

        </div>

        <div class="access-denied-actions
                    d-flex
                    justify-content-center
                    align-items-center
                    flex-wrap
                    gap-2">

            <a href="${dashboardUrl}"
               class="btn btn-primary">
                Return to Dashboard
            </a>

            <c:if test="${not empty sessionScope.loggedInUser
                          and (
                              sessionScope.loggedInUser.role eq 'ADMIN'
                              or
                              sessionScope.loggedInUser.role eq
                              'PATHOLOGIST'
                          )}">

                <a href="${alertsUrl}"
                   class="btn btn-outline-secondary">
                    View All Alerts
                </a>

            </c:if>

            <c:if test="${requestScope.showReportsLink eq true}">

                <a href="${reportsUrl}"
                   class="btn btn-outline-primary">
                    View Reports
                </a>

            </c:if>

        </div>

        <c:if test="${not empty sessionScope.loggedInUser}">

            <div class="access-denied-user mt-4">

                Signed in as:

                <strong>

                    <c:choose>

                        <c:when test="${not empty
                                      sessionScope.loggedInUser.fullName}">

                            <c:out value="${
                                sessionScope.loggedInUser.fullName
                            }" />

                        </c:when>

                        <c:otherwise>

                            <c:out value="${
                                sessionScope.loggedInUser.username
                            }" />

                        </c:otherwise>

                    </c:choose>

                </strong>

                <c:if test="${not empty
                              sessionScope.loggedInUser.role}">

                    <span class="badge bg-secondary ms-1">

                        <c:out value="${
                            sessionScope.loggedInUser.role
                        }" />

                    </span>

                </c:if>

            </div>

        </c:if>

    </div>

</div>

<%@ include file="includes/footer.jspf" %>