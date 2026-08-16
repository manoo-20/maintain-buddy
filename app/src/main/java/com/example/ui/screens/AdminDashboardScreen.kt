package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicYear
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.UserProfile
import com.example.ui.components.AppHeader
import com.example.ui.components.QuickActionTile
import com.example.ui.components.StatCard
import com.example.ui.components.formatIndianRupees
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnRoseContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    currentSchool: School?,
    currentUser: UserProfile?,
    students: List<Student>,
    teachers: List<Teacher>,
    classes: List<SchoolClass>,
    attendanceList: List<AttendanceRecord>,
    feesList: List<FeeStructure>,
    paymentsList: List<FeePayment>,
    academicYears: List<AcademicYear>,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToCsvImport: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSwitchTenant: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val displayDate = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    // Computations
    val todayAttendance = remember(attendanceList, todayDate) {
        attendanceList.filter { it.date == todayDate }
    }
    val totalMarked = todayAttendance.size
    val presentCount = todayAttendance.count { it.status.equals("present", ignoreCase = true) }
    val absentCount = todayAttendance.count { it.status.equals("absent", ignoreCase = true) }
    val attendancePct = if (totalMarked > 0) (presentCount.toDouble() / totalMarked.toDouble()) * 100.0 else 0.0

    val totalFeesAmount = remember(feesList) { feesList.sumOf { it.netPayable } }
    val totalCollected = remember(paymentsList) { paymentsList.sumOf { it.amount_paid } }
    val totalPending = (totalFeesAmount - totalCollected).coerceAtLeast(0.0)
    val feeCollectionPct = if (totalFeesAmount > 0) (totalCollected / totalFeesAmount) * 100.0 else 0.0

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
            // Welcome & Date Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Admin Management Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Today: $displayDate • Academic Year: ${academicYears.firstOrNull { it.is_active }?.name ?: "2025-2026"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            color = OnEmeraldContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Row 1: Students & Teachers & Classes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Students",
                    value = "${students.size}",
                    subtitle = "छात्र",
                    icon = Icons.Default.People,
                    accentColor = IndigoPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStudents
                )
                StatCard(
                    title = "Teachers",
                    value = "${teachers.size}",
                    subtitle = "शिक्षक",
                    icon = Icons.Default.School,
                    accentColor = SaffronSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTeachers
                )
                StatCard(
                    title = "Classes",
                    value = "${classes.size}",
                    subtitle = "कक्षाएं",
                    icon = Icons.Default.Class,
                    accentColor = EmeraldTertiary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToClasses
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Attendance (आज की उपस्थिति)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", attendancePct)}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (attendancePct >= 75) EmeraldTertiary else SaffronSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (attendancePct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (attendancePct >= 75) EmeraldTertiary else SaffronSecondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldContainer
                        ) {
                            Text(
                                text = "Present: $presentCount",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnEmeraldContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoseContainer
                        ) {
                            Text(
                                text = "Absent: $absentCount",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnRoseContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SaffronContainer
                        ) {
                            Text(
                                text = "Total Marked: $totalMarked / ${students.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSaffronContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fees Collection Overview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = EmeraldTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fee Collection Summary (शुल्क विवरण)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.0f", feeCollectionPct)}% Paid",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(IndigoContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Total Fee (कुल)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatIndianRupees(totalFeesAmount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(EmeraldContainer.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Collected (प्राप्त)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatIndianRupees(totalCollected),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldTertiary
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(RoseContainer.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Outstanding (शेष)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatIndianRupees(totalPending),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoseAlert
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Section Header
            Text(
                text = "Administrative Actions (मुख्य कार्य)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Grid (2x4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Mark Attendance",
                    hindiTitle = "दैनिक उपस्थिति",
                    icon = Icons.Default.AssignmentTurnedIn,
                    color = IndigoPrimary,
                    onClick = onNavigateToAttendance,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "Enter Marks",
                    hindiTitle = "परीक्षा अंक प्रविष्टि",
                    icon = Icons.Default.EditNote,
                    color = SaffronSecondary,
                    onClick = onNavigateToMarks,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Fee Management",
                    hindiTitle = "शुल्क रसीद व बकाया",
                    icon = Icons.Default.ReceiptLong,
                    color = EmeraldTertiary,
                    onClick = onNavigateToFees,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "Student Directory",
                    hindiTitle = "छात्र नामांकन सूची",
                    icon = Icons.Default.Group,
                    color = IndigoPrimary,
                    onClick = onNavigateToStudents,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Teachers & Subject",
                    hindiTitle = "शिक्षक व विषय आवंटन",
                    icon = Icons.Default.School,
                    color = SaffronSecondary,
                    onClick = onNavigateToTeachers,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "Classes & Sections",
                    hindiTitle = "कक्षा व अनुभाग",
                    icon = Icons.Default.Class,
                    color = IndigoPrimary,
                    onClick = onNavigateToClasses,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Excel/CSV Bulk Import",
                    hindiTitle = "एक्सेल से छात्र आयात",
                    icon = Icons.Default.CloudUpload,
                    color = EmeraldTertiary,
                    onClick = onNavigateToCsvImport,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "Reports & Analytics",
                    hindiTitle = "रिपोर्ट व अंकतालिका",
                    icon = Icons.Default.Assessment,
                    color = SaffronSecondary,
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
