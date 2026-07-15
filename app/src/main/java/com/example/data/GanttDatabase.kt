package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GanttProject::class, GanttTask::class], version = 1, exportSchema = false)
abstract class GanttDatabase : RoomDatabase() {
    abstract fun ganttDao(): GanttDao

    companion object {
        @Volatile
        private var INSTANCE: GanttDatabase? = null

        fun getDatabase(context: Context): GanttDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GanttDatabase::class.java,
                    "gantt_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
