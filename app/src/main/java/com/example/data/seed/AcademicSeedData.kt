package com.example.data.seed

import com.example.model.*

object AcademicSeedData {

  val initialNotifications = listOf(
    AppNotification(
      id = "notif_1",
      title = "New Homework: Physics (Class 12-A)",
      message = "Electromagnetic Induction numericals assigned by Prof. Sarah Jenkins. Due tomorrow, 09:00 AM.",
      timeAgo = "10m ago",
      type = NotificationType.HOMEWORK,
      isRead = false,
      actionRoute = "homework",
      isUrgent = true
    ),
    AppNotification(
      id = "notif_2",
      title = "Daily Attendance Marked",
      message = "Your attendance for today (Grade 12-A) has been marked as Full Day Present (FD).",
      timeAgo = "1h ago",
      type = NotificationType.ATTENDANCE,
      isRead = false,
      actionRoute = "attendance"
    ),
    AppNotification(
      id = "notif_3",
      title = "Urgent: Science & Tech Expo 2026",
      message = "All Grade 12 project exhibit registrations must be submitted to the STEM cell by Friday 4 PM.",
      timeAgo = "3h ago",
      type = NotificationType.NOTICE,
      isRead = false,
      actionRoute = "notices",
      isUrgent = true
    ),
    AppNotification(
      id = "notif_4",
      title = "Mid-Term Datesheet Released",
      message = "Class 12 board preparation mid-term examination timetable is now live on the student portal.",
      timeAgo = "Yesterday",
      type = NotificationType.EXAM,
      isRead = true,
      actionRoute = "timetable"
    ),
    AppNotification(
      id = "notif_5",
      title = "Term 2 Fee Receipt #SJ-8921",
      message = "Official receipt for Term 2 tuition & laboratory fees generated. Status: Verified Paid.",
      timeAgo = "2 days ago",
      type = NotificationType.FEE,
      isRead = true
    )
  )

  val initialNotices = listOf(
    Notice(
      id = "not_1",
      title = "Annual Science & Tech Expo 2026",
      content = "St. Joseph's Annual Science & Technology Expo will be held on August 20th. All student project exhibits must be registered with science faculty by Friday.",
      date = "14 Aug 2026",
      category = NoticeCategory.EVENT,
      publisherRole = "Principal",
      publisherName = "Dr. Anthony Davies",
      isUrgent = true,
      attachmentName = "Expo_Guidelines_2026.pdf"
    ),
    Notice(
      id = "not_2",
      title = "Mid-Term Examination Schedule Released",
      content = "The timetable for Mid-Term Exams starting next month has been uploaded to the student portal. Practical examinations commence a week prior.",
      date = "12 Aug 2026",
      category = NoticeCategory.ACADEMIC,
      publisherRole = "Exam Cell",
      publisherName = "Prof. Sarah Jenkins",
      isUrgent = false,
      attachmentName = "MidTerm_Datesheet_X_XII.pdf"
    ),
    Notice(
      id = "not_3",
      title = "Inter-School Football Tournament Selections",
      content = "Under-16 and Under-18 football trials will take place at the school main ground at 3:30 PM this Wednesday. Bring your sports kits.",
      date = "11 Aug 2026",
      category = NoticeCategory.SPORTS,
      publisherRole = "Sports Dept",
      publisherName = "Coach Raymond",
      isUrgent = false
    ),
    Notice(
      id = "not_4",
      title = "Independence Day Celebration Protocol",
      content = "Flag hoisting ceremony begins promptly at 08:00 AM on August 15th in the central amphitheatre. Uniform check will be conducted.",
      date = "10 Aug 2026",
      category = NoticeCategory.GENERAL,
      publisherRole = "Administration",
      publisherName = "Office of the Dean",
      isUrgent = false
    )
  )

