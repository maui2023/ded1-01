package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GanttViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GanttRepository

    init {
        val database = GanttDatabase.getDatabase(application)
        repository = GanttRepository(database.ganttDao())
        
        // Auto-populate with mock projects if empty
        viewModelScope.launch {
            repository.allProjects.first().let { projects ->
                if (projects.isEmpty()) {
                    populateDefaultData()
                } else {
                    // Set default selected project
                    _selectedProject.value = projects.first()
                }
            }
        }
    }

    val allProjects: StateFlow<List<GanttProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<GanttProject?>(null)
    val selectedProject: StateFlow<GanttProject?> = _selectedProject.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectTasks: StateFlow<List<GanttTask>> = _selectedProject
        .flatMapLatest { project ->
            if (project != null) {
                repository.getTasksForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _zoomLevel = MutableStateFlow("Daily") // "Daily" or "Weekly"
    val zoomLevel: StateFlow<String> = _zoomLevel.asStateFlow()

    // Dialog state for project creation
    private val _showAddProjectDialog = MutableStateFlow(false)
    val showAddProjectDialog = _showAddProjectDialog.asStateFlow()

    // Dialog/Drawer state for task add/edit
    private val _editingTask = MutableStateFlow<GanttTask?>(null)
    val editingTask = _editingTask.asStateFlow()
    
    private val _isNewTask = MutableStateFlow(false)
    val isNewTask = _isNewTask.asStateFlow()

    // Export preview and simulation state
    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog = _showExportDialog.asStateFlow()
    
    private val _exportType = MutableStateFlow("HTML") // "HTML" or "PDF"
    val exportType = _exportType.asStateFlow()

    private val _isSimulatingExport = MutableStateFlow(false)
    val isSimulatingExport = _isSimulatingExport.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress = _exportProgress.asStateFlow()

    fun selectProject(project: GanttProject) {
        _selectedProject.value = project
    }

    fun setZoomLevel(level: String) {
        _zoomLevel.value = level
    }

    fun setShowAddProjectDialog(show: Boolean) {
        _showAddProjectDialog.value = show
    }

    fun startAddTask() {
        val currentProject = _selectedProject.value ?: return
        _isNewTask.value = true
        _editingTask.value = GanttTask(
            projectId = currentProject.id,
            title = "",
            phase = "Design",
            startDayOffset = 0,
            durationDays = 5,
            progress = 0f,
            colorHex = "#8B5CF6"
        )
    }

    fun startEditTask(task: GanttTask) {
        _isNewTask.value = false
        _editingTask.value = task
    }

    fun cancelEditTask() {
        _editingTask.value = null
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
        if (!show) {
            _isSimulatingExport.value = false
            _exportProgress.value = 0f
        }
    }

    fun setExportType(type: String) {
        _exportType.value = type
    }

    fun addProject(name: String, description: String) {
        viewModelScope.launch {
            val project = GanttProject(name = name, description = description)
            val newId = repository.insertProject(project)
            val createdProject = project.copy(id = newId.toInt())
            _selectedProject.value = createdProject
            _showAddProjectDialog.value = false
            
            // Add initial tasks so it's not empty
            repository.insertTasks(listOf(
                GanttTask(
                    projectId = newId.toInt(),
                    title = "Initial Planning",
                    phase = "Strategy",
                    startDayOffset = 0,
                    durationDays = 4,
                    progress = 0.5f,
                    colorHex = "#6366F1"
                ),
                GanttTask(
                    projectId = newId.toInt(),
                    title = "Concept Design",
                    phase = "Design",
                    startDayOffset = 3,
                    durationDays = 5,
                    progress = 0f,
                    colorHex = "#8B5CF6"
                )
            ))
        }
    }

    fun deleteCurrentProject() {
        val currentProject = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.deleteProjectById(currentProject.id)
            val projects = repository.allProjects.first()
            if (projects.isNotEmpty()) {
                _selectedProject.value = projects.first()
            } else {
                _selectedProject.value = null
            }
        }
    }

    fun saveTask(task: GanttTask) {
        viewModelScope.launch {
            if (_isNewTask.value) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            _editingTask.value = null
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
            _editingTask.value = null
        }
    }

    fun runExportSimulation(onFinished: () -> Unit) {
        viewModelScope.launch {
            _isSimulatingExport.value = true
            _exportProgress.value = 0f
            for (i in 1..20) {
                kotlinx.coroutines.delay(100)
                _exportProgress.value = i / 20f
            }
            _isSimulatingExport.value = false
            onFinished()
        }
    }

    private suspend fun populateDefaultData() {
        val p1Id = repository.insertProject(GanttProject(
            name = "Interactive HTML Proposal",
            description = "Proposal Gantt chart styling system using modern CSS grids, premium gradients, and export modules."
        ))

        val tasks1 = listOf(
            GanttTask(
                projectId = p1Id.toInt(),
                title = "Discovery & Project Kickoff",
                phase = "Strategy",
                startDayOffset = 0,
                durationDays = 4,
                progress = 1.0f,
                colorHex = "#6366F1"
            ),
            GanttTask(
                projectId = p1Id.toInt(),
                title = "User Experience Architecture",
                phase = "Design",
                startDayOffset = 3,
                durationDays = 6,
                progress = 1.0f,
                colorHex = "#8B5CF6"
            ),
            GanttTask(
                projectId = p1Id.toInt(),
                title = "UI Moodboards & Gradients",
                phase = "Design",
                startDayOffset = 6,
                durationDays = 5,
                progress = 0.85f,
                colorHex = "#EC4899"
            ),
            GanttTask(
                projectId = p1Id.toInt(),
                title = "Front-end Flexbox Framework",
                phase = "Development",
                startDayOffset = 10,
                durationDays = 8,
                progress = 0.60f,
                colorHex = "#3B82F6"
            ),
            GanttTask(
                projectId = p1Id.toInt(),
                title = "PDF Rendering Engine Setup",
                phase = "Development",
                startDayOffset = 15,
                durationDays = 7,
                progress = 0.25f,
                colorHex = "#10B981"
            ),
            GanttTask(
                projectId = p1Id.toInt(),
                title = "Client Sandbox Deployment",
                phase = "Launch",
                startDayOffset = 21,
                durationDays = 5,
                progress = 0.0f,
                colorHex = "#F59E0B"
            )
        )
        repository.insertTasks(tasks1)

        val p2Id = repository.insertProject(GanttProject(
            name = "Mobile App Design System",
            description = "Developing custom Jetpack Compose themes, component tokens, and fluid typography sheets."
        ))

        val tasks2 = listOf(
            GanttTask(
                projectId = p2Id.toInt(),
                title = "Compose Typography Hierarchy",
                phase = "Design",
                startDayOffset = 0,
                durationDays = 5,
                progress = 1.0f,
                colorHex = "#8B5CF6"
            ),
            GanttTask(
                projectId = p2Id.toInt(),
                title = "Glassmorphic Surface Design",
                phase = "Design",
                startDayOffset = 4,
                durationDays = 6,
                progress = 0.90f,
                colorHex = "#EC4899"
            ),
            GanttTask(
                projectId = p2Id.toInt(),
                title = "M3 Component Tokens",
                phase = "Development",
                startDayOffset = 9,
                durationDays = 8,
                progress = 0.50f,
                colorHex = "#3B82F6"
            ),
            GanttTask(
                projectId = p2Id.toInt(),
                title = "Interactive Motion Prototyping",
                phase = "Development",
                startDayOffset = 15,
                durationDays = 7,
                progress = 0.10f,
                colorHex = "#10B981"
            )
        )
        repository.insertTasks(tasks2)

        // Set selected project to the first one
        val firstProject = repository.allProjects.first().firstOrNull()
        _selectedProject.value = firstProject
    }
    
    fun generateExportCode(project: GanttProject?, tasks: List<GanttTask>): String {
        if (project == null) return "<!-- No project selected -->"
        
        val maxDays = if (tasks.isEmpty()) 15 else tasks.maxOf { it.startDayOffset + it.durationDays }.coerceAtLeast(15)
        
        val taskItemsHtml = StringBuilder()
        val cssGridTemplate = "180px repeat($maxDays, 1fr)"
        
        tasks.forEachIndexed { index, task ->
            val startCol = task.startDayOffset + 2
            val endCol = startCol + task.durationDays
            val progressPercent = (task.progress * 100).toInt()
            
            taskItemsHtml.append("""
            <!-- Task ${index + 1}: ${task.title} -->
            <div class="task-label">${task.title} <span class="phase-badge">${task.phase}</span></div>
            <div class="gantt-bar-container" style="grid-column: $startCol / $endCol;">
                <div class="gantt-bar" style="background: linear-gradient(135deg, ${task.colorHex}, ${task.colorHex}dd);">
                    <div class="gantt-progress" style="width: ${progressPercent}%;"></div>
                    <span class="gantt-bar-text">${progressPercent}%</span>
                </div>
            </div>
            """).append("\n")
        }
        
        val headerDaysHtml = StringBuilder()
        for (i in 1..maxDays) {
            headerDaysHtml.append("<div class=\"day-header\">D$i</div>\n")
        }

        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${project.name} - Gantt Chart Proposal</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap');
        
        :root {
            --bg-gradient: linear-gradient(135deg, #0f172a, #1e1b4b);
            --panel-bg: rgba(255, 255, 255, 0.03);
            --panel-border: rgba(255, 255, 255, 0.08);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            background: var(--bg-gradient);
            color: var(--text-main);
            margin: 0;
            padding: 40px 20px;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .container {
            width: 100%;
            max-width: 1100px;
            background: var(--panel-bg);
            backdrop-filter: blur(20px);
            border: 1px solid var(--panel-border);
            border-radius: 24px;
            padding: 32px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }

        header {
            margin-bottom: 32px;
            border-bottom: 1px solid var(--panel-border);
            padding-bottom: 24px;
        }

        h1 {
            font-size: 2.25rem;
            font-weight: 700;
            margin: 0 0 8px 0;
            background: linear-gradient(to right, #818cf8, #f472b6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        p {
            color: var(--text-muted);
            font-size: 1rem;
            margin: 0;
            line-height: 1.6;
        }

        .gantt-chart {
            display: grid;
            grid-template-columns: $cssGridTemplate;
            row-gap: 16px;
            column-gap: 4px;
            overflow-x: auto;
            padding-bottom: 16px;
        }

        .gantt-chart::-webkit-scrollbar {
            height: 8px;
        }

        .gantt-chart::-webkit-scrollbar-track {
            background: rgba(255, 255, 255, 0.02);
            border-radius: 4px;
        }

        .gantt-chart::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.1);
            border-radius: 4px;
        }

        /* Headers */
        .header-spacer {
            grid-column: 1;
            font-weight: 600;
            color: var(--text-muted);
            display: flex;
            align-items: center;
            font-size: 0.85rem;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }

        .day-header {
            text-align: center;
            padding: 8px 4px;
            font-weight: 600;
            color: var(--text-muted);
            font-size: 0.8rem;
            background: rgba(255, 255, 255, 0.01);
            border-radius: 6px;
            border: 1px solid rgba(255, 255, 255, 0.03);
        }

        /* Tasks */
        .task-label {
            grid-column: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            font-size: 0.95rem;
            font-weight: 500;
            padding-right: 16px;
            border-right: 1px solid var(--panel-border);
        }

        .phase-badge {
            font-size: 0.7rem;
            font-weight: 600;
            color: #818cf8;
            margin-top: 4px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .gantt-bar-container {
            display: flex;
            align-items: center;
            padding: 4px 0;
        }

        .gantt-bar {
            width: 100%;
            height: 36px;
            border-radius: 10px;
            position: relative;
            overflow: hidden;
            display: flex;
            align-items: center;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            transition: transform 0.2s ease;
        }

        .gantt-bar:hover {
            transform: scaleY(1.04);
        }

        .gantt-progress {
            height: 100%;
            background: rgba(255, 255, 255, 0.18);
            position: absolute;
            left: 0;
            top: 0;
            border-right: 1px solid rgba(255, 255, 255, 0.25);
        }

        .gantt-bar-text {
            position: relative;
            z-index: 2;
            font-size: 0.8rem;
            font-weight: 700;
            color: #ffffff;
            margin-left: 12px;
            text-shadow: 0 1px 4px rgba(0,0,0,0.4);
        }

        @media print {
            body {
                background: #ffffff !important;
                color: #0f172a !important;
                padding: 0;
            }
            .container {
                box-shadow: none !important;
                border: none !important;
                background: transparent !important;
                padding: 0 !important;
                backdrop-filter: none !important;
            }
            h1 {
                background: none !important;
                -webkit-text-fill-color: initial !important;
                color: #0f172a !important;
            }
            p {
                color: #475569 !important;
            }
            .day-header {
                border: 1px solid #e2e8f0 !important;
                color: #475569 !important;
            }
            .task-label {
                border-right: 1px solid #e2e8f0 !important;
                color: #0f172a !important;
            }
            .phase-badge {
                color: #4f46e5 !important;
            }
            .gantt-bar {
                box-shadow: none !important;
                border: 1px solid rgba(0, 0, 0, 0.1) !important;
            }
            .gantt-progress {
                background: rgba(0, 0, 0, 0.08) !important;
                border-right: 1px solid rgba(0, 0, 0, 0.15) !important;
            }
            .gantt-bar-text {
                color: #ffffff !important;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>${project.name}</h1>
            <p>${project.description}</p>
        </header>
        <div class="gantt-chart">
            <div class="header-spacer">Timeline Tasks</div>
            $headerDaysHtml
            
            $taskItemsHtml
        </div>
    </div>
</body>
</html>"""
    }
}
