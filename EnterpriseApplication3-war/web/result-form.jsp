<%-- Document : result-form Purpose : Capture and validate laboratory results Author : USER --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="includes/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-xl-9 col-lg-10">
        <div class="card app-card">
            <%-- Page heading --%>
            <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div>
                    <h4 class="mb-1"> Capture Laboratory Result </h4>
                    <small class="text-muted"> Enter the patient, test result and reference-range information. </small>
                </div>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline-secondary btn-sm">
                    Back to Dashboard
                </a>
            </div>

            <div class="card-body p-4">
                <%-- Server-side validation message --%>
                <c:if test="${not empty requestScope.error}">
                    <div class="alert alert-danger" role="alert">
                        <c:out value="${requestScope.error}" />
                    </div>
                </c:if>

                <%-- Error passed through the URL --%>
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger" role="alert">
                        <c:out value="${param.error}" />
                    </div>
                </c:if>

                <%-- No patient records available --%>
                <c:if test="${empty patients}">
                    <div class="alert alert-warning" role="alert">
                        No patients are available. A patient must be created and assigned before a laboratory result can be captured.
                    </div>
                </c:if>

                <%-- Result-capture form --%>
                <form id="resultForm" action="${pageContext.request.contextPath}/submit-result" method="post" class="needs-validation" novalidate>

                    <%-- CSRF token, if supplied by the security filter --%>
                    <c:if test="${not empty requestScope._csrf}">
                        <input type="hidden" name="_csrf" value="${requestScope._csrf}" />
                    </c:if>

                    <div class="row g-3">
                        <%-- Patient --%>
                        <div class="col-md-6">
                            <label for="patientId" class="form-label">
                                Patient <span class="text-danger">*</span>
                            </label>
                            <select id="patientId" name="patientId" class="form-select" required>
                                <option value=""> Select patient </option>
                                <c:forEach items="${patients}" var="p">
                                    <option value="${p.id}" ${formPatientId eq p.id or formPatientId eq p.id.toString() ? 'selected' : ''}>
                                        <c:out value="${p.patientCode}" /> - <c:out value="${p.fullName}" />
                                    </option>
                                </c:forEach>
                            </select>
                            <div class="invalid-feedback"> Select a valid patient. </div>
                        </div>

                        <%-- Test name --%>
                        <div class="col-md-6">
                            <label for="testName" class="form-label">
                                Test Name <span class="text-danger">*</span>
                            </label>
                            <select id="testName" name="testName" class="form-select" required>
                                <option value=""> Select laboratory test </option>
                                <option value="Glucose" ${formTestName eq 'Glucose' ? 'selected' : ''}>Glucose</option>
                                <option value="Potassium" ${formTestName eq 'Potassium' ? 'selected' : ''}>Potassium</option>
                                <option value="Hemoglobin" ${formTestName eq 'Hemoglobin' ? 'selected' : ''}>Hemoglobin</option>
                                <option value="WBC" ${formTestName eq 'WBC' ? 'selected' : ''}>White Blood Cell Count</option>
                                <option value="Platelets" ${formTestName eq 'Platelets' ? 'selected' : ''}>Platelets</option>
                            </select>
                            <div class="invalid-feedback"> Select a supported laboratory test. </div>
                        </div>

                        <%-- Result value --%>
                        <div class="col-md-4">
                            <label for="resultValue" class="form-label">
                                Result Value <span class="text-danger">*</span>
                            </label>
                            <input id="resultValue" name="resultValue" type="number" class="form-control" value="<c:out value='${formResultValue}' />" min="0" max="1000000" step="0.01" placeholder="0.00" required />
                            <div class="invalid-feedback"> Enter a non-negative numerical result. </div>
                        </div>

                        <%-- Unit --%>
                        <div class="col-md-4">
                            <label for="unit" class="form-label">
                                Unit <span class="text-danger">*</span>
                            </label>
                            <input id="unit" name="unit" type="text" class="form-control" value="<c:out value='${formUnit}' />" maxlength="30" placeholder="Example: mmol/L" required />
                            <div class="invalid-feedback"> Enter the unit of measurement. </div>
                        </div>

                        <%-- Normal minimum --%>
                        <div class="col-md-2">
                            <label for="normalMin" class="form-label">
                                Normal Min <span class="text-danger">*</span>
                            </label>
                            <input id="normalMin" name="normalMin" type="number" class="form-control" value="<c:out value='${formNormalMin}' />" min="0" max="1000000" step="0.01" placeholder="0.00" required />
                            <div class="invalid-feedback"> Enter a valid non-negative minimum. </div>
                        </div>

                        <%-- Normal maximum --%>
                        <div class="col-md-2">
                            <label for="normalMax" class="form-label">
                                Normal Max <span class="text-danger">*</span>
                            </label>
                            <input id="normalMax" name="normalMax" type="number" class="form-control" value="<c:out value='${formNormalMax}' />" min="0" max="1000000" step="0.01" placeholder="0.00" required />
                            <div class="invalid-feedback"> Enter a maximum greater than the minimum. </div>
                        </div>
                    </div>

                    <%-- Cross-field validation message --%>
                    <div id="rangeValidationError" class="alert alert-danger mt-3 d-none" role="alert">
                        Normal maximum must be greater than normal minimum.
                    </div>

                    <%-- Workflow information --%>
                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Evaluation process:</strong> The system will validate the result, retrieve the patient's previous result where available, and process the submitted information using the AI/Rule Engine.
                    </div>

                    <%-- Form actions --%>
                    <div class="mt-4 d-flex justify-content-end align-items-center flex-wrap gap-2">
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">
                            Cancel
                        </a>
                        <c:choose>
                            <c:when test="${empty patients}">
                                <button id="submitButton" type="submit" class="btn btn-primary" disabled>
                                    Save Result &amp; Analyse
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button id="submitButton" type="submit" class="btn btn-primary">
                                    Save Result &amp; Analyse
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    (() => {
        "use strict";
        const form = document.getElementById("resultForm");
        const resultValue = document.getElementById("resultValue");
        const normalMin = document.getElementById("normalMin");
        const normalMax = document.getElementById("normalMax");
        const unit = document.getElementById("unit");
        const rangeError = document.getElementById("rangeValidationError");
        const submitButton = document.getElementById("submitButton");

        if (!form) {
            return;
        }

        function validateResultValue() {
            if (resultValue.value === "") {
                resultValue.setCustomValidity("");
                return true;
            }
            const value = Number(resultValue.value);
            const valid = Number.isFinite(value) && value >= 0 && value <= 1000000;
            resultValue.setCustomValidity(
                    valid ? "" : "Result value must be a non-negative number."
                    );
            return valid;
        }

        function validateReferenceRange() {
            if (normalMin.value === "" || normalMax.value === "") {
                normalMin.setCustomValidity("");
                normalMax.setCustomValidity("");
                rangeError.classList.add("d-none");
                return true;
            }
            const minimum = Number(normalMin.value);
            const maximum = Number(normalMax.value);
            const valid = Number.isFinite(minimum) && Number.isFinite(maximum) && minimum >= 0 && maximum >= 0 && minimum < maximum;
            normalMin.setCustomValidity(
                    valid ? "" : "Normal minimum must be lower than normal maximum."
                    );
            normalMax.setCustomValidity(
                    valid ? "" : "Normal maximum must be greater than normal minimum."
                    );
            rangeError.classList.toggle("d-none", valid);
            return valid;
        }

        function validateUnit() {
            const value = unit.value.trim();
            const valid = value.length > 0 && value.length <= 30;
            unit.setCustomValidity(
                    valid ? "" : "Enter a valid unit of measurement."
                    );
            return valid;
        }

        resultValue.addEventListener("input", validateResultValue);
        normalMin.addEventListener("input", validateReferenceRange);
        normalMax.addEventListener("input", validateReferenceRange);
        unit.addEventListener("input", validateUnit);

        form.addEventListener("submit", event => {
            const resultIsValid = validateResultValue();
            const rangeIsValid = validateReferenceRange();
            const unitIsValid = validateUnit();

            form.classList.add("was-validated");

            if (!form.checkValidity() || !resultIsValid || !rangeIsValid || !unitIsValid) {
                event.preventDefault();
                event.stopPropagation();
                return;
            }

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Processing...";
            }
        });
    })();
</script>

<%@ include file="includes/footer.jspf" %>