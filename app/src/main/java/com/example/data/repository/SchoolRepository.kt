package com.example.data.repository

import android.content.Context
import com.example.data.local.SchoolDao
import com.example.data.local.SchoolDatabase
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
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SchoolRepository(
    private val schoolDao: SchoolDao,
    private val supabaseClient: SupabaseClient = SupabaseClient()
) {

    companion object {
        @Volatile
        private var INSTANCE: SchoolRepository? = null

        fun getInstance(context: Context): SchoolRepository {
            return INSTANCE ?: synchronized(this) {
                val db = SchoolDatabase.getDatabase(context)
                val instance = SchoolRepository(db.schoolDao(), SupabaseClient())
                INSTANCE = instance
                instance
            }
        }
    }

    // === SEED INITIAL SAMPLE DATA FOR MULTI-TENANT ISOLATION DEMO ===
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingSchools = schoolDao.getAllSchools().first()
        if (existingSchools.isNotEmpty()) return@withContext

        val school1Id = "school-rampur-001"
        val school2Id = "school-sonpur-002"

        // School 1: Rampur
        val school1 = School(
            id = school1Id,
            name = "Saraswati Vidya Mandir",
            code = "SVM-RAMPUR",
            village = "Rampur Kalan",
            district = "Sitapur",
            state = "Uttar Pradesh",
            udise_number = "09240100101",
            phone = "+91 94150 12345",
            email = "admin@svmrampur.edu.in"
        )

        // School 2: Sonpur
        val school2 = School(
            id = school2Id,
            name = "Adarsh Gramin High School",
            code = "AGHS-SONPUR",
            village = "Sonpur Basti",
            district = "Saran",
            state = "Bihar",
            udise_number = "10180200204",
            phone = "+91 98350 67890",
            email = "contact@sonpurschool.org"
        )

        schoolDao.insertSchools(listOf(school1, school2))

        // Populate School 1
        populateSchoolData(school1Id, "2025-2026")
        // Populate School 2 (isolated tenant)
        populateSchoolData(school2Id, "2025-2026")
    }

    private suspend fun populateSchoolData(schoolId: String, academicYearName: String) {
        val yearId = UUID.randomUUID().toString()
        val academicYear = AcademicYear(
            id = yearId,
            school_id = schoolId,
            name = academicYearName,
            start_date = "2025-04-01",
            end_date = "2026-03-31",
            is_active = true
        )
        schoolDao.insertAcademicYear(academicYear)

        // Admin Profile
        val adminProfileId = "admin-$schoolId"
        val adminProfile = UserProfile(
            id = adminProfileId,
            school_id = schoolId,
            role = "admin",
            full_name = if (schoolId.contains("rampur")) "Shri Rajeshwar Sharma (Prabandhak)" else "Dr. Anand Kumar Mishra (Principal)",
            email = if (schoolId.contains("rampur")) "admin@svmrampur.edu.in" else "admin@sonpur.edu.in",
            phone = "+91 94150 11223"
        )
        schoolDao.insertProfile(adminProfile)

        // Classes 1 to 10
        val classNames = listOf("Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
        val classes = classNames.map { cName ->
            SchoolClass(
                id = "$schoolId-${cName.replace(" ", "").lowercase()}-A",
                school_id = schoolId,
                academic_year_id = yearId,
                name = cName,
                section = "A"
            )
        }
        schoolDao.insertClasses(classes)

        val class5Id = "$schoolId-class5-A"
        val class8Id = "$schoolId-class8-A"
        val class10Id = "$schoolId-class10-A"

        // Subjects for Class 5, 8, 10
        val subjects = mutableListOf<Subject>()
        val subjectNames = listOf("Hindi", "Mathematics", "Science", "Social Science", "English", "Sanskrit")
        for (sc in classes) {
            for (subName in subjectNames) {
                subjects.add(
                    Subject(
                        id = "$schoolId-${sc.name.replace(" ", "").lowercase()}-${subName.lowercase()}",
                        school_id = schoolId,
                        class_id = sc.id,
                        name = subName,
                        code = subName.take(3).uppercase()
                    )
                )
            }
        }
        schoolDao.insertSubjects(subjects)

        // Teachers
        val teacher1UserId = "teacher-user-1-$schoolId"
        val teacher2UserId = "teacher-user-2-$schoolId"
        val teacher3UserId = "teacher-user-3-$schoolId"

        val teacherProfile1 = UserProfile(
            id = teacher1UserId,
            school_id = schoolId,
            role = "teacher",
            full_name = "Master Ram Prakash Pandey",
            email = "ramprakash@school.edu.in",
            phone = "+91 98765 43210"
        )
        val teacherProfile2 = UserProfile(
            id = teacher2UserId,
            school_id = schoolId,
            role = "teacher",
            full_name = "Smt. Sunita Devi (TGT Science)",
            email = "sunita@school.edu.in",
            phone = "+91 98765 43211"
        )
        val teacherProfile3 = UserProfile(
            id = teacher3UserId,
            school_id = schoolId,
            role = "teacher",
            full_name = "Shri Vikram Singh (Maths)",
            email = "vikram@school.edu.in",
            phone = "+91 98765 43212"
        )
        schoolDao.insertProfiles(listOf(teacherProfile1, teacherProfile2, teacherProfile3))

        val teacher1 = Teacher(
            id = "t1-$schoolId",
            school_id = schoolId,
            user_id = teacher1UserId,
            employee_id = "EMP-01",
            full_name = "Master Ram Prakash Pandey",
            phone = "+91 98765 43210",
            qualification = "M.A. (Hindi), B.Ed",
            joining_date = "2020-07-01"
        )
        val teacher2 = Teacher(
            id = "t2-$schoolId",
            school_id = schoolId,
            user_id = teacher2UserId,
            employee_id = "EMP-02",
            full_name = "Smt. Sunita Devi (TGT Science)",
            phone = "+91 98765 43211",
            qualification = "B.Sc, B.Ed",
            joining_date = "2021-08-15"
        )
        val teacher3 = Teacher(
            id = "t3-$schoolId",
            school_id = schoolId,
            user_id = teacher3UserId,
            employee_id = "EMP-03",
            full_name = "Shri Vikram Singh (Maths)",
            phone = "+91 98765 43212",
            qualification = "M.Sc (Mathematics)",
            joining_date = "2022-04-01"
        )
        schoolDao.insertTeachers(listOf(teacher1, teacher2, teacher3))

        // Teacher assignments
        val assignments = listOf(
            TeacherAssignment(id = UUID.randomUUID().toString(), school_id = schoolId, teacher_id = teacher1.id, class_id = class5Id, subject_id = "$schoolId-class5-hindi"),
            TeacherAssignment(id = UUID.randomUUID().toString(), school_id = schoolId, teacher_id = teacher1.id, class_id = class8Id, subject_id = "$schoolId-class8-hindi"),
            TeacherAssignment(id = UUID.randomUUID().toString(), school_id = schoolId, teacher_id = teacher2.id, class_id = class5Id, subject_id = "$schoolId-class5-science"),
            TeacherAssignment(id = UUID.randomUUID().toString(), school_id = schoolId, teacher_id = teacher3.id, class_id = class5Id, subject_id = "$schoolId-class5-mathematics"),
            TeacherAssignment(id = UUID.randomUUID().toString(), school_id = schoolId, teacher_id = teacher3.id, class_id = class10Id, subject_id = "$schoolId-class10-mathematics")
        )
        schoolDao.insertAssignments(assignments)

        // Students in Class 5 (Rural Indian students names)
        val studentNames = listOf(
            Triple("Amit Kumar", "Male", "Ramesh Kumar"),
            Triple("Priya Verma", "Female", "Dinesh Verma"),
            Triple("Rahul Yadav", "Male", "Surendra Yadav"),
            Triple("Anjali Sharma", "Female", "Manoj Sharma"),
            Triple("Vikas Maurya", "Male", "Ram Lal Maurya"),
            Triple("Komal Tiwari", "Female", "Ajay Tiwari"),
            Triple("Saurabh Patel", "Male", "Santosh Patel"),
            Triple("Neha Kumari", "Female", "Brijesh Kumar"),
            Triple("Deepak Chauhan", "Male", "Vijay Chauhan"),
            Triple("Pooja Rawat", "Female", "Hira Lal Rawat")
        )

        val students = mutableListOf<Student>()
        val studentProfiles = mutableListOf<UserProfile>()
        val feeStructures = mutableListOf<FeeStructure>()
        val feePayments = mutableListOf<FeePayment>()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val attendanceList = mutableListOf<AttendanceRecord>()

        studentNames.forEachIndexed { index, (sName, gender, father) ->
            val roll = (index + 1).toString().padStart(2, '0')
            val adm = "ADM-${2025000 + index + 1}"
            val sId = "std-$schoolId-c5-$roll"
            val uId = "user-std-$schoolId-$roll"

            // Student profile
            val sp = UserProfile(
                id = uId,
                school_id = schoolId,
                role = "student",
                full_name = sName,
                email = "student$roll@school.edu.in",
                phone = "+91 94500 ${10000 + index}"
            )
            studentProfiles.add(sp)

            val student = Student(
                id = sId,
                school_id = schoolId,
                user_id = uId,
                class_id = class5Id,
                admission_no = adm,
                roll_no = roll,
                full_name = sName,
                gender = gender,
                dob = "2013-0${(index % 9) + 1}-15",
                guardian_name = father,
                guardian_phone = "+91 94500 ${10000 + index}",
                village_address = "Gram Panchayat Rampur, Tola ${index + 1}",
                aadhaar_last4 = "${1000 + index * 37}"
            )
            students.add(student)

            // Fees
            val totalFee = 6500.0 // Annual fee in Rupees
            val discount = if (index % 3 == 0) 1000.0 else 0.0
            val feeId = "fee-$sId"
            val fee = FeeStructure(
                id = feeId,
                school_id = schoolId,
                student_id = sId,
                academic_year_id = yearId,
                title = "Annual Tuition, Books & Examination Fee",
                total_amount = totalFee,
                discount_amount = discount,
                due_date = "2025-10-31"
            )
            feeStructures.add(fee)

            // Payments (Some paid full, some partial, some due)
            val netPayable = totalFee - discount
            val paidAmount = when (index % 4) {
                0 -> netPayable
                1 -> 3500.0
                2 -> 2000.0
                else -> 0.0
            }

            if (paidAmount > 0) {
                feePayments.add(
                    FeePayment(
                        id = UUID.randomUUID().toString(),
                        school_id = schoolId,
                        fee_id = feeId,
                        student_id = sId,
                        receipt_no = "REC-${schoolId.take(3).uppercase()}-25-${100 + index}",
                        amount_paid = paidAmount,
                        payment_date = "2025-07-${10 + index}",
                        payment_mode = if (index % 2 == 0) "cash" else "upi",
                        recorded_by = adminProfileId,
                        notes = "Installment 1 received with thanks"
                    )
                )
            }

            // Attendance today
            val status = if (index == 3 || index == 8) "absent" else if (index == 6) "late" else "present"
            attendanceList.add(
                AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    school_id = schoolId,
                    student_id = sId,
                    class_id = class5Id,
                    date = todayDate,
                    status = status,
                    remarks = if (status == "absent") "Fever/Leave application" else "",
                    marked_by = teacherProfile1.id
                )
            )
        }

        schoolDao.insertProfiles(studentProfiles)
        schoolDao.insertStudents(students)
        schoolDao.insertFees(feeStructures)
        schoolDao.insertFeePayments(feePayments)
        schoolDao.insertAttendanceList(attendanceList)

        // Exams & Marks
        val exam1 = Exam(
            id = "exam-ut1-$schoolId",
            school_id = schoolId,
            academic_year_id = yearId,
            name = "Unit Test 1 (मासिक परीक्षा)",
            start_date = "2025-07-20",
            end_date = "2025-07-25"
        )
        val exam2 = Exam(
            id = "exam-hy-$schoolId",
            school_id = schoolId,
            academic_year_id = yearId,
            name = "Half Yearly Examination (अर्धवार्षिक)",
            start_date = "2025-10-15",
            end_date = "2025-10-25"
        )
        schoolDao.insertExams(listOf(exam1, exam2))

        val marksList = mutableListOf<Mark>()
        val class5HindiSubId = "$schoolId-class5-hindi"
        val class5MathsSubId = "$schoolId-class5-mathematics"
        val class5ScienceSubId = "$schoolId-class5-science"

        students.take(8).forEachIndexed { i, std ->
            val hMarks = (70 + (i * 3) % 28).toDouble()
            val mMarks = (65 + (i * 4) % 32).toDouble()
            val sMarks = (68 + (i * 3) % 27).toDouble()

            marksList.add(
                Mark(
                    id = UUID.randomUUID().toString(),
                    school_id = schoolId,
                    exam_id = exam1.id,
                    student_id = std.id,
                    subject_id = class5HindiSubId,
                    marks_obtained = hMarks,
                    max_marks = 100.0,
                    grade = calculateGrade(hMarks, 100.0),
                    entered_by = teacherProfile1.id
                )
            )
            marksList.add(
                Mark(
                    id = UUID.randomUUID().toString(),
                    school_id = schoolId,
                    exam_id = exam1.id,
                    student_id = std.id,
                    subject_id = class5MathsSubId,
                    marks_obtained = mMarks,
                    max_marks = 100.0,
                    grade = calculateGrade(mMarks, 100.0),
                    entered_by = teacherProfile3.id
                )
            )
            marksList.add(
                Mark(
                    id = UUID.randomUUID().toString(),
                    school_id = schoolId,
                    exam_id = exam1.id,
                    student_id = std.id,
                    subject_id = class5ScienceSubId,
                    marks_obtained = sMarks,
                    max_marks = 100.0,
                    grade = calculateGrade(sMarks, 100.0),
                    entered_by = teacherProfile2.id
                )
            )
        }
        schoolDao.insertMarks(marksList)
    }

    private fun calculateGrade(obtained: Double, max: Double): String {
        val pct = (obtained / max) * 100.0
        return when {
            pct >= 90.0 -> "A+"
            pct >= 80.0 -> "A"
            pct >= 70.0 -> "B+"
            pct >= 60.0 -> "B"
            pct >= 50.0 -> "C"
            pct >= 33.0 -> "D"
            else -> "F"
        }
    }

    // === SCHOOL TENANT OPERATIONS ===
    fun observeAllSchools(): Flow<List<School>> = schoolDao.getAllSchools()
    suspend fun getSchoolById(schoolId: String): School? = schoolDao.getSchoolById(schoolId)
    fun observeSchool(schoolId: String): Flow<School?> = schoolDao.observeSchoolById(schoolId)

    suspend fun registerSchool(
        name: String,
        code: String,
        village: String,
        district: String,
        state: String,
        udise: String,
        phone: String,
        email: String,
        adminName: String,
        adminPassword: String
    ): Pair<School, UserProfile> = withContext(Dispatchers.IO) {
        val schoolId = "school-${UUID.randomUUID().toString().take(8)}"
        val school = School(
            id = schoolId,
            name = name,
            code = code.uppercase(),
            village = village,
            district = district,
            state = state,
            udise_number = udise,
            phone = phone,
            email = email
        )
        schoolDao.insertSchool(school)

        val adminProfileId = "admin-$schoolId"
        val adminProfile = UserProfile(
            id = adminProfileId,
            school_id = schoolId,
            role = "admin",
            full_name = adminName,
            email = email,
            phone = phone
        )
        schoolDao.insertProfile(adminProfile)

        // Initialize default academic year and classes
        populateSchoolData(schoolId, "2025-2026")

        Pair(school, adminProfile)
    }

    suspend fun updateSchool(school: School) = withContext(Dispatchers.IO) {
        schoolDao.updateSchool(school)
    }

    // === PROFILES & AUTH ===
    suspend fun getProfileById(profileId: String): UserProfile? = schoolDao.getProfileById(profileId)
    suspend fun getProfileByEmail(email: String): UserProfile? = schoolDao.getProfileByEmail(email)
    fun observeProfiles(schoolId: String): Flow<List<UserProfile>> = schoolDao.getProfilesBySchool(schoolId)

    // === ACADEMIC YEARS ===
    fun observeAcademicYears(schoolId: String): Flow<List<AcademicYear>> = schoolDao.getAcademicYears(schoolId)
    suspend fun addAcademicYear(year: AcademicYear) = schoolDao.insertAcademicYear(year)

    // === CLASSES & SUBJECTS ===
    fun observeClasses(schoolId: String): Flow<List<SchoolClass>> = schoolDao.getClasses(schoolId)
    suspend fun addClass(schoolClass: SchoolClass) = schoolDao.insertClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = schoolDao.deleteClass(schoolClass)

    fun observeSubjects(schoolId: String): Flow<List<Subject>> = schoolDao.getSubjects(schoolId)
    fun observeSubjectsByClass(schoolId: String, classId: String): Flow<List<Subject>> = schoolDao.getSubjectsByClass(schoolId, classId)
    suspend fun addSubject(subject: Subject) = schoolDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = schoolDao.deleteSubject(subject)

    // === TEACHERS & ASSIGNMENTS ===
    fun observeTeachers(schoolId: String): Flow<List<Teacher>> = schoolDao.getTeachers(schoolId)
    suspend fun getTeacherByUserId(userId: String): Teacher? = schoolDao.getTeacherByUserId(userId)
    suspend fun addTeacher(teacher: Teacher, profile: UserProfile? = null) = withContext(Dispatchers.IO) {
        if (profile != null) {
            schoolDao.insertProfile(profile)
        }
        schoolDao.insertTeacher(teacher)
    }
    suspend fun deleteTeacher(teacher: Teacher) = schoolDao.deleteTeacher(teacher)

    fun observeAssignments(schoolId: String): Flow<List<TeacherAssignment>> = schoolDao.getAssignments(schoolId)
    fun observeAssignmentsByTeacher(schoolId: String, teacherId: String): Flow<List<TeacherAssignment>> = schoolDao.getAssignmentsByTeacher(schoolId, teacherId)
    suspend fun assignTeacher(assignment: TeacherAssignment) = schoolDao.insertAssignment(assignment)
    suspend fun clearAssignmentsForTeacher(teacherId: String) = schoolDao.deleteAssignmentsForTeacher(teacherId)

    // === STUDENTS ===
    fun observeStudents(schoolId: String): Flow<List<Student>> = schoolDao.getStudents(schoolId)
    fun observeStudentsByClass(schoolId: String, classId: String): Flow<List<Student>> = schoolDao.getStudentsByClass(schoolId, classId)
    suspend fun getStudentById(studentId: String): Student? = schoolDao.getStudentById(studentId)
    suspend fun getStudentByUserId(userId: String): Student? = schoolDao.getStudentByUserId(userId)
    suspend fun addStudent(student: Student, initialFee: Double = 6500.0) = withContext(Dispatchers.IO) {
        schoolDao.insertStudent(student)
        val fee = FeeStructure(
            id = "fee-${student.id}",
            school_id = student.school_id,
            student_id = student.id,
            title = "Annual Tuition & Development Fee",
            total_amount = initialFee,
            discount_amount = 0.0,
            due_date = "2025-10-31"
        )
        schoolDao.insertFee(fee)
    }
    suspend fun updateStudent(student: Student) = schoolDao.insertStudent(student)
    suspend fun deleteStudent(student: Student) = schoolDao.deleteStudent(student)

    // === EXAMS & MARKS ===
    fun observeExams(schoolId: String): Flow<List<Exam>> = schoolDao.getExams(schoolId)
    suspend fun addExam(exam: Exam) = schoolDao.insertExam(exam)

    fun observeMarks(schoolId: String): Flow<List<Mark>> = schoolDao.getMarks(schoolId)
    fun observeMarksByExamAndSubject(schoolId: String, examId: String, subjectId: String): Flow<List<Mark>> = schoolDao.getMarksByExamAndSubject(schoolId, examId, subjectId)
    fun observeMarksByStudent(schoolId: String, studentId: String): Flow<List<Mark>> = schoolDao.getMarksByStudent(schoolId, studentId)

    suspend fun saveMarks(
        schoolId: String,
        examId: String,
        subjectId: String,
        entries: List<Pair<String, Double>>, // studentId to marksObtained
        maxMarks: Double,
        enteredByProfileId: String?
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Validation: marks must not be negative or exceed maxMarks
            for ((_, obtained) in entries) {
                if (obtained < 0 || obtained > maxMarks) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Marks must be between 0 and $maxMarks")
                    )
                }
            }

            val marksToSave = entries.map { (studentId, obtained) ->
                Mark(
                    id = "$schoolId-$examId-$studentId-$subjectId",
                    school_id = schoolId,
                    exam_id = examId,
                    student_id = studentId,
                    subject_id = subjectId,
                    marks_obtained = obtained,
                    max_marks = maxMarks,
                    grade = calculateGrade(obtained, maxMarks),
                    entered_by = enteredByProfileId
                )
            }
            schoolDao.insertMarks(marksToSave)
            Result.success(marksToSave.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === ATTENDANCE ===
    fun observeAttendance(schoolId: String): Flow<List<AttendanceRecord>> = schoolDao.getAttendance(schoolId)
    fun observeAttendanceByDateAndClass(schoolId: String, date: String, classId: String): Flow<List<AttendanceRecord>> = schoolDao.getAttendanceByDateAndClass(schoolId, date, classId)
    fun observeAttendanceByDate(schoolId: String, date: String): Flow<List<AttendanceRecord>> = schoolDao.getAttendanceByDate(schoolId, date)
    fun observeAttendanceByStudent(schoolId: String, studentId: String): Flow<List<AttendanceRecord>> = schoolDao.getAttendanceByStudent(schoolId, studentId)

    suspend fun saveAttendance(
        schoolId: String,
        classId: String,
        date: String,
        records: List<Pair<String, String>>, // studentId to status ("present", "absent", "late", "leave")
        markedByProfileId: String?
    ) = withContext(Dispatchers.IO) {
        val attendanceEntities = records.map { (studentId, status) ->
            AttendanceRecord(
                id = "$schoolId-$studentId-$date",
                school_id = schoolId,
                student_id = studentId,
                class_id = classId,
                date = date,
                status = status,
                marked_by = markedByProfileId
            )
        }
        schoolDao.insertAttendanceList(attendanceEntities)
    }

    // === FEES & PAYMENTS ===
    fun observeFees(schoolId: String): Flow<List<FeeStructure>> = schoolDao.getFees(schoolId)
    fun observeFeesByStudent(schoolId: String, studentId: String): Flow<List<FeeStructure>> = schoolDao.getFeesByStudent(schoolId, studentId)
    suspend fun updateFeeStructure(fee: FeeStructure) = schoolDao.insertFee(fee)

    fun observeFeePayments(schoolId: String): Flow<List<FeePayment>> = schoolDao.getFeePayments(schoolId)
    fun observeFeePaymentsByStudent(schoolId: String, studentId: String): Flow<List<FeePayment>> = schoolDao.getFeePaymentsByStudent(schoolId, studentId)

    suspend fun recordFeePayment(
        schoolId: String,
        feeId: String,
        studentId: String,
        amount: Double,
        paymentMode: String,
        date: String,
        notes: String,
        recordedByProfileId: String?
    ): FeePayment = withContext(Dispatchers.IO) {
        val receiptNumber = "REC-${schoolId.take(4).uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}"
        val payment = FeePayment(
            id = UUID.randomUUID().toString(),
            school_id = schoolId,
            fee_id = feeId,
            student_id = studentId,
            receipt_no = receiptNumber,
            amount_paid = amount,
            payment_date = date,
            payment_mode = paymentMode,
            recorded_by = recordedByProfileId,
            notes = notes
        )
        schoolDao.insertFeePayment(payment)
        payment
    }

    // === CSV / EXCEL BULK IMPORT PARSER ===
    suspend fun importStudentsFromCsv(
        schoolId: String,
        defaultClassId: String,
        csvContent: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return@withContext Result.failure(IllegalArgumentException("CSV content is empty"))

            var importedCount = 0
            val studentsToInsert = mutableListOf<Student>()
            val feesToInsert = mutableListOf<FeeStructure>()

            val header = lines.first().lowercase()
            val hasHeader = header.contains("admission") || header.contains("name") || header.contains("roll")
            val dataLines = if (hasHeader) lines.drop(1) else lines

            for (line in dataLines) {
                val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                if (parts.size >= 2) {
                    val admNo = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "ADM-${System.currentTimeMillis().toString().takeLast(5)}"
                    val rollNo = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: (importedCount + 1).toString()
                    val fullName = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "Student $rollNo"
                    val gender = parts.getOrNull(3)?.takeIf { it.isNotBlank() } ?: "Male"
                    val guardian = parts.getOrNull(4) ?: "Guardian of $fullName"
                    val phone = parts.getOrNull(5) ?: "+91 94150 00000"
                    val village = parts.getOrNull(6) ?: "Village Area"

                    val studentId = "$schoolId-csv-$admNo"
                    val student = Student(
                        id = studentId,
                        school_id = schoolId,
                        class_id = defaultClassId,
                        admission_no = admNo,
                        roll_no = rollNo,
                        full_name = fullName,
                        gender = gender,
                        guardian_name = guardian,
                        guardian_phone = phone,
                        village_address = village
                    )
                    studentsToInsert.add(student)

                    feesToInsert.add(
                        FeeStructure(
                            id = "fee-$studentId",
                            school_id = schoolId,
                            student_id = studentId,
                            total_amount = 6000.0,
                            discount_amount = 0.0
                        )
                    )
                    importedCount++
                }
            }

            if (studentsToInsert.isNotEmpty()) {
                schoolDao.insertStudents(studentsToInsert)
                schoolDao.insertFees(feesToInsert)
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === SUPABASE CLOUD SYNC & STATUS ===
    fun getSupabaseConfig(): SupabaseConfig = supabaseClient.getConfig()
    fun updateSupabaseConfig(config: SupabaseConfig) = supabaseClient.updateConfig(config)
    suspend fun testSupabaseConnection(): Result<String> = supabaseClient.testConnection()
}
