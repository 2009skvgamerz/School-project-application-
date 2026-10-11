package com.example.data.seed

import com.example.data.DemoAccount
import com.example.model.*

object UsersAndDirectorySeedData {

  const val DEMO_PASSWORD = "password123"

  val demoUsers = listOf(
    DemoAccount("student01", DEMO_PASSWORD, UserRole.STUDENT, "Keerthivasan", "keerthivasan.s@stjosephs.edu"),
    DemoAccount("teacher01", DEMO_PASSWORD, UserRole.TEACHER, "Prof. Sarah Jenkins", "s.jenkins@stjosephs.edu"),
    DemoAccount("staff01", DEMO_PASSWORD, UserRole.STAFF, "Mr. Thomas Wright", "t.wright@stjosephs.edu"),
    DemoAccount("driver01", DEMO_PASSWORD, UserRole.DRIVER, "Mr. Ramesh Kumar", "driver@stjosephs.edu"),
    DemoAccount("admin01", DEMO_PASSWORD, UserRole.ADMIN, "Dr. Arthur Pendelton", "principal@stjosephs.edu"),
    DemoAccount("dev", DEMO_PASSWORD, UserRole.DEVELOPER, "Keerthivasan", "keerthivasan.dev@stjosephs.edu")
  )

  val initialSystemUsers = listOf(
    SystemUserRecord(
      id = "usr_dev_01",
      username = "dev",
      fullName = "Keerthivasan",
      email = "keerthivasan.dev@stjosephs.edu",
      role = UserRole.DEVELOPER,
      phone = "+91 99999 00000",
      designation = "Lead Systems Developer & Root Admin",
      identifier = "DEV-ROOT-007",
      departmentOrGrade = "Core Infrastructure & Engineering",
      sectionOrRoom = "Server Room Alpha",
      extraNotes = "Full unrestricted master write & debug access"
    ),
    SystemUserRecord(
      id = "usr_student_01",
      username = "student01",
      fullName = "Keerthivasan",
      email = "keerthivasan.s@stjosephs.edu",
      role = UserRole.STUDENT,
      phone = "+91 98450 12001",
      designation = "Grade 12 - Section A",
      identifier = "SJ-2024-1201",
      departmentOrGrade = "12",
      sectionOrRoom = "A",
      extraNotes = "Science Expo Head Coordinator, Roll #1"
    ),
    SystemUserRecord(
      id = "usr_std_1202",
      username = "kavya_s",
      fullName = "Kavya Sundaram",
      email = "kavya.s@stjosephs.edu",
      role = UserRole.STUDENT,
      phone = "+91 98450 12002",
      designation = "Grade 12 - Section A",
      identifier = "SJ-2024-1202",
      departmentOrGrade = "12",
      sectionOrRoom = "A",
      extraNotes = "Class Representative, Roll #2"
    ),
    SystemUserRecord(
      id = "usr_std_1203",
      username = "varun_r",
      fullName = "Varun Raghavan",
      email = "varun.r@stjosephs.edu",
      role = UserRole.STUDENT,
      phone = "+91 98450 12003",
      designation = "Grade 12 - Section A",
      identifier = "SJ-2024-1203",
      departmentOrGrade = "12",
      sectionOrRoom = "A",
      extraNotes = "Robotics Team Captain, Roll #3"
    ),
    SystemUserRecord(
      id = "usr_std_1204",
      username = "ananya_i",
      fullName = "Ananya Iyer",
      email = "ananya.i@stjosephs.edu",
      role = UserRole.STUDENT,
      phone = "+91 98450 12004",
      designation = "Grade 12 - Section A",
      identifier = "SJ-2024-1204",
      departmentOrGrade = "12",
      sectionOrRoom = "A",
      extraNotes = "Science Olympiad Winner, Roll #4"
    ),
    SystemUserRecord(
      id = "usr_teacher_01",
      username = "teacher01",
      fullName = "Prof. Sarah Jenkins",
      email = "s.jenkins@stjosephs.edu",
      role = UserRole.TEACHER,
      phone = "+91 98765 22001",
      designation = "Senior Physics Faculty • Class Teacher (10-A)",
      identifier = "EMP-T-2018",
      departmentOrGrade = "Physical & Chemical Sciences",
      sectionOrRoom = "Staff Room 2B",
      extraNotes = "M.Sc. Physics, Gold Medalist"
    ),
    SystemUserRecord(
      id = "usr_tch_202",
      username = "dmiller",
      fullName = "Mr. David Miller",
      email = "d.miller@stjosephs.edu",
      role = UserRole.TEACHER,
      phone = "+91 98765 22002",
      designation = "Senior Mathematics Faculty",
      identifier = "EMP-T-2019",
      departmentOrGrade = "Mathematics & Statistics",
      sectionOrRoom = "Staff Room 2A",
      extraNotes = "Class Teacher (10-B)"
    ),
    SystemUserRecord(
      id = "usr_tch_203",
      username = "asharma",
      fullName = "Dr. Anita Sharma",
      email = "a.sharma@stjosephs.edu",
      role = UserRole.TEACHER,
      phone = "+91 98765 22003",
      designation = "Head of Chemistry Department",
      identifier = "EMP-T-2015",
      departmentOrGrade = "Chemical Sciences",
      sectionOrRoom = "Chemistry Lab 1",
      extraNotes = "Ph.D. Organic Chemistry"
    ),
    SystemUserRecord(
      id = "usr_tch_204",
      username = "kross",
      fullName = "Mr. Kevin Ross",
      email = "k.ross@stjosephs.edu",
      role = UserRole.TEACHER,
      phone = "+91 98765 22004",
      designation = "Lead Computer Science & AI Faculty",
      identifier = "EMP-T-2021",
      departmentOrGrade = "Computer Science & Robotics",
      sectionOrRoom = "Computer Lab B",
      extraNotes = "Robotics Club Mentor"
    ),
    SystemUserRecord(
      id = "usr_staff_01",
      username = "staff01",
      fullName = "Mr. Thomas Wright",
      email = "t.wright@stjosephs.edu",
      role = UserRole.STAFF,
      phone = "+91 98765 33014",
      designation = "Facilities & Campus Supervisor",
      identifier = "EMP-S-304",
      departmentOrGrade = "Campus Operations & Safety",
      sectionOrRoom = "Main Operations Center",
      extraNotes = "Campus Fire & Safety Marshal"
    ),
    SystemUserRecord(
      id = "usr_stf_302",
      username = "rtaylor",
      fullName = "Robert Taylor",
      email = "r.taylor@stjosephs.edu",
      role = UserRole.STAFF,
      phone = "+91 98765 33015",
      designation = "Senior Laboratory Technician",
      identifier = "EMP-S-305",
      departmentOrGrade = "Science Block Laboratories",
      sectionOrRoom = "Lab Storage & Prep Room",
      extraNotes = "Safety Calibration In-charge"
    ),
    SystemUserRecord(
      id = "usr_admin_01",
      username = "admin01",
      fullName = "Dr. Arthur Pendelton",
      email = "principal@stjosephs.edu",
      role = UserRole.ADMIN,
      phone = "+91 98765 00001",
      designation = "Principal & Head of Institution",
      identifier = "ADM-001",
      departmentOrGrade = "Executive Administration",
      sectionOrRoom = "Principal's Office, Main Block",
      extraNotes = "Institutional Executive Authority"
    )
  )

