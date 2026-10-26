package com.example.azm_gymtracker.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.azm_gymtracker.data.local.dao.AppSettingDao
import com.example.azm_gymtracker.data.local.dao.ExerciseDao
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.dao.WorkoutDao
import com.example.azm_gymtracker.data.local.entity.AppSettingEntity
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.data.local.entity.SessionParticipantEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity

/**
 * Room database for AZM / عزم (Schema v2).
 *
 * Schema version history:
 * - Version 1: Initial single-member schema.
 * - Version 2: Multi-participant shared session support (`session_participants`),
 *              isolated set ownership (`workout_sets.member_id`), native `app_settings`,
 *              and case-insensitive collation for member names.
 */
@Database(
    entities = [
        MemberEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        SessionParticipantEntity::class,
        AppSettingEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AZMDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        const val DB_NAME = "azm_database.db"

        @Volatile
        private var INSTANCE: AZMDatabase? = null

        /**
         * Migration from Schema v1 to v2:
         * 1. Creates `session_participants` cross-reference table.
         * 2. Migrates existing sessions to `session_participants`.
         * 3. Rebuilds `workout_sets` adding non-nullable `member_id`, `is_completed`, `completed_at`.
         * 4. Rebuilds `workout_sessions` removing single `member_id`, adding `paused_total_ms` and `notes`.
         * 5. Creates `app_settings` table.
         * 6. Rebuilds `members` with `COLLATE NOCASE` on `name`.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create session_participants table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `session_participants` (
                        `session_id` INTEGER NOT NULL,
                        `member_id` INTEGER NOT NULL,
                        `join_order` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`session_id`, `member_id`),
                        FOREIGN KEY(`session_id`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`member_id`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_participants_session_id` ON `session_participants` (`session_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_participants_member_id` ON `session_participants` (`member_id`)")

                // 2. Populate session_participants from existing workout_sessions
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `session_participants` (`session_id`, `member_id`, `join_order`)
                    SELECT `id`, `member_id`, 0 FROM `workout_sessions` WHERE `member_id` > 0
                    """.trimIndent(),
                )

                // 3. Rebuild workout_sets with member_id, is_completed, completed_at
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sets_v2` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workout_exercise_id` INTEGER NOT NULL,
                        `member_id` INTEGER NOT NULL,
                        `set_number` INTEGER NOT NULL,
                        `reps` INTEGER NOT NULL,
                        `weight_kg` REAL,
                        `is_completed` INTEGER NOT NULL DEFAULT 0,
                        `completed_at` INTEGER,
                        FOREIGN KEY(`workout_exercise_id`) REFERENCES `workout_exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`member_id`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `workout_sets_v2` (`id`, `workout_exercise_id`, `member_id`, `set_number`, `reps`, `weight_kg`, `is_completed`, `completed_at`)
                    SELECT ws.`id`, ws.`workout_exercise_id`, COALESCE(s.`member_id`, 1), ws.`set_number`, ws.`reps`, ws.`weight_kg`, 1, NULL
                    FROM `workout_sets` ws
                    LEFT JOIN `workout_exercises` we ON ws.`workout_exercise_id` = we.`id`
                    LEFT JOIN `workout_sessions` s ON we.`session_id` = s.`id`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `workout_sets`")
                db.execSQL("ALTER TABLE `workout_sets_v2` RENAME TO `workout_sets`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workout_exercise_id` ON `workout_sets` (`workout_exercise_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workout_exercise_id_member_id` ON `workout_sets` (`workout_exercise_id`, `member_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_member_id` ON `workout_sets` (`member_id`)")

                // 4. Rebuild workout_sessions removing member_id, renaming finished_at to ended_at, mapping DISCARDED -> CANCELLED
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sessions_v2` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `started_at` INTEGER NOT NULL,
                        `ended_at` INTEGER,
                        `paused_total_ms` INTEGER NOT NULL DEFAULT 0,
                        `status` TEXT NOT NULL,
                        `notes` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `workout_sessions_v2` (`id`, `started_at`, `ended_at`, `paused_total_ms`, `status`, `notes`)
                    SELECT `id`, `started_at`, `finished_at`, 0,
                           CASE WHEN `status` = 'DISCARDED' THEN 'CANCELLED' ELSE `status` END,
                           NULL
                    FROM `workout_sessions`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `workout_sessions`")
                db.execSQL("ALTER TABLE `workout_sessions_v2` RENAME TO `workout_sessions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_status` ON `workout_sessions` (`status`)")

                // 5. Create app_settings table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_settings` (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        `value` TEXT NOT NULL
                    )
                    """.trimIndent(),
                )

                // 6. Rebuild members with COLLATE NOCASE
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `members_v2` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL COLLATE NOCASE,
                        `color_hex` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `members_v2` (`id`, `name`, `color_hex`, `created_at`)
                    SELECT `id`, `name`, `color_hex`, `created_at` FROM `members`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `members`")
                db.execSQL("ALTER TABLE `members_v2` RENAME TO `members`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_members_name` ON `members` (`name`)")
            }
        }

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
                .addMigrations(MIGRATION_1_2)
                .addCallback(DatabaseCreationCallback())
                .build()
        }

        /**
         * Direct pre-population using SupportSQLiteDatabase to guarantee
         * exercise availability without depending on uninitialized DAOs.
         */
        fun seedPreloadedExercisesDirect(db: SupportSQLiteDatabase) {
            val countCursor = db.query("SELECT COUNT(*) FROM exercises")
            val count = if (countCursor.moveToFirst()) countCursor.getInt(0) else 0
            countCursor.close()

            if (count == 0) {
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    for (exercise in PRELOADED_EXERCISES) {
                        values.clear()
                        values.put("name_en", exercise.nameEn)
                        values.put("name_ar", exercise.nameAr)
                        values.put("category", exercise.category)
                        values.put("is_custom", if (exercise.isCustom) 1 else 0)
                        values.putNull("creator_member_id")
                        db.insert("exercises", SQLiteDatabase.CONFLICT_IGNORE, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    /**
     * Database callback that seeds exercises immediately on DB creation and verifies on open.
     */
    private class DatabaseCreationCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedPreloadedExercisesDirect(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            seedPreloadedExercisesDirect(db)
        }
    }
}

/**
 * Standard gym exercise catalog preloaded on first launch.
 */
val PRELOADED_EXERCISES = listOf(
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
