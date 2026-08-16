package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // === SCHOOLS ===
    @Query("SELECT * FROM schools ORDER BY name ASC")
    fun getAllSchools(): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE id = :schoolId LIMIT 1")
    suspend fun getSchoolById(schoolId: String): School?

    @Query("SELECT * FROM schools WHERE id = :schoolId LIMIT 1")
    fun observeSchoolById(schoolId: String): Flow<School?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: School)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    @Update
    suspend fun updateSchool(school: School)

    // === PROFILES ===
    @Query("SELECT * FROM profiles WHERE school_id = :schoolId")
    fun getProfilesBySchool(schoolId: String): Flow<List<UserProfile>>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): UserProfile?

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    // === ACADEMIC YEARS ===
    @Query("SELECT * FROM academic_years WHERE school_id = :schoolId ORDER BY is_active DESC, name DESC")
    fun getAcademicYears(schoolId: String): Flow<List<AcademicYear>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(year: AcademicYear)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYears(years: List<AcademicYear>)

    // === CLASSES ===
    @Query("SELECT * FROM classes WHERE school_id = :schoolId ORDER BY name ASC, section ASC")
    fun getClasses(schoolId: String): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE id = :classId LIMIT 1")
    suspend fun getClassById(classId: String): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // === SUBJECTS ===
    @Query("SELECT * FROM subjects WHERE school_id = :schoolId ORDER BY name ASC")
    fun getSubjects(schoolId: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE school_id = :schoolId AND class_id = :classId ORDER BY name ASC")
    fun getSubjectsByClass(schoolId: String, classId: String): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // === TEACHERS ===
    @Query("SELECT * FROM teachers WHERE school_id = :schoolId ORDER BY full_name ASC")
    fun getTeachers(schoolId: String): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :teacherId LIMIT 1")
    suspend fun getTeacherById(teacherId: String): Teacher?

    @Query("SELECT * FROM teachers WHERE user_id = :userId LIMIT 1")
    suspend fun getTeacherByUserId(userId: String): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    // === TEACHER ASSIGNMENTS ===
    @Query("SELECT * FROM teacher_assignments WHERE school_id = :schoolId")
    fun getAssignments(schoolId: String): Flow<List<TeacherAssignment>>

    @Query("SELECT * FROM teacher_assignments WHERE school_id = :schoolId AND teacher_id = :teacherId")
    fun getAssignmentsByTeacher(schoolId: String, teacherId: String): Flow<List<TeacherAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: TeacherAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<TeacherAssignment>)

    @Query("DELETE FROM teacher_assignments WHERE teacher_id = :teacherId")
    suspend fun deleteAssignmentsForTeacher(teacherId: String)

    // === STUDENTS ===
    @Query("SELECT * FROM students WHERE school_id = :schoolId ORDER BY roll_no ASC, full_name ASC")
    fun getStudents(schoolId: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE school_id = :schoolId AND class_id = :classId ORDER BY roll_no ASC, full_name ASC")
    fun getStudentsByClass(schoolId: String, classId: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): Student?

    @Query("SELECT * FROM students WHERE user_id = :userId LIMIT 1")
    suspend fun getStudentByUserId(userId: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Delete
    suspend fun deleteStudent(student: Student)

    // === EXAMS ===
    @Query("SELECT * FROM exams WHERE school_id = :schoolId ORDER BY start_date DESC")
    fun getExams(schoolId: String): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    // === MARKS ===
    @Query("SELECT * FROM marks WHERE school_id = :schoolId")
    fun getMarks(schoolId: String): Flow<List<Mark>>

    @Query("SELECT * FROM marks WHERE school_id = :schoolId AND exam_id = :examId AND subject_id = :subjectId")
    fun getMarksByExamAndSubject(schoolId: String, examId: String, subjectId: String): Flow<List<Mark>>

    @Query("SELECT * FROM marks WHERE school_id = :schoolId AND student_id = :studentId")
    fun getMarksByStudent(schoolId: String, studentId: String): Flow<List<Mark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: Mark)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<Mark>)

    // === ATTENDANCE ===
    @Query("SELECT * FROM attendance WHERE school_id = :schoolId")
    fun getAttendance(schoolId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE school_id = :schoolId AND date = :date AND class_id = :classId")
    fun getAttendanceByDateAndClass(schoolId: String, date: String, classId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE school_id = :schoolId AND date = :date")
    fun getAttendanceByDate(schoolId: String, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE school_id = :schoolId AND student_id = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(schoolId: String, studentId: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)

    // === FEES & PAYMENTS ===
    @Query("SELECT * FROM fees WHERE school_id = :schoolId")
    fun getFees(schoolId: String): Flow<List<FeeStructure>>

    @Query("SELECT * FROM fees WHERE school_id = :schoolId AND student_id = :studentId")
    fun getFeesByStudent(schoolId: String, studentId: String): Flow<List<FeeStructure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeStructure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<FeeStructure>)

    @Query("SELECT * FROM fee_payments WHERE school_id = :schoolId ORDER BY payment_date DESC")
    fun getFeePayments(schoolId: String): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE school_id = :schoolId AND student_id = :studentId ORDER BY payment_date DESC")
    fun getFeePaymentsByStudent(schoolId: String, studentId: String): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(payment: FeePayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayments(payments: List<FeePayment>)

    // Clear tenant data if requested
    @Query("DELETE FROM students WHERE school_id = :schoolId")
    suspend fun clearStudents(schoolId: String)
}
