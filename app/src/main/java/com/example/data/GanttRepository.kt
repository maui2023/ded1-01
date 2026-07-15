package com.example.data

import kotlinx.coroutines.flow.Flow

class GanttRepository(private val ganttDao: GanttDao) {
    val allProjects: Flow<List<GanttProject>> = ganttDao.getAllProjects()

    fun getTasksForProject(projectId: Int): Flow<List<GanttTask>> {
        return ganttDao.getTasksForProject(projectId)
    }

    suspend fun insertProject(project: GanttProject): Long {
        return ganttDao.insertProject(project)
    }

    suspend fun updateProject(project: GanttProject) {
        ganttDao.updateProject(project)
    }

    suspend fun deleteProjectById(projectId: Int) {
        ganttDao.deleteProjectById(projectId)
    }

    suspend fun insertTask(task: GanttTask): Long {
        return ganttDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<GanttTask>) {
        ganttDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: GanttTask) {
        ganttDao.updateTask(task)
    }

    suspend fun deleteTaskById(taskId: Int) {
        ganttDao.deleteTaskById(taskId)
    }
}
