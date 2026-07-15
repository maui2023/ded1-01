package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GanttDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<GanttProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: GanttProject): Long

    @Update
    suspend fun updateProject(project: GanttProject)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Int)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY startDayOffset ASC")
    fun getTasksForProject(projectId: Int): Flow<List<GanttTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: GanttTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<GanttTask>)

    @Update
    suspend fun updateTask(task: GanttTask)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
}
