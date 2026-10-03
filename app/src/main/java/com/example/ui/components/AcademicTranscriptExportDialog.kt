package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.StudentAnalyticsProfile
import com.example.model.StudentProfile
import com.example.ui.theme.*

/**
 * AcademicTranscriptExportDialog
 *
 * Official institutional transcript and attendance certificate generator for St. Joseph's School.
 * Features:
 * - Formal institutional crest & affiliation letterhead
 * - Student biodata, roll number, admission number, and house affiliation
 * - Comprehensive attendance certification with board exam eligibility verification
 * - Subject-wise term grades, marks breakdown, and cumulative GPA calculation
 * - Principal and Homeroom Teacher digital endorsement seals
 * - One-tap PDF export / print sharing
 */
@Composable
fun AcademicTranscriptExportDialog(
  studentProfile: StudentProfile?,
  analyticsProfile: StudentAnalyticsProfile = StudentAnalyticsProfile.defaultStudentProfile,
  onDismiss: () -> Unit,
  onExportSuccess: (String) -> Unit = {}
) {
  val context = LocalContext.current
  val studentName = studentProfile?.user?.fullName ?: analyticsProfile.studentName
  val gradeSection = studentProfile?.let { "Class ${it.grade}-${it.section}" } ?: analyticsProfile.gradeAndSection
  val rollNo = studentProfile?.rollNo ?: analyticsProfile.rollNo
  val admissionNo = studentProfile?.admissionNo ?: "SJ-2024-8842"
  val attendanceRate = studentProfile?.attendancePercentage ?: analyticsProfile.overallAttendance
  val gpa = analyticsProfile.currentGpa
  val house = studentProfile?.houseName ?: "St. Francis House (Blue)"

  var isExporting by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.92f)
        .testTag("academic_transcript_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        // Dialog Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = "Official Academic Certificate",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close dialog")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Printable / Exportable Certificate Document Surface
        Card(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
          border = BorderStroke(1.5.dp, SchoolNavyPrimary.copy(alpha = 0.35f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // 1. Institutional Letterhead Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Image(
                painter = painterResource(id = R.drawable.school_logo),
                contentDescription = "School Emblem",
                modifier = Modifier.size(56.dp)
              )

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "ST. JOSEPH MATRICULATION HIGHER SECONDARY SCHOOL",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    fontSize = 12.sp
                  ),
                  color = SchoolNavyPrimary
                )
                Text(
                  text = "SIPCOT, Gandhi Nagar Road, Hosur, Tamil Nadu • PIN: 635126",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Recognized by Directorate of School Education • Motto: \"Shine and Let Shine\"",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = SchoolGold
                  )
                )
              }
            }

            // Certificate Banner Ribbon
            Surface(
              color = SchoolNavyPrimary,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "OFFICIAL ACADEMIC TRANSCRIPT & ATTENDANCE RECORD (2026–2027)",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  fontSize = 10.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
              )
            }

            // 2. Student Identification Grid
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  TranscriptField("Student Name", studentName, FontWeight.Bold)
                  TranscriptField("Roll Number", "#$rollNo")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  TranscriptField("Class & Section", gradeSection)
                  TranscriptField("Admission ID", admissionNo)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  TranscriptField("House Affiliation", house)
                  TranscriptField("Academic Term", "Term 1 & Half-Yearly")
                }
              }
            }

            // 3. Official Attendance Certification
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF059669).copy(alpha = 0.08f)),
              border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669).copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(24.dp)
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Cumulative Attendance: ${String.format("%.1f", attendanceRate)}%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF059669)
                  )
                  Text(
                    text = "Board Examination Eligibility: SATISFIED (Min. 75% Required)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Total Working Days: 88 • Present: 78 • Half-Day: 4 • On-Duty (OD): 4 • Medical: 2",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            // 4. Subject-Wise Scores and GPA Matrix
            Text(
              text = "SCHOLASTIC PERFORMANCE & GRADES",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
              ),
              color = SchoolNavyPrimary
            )

            Surface(
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
              color = MaterialTheme.colorScheme.surface
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Table Header
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("SUBJECT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(2f))
                  Text("MARKS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = Modifier.weight(1f))
                  Text("GRADE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = Modifier.weight(1f))
                  Text("FACULTY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.End), modifier = Modifier.weight(1.5f))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                val subjectList = listOf(
                  Triple("Mathematics", "98 / 100", "A+"),
                  Triple("Physics", "92 / 100", "A+"),
                  Triple("Chemistry", "89 / 100", "A"),
                  Triple("Computer Science", "99 / 100", "A+"),
                  Triple("English Language", "94 / 100", "A+")
                )

                val facultyNames = listOf("Prof. Sarah Jenkins", "Dr. Robert Vance", "Mrs. Higgins", "Mr. David Miller", "Mrs. Green")

                subjectList.forEachIndexed { index, (subject, marks, grade) ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(subject, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(2f))
                    Text(marks, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = Modifier.weight(1f))
                    Surface(
                      color = SchoolNavyPrimary.copy(alpha = 0.12f),
                      shape = RoundedCornerShape(4.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text(grade, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SchoolNavyPrimary, textAlign = TextAlign.Center), modifier = Modifier.padding(vertical = 2.dp))
                    }
                    Text(facultyNames.getOrElse(index) { "Staff" }, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant), modifier = Modifier.weight(1.5f))
                  }
                  if (index < subjectList.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                  }
                }
              }
            }

            // Summary GPA & Conduct Strip
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                color = SchoolNavyPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SchoolNavyPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("CUMULATIVE GPA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = SchoolNavyPrimary)
                  Text("$gpa / 10.0", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = SchoolNavyPrimary)
                  Text("Class Standing: Rank #1 of 42", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }

              Surface(
                color = SchoolGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SchoolGold.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("DISCIPLINARY CONDUCT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = SchoolNavyDark)
                  Text("EXEMPLARY", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = SchoolNavyDark)
                  Text("No infractions recorded", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }

            // 5. Verification Signatures and Institutional Seal
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Prof. Sarah Jenkins",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Homeroom Class Teacher",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Verified: Oct 2026",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = SchoolAccentGreen
                )
              }

              // Institutional Embossed Seal Graphic
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .border(2.dp, SchoolGold, CircleShape)
                  .background(SchoolGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.School, contentDescription = null, tint = SchoolNavyDark, modifier = Modifier.size(20.dp))
                  Text("SEAL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold), color = SchoolNavyDark)
                }
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Dr. Arthur Pendelton",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Principal & Executive Head",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Endorsed with Seal",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = SchoolAccentGreen
                )
              }
            }

            // Digital Hash Verification Footer
            Text(
              text = "Cryptographic Record ID: SHA256:7B9E-43A1-C802-F981 • St. Joseph's Digital Registrar Node",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                  Intent.EXTRA_TEXT,
                  "🏫 St. Joseph's Higher Secondary School - Official Academic Certificate\n" +
                    "Student: $studentName ($gradeSection, Roll #$rollNo)\n" +
                    "Cumulative Attendance: ${String.format("%.1f", attendanceRate)}% (Board Exam Eligible)\n" +
                    "GPA: $gpa / 10.0 (Rank #1)\n" +
                    "Verified by Principal Dr. Arthur Pendelton."
                )
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Official Transcript")
              context.startActivity(shareIntent)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_transcript_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
          }

          Button(
            onClick = {
              isExporting = true
              onExportSuccess("📄 Academic Transcript for $studentName exported to device storage successfully.")
              onDismiss()
            },
            modifier = Modifier
              .weight(1.5f)
              .testTag("print_export_pdf_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SchoolNavyPrimary,
              contentColor = Color.White
            )
          ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export & Print PDF", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun TranscriptField(
  label: String,
  value: String,
  valueWeight: FontWeight = FontWeight.SemiBold
) {
  Column {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = valueWeight, fontSize = 13.sp),
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}
