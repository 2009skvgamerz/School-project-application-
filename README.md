# 🏛️ St. Joseph Matriculation Hr. Sec. School — Android Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Database-Android%20Room%20SQLite-FFCA28?logo=sqlite&logoColor=black)](https://developer.android.com/training/data-storage/room)
[![FCM](https://img.shields.io/badge/Push-Firebase%20Cloud%20Messaging-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Release](https://img.shields.io/badge/Release-v3.0.0-blue.svg)](https://github.com/2009skvgamerz/School-project-application-/releases/tag/v3.0.0)

A production-grade, native Android **School ERP & Student Information System** built with **Jetpack Compose (Material Design 3)**, **Kotlin Coroutines & Flow**, **Android Room (SQLite)**, and **Firebase Cloud Messaging (FCM)**. 

Engineered for **St. Joseph Matriculation Higher Secondary School** (*Hosur, Tamil Nadu*), it unifies daily institutional operations — real-time attendance roll calls, interactive timetables, homework distribution, digital ID cards, official circulars, fee tracking, campus facility duty rosters, and live GPS school bus transit — into an offline-first, role-tailored mobile experience.

---

## 📑 Table of Contents

- [🚀 What's New in Release v3.0.0](#-whats-new-in-release-v300)
- [🏛️ Role-Based Portals & Modules](#-role-based-portals--modules)
- [🏗️ Technical Architecture](#️-technical-architecture)
- [📂 Source Code Directory Structure](#-source-code-directory-structure)
- [🛠️ Getting Started & Build Instructions](#️-getting-started--build-instructions)
- [👥 Demo Institutional Accounts](#-demo-institutional-accounts)
- [📄 Institution Details & Attribution](#-institution-details--attribution)

---

## 🚀 What's New in Release v3.0.0

> **Release:** `v3.0.0` | **Version Code:** `30` | **Target SDK:** `Android 15 / 16 (API 36)`

### 1. 🔔 Autonomous Background Notification & Alarm Engine
- **Continuous Background Alerting**: Integrated Android system `AlarmManager` (`setExactAndAllowWhileIdle` / `setAndAllowWhileIdle`) and `SchoolAlarmReceiver` to dispatch realistic school notifications even when the application is completely killed or the device is idling in Doze mode.
- **Dynamic Event Rotation**: Dispatches timely contextual updates including live bus approach ETAs, upcoming period reminders, homework assignments, morning attendance confirmations, exam circulars, and sports day trials.
- **Reboot Persistence**: Registered `BootReceiver` (`RECEIVE_BOOT_COMPLETED`) and `SchoolBackgroundSyncWorker` to automatically re-arm background schedulers whenever the device restarts.

### 2. ☁️ Firebase Cloud Messaging (FCM) & Google Services
- Native `SchoolFirebaseMessagingService` with automatic device token generation and multi-topic subscriptions (`#all_school`, `#announcements`, `#events`, `#exams`, `#sports`).
- Standardized notification payload mapping with `SystemNotificationHelper` for instant high-priority heads-up banners with deep-link navigation directly to relevant academic screens.

### 3. 🎨 Google Material You (M3) Floating Top Search Bar & Design Refresh
- Transformed the navigation header into a floating search pill (`28.dp` rounded container) with built-in search filter, quick role switcher, notification badge counter, and Google Account profile avatar ring.
- Refined layout padding with `statusBarsPadding()` and `WindowInsets` handling to prevent overlaps with system status bars, display cutouts, and camera notches.
- Modernized all UI surfaces with Material Design 3 tonal containers (`surfaceContainerLow`), high-contrast typography, and smooth transitions.

### 4. 🌐 Real-Time Cloud Sync Indicator & Telemetry
- Interactive App Bar widget dynamically reflecting real-time connection states:
  - 🟢 **Synced**: Active connection to cloud services.
  - 🔵 **Syncing**: Active background synchronization in progress.
  - 🟡 **Offline Cache**: Local Room SQLite database active.
  - 🔴 **Sync Alert**: Connection interruption notice.
- Comprehensive **Cloud Sync Telemetry Dialog** displaying live cache metrics, last synced timestamp, and manual force-sync trigger.

### 5. ⚡ Sub-Second Instant Launch & Session Engine
- Instant session restoration with `SessionPreferences` and Room SQLite cache, eliminating cold-boot delays and opening directly into the active dashboard.

---

## 🏛️ Role-Based Portals & Modules

### 👨‍🎓 1. Student Portal
- **Dashboard Overview**: Cumulative GPA, attendance percentage, today's schedule, pending tasks, and recent announcements.
- **Attendance Insights**: Interactive circular completion meter with session breakdown (*Full Day, Half Day, On Duty, Absent*) and subject-wise attendance bars.
- **Academic Timetable**: Real-time period-by-period class schedule with subject, room number, and faculty assignment.
- **Homework & Submissions**: Assignment details, due dates, submission status tags, and teacher feedback.
- **Digital Student ID Card**: High-resolution digital badge featuring student photo, scannable barcode/QR, blood group, emergency contact, and house affiliation (*St. Patrick, St. Joseph, St. Mary, St. Francis*).

### 👩‍🏫 2. Teacher & Faculty Portal
- **Homeroom Roll Call**: Fast digital attendance register with one-tap batch "All Full Day" marking and individual FD/HD/OD/AB status toggles.
- **Class Rosters**: Section-by-section student directory with real-time attendance statistics and contact records.
- **Assignment Publisher**: Distribute homework, set deadlines, and monitor student submission progress.
- **Curriculum & Invigilation**: Track syllabus completion and view assigned exam invigilation schedules.

### 🛠️ 3. Operations & Campus Staff Portal
- **Campus Duty Rosters**: Assigned shift management covering gate supervision, lab maintenance, cafeteria oversight, and ground setup.
- **Incident & Facility Reporting**: Log maintenance tickets and facility requirements with priority flags.
- **Institutional Directory**: Search internal faculty phone extensions and department contacts.

### 🚗 4. Transport & Driver Dashboard
- **Interactive WebView GPS Map**: Real-time route coordinate visualization with GPS-lock and route bounds fitting.
- **Passenger Boarding Checklist**: Mark student pickups and drop-offs at designated route stops.
- **Turn-by-Turn Navigation**: One-tap shortcut launching Google Maps Navigation for scheduled bus stops.

### 👑 5. Principal & Administration Portal
- **Governance Dashboard**: Institutional attendance averages, total student/staff strength, fee collection summaries, and academic grade distributions.
- **School-Wide Circulars**: Compose and broadcast official circulars to specific cohorts or the entire school.
- **Database Administration**: Inspect local Room SQLite records and trigger full seed data resets.

---

## 🏗️ Technical Architecture

```
┌─────────────────────────────────────────────────────────────┐
│             Jetpack Compose UI (Material 3)                 │
│   (MainSchoolApp • Floating Search Pill • Role Dashboards)  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 ViewModel Layer & StateFlow                 │
│      (SchoolViewModel • AuthenticationViewModel)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository & Business Logic                │
│       (SchoolRepository • BackgroundSyncManager)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
             ┌─────────────────┴─────────────────┐
             ▼                                   ▼
┌─────────────────────────────┐     ┌─────────────────────────────┐
│     Android Room SQLite     │     │   AlarmManager & Firebase   │
│  (DAOs • Entities • KSP)    │     │(SchoolAlarmReceiver • FCM)  │
└─────────────────────────────┘     └─────────────────────────────┘
```

| Component | Technology | Description |
|---|---|---|
| **Language** | Kotlin 2.0 | Modern, type-safe development with Coroutines & Flow |
| **UI Framework** | Jetpack Compose (M3) | Declarative UI, dynamic color theming, adaptive scaffolding |
| **State Management** | StateFlow & SharedFlow | Lifecycle-aware, reactive UI state streams |
| **Local Persistence** | Android Room Database | Type-safe SQLite database with Kotlin Symbol Processing (KSP) |
| **Background Scheduling** | AlarmManager & WorkManager | Continuous autonomous background alerts & sync workers |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Cloud-to-device push messaging & heads-up system alerts |
| **Navigation** | Navigation Compose | Type-safe route backstack management with deep linking |

---

## 📂 Source Code Directory Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Single-activity container with edge-to-edge layout
├── SchoolApplication.kt                 # Application startup, notification channels, background initialization
├── model/
│   ├── SchoolModels.kt                  # Domain entities, enums (AttendanceStatus, UserRole, NotificationType)
│   └── ThemeMode.kt                     # Theme mode configuration (System, Light, Dark)
├── data/
│   ├── SchoolRepository.kt              # Central data orchestrator & business repository
│   └── local/
│       ├── AppDatabase.kt               # Room database definition, migrations & initial seed data
│       ├── Converters.kt                # Room TypeConverters for custom types and enums
│       ├── dao/
│       │   ├── AttendanceDao.kt         # Daily attendance queries & batch transactions
│       │   ├── StudentDao.kt            # Student roster & profile data access
│       │   ├── TeacherDao.kt            # Faculty directory data access
│       │   └── NotificationDao.kt       # Persistent notification record queries
│       └── entity/
│           ├── AttendanceEntity.kt      # Attendance SQLite table
│           ├── StudentEntity.kt         # Student SQLite table
│           ├── TeacherEntity.kt         # Faculty SQLite table
│           └── NotificationEntity.kt    # System notification archive table
├── viewmodel/
│   ├── AuthenticationViewModel.kt       # Credential authentication & session management
│   └── SchoolViewModel.kt               # School operations, attendance, timetable & alert state
├── service/
│   └── SchoolFirebaseMessagingService.kt # FCM background push notification handler
├── receiver/
│   ├── BootReceiver.kt                  # Reboots recovery receiver
│   └── SchoolAlarmReceiver.kt           # Continuous autonomous background alert receiver
├── worker/
│   └── SchoolBackgroundSyncWorker.kt    # Periodic WorkManager background network sync
├── util/
│   ├── SchoolBackgroundScheduler.kt     # System AlarmManager scheduler
│   ├── BackgroundSyncManager.kt         # WorkManager scheduler & connectivity observer
│   ├── SystemNotificationHelper.kt      # NotificationManager heads-up builder & channel configuration
│   └── SessionPreferences.kt            # Persistent user session preferences
└── ui/
    ├── MainSchoolApp.kt                 # Top-level scaffold, navigation bar, dialogs
    ├── auth/
    │   └── LoginScreen.kt               # Institutional login screen with role quick-switchers
    ├── screens/
    │   ├── AttendanceScreen.kt          # Student attendance report & roll-call interface
    │   ├── TeacherAttendanceScreen.kt   # Dedicated homeroom teacher register
    │   ├── ClassesScreen.kt             # Academic class rosters & section schedules
    │   ├── HomeworkScreen.kt            # Homework assignments & submission tracker
    │   ├── TimetableScreen.kt           # Weekly period-by-period class timetable
    │   ├── NoticesScreen.kt             # Official circulars & urgent bulletins
    │   ├── FeesScreen.kt                # Tuition fees breakdown & digital receipts
    │   ├── DutiesScreen.kt              # Operations staff duty rosters
    │   ├── ManagementScreen.kt          # Institutional records & staff directory
    │   ├── SettingsScreen.kt            # Theme toggles, notification channel settings, database reset
    │   └── ProfileScreen.kt             # Digital ID card & role switcher
    ├── dashboard/                       # Role-tailored home dashboards (Student, Teacher, Admin, Staff, Driver)
    ├── components/                      # Reusable M3 cards, headers, stat badges, modal sheets
    └── theme/                           # Color.kt, Type.kt, Theme.kt
```

---

## 🛠️ Getting Started & Build Instructions

### Option A — Install the Pre-Built APK
Download the compiled release APK directly from the official [**v3.0.0 GitHub Release**](https://github.com/2009skvgamerz/School-project-application-/releases/tag/v3.0.0), install on any Android device running Android 7.0+ (API 24+), and sign in using a [demo account](#-demo-institutional-accounts).

### Option B — Build from Source
1. **Clone the repository**:
   ```bash
   git clone https://github.com/2009skvgamerz/School-project-application-.git
   cd School-project-application-
   ```
2. **Open in Android Studio**: Recommended version: Android Studio Ladybug / Meerkat / Koala.
3. **Configure Environment**:
   - Copy `.env.example` to `.env` if custom API configurations are required.
   - Verify `google-services.json` is present in the `/app` directory.
4. **Build & Run**:
   ```bash
   gradle assembleDebug
   ```
   Deploy directly to an emulator or connected physical Android device.

---

## 👥 Demo Institutional Accounts

> 💡 **Quick Login:** Tap any role avatar on the login screen or use the profile switcher to instantly preview that role's interface.

**Default Prototype Password:** `password123`

| Role | Username | Email | Name | Designation / Assignment |
|---|---|---|---|---|
| 👨‍🎓 **Student** | `student01` | `alex.j@stjosephs.edu` | Alex Johnson | Class 10-A, Roll #1, St. Patrick House |
| 👩‍🏫 **Teacher** | `teacher01` | `s.jenkins@stjosephs.edu` | Prof. Sarah Jenkins | Class Teacher of 10-A (Physics & Lab) |
| 🛠️ **Staff (Operations)** | `staff01` | `t.wright@stjosephs.edu` | Mr. Thomas Wright | Senior Operations Supervisor |
| 🚗 **Staff (Driver)** | `driver01` | `m.ross@stjosephs.edu` | Mr. Marcus Ross | Transport Driver (Route #05) |
| 👑 **Admin** | `admin01` | `principal@stjosephs.edu` | Dr. Arthur Pendelton | Principal & Head of Institution |

---

## 📄 Institution Details & Attribution

**St. Joseph Matriculation Higher Secondary School**  
📍 *SIPCOT, Gandhi Nagar Rd, Mookondapalli, Hosur, Tamil Nadu 635126*  
📞 Phone: +91 4344 276544 | ✉️ Email: info@stjosephshosur.edu.in  
✨ **Motto:** *"Shine and Let Shine"*

Developed with **Google AI Studio**. All rights reserved.
