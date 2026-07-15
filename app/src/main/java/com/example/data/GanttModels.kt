package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class GanttProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GanttProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GanttTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val phase: String, // e.g. "Strategy", "Design", "Development", "Deployment"
    val startDayOffset: Int, // Day offset from start of project
    val durationDays: Int, // Duration in days
    val progress: Float, // 0.0 to 1.0
    val colorHex: String, // Hex color for the task
    val dependsOnTaskId: Int? = null
)
