# AI Lab Alert Management System (AILabAlertSystem)

The **AI Lab Alert Management System (AILabAlertSystem)** is an intelligent healthcare application that automates the detection of abnormal laboratory test results, maintains audit records, and provides operational analytics through Microsoft Power BI.

It combines enterprise Java EE architecture, artificial intelligence, email notification services, audit logging, and business intelligence reporting to improve patient safety and clinical turnaround times.

---

## Technologies & Frameworks

- **Enterprise Java:** Jakarta EE, Servlets, EJB (`jakarta.ejb`, `jakarta.servlet`, `jakarta.mail`, `jakarta.persistence`)
- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap
- **Database:** MySQL Server 8, MySQL Workbench, MySQL Connector/J
- **Email & Data Services:** Jakarta Mail API, Java Activation Framework, Gson (Google JSON Library)
- **Logging:** Java Util Logging
- **Business Intelligence:** Microsoft Power BI, DAX, Power Query
- **IDE & Server:** Apache NetBeans 22, GlassFish Server 7, Java JDK 17+

---

## AI Engine

The **AI Engine** evaluates incoming laboratory test results and automatically identifies critical patient conditions.

### Core AI Functions
- Laboratory result evaluation
- Normal range comparison
- Severity classification
- Risk scoring & critical value detection
- Alert prioritisation & decision support recommendations

### Risk Score Classification

| Risk Score | Risk Category | Alert Level |
| :--- | :--- | :--- |
| **0 – 30** | Low Risk | Normal |
| **31 – 60** | Moderate Risk | Warning |
| **61 – 80** | High Risk | High |
| **81 – 100** | Critical Risk | Critical |

---

## Database Design

The system uses a relational **MySQL** database with the following core tables:

- `patients` — Stores patient information.
- `test_results` — Stores laboratory test results.
- `alerts` — Stores generated alerts.
- `pathologists` — Stores pathologist accounts.
- `users` — Stores system users.
- `email_logs` — Stores email notification history.
- `audit_logs` — Stores audit trail records.

---

## Power BI Dashboards

- **Page 1 – Executive Overview:** Critical Alerts, Open Alerts, Overdue Alerts, Average Review Time
- **Page 2 – Live Operations:** Active Alerts, Department Monitoring, Real-Time Alert Status
- **Page 3 – Alert Detail:** Drill-through Analysis, Patient-level Alert Tracking
- **Page 4 – Audit Activity:** User Activity Monitoring, Compliance Reporting, Security Tracking

---

## Project Structure

```text
AILabAlertSystem/
├── AILabAlertSystem-ear/
├── AILabAlertSystem-ejb/
├── AILabAlertSystem-war/
├── Database/
│   ├── create_database.sql
│   ├── create_tables.sql
│   ├── sample_data.sql
│   └── views.sql
├── UML/
│   ├── UseCase.drawio
│   ├── Activity.drawio
│   ├── Sequence.drawio
│   ├── Component.drawio
│   └── Deployment.drawio
├── PowerBI/
│   └── LabAlertDashboard.pbix
├── Documentation/
│   ├── UserManual.pdf
│   ├── InstallationGuide.pdf
│   └── Dissertation.pdf
└── README.md
```

---

## Installation Requirements

### Software Requirements
- Java JDK 17+
- Apache NetBeans 22
- GlassFish Server 7
- MySQL Server 8
- MySQL Workbench
- Power BI Desktop

---

## Author

**L.T. Maduana**  
**213213180**  
Advanced Diploma in Computer Science  
Tshwane University of Technology (TUT)  
2026

---

## Project Objectives

The primary objective of this project is to improve patient safety and laboratory workflow efficiency through intelligent alert generation, automated notifications, audit compliance, and business intelligence reporting.

---

## Repository

- **GitHub Repository:** [https://github.com/Maduana/lab-alert-system](https://github.com/Maduana/lab-alert-system)
