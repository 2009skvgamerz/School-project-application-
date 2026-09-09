package com.example.model

/**
 * Data models for visualizing student academic performance trends and attendance metrics.
 * Designed for D3.js and Recharts-style interactive charting engines.
 */
data class AcademicTermTrend(
  val termId: String,
  val termName: String,
  val shortName: String,
  val studentScore: Double,
  val classAverage: Double,
  val gpa: Double,
  val date: String,
  val gradeLetter: String
)

data class MonthlyAttendanceTrend(
  val monthName: String,
  val shortMonth: String,
  val attendanceRate: Double,
  val workingDays: Int,
  val presentDays: Double,
  val isCurrent: Boolean = false
)

data class SubjectPerformance(
  val subjectName: String,
  val scorePercentage: Double,
  val attendanceRate: Double,
  val highestScore: Double,
  val teacherName: String,
  val colorHex: Long
)

data class AttendanceDistribution(
  val fullDays: Int,
  val halfDays: Int,
  val onDutyDays: Int,
  val absentDays: Int,
  val overallPercentage: Double,
  val examEligibilityThreshold: Double = 75.0
)

data class StudentAnalyticsProfile(
  val studentName: String,
  val gradeAndSection: String,
  val rollNo: Int,
  val currentGpa: Double,
  val overallAttendance: Double,
  val terms: List<AcademicTermTrend>,
  val monthlyAttendance: List<MonthlyAttendanceTrend>,
  val subjects: List<SubjectPerformance>,
  val attendanceDistribution: AttendanceDistribution
) {
  companion object {
    val defaultStudentProfile = StudentAnalyticsProfile(
      studentName = "Alex Johnson",
      gradeAndSection = "Class 10-A",
      rollNo = 1,
      currentGpa = 9.45,
      overallAttendance = 97.5,
      terms = listOf(
        AcademicTermTrend(
          termId = "term_1",
          termName = "Unit Test 1",
          shortName = "UT-1",
          studentScore = 88.5,
          classAverage = 76.2,
          gpa = 8.85,
          date = "Jul 2026",
          gradeLetter = "A"
        ),
        AcademicTermTrend(
          termId = "term_2",
          termName = "Quarterly Exam",
          shortName = "Quarterly",
          studentScore = 91.0,
          classAverage = 78.4,
          gpa = 9.10,
          date = "Sep 2026",
          gradeLetter = "A+"
        ),
        AcademicTermTrend(
          termId = "term_3",
          termName = "Mid-Term Exam",
          shortName = "Mid-Term",
          studentScore = 89.5,
          classAverage = 77.1,
          gpa = 8.95,
          date = "Nov 2026",
          gradeLetter = "A"
        ),
        AcademicTermTrend(
          termId = "term_4",
          termName = "Half-Yearly Exam",
          shortName = "Half-Yearly",
          studentScore = 94.0,
          classAverage = 80.3,
          gpa = 9.40,
          date = "Dec 2026",
          gradeLetter = "A+"
        ),
        AcademicTermTrend(
          termId = "term_5",
          termName = "Unit Test 2",
          shortName = "UT-2",
          studentScore = 96.5,
          classAverage = 81.8,
          gpa = 9.65,
          date = "Jan 2027",
          gradeLetter = "O (Outstanding)"
        ),
        AcademicTermTrend(
          termId = "term_6",
          termName = "Model Pre-Board",
          shortName = "Pre-Board",
          studentScore = 95.0,
          classAverage = 82.5,
          gpa = 9.50,
          date = "Feb 2027",
          gradeLetter = "O (Outstanding)"
        )
      ),
      monthlyAttendance = listOf(
        MonthlyAttendanceTrend("June 2026", "Jun", 98.0, 22, 21.5),
        MonthlyAttendanceTrend("July 2026", "Jul", 96.5, 24, 23.0),
        MonthlyAttendanceTrend("August 2026", "Aug", 94.0, 21, 19.5),
        MonthlyAttendanceTrend("September 2026", "Sep", 98.5, 23, 22.5),
        MonthlyAttendanceTrend("October 2026", "Oct", 95.0, 20, 19.0),
        MonthlyAttendanceTrend("November 2026", "Nov", 97.0, 22, 21.5),
        MonthlyAttendanceTrend("December 2026", "Dec", 93.5, 18, 17.0),
        MonthlyAttendanceTrend("January 2027", "Jan", 99.0, 22, 21.8),
        MonthlyAttendanceTrend("February 2027", "Feb", 96.0, 21, 20.0),
        MonthlyAttendanceTrend("March 2027", "Mar", 97.5, 22, 21.5, isCurrent = true)
      ),
      subjects = listOf(
        SubjectPerformance("Mathematics", 98.0, 98.0, 100.0, "Prof. Sarah Jenkins", 0xFF2563EB),
        SubjectPerformance("Physics", 92.5, 96.5, 95.0, "Dr. Rachel Green", 0xFF0284C7),
        SubjectPerformance("Chemistry", 89.0, 95.0, 94.0, "Dr. Anita Sharma", 0xFF059669),
        SubjectPerformance("Computer Science", 99.0, 100.0, 99.0, "Prof. Sarah Jenkins", 0xFF7C3AED),
        SubjectPerformance("English Literature", 91.5, 94.0, 96.0, "Mrs. Clara Higgins", 0xFFD97706),
        SubjectPerformance("Tamil / Language", 94.0, 97.0, 97.0, "Mr. Kevin Ross", 0xFFE11D48)
      ),
      attendanceDistribution = AttendanceDistribution(
        fullDays = 78,
        halfDays = 4,
        onDutyDays = 4,
        absentDays = 2,
        overallPercentage = 97.5,
        examEligibilityThreshold = 75.0
      )
    )

    val classAggregateProfile = StudentAnalyticsProfile(
      studentName = "Class 10-A (Aggregate)",
      gradeAndSection = "Class 10-A",
      rollNo = 0,
      currentGpa = 8.15,
      overallAttendance = 94.2,
      terms = listOf(
        AcademicTermTrend("term_1", "Unit Test 1", "UT-1", 76.2, 76.2, 7.62, "Jul 2026", "B+"),
        AcademicTermTrend("term_2", "Quarterly Exam", "Quarterly", 78.4, 78.4, 7.84, "Sep 2026", "B+"),
        AcademicTermTrend("term_3", "Mid-Term Exam", "Mid-Term", 77.1, 77.1, 7.71, "Nov 2026", "B+"),
        AcademicTermTrend("term_4", "Half-Yearly Exam", "Half-Yearly", 80.3, 80.3, 8.03, "Dec 2026", "A"),
        AcademicTermTrend("term_5", "Unit Test 2", "UT-2", 81.8, 81.8, 8.18, "Jan 2027", "A"),
        AcademicTermTrend("term_6", "Model Pre-Board", "Pre-Board", 82.5, 82.5, 8.25, "Feb 2027", "A")
      ),
      monthlyAttendance = listOf(
        MonthlyAttendanceTrend("June 2026", "Jun", 95.2, 22, 20.9),
        MonthlyAttendanceTrend("July 2026", "Jul", 94.8, 24, 22.7),
        MonthlyAttendanceTrend("August 2026", "Aug", 93.1, 21, 19.5),
        MonthlyAttendanceTrend("September 2026", "Sep", 95.6, 23, 22.0),
        MonthlyAttendanceTrend("October 2026", "Oct", 93.8, 20, 18.7),
        MonthlyAttendanceTrend("November 2026", "Nov", 94.2, 22, 20.7),
        MonthlyAttendanceTrend("December 2026", "Dec", 92.0, 18, 16.5),
        MonthlyAttendanceTrend("January 2027", "Jan", 95.5, 22, 21.0),
        MonthlyAttendanceTrend("February 2027", "Feb", 93.7, 21, 19.6),
        MonthlyAttendanceTrend("March 2027", "Mar", 94.2, 22, 20.7, isCurrent = true)
      ),
      subjects = listOf(
        SubjectPerformance("Mathematics", 82.0, 94.0, 100.0, "Prof. Sarah Jenkins", 0xFF2563EB),
        SubjectPerformance("Physics", 79.5, 93.5, 95.0, "Dr. Rachel Green", 0xFF0284C7),
        SubjectPerformance("Chemistry", 81.0, 92.5, 94.0, "Dr. Anita Sharma", 0xFF059669),
        SubjectPerformance("Computer Science", 88.5, 96.0, 99.0, "Prof. Sarah Jenkins", 0xFF7C3AED),
        SubjectPerformance("English Literature", 83.0, 93.0, 96.0, "Mrs. Clara Higgins", 0xFFD97706),
        SubjectPerformance("Tamil / Language", 85.0, 95.5, 97.0, "Mr. Kevin Ross", 0xFFE11D48)
      ),
      attendanceDistribution = AttendanceDistribution(
        fullDays = 2150,
        halfDays = 120,
        onDutyDays = 85,
        absentDays = 95,
        overallPercentage = 94.2,
        examEligibilityThreshold = 75.0
      )
    )
  }
}
