package com.example.opsc7311_sem2_2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new table
        database.execSQL("CREATE TABLE IF NOT EXISTS `category_stats` (`categoryName` TEXT NOT NULL, `totalMinutes` INTEGER NOT NULL, PRIMARY KEY(`categoryName`))")
    }
}

@Database(entities = [TaskItem::class, CategoryStats::class], version = 2, exportSchema = false)
@TypeConverters(TaskSessionConverter::class)

abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskItemDao(): TaskItemDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null


        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }

        }

    }

}

