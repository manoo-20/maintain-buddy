package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.Exam
import com.example.data.model.Mark
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherAssignment
import com.example.data.model.UserProfile
import com.example.ui.components.AppHeader
import com.example.ui.components.QuickActionTile
import com.example.ui.components.StatCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherDashboardScreen(
    currentSchool: School?,
    currentUser: UserProfile?,
    teacher: Teacher?,
    assignments: List<TeacherAssignment>,
    allClasses: List<SchoolClass>,
    allSubjects: List<Subject>,
    allStudents: List<Student>,
    allExams: List<Exam>,
    allMarks: List<Mark>,
    allAttendance: List<AttendanceRecord>,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onSwitchTenant: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Assigned class IDs for this teacher
    val teacherId = teacher?.id ?: ""
    val teacherAssignments = remember(assignments, teacherId) {
        if (teacherId.isNotBlank()) assignments.filter { it.teacher_id == teacherId } else assignments
    }

    val assignedClassIds = remember(teacherAssignments, allClasses) {
        val direct = teacherAssignments.map { it.class_id }.toSet()
        if (direct.isNotEmpty()) direct else allClasses.take(2).map { it.id }.toSet()
    }

    val assignedClasses = remember(allClasses, assignedClassIds) {
        allClasses.filter { it.id in assignedClassIds }
    }

    val assignedStudents = remember(allStudents, assignedClassIds) {
        allStudents.filter { it.class_id in assignedClassIds }
    }

    // Attendance marked today for assigned classes
    val todayClassAttendance = remember(allAttendance, assignedClassIds, todayDate) {
        allAttendance.filter { it.date == todayDate && it.class_id in assignedClassIds }
    }

    val attendanceCompleted = todayClassAttendance.isNotEmpty() && assignedStudents.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppHeader(
            currentSchool = currentSchool,
            currentUser = currentUser,
            onSwitchTenant = onSwitchTenant,
            onOpenSettings = onOpenSettings,
            onLogout = onLogout
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Teacher Profile Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = EmeraldTertiary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = teacher?.full_name ?: currentUser?.full_name ?: "Teacher Faculty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Emp ID: ${teacher?.employee_id.takeIf { !it.isNullOrBlank() } ?: "EMP-2025"} • Qualification: ${teacher?.qualification ?: "B.Ed"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Assigned Classes: ${assignedClasses.joinToString { it.displayName }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Action Alert Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (attendanceCompleted) EmeraldContainer else SaffronContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (attendanceCompleted) Icons.Default.AssignmentTurnedIn else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (attendanceCompleted) OnEmeraldContainer else OnSaffronContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (attendanceCompleted) "Today's Attendance Marked" else "Daily Attendance Pending!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (attendanceCompleted) OnEmeraldContainer else OnSaffronContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (attendanceCompleted)
                            "You have marked attendance for ${todayClassAttendance.size} students in your assigned classes today."
                        else
                            "Please mark daily student attendance for your assigned classes (Date: $todayDate).",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (attendanceCompleted) OnEmeraldContainer else OnSaffronContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToAttendance,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_teacher_mark_attendance"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (attendanceCompleted) EmeraldTertiary else SaffronSecondary
                        )
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (attendanceCompleted) "Review / Edit Attendance" else "Mark Class Attendance Now (उपस्थिति दर्ज करें)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "My Classes",
                    value = "${assignedClasses.size}",
                    subtitle = "आवंटित कक्षा",
                    icon = Icons.Default.Class,
                    accentColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "My Students",
                    value = "${assignedStudents.size}",
                    subtitle = "कुल छात्र",
                    icon = Icons.Default.Groups,
                    accentColor = EmeraldTertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Exams",
                    value = "${allExams.size}",
                    subtitle = "परीक्षा",
                    icon = Icons.Default.EditNote,
                    accentColor = SaffronSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMarks
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Assigned Subjects & Classes Chips
            Text(
                text = "My Teaching Subjects & Classes (मेरे विषय)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (allSubjects.isEmpty()) {
                    Text("No subjects assigned yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    allSubjects.take(6).forEach { sub ->
                        val matchingClass = allClasses.find { it.id == sub.class_id }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IndigoContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${sub.name} (${matchingClass?.displayName ?: "Class"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnIndigoContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Daily Attendance",
                    hindiTitle = "दैनिक उपस्थिति",
                    icon = Icons.Default.AssignmentTurnedIn,
                    color = EmeraldTertiary,
                    onClick = onNavigateToAttendance,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "Enter Exam Marks",
                    hindiTitle = "अंक प्रविष्टि",
                    icon = Icons.Default.EditNote,
                    color = IndigoPrimary,
                    onClick = onNavigateToMarks,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Students in Assigned Classes Roster
            Text(
                text = "Assigned Students Roster (${assignedStudents.size} छात्र)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            assignedStudents.take(8).forEach { std ->
                val stdClass = allClasses.find { it.id == std.class_id }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(IndigoContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = std.roll_no,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = std.full_name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Adm: ${std.admission_no} • ${stdClass?.displayName ?: "Class"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldContainer
                        ) {
                            Text(
                                text = "Active",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = OnEmeraldContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
