# Role-Based CSV User Data Organizer & Database Sync Engine

This update introduces an organized suite of role-specific CSV files directly in the project assets (`students_data.csv`, `teachers_data.csv`, `staff_data.csv`, `drivers_data.csv`, and `admins_data.csv`) paired with an automatic CSV parser and Room/Auth synchronization engine, enabling administrators and data entry staff to add or update user credentials, dates of birth, addresses, and profile details simply by editing spreadsheet-compatible CSV files or importing updated CSVs at runtime.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and workflow choices reflect your confirmed preferences and are ready for your review before code changes begin.

- **Confirmed Decision 1 — Separate CSV Files per Role**: User credentials and profile attributes will be organized into dedicated role-specific CSV files (`students_data.csv`, `teachers_data.csv`, `staff_data.csv`, `drivers_data.csv`, and `admins_data.csv`) inside the project assets rather than hardcoded in Kotlin classes.
- **Confirmed Decision 2 — Dual Sync Strategy (Startup Auto-Sync + In-App CSV Import)**: Whenever the app starts, it automatically parses the bundled CSV files and upserts any new or modified user rows into the local Room SQLite database and authentication roster. Additionally, administrators and developers can import external `.csv` files directly from device storage at runtime.
- **Confirmed Decision 3 — Focused Scope on User & Role Datasets**: Only user, authentication, and role profile records are migrated to CSV files, keeping academic and operational datasets fast and focused.

---

## 1. Overview & Core Concept

- **What It Does**: Replaces hardcoded Kotlin user lists with clean, human-editable CSV files bundled in the project codebase. Each row defines a user's login credentials (`id`, `username`, `password`) alongside role-specific demographic and institutional fields (`full_name`, `dob`, `address`, `email`, `phone`, `admission_no` / `employee_id`, `grade`, `section`, `blood_group`, `parent_name`, etc.).
- **Target Audience / Persona**: School data entry operators, administrators, and developers maintaining the St. Joseph's School Management System who need to bulk-add or edit students, faculty, support staff, and bus drivers using Excel, Google Sheets, or any text editor before or after building the APK.
- **Key Value**: Eliminates the need to touch Kotlin source code when onboarding new batches of students or staff. Adding a row to `students_data.csv` automatically makes that student's username and password active on the Login Screen and populates their full profile across Attendance, Directory, ID Card, and Management modules.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Codebase CSV Editing Flow (Developer / Data Maintainer)**:
     - Open `students_data.csv` (or `teachers_data.csv`, `staff_data.csv`, `drivers_data.csv`, `admins_data.csv`) in the project's assets directory.
     - Add a new row with `id,username,password,full_name,dob,address,...` and build/run the app.
     - On launch, the CSV Sync Engine parses all role CSVs, registers the login credentials (`username` + `password`), and upserts the records into Room SQLite and the active session roster.
     - The newly added user can immediately sign in on the Login Screen using their CSV `username` (or `id`) and `password`.
  2. **Runtime CSV Import & Inspection Flow (Admin & Developer Portals)**:
     - Navigate to **Management → Data Files / CSV Sync** or **Developer Dashboard → CSV Data Organizer**.
     - View each bundled CSV file's live row count, column headers, and parsed user records (including `username`, `id`, `dob`, `address`, and role details).
     - Tap **Import CSV from Device** to pick an updated `.csv` file via the Android system file picker, immediately merging new users into the database without rebuilding the APK, or tap **Re-Sync Bundled CSVs** to reload from the codebase assets.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Clean, structured institutional data workspace consistent with the existing St. Joseph's Navy (`#0F3875`), Royal Blue (`#2563EB`), and Emerald (`#10B981`) palette.
  - *Color Palette & Mood*: Role-coded badges for each CSV dataset (Blue for Students, Emerald for Teachers, Purple for Staff, Orange for Drivers, Amber for Admins) with high-contrast monospace previews for CSV headers and credentials.
  - *Typography & Hierarchy*: Clear tabular layout for CSV rows, bold primary identifiers (`username`, `admission_no` / `employee_id`), and structured metadata chips for `dob` and `address`.
- **Interactive Feedback & Motion**:
  - Instant validation summary banner after CSV parsing or file import showing total rows processed, new accounts registered, updated profiles, and any malformed rows skipped with clear line numbers.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: RFC-4180 Compliant CSV Parser with Quoted Field Support**
  - *Chosen Approach*: Implement a dedicated, zero-dependency Kotlin CSV parser that properly handles commas inside quoted fields (such as street addresses like `"14, MG Road, Bengaluru"` or multi-subject lists like `"Physics|Science Lab"`).
  - *Why*: Student and staff addresses (`address` column) naturally contain commas. A naive `.split(",")` would break column alignment; proper quote-aware parsing ensures addresses and notes never corrupt adjacent columns.
  - *Alternatives Considered*: Third-party CSV libraries were passed over to keep the APK lightweight and avoid external dependency conflicts.
