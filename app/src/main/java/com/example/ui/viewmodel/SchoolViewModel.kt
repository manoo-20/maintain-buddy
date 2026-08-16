package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AcademicYear
import com.example.data.model.AttendanceRecord
import com.example.data.model.Exam
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.Mark
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherAssignment
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.remote.SupabaseConfig
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SchoolRepository.getInstance(application)

    // Current Tenant (School) ID
    private val _currentSchoolId = MutableStateFlow<String?>(null)
    val currentSchoolId = _currentSchoolId.asStateFlow()

    // Current Logged-in User
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Supabase Connection Status / Notice
    private val _supabaseStatus = MutableStateFlow<String>("Supabase initialized. Multi-tenant RLS active.")
    val supabaseStatus = _supabaseStatus.asStateFlow()

    // UI Snack/Notification
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    val allSchools: StateFlow<List<School>> = repository.observeAllSchools()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSchool: StateFlow<School?> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeSchool(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSchoolProfiles: StateFlow<List<UserProfile>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeProfiles(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<SchoolClass>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeClasses(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeSubjects(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeTeachers(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherAssignments: StateFlow<List<TeacherAssignment>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeAssignments(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<Student>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeStudents(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<Exam>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeExams(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marks: StateFlow<List<Mark>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeMarks(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceRecord>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeAttendance(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fees: StateFlow<List<FeeStructure>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeFees(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feePayments: StateFlow<List<FeePayment>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeFeePayments(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academicYears: StateFlow<List<AcademicYear>> = _currentSchoolId
        .flatMapLatest { id ->
            if (id != null) repository.observeAcademicYears(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student Role Specific Profile
    val loggedInStudent: StateFlow<Student?> = combine(_currentUser, students) { user, stdList ->
        if (user?.role?.lowercase() == "student") {
            stdList.find { it.user_id == user.id || it.full_name.equals(user.full_name, ignoreCase = true) }
                ?: stdList.firstOrNull()
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Teacher Role Specific Profile
    val loggedInTeacher: StateFlow<Teacher?> = combine(_currentUser, teachers) { user, tList ->
        if (user?.role?.lowercase() == "teacher") {
            tList.find { it.user_id == user.id || it.full_name.equals(user.full_name, ignoreCase = true) }
                ?: tList.firstOrNull()
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            val initialSchools = repository.observeAllSchools().first()
            if (initialSchools.isNotEmpty()) {
                val firstSchool = initialSchools.first()
                _currentSchoolId.value = firstSchool.id
                val profiles = repository.observeProfiles(firstSchool.id).first()
                _currentUser.value = profiles.find { it.role == "admin" } ?: profiles.firstOrNull()
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun selectSchool(school: School) {
        _currentSchoolId.value = school.id
        viewModelScope.launch {
            val profiles = repository.observeProfiles(school.id).first()
            _currentUser.value = profiles.find { it.role == "admin" } ?: profiles.firstOrNull()
            _userMessage.value = "Switched to ${school.name}"
        }
    }

    fun loginWithProfile(profile: UserProfile) {
        _currentSchoolId.value = profile.school_id
        _currentUser.value = profile
        _userMessage.value = "Logged in as ${profile.full_name} (${profile.role.uppercase()})"
    }

    fun loginAsRole(role: UserRole) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val profiles = repository.observeProfiles(sId).first()
            val matchingProfile = profiles.find { UserRole.fromString(it.role) == role }
            if (matchingProfile != null) {
                _currentUser.value = matchingProfile
                _userMessage.value = "Switched persona to ${role.displayName}"
            } else {
                // Auto create demo profile for role if missing
                val newP = UserProfile(
                    school_id = sId,
                    role = role.name.lowercase(),
                    full_name = "${role.displayName} Demo",
                    email = "${role.name.lowercase()}@demo.school"
                )
                repository.addTeacher(
                    teacher = Teacher(school_id = sId, full_name = newP.full_name),
                    profile = newP
                )
                _currentUser.value = newP
                _userMessage.value = "Switched persona to ${role.displayName}"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun registerNewSchool(
        name: String,
        code: String,
        village: String,
        district: String,
        state: String,
        udise: String,
        phone: String,
        email: String,
        adminName: String,
        adminPass: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val (school, admin) = repository.registerSchool(
                    name = name,
                    code = code,
                    village = village,
                    district = district,
                    state = state,
                    udise = udise,
                    phone = phone,
                    email = email,
                    adminName = adminName,
                    adminPassword = adminPass
                )
                _currentSchoolId.value = school.id
                _currentUser.value = admin
                _userMessage.value = "School registered successfully! Welcome ${admin.full_name}"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Registration failed: ${e.message}"
            }
        }
    }

    // === ATTENDANCE ===
    fun markAttendance(
        classId: String,
        date: String,
        records: List<Pair<String, String>>, // studentId to status
        onSaved: () -> Unit
    ) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.saveAttendance(
                schoolId = sId,
                classId = classId,
                date = date,
                records = records,
                markedByProfileId = _currentUser.value?.id
            )
            _userMessage.value = "Attendance saved for ${records.size} students!"
            onSaved()
        }
    }

    // === MARKS ===
    fun saveMarks(
        examId: String,
        subjectId: String,
        entries: List<Pair<String, Double>>,
        maxMarks: Double,
        onSaved: () -> Unit
    ) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val result = repository.saveMarks(
                schoolId = sId,
                examId = examId,
                subjectId = subjectId,
                entries = entries,
                maxMarks = maxMarks,
                enteredByProfileId = _currentUser.value?.id
            )
            result.onSuccess { count ->
                _userMessage.value = "Successfully recorded marks for $count students!"
                onSaved()
            }.onFailure { err ->
                _userMessage.value = "Error saving marks: ${err.message}"
            }
        }
    }

    // === FEES ===
    fun recordPayment(
        feeId: String,
        studentId: String,
        amount: Double,
        paymentMode: String,
        date: String,
        notes: String,
        onSuccess: (FeePayment) -> Unit
    ) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            try {
                val payment = repository.recordFeePayment(
                    schoolId = sId,
                    feeId = feeId,
                    studentId = studentId,
                    amount = amount,
                    paymentMode = paymentMode,
                    date = date,
                    notes = notes,
                    recordedByProfileId = _currentUser.value?.id
                )
                _userMessage.value = "Payment of ₹$amount recorded! Receipt: ${payment.receipt_no}"
                onSuccess(payment)
            } catch (e: Exception) {
                _userMessage.value = "Error recording fee: ${e.message}"
            }
        }
    }

    fun updateFeeStructure(fee: FeeStructure) {
        viewModelScope.launch {
            repository.updateFeeStructure(fee)
            _userMessage.value = "Fee structure updated"
        }
    }

    // === STUDENTS CRUD ===
    fun addStudent(student: Student, initialFee: Double = 6500.0, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addStudent(student, initialFee)
            _userMessage.value = "Student ${student.full_name} enrolled successfully!"
            onComplete()
        }
    }

    fun updateStudent(student: Student, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _userMessage.value = "Student updated"
            onComplete()
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _userMessage.value = "Student ${student.full_name} removed"
        }
    }

    // === TEACHERS CRUD ===
    fun addTeacher(teacher: Teacher, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addTeacher(teacher)
            _userMessage.value = "Teacher ${teacher.full_name} added"
            onComplete()
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            _userMessage.value = "Teacher removed"
        }
    }

    fun assignTeacher(teacherId: String, classId: String, subjectId: String) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val assignment = TeacherAssignment(
                school_id = sId,
                teacher_id = teacherId,
                class_id = classId,
                subject_id = subjectId
            )
            repository.assignTeacher(assignment)
            _userMessage.value = "Teacher assigned to class & subject"
        }
    }

    // === CLASSES & SUBJECTS ===
    fun addClass(name: String, section: String) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val sc = SchoolClass(
                school_id = sId,
                name = name,
                section = section
            )
            repository.addClass(sc)
            _userMessage.value = "Class $name-$section created"
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.deleteClass(schoolClass)
            _userMessage.value = "Class deleted"
        }
    }

    fun addSubject(classId: String, name: String, code: String) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val sub = Subject(
                school_id = sId,
                class_id = classId,
                name = name,
                code = code
            )
            repository.addSubject(sub)
            _userMessage.value = "Subject $name added"
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _userMessage.value = "Subject deleted"
        }
    }

    // === CSV / BULK IMPORT ===
    fun importStudentsCsv(classId: String, csvText: String, onComplete: (Int) -> Unit) {
        val sId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val res = repository.importStudentsFromCsv(sId, classId, csvText)
            res.onSuccess { count ->
                _userMessage.value = "Successfully imported $count students from CSV!"
                onComplete(count)
            }.onFailure { err ->
                _userMessage.value = "CSV Import failed: ${err.message}"
            }
        }
    }

    // === SUPABASE SETTINGS ===
    fun getSupabaseConfig(): SupabaseConfig = repository.getSupabaseConfig()

    fun updateSupabaseConfig(config: SupabaseConfig) {
        repository.updateSupabaseConfig(config)
        viewModelScope.launch {
            val res = repository.testSupabaseConnection()
            res.onSuccess { msg ->
                _supabaseStatus.value = msg
                _userMessage.value = msg
            }.onFailure { err ->
                _supabaseStatus.value = "Supabase notice: ${err.message}"
                _userMessage.value = "Supabase notice: ${err.message}"
            }
        }
    }
}