  val initialDirectoryContacts = listOf(
    // 1. Helplines & Emergency
    DirectoryContact(
      id = "dir_sos_1",
      name = "24/7 Campus Emergency SOS & Security Desk",
      role = UserRole.ADMIN,
      category = DirectoryCategory.HELPLINE,
      designation = "24/7 Rapid Emergency Response Control",
      departmentOrGrade = "Campus Security & Safety Command",
      phoneNumber = "+91 98450 91100",
      email = "emergency@stjosephs.edu",
      roomOrLocation = "Gate 1 Main Security Office",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_sos_2",
      name = "Campus Clinic & Medical Infirmary",
      role = UserRole.STAFF,
      category = DirectoryCategory.HELPLINE,
      designation = "Head Nurse & First Aid Response",
      departmentOrGrade = "Health & Medical Wellness",
      phoneNumber = "+91 98450 91102",
      email = "infirmary@stjosephs.edu",
      roomOrLocation = "Ground Floor Health Wing, Block A",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_sos_3",
      name = "Student Well-being & Counseling Cell",
      role = UserRole.STAFF,
      category = DirectoryCategory.HELPLINE,
      designation = "Lead Student Counselor & Psychologist",
      departmentOrGrade = "Student Wellness Department",
      phoneNumber = "+91 98450 91101",
      email = "counselor@stjosephs.edu",
      roomOrLocation = "Room 108, Block C",
      isStudent = false
    ),

    // 2. Teachers & Faculty
    DirectoryContact(
      id = "dir_tch_1",
      name = "Prof. Sarah Jenkins",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Senior Faculty & Class 10-A Mentor",
      departmentOrGrade = "Physics & Applied Sciences",
      phoneNumber = "+91 98765 22001",
      email = "s.jenkins@stjosephs.edu",
      roomOrLocation = "Staff Room 2B, 2nd Floor",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_tch_2",
      name = "Dr. Michael Chang",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Head of Department (HOD) - Mathematics",
      departmentOrGrade = "Mathematics & Advanced Calculus",
      phoneNumber = "+91 98765 22002",
      email = "m.chang@stjosephs.edu",
      roomOrLocation = "Math Faculty Suite 1A",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_tch_3",
      name = "Mrs. Anita Sharma",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Head of Department (HOD) - Chemistry",
      departmentOrGrade = "Chemical Sciences & Research",
      phoneNumber = "+91 98765 22003",
      email = "a.sharma@stjosephs.edu",
      roomOrLocation = "Chemistry Lab 1 Prep Room",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_tch_4",
      name = "Mr. Kevin Ross",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Lead Computer Science & AI Faculty",
      departmentOrGrade = "Computer Science & Robotics",
      phoneNumber = "+91 98765 22004",
      email = "k.ross@stjosephs.edu",
      roomOrLocation = "Computer Lab B, Tech Block",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_tch_5",
      name = "Ms. Rachel Green",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Senior English & Debate Master",
      departmentOrGrade = "Humanities & Languages",
      phoneNumber = "+91 98765 22005",
      email = "r.green@stjosephs.edu",
      roomOrLocation = "Staff Room 2A",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_tch_6",
      name = "Coach David Miller",
      role = UserRole.TEACHER,
      category = DirectoryCategory.FACULTY,
      designation = "Director of Physical Education & Sports",
      departmentOrGrade = "Physical Education & Athletics",
      phoneNumber = "+91 98765 22006",
      email = "d.miller@stjosephs.edu",
      roomOrLocation = "Sports Pavilion Office",
      isStudent = false
    ),

    // 3. Administration
    DirectoryContact(
      id = "dir_adm_1",
      name = "Dr. Arthur Pendelton",
      role = UserRole.ADMIN,
      category = DirectoryCategory.ADMINISTRATION,
      designation = "Principal & Head of Institution",
      departmentOrGrade = "Executive Administration Directorate",
      phoneNumber = "+91 98765 00001",
      email = "principal@stjosephs.edu",
      roomOrLocation = "Principal's Office, Main Block",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_adm_2",
      name = "Dr. Anthony Davies",
      role = UserRole.ADMIN,
      category = DirectoryCategory.ADMINISTRATION,
      designation = "Vice Principal & Academic Dean",
      departmentOrGrade = "Academic Directorate",
      phoneNumber = "+91 98765 00002",
      email = "viceprincipal@stjosephs.edu",
      roomOrLocation = "Dean's Office 102",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_adm_3",
      name = "Mr. S. Ramanathan",
      role = UserRole.ADMIN,
      category = DirectoryCategory.ADMINISTRATION,
      designation = "Bursar & Accounts Controller",
      departmentOrGrade = "Finance & Fee Management",
      phoneNumber = "+91 98765 00003",
      email = "accounts@stjosephs.edu",
      roomOrLocation = "Accounts Wing, Ground Floor",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_adm_4",
      name = "Mrs. Jennifer Lee",
      role = UserRole.ADMIN,
      category = DirectoryCategory.ADMINISTRATION,
      designation = "Exam Cell Controller & Registrar",
      departmentOrGrade = "Central Examination Bureau",
      phoneNumber = "+91 98765 00004",
      email = "examcell@stjosephs.edu",
      roomOrLocation = "Exam Control Room 105",
      isStudent = false
    ),

    // 4. Staff & Operations
    DirectoryContact(
      id = "dir_stf_1",
      name = "Mr. Thomas Wright",
      role = UserRole.STAFF,
      category = DirectoryCategory.STAFF,
      designation = "Facilities & Campus Supervisor",
      departmentOrGrade = "Campus Operations & Safety",
      phoneNumber = "+91 98765 33014",
      email = "t.wright@stjosephs.edu",
      roomOrLocation = "Main Operations Center",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_stf_2",
      name = "Mr. Robert Taylor",
      role = UserRole.STAFF,
      category = DirectoryCategory.STAFF,
      designation = "Senior Laboratory Technician",
      departmentOrGrade = "Science Block Laboratories",
      phoneNumber = "+91 98765 33015",
      email = "r.taylor@stjosephs.edu",
      roomOrLocation = "Lab Storage & Prep Room",
      isStudent = false
    ),

    // 5. Transport Fleet & Drivers
    DirectoryContact(
      id = "dir_trn_0",
      name = "Mr. Gururaj Naik",
      role = UserRole.STAFF,
      category = DirectoryCategory.TRANSPORT,
      designation = "Chief Transport & Fleet In-charge",
      departmentOrGrade = "Transport Management Bureau",
      phoneNumber = "+91 98450 99001",
      email = "transport@stjosephs.edu",
      roomOrLocation = "Transport Fleet Command Desk",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_trn_1",
      name = "Mr. Ramesh Kumar (Bus 12 Driver)",
      role = UserRole.STAFF,
      category = DirectoryCategory.TRANSPORT,
      designation = "Designated Driver - Route #12 Indiranagar",
      departmentOrGrade = "School Bus Fleet",
      phoneNumber = "+91 98451 22334",
      email = "bus12@stjosephs.edu",
      roomOrLocation = "Bus Bay #12",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_trn_2",
      name = "Mr. Suresh Babu (Bus 05 Driver)",
      role = UserRole.STAFF,
      category = DirectoryCategory.TRANSPORT,
      designation = "Designated Driver - Route #05 Whitefield",
      departmentOrGrade = "School Bus Fleet",
      phoneNumber = "+91 98452 33445",
      email = "bus05@stjosephs.edu",
      roomOrLocation = "Bus Bay #05",
      isStudent = false
    ),
    DirectoryContact(
      id = "dir_trn_3",
      name = "Mr. Mohan Raj (Bus 08 Driver)",
      role = UserRole.STAFF,
      category = DirectoryCategory.TRANSPORT,
      designation = "Designated Driver - Route #08 Koramangala",
      departmentOrGrade = "School Bus Fleet",
      phoneNumber = "+91 98453 44556",
      email = "bus08@stjosephs.edu",
      roomOrLocation = "Bus Bay #08",
      isStudent = false
    ),

    // 6. Students (Privacy-Protected for peer students)
    DirectoryContact(
      id = "dir_stu_1",
      name = "Keerthivasan S",
      role = UserRole.STUDENT,
      category = DirectoryCategory.STUDENTS,
      designation = "Grade 12-A • Roll #1 • House Captain",
      departmentOrGrade = "Grade 12 - Section A",
      phoneNumber = "+91 98450 12001",
      email = "keerthivasan.s@stjosephs.edu",
      roomOrLocation = "Classroom 12-A",
      isStudent = true,
      parentContact = "+91 98450 78912",
      bloodGroup = "O+ve"
    ),
    DirectoryContact(
      id = "dir_stu_2",
      name = "Rahul Sharma",
      role = UserRole.STUDENT,
      category = DirectoryCategory.STUDENTS,
      designation = "Grade 12-A • Roll #2 • Science Club Lead",
      departmentOrGrade = "Grade 12 - Section A",
      phoneNumber = "+91 98450 12002",
      email = "rahul.s@stjosephs.edu",
      roomOrLocation = "Classroom 12-A",
      isStudent = true,
      parentContact = "+91 98450 78913",
      bloodGroup = "B+ve"
    ),
    DirectoryContact(
      id = "dir_stu_3",
      name = "Ananya Verma",
      role = UserRole.STUDENT,
      category = DirectoryCategory.STUDENTS,
      designation = "Grade 12-A • Roll #3 • Head Girl",
      departmentOrGrade = "Grade 12 - Section A",
      phoneNumber = "+91 98450 12003",
      email = "ananya.v@stjosephs.edu",
      roomOrLocation = "Classroom 12-A",
      isStudent = true,
      parentContact = "+91 98450 78914",
      bloodGroup = "A+ve"
    ),
    DirectoryContact(
      id = "dir_stu_4",
      name = "Priya Nair",
      role = UserRole.STUDENT,
      category = DirectoryCategory.STUDENTS,
      designation = "Grade 10-A • Roll #1 • Cultural Secretary",
      departmentOrGrade = "Grade 10 - Section A",
      phoneNumber = "+91 98450 10001",
      email = "priya.n@stjosephs.edu",
      roomOrLocation = "Classroom 10-A",
      isStudent = true,
      parentContact = "+91 98450 78915",
      bloodGroup = "AB+ve"
    ),
    DirectoryContact(
      id = "dir_stu_5",
      name = "Rohan Gupta",
      role = UserRole.STUDENT,
      category = DirectoryCategory.STUDENTS,
      designation = "Grade 10-A • Roll #2 • Sports Vice Captain",
      departmentOrGrade = "Grade 10 - Section A",
      phoneNumber = "+91 98450 10002",
      email = "rohan.g@stjosephs.edu",
      roomOrLocation = "Classroom 10-A",
      isStudent = true,
      parentContact = "+91 98450 78916",
      bloodGroup = "O-ve"
    )
  )
}