  val initialHomeworks = listOf(
    Homework(
      id = "hw_1",
      title = "Electromagnetic Induction Numericals",
      description = "Complete exercises 5.1 to 5.4 from chapter 5. Write derivations for Faraday's and Lenz's Law in the physics notebook.",
      subjectName = "Physics",
      className = "Class 12-A",
      assignedDate = "Yesterday",
      dueDate = "Tomorrow, 09:00 AM",
      teacherName = "Prof. Sarah Jenkins",
      status = HomeworkStatus.PENDING,
      maxMarks = 20,
      submissionsCount = 22,
      totalStudents = 28
    ),
    Homework(
      id = "hw_2",
      title = "Calculus Integration Problem Set",
      description = "Solve definite integrals by substitution on page 112 (Q1 to Q20). Show all steps clearly.",
      subjectName = "Mathematics",
      className = "Class 12-A",
      assignedDate = "12 Aug",
      dueDate = "16 Aug, 10:00 AM",
      teacherName = "Mr. David Miller",
      status = HomeworkStatus.PENDING,
      maxMarks = 25,
      submissionsCount = 18,
      totalStudents = 28
    ),
    Homework(
      id = "hw_3",
      title = "Organic Chemistry Synthesis & Reaction Mechanisms",
      description = "Write down aldehyde and ketone nucleophilic addition mechanisms with energy profile diagrams.",
      subjectName = "Chemistry",
      className = "Class 12-A",
      assignedDate = "10 Aug",
      dueDate = "14 Aug, 02:00 PM",
      teacherName = "Dr. Anita Sharma",
      status = HomeworkStatus.SUBMITTED,
      maxMarks = 15,
      submissionNote = "Lab report with observations attached",
      submissionsCount = 27,
      totalStudents = 28
    ),
    Homework(
      id = "hw_4",
      title = "Data Structures: Binary Trees & Graphs",
      description = "Implement BFS and DFS tree traversal algorithms in Kotlin / C++ and analyze time complexity.",
      subjectName = "Computer Science",
      className = "Class 12-A",
      assignedDate = "08 Aug",
      dueDate = "11 Aug",
      teacherName = "Mr. Kevin Ross",
      status = HomeworkStatus.EVALUATED,
      maxMarks = 20,
      submissionNote = "Score: 19/20 - Outstanding algorithm design!",
      submissionsCount = 28,
      totalStudents = 28
    )
  )

