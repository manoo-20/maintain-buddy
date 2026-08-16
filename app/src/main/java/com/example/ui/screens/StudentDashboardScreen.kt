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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.Exam
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.Mark
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatIndianRupees
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.OnRoseContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    currentSchool: School?,
    currentUser: UserProfile?,
    student: Student?,
    classes: List<SchoolClass>,
    subjects: List<Subject>,
    exams: List<Exam>,
    allMarks: List<Mark>,
    allAttendance: List<AttendanceRecord>,
    fees: List<FeeStructure>,
    payments: List<FeePayment>,
    onSwitchTenant: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val studentId = student?.id ?: ""
    val studentClass = remember(classes, student) {
        classes.find { it.id == student?.class_id }
    }

    // Student's Attendance Metrics
    val myAttendance = remember(allAttendance, studentId) {
        allAttendance.filter { it.student_id == studentId }
    }
    val totalDays = myAttendance.size
    val daysPresent = myAttendance.count { it.status.equals("present", ignoreCase = true) }
    val daysAbsent = myAttendance.count { it.status.equals("absent", ignoreCase = true) }
    val attendancePct = if (totalDays > 0) (daysPresent.toDouble() / totalDays.toDouble()) * 100.0 else 92.5

    // Student's Marks
    val myMarks = remember(allMarks, studentId) {
        allMarks.filter { it.student_id == studentId }
    }
    val totalObtained = myMarks.sumOf { it.marks_obtained }
    val totalMax = myMarks.sumOf { it.max_marks }
    val overallPercentage = if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0

    // Student's Fees
    val myFees = remember(fees, studentId) {
        fees.filter { it.student_id == studentId }
    }
    val totalFeeAmount = myFees.sumOf { it.netPayable }
    val myPayments = remember(payments, studentId) {
        payments.filter { it.student_id == studentId }
    }
    val amountPaid = myPayments.sumOf { it.amount_paid }
    val outstandingBalance = (totalFeeAmount - amountPaid).coerceAtLeast(0.0)

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
            // Read-Only Banner Notice
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = IndigoContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Student Portal (छात्र पोर्टल) • Read-Only Account",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnIndigoContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Student Identity Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = student?.full_name ?: currentUser?.full_name ?: "Student Name",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class: ${studentClass?.displayName ?: "Class 5 - A"} • Roll No: ${student?.roll_no ?: "01"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Admission No: ${student?.admission_no ?: "ADM-2025001"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Guardian (अभिभावक)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(student?.guardian_name ?: "Guardian", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Village (ग्राम)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(student?.village_address ?: currentSchool?.village ?: "Village", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Section Card
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
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "My Attendance (उपस्थिति)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (attendancePct >= 75) EmeraldContainer else SaffronContainer
                        ) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", attendancePct)}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (attendancePct >= 75) OnEmeraldContainer else OnSaffronContainer
                            )
                        }
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
                        Text("Present Days: $daysPresent", style = MaterialTheme.typography.bodyMedium, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                        Text("Absent Days: $daysAbsent", style = MaterialTheme.typography.bodyMedium, color = RoseAlert, fontWeight = FontWeight.Bold)
                        Text("Total Sessions: $totalDays", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marks / Examination Report Card
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
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Academic Performance (अंकतालिका)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (myMarks.isNotEmpty()) {
                            Text(
                                text = "Avg: ${String.format(Locale.getDefault(), "%.1f", overallPercentage)}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (myMarks.isEmpty()) {
                        Text(
                            text = "Marks for recent unit tests or exams will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        myMarks.forEach { mark ->
                            val sub = subjects.find { it.id == mark.subject_id }
                            val exam = exams.find { it.id == mark.exam_id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sub?.name ?: "Subject",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = exam?.name ?: "Unit Test",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${mark.marks_obtained.toInt()} / ${mark.max_marks.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (mark.grade) {
                                            "A+", "A" -> EmeraldContainer
                                            "B+", "B" -> IndigoContainer
                                            "C" -> SaffronContainer
                                            else -> RoseContainer
                                        }
                                    ) {
                                        Text(
                                            text = "Grade ${mark.grade}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (mark.grade) {
                                                "A+", "A" -> OnEmeraldContainer
                                                "B+", "B" -> OnIndigoContainer
                                                "C" -> OnSaffronContainer
                                                else -> OnRoseContainer
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fees Statement Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fee Statement & Receipts (शुल्क स्थिति)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                                .padding(8.dp)
                        ) {
                            Text("Total Fee (कुल)", style = MaterialTheme.typography.labelSmall)
                            Text(formatIndianRupees(totalFeeAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(EmeraldContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text("Paid (जमा)", style = MaterialTheme.typography.labelSmall)
                            Text(formatIndianRupees(amountPaid), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (outstandingBalance > 0) RoseContainer.copy(alpha = 0.6f) else EmeraldContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text("Balance (शेष)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                formatIndianRupees(outstandingBalance),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (outstandingBalance > 0) RoseAlert else EmeraldTertiary
                            )
                        }
                    }

                    if (myPayments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Payment Receipts History (${myPayments.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        myPayments.forEach { pay ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = pay.receipt_no, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "${pay.payment_date} • Mode: ${pay.payment_mode.uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = formatIndianRupees(pay.amount_paid),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
