# 🏛️ St. Joseph's Higher Secondary School — Android Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Database-Android%20Room%20SQLite-FFCA28?logo=sqlite&logoColor=black)](https://developer.android.com/training/data-storage/room)
[![Release](https://img.shields.io/badge/Release-v2.5.0-blue.svg)](https://github.com/2009skvgamerz/School-project-application-/releases/tag/v2.5.0)

A native Android **School ERP & Student Information System** built with **Jetpack Compose (Material Design 3)**, **Kotlin Coroutines & Flow**, and **Android Room**. Designed for St. Joseph's Higher Secondary School, it unifies daily academic operations — digital roll-calls, timetables, homework, circulars, fee tracking, transport, and campus duty management — into a single role-tailored mobile app.

---

## Table of Contents

- [What's New](#whats-new-in-release-v250)
- [Role-Based Modules](#role-based-modules--feature-breakdown)
- [Architecture](#architecture--technical-stack)
- [Source Code Structure](#source-code-structure)
- [Getting Started](#getting-started)
- [Demo Accounts](#demo-institutional-accounts)
- [License](#license--attribution)

---

## 🚀 What's New in Release v2.5.0

> **Previous Release:** `v2.4.0` → **Current Release:** `v2.5.0`
> *(Top-Level UserProfileHeader Component, Full Dashboard Layout Optimization & Header Streamlining)*

1. **👤 Universal `UserProfileHeader` Component**
   Added across all role-based dashboards (Student, Teacher, Operations Staff, Transport Driver, Admin, Developer). Displays full name, role-specific avatar badge, live status indicator (*"Active Session"*, *"PILOT ACTIVE"*, *"GOD MODE ACTIVE"*), and current session pill (*"Academic Session 2026–2027"*), plus contextual details (Class/Section & Roll #, Department & Employee ID, Vehicle Route & License, or Office Location).

2. **✨ Dashboard Layout Optimization & Header De-duplication**
   Removed redundant duplicate header cards on Driver and Developer dashboards. Refined the Driver cockpit into a focused *"Operating Vehicle & Route"* card (active bus info, route details, quick bus switcher, in-app map shortcut). Fixed duplicate `"Bus #Bus #"` subtitle formatting bug.

3. **📱 Top Bar Compact Layout**
   Compacted the role-switcher icon in `BaseDashboardScaffold` to prevent truncation of the *"St. Joseph's"* title, network badge, and tab titles on smaller viewports.

4. **🏷️ Metadata & String Standardization**
   Aligned `metadata.json` and `strings.xml` to officially declare v2.5.0.

---

## 🏛️ Role-Based Modules & Feature Breakdown

### 👨‍🎓 Student Portal
- **Dashboard Overview** — GPA, attendance percentage, today's schedule, pending tasks
- **Attendance Insights** — interactive circular meter, FD/HD/OD/AB session-weight breakdown, subject-wise progress bars
- **Academic Timetable** — real-time period-by-period schedule with room numbers and teacher names
- **Homework & Submissions** — assignment details, due dates, submission status, teacher notes
- **Digital ID Card** — photo, barcode/QR badge, blood group, emergency contact, house affiliation

### 👩‍🏫 Teacher & Faculty Portal
- **Homeroom Roll Call** — one-tap register with batch "All Full Day" and individual FD/HD/OD/AB entry
- **Remarks & Notes** — medical leave, extracurricular participation, administrative notes
- **Class Rosters** — filter and inspect rosters across assigned classes and sections
- **Assignment Publisher** — distribute homework, set deadlines, monitor submissions
- **Syllabus & Duties** — track curriculum progress and view invigilation duty assignments

### 🛠️ Operations & Campus Staff Portal
- **Sub-Role Support** — Operational Staff or Transport Route Management (Driver)
- **Campus Duty Rosters** — gate supervision, lab maintenance, cafeteria oversight, sports ground setup
- **Incident & Facility Reporting** — log maintenance requests with priority flags
- **Institutional Directory** — search faculty and department extensions

### 🚗 Transport & Driver Dashboard
- **Interactive WebView Map** — real-time coordinates, GPS-lock, route bounds fitting
- **Passenger Checklist** — check students off at designated stop times
- **Google Maps Navigation** — one-tap redirect to turn-by-turn navigation for a stop

### 👑 Principal & Administration Portal
- **Governance Dashboard** — institutional attendance averages, staff strength, fee collection summaries, grade distribution
- **School-Wide Circulars** — publish notices and announcements to specific cohorts
- **Database Administration** — inspect demo SQLite seed data, manage global academic configuration

---

## 🏗️ Architecture & Technical Stack

```
┌─────────────────────────────┐
│     Jetpack Compose UI      │
│   (Material 3 Components)   │
└──────────────┬───────────────┘
               │
               ▼
┌─────────────────────────────┐
│  ViewModels & UI StateFlow  │
│ (SchoolViewModel / AuthVM)  │
└──────────────┬───────────────┘
               │
               ▼
┌─────────────────────────────┐
│      SchoolRepository       │
│(State orchestration / Cache)│
└──────────────┬───────────────┘
               │
               ▼
┌─────────────────────────────┐
│      Android Room DB        │
│  (SQLite with KSP & DAOs)   │
└─────────────────────────────┘
```

| Component | Technology | Description |
|---|---|---|
| **Language** | Kotlin 2.0 | Type-safe, coroutine-powered development |
| **UI System** | Jetpack Compose (M3) | Declarative UI, dynamic color, adaptive scaffolding |
| **State Management** | StateFlow & SharedFlow | Reactive, lifecycle-aware architecture |
| **Local Persistence** | Android Room | Type-safe SQLite persistence via KSP compiler |
| **Dependency Injection** | Constructor Injection | Modular, testable architecture |
| **Testing** | Robolectric & Roborazzi | JVM component testing and visual regression suites |

---

## 📂 Source Code Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                    # Single-activity container, edge-to-edge support
├── model/
│   ├── SchoolModels.kt                # Domain entities, enums (AttendanceStatus, UserRole, etc.)
│   └── ThemeMode.kt                   # Theme configuration (System, Light, Dark)
├── data/
│   ├── SchoolRepository.kt            # Central data provider & business logic
│   └── local/
│       ├── AppDatabase.kt             # Room database definition, migrations & seeding
│       ├── Converters.kt              # Room TypeConverters for enums & lists
│       ├── dao/
│       │   ├── AttendanceDao.kt       # Daily attendance queries & batch transactions
│       │   ├── StudentDao.kt          # Student roster & profile data access
│       │   └── TeacherDao.kt          # Teacher faculty data access
│       └── entity/
│           ├── AttendanceEntity.kt    # Attendance SQLite table entity
│           ├── StudentEntity.kt       # Student SQLite table entity
│           └── TeacherEntity.kt       # Teacher SQLite table entity
├── viewmodel/
│   ├── AuthenticationViewModel.kt     # Room credential validation & session state
│   └── SchoolViewModel.kt             # School operations, attendance & timetable state
└── ui/
    ├── MainSchoolApp.kt               # Top-level scaffold, navigation bar, dialogs
    ├── auth/
    │   └── LoginScreen.kt             # Institutional login with unified staff panel
    ├── screens/
    │   ├── AttendanceScreen.kt        # Student report & teacher roll-call register
    │   ├── TeacherAttendanceScreen.kt # Dedicated homeroom teacher register
    │   ├── ClassesScreen.kt           # Academic class rosters & section timetables
    │   ├── HomeworkScreen.kt          # Homework tasks & submission flow
    │   ├── TimetableScreen.kt         # Weekly class schedule
    │   ├── NoticesScreen.kt           # Official circulars & urgent bulletins
    │   ├── FeesScreen.kt              # Term fees breakdown & digital receipts
    │   ├── DutiesScreen.kt            # Operations staff task rosters
    │   ├── ManagementScreen.kt        # Institutional records & directory
    │   ├── SettingsScreen.kt          # Dark mode toggles, notifications, DB reset
    │   └── ProfileScreen.kt           # Digital ID badge & role switcher
    ├── dashboard/                     # Role-specific home dashboards
    ├── components/                    # Reusable M3 cards, headers, stat badges, dialogs
    └── theme/                         # Theme.kt, Color.kt, Type.kt
```

---

## Getting Started

### Option A — Download the APK
Grab the latest build directly from the [**v2.5.0 Release**](https://github.com/2009skvgamerz/School-project-application-/releases/tag/v2.5.0), install it on your device, and log in with a [demo account](#demo-institutional-accounts) below.

### Option B — Build from source
1. Clone the repository
   ```bash
   git clone https://github.com/2009skvgamerz/School-project-application-.git
   ```
2. Open the project in **Android Studio** (Koala or newer recommended)
3. Copy `.env.example` to `.env` and fill in any required values
4. Let Gradle sync, then run on an emulator or physical device (min SDK per `build.gradle.kts`)
5. Log in with one of the [demo accounts](#demo-institutional-accounts) below

---

## 👥 Demo Institutional Accounts

> 💡 Tap any quick-switch avatar on the Login Screen, or select a role from the **Profile Tab**, to instantly preview that stakeholder's dashboard.

**Default prototype password:** `password123`

| Role | Username | Email | Name | Designation / Assignment |
|---|---|---|---|---|
| 👨‍🎓 Student | `student01` | `alex.j@stjosephs.edu` | Alex Johnson | Class 10-A, Roll #1, St. Patrick House |
| 👩‍🏫 Teacher | `teacher01` | `s.jenkins@stjosephs.edu` | Prof. Sarah Jenkins | Class Teacher of 10-A (Physics & Lab) |
| 🛠️ Staff (Operations) | `staff01` | `t.wright@stjosephs.edu` | Mr. Thomas Wright | Senior Operations Supervisor |
| 🚗 Staff (Driver) | `driver01` | `m.ross@stjosephs.edu` | Mr. Marcus Ross | Transport Driver (Route #05) |
| 👑 Admin | `admin01` | `principal@stjosephs.edu` | Dr. Arthur Pendelton | Principal & Head of Institution |

> ⚠️ These are seeded demo credentials for local/offline evaluation only — not intended for production use.

---

## 📄 License & Attribution

**St. Joseph's Higher Secondary School Management System**
*Motto: "Shine and Let Shine"*

Designed and engineered using Google AI Studio. All rights reserved.
