package com.example.data.seed

import com.example.model.*

object TransportAndEventsSeedData {

  val initialCalendarEvents = listOf(
    CalendarEvent(
      id = "evt_1",
      title = "Independence Day & Cultural Assembly",
      description = "Special patriotic flag hoisting ceremony followed by patriotic song & dance performances by middle and high school students.",
      date = "2026-08-15",
      formattedDate = "Sat, 15 Aug 2026",
      time = "08:30 AM - 11:30 AM",
      location = "Main Campus Grounds & Flagpost",
      category = CalendarCategory.HOLIDAY,
      isHoliday = true,
      targetGrades = "All Grades (1 - 12)",
      organizer = "Cultural Affairs Committee",
      hasReminder = true
    ),
    CalendarEvent(
      id = "evt_2",
      title = "Annual Science, AI & Robotics Expo 2026",
      description = "Showcase of over 85 working models, robotics automation exhibits, and research papers from Grades 9-12. Parents & visitors welcome.",
      date = "2026-08-20",
      formattedDate = "Thu, 20 Aug 2026",
      time = "09:30 AM - 04:30 PM",
      location = "Main Auditorium & STEM Innovation Hub",
      category = CalendarCategory.ACADEMIC,
      isHoliday = false,
      targetGrades = "Grades 9 to 12",
      organizer = "STEM Faculty Council",
      hasReminder = true
    ),
    CalendarEvent(
      id = "evt_3",
      title = "Teacher Professional Development Day",
      description = "Workshops on modern pedagogies, AI tools in classroom teaching, and student mental wellness counseling.",
      date = "2026-08-25",
      formattedDate = "Tue, 25 Aug 2026",
      time = "01:30 PM - 04:30 PM",
      location = "AV Conference Hall 1",
      category = CalendarCategory.MEETING,
      isHoliday = false,
      targetGrades = "Faculty & Staff Only",
      organizer = "Dean of Academics"
    ),
    CalendarEvent(
      id = "evt_4",
      title = "Parent-Teacher Consultation (PTM) - Term 1",
      description = "One-on-one 10-minute performance review consultations between parents and class subject teachers.",
      date = "2026-08-29",
      formattedDate = "Sat, 29 Aug 2026",
      time = "09:00 AM - 01:30 PM",
      location = "Respective Classrooms (Blocks A & B)",
      category = CalendarCategory.MEETING,
      isHoliday = false,
      targetGrades = "All Classes (Pre-KG to 12)",
      organizer = "Academic Affairs Directorate",
      hasReminder = true
    ),
    CalendarEvent(
      id = "evt_5",
      title = "Term 1 Mid-Year Board Preparatory Exams",
      description = "Mid-term comprehensive assessments covering 50% prescribed syllabus for CBSE/ICSE Board batches.",
      date = "2026-09-01",
      formattedDate = "01 Sep - 12 Sep 2026",
      time = "09:00 AM - 12:30 PM",
      location = "Examination Halls A, B & Science Labs",
      category = CalendarCategory.EXAM,
      isHoliday = false,
      targetGrades = "Classes 10, 11 & 12",
      organizer = "Central Examination Cell"
    ),
    CalendarEvent(
      id = "evt_6",
      title = "Inter-School Athletics & Sports Championship",
      description = "Track & field, sprint relays, basketball finals, and inter-house championship matches.",
      date = "2026-09-18",
      formattedDate = "Fri, 18 Sep 2026",
      time = "08:00 AM - 05:00 PM",
      location = "St. Joseph's Sports Stadium & Courts",
      category = CalendarCategory.SPORTS,
      isHoliday = false,
      targetGrades = "Athletes & House Squads",
      organizer = "Physical Education Department"
    ),
    CalendarEvent(
      id = "evt_7",
      title = "Annual Cultural Gala 'Euphoria 2026'",
      description = "Grand music, drama, choir concert, and art gallery exhibition celebrating student artistic brilliance.",
      date = "2026-09-26",
      formattedDate = "Sat, 26 Sep 2026",
      time = "04:30 PM - 08:30 PM",
      location = "Open Air Amphitheatre",
      category = CalendarCategory.CULTURAL,
      isHoliday = false,
      targetGrades = "Entire School Community",
      organizer = "Student Union & Arts Council"
    )
  )

