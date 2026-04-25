package com.culturesync.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.culturesync.app.data.local.daos.*
import com.culturesync.app.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        SessionEntity::class,
        TechniqueEntity::class,
        SessionFrameEntity::class,
        TemplateEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun techniqueDao(): TechniqueDao
    abstract fun sessionFrameDao(): SessionFrameDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Migration 1 → 2: added syncStatus column to users and sessions,
         * added remoteId column to users and sessions, added templates table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE sessions ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        techniqueId INTEGER NOT NULL,
                        masterName TEXT NOT NULL,
                        recordingDate TEXT NOT NULL,
                        frameCount INTEGER NOT NULL,
                        landmarkDataJson TEXT NOT NULL,
                        isAveraged INTEGER NOT NULL,
                        FOREIGN KEY(techniqueId) REFERENCES techniques(id) ON DELETE CASCADE
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_templates_techniqueId ON templates (techniqueId)")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "culturesync.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
