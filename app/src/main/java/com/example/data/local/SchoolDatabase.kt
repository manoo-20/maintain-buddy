package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        School::class,
        UserProfile::class,
        AcademicYear::class,
        SchoolClass::class,
        Subject::class,
        Teacher::class,
        TeacherAssignment::class,
        Student::class,
        Exam::class,
        Mark::class,
        AttendanceRecord::class,
        FeeStructure::class,
        FeePayment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "gramin_shala_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
