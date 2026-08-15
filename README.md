# St. Joseph's Higher Secondary School - Android Management System

A modern, institutional Android application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, and **Android Room Database** to streamline school operations, classroom attendance tracking, academic timetables, fee management, student notices, and staff workflows.

---

## 🏛️ Application Overview

St. Joseph's School Management System provides a role-tailored experience for the primary stakeholders of an educational institution:
1. **Students & Parents**: View daily timetables, homework assignments with submission statuses, examination report cards, academic attendance records, and institutional fee payment receipts.
2. **Teachers & Faculty**: Class Teacher homeroom management, daily attendance roll call register with four institutional statuses, homework assignment publishing, syllabus progress tracking, and student progress reports.
3. **Operations Staff**: Campus facility maintenance duty rosters, schedule tracking, visitor logs, and issue reporting.
4. **School Administration & Principal**: Institutional overview, faculty attendance, fee collection metrics, circular announcements, and grade distribution analytics.

---

## ✨ Key Features & Modules

### 1. 📋 Teacher Daily Roll-Call & Room Database Attendance
- **Four Daily Attendance Options**:
  - 🟢 **Full-day (`FD`, 1.0 weight)** — Regular full session presence.
  - 🟡 **Half-day (`HD`, 0.5 weight)** — Morning or afternoon partial session.
  - 🔵 **On-duty (`OD`, 1.0 weight)** — Authorized institutional representation (Inter-School Sports, Science Olympiad, Arts Fests).
  - 🔴 **Absent (`AB`, 0.0 weight)** — Unexcused absence or parent-notified sick leave.
- **Class Teacher Authorization**: Homeroom teachers (*e.g., Prof. Sarah Jenkins for Class 10-A*) have exclusive permissions to record daily roll calls.
- **Batch Actions**: One-click **"Mark All Full Day"** and status filters.
- **Remark & Justification Notes**: Add specific event descriptions for OD/HD/AB notes (*e.g., "State Basketball Championship"*, *"Medical leave"*).
- **SQLite Persistence**: Directly written to and queried from the Room Database `attendance_records` table.

### 2. 🔐 Authentication & Session Management (`AuthenticationViewModel`)
- **Institutional Credential Validation**:
  - Validates credentials directly against Room SQLite database tables (`students`, `teachers`).
  - Supports search and login via Admission Number (*e.g., `SJ-2024-1001`*), Employee ID (*e.g., `EMP-0412`*), or institutional email addresses.
  - Handles administrative and operations staff session initializations.
- **Reactive Session State**: Manages `AuthState` (`Unauthenticated`, `Authenticating`, `Authenticated`, `Error`) with role-specific profile hydration (`StudentProfile`, `TeacherProfile`, `AdminProfile`, `StaffProfile`).

### 3. 📚 Academic Management & Timetables
- **Class Schedules**: Daily period-by-period timetable with subject codes, teacher assignments, and classroom room numbers.
- **Homework & Assignments**: Track pending, submitted, and graded tasks with submission notes and deadline countdowns.
- **Digital ID Card**: Integrated institutional ID card with QR code badge, blood group, parent emergency contact, and house allocation.

### 4. 📢 Circulars, Notices & Fee Portal
- **Notice Board**: Categorized bulletins (*Academic, Sports, Examinations, Administrative*) with urgent priority tags.
- **Fee Management**: Term fee breakdowns, scholarship adjustments, due date warnings, and payment receipt generation.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: Modern Jetpack Compose with Material Design 3 (M3).
- **Language**: 100% Kotlin with strict null-safety and sealed classes.
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture with reactive Kotlin `StateFlow` and `SharedFlow`.
- **Database / Local Persistence**:
  - **Android Room Database** with KSP (`AppDatabase`).
  - Tables: `students`, `teachers`, `attendance_records`.
  - Type converters (`Converters.kt`) for `List<String>` and `AttendanceStatus` enums.
  - Incremental database migration strategy (`MIGRATION_1_2`) and automated seeding callbacks.
