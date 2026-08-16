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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.example.data.model.Student
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.RoseAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    schoolId: String,
    classes: List<SchoolClass>,
    students: List<Student>,
    onAddStudent: (Student, Double) -> Unit,
    onUpdateStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf<String?>(null) }
    var classFilterExpanded by remember { mutableStateOf(false) }

    // Add / Edit Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }

    var formFullName by remember { mutableStateOf("") }
    var formRollNo by remember { mutableStateOf("") }
    var formAdmissionNo by remember { mutableStateOf("") }
    var formGender by remember { mutableStateOf("Male") }
    var formGuardianName by remember { mutableStateOf("") }
    var formGuardianPhone by remember { mutableStateOf("") }
    var formVillage by remember { mutableStateOf("") }
    var formAadhaar by remember { mutableStateOf("") }
    var formClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "") }
    var formClassExpanded by remember { mutableStateOf(false) }
    var formInitialFee by remember { mutableStateOf("6500") }

    val filteredStudents = remember(students, searchQuery, selectedClassFilter) {
        students.filter { std ->
            val matchQ = searchQuery.isBlank() ||
                    std.full_name.contains(searchQuery, ignoreCase = true) ||
                    std.roll_no.contains(searchQuery, ignoreCase = true) ||
                    std.admission_no.contains(searchQuery, ignoreCase = true)
            val matchC = selectedClassFilter == null || std.class_id == selectedClassFilter
            matchQ && matchC
        }.sortedBy { it.roll_no }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Student Directory (${students.size} छात्र)", fontWeight = FontWeight.Bold) },
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
                    editingStudent = null
                    formFullName = ""
                    formRollNo = (students.size + 1).toString().padStart(2, '0')
                    formAdmissionNo = "ADM-2025" + (students.size + 1).toString().padStart(3, '0')
                    formGuardianName = ""
                    formGuardianPhone = "+91 9"
                    formVillage = ""
                    formAadhaar = ""
                    formClassId = classes.firstOrNull()?.id ?: ""
                    formInitialFee = "6500"
                    showAddEditDialog = true
                },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Filter Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name or roll...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1.2f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = classFilterExpanded,
                        onExpandedChange = { classFilterExpanded = !classFilterExpanded },
                        modifier = Modifier.weight(0.8f)
                    ) {
                        val selClassName = classes.find { it.id == selectedClassFilter }?.displayName ?: "All Classes"
                        OutlinedTextField(
                            value = selClassName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classFilterExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = classFilterExpanded,
                            onDismissRequest = { classFilterExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("All Classes") }, onClick = { selectedClassFilter = null; classFilterExpanded = false })
                            classes.forEach { sc ->
                                DropdownMenuItem(text = { Text(sc.displayName) }, onClick = { selectedClassFilter = sc.id; classFilterExpanded = false })
                            }
                        }
                    }
                }
            }

            // Student Roster List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents, key = { it.id }) { std ->
                    val stdClass = classes.find { it.id == std.class_id }

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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(IndigoContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = std.roll_no,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(std.full_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${stdClass?.displayName ?: "Class"} • Adm: ${std.admission_no}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Guardian: ${std.guardian_name} • Ph: ${std.guardian_phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        editingStudent = std
                                        formFullName = std.full_name
                                        formRollNo = std.roll_no
                                        formAdmissionNo = std.admission_no
                                        formGender = std.gender
                                        formGuardianName = std.guardian_name
                                        formGuardianPhone = std.guardian_phone
                                        formVillage = std.village_address
                                        formAadhaar = std.aadhaar_last4
                                        formClassId = std.class_id
                                        showAddEditDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndigoPrimary)
                                }
                                IconButton(onClick = { onDeleteStudent(std) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseAlert)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Student Dialog
        if (showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { showAddEditDialog = false },
                title = {
                    Text(
                        text = if (editingStudent == null) "New Student Admission" else "Edit Student Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = formFullName,
                            onValueChange = { formFullName = it },
                            label = { Text("Full Name (छात्र का नाम) *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_student_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formRollNo,
                                onValueChange = { formRollNo = it },
                                label = { Text("Roll No *") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dialog_student_roll")
                            )
                            OutlinedTextField(
                                value = formAdmissionNo,
                                onValueChange = { formAdmissionNo = it },
                                label = { Text("Adm No *") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dialog_student_adm")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Class Dropdown
                        ExposedDropdownMenuBox(
                            expanded = formClassExpanded,
                            onExpandedChange = { formClassExpanded = !formClassExpanded }
                        ) {
                            val curClassName = classes.find { it.id == formClassId }?.displayName ?: "Select Class"
                            OutlinedTextField(
                                value = curClassName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class & Section *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formClassExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = formClassExpanded,
                                onDismissRequest = { formClassExpanded = false }
                            ) {
                                classes.forEach { sc ->
                                    DropdownMenuItem(
                                        text = { Text(sc.displayName) },
                                        onClick = {
                                            formClassId = sc.id
                                            formClassExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formGuardianName,
                            onValueChange = { formGuardianName = it },
                            label = { Text("Father / Guardian Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formGuardianPhone,
                                onValueChange = { formGuardianPhone = it },
                                label = { Text("Phone No") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formVillage,
                                onValueChange = { formVillage = it },
                                label = { Text("Village / Gram") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (editingStudent == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = formInitialFee,
                                onValueChange = { formInitialFee = it },
                                label = { Text("Annual Fee Structure (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formFullName.isNotBlank() && formRollNo.isNotBlank()) {
                                if (editingStudent == null) {
                                    val newStd = Student(
                                        school_id = schoolId,
                                        class_id = formClassId,
                                        full_name = formFullName,
                                        roll_no = formRollNo,
                                        admission_no = formAdmissionNo,
                                        gender = formGender,
                                        guardian_name = formGuardianName,
                                        guardian_phone = formGuardianPhone,
                                        village_address = formVillage,
                                        aadhaar_last4 = formAadhaar.take(4)
                                    )
                                    val feeAmt = formInitialFee.toDoubleOrNull() ?: 6500.0
                                    onAddStudent(newStd, feeAmt)
                                } else {
                                    val updated = editingStudent!!.copy(
                                        full_name = formFullName,
                                        roll_no = formRollNo,
                                        admission_no = formAdmissionNo,
                                        gender = formGender,
                                        guardian_name = formGuardianName,
                                        guardian_phone = formGuardianPhone,
                                        village_address = formVillage,
                                        aadhaar_last4 = formAadhaar.take(4),
                                        class_id = formClassId
                                    )
                                    onUpdateStudent(updated)
                                }
                                showAddEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text(if (editingStudent == null) "Enroll Student" else "Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
