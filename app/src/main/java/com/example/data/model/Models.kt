package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schools")
data class School(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val code: String,
    val village: String,
    val district: String,
    val state: String = "Uttar Pradesh",
    val udise_number: String = "",
    val phone: String = "",
    val email: String = "",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["school_id", "id"])]
)
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val role: String, // 'admin', 'teacher', 'student'
    val full_name: String,
    val email: String = "",
    val phone: String = "",
    val avatar_url: String = "",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "academic_years",
    indices = [Index(value = ["school_id"])]
)
data class AcademicYear(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val name: String, // e.g. "2025-2026"
    val start_date: String, // "2025-04-01"
    val end_date: String, // "2026-03-31"
    val is_active: Boolean = true,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "classes",
    indices = [Index(value = ["school_id", "name", "section"], unique = true)]
)
data class SchoolClass(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val academic_year_id: String? = null,
    val name: String, // "Class 1", "Class 5", "Class 10"
    val section: String = "A",
    val created_at: Long = System.currentTimeMillis()
) {
    val displayName: String get() = if (section.isNotBlank()) "$name - $section" else name
}

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["school_id", "class_id"])]
)
data class Subject(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val class_id: String,
    val name: String, // "Hindi", "Mathematics", "Science", "Social Science", "English"
    val code: String = "",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["school_id"])]
)
data class Teacher(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val user_id: String? = null,
    val employee_id: String = "",
    val full_name: String,
    val phone: String = "",
    val qualification: String = "B.Ed, B.Sc",
    val joining_date: String = "2024-07-01",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "teacher_assignments",
    indices = [Index(value = ["school_id", "teacher_id", "class_id", "subject_id"], unique = true)]
)
data class TeacherAssignment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val teacher_id: String,
    val class_id: String,
    val subject_id: String = "",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["school_id", "admission_no"], unique = true),
        Index(value = ["school_id", "class_id"])
    ]
)
data class Student(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val user_id: String? = null,
    val class_id: String,
    val admission_no: String,
    val roll_no: String,
    val full_name: String,
    val gender: String = "Male", // "Male", "Female", "Other"
    val dob: String = "2012-05-15",
    val guardian_name: String = "",
    val guardian_phone: String = "",
    val village_address: String = "",
    val aadhaar_last4: String = "",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exams",
    indices = [Index(value = ["school_id"])]
)
data class Exam(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val academic_year_id: String? = null,
    val name: String, // "Unit Test 1", "Quarterly Exam", "Half Yearly Exam", "Annual Exam"
    val start_date: String = "2025-09-10",
    val end_date: String = "2025-09-20",
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "marks",
    indices = [Index(value = ["school_id", "exam_id", "student_id", "subject_id"], unique = true)]
)
data class Mark(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val exam_id: String,
    val student_id: String,
    val subject_id: String,
    val marks_obtained: Double,
    val max_marks: Double = 100.0,
    val grade: String = "A",
    val entered_by: String? = null,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["school_id", "student_id", "date"], unique = true),
        Index(value = ["school_id", "date", "class_id"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val student_id: String,
    val class_id: String,
    val date: String, // "YYYY-MM-DD"
    val status: String, // "present", "absent", "late", "leave"
    val remarks: String = "",
    val marked_by: String? = null,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fees",
    indices = [Index(value = ["school_id", "student_id"])]
)
data class FeeStructure(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val student_id: String,
    val academic_year_id: String? = null,
    val title: String = "Annual Tuition & Development Fee",
    val total_amount: Double,
    val discount_amount: Double = 0.0,
    val due_date: String = "2025-10-31",
    val created_at: Long = System.currentTimeMillis()
) {
    val netPayable: Double get() = (total_amount - discount_amount).coerceAtLeast(0.0)
}

@Entity(
    tableName = "fee_payments",
    indices = [Index(value = ["school_id", "fee_id"])]
)
data class FeePayment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val school_id: String,
    val fee_id: String,
    val student_id: String,
    val receipt_no: String,
    val amount_paid: Double,
    val payment_date: String, // "YYYY-MM-DD"
    val payment_mode: String = "cash", // "cash", "upi", "bank_transfer", "cheque"
    val recorded_by: String? = null,
    val notes: String = "",
    val created_at: Long = System.currentTimeMillis()
)
