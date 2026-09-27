# 🏫 St. Joseph's School Management System (v3.5.0 Production)

<p align="center">
  <a href="https://developer.android.com/about/versions/15"><img src="https://img.shields.io/badge/Platform-Android%2015%20%7C%2016-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://osmdroid.github.io/osmdroid/"><img src="https://img.shields.io/badge/Maps-OpenStreetMap%20(osmdroid)-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white" alt="OpenStreetMap" /></a>
</p>

---

## 🌟 Executive Overview

**St. Joseph's School Management System** is a production-grade, offline-first institutional OS designed for **St. Joseph Matriculation Higher Secondary School**. Powered by modern Jetpack Compose (Material 3 Expressive), an offline-first Room database, real-time OpenStreetMap telemetry, and Firebase Cloud Messaging, this application unifies daily workflows for students, teachers, administrators, school bus drivers, and developers into a cohesive mobile hub.

Key pillars of v3.5.0:
- **Zero-Billing Campus & Transit Maps**: 100% free OpenStreetMap (`osmdroid`) engine with multi-stop bus telemetry and interactive campus building directory.
- **Unified Academic Administration**: Fast roll call, timetable schedules, homework dispatch, exam report cards, and digital student ID badges with QR/barcodes.
- **Enterprise Offline-First Architecture**: Continuous Room SQLite caching with background `WorkManager` synchronization when network connectivity restores.
- **Multi-Category Push Communications**: Categorized notification channels (Academics, Transport, Examinations, Events, Emergency) with exact-alarm delivery.

---

## 📱 Key Modules & Capabilities

### 1. 🎓 Student & Academic Hub
- **Digital Student Badge**: Instant QR and barcode credential generation with crest styling and student identity details.
- **Attendance Visualizer**: Live percentage tracking against mandatory institutional attendance thresholds.
- **Fee Management**: Tuition, laboratory, and library fee ledger with invoice tracking.
- **Timetable & Homework**: Daily schedule breakdown by period, classroom location, and assignment status.

### 2. 👩‍🏫 Faculty & Classroom Tools
- **Roster & Fast Roll Call**: One-tap student attendance marking (Present, Absent, Late, Excused) with bulk commit.
- **Circular Dispatcher**: Instant announcements published school-wide or class-specific.
- **Exam & Marks Entry**: Fast grade tracking and student report generation.

### 3. 🚍 OpenStreetMap Bus Telemetry & Campus Guide
- **OpenStreetMap (`osmdroid`) Engine**: Completely free mapping with vector/raster Mapnik tile streaming and offline caching. No Google Cloud billing or API key required.
- **Live Bus Radar**: Real-time vehicle location tracking, driver contacts, speed telemetry, and route stop progress.
- **Campus Building Guide**: Interactive directory mapping Academic Blocks, Laboratories, Sports Turf, Auditorium, and Bus Fleet Bays.
- **Engine Switcher**: Seamless toggle between OpenStreetMap (Mapnik/OpenTopo), Vector Canvas, and Google Maps SDK.

### 4. 🔔 Multi-Category Notification Center
- **Categorized Feed**: Filter alerts across *Academics*, *Transport*, *Examinations*, *Events*, and *Emergency*.
- **Critical Alert Dispatch**: Emergency notifications delivered via `AlarmManager` and Android heads-up channels.
- **FCM Push Manager**: Built-in Firebase Cloud Messaging client with topic subscription controls.

### 5. 🛠️ Systems & Developer Console
- **Telemetry & Cache Diagnostics**: Cache purge, network state simulation, and manual sync triggering.
- **Database Entity Inspector**: Live inspection of Room database tables and schema status.
- **State Reset Utilities**: Reset demo data to initial factory states for testing.

---

## 🏗️ Technical Architecture

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.1.10 | Coroutines, Flow, StateFlow, Serialization |
| **UI Framework** | Jetpack Compose | Material Design 3 (M3 Expressive), Dynamic Theming |
| **Database** | Android Jetpack Room | Local-first SQLite database with DAO abstractions |
| **Mapping Engine** | OpenStreetMap (`osmdroid 6.1.20`) | Zero-API-key tile streaming with vector canvas fallback |
| **Background Sync** | AndroidX WorkManager 2.10 | Battery-efficient, network-constrained background sync |
| **Notifications** | FCM + Android NotificationManager | Priority channels, rich heads-up alerts, exact alarms |
| **Architecture** | Unidirectional Data Flow (MVVM) | Clean Architecture with state hoisting and testable ViewModels |

---

## 👥 Demo Accounts & Access

The application includes pre-configured demo profiles for testing and evaluation across all supported roles:

| Role | Username / ID | Default Profile | Assigned Privileges |
| :--- | :--- | :--- | :--- |
| **Student** | `STU-2024-001` | Aarav Sharma (Class 10-A) | Personal grades, attendance, timetable, digital badge, bus route |
| **Teacher** | `TCH-101` | Priya Sundaram (Mathematics) | Roll-call submission, homework assignment, class announcements |
| **Admin** | `ADM-001` | Sister Maria (Headmistress) | Institutional broadcast, fee oversight, student directory, reporting |
| **Driver** | `DRV-001` | Murugan K. (Bus Route #4) | Live GPS ping transmission, student pickup checklist, emergency alert |
| **Developer** | `DEV-001` | System Engineer | Room database explorer, sync telemetry, cache and state controls |

> 🔑 **Standard Prototype Password:** `password123`

---

## ⚙️ Getting Started & Installation

### System Prerequisites
- **Android OS**: Android 7.0 (API Level 24) or higher.
- **Target OS**: Android 16 (API Level 36 readiness).
- **IDE**: Android Studio Ladybug / Meerkat or later.
- **JDK**: Java 17 or Java 21.

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

*Note on Maps:* OpenStreetMap (`osmdroid`) is configured out of the box — no Google Maps API key or billing account is required. The map will stream and cache tiles automatically.

---

## 🏛️ Institutional Information

- **Institution**: St. Joseph Matriculation Higher Secondary School
- **Affiliation**: Tamil Nadu State Board of Matriculation
- **Address**: 12, Church Road, Cantonment, Tiruchirappalli, Tamil Nadu 620001
- **Phone**: +91 431 241 0422
- **Email**: info@stjosephtrichy.edu.in
- **Website**: https://stjosephtrichy.edu.in
