<%-- 
    Document   : audit-logs
    Created on : 19 Apr 2026, 00:04:52
    Author     : USER
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="includes/header.jspf" %>

<h3 class="fw-bold mb-2">Audit Logs</h3>
<p class="text-muted">System activity and user actions</p>

${pageContext.request.contextPath}/audit-logs
    <select name="limit" class="form-select form-select-sm" style="width:140px">
        <option value="50"  ${limit==50?'selected':''}>Last 50</option>
        <option value="100" ${limit==100?'selected':''}>Last 100</option>
        <option value="200" ${limit==200?'selected':''}>Last 200</option>
        <option value="500" ${limit==500?'selected':''}>Last 500</option>
    </select>
    <button class="btn btn-sm btn-outline-primary">Refresh</button>
    ${pageContext.request.contextPath}/dashboardBack</a>
</form>

<div class="card app-card">
    <div class="card-body table-responsive">

        <c:choose>
            <c:when test="${empty logs}">
                <div class="alert alert-info">No audit logs available.</div>
            </c:when>

            <c:otherwise>
                <table class="table table-striped table-hover align-middle">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>User</th>
                            <th>Role</th>
                            <th>Action</th>
                            <th>Entity</th>
                            <th>ID</th>
                            <th>Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${logs}" var="log">
                            <tr>
                                <td>${log.createdAt}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty log.username}">
                                            ${log.username}
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">System</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge bg-dark">${log.role}</span>
                                </td>
                                <td class="fw-bold">${log.actionType}</td>
                                <td>${log.entityType}</td>
                                <td>${log.entityId}</td>
                                <td style="min-width:300px">
                                    ${log.actionDetails}
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