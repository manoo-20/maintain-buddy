package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolClass
import com.example.data.model.Student
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    classes: List<SchoolClass>,
    students: List<Student>,
    existingAttendance: List<AttendanceRecord>,
    onSaveAttendance: (classId: String, date: String, records: List<Pair<String, String>>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: "") }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val todayFormatted = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayFormatted) }

    val selectedClass = remember(classes, selectedClassId) {
        classes.find { it.id == selectedClassId }
    }

    val classStudents = remember(students, selectedClassId) {
        students.filter { it.class_id == selectedClassId }.sortedBy { it.roll_no }
    }

    // Attendance state map: studentId -> status ("present", "absent", "late", "leave")
    val attendanceMap = remember { mutableStateMapOf<String, String>() }

    // Synchronize attendanceMap when class, date, or existing records change
    LaunchedEffect(selectedClassId, selectedDate, existingAttendance, classStudents) {
        attendanceMap.clear()
        val existingForDateAndClass = existingAttendance.filter {
            it.class_id == selectedClassId && it.date == selectedDate
        }

        classStudents.forEach { student ->
            val match = existingForDateAndClass.find { it.student_id == student.id }
            attendanceMap[student.id] = match?.status ?: "present"
        }
    }

    val presentCount = attendanceMap.values.count { it.equals("present", ignoreCase = true) }
    val absentCount = attendanceMap.values.count { it.equals("absent", ignoreCase = true) }
    val lateCount = attendanceMap.values.count { it.equals("late", ignoreCase = true) }
    val totalCount = classStudents.size
    val percentage = if (totalCount > 0) (presentCount.toDouble() / totalCount.toDouble()) * 100.0 else 0.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Daily Attendance (दैनिक उपस्थिति)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Controls: Class & Date
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Class Selector
                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedClass?.displayName ?: "Select Class",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                classes.forEach { sc ->
                                    DropdownMenuItem(
                                        text = { Text(sc.displayName, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedClassId = sc.id
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Date Input
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = { selectedDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Banner & Mark All Present Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = EmeraldContainer) {
                                Text("P: $presentCount", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = OnEmeraldContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = RoseContainer) {
                                Text("A: $absentCount", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = OnRoseContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = SaffronContainer) {
                                Text("Late: $lateCount", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = OnSaffronContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                classStudents.forEach { std ->
                                    attendanceMap[std.id] = "present"
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_mark_all_present")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                        }
                    }
                }
            }

            // Student Roster with Toggle Controls
            if (classStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students enrolled in ${selectedClass?.displayName ?: "this class"}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(classStudents, key = { it.id }) { student ->
                        val currentStatus = attendanceMap[student.id] ?: "present"

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                when (currentStatus) {
                                    "present" -> EmeraldTertiary.copy(alpha = 0.4f)
                                    "absent" -> RoseAlert.copy(alpha = 0.4f)
                                    "late" -> SaffronSecondary.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (currentStatus) {
                                                    "present" -> EmeraldContainer
                                                    "absent" -> RoseContainer
                                                    "late" -> SaffronContainer
                                                    else -> IndigoContainer
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.roll_no,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = when (currentStatus) {
                                                "present" -> OnEmeraldContainer
                                                "absent" -> OnRoseContainer
                                                "late" -> OnSaffronContainer
                                                else -> OnIndigoContainer
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = student.full_name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Adm: ${student.admission_no} • S/O ${student.guardian_name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Toggle buttons (Present, Absent, Late)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .clickable { attendanceMap[student.id] = "present" }
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = if (currentStatus == "present") EmeraldTertiary else EmeraldContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "P",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStatus == "present") Color.White else OnEmeraldContainer,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clickable { attendanceMap[student.id] = "absent" }
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = if (currentStatus == "absent") RoseAlert else RoseContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "A",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStatus == "absent") Color.White else OnRoseContainer,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clickable { attendanceMap[student.id] = "late" }
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = if (currentStatus == "late") SaffronSecondary else SaffronContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "L",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStatus == "late") Color.White else OnSaffronContainer,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save Attendance Bottom Bar
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Overall Attendance", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", percentage)}% ($presentCount / $totalCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val records = classStudents.map { std ->
                                Pair(std.id, attendanceMap[std.id] ?: "present")
                            }
                            onSaveAttendance(selectedClassId, selectedDate, records)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_save_attendance"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Attendance (सुरक्षित करें)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
