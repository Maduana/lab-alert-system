<%--
    Document   : result-form
    Purpose    : Capture and validate laboratory results
    Author     : USER
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="includes/header.jspf" %>

<c:url var="dashboardUrl" value="/dashboard" />
<c:url var="submitResultUrl" value="/submit-result" />
<c:url var="patientLookupUrl" value="/patients-by-pathologist" />

<div class="row justify-content-center">

    <div class="col-xl-9 col-lg-10">

        <div class="card app-card">

            <%-- Page heading --%>
            <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">

                <div>
                    <h4 class="mb-1">
                        Capture Laboratory Result
                    </h4>

                    <small class="text-muted">
                        Select the assigned pathologist and patient,
                        then enter the laboratory result and reference range.
                    </small>
                </div>

                <a href="${dashboardUrl}" class="btn btn-outline-secondary btn-sm">
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

                <%-- No active pathologists available --%>
                <c:if test="${empty pathologists}">
                    <div class="alert alert-warning" role="alert">
                        No active pathologists are available.
                        At least one active pathologist must exist before
                        a laboratory result can be captured.
                    </div>
                </c:if>

                <%-- Result-capture form --%>
                <form id="resultForm"
                      action="${submitResultUrl}"
                      method="post"
                      class="needs-validation"
                      novalidate>

                    <%-- CSRF token --%>
                    <c:if test="${not empty requestScope._csrf}">
                        <input type="hidden"
                               name="_csrf"
                               value="${fn:escapeXml(requestScope._csrf)}" />
                    </c:if>

                    <div class="row g-3">

                        <%-- Pathologist --%>
                        <div class="col-md-6">

                            <label for="pathologistId" class="form-label">
                                Pathologist <span class="text-danger">*</span>
                            </label>

                            <select id="pathologistId"
                                    name="pathologistId"
                                    class="form-select"
                                    required
                                    <c:if test="${empty pathologists}">disabled</c:if>>

                                <option value="">
                                    Select pathologist
                                </option>

                                <c:forEach items="${pathologists}" var="pathologist">
                                    <option value="${pathologist.id}"
                                        ${formPathologistId eq pathologist.id or formPathologistId eq pathologist.id.toString() ? 'selected' : ''}>
                                        <c:out value="${pathologist.fullName}" />
                                        <c:if test="${not empty pathologist.employeeCode}">
                                            - <c:out value="${pathologist.employeeCode}" />
                                        </c:if>
                                        <c:if test="${not empty pathologist.specialty}">
                                            (<c:out value="${pathologist.specialty}" />)
                                        </c:if>
                                    </option>
                                </c:forEach>

                            </select>

                            <div class="invalid-feedback">
                                Select a valid active pathologist.
                            </div>

                            <div class="form-text">
                                The patient list will be filtered according to the selected pathologist.
                            </div>

                        </div>

                        <%-- Patient --%>
                        <div class="col-md-6">

                            <label for="patientId" class="form-label">
                                Patient <span class="text-danger">*</span>
                            </label>

                            <select id="patientId"
                                    name="patientId"
                                    class="form-select"
                                    data-selected-patient-id="${fn:escapeXml(formPatientId)}"
                                    required
                                    disabled>

                                <option value="">
                                    Select a pathologist first
                                </option>

                            </select>

                            <div class="invalid-feedback">
                                Select a patient assigned to the selected pathologist.
                            </div>

                            <div id="patientLookupMessage" class="form-text" aria-live="polite">
                                Select an active pathologist to load assigned patients.
                            </div>

                        </div>

                        <%-- Test name --%>
                        <div class="col-md-6">

                            <label for="testName" class="form-label">
                                Test Name <span class="text-danger">*</span>
                            </label>

                            <select id="testName" name="testName" class="form-select" required>

                                <option value="">
                                    Select laboratory test
                                </option>

                                <option value="Glucose" ${formTestName eq 'Glucose' ? 'selected' : ''}>
                                    Glucose
                                </option>

                                <option value="Potassium" ${formTestName eq 'Potassium' ? 'selected' : ''}>
                                    Potassium
                                </option>

                                <option value="Hemoglobin" ${formTestName eq 'Hemoglobin' ? 'selected' : ''}>
                                    Hemoglobin
                                </option>

                                <option value="WBC" ${formTestName eq 'WBC' ? 'selected' : ''}>
                                    White Blood Cell Count
                                </option>

                                <option value="Platelets" ${formTestName eq 'Platelets' ? 'selected' : ''}>
                                    Platelets
                                </option>

                            </select>

                            <div class="invalid-feedback">
                                Select a supported laboratory test.
                            </div>

                        </div>

                        <%-- Result value --%>
                        <div class="col-md-6">

                            <label for="resultValue" class="form-label">
                                Result Value <span class="text-danger">*</span>
                            </label>

                            <input id="resultValue"
                                   name="resultValue"
                                   type="number"
                                   class="form-control"
                                   value="${fn:escapeXml(formResultValue)}"
                                   min="0"
                                   max="1000000"
                                   step="0.01"
                                   placeholder="0.00"
                                   required />

                            <div class="invalid-feedback">
                                Enter a non-negative numerical result.
                            </div>

                        </div>

                        <%-- Unit --%>
                        <div class="col-md-4">

                            <label for="unit" class="form-label">
                                Unit <span class="text-danger">*</span>
                            </label>

                            <input id="unit"
                                   name="unit"
                                   type="text"
                                   class="form-control"
                                   value="${fn:escapeXml(formUnit)}"
                                   maxlength="30"
                                   placeholder="Example: mmol/L"
                                   required />

                            <div class="invalid-feedback">
                                Enter the unit of measurement.
                            </div>

                        </div>

                        <%-- Normal minimum --%>
                        <div class="col-md-4">

                            <label for="normalMin" class="form-label">
                                Normal Min <span class="text-danger">*</span>
                            </label>

                            <input id="normalMin"
                                   name="normalMin"
                                   type="number"
                                   class="form-control"
                                   value="${fn:escapeXml(formNormalMin)}"
                                   min="0"
                                   max="1000000"
                                   step="0.01"
                                   placeholder="0.00"
                                   required />

                            <div class="invalid-feedback">
                                Enter a valid non-negative minimum.
                            </div>

                        </div>

                        <%-- Normal maximum --%>
                        <div class="col-md-4">

                            <label for="normalMax" class="form-label">
                                Normal Max <span class="text-danger">*</span>
                            </label>

                            <input id="normalMax"
                                   name="normalMax"
                                   type="number"
                                   class="form-control"
                                   value="${fn:escapeXml(formNormalMax)}"
                                   min="0"
                                   max="1000000"
                                   step="0.01"
                                   placeholder="0.00"
                                   required />

                            <div class="invalid-feedback">
                                Enter a maximum greater than the minimum.
                            </div>

                        </div>

                    </div>

                    <%-- Patient lookup error --%>
                    <div id="patientLookupError" class="alert alert-danger mt-3 d-none" role="alert"></div>

                    <%-- Cross-field validation message --%>
                    <div id="rangeValidationError" class="alert alert-danger mt-3 d-none" role="alert">
                        Normal maximum must be greater than normal minimum.
                    </div>

                    <%-- Workflow information --%>
                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Evaluation process:</strong>
                        The system will verify that the patient belongs to
                        the selected pathologist, validate the submitted
                        values, retrieve the patient's previous result where
                        available, and process the information using the AI/Rule Engine.
                    </div>

                    <%-- Form actions --%>
                    <div class="mt-4 d-flex justify-content-end align-items-center flex-wrap gap-2">

                        <a href="${dashboardUrl}" class="btn btn-outline-secondary">
                            Cancel
                        </a>

                        <button id="submitButton"
                                type="submit"
                                class="btn btn-primary"
                                disabled>
                            Save Result &amp; Analyse
                        </button>

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
        const pathologistSelect = document.getElementById("pathologistId");
        const patientSelect = document.getElementById("patientId");
        const patientLookupMessage = document.getElementById("patientLookupMessage");
        const patientLookupError = document.getElementById("patientLookupError");

        // Element references
        const resultValue = document.getElementById("resultValue");
        const normalMin = document.getElementById("normalMin");
        const normalMax = document.getElementById("normalMax");
        const unit = document.getElementById("unit");
        const rangeError = document.getElementById("rangeValidationError");
        const submitButton = document.getElementById("submitButton");

        const patientLookupUrl = "${patientLookupUrl}";

        let selectedPatientId = patientSelect ? patientSelect.dataset.selectedPatientId || "" : "";
        let lookupSequence = 0;

        if (!form || !pathologistSelect || !patientSelect) {
            return;
        }

        function clearPatientOptions(message) {
            patientSelect.innerHTML = "";
            const option = document.createElement("option");
            option.value = "";
            option.textContent = message;
            patientSelect.appendChild(option);
            patientSelect.disabled = true;
            updateSubmitState();
        }

        function hidePatientLookupError() {
            if (patientLookupError) {
                patientLookupError.textContent = "";
                patientLookupError.classList.add("d-none");
            }
        }

        function showPatientLookupError(message) {
            if (patientLookupError) {
                patientLookupError.textContent = message || "The assigned patients could not be loaded.";
                patientLookupError.classList.remove("d-none");
            }
        }

        function updateSubmitState() {
            const pathologistSelected = pathologistSelect.value !== "";
            const patientSelected = !patientSelect.disabled && patientSelect.value !== "";
            const pathologistsAvailable = pathologistSelect.options.length > 1 && !pathologistSelect.disabled;

            submitButton.disabled = !pathologistsAvailable || !pathologistSelected || !patientSelected;
        }

        async function loadPatients(pathologistId, patientIdToRestore = "") {
            const requestSequence = ++lookupSequence;

            hidePatientLookupError();
            clearPatientOptions("Loading assigned patients...");

            patientLookupMessage.textContent = "Loading patients assigned to the selected pathologist.";

            try {
                const response = await fetch(
                    patientLookupUrl + "?pathologistId=" + encodeURIComponent(pathologistId),
                    {
                        method: "GET",
                        credentials: "same-origin",
                        cache: "no-store",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
                );

                const data = await response.json();

                if (requestSequence !== lookupSequence) {
                    return;
                }

                if (!response.ok || !data.success) {
                    throw new Error(data.message || "The assigned patients could not be loaded.");
                }

                patientSelect.innerHTML = "";
                const placeholder = document.createElement("option");
                placeholder.value = "";

                if (data.count === 0) {
                    placeholder.textContent = "No patients assigned to this pathologist";
                    patientSelect.appendChild(placeholder);
                    patientSelect.disabled = true;
                    patientLookupMessage.textContent = "The selected pathologist has no assigned patients.";
                    updateSubmitState();
                    return;
                }

                placeholder.textContent = "Select assigned patient";
                patientSelect.appendChild(placeholder);

                data.patients.forEach(patient => {
                    const option = document.createElement("option");
                    option.value = String(patient.id);
                    option.textContent = patient.displayName;

                    if (patientIdToRestore !== "" && String(patient.id) === String(patientIdToRestore)) {
                        option.selected = true;
                    }

                    patientSelect.appendChild(option);
                });

                patientSelect.disabled = false;
                patientLookupMessage.textContent = data.count === 1
                    ? "1 assigned patient loaded."
                    : data.count + " assigned patients loaded.";

                selectedPatientId = "";
                updateSubmitState();

            } catch (error) {
                if (requestSequence !== lookupSequence) {
                    return;
                }

                clearPatientOptions("Unable to load assigned patients");
                patientLookupMessage.textContent = "Patient lookup failed.";
                showPatientLookupError(error.message);
            }
        }

        function handlePathologistChange() {
            const pathologistId = pathologistSelect.value;
            selectedPatientId = "";

            if (pathologistId === "") {
                lookupSequence++;
                clearPatientOptions("Select a pathologist first");
                patientLookupMessage.textContent = "Select an active pathologist to load assigned patients.";
                hidePatientLookupError();
                return;
            }

            loadPatients(pathologistId);
        }

        function validateResultValue() {
            if (!resultValue || resultValue.value === "") {
                if (resultValue) resultValue.setCustomValidity("");
                return true;
            }

            const value = Number(resultValue.value);
            const valid = Number.isFinite(value) && value >= 0 && value <= 1000000;

            resultValue.setCustomValidity(valid ? "" : "Result value must be a non-negative number.");
            return valid;
        }

        function validateReferenceRange() {
            if (!normalMin || !normalMax || normalMin.value === "" || normalMax.value === "") {
                if (normalMin) normalMin.setCustomValidity("");
                if (normalMax) normalMax.setCustomValidity("");
                if (rangeError) rangeError.classList.add("d-none");
                return true;
            }

            const minimum = Number(normalMin.value);
            const maximum = Number(normalMax.value);
            const valid = Number.isFinite(minimum) && Number.isFinite(maximum) && minimum >= 0 && maximum >= 0 && minimum < maximum;

            normalMin.setCustomValidity(valid ? "" : "Normal minimum must be lower than normal maximum.");
            normalMax.setCustomValidity(valid ? "" : "Normal maximum must be greater than normal minimum.");

            if (rangeError) {
                rangeError.classList.toggle("d-none", valid);
            }

            return valid;
        }

        function validateUnit() {
            if (!unit) return true;

            const value = unit.value.trim();
            const valid = value.length > 0 && value.length <= 30;

            unit.setCustomValidity(valid ? "" : "Enter a valid unit of measurement.");
            return valid;
        }

        function validateSelections() {
            const pathologistValid = pathologistSelect.value !== "";
            const patientValid = !patientSelect.disabled && patientSelect.value !== "";

            pathologistSelect.setCustomValidity(pathologistValid ? "" : "Select an active pathologist.");
            patientSelect.setCustomValidity(patientValid ? "" : "Select a patient assigned to the selected pathologist.");

            return pathologistValid && patientValid;
        }

        pathologistSelect.addEventListener("change", handlePathologistChange);
        patientSelect.addEventListener("change", updateSubmitState);

        if (resultValue) resultValue.addEventListener("input", validateResultValue);
        if (normalMin) normalMin.addEventListener("input", validateReferenceRange);
        if (normalMax) normalMax.addEventListener("input", validateReferenceRange);
        if (unit) unit.addEventListener("input", validateUnit);

        form.addEventListener("submit", event => {
            const selectionsAreValid = validateSelections();
            const resultIsValid = validateResultValue();
            const rangeIsValid = validateReferenceRange();
            const unitIsValid = validateUnit();

            form.classList.add("was-validated");

            if (!form.checkValidity() || !selectionsAreValid || !resultIsValid || !rangeIsValid || !unitIsValid) {
                event.preventDefault();
                event.stopPropagation();
                return;
            }

            submitButton.disabled = true;
            submitButton.textContent = "Processing...";
        });

        /*
         * Restore the selected pathologist and patient after a
         * server-side validation error.
         */
        if (pathologistSelect.value !== "") {
            loadPatients(pathologistSelect.value, selectedPatientId);
        } else {
            clearPatientOptions("Select a pathologist first");
            patientLookupMessage.textContent = "Select an active pathologist to load assigned patients.";
        }
    })();
</script>

<%@ include file="includes/footer.jspf" %>