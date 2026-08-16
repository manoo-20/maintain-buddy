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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.Mark
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryScreen(
    classes: List<SchoolClass>,
    subjects: List<Subject>,
    exams: List<Exam>,
    students: List<Student>,
    existingMarks: List<Mark>,
    onSaveMarks: (examId: String, subjectId: String, entries: List<Pair<String, Double>>, maxMarks: Double) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: "") }
    var selectedExamId by remember(exams) { mutableStateOf(exams.firstOrNull()?.id ?: "") }
    var selectedSubjectId by remember(subjects, selectedClassId) {
        val classSubs = subjects.filter { it.class_id == selectedClassId }
        mutableStateOf(classSubs.firstOrNull()?.id ?: subjects.firstOrNull()?.id ?: "")
    }

    var maxMarksInput by remember { mutableStateOf("100") }

    var classExpanded by remember { mutableStateOf(false) }
    var examExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    val selectedClass = remember(classes, selectedClassId) { classes.find { it.id == selectedClassId } }
    val selectedExam = remember(exams, selectedExamId) { exams.find { it.id == selectedExamId } }
    val classSubjects = remember(subjects, selectedClassId) {
        subjects.filter { it.class_id == selectedClassId }.ifEmpty { subjects }
    }
    val selectedSubject = remember(subjects, selectedSubjectId) { subjects.find { it.id == selectedSubjectId } }

    val classStudents = remember(students, selectedClassId) {
        students.filter { it.class_id == selectedClassId }.sortedBy { it.roll_no }
    }

    // studentId -> entered marks string
    val marksInputs = remember { mutableStateMapOf<String, String>() }

    // Prepopulate existing marks
    LaunchedEffect(selectedExamId, selectedSubjectId, existingMarks, classStudents) {
        marksInputs.clear()
        val existing = existingMarks.filter { it.exam_id == selectedExamId && it.subject_id == selectedSubjectId }
        classStudents.forEach { std ->
            val match = existing.find { it.student_id == std.id }
            if (match != null) {
                marksInputs[std.id] = if (match.marks_obtained % 1.0 == 0.0) match.marks_obtained.toInt().toString() else match.marks_obtained.toString()
                maxMarksInput = match.max_marks.toInt().toString()
            } else {
                marksInputs[std.id] = ""
            }
        }
    }

    val maxMarksDouble = maxMarksInput.toDoubleOrNull() ?: 100.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Exam Marks Entry (अंक प्रविष्टि)", fontWeight = FontWeight.Bold) },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dropdowns Configuration Card
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
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = !classExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedClass?.displayName ?: "Select Class",
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
                                        text = { Text(sc.displayName, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedClassId = sc.id
                                            val subs = subjects.filter { it.class_id == sc.id }
                                            if (subs.isNotEmpty()) selectedSubjectId = subs.first().id
                                            classExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Exam Selector
                        ExposedDropdownMenuBox(
                            expanded = examExpanded,
                            onExpandedChange = { examExpanded = !examExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedExam?.name ?: "Select Exam",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Exam") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = examExpanded,
                                onDismissRequest = { examExpanded = false }
                            ) {
                                exams.forEach { ex ->
                                    DropdownMenuItem(
                                        text = { Text(ex.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedExamId = ex.id
                                            examExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Subject Selector
                        ExposedDropdownMenuBox(
                            expanded = subjectExpanded,
                            onExpandedChange = { subjectExpanded = !subjectExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedSubject?.name ?: "Select Subject",
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
                                        text = { Text(sub.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedSubjectId = sub.id
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Max Marks Input
                        OutlinedTextField(
                            value = maxMarksInput,
                            onValueChange = { maxMarksInput = it },
                            label = { Text("Max Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }

            // Student Marks Roster
            if (classStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students in selected class", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(classStudents, key = { it.id }) { std ->
                        val markStr = marksInputs[std.id] ?: ""
                        val markVal = markStr.toDoubleOrNull()
                        val isExceeded = markVal != null && markVal > maxMarksDouble
                        val isNegative = markVal != null && markVal < 0

                        val grade = when {
                            markVal == null -> "-"
                            maxMarksDouble <= 0 -> "-"
                            else -> {
                                val pct = (markVal / maxMarksDouble) * 100.0
                                when {
                                    pct >= 90 -> "A+"
                                    pct >= 80 -> "A"
                                    pct >= 70 -> "B+"
                                    pct >= 60 -> "B"
                                    pct >= 50 -> "C"
                                    pct >= 33 -> "D"
                                    else -> "F"
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                        Text(std.full_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "Adm: ${std.admission_no}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = markStr,
                                        onValueChange = { marksInputs[std.id] = it },
                                        placeholder = { Text("Marks") },
                                        isError = isExceeded || isNegative,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(56.dp)
                                            .testTag("input_marks_${std.roll_no}")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (grade) {
                                            "A+", "A" -> EmeraldContainer
                                            "B+", "B" -> IndigoContainer
                                            "C" -> SaffronContainer
                                            "D" -> SaffronContainer
                                            "F" -> RoseContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.width(38.dp)
                                    ) {
                                        Text(
                                            text = grade,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = when (grade) {
                                                "A+", "A" -> OnEmeraldContainer
                                                "B+", "B" -> OnIndigoContainer
                                                "C", "D" -> OnSaffronContainer
                                                "F" -> OnRoseContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save Marks Bottom Bar
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
                        Text("Exam & Subject", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${selectedExam?.name ?: ""} • ${selectedSubject?.name ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val entries = classStudents.mapNotNull { std ->
                                val v = marksInputs[std.id]?.toDoubleOrNull()
                                if (v != null && v >= 0 && v <= maxMarksDouble) Pair(std.id, v) else null
                            }
                            if (selectedExamId.isNotBlank() && selectedSubjectId.isNotBlank()) {
                                onSaveMarks(selectedExamId, selectedSubjectId, entries, maxMarksDouble)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_save_marks"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronSecondary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Marks (अंक दर्ज करें)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