  val initialTimetable = listOf(
    TimetableEntry("tt_1", DayOfWeek.MONDAY, 1, "08:30 AM", "09:15 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
    TimetableEntry("tt_2", DayOfWeek.MONDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
    TimetableEntry("tt_3", DayOfWeek.MONDAY, 3, "10:15 AM", "11:00 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 12-A"),
    TimetableEntry("tt_4", DayOfWeek.MONDAY, 4, "11:00 AM", "11:45 AM", "English Lit", "Mrs. Clara Higgins", "Room 302", "Class 12-A"),
    TimetableEntry("tt_5", DayOfWeek.MONDAY, 5, "12:30 PM", "01:15 PM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
    TimetableEntry("tt_6", DayOfWeek.MONDAY, 6, "01:15 PM", "02:00 PM", "Physical Education", "Coach Raymond", "Ground 1", "Class 12-A"),

    TimetableEntry("tt_7", DayOfWeek.TUESDAY, 1, "08:30 AM", "09:15 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
    TimetableEntry("tt_8", DayOfWeek.TUESDAY, 2, "09:15 AM", "10:00 AM", "Physics Lab", "Prof. Sarah Jenkins", "Physics Lab", "Class 12-A"),
    TimetableEntry("tt_9", DayOfWeek.TUESDAY, 3, "10:15 AM", "11:00 AM", "Chemistry Lab", "Dr. Anita Sharma", "Chem Lab", "Class 12-A"),
    TimetableEntry("tt_10", DayOfWeek.TUESDAY, 4, "11:00 AM", "11:45 AM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
    TimetableEntry("tt_11", DayOfWeek.TUESDAY, 5, "12:30 PM", "01:15 PM", "Moral Science", "Fr. Joseph Mathew", "Chapel Hall", "Class 12-A"),

    TimetableEntry("tt_12", DayOfWeek.WEDNESDAY, 1, "08:30 AM", "09:15 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 12-A"),
    TimetableEntry("tt_13", DayOfWeek.WEDNESDAY, 2, "09:15 AM", "10:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
    TimetableEntry("tt_14", DayOfWeek.WEDNESDAY, 3, "10:15 AM", "11:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
    TimetableEntry("tt_15", DayOfWeek.WEDNESDAY, 4, "11:00 AM", "11:45 AM", "Library & Research", "Ms. Beatrice Vance", "Main Library", "Class 12-A"),

    TimetableEntry("tt_16", DayOfWeek.THURSDAY, 1, "08:30 AM", "09:15 AM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
    TimetableEntry("tt_17", DayOfWeek.THURSDAY, 2, "09:15 AM", "10:00 AM", "English Lit", "Mrs. Clara Higgins", "Room 302", "Class 12-A"),
    TimetableEntry("tt_18", DayOfWeek.THURSDAY, 3, "10:15 AM", "11:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
    TimetableEntry("tt_19", DayOfWeek.THURSDAY, 4, "11:00 AM", "11:45 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),

    TimetableEntry("tt_20", DayOfWeek.FRIDAY, 1, "08:30 AM", "09:15 AM", "Robotics & AI Expo Prep", "Prof. Sarah Jenkins", "STEM Lab", "Class 12-A"),
    TimetableEntry("tt_21", DayOfWeek.FRIDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
    TimetableEntry("tt_22", DayOfWeek.FRIDAY, 3, "10:15 AM", "11:00 AM", "Chemistry", "Dr. Anita Sharma", "Room 302", "Class 12-A"),
    TimetableEntry("tt_23", DayOfWeek.FRIDAY, 4, "11:00 AM", "11:45 AM", "Sports & Games", "Coach Raymond", "School Field", "Class 12-A"),

    TimetableEntry("tt_24", DayOfWeek.SATURDAY, 1, "08:30 AM", "09:30 AM", "Science & AI Club", "Prof. Sarah Jenkins", "Auditorium", "Class 12-A"),
    TimetableEntry("tt_25", DayOfWeek.SATURDAY, 2, "09:45 AM", "11:00 AM", "Senior House Assembly", "Fr. Joseph Mathew", "Campus Grounds", "Class 12-A")
  )

  val initialAttendanceRecords = listOf(
    // Class 12-A (Keerthivasan's class)
    AttendanceRecord("att_1201", "std_1201", "Keerthivasan", 1, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_1202", "std_1202", "Kavya Sundaram", 2, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_1203", "std_1203", "Varun Raghavan", 3, "Class 12-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Departed 12:30 PM (Medical appointment)"),
    AttendanceRecord("att_1204", "std_1204", "Ananya Iyer", 4, "Class 12-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "Inter-School Science Olympiad (OD Approved)"),
    AttendanceRecord("att_1205", "std_1205", "Rohan Verma", 5, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_1206", "std_1206", "Sneha Nair", 6, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    // Class 10-A
    AttendanceRecord("att_1", "std_101", "Alex Johnson", 1, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_2", "std_102", "Bella Collins", 2, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_3", "std_103", "Christian Davies", 3, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Departed 12:30 PM (Medical appointment)"),
    AttendanceRecord("att_4", "std_104", "Daniel Evans", 4, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "Inter-School Science Olympiad (OD Approved)"),
    AttendanceRecord("att_5", "std_105", "Emma Foster", 5, "Class 10-A", "Today", AttendanceStatus.ABSENT, "Prof. Sarah Jenkins (Class Teacher)", "Sick leave (Parent letter received)"),
    AttendanceRecord("att_6", "std_106", "Felix Gomez", 6, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_7", "std_107", "Grace Howard", 7, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_8", "std_108", "Henry Irwin", 8, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "State Basketball Championship (OD Approved)"),
    AttendanceRecord("att_9", "std_109", "Isabella Jackson", 9, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_10", "std_110", "Jacob Klein", 10, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    AttendanceRecord("att_11", "std_111", "Lily Morris", 11, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Morning Session Only (Family event)"),
    AttendanceRecord("att_12", "std_112", "Noah Parker", 12, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
    // Class 10-B
    AttendanceRecord("att_201", "std_201", "Aaron Cooper", 1, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
    AttendanceRecord("att_202", "std_202", "Brianna Diaz", 2, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
    AttendanceRecord("att_203", "std_203", "Chloe Edwards", 3, "Class 10-B", "Today", AttendanceStatus.ON_DUTY, "Mr. David Miller (Class Teacher)", "Debate Competition"),
    AttendanceRecord("att_204", "std_204", "Dylan Flores", 4, "Class 10-B", "Today", AttendanceStatus.ABSENT, "Mr. David Miller (Class Teacher)", "Fever"),
    AttendanceRecord("att_205", "std_205", "Elena Garcia", 5, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
    // Class 9-A
    AttendanceRecord("att_301", "std_301", "Adrian Hughes", 1, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
    AttendanceRecord("att_302", "std_302", "Brooke Jenkins", 2, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
    AttendanceRecord("att_303", "std_303", "Caleb Kelly", 3, "Class 9-A", "Today", AttendanceStatus.HALF_DAY, "Mrs. Clara Higgins (Class Teacher)", "Afternoon appointment"),
    AttendanceRecord("att_304", "std_304", "Daisy Lewis", 4, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
    // Class 11-Science
    AttendanceRecord("att_401", "std_401", "Alexander Scott", 1, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)"),
    AttendanceRecord("att_402", "std_402", "Benjamin Ward", 2, "Class 11-Science", "Today", AttendanceStatus.ON_DUTY, "Dr. Rachel Green (Class Teacher)", "Robotics Expo Prep"),
    AttendanceRecord("att_403", "std_403", "Charlotte Young", 3, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)")
  )

  val initialDuties = listOf(
    DutyTask("d_1", "Science Expo Main Stage Audio & Projector Setup", "School Auditorium", "08:00 AM", DutyStatus.IN_PROGRESS, "Robert Taylor", DutyPriority.HIGH),
    DutyTask("d_2", "Physics & Chemistry Labs Safety Inspection", "Science Block Floor 2", "10:30 AM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.HIGH),
    DutyTask("d_3", "School Bus Fleet GPS & First Aid Kits Check", "Transport Bay", "01:00 PM", DutyStatus.COMPLETED, "Robert Taylor", DutyPriority.MEDIUM),
    DutyTask("d_4", "Generator & Power Backup Routine Test", "Utility Room", "03:30 PM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.MEDIUM),
    DutyTask("d_5", "Campus Fire Extinguisher Monthly Audit", "All Blocks", "04:00 PM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.LOW)
  )

  val initialEvents = listOf(
    SchoolEvent("ev_1", "Science & Tech Expo 2026", "Grand exhibition featuring robotics, physics simulations, and environmental science models.", "20 Aug 2026", "09:00 AM - 04:00 PM", "Auditorium & Labs", "Expo"),
    SchoolEvent("ev_2", "Independence Day Parade", "Flag hoisting, band performance, patriotic choir, and address by the Principal.", "15 Aug 2026", "08:00 AM - 10:30 AM", "Central Ground", "Ceremony"),
    SchoolEvent("ev_3", "Inter-House Debate Championship", "Topic: 'Artificial Intelligence in Modern Education: Opportunity or Challenge?'", "25 Aug 2026", "01:30 PM - 03:30 PM", "AV Hall", "Academics"),
    SchoolEvent("ev_4", "Parent-Teacher Conference (Mid-Term)", "Interactive review of academic progress and attendance records for Term 1.", "05 Sep 2026", "08:30 AM - 01:30 PM", "Respective Classrooms", "Meeting")
  )

  val initialClasses = listOf(
    SchoolClass("cls_12a", "Class 12", "A", "Room 302", "Prof. Sarah Jenkins", 28, 96.5),
    SchoolClass("cls_10a", "Class 10", "A", "Room 204", "Prof. Sarah Jenkins", 32, 95.2),
    SchoolClass("cls_10b", "Class 10", "B", "Room 205", "Mr. David Miller", 30, 93.8),
    SchoolClass("cls_9a", "Class 9", "A", "Room 102", "Mrs. Clara Higgins", 34, 96.0),
    SchoolClass("cls_9b", "Class 9", "B", "Room 103", "Dr. Anita Sharma", 33, 94.1),
    SchoolClass("cls_11sci", "Class 11", "Science", "Room 301", "Dr. Rachel Green", 28, 92.5),
    SchoolClass("cls_12sci", "Class 12", "Science", "Room 302", "Mr. Kevin Ross", 26, 96.8)
  )
}