  val initialBusRoutes = listOf(
    BusRoute(
      id = "route_12",
      routeNumber = "Route #12",
      routeName = "SIPCOT - Gandhi Nagar Express",
      busRegistration = "TN-70-SJ-1012",
      driverName = "Mr. Ramesh Kumar",
      driverPhone = "+91 98451 22334",
      attendantName = "Mrs. Sunita Devi",
      attendantPhone = "+91 98451 99881",
      currentSpeedKmH = 36,
      currentLocationName = "Gandhi Nagar Main Circle",
      nextStopName = "Mookondapalli Junction (Stop 3)",
      estimatedArrivalMins = 6,
      status = BusStatus.ON_TIME,
      delayMinutes = 0,
      capacity = 45,
      studentsOnboard = 34,
      progressPercent = 0.45f,
      currentLatitude = 12.7435,
      currentLongitude = 77.8120,
      currentHeadingDegrees = 72f,
      schoolLatitude = 12.74632,
      schoolLongitude = 77.80728,
      stops = listOf(
        BusStop(
          id = "st_12_1",
          name = "Hosur Bus Stand Circle",
          scheduledTime = "07:15 AM",
          isCompleted = true,
          studentCount = 4,
          latitude = 12.7410,
          longitude = 77.8250,
          passengers = listOf(
            BusPassenger("p_12_1_1", "Keerthivasan S", UserRole.STUDENT, "Class 12-A", 1, "S. Sundar", "+91 98450 78912", "+91 98450 78912", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:14 AM"),
            BusPassenger("p_12_1_2", "Aarav Patel", UserRole.STUDENT, "Class 10-B", 4, "Vikram Patel", "+91 98450 78920", "+91 98450 78920", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:15 AM"),
            BusPassenger("p_12_1_3", "Prof. Sarah Jenkins", UserRole.TEACHER, "Science Faculty", null, "Self", "+91 98765 22001", "+91 98765 22001", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:13 AM"),
            BusPassenger("p_12_1_4", "Meera Nair", UserRole.STUDENT, "Class 9-A", 14, "Gopal Nair", "+91 98450 78933", "+91 98450 78933", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:15 AM")
          )
        ),
        BusStop(
          id = "st_12_2",
          name = "Gandhi Nagar Junction",
          scheduledTime = "07:30 AM",
          isCompleted = true,
          isCurrent = true,
          studentCount = 5,
          latitude = 12.7435,
          longitude = 77.8120,
          passengers = listOf(
            BusPassenger("p_12_2_1", "Rahul Sharma", UserRole.STUDENT, "Class 12-A", 2, "Rajesh Sharma", "+91 98450 78913", "+91 98450 78913", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:29 AM"),
            BusPassenger("p_12_2_2", "Ananya Verma", UserRole.STUDENT, "Class 12-A", 3, "Col. K. Verma", "+91 98450 78914", "+91 98450 78914", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:30 AM"),
            BusPassenger("p_12_2_3", "Devansh Joshi", UserRole.STUDENT, "Class 11-B", 8, "Sanjay Joshi", "+91 98450 78945", "+91 98450 78945", PassengerBoardingStatus.WAITING, "st_12_2", "Gandhi Nagar Junction"),
            BusPassenger("p_12_2_4", "Dr. Emily Watson", UserRole.TEACHER, "English Faculty", null, "Self", "+91 98765 22003", "+91 98765 22003", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:28 AM"),
            BusPassenger("p_12_2_5", "Sneha Rao", UserRole.STUDENT, "Class 8-C", 19, "M. Rao", "+91 98450 78950", "+91 98450 78950", PassengerBoardingStatus.ABSENT, "st_12_2", "Gandhi Nagar Junction")
          )
        ),
        BusStop(
          id = "st_12_3",
          name = "Mookondapalli Ring Road",
          scheduledTime = "07:45 AM",
          isCompleted = false,
          studentCount = 4,
          latitude = 12.7450,
          longitude = 77.8090,
          passengers = listOf(
            BusPassenger("p_12_3_1", "Aditya Krishnan", UserRole.STUDENT, "Class 10-A", 6, "S. Krishnan", "+91 98450 78955", "+91 98450 78955", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
            BusPassenger("p_12_3_2", "Pooja Reddy", UserRole.STUDENT, "Class 11-A", 12, "V. Reddy", "+91 98450 78960", "+91 98450 78960", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
            BusPassenger("p_12_3_3", "Rohan Pillai", UserRole.STUDENT, "Class 12-B", 15, "T. Pillai", "+91 98450 78965", "+91 98450 78965", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
            BusPassenger("p_12_3_4", "Mr. David Miller", UserRole.TEACHER, "Mathematics Faculty", null, "Self", "+91 98765 22002", "+91 98765 22002", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road")
          )
        ),
        BusStop(
          id = "st_12_4",
          name = "SIPCOT Phase-1 Gate",
          scheduledTime = "08:00 AM",
          isCompleted = false,
          studentCount = 3,
          latitude = 12.7460,
          longitude = 77.8080,
          passengers = listOf(
            BusPassenger("p_12_4_1", "Tanvi Sengupta", UserRole.STUDENT, "Class 9-B", 22, "A. Sengupta", "+91 98450 78970", "+91 98450 78970", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate"),
            BusPassenger("p_12_4_2", "Varun Chopra", UserRole.STUDENT, "Class 12-A", 9, "N. Chopra", "+91 98450 78975", "+91 98450 78975", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate"),
            BusPassenger("p_12_4_3", "Riya Sen", UserRole.STUDENT, "Class 10-A", 18, "B. Sen", "+91 98450 78980", "+91 98450 78980", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate")
          )
        ),
        BusStop(
          id = "st_12_5",
          name = "St. Joseph Matriculation Hr. Sec. School (Campus)",
          scheduledTime = "08:15 AM",
          isCompleted = false,
          studentCount = 0,
          latitude = 12.74632,
          longitude = 77.80728,
          passengers = emptyList()
        )
      )
    ),
    BusRoute(
      id = "route_05",
      routeNumber = "Route #05",
      routeName = "Bagalur Road - Mathigiri Shuttle",
      busRegistration = "TN-70-SJ-1005",
      driverName = "Mr. Suresh Babu",
      driverPhone = "+91 98452 33445",
      attendantName = "Mrs. Mary D'Souza",
      attendantPhone = "+91 98452 88772",
      currentSpeedKmH = 24,
      currentLocationName = "Mathigiri Junction",
      nextStopName = "Mookondapalli Cross (Stop 4)",
      estimatedArrivalMins = 11,
      status = BusStatus.DELAYED,
      delayMinutes = 5,
      delayReason = "Overbridge maintenance detour",
      capacity = 45,
      studentsOnboard = 38,
      progressPercent = 0.52f,
      currentLatitude = 12.7300,
      currentLongitude = 77.8200,
      currentHeadingDegrees = 240f,
      schoolLatitude = 12.74632,
      schoolLongitude = 77.80728,
      activeDetourAlert = "🚧 Detour active: Stop 3 diverted via Mathigiri Main Rd.",
      stops = listOf(
        BusStop(
          id = "st_05_1",
          name = "Bagalur Road Circle",
          scheduledTime = "07:10 AM",
          isCompleted = true,
          studentCount = 4,
          latitude = 12.7250,
          longitude = 77.8350,
          passengers = listOf(
            BusPassenger("p_05_1_1", "Kavya Menon", UserRole.STUDENT, "Class 11-A", 5, "S. Menon", "+91 98452 00101", "+91 98452 00101", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:09 AM"),
            BusPassenger("p_05_1_2", "Siddharth Roy", UserRole.STUDENT, "Class 12-B", 7, "P. Roy", "+91 98452 00102", "+91 98452 00102", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:10 AM"),
            BusPassenger("p_05_1_3", "Arjun Nambiar", UserRole.STUDENT, "Class 9-A", 11, "K. Nambiar", "+91 98452 00103", "+91 98452 00103", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:11 AM"),
            BusPassenger("p_05_1_4", "Mrs. Maya Pillai", UserRole.TEACHER, "Biology Faculty", null, "Self", "+91 98765 22004", "+91 98765 22004", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:08 AM")
          )
        ),
        BusStop(
          id = "st_05_2",
          name = "Mathigiri Cattle Farm Rd",
          scheduledTime = "07:25 AM",
          isCompleted = true,
          studentCount = 3,
          latitude = 12.7280,
          longitude = 77.8280,
          passengers = listOf(
            BusPassenger("p_05_2_1", "Neha Hegde", UserRole.STUDENT, "Class 10-B", 14, "U. Hegde", "+91 98452 00201", "+91 98452 00201", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:24 AM"),
            BusPassenger("p_05_2_2", "Aryan Gupta", UserRole.STUDENT, "Class 8-A", 3, "R. Gupta", "+91 98452 00202", "+91 98452 00202", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:25 AM"),
            BusPassenger("p_05_2_3", "Ishaan Kapoor", UserRole.STUDENT, "Class 12-A", 17, "S. Kapoor", "+91 98452 00203", "+91 98452 00203", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:26 AM")
          )
        ),
        BusStop(
          id = "st_05_3",
          name = "Mathigiri Junction",
          scheduledTime = "07:42 AM",
          isCompleted = true,
          isCurrent = true,
          studentCount = 3,
          latitude = 12.7300,
          longitude = 77.8200,
          passengers = listOf(
            BusPassenger("p_05_3_1", "Divya Suresh", UserRole.STUDENT, "Class 10-A", 10, "K. Suresh", "+91 98452 00301", "+91 98452 00301", PassengerBoardingStatus.BOARDED, "st_05_3", "Mathigiri Junction", "07:41 AM"),
            BusPassenger("p_05_3_2", "Pranav Nair", UserRole.STUDENT, "Class 11-B", 21, "C. Nair", "+91 98452 00302", "+91 98452 00302", PassengerBoardingStatus.WAITING, "st_05_3", "Mathigiri Junction"),
            BusPassenger("p_05_3_3", "Gauri Deshmukh", UserRole.STUDENT, "Class 9-C", 16, "A. Deshmukh", "+91 98452 00303", "+91 98452 00303", PassengerBoardingStatus.ABSENT, "st_05_3", "Mathigiri Junction")
          )
        ),
        BusStop(
          id = "st_05_4",
          name = "Mookondapalli Cross",
          scheduledTime = "07:58 AM",
          isCompleted = false,
          studentCount = 2,
          latitude = 12.7420,
          longitude = 77.8110,
          passengers = listOf(
            BusPassenger("p_05_4_1", "Manish Tiwari", UserRole.STUDENT, "Class 12-A", 25, "H. Tiwari", "+91 98452 00401", "+91 98452 00401", PassengerBoardingStatus.WAITING, "st_05_4", "Mookondapalli Cross"),
            BusPassenger("p_05_4_2", "Shruti Rao", UserRole.STUDENT, "Class 7-B", 8, "V. Rao", "+91 98452 00402", "+91 98452 00402", PassengerBoardingStatus.WAITING, "st_05_4", "Mookondapalli Cross")
          )
        ),
        BusStop(
          id = "st_05_5",
          name = "St. Joseph Matriculation Hr. Sec. School (Campus)",
          scheduledTime = "08:18 AM",
          isCompleted = false,
          studentCount = 0,
          latitude = 12.74632,
          longitude = 77.80728,
          passengers = emptyList()
        )
      )
    ),
    BusRoute(
      id = "route_08",
      routeNumber = "Route #08",
      routeName = "Zuzuvadi - SIPCOT Line",
      busRegistration = "TN-70-SJ-1008",
      driverName = "Mr. Mohan Raj",
      driverPhone = "+91 98453 44556",
      attendantName = "Mrs. Rekha Sharma",
      attendantPhone = "+91 98453 77663",
      currentSpeedKmH = 42,
      currentLocationName = "Zuzuvadi Checkpost Rd",
      nextStopName = "SIPCOT Phase 2 Gate (Stop 4)",
      estimatedArrivalMins = 5,
      status = BusStatus.ON_TIME,
      delayMinutes = 0,
      capacity = 45,
      studentsOnboard = 41,
      progressPercent = 0.78f,
      currentLatitude = 12.7550,
      currentLongitude = 77.7950,
      currentHeadingDegrees = 45f,
      schoolLatitude = 12.74632,
      schoolLongitude = 77.80728,
      stops = listOf(
        BusStop("st_08_1", "Zuzuvadi Flyover", "07:05 AM", isCompleted = true, studentCount = 3, latitude = 12.7600, longitude = 77.7900),
        BusStop("st_08_2", "Moranapalli Junction", "07:22 AM", isCompleted = true, studentCount = 4, latitude = 12.7580, longitude = 77.7920),
        BusStop("st_08_3", "Zuzuvadi Checkpost", "07:38 AM", isCompleted = true, isCurrent = true, studentCount = 3, latitude = 12.7550, longitude = 77.7950),
        BusStop("st_08_4", "SIPCOT Phase 2 Gate", "07:50 AM", isCompleted = false, studentCount = 2, latitude = 12.7500, longitude = 77.8020),
        BusStop("st_08_5", "St. Joseph Matriculation Hr. Sec. School (Campus)", "08:05 AM", isCompleted = false, studentCount = 0, latitude = 12.74632, longitude = 77.80728)
      )
    ),
    BusRoute(
      id = "route_15",
      routeNumber = "Route #15",
      routeName = "Kamaraj Nagar - SIPCOT Express",
      busRegistration = "TN-70-SJ-1015",
      driverName = "Mr. Anand Paul",
      driverPhone = "+91 98454 55667",
      attendantName = "Mrs. Geeta Nayak",
      attendantPhone = "+91 98454 66554",
      currentSpeedKmH = 31,
      currentLocationName = "Kamaraj Nagar Main Rd",
      nextStopName = "Gandhi Nagar Cross (Stop 4)",
      estimatedArrivalMins = 9,
      status = BusStatus.ON_TIME,
      delayMinutes = 0,
      capacity = 45,
      studentsOnboard = 36,
      progressPercent = 0.68f,
      currentLatitude = 12.7380,
      currentLongitude = 77.8180,
      currentHeadingDegrees = 30f,
      schoolLatitude = 12.74632,
      schoolLongitude = 77.80728,
      stops = listOf(
        BusStop("st_15_1", "Rayakottai Road Junction", "07:00 AM", isCompleted = true, studentCount = 4, latitude = 12.7300, longitude = 77.8300),
        BusStop("st_15_2", "Kamaraj Nagar Bus Stop", "07:18 AM", isCompleted = true, studentCount = 5, latitude = 12.7350, longitude = 77.8220),
        BusStop("st_15_3", "Kamaraj Nagar Main Rd", "07:35 AM", isCompleted = true, isCurrent = true, studentCount = 3, latitude = 12.7380, longitude = 77.8180),
        BusStop("st_15_4", "Gandhi Nagar Cross", "07:52 AM", isCompleted = false, studentCount = 2, latitude = 12.7430, longitude = 77.8120),
        BusStop("st_15_5", "St. Joseph Matriculation Hr. Sec. School (Campus)", "08:15 AM", isCompleted = false, studentCount = 0, latitude = 12.74632, longitude = 77.80728)
      )
    )
  )

  val initialAnnouncements = listOf(
    SchoolAnnouncement(
      id = "ann_1",
      title = "Severe Weather Alert: Early Bus Departure & Sports Session Rescheduling",
      content = "Attention all parents and students: Due to heavy precipitation warnings issued by civic authorities, all evening outdoor coaching and clubs are suspended today. School buses will commence return trips starting at 03:00 PM. Parents picking up wards via private transit are requested to arrive at Gate 2.",
      priority = AnnouncementPriority.URGENT,
      targetAudience = AnnouncementAudience.ALL_SCHOOL,
      date = "22 Aug 2026",
      timeAgo = "15m ago",
      authorName = "Dr. Arthur Pendelton",
      authorRole = "Principal",
      isEmergency = true,
      audioDurationSec = 38,
      acknowledgedByCurrentUser = false,
      acknowledgmentsCount = 184
    ),
    SchoolAnnouncement(
      id = "ann_2",
      title = "Annual Science & Tech Expo 2026: Project Exhibits & Hall Layout",
      content = "Final booth allotments for Grade 11 & 12 robotics, clean energy models, and software prototypes have been published. Team captains must complete electrical safety checks with Mr. Robert Taylor in Lab Prep 2 by 4:00 PM Thursday.",
      priority = AnnouncementPriority.HIGH,
      targetAudience = AnnouncementAudience.SENIOR_SECONDARY,
      date = "21 Aug 2026",
      timeAgo = "3h ago",
      authorName = "Prof. Sarah Jenkins",
      authorRole = "Lead Science Faculty",
      isEmergency = false,
      attachmentName = "Expo_Floor_Plan_2026.pdf",
      acknowledgedByCurrentUser = true,
      acknowledgmentsCount = 76
    ),
    SchoolAnnouncement(
      id = "ann_3",
      title = "Parent-Teacher Consultation (PTM) Online Slot Booking Open",
      content = "Online appointment booking for the Term 1 Parent-Teacher Conference (Saturday, 29 Aug) is now open. Parents can reserve individual 10-minute consultation slots with class educators and subject specialists.",
      priority = AnnouncementPriority.GENERAL,
      targetAudience = AnnouncementAudience.PARENTS_ONLY,
      date = "20 Aug 2026",
      timeAgo = "Yesterday",
      authorName = "Academic Affairs Bureau",
      authorRole = "Administration",
      isEmergency = false,
      acknowledgedByCurrentUser = true,
      acknowledgmentsCount = 210
    ),
    SchoolAnnouncement(
      id = "ann_4",
      title = "Transport Route #05 Temporary Road Diversion Notice",
      content = "Due to civic drainage maintenance on Kundalahalli Gate main road, Bus Route #05 will detour via AECS Layout for the next 3 working days. Morning pickup times at stops 1 and 2 will be 5 minutes earlier.",
      priority = AnnouncementPriority.HIGH,
      targetAudience = AnnouncementAudience.ALL_SCHOOL,
      date = "19 Aug 2026",
      timeAgo = "2 days ago",
      authorName = "Mr. Thomas Wright",
      authorRole = "Campus Operations Supervisor",
      isEmergency = false,
      acknowledgedByCurrentUser = false,
      acknowledgmentsCount = 112
    )
  )
}
