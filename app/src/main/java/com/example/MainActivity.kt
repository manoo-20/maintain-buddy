package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.School
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.remote.SupabaseConfig
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClassManagementScreen
import com.example.ui.screens.CsvImportExportScreen
import com.example.ui.screens.FeeManagementScreen
import com.example.ui.screens.MarksEntryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.screens.TeacherManagementScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.GraminShalaTheme
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary
import com.example.ui.viewmodel.SchoolViewModel

enum class AppScreenRoute {
    DASHBOARD,
    ATTENDANCE,
    MARKS,
    FEES,
    STUDENTS,
    TEACHERS,
    CLASSES,
    CSV_IMPORT,
    REPORTS
}

class MainActivity : ComponentActivity() {
    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GraminShalaTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: SchoolViewModel) {
    val allSchools by viewModel.allSchools.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val schoolProfiles by viewModel.currentSchoolProfiles.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val teacherAssignments by viewModel.teacherAssignments.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val marks by viewModel.marks.collectAsStateWithLifecycle()
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val fees by viewModel.fees.collectAsStateWithLifecycle()
    val feePayments by viewModel.feePayments.collectAsStateWithLifecycle()
    val academicYears by viewModel.academicYears.collectAsStateWithLifecycle()
    val loggedInStudent by viewModel.loggedInStudent.collectAsStateWithLifecycle()
    val loggedInTeacher by viewModel.loggedInTeacher.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val supabaseStatus by viewModel.supabaseStatus.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentRoute by remember { mutableStateOf(AppScreenRoute.DASHBOARD) }

    // Dialog state
    var showSwitchTenantDialog by remember { mutableStateOf(false) }
    var showCloudSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (currentUser == null) {
                AuthScreen(
                    schools = allSchools,
                    currentSchool = currentSchool,
                    schoolProfiles = schoolProfiles,
                    onSelectSchool = { viewModel.selectSchool(it) },
                    onLoginWithProfile = { viewModel.loginWithProfile(it) },
                    onLoginAsRole = { viewModel.loginAsRole(it) },
                    onRegisterSchool = { name, code, village, district, state, udise, phone, email, adminName, adminPass ->
                        viewModel.registerNewSchool(
                            name = name,
                            code = code,
                            village = village,
                            district = district,
                            state = state,
                            udise = udise,
                            phone = phone,
                            email = email,
                            adminName = adminName,
                            adminPass = adminPass,
                            onSuccess = { currentRoute = AppScreenRoute.DASHBOARD }
                        )
                    },
                    onOpenSupabaseSettings = { showCloudSettingsDialog = true }
                )
            } else {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { route ->
                    when (route) {
                        AppScreenRoute.DASHBOARD -> {
                            val userRole = UserRole.fromString(currentUser?.role ?: "admin")
                            when (userRole) {
                                UserRole.ADMIN -> {
                                    AdminDashboardScreen(
                                        currentSchool = currentSchool,
                                        currentUser = currentUser,
                                        students = students,
                                        teachers = teachers,
                                        classes = classes,
                                        attendanceList = attendance,
                                        feesList = fees,
                                        paymentsList = feePayments,
                                        academicYears = academicYears,
                                        onNavigateToAttendance = { currentRoute = AppScreenRoute.ATTENDANCE },
                                        onNavigateToMarks = { currentRoute = AppScreenRoute.MARKS },
                                        onNavigateToFees = { currentRoute = AppScreenRoute.FEES },
                                        onNavigateToStudents = { currentRoute = AppScreenRoute.STUDENTS },
                                        onNavigateToTeachers = { currentRoute = AppScreenRoute.TEACHERS },
                                        onNavigateToClasses = { currentRoute = AppScreenRoute.CLASSES },
                                        onNavigateToCsvImport = { currentRoute = AppScreenRoute.CSV_IMPORT },
                                        onNavigateToReports = { currentRoute = AppScreenRoute.REPORTS },
                                        onSwitchTenant = { showSwitchTenantDialog = true },
                                        onOpenSettings = { showCloudSettingsDialog = true },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                UserRole.TEACHER -> {
                                    TeacherDashboardScreen(
                                        currentSchool = currentSchool,
                                        currentUser = currentUser,
                                        teacher = loggedInTeacher,
                                        assignments = teacherAssignments,
                                        allClasses = classes,
                                        allSubjects = subjects,
                                        allStudents = students,
                                        allExams = exams,
                                        allMarks = marks,
                                        allAttendance = attendance,
                                        onNavigateToAttendance = { currentRoute = AppScreenRoute.ATTENDANCE },
                                        onNavigateToMarks = { currentRoute = AppScreenRoute.MARKS },
                                        onSwitchTenant = { showSwitchTenantDialog = true },
                                        onOpenSettings = { showCloudSettingsDialog = true },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                UserRole.STUDENT -> {
                                    StudentDashboardScreen(
                                        currentSchool = currentSchool,
                                        currentUser = currentUser,
                                        student = loggedInStudent,
                                        classes = classes,
                                        subjects = subjects,
                                        exams = exams,
                                        allMarks = marks,
                                        allAttendance = attendance,
                                        fees = fees,
                                        payments = feePayments,
                                        onSwitchTenant = { showSwitchTenantDialog = true },
                                        onOpenSettings = { showCloudSettingsDialog = true },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            }
                        }
                        AppScreenRoute.ATTENDANCE -> {
                            AttendanceScreen(
                                classes = classes,
                                students = students,
                                existingAttendance = attendance,
                                onSaveAttendance = { classId, date, records ->
                                    viewModel.markAttendance(classId, date, records) {
                                        currentRoute = AppScreenRoute.DASHBOARD
                                    }
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.MARKS -> {
                            MarksEntryScreen(
                                classes = classes,
                                subjects = subjects,
                                exams = exams,
                                students = students,
                                existingMarks = marks,
                                onSaveMarks = { examId, subjectId, entries, maxMarks ->
                                    viewModel.saveMarks(examId, subjectId, entries, maxMarks) {
                                        currentRoute = AppScreenRoute.DASHBOARD
                                    }
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.FEES -> {
                            FeeManagementScreen(
                                currentSchool = currentSchool,
                                classes = classes,
                                students = students,
                                fees = fees,
                                payments = feePayments,
                                onRecordPayment = { feeId, studentId, amount, mode, date, notes, callback ->
                                    viewModel.recordPayment(feeId, studentId, amount, mode, date, notes, callback)
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.STUDENTS -> {
                            StudentManagementScreen(
                                schoolId = currentSchool?.id ?: "",
                                classes = classes,
                                students = students,
                                onAddStudent = { newStd, initialFee ->
                                    viewModel.addStudent(newStd, initialFee) {}
                                },
                                onUpdateStudent = { updatedStd ->
                                    viewModel.updateStudent(updatedStd) {}
                                },
                                onDeleteStudent = { std ->
                                    viewModel.deleteStudent(std)
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.TEACHERS -> {
                            TeacherManagementScreen(
                                schoolId = currentSchool?.id ?: "",
                                teachers = teachers,
                                classes = classes,
                                subjects = subjects,
                                assignments = teacherAssignments,
                                onAddTeacher = { t ->
                                    viewModel.addTeacher(t) {}
                                },
                                onDeleteTeacher = { t ->
                                    viewModel.deleteTeacher(t)
                                },
                                onAssignTeacher = { tId, cId, sId ->
                                    viewModel.assignTeacher(tId, cId, sId)
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.CLASSES -> {
                            ClassManagementScreen(
                                schoolId = currentSchool?.id ?: "",
                                classes = classes,
                                subjects = subjects,
                                onAddClass = { name, section ->
                                    viewModel.addClass(name, section)
                                },
                                onDeleteClass = { sc ->
                                    viewModel.deleteClass(sc)
                                },
                                onAddSubject = { cId, name, code ->
                                    viewModel.addSubject(cId, name, code)
                                },
                                onDeleteSubject = { s ->
                                    viewModel.deleteSubject(s)
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.CSV_IMPORT -> {
                            CsvImportExportScreen(
                                classes = classes,
                                students = students,
                                onImportCsv = { classId, csvText, callback ->
                                    viewModel.importStudentsCsv(classId, csvText, callback)
                                },
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                        AppScreenRoute.REPORTS -> {
                            ReportsScreen(
                                currentSchool = currentSchool,
                                classes = classes,
                                students = students,
                                subjects = subjects,
                                exams = exams,
                                marks = marks,
                                attendance = attendance,
                                fees = fees,
                                payments = feePayments,
                                onBack = { currentRoute = AppScreenRoute.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }

    // Switch Tenant / Role Dialog
    if (showSwitchTenantDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchTenantDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Tenant / Persona", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Quickly switch active school or test different role views:", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Active Schools (Multi-Tenant):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    allSchools.forEach { s ->
                        val isSelected = s.id == currentSchool?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    viewModel.selectSchool(s)
                                    showSwitchTenantDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) IndigoContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${s.village}, ${s.district}", style = MaterialTheme.typography.labelSmall)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Switch Role Persona:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            val isRoleActive = UserRole.fromString(currentUser?.role ?: "") == role
                            Button(
                                onClick = {
                                    viewModel.loginAsRole(role)
                                    currentRoute = AppScreenRoute.DASHBOARD
                                    showSwitchTenantDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRoleActive) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isRoleActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(role.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchTenantDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Cloud / Supabase Settings Dialog
    if (showCloudSettingsDialog) {
        val currentCfg = viewModel.getSupabaseConfig()
        var cfgUrl by remember { mutableStateOf(currentCfg.url) }
        var cfgKey by remember { mutableStateOf(currentCfg.anonKey) }

        AlertDialog(
            onDismissRequest = { showCloudSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud Database & Multi-Tenant RLS", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Configured for Supabase PostgreSQL with Row Level Security (RLS) ensuring strict isolation by school_id.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cfgUrl,
                        onValueChange = { cfgUrl = it },
                        label = { Text("Supabase Project URL") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cfgKey,
                        onValueChange = { cfgKey = it },
                        label = { Text("Supabase Anon / API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RLS Multi-Tenancy Architecture", fontWeight = FontWeight.Bold, color = OnEmeraldContainer, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Policy: `CREATE POLICY tenant_isolation ON table USING (school_id = auth.school_id())`",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnEmeraldContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCfg = SupabaseConfig(url = cfgUrl, anonKey = cfgKey, isConnected = true)
                        viewModel.updateSupabaseConfig(newCfg)
                        showCloudSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Save & Test Connection")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
