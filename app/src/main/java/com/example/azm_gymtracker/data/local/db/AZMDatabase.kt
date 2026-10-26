package com.example.azm_gymtracker.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.azm_gymtracker.data.local.dao.ExerciseDao
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.dao.WorkoutDao
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for AZM / عزم.
 *
 * Schema version history:
 * - Version 1: Initial schema (members, exercises, workout_sessions, workout_exercises, workout_sets).
 *
 * Configuration:
 * - Write-Ahead Logging (WAL) journal mode for improved concurrent read performance.
 * - Foreign key support enforced at the SQLite level.
 */
@Database(
    entities = [
        MemberEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AZMDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        private const val DB_NAME = "azm_database.db"

        @Volatile
        private var INSTANCE: AZMDatabase? = null

        fun getInstance(context: Context): AZMDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AZMDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AZMDatabase::class.java,
                DB_NAME,
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addCallback(PrePopulateCallback())
                .build()
        }
    }

    /**
     * Callback that pre-populates the exercise catalog on first database creation.
     * Uses [Dispatchers.IO] to avoid blocking the main thread.
     */
    private class PrePopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.exerciseDao().insertExercises(PRELOADED_EXERCISES)
                }
            }
        }
    }
}

/**
 * Standard gym exercise catalog preloaded on first launch.
 * All exercises include both English and Arabic names for RTL support.
 */
private val PRELOADED_EXERCISES = listOf(
    // ── CHEST ──────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Bench Press", nameAr = "ضغط المقعد", category = "CHEST"),
    ExerciseEntity(nameEn = "Incline Bench Press", nameAr = "ضغط المقعد المائل", category = "CHEST"),
    ExerciseEntity(nameEn = "Decline Bench Press", nameAr = "ضغط المقعد المنخفض", category = "CHEST"),
    ExerciseEntity(nameEn = "Chest Fly", nameAr = "تمرين الفراشة", category = "CHEST"),
    ExerciseEntity(nameEn = "Push-Ups", nameAr = "ضغطات الأرض", category = "CHEST"),
    ExerciseEntity(nameEn = "Cable Crossover", nameAr = "كروس أوفر بالكابل", category = "CHEST"),
    ExerciseEntity(nameEn = "Dumbbell Pullover", nameAr = "بولوفر بالدمبل", category = "CHEST"),

    // ── BACK ──────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Deadlift", nameAr = "رفعة الموتى", category = "BACK"),
    ExerciseEntity(nameEn = "Pull-Ups", nameAr = "عقلة", category = "BACK"),
    ExerciseEntity(nameEn = "Lat Pulldown", nameAr = "سحب عمودي", category = "BACK"),
    ExerciseEntity(nameEn = "Barbell Row", nameAr = "تجديف بالبار", category = "BACK"),
    ExerciseEntity(nameEn = "Seated Cable Row", nameAr = "تجديف بالكابل جلوساً", category = "BACK"),
    ExerciseEntity(nameEn = "T-Bar Row", nameAr = "تجديف تي بار", category = "BACK"),
    ExerciseEntity(nameEn = "Hyperextension", nameAr = "تمديد ظهري", category = "BACK"),

    // ── LEGS ──────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Squat", nameAr = "القرفصاء", category = "LEGS"),
    ExerciseEntity(nameEn = "Leg Press", nameAr = "ضغط الرجل", category = "LEGS"),
    ExerciseEntity(nameEn = "Romanian Deadlift", nameAr = "رفعة الموتى الرومانية", category = "LEGS"),
    ExerciseEntity(nameEn = "Leg Curl", nameAr = "كيرل الرجل", category = "LEGS"),
    ExerciseEntity(nameEn = "Leg Extension", nameAr = "تمديد الرجل", category = "LEGS"),
    ExerciseEntity(nameEn = "Lunges", nameAr = "طعنات الرجل", category = "LEGS"),
    ExerciseEntity(nameEn = "Calf Raise", nameAr = "رفعة الساق", category = "LEGS"),

    // ── SHOULDERS ─────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Overhead Press", nameAr = "ضغط فوق الرأس", category = "SHOULDERS"),
    ExerciseEntity(nameEn = "Dumbbell Shoulder Press", nameAr = "ضغط الكتف بالدمبل", category = "SHOULDERS"),
    ExerciseEntity(nameEn = "Lateral Raise", nameAr = "رفع جانبي", category = "SHOULDERS"),
    ExerciseEntity(nameEn = "Front Raise", nameAr = "رفع أمامي", category = "SHOULDERS"),
    ExerciseEntity(nameEn = "Rear Delt Fly", nameAr = "فراشة الكتف الخلفي", category = "SHOULDERS"),
    ExerciseEntity(nameEn = "Upright Row", nameAr = "تجديف عمودي", category = "SHOULDERS"),

    // ── ARMS ──────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Bicep Curl", nameAr = "كيرل البايسبس", category = "ARMS"),
    ExerciseEntity(nameEn = "Hammer Curl", nameAr = "كيرل المطرقة", category = "ARMS"),
    ExerciseEntity(nameEn = "Preacher Curl", nameAr = "كيرل المنبر", category = "ARMS"),
    ExerciseEntity(nameEn = "Tricep Pushdown", nameAr = "دفع الترايسبس", category = "ARMS"),
    ExerciseEntity(nameEn = "Skull Crusher", nameAr = "سكول كراشر", category = "ARMS"),
    ExerciseEntity(nameEn = "Dips", nameAr = "ديبس", category = "ARMS"),
    ExerciseEntity(nameEn = "Close-Grip Bench Press", nameAr = "ضغط القبضة الضيقة", category = "ARMS"),

    // ── CORE ──────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Plank", nameAr = "بلانك", category = "CORE"),
    ExerciseEntity(nameEn = "Crunches", nameAr = "ثني البطن", category = "CORE"),
    ExerciseEntity(nameEn = "Leg Raises", nameAr = "رفع الرجلين", category = "CORE"),
    ExerciseEntity(nameEn = "Russian Twist", nameAr = "لف روسي", category = "CORE"),
    ExerciseEntity(nameEn = "Cable Crunch", nameAr = "ثني البطن بالكابل", category = "CORE"),
    ExerciseEntity(nameEn = "Ab Wheel Rollout", nameAr = "عجلة البطن", category = "CORE"),

    // ── CARDIO ────────────────────────────────────────────────────────────────
    ExerciseEntity(nameEn = "Treadmill Run", nameAr = "جري على جهاز المشي", category = "CARDIO"),
    ExerciseEntity(nameEn = "Jump Rope", nameAr = "حبل القفز", category = "CARDIO"),
    ExerciseEntity(nameEn = "Cycling", nameAr = "ركوب الدراجة", category = "CARDIO"),
    ExerciseEntity(nameEn = "Rowing Machine", nameAr = "جهاز التجديف", category = "CARDIO"),
)
