# 🏛️ St. Joseph's Higher Secondary School — Android Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Database-Android%20Room%20SQLite-FFCA28.svg?logo=sqlite&logoColor=black)](https://developer.android.com/training/data-storage/room)
[![Release](https://img.shields.io/badge/Release-v2.5.0-blue.svg)](https://github.com/2009skvgamerz/School-project-application-/releases)

A native Android School ERP and Student Information System built with **Jetpack Compose (Material Design 3)**, **Kotlin Coroutines & Flow**, and **Android Room Database**. Designed specifically for St. Joseph's Higher Secondary School, this platform unifies daily academic operations, digital homeroom roll-calls, student timetables, homework assignments, circulars, fee tracking, transport systems, and campus duty management into an intuitive, role-tailored mobile experience.

---

## 🚀 What's New in Release v2.5.0

> **Previous Release**: `v2.4.0`  
> **Current Release**: `v2.5.0` (Top-Level UserProfileHeader Component, Full Dashboard Layout Optimization & Header Streamlining)

### 🌟 Key Highlights & Enhancements
1. **👤 Universal `UserProfileHeader` Component**:
   - Created a top-level `UserProfileHeader` component added across all role-based dashboards (Student, Teacher, Operations Staff, Transport Driver, Admin, Developer).
   - Displays user's full name, role-specific profile avatar badge, live status indicator (*"Active Session"*, *"PILOT ACTIVE"*, *"GOD MODE ACTIVE"*), and current school session pill (*"Academic Session 2026–2027"*).
   - Includes contextual details such as Class/Section & Roll #, Department & Employee ID, Vehicle Route & Driver's License, or Office Location.

2. **✨ Full Dashboard Layout Optimization & Header De-duplication**:
   - Streamlined Driver and Developer dashboards by removing redundant duplicate header cards beneath the top header for a clean, hierarchical layout.
   - Refined Driver cockpit with a focused *"Operating Vehicle & Route"* card containing active bus info, route details, quick bus switcher, and in-app map shortcut.
   - Fixed subtitle formatting bugs (eliminating duplicate `"Bus #Bus #"` prefixes).

3. **📱 Top Bar Compact Layout & Truncation Prevention**:
   - Streamlined top bar actions in `BaseDashboardScaffold` to compact the role switcher icon button, preserving header space and preventing text truncation of *"St. Joseph's"* title, network badge, and tab titles on smaller device viewports.

4. **🏷️ Metadata & String Standardization**:
   - Aligned platform `metadata.json` and Android resource `strings.xml` to officially declare St. Joseph's School **v2.5.0**.

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
- **Sub-Role Support**: Select either Operational Staff duties or **Transport Route Management (Driver)**.
- **Campus Duty Rosters**: Daily assigned operational duties (Gate supervision, lab maintenance, cafeteria oversight, sports ground setup).
- **Incident & Facility Reporting**: Log campus maintenance requests with priority flags.
- **Institutional Directory**: Search faculty and department extensions.

### 🚗 4. Transport & Driver Dashboard
- **Interactive WebView Map**: Real-time coordinate visualization, GPS-lock switches, and route bounds fitting.
- **Passenger Checklist**: Check students off at designated stop times.
- **Google Maps Navigation**: One-tap redirection to open default Google Maps navigation for specific stops.

### 👑 5. Principal & Administration Portal
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
    │   └── LoginScreen.kt                # Institutional login with unified staff panel
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
| 🛠️ **Staff (Operations)** | `staff01` | `t.wright@stjosephs.edu` | Mr. Thomas Wright | Senior Operations Supervisor |
| 🚗 **Staff (Driver)** | `driver01` | `m.ross@stjosephs.edu` | Mr. Marcus Ross | Transport Driver (Route #05) |
| 👑 **Admin** | `admin01` | `principal@stjosephs.edu` | Dr. Arthur Pendelton | Principal & Head of Institution |

---

## 📄 License & Attribution
**St. Joseph's Higher Secondary School Management System**  
*Motto: "Shine and Let Shine"*  
Designed and engineered using Google AI Studio. All rights reserved.
