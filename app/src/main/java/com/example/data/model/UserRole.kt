package com.example.data.model

enum class UserRole(val displayName: String, val hindiName: String) {
    ADMIN("School Admin", "स्कूल प्रबंधक"),
    TEACHER("Teacher", "शिक्षक"),
    STUDENT("Student", "छात्र / छात्रा");

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.lowercase().trim()) {
                "admin", "school_admin", "principal" -> ADMIN
                "teacher", "faculty" -> TEACHER
                "student", "pupil" -> STUDENT
                else -> STUDENT
            }
        }
    }
}