- **Decision 2: Upsert-Based Database & Auth Synchronization**
  - *Chosen Approach*: Use primary key and username matching (`OnConflictStrategy.REPLACE`) when syncing CSV rows into the local Room SQLite database (`students`, `teachers`, and `user_credentials` tables) while preserving persistent storage across app restarts.
  - *Why*: Ensures that editing a student's password, address, or phone number in `students_data.csv` seamlessly updates the existing record on startup without duplicating rows or losing runtime attendance history.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Bundled Codebase CSV Assets                         │
│  • students_data.csv   (id, username, password, full_name, dob, address,│
│                         admission_no, grade, section, roll_no, ...)     │
│  • teachers_data.csv   (id, username, password, full_name, dob, address,│
│                         employee_id, department, classes, subjects, ...)│
│  • staff_data.csv      (id, username, password, full_name, dob, address,│
│                         employee_id, department, duties, shift, ...)    │
│  • drivers_data.csv    (id, username, password, full_name, dob, address,│
│                         driver_id, license_no, bus_no, route_id, ...)   │
│  • admins_data.csv     (id, username, password, full_name, dob, address,│
│                         employee_id, admin_role, office_location, ...)  │
└───────────────────────────────────┬─────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CsvUserDataOrganizer & Parser                        │
│  • Quote-aware CSV reader (supports commas in addresses & pipe lists)   │
│  • Header-mapped row parser with sensible defaults for optional columns │
│  • Runtime URI stream reader for importing external .csv files          │
└─────────────────┬───────────────────────────────────┬───────────────────┘
                  │                                   │
                  ▼                                   ▼
┌───────────────────────────────────┐ ┌───────────────────────────────────┐
│   Room SQLite Persistent Tables   │ │   Authentication & Roster State   │
│  • StudentEntity (+dob, +address, │ │  • Dynamic CSV credential lookup  │
│    +username, +password)          │ │    (username / ID + password)     │
│  • TeacherEntity (+dob, +address, │ │  • Master SystemUserRecord list   │
│    +username, +password)          │ │  • Role Profile builders          │
│  • UserAccountEntity (all roles)  │ │  • Directory contact auto-generation│
└─────────────────┬─────────────────┘ └───────────────┬───────────────────┘
                  │                                   │
                  └─────────────────┬─────────────────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 SchoolRepository & SchoolViewModel                      │
│  • Auto-syncs bundled CSVs on initialization                            │
│  • Authenticates any CSV user on LoginScreen                            │
│  • Exposes live CSV metadata, row previews, and runtime CSV import      │
└─────────────────────────────────────────────────────────────────────────┘
```

- **Data Model & CSV Column Schema**:
  - **`students_data.csv`**: `id`, `username`, `password`, `full_name`, `dob`, `address`, `email`, `phone`, `admission_no`, `grade`, `section`, `roll_no`, `parent_name`, `parent_phone`, `blood_group`, `attendance_percentage`, `house_name`, `bus_route`, `academic_year`, `emergency_contact`
  - **`teachers_data.csv`**: `id`, `username`, `password`, `full_name`, `dob`, `address`, `email`, `phone`, `employee_id`, `department`, `assigned_classes` (`|`-separated), `subjects_taught` (`|`-separated), `qualification`, `is_class_teacher`, `class_teacher_of`, `room_no`, `joining_date`
  - **`staff_data.csv`**: `id`, `username`, `password`, `full_name`, `dob`, `address`, `email`, `phone`, `employee_id`, `department`, `duties` (`|`-separated), `shift_timing`, `emergency_role`, `location_area`
  - **`drivers_data.csv`**: `id`, `username`, `password`, `full_name`, `dob`, `address`, `email`, `phone`, `driver_id`, `license_no`, `assigned_bus_no`, `bus_registration`, `assigned_route_id`, `shift`, `experience_years`, `attendant_name`, `attendant_phone`
  - **`admins_data.csv`**: `id`, `username`, `password`, `full_name`, `dob`, `address`, `email`, `phone`, `employee_id`, `admin_role`, `office_location`, `permissions` (`|`-separated)
- **Interactive Component & State Mapping**:
  - **Startup Sync**: On app initialization, `CsvUserDataOrganizer` reads all role CSV files from `assets/data/`, parses every row, upserts entities into Room SQLite, and registers their `username`/`id` and `password` in `SchoolRepository`.
  - **Login Authentication**: When a user enters their username (or admission/employee ID) and password on the Login Screen, `SchoolRepository.login()` validates against the CSV-backed credential store and loads their exact `StudentProfile`, `TeacherProfile`, `StaffProfile`, `DriverProfile`, or `AdminProfile` (including `dob` and `address`).
  - **Runtime CSV Import**: Using Android's `ActivityResultContracts.OpenDocument`, an admin or developer can select a `.csv` file for any role, which is parsed and merged into the Room database and active state flows with an immediate confirmation summary.
