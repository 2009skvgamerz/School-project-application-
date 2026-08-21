# 🏛️ St. Joseph's Higher Secondary School — Android Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Database-Android%20Room%20SQLite-FFCA28.svg?logo=sqlite&logoColor=black)](https://developer.android.com/training/data-storage/room)
[![Release](https://img.shields.io/badge/Release-v1.1.0-blue.svg)](https://github.com)

A native Android School ERP and Student Information System built with **Jetpack Compose (Material Design 3)**, **Kotlin Coroutines & Flow**, and **Android Room Database**. Designed specifically for St. Joseph's Higher Secondary School, this platform unifies daily academic operations, digital homeroom roll-calls, student timetables, homework assignments, circulars, fee tracking, and campus duty management into an intuitive, role-tailored mobile experience.

---

## 🚀 What's New in Release v1.1.0

> **Previous Release**: `v1.0.0` (Initial Prototype & Core Role Dashboards)  
> **Current Release**: `v1.1.0` (Dark Mode Accessibility, Homeroom Authorization & Room DB Integration)

### 🌟 Key Highlights & Enhancements
1. **🌙 Full Dark Mode Contrast & Dynamic Theming**:
   - Replaced all static dark navy color references with dynamic Material 3 color tokens (`MaterialTheme.colorScheme.onSurface`, `primary`, `onSurfaceVariant`, `primaryContainer`, `surfaceContainer`).
   - High-contrast, WCAG-compliant legibility across all tabs in both Light and Dark themes.
2. **🛡️ Strict Homeroom Teacher Roll-Call Access Control**:
   - Enforced security authorization: **Only the designated Homeroom Class Teacher** (e.g., *Prof. Sarah Jenkins* for Class 10-A) can record or modify student attendance.
   - Non-homeroom faculty and student views automatically transition into a locked read-only register with clear authorization tooltips.
3. **📊 4-Tier Attendance Roll Call Model**:
   - 🟢 **Full-day (`FD`, 1.0 weight)** — Standard presence.
   - 🟡 **Half-day (`HD`, 0.5 weight)** — Morning/Afternoon session.
   - 🔵 **On-duty (`OD`, 1.0 weight)** — Authorized school representation (Sports tournaments, Olympiads, Science exhibitions).
   - 🔴 **Absent (`AB`, 0.0 weight)** — Medical or unexcused leaves with mandatory remarks.
4. **💾 Reactive Room Database Layer**:
   - Consolidated SQLite persistence (`AppDatabase.kt`) with DAOs for students, teachers, and attendance logs.
   - Live synchronization between interactive UI chips, filter tabs, and on-device storage.
5. **🎓 Exam Eligibility & Academic Tracking**:
   - Real-time gauge validating the institutional 75% minimum attendance threshold for Term Exam hall tickets.

---

## 🏛️ Role-Based Modules & Feature Breakdown

### 👨‍🎓 1. Student Portal
- **Dashboard Overview**: Quick access to current GPA, attendance percentage, today's schedule, and pending tasks.
- **Attendance Insights**: Interactive circular attendance meter, breakdown by session weight (FD/HD/OD/AB), and subject-wise attendance progress bars.
- **Academic Timetable**: Real-time period-by-period daily schedule with room numbers and teacher names.
- **Homework & Submissions**: View assignment details, due dates, submission statuses, and teacher notes.
- **Digital ID Card**: Integrated institutional ID card with student photo, barcode/QR badge, blood group, emergency contact, and school house affiliation.

### 👩‍🏫 2. Teacher & Faculty Portal
- **Homeroom Roll Call**: Fast one-tap attendance register with batch **"All Full Day"** and individual FD/HD/OD/AB selection.
- **Remarks & Notes**: Add notes for medical leave, extracurricular participation, or administrative remarks.
- **Class Rosters**: Filter and inspect student rosters across assigned classes and sections.
- **Assignment Publisher**: Distribute homework, set deadlines, and monitor student submission progress.
- **Syllabus & Duties**: Track curriculum progress and view assigned campus invigilation duties.

### 🛠️ 3. Operations & Campus Staff Portal
- **Campus Duty Rosters**: Daily assigned operational duties (Gate supervision, lab maintenance, cafeteria oversight, sports ground setup).
- **Incident & Facility Reporting**: Log campus maintenance requests with priority flags.
- **Institutional Directory**: Search faculty and department extensions.

### 👑 4. Principal & Administration Portal
- **Governance Dashboard**: Institutional attendance averages, staff strength metrics, fee collection summaries, and grade distribution.
- **School-Wide Circulars**: Publish urgent notices and announcements across specific user cohorts.
- **Database Administration**: Inspect demo SQLite seeding records and manage global academic configurations.

---

## 🏗️ Architecture & Technical Stack

```
                              ┌─────────────────────────────┐
                              │     Jetpack Compose UI      │
                              │  (Material 3 Components)   │
                              └──────────────┬──────────────┘
                                             │
                                             ▼
                              ┌─────────────────────────────┐
                              │  ViewModels & UI StateFlow  │
                              │(SchoolViewModel / AuthVM)   │
                              └──────────────┬──────────────┘
                                             │
                                             ▼
                              ┌─────────────────────────────┐
                              │      SchoolRepository       │
                              │ (State orchestration / Cache)│
                              └──────────────┬──────────────┘
                                             │
                                             ▼
                              ┌─────────────────────────────┐
                              │     Android Room DB         │
                              │  (SQLite with KSP & DAOs)   │
                              └─────────────────────────────┘
```

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0 | Type-safe, coroutine-powered development |
| **UI System** | Jetpack Compose (M3) | Declarative UI with Dynamic Color & Adaptive Scaffolding |
| **State Management** | StateFlow & SharedFlow | Reactive, lifecycle-aware architecture |
| **Local Persistence** | Android Room Database | Type-safe SQLite persistence using KSP compiler |
| **Dependency Injection**| Constructor Injection | Modular, clean architecture |
| **Testing** | Robolectric & Roborazzi | JVM component testing and visual regression suites |

---

## 📂 Source Code Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                       # Single-activity container with Edge-to-Edge support
├── model/
│   ├── SchoolModels.kt                   # Domain entities, Enums (AttendanceStatus, UserRole, etc.)
│   └── ThemeMode.kt                      # Theme configuration models (System, Light, Dark)
├── data/
│   ├── SchoolRepository.kt               # Central data provider & business logic
│   └── local/
│       ├── AppDatabase.kt                # Room Database definition, migrations & seeding
│       ├── Converters.kt                 # Room TypeConverters for Enums & Lists
│       ├── dao/
│       │   ├── AttendanceDao.kt          # Daily attendance queries & batch transactions
│       │   ├── StudentDao.kt             # Student roster and profile data access
│       │   └── TeacherDao.kt             # Teacher faculty data access
│       └── entity/
│           ├── AttendanceEntity.kt       # Attendance SQLite table entity
│           ├── StudentEntity.kt          # Student SQLite table entity
│           └── TeacherEntity.kt          # Teacher SQLite table entity
├── viewmodel/
│   ├── AuthenticationViewModel.kt        # Room credential validation & session state
│   └── SchoolViewModel.kt                # School operations, attendance & timetable state
└── ui/
    ├── MainSchoolApp.kt                  # Top-level scaffold, navigation bar, and dialogs
    ├── auth/
    │   └── LoginScreen.kt                # Institutional login & quick demo selector
    ├── screens/
    │   ├── AttendanceScreen.kt           # Student report & Teacher roll-call register
    │   ├── TeacherAttendanceScreen.kt    # Dedicated Homeroom Teacher register
    │   ├── ClassesScreen.kt              # Academic class rosters & section timetables
    │   ├── HomeworkScreen.kt             # Homework tasks and student submission flow
    │   ├── TimetableScreen.kt            # Weekly class schedule
    │   ├── NoticesScreen.kt              # Official circulars & urgent bulletins
    │   ├── FeesScreen.kt                 # Term fees breakdown and digital receipts
    │   ├── DutiesScreen.kt               # Operations staff task rosters
    │   ├── ManagementScreen.kt           # Institutional records & directory
    │   ├── SettingsScreen.kt             # Dark mode toggles, notifications & DB reset
    │   └── ProfileScreen.kt              # Digital ID badge & role switcher
    ├── dashboard/                        # Role-specific home dashboards
    ├── components/                       # Reusable M3 cards, headers, stat badges & dialogs
    └── theme/                            # Theme.kt, Color.kt, Type.kt
```

---

## 👥 Demo Institutional Accounts

> 💡 **Tip**: Tap any quick-switch avatar on the Login Screen or select a role from the **Profile Tab** to immediately preview that stakeholder's dashboard.

**Default Prototype Password**: `password123`

| Role | Username | Email | Name | Designation / Assignment |
| :--- | :--- | :--- | :--- | :--- |
| 👨‍🎓 **Student** | `student01` | `alex.j@stjosephs.edu` | Alex Johnson | Class 10-A, Roll #1, St. Patrick House |
| 👩‍🏫 **Teacher** | `teacher01` | `s.jenkins@stjosephs.edu` | Prof. Sarah Jenkins | Class Teacher of Class 10-A (Physics & Lab) |
| 🛠️ **Staff** | `staff01` | `t.wright@stjosephs.edu` | Mr. Thomas Wright | Senior Operations Supervisor |
| 👑 **Admin** | `admin01` | `principal@stjosephs.edu` | Dr. Arthur Pendelton | Principal & Head of Institution |


---

## 📦 How to Build, Export & Publish

### 1. Pushing Changes to GitHub
In **Google AI Studio**:
1. Open the project menu in the top navigation bar or settings sidebar.
2. Click **Export / Push to GitHub**.
3. Select or link your target GitHub repository and branch (e.g., `main`).
4. Commit and push the updated codebase.

### 2. Generating the Release APK / AAB
1. In the AI Studio settings menu, select **Build APK** or **Generate Release Bundle**.
2. Alternatively, compile and assemble via Gradle / CLI:
   ```bash
   # Build Debug APK
   gradle assembleDebug

   # Build Release APK (Unsigned/Signed)
   gradle assembleRelease
   ```
3. Locate the generated APK at `app/build/outputs/apk/release/app-release-unsigned.apk` (or `debug/app-debug.apk`).

### 3. Automated GitHub Actions CI/CD Workflow
This repository includes `.github/workflows/build.yml` which automatically:
- Builds both Debug and Release APKs on push / PR to `main` or via `workflow_dispatch`.
- Archives and uploads the generated APK artifacts (`app-debug-v1.1.0` and `app-release-unsigned-v1.1.0`) directly to GitHub Actions Summary for 1-click download.

### 4. Running Automated Tests
```bash
# Execute local JVM Robolectric unit tests
gradle :app:testDebugUnitTest
```

---

## 📄 License & Attribution
**St. Joseph's Higher Secondary School Management System**  
*Motto: "Shine and Let Shine"*  
Designed and engineered using Google AI Studio. All rights reserved.
