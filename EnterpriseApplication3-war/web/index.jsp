<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="includes/header.jspf" %>

<div class="login-wrapper">
    <div class="card app-card login-card">
        <div class="card-body p-4 p-md-5">

            <img src="${pageContext.request.contextPath}/assets/images/logo.png"
                 alt="Lab Alert System logo"
                 class="login-logo">

            <h1 class="login-title">Lab Alert System</h1>
            <p class="login-subtitle">
                AI-Based Laboratory Test Result Flagging & Alert System
            </p>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger">${param.error}</div>
            </c:if>

            <c:if test="${not empty param.msg}">
                <div class="alert alert-success">${param.msg}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="mb-3">
                    <label class="form-label">Username</label>
                    <input type="text"
                           name="username"
                           class="form-control"
                           value="admin"
                           required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <input type="password"
                           name="password"
                           class="form-control"
                           required>
                </div>

                <button type="submit" class="btn btn-primary w-100">
                    Login
                </button>
            </form>

            <div class="mt-3 text-center text-muted small">
                Demo login: <strong>admin</strong> / <strong>admin123</strong>
            </div>
        </div>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>