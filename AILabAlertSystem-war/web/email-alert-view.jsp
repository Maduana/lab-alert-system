<%-- 
    Document   : email-alert-view
    Created on : 18 Apr 2026, 05:50:49
    Author     : USER
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Laboratory Alert Review</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background: #f4f7fb;
            font-family: Arial, Helvetica, sans-serif;
        }

        .card {
            border-radius: 18px;
            border: none;
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
        }

        .page-header {
            background: linear-gradient(90deg, #2f2fc3 0%, #4a58e8 100%);
            color: #ffffff;
            border-radius: 18px 18px 0 0;
            padding: 22px 24px;
        }

        .page-subtitle {
            color: rgba(255, 255, 255, 0.85);
            font-size: 0.92rem;
            margin-top: 4px;
        }

        .badge-critical {
            background: #dc2626;
            color: #ffffff;
        }

        .badge-warning {
            background: #f59e0b;
            color: #ffffff;
        }

        .badge-normal {
            background: #16a34a;
            color: #ffffff;
        }

        .table th {
            width: 32%;
            background: #f8fafc;
        }

        .section-title {
            font-weight: 700;
            color: #111827;
            margin-bottom: 0.9rem;
        }
    </style>
</head>
<body>

<div class="container py-5">
    <div class="card">
        <div class="page-header">
            <h2 class="mb-0">Laboratory Alert Review</h2>
            <div class="page-subtitle">
                AI-Based Laboratory Test Result Flagging & Alert System
            </div>
        </div>

        <div class="card-body p-4">

            <c:if test="${not empty param.msg}">
                <div class="alert alert-success">${param.msg}</div>
            </c:if>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger">${param.error}</div>
            </c:if>

            <div class="d-flex flex-wrap gap-2 mb-3">
                <span class="badge p-2 ${alert.severity eq 'CRITICAL' ? 'badge-critical' : 'badge-warning'}">
                    ${alert.severity}
                </span>

                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline-primary btn-sm">
                    Open Dashboard
                </a>
            </div>

            <h5 class="section-title">Patient Information</h5>
            <table class="table table-bordered align-middle">
                <tr>
                    <th>Patient Code</th>
                    <td>${alert.patientCode}</td>
                </tr>
                <tr>
                    <th>Patient Name</th>
                    <td>${alert.patientName}</td>
                </tr>
                <tr>
                    <th>Patient ID</th>
                    <td>${alert.patientId}</td>
                </tr>
            </table>

            <h5 class="section-title mt-4">Result Information</h5>
            <table class="table table-bordered align-middle">
                <tr>
                    <th>Test Name</th>
                    <td>${result.testName}</td>
                </tr>
                <tr>
                    <th>Result Value</th>
                    <td>${result.resultValue} ${result.unit}</td>
                </tr>
                <tr>
                    <th>Normal Range</th>
                    <td>${result.normalMin} - ${result.normalMax}</td>
                </tr>
                <tr>
                    <th>Result Status</th>
                    <td>
                        <span class="badge p-2
                            ${result.resultStatus eq 'CRITICAL' ? 'badge-critical' :
                              (result.resultStatus eq 'ABNORMAL' ? 'badge-warning' : 'badge-normal')}">
                            ${result.resultStatus}
                        </span>
                    </td>
                </tr>
                <tr>
                    <th>AI Score</th>
                    <td>${result.aiScore}</td>
                </tr>
                <tr>
                    <th>Comment</th>
                    <td>${result.resultComment}</td>
                </tr>
                <tr>
                    <th>Captured At</th>
                    <td>${result.capturedAt}</td>
                </tr>
            </table>

            <h5 class="section-title mt-4">Alert Information</h5>
            <table class="table table-bordered align-middle">
                <tr>
                    <th>Alert Message</th>
                    <td>${alert.alertMessage}</td>
                </tr>
                <tr>
                    <th>Severity</th>
                    <td>${alert.severity}</td>
                </tr>
                <tr>
                    <th>Notification Status</th>
                    <td>${alert.notificationStatus}</td>
                </tr>
                <tr>
                    <th>Acknowledged</th>
                    <td>${alert.acknowledged ? 'Yes' : 'No'}</td>
                </tr>
                <tr>
                    <th>Acknowledged By</th>
                    <td>${empty alert.acknowledgedBy ? '-' : alert.acknowledgedBy}</td>
                </tr>
                <tr>
                    <th>Acknowledged At</th>
                    <td>${empty alert.acknowledgedAt ? '-' : alert.acknowledgedAt}</td>
                </tr>
                <tr>
                    <th>Created At</th>
                    <td>${alert.createdAt}</td>
                </tr>
            </table>

            <c:choose>
                <c:when test="${!alert.acknowledged}">
                    <div class="mt-4">
                        <form action="${ackUrl}" method="post">
                            <input type="hidden" name="alertId" value="${alert.id}">
                            <input type="hidden" name="exp" value="${exp}">
                            <input type="hidden" name="sig" value="${sig}">

                            <button type="submit" class="btn btn-success">
                                Acknowledge Alert
                            </button>
                        </form>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-info mt-4">
                        This alert has already been acknowledged.
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>