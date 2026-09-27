# 🏫 St. Joseph's School Management System (v3.5.0)

<p align="center">
  <a href="https://developer.android.com/tools/releases/platforms#7.0"><img src="https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 7.0+ (API 24)" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.1.10" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose M3" /></a>
  <a href="https://osmdroid.github.io/osmdroid/"><img src="https://img.shields.io/badge/Maps-OpenStreetMap%20(osmdroid)-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white" alt="OpenStreetMap osmdroid" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Institutional%20%2F%20Academic-blue?style=for-the-badge" alt="License" /></a>
</p>

---

## 📑 Table of Contents

- [Executive Overview](#-executive-overview)
- [What's New in v3.5.0](#-whats-new-in-v350)
- [Key Modules & Capabilities](#-key-modules--capabilities)
- [Technical Architecture](#-technical-architecture)
- [Data & Privacy](#-data--privacy)
- [Demo Accounts & Access](#-demo-accounts--access)
- [Getting Started & Installation](#-getting-started--installation)
- [Known Limitations & Roadmap](#-known-limitations--roadmap)
- [Institutional Information](#-institutional-information)
- [License](#-license)

---

## 🌟 Executive Overview

**St. Joseph's School Management System** is an offline-first, native Android institution operating platform built specifically for **St. Joseph Matriculation Higher Secondary School**. Designed as a senior SPL (Software Project Lab) submission and enterprise-ready institutional tool, the application delivers end-to-end administration across six primary roles: **Students, Teachers, Admins, Drivers, Parents, and System Developers**.

The platform provides unified management of:
- **Daily Attendance & Academics**: Fast QR/barcode check-in, real-time subject attendance tracking, timetable scheduling, and grade reporting.
- **Campus & Fleet Transit**: Free, open-source real-time bus tracking powered by **OpenStreetMap (`osmdroid`)** with zero Google Cloud API key requirements.
- **Institutional Communication**: Categorized announcements, urgent broadcast alerts with exact alarm scheduling, and Firebase Cloud Messaging (FCM) topic subscriptions.
- **Offline-First Synchronization**: On-device Room SQLite persistence paired with background `WorkManager` workers to ensure complete functionality in low-connectivity environments.

---

## 🚀 What's New in v3.5.0

> **Previous Release:** `v2.5.0` → **Current Release:** `v3.5.0`

1. **🚍 Live Fleet & Campus Navigation** — Added OpenStreetMap-powered (`osmdroid`) real-time bus tracking and campus building guide, replacing earlier static WebView approaches. Operates 100% free with no Google Cloud billing or API key requirement.
2. **🔔 Multi-Category Notification Center** — Categorized alert feeds (Academics, Transport, Examinations, Events, Emergency), Firebase Cloud Messaging integration, and exact-alarm delivery via `AlarmManager` for critical alerts.
3. **⚡ Offline-First Sync Layer** — Introduced `WorkManager`-based `SchoolBackgroundSyncWorker` to queue offline actions and sync automatically once connectivity returns.
4. **🪪 Smart Digital Badges** — Upgraded ID cards with scannable barcode/QR codes and alpha-channel crest rendering across light and dark themes.
5. **💻 Developer/Systems Role** — Added a dedicated systems role for live database inspection, sync telemetry, and cache controls.

---

## 📱 Key Modules & Capabilities

### 1. 🎓 Student & Academic Hub
- **Digital ID Card**: High-contrast, scannable QR/barcode credentials with student photo and roll identity.
- **Subject-Wise Attendance**: Visual progress rings tracking attendance percentages against institutional minimum thresholds.
- **Fee Ledger & Invoices**: Itemized tuition, laboratory, and library fee statements with payment tracking status.
- **Timetable & Homework**: Daily schedule viewer with subject tags, room locations, and assignment due dates.

### 2. 👩‍🏫 Faculty & Classroom Tools
- **Roster & Fast Roll Call**: One-tap student attendance marking (Present, Absent, Late, Excused) with bulk submission.
- **Circular Dispatch**: Instant announcement publishing categorized by audience (All School, Specific Classes, Faculty Only).
- **Exam Grading**: Gradebook records and performance assessment tracking.

### 3. 🚍 OpenStreetMap Bus Telemetry & Campus Guide
- **Zero-Billing Mapping**: Fully open-source `osmdroid` mapping layer avoiding proprietary API billing restrictions.
- **Dual-Mode Map Navigation**:
  - **Live Bus Radar**: Real-time vehicle location tracking, driver phone contacts, speed telemetry, and route stop progress.
  - **Campus Building Directory**: Interactive pins for Academic Blocks, Laboratories, Sports Turf, Auditorium, and Bus Fleet Bays.
- **Engine Fallback**: Runtime toggle supporting OpenStreetMap (Mapnik/OpenTopo), Vector Canvas, and Google Maps SDK.

### 4. 🔔 Multi-Category Notification Center
- **Categorized Feed**: Filter alerts by *Academics*, *Transport*, *Examinations*, *Events*, or *Emergency*.
- **Critical Alarm Delivery**: Integrated `AlarmManager` and Android notification channels for time-sensitive emergency alerts.
- **FCM Topics**: Firebase Cloud Messaging subscriber for automated institutional broadcast channels.

### 5. 🛠️ Systems & Developer Console
- **Telemetry & Cache Diagnostics**: Cache clearance, network simulator, and live sync engine trigger.
- **Database Entity Inspector**: Direct inspection of Room tables (Students, Teachers, Attendance, Bus Routes, Notifications).
- **State Reset Utilities**: Reset demo data to pristine initial states for lab demonstrations.

---

## 🏗️ Technical Architecture

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.1.10 | Modern idiomatic Kotlin with Coroutines & StateFlow |
| **UI Framework** | Jetpack Compose | Material Design 3 (M3 Expressive) dynamic theming |
| **Database** | Android Jetpack Room (SQLite) | Embedded, schema-versioned local-first relational database |
| **Mapping Engine** | OpenStreetMap (`osmdroid 6.1.20`) | Tile-cached, zero-API-key vector/raster GPS map renderer |
| **Background Sync** | AndroidX WorkManager 2.10 | Battery-efficient, network-constrained background sync worker |
| **Notifications** | FCM + Android NotificationManager | Priority channels, rich heads-up alerts, and exact alarms |
| **Architecture** | Unidirectional Data Flow (MVVM) | Clean Architecture with state hoisting and testable ViewModels |
| **Testing** | Robolectric & Roborazzi | JVM unit testing and Compose visual regression suites |

---

## 🔒 Data & Privacy

- **Local-first storage**: All student, staff, attendance, and fee data is stored in an on-device Room (SQLite) database. No data is transmitted off-device except push notification tokens/topics via Firebase Cloud Messaging.
- **No third-party data sharing**: Attendance, academic, and contact information stays within the school's own deployment; it is not sent to any external analytics or advertising service.
- **Credentials**: Demo builds use a shared prototype password for evaluation only (see notice below). A production rollout requires per-user hashed credentials before any real student or staff data is entered.
- **Access scope**: Each role only sees data relevant to its function (e.g., a teacher sees their assigned classes, not the full student body; a driver sees their assigned route, not academic records).

> *Note:* This section reflects the current local-first architecture. If a cloud backend or remote sync is connected in a future release, this section should be updated to describe what leaves the device and how it is secured in transit.

---

## 👥 Demo Accounts & Access

The application includes pre-configured roles for demonstration, lab grading, and functional evaluation:

| Role | Username / ID | Default Profile | Assigned Privileges |
| :--- | :--- | :--- | :--- |
| **Student** | `STU-2024-001` | Aarav Sharma (Class 10-A) | Personal grades, attendance, timetable, digital badge, bus route |
| **Teacher** | `TCH-101` | Priya Sundaram (Mathematics) | Roll-call submission, homework assignment, class announcements |
| **Admin** | `ADM-001` | Sister Maria (Headmistress) | Institutional broadcast, fee oversight, student directory, reporting |
| **Driver** | `DRV-001` | Murugan K. (Bus Route #4) | Live GPS ping transmission, student pickup checklist, emergency alert |
| **Developer** | `DEV-001` | System Engineer | Room database explorer, sync telemetry, cache and state controls |

> 🔑 **Standard Prototype Password:** `password123`
>
> ⚠️ **Production Notice:** This password is for local demo/evaluation only. Before deployment to real students and staff, all accounts must be migrated to hashed, individually-set credentials. Plaintext shared passwords must never be used with real student data.

---

## ⚙️ Getting Started & Installation

### System Prerequisites
- **Android OS**: Android 7.0 (API Level 24) or higher.
- **Target OS**: Android 16 (API Level 36 readiness).
- **IDE**: Android Studio Ladybug / Meerkat or later.
- **JDK**: Java 17 or Java 21 (Temurin / OpenJDK).

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/example/st-joseph-school-app.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync dependencies from Maven Central and Google repositories.
4. Select an Android device or emulator running **API 24+**.
5. Click **Run (`Shift + F10`)** or build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

*Note on Maps:* Because OpenStreetMap (`osmdroid`) is configured out of the box, you do not need to register a Google Maps API key or setup Google Cloud billing. The map will load and cache tiles automatically over any active internet connection.

---

## 🧭 Known Limitations & Roadmap

**Current limitations:**
- **No parent/guardian independent portal**: Parents currently view student status via the student portal; a standalone guardian view is planned.
- **Local-first synchronization**: All sync operations queue locally; there is no centralized remote web dashboard yet.
- **Demo credential sharing**: Default evaluation accounts utilize prototype shared credentials (see Production Notice above).
- **Offline fee record keeping**: Fee payments and receipts are tracked on-device but do not yet integrate with a live online payment gateway (UPI/Razorpay/Stripe).

**Planned for future releases:**
- Hashed, individually-provisioned credentials for all roles with biometric authentication.
- Dedicated Parent/Guardian portal with read-only access to their linked child's data.
- PDF report card and attendance certificate export with institutional digital crest.
- Combined academic calendar integrating timetables, homework due dates, and circular deadlines.
- Teacher-facing grade/marks entry directly updating the live student GPA and transcript display.

---

## 🏛️ Institutional Information

- **Institution**: St. Joseph Matriculation Higher Secondary School
- **Affiliation**: Tamil Nadu State Board of Matriculation
- **Project Type**: Senior Software Project Lab (SPL) & Institutional Digital OS
- **Support & Internal Documentation**: Contact the school systems administration office or faculty project coordinator. For security and privacy, direct staff contact details are maintained in the institution's internal administrative portal.

---

## 📄 License

This project is developed as an academic SPL submission and for deployment at St. Joseph Matriculation Higher Secondary School. All rights reserved unless a LICENSE file states otherwise. Contact the developer before reusing this codebase for another institution. See the [LICENSE](LICENSE) file for complete terms of use.
