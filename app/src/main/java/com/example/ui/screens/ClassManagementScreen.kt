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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    schoolId: String,
    classes: List<SchoolClass>,
    subjects: List<Subject>,
    onAddClass: (name: String, section: String) -> Unit,
    onDeleteClass: (SchoolClass) -> Unit,
    onAddSubject: (classId: String, name: String, code: String) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddClassDialog by remember { mutableStateOf(false) }
    var classNameInput by remember { mutableStateOf("") }
    var classSectionInput by remember { mutableStateOf("A") }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var activeClassForSubject by remember { mutableStateOf<SchoolClass?>(null) }
    var subjectNameInput by remember { mutableStateOf("") }
    var subjectCodeInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Classes & Subjects Management", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    classNameInput = "Class ${classes.size + 1}"
                    classSectionInput = "A"
                    showAddClassDialog = true
                },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_class")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes, key = { it.id }) { sc ->
                    val classSubs = subjects.filter { it.class_id == sc.id }

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
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(IndigoContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Class, contentDescription = null, tint = IndigoPrimary)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(sc.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Section: ${sc.section} • ${classSubs.size} Subjects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row {
                                    OutlinedButton(
                                        onClick = {
                                            activeClassForSubject = sc
                                            subjectNameInput = ""
                                            subjectCodeInput = "SUB-" + (classSubs.size + 1).toString().padStart(2, '0')
                                            showAddSubjectDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(onClick = { onDeleteClass(sc) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Class", tint = RoseAlert)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Subjects in this Class:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (classSubs.isEmpty()) {
                                Text("No subjects configured for this class.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    classSubs.forEach { sub ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Book, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${sub.name} (${sub.code})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldTertiary
                                                )
                                                IconButton(
                                                    onClick = { onDeleteSubject(sub) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = RoseAlert, modifier = Modifier.size(14.dp))
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
        }

        // Add Class Dialog
        if (showAddClassDialog) {
            AlertDialog(
                onDismissRequest = { showAddClassDialog = false },
                title = { Text("Create New Class & Section", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = classNameInput,
                            onValueChange = { classNameInput = it },
                            label = { Text("Class Name (e.g. Class 9, Nursery)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_class_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = classSectionInput,
                            onValueChange = { classSectionInput = it },
                            label = { Text("Section (e.g. A, B, C)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (classNameInput.isNotBlank()) {
                                onAddClass(classNameInput, classSectionInput.ifBlank { "A" })
                                showAddClassDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Create Class")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddClassDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Subject Dialog
        if (showAddSubjectDialog && activeClassForSubject != null) {
            val curC = activeClassForSubject!!
            AlertDialog(
                onDismissRequest = { showAddSubjectDialog = false },
                title = { Text("Add Subject to ${curC.displayName}", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = subjectNameInput,
                            onValueChange = { subjectNameInput = it },
                            label = { Text("Subject Name (e.g. Sanskrit, Science)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_subject_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = subjectCodeInput,
                            onValueChange = { subjectCodeInput = it },
                            label = { Text("Subject Code (e.g. SAN-01)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (subjectNameInput.isNotBlank()) {
                                onAddSubject(curC.id, subjectNameInput, subjectCodeInput.ifBlank { subjectNameInput.take(3).uppercase() })
                                showAddSubjectDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                    ) {
                        Text("Add Subject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubjectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
