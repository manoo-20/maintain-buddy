package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherAssignment
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherManagementScreen(
    schoolId: String,
    teachers: List<Teacher>,
    classes: List<SchoolClass>,
    subjects: List<Subject>,
    assignments: List<TeacherAssignment>,
    onAddTeacher: (Teacher) -> Unit,
    onDeleteTeacher: (Teacher) -> Unit,
    onAssignTeacher: (teacherId: String, classId: String, subjectId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var activeTeacherForAssign by remember { mutableStateOf<Teacher?>(null) }

    // Add Teacher form
    var teacherName by remember { mutableStateOf("") }
    var teacherPhone by remember { mutableStateOf("+91 9") }
    var teacherEmail by remember { mutableStateOf("") }
    var teacherQualification by remember { mutableStateOf("B.Ed, B.Sc") }
    var teacherEmpId by remember { mutableStateOf("EMP-" + (teachers.size + 1).toString().padStart(3, '0')) }

    // Assignment form
    var assignClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "") }
    var assignSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Teacher & Subject Faculty (${teachers.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaffronSecondary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    teacherName = ""
                    teacherPhone = "+91 9"
                    teacherEmail = ""
                    teacherQualification = "B.Ed"
                    teacherEmpId = "EMP-" + (teachers.size + 1).toString().padStart(3, '0')
                    showAddTeacherDialog = true
                },
                containerColor = SaffronSecondary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_teacher")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Teacher")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(teachers, key = { it.id }) { teacher ->
                    val teacherAssignments = assignments.filter { it.teacher_id == teacher.id }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(SaffronContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = SaffronSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(teacher.full_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "ID: ${teacher.employee_id ?: "N/A"} • ${teacher.qualification ?: "Faculty"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            activeTeacherForAssign = teacher
                                            assignClassId = classes.firstOrNull()?.id ?: ""
                                            assignSubjectId = subjects.firstOrNull()?.id ?: ""
                                            showAssignDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Assignment, contentDescription = "Assign", tint = IndigoPrimary)
                                    }
                                    IconButton(onClick = { onDeleteTeacher(teacher) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseAlert)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Assigned Classes & Subjects:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (teacherAssignments.isEmpty()) {
                                Text("No subjects assigned yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    teacherAssignments.forEach { assign ->
                                        val cObj = classes.find { it.id == assign.class_id }
                                        val sObj = subjects.find { it.id == assign.subject_id }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = IndigoContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Book, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${sObj?.name ?: "Subject"} (${cObj?.displayName ?: "Class"})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OnIndigoContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Teacher Dialog
        if (showAddTeacherDialog) {
            AlertDialog(
                onDismissRequest = { showAddTeacherDialog = false },
                title = { Text("Add New Teacher Faculty", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("Teacher Full Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_teacher_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = teacherEmpId,
                            onValueChange = { teacherEmpId = it },
                            label = { Text("Employee ID") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = teacherQualification,
                            onValueChange = { teacherQualification = it },
                            label = { Text("Qualifications (e.g. M.Sc, B.Ed)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = teacherPhone,
                            onValueChange = { teacherPhone = it },
                            label = { Text("Contact Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (teacherName.isNotBlank()) {
                                val t = Teacher(
                                    school_id = schoolId,
                                    full_name = teacherName,
                                    employee_id = teacherEmpId,
                                    qualification = teacherQualification,
                                    phone = teacherPhone
                                )
                                onAddTeacher(t)
                                showAddTeacherDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronSecondary)
                    ) {
                        Text("Add Faculty")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTeacherDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Assign Class & Subject Dialog
        if (showAssignDialog && activeTeacherForAssign != null) {
            val t = activeTeacherForAssign!!
            val classSubjects = subjects.filter { it.class_id == assignClassId }.ifEmpty { subjects }

            AlertDialog(
                onDismissRequest = { showAssignDialog = false },
                title = { Text("Assign to ${t.full_name}", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Select Class and Subject to allocate to this faculty member.", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Class Dropdown
                        ExposedDropdownMenuBox(
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = !classExpanded }
                        ) {
                            val cName = classes.find { it.id == assignClassId }?.displayName ?: "Select Class"
                            OutlinedTextField(
                                value = cName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = classExpanded,
                                onDismissRequest = { classExpanded = false }
                            ) {
                                classes.forEach { sc ->
                                    DropdownMenuItem(
                                        text = { Text(sc.displayName) },
                                        onClick = {
                                            assignClassId = sc.id
                                            val subs = subjects.filter { it.class_id == sc.id }
                                            if (subs.isNotEmpty()) assignSubjectId = subs.first().id
                                            classExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject Dropdown
                        ExposedDropdownMenuBox(
                            expanded = subjectExpanded,
                            onExpandedChange = { subjectExpanded = !subjectExpanded }
                        ) {
                            val sName = subjects.find { it.id == assignSubjectId }?.name ?: "Select Subject"
                            OutlinedTextField(
                                value = sName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Subject (विषय)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = subjectExpanded,
                                onDismissRequest = { subjectExpanded = false }
                            ) {
                                classSubjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            assignSubjectId = sub.id
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (assignClassId.isNotBlank() && assignSubjectId.isNotBlank()) {
                                onAssignTeacher(t.id, assignClassId, assignSubjectId)
                                showAssignDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Assign Subject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAssignDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