- **Testing**:
  - JVM Unit and Component testing powered by **Robolectric** (SDK 36).
  - Roborazzi visual regression testing infrastructure.

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                       # Entry activity with Edge-to-Edge and Theme setup
├── model/
│   └── SchoolModels.kt                   # Domain data classes, Enums (AttendanceStatus, UserRole, etc.)
├── data/
│   ├── SchoolRepository.kt               # Central data provider and repository
│   └── local/
│       ├── AppDatabase.kt                # Room database definition, migrations & callbacks
│       ├── Converters.kt                 # Room TypeConverters for Enums & Collections
│       ├── dao/
│       │   ├── AttendanceDao.kt          # Attendance table DAO queries & updates
│       │   ├── StudentDao.kt             # Student table DAO queries
│       │   └── TeacherDao.kt             # Teacher table DAO queries
│       └── entity/
│           ├── AttendanceEntity.kt       # Attendance SQLite entity
│           ├── StudentEntity.kt          # Student roster SQLite entity
│           └── TeacherEntity.kt          # Teacher faculty SQLite entity
├── viewmodel/
│   ├── AuthenticationViewModel.kt        # Room credential validation & session lifecycle
│   └── SchoolViewModel.kt                # School operations, attendance & academic state
└── ui/
    ├── MainSchoolApp.kt                  # Top-level scaffold and navigation tabs
    ├── auth/
    │   └── LoginScreen.kt                # Role-based login and demo selector screen
    ├── screens/
    │   ├── TeacherAttendanceScreen.kt    # Dedicated Teacher daily roll call interface
    │   ├── AttendanceScreen.kt           # Student/Parent/Admin attendance overview
    │   ├── ClassesScreen.kt              # Timetable and syllabus tracking
    │   ├── HomeworkScreen.kt             # Homework tasks and submissions
    │   ├── NoticesScreen.kt              # School announcements and bulletins
    │   ├── FeesScreen.kt                 # Institutional fee breakdown and receipts
    │   ├── ManagementScreen.kt           # Staff duty rosters and facility tickets
    │   └── ProfileScreen.kt              # Profile & Digital ID badge
    ├── dashboard/
    │   ├── StudentDashboardScreen.kt     # Student home dashboard
    │   ├── TeacherDashboardScreen.kt     # Faculty home dashboard
    │   ├── StaffDashboardScreen.kt       # Operations staff dashboard
    │   └── AdminDashboardScreen.kt       # Principal / Admin dashboard
    ├── components/                       # Reusable UI cards, headers, dialogs & badges
    └── theme/                            # Material 3 Color Schemes, Typography, Shapes
```

---

## 🚀 Running & Verification

### Build the App
```bash
gradle assembleDebug
```

### Run Local Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 👥 Demo Institutional Accounts (Science Expo Prototype)

> ℹ️ **Prototype Authentication Notice**: The current demo authentication system is temporary and explicitly configured for the interactive Science Expo demonstration. Unauthorized or unknown usernames are rejected. In future production releases, this authentication layer will be replaced with Firebase Authentication using Google Sign-In and institutional identity federation.

**Default Prototype Password**: `password123`

| Role | Username | Email | Name | Default Assignment / Profile |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | `student01` | `alex.j@stjosephs.edu` | Alex Johnson | Class 10-A, Roll #1, St. Patrick House |
| **Teacher** | `teacher01` | `s.jenkins@stjosephs.edu` | Prof. Sarah Jenkins | Class Teacher of Class 10-A (Physics & Lab) |
| **Staff** | `staff01` | `t.wright@stjosephs.edu` | Mr. Thomas Wright | Senior Operations Supervisor |
| **Admin** | `admin01` | `principal@stjosephs.edu` | Dr. Arthur Pendelton | Principal & Head of Institution |

---

## 🏗️ Architecture & Cleaned Structure

- **Single Authoritative Room Database**: `AppDatabase.kt` manages all local SQLite tables (`students`, `teachers`, `attendance_records`) with automated seeding and DAOs. Duplicate databases have been removed.
- **Single Authoritative ViewModel**: `viewmodel/SchoolViewModel.kt` handles all application state, timetable flows, notice publishing, homework management, and attendance updates with Room synchronization. Duplicate ViewModels have been removed.
- **Isolate Prototype Authentication**: `AuthenticationViewModel.kt` and `SchoolRepository.kt` manage login validation against explicitly authorized demo accounts and Room database entities, rejecting unknown usernames with descriptive error feedback.

---

## 📄 License
Internal Institutional Software for St. Joseph's Higher Secondary School. Built with Google AI Studio.
