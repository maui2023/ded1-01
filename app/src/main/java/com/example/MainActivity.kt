package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.GanttViewModel
import com.example.ui.theme.*
import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.glassmorphic(
    cornerRadius: Dp = 20.dp,
    alpha: Float = 0.55f,
    borderAlpha: Float = 0.25f
): Modifier = this.then(
    Modifier
        .graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                renderEffect = RenderEffect.createBlurEffect(
                    15f, 15f,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
        .clip(RoundedCornerShape(cornerRadius))
        .background(ObsidianSurface.copy(alpha = alpha))
        .border(
            BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        ObsidianBorder.copy(alpha = borderAlpha)
                    )
                )
            ),
            RoundedCornerShape(cornerRadius)
        )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBg)
                ) { innerPadding ->
                    GanttAppContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GanttAppContent(
    modifier: Modifier = Modifier,
    viewModel: GanttViewModel = viewModel()
) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val tasks by viewModel.projectTasks.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()

    val showAddProjectDialog by viewModel.showAddProjectDialog.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val isNewTask by viewModel.isNewTask.collectAsStateWithLifecycle()
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBg,
                        ObsidianSurfaceElevated
                    )
                )
            )
            .drawBehind {
                // Background ambient glows to simulate luxury design
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.2f)
                    ),
                    radius = size.width * 0.5f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.7f)
                    ),
                    radius = size.width * 0.4f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            AppHeaderRow(
                onExportCenterClick = {
                    if (selectedProject != null) {
                        viewModel.setExportType("HTML")
                        viewModel.setShowExportDialog(true)
                    } else {
                        Toast.makeText(context, "Please create/select a project first", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Project Selector Section
            ProjectSelectionRow(
                projects = projects,
                selectedProject = selectedProject,
                onSelectProject = { viewModel.selectProject(it) },
                onAddProjectClick = { viewModel.setShowAddProjectDialog(true) },
                onDeleteProjectClick = { viewModel.deleteCurrentProject() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedProject == null) {
                // Empty state if no projects
                EmptyProjectsState(
                    modifier = Modifier.weight(1f),
                    onAddProjectClick = { viewModel.setShowAddProjectDialog(true) }
                )
            } else {
                // Main Dashboard Body
                DashboardControlsRow(
                    zoomLevel = zoomLevel,
                    onZoomChange = { viewModel.setZoomLevel(it) },
                    onAddTaskClick = { viewModel.startAddTask() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The Gantt Chart Canvas Panel
                GanttChartCard(
                    project = selectedProject!!,
                    tasks = tasks,
                    zoomLevel = zoomLevel,
                    onTaskClick = { viewModel.startEditTask(it) }
                )
            }
        }

        // --- dialogs ---

        if (showAddProjectDialog) {
            ProjectCreateDialog(
                onDismiss = { viewModel.setShowAddProjectDialog(false) },
                onConfirm = { name, desc -> viewModel.addProject(name, desc) }
            )
        }

        editingTask?.let { task ->
            TaskEditDialog(
                task = task,
                isNew = isNewTask,
                allTasks = tasks,
                onDismiss = { viewModel.cancelEditTask() },
                onSave = { viewModel.saveTask(it) },
                onDelete = { viewModel.deleteTask(it) }
            )
        }

        if (showExportDialog) {
            ExportDialog(
                project = selectedProject,
                tasks = tasks,
                viewModel = viewModel,
                onDismiss = { viewModel.setShowExportDialog(false) }
            )
        }
    }
}

@Composable
fun AppHeaderRow(onExportCenterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gantt Timeline",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                )
            }
            Text(
                text = "Premium Proposal Systems",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        }

        // Export Center glassmorphic action button
        Button(
            onClick = onExportCenterClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier
                .testTag("export_center_button")
                .border(
                    BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(NeonViolet, NeonPink)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = NeonViolet.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Export Center",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun ProjectSelectionRow(
    projects: List<GanttProject>,
    selectedProject: GanttProject?,
    onSelectProject: (GanttProject) -> Unit,
    onAddProjectClick: () -> Unit,
    onDeleteProjectClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Proposal Projects",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            )

            if (selectedProject != null) {
                IconButton(
                    onClick = onDeleteProjectClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete project",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Add Project" mini-card
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, ObsidianBorder), RoundedCornerShape(12.dp))
                    .background(ObsidianSurfaceElevated.copy(alpha = 0.5f))
                    .clickable(onClick = onAddProjectClick)
                    .testTag("add_project_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NeonViolet
                        )
                    )
                }
            }

            // Project cards
            projects.forEach { project ->
                val isSelected = selectedProject?.id == project.id
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        NeonViolet.copy(alpha = 0.25f),
                                        NeonIndigo.copy(alpha = 0.25f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        ObsidianSurface.copy(alpha = 0.8f),
                                        ObsidianSurfaceElevated.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        )
                        .border(
                            BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                brush = if (isSelected) {
                                    Brush.linearGradient(colors = listOf(NeonViolet, NeonIndigo))
                                } else {
                                    SolidColor(ObsidianBorder)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectProject(project) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = project.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        )
                        Text(
                            text = if (project.description.length > 25) {
                                project.description.take(25) + "..."
                            } else {
                                project.description
                            },
                            maxLines = 1,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsState(
    modifier: Modifier = Modifier,
    onAddProjectClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .glassmorphic(cornerRadius = 20.dp, alpha = 0.45f, borderAlpha = 0.3f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(NeonViolet.copy(alpha = 0.1f), CircleShape)
                    .border(BorderStroke(1.dp, NeonViolet.copy(alpha = 0.3f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Proposal Timelines Yet",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Establish your first professional Gantt chart proposal with gradients, custom intervals, and CSS exports.",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddProjectClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Proposal",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DashboardControlsRow(
    zoomLevel: String,
    onZoomChange: (String) -> Unit,
    onAddTaskClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Zoom levels (Daily / Weekly)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianSurfaceElevated)
                .border(BorderStroke(1.dp, ObsidianBorder), RoundedCornerShape(10.dp))
                .padding(3.dp)
        ) {
            listOf("Daily", "Weekly").forEach { level ->
                val isSelected = zoomLevel == level
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) ObsidianSurface else Color.Transparent
                        )
                        .clickable { onZoomChange(level) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) NeonViolet else TextSecondary
                        )
                    )
                }
            }
        }

        // Add task button
        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.testTag("add_task_button")
        ) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Task",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun GanttChartCard(
    project: GanttProject,
    tasks: List<GanttTask>,
    zoomLevel: String,
    onTaskClick: (GanttTask) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(cornerRadius = 20.dp, alpha = 0.45f, borderAlpha = 0.3f)
            .padding(16.dp)
    ) {
        // Project info inside Card
        Column {
            Text(
                text = project.name,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = project.description,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = TextSecondary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gantt chart scrollable container
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tasks in this proposal.",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Add Task' to build your timeline structure.",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    )
                }
            }
        } else {
            // Horizontal scroll container holding the entire Gantt widget
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Column holding headers and all task rows
                Column {
                    // Timeline Column Definitions
                    val numCols = if (zoomLevel == "Daily") 20 else 8
                    val colWidth = if (zoomLevel == "Daily") 48.dp else 80.dp
                    val labelWidth = 160.dp

                    // Headers
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Label spacing header
                        Box(
                            modifier = Modifier
                                .width(labelWidth)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Timeline Tasks",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            )
                        }

                        // Grid Columns Header Row
                        Row(horizontalArrangement = Arrangement.Start) {
                            for (i in 1..numCols) {
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .padding(horizontal = 2.dp)
                                        .background(ObsidianSurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .border(BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (zoomLevel == "Daily") "D$i" else "W$i",
                                        style = TextStyle(
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = NeonViolet
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Divider
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ObsidianBorder.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Task rows
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        tasks.forEach { task ->
                            GanttChartRow(
                                task = task,
                                colWidth = colWidth,
                                labelWidth = labelWidth,
                                maxCols = numCols,
                                zoomLevel = zoomLevel,
                                onTaskClick = onTaskClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GanttChartRow(
    task: GanttTask,
    colWidth: Dp,
    labelWidth: Dp,
    maxCols: Int,
    zoomLevel: String,
    onTaskClick: (GanttTask) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onTaskClick(task) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Task meta info
        Row(
            modifier = Modifier
                .width(labelWidth)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(android.graphics.Color.parseColor(task.colorHex)), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = task.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.phase,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    )
                }
            }
        }

        // Right Column: Gantt timeline bar
        Box(
            modifier = Modifier
                .width(colWidth * maxCols)
                .height(38.dp)
                .drawBehind {
                    // Draw soft vertical grid dividers
                    val strokeWidth = 1.dp.toPx()
                    val colWidthPx = colWidth.toPx()
                    for (i in 1..maxCols) {
                        drawLine(
                            color = ObsidianBorder.copy(alpha = 0.15f),
                            start = Offset(i * colWidthPx, 0f),
                            end = Offset(i * colWidthPx, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Map offsets to layout dimensions
            val startOffset: Float
            val spanWidth: Float

            if (zoomLevel == "Daily") {
                startOffset = task.startDayOffset.toFloat()
                spanWidth = task.durationDays.toFloat()
            } else {
                // Weekly zoom: each week is 7 days
                startOffset = task.startDayOffset / 7f
                spanWidth = task.durationDays / 7f
            }

            // Coerce values inside visual bounds
            val startDp = (startOffset * colWidth.value).dp
            val barWidthDp = (spanWidth * colWidth.value).coerceAtLeast(12f).dp

            val baseTaskColor = Color(android.graphics.Color.parseColor(task.colorHex))

            // Draw task bar
            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(barWidthDp)
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                baseTaskColor,
                                baseTaskColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                // Internal progress background overlay
                val progressFraction = task.progress.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .background(Color.White.copy(alpha = 0.18f))
                )

                // Optional label inside the bar if long enough
                if (spanWidth > 1.5f || (zoomLevel == "Daily" && spanWidth > 2f)) {
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCreateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .glassmorphic(cornerRadius = 24.dp, alpha = 0.85f, borderAlpha = 0.4f)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "New Proposal Project",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Name your timeline and detail the proposal scope.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Website Overhaul") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Proposal Scope Description") },
                    placeholder = { Text("e.g. Creating beautiful front-end designs with gradients.") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, desc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEditDialog(
    task: GanttTask,
    isNew: Boolean,
    allTasks: List<GanttTask>,
    onDismiss: () -> Unit,
    onSave: (GanttTask) -> Unit,
    onDelete: (Int) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var phase by remember { mutableStateOf(task.phase) }
    var startDayOffset by remember { mutableStateOf(task.startDayOffset.toFloat()) }
    var durationDays by remember { mutableStateOf(task.durationDays.toFloat()) }
    var progress by remember { mutableStateOf(task.progress * 100f) }
    var selectedColorHex by remember { mutableStateOf(task.colorHex) }
    var dependsOnTaskId by remember { mutableStateOf(task.dependsOnTaskId) }

    val phases = listOf("Strategy", "Design", "Development", "Launch")
    val colors = listOf(
        "#6366F1", // Indigo
        "#8B5CF6", // Violet
        "#EC4899", // Pink
        "#3B82F6", // Blue
        "#10B981", // Emerald
        "#F59E0B"  // Amber
    )

    var phaseDropdownExpanded by remember { mutableStateOf(false) }
    var depDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .glassmorphic(cornerRadius = 24.dp, alpha = 0.85f, borderAlpha = 0.4f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add Timeline Task" else "Modify Task Settings",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    )

                    if (!isNew) {
                        IconButton(onClick = { onDelete(task.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Category Picker
                Text(
                    text = "Phase Category",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, ObsidianBorder), RoundedCornerShape(10.dp))
                            .clickable { phaseDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = phase, color = TextPrimary, fontSize = 14.sp)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = phaseDropdownExpanded,
                        onDismissRequest = { phaseDropdownExpanded = false },
                        modifier = Modifier.glassmorphic(cornerRadius = 12.dp, alpha = 0.92f, borderAlpha = 0.35f)
                    ) {
                        phases.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = TextPrimary) },
                                onClick = {
                                    phase = p
                                    phaseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Start Day offset Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start Interval (Day Offset)",
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = TextSecondary)
                    )
                    Text(
                        text = "Day ${startDayOffset.toInt()}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonViolet)
                    )
                }
                Slider(
                    value = startDayOffset,
                    onValueChange = { startDayOffset = it },
                    valueRange = 0f..15f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = ObsidianBorder,
                        thumbColor = NeonViolet
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Duration Days Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Interval Length (Duration)",
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = TextSecondary)
                    )
                    Text(
                        text = "${durationDays.toInt()} Days",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonViolet)
                    )
                }
                Slider(
                    value = durationDays,
                    onValueChange = { durationDays = it },
                    valueRange = 1f..15f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = ObsidianBorder,
                        thumbColor = NeonViolet
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status / Progress Percentage",
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = TextSecondary)
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonEmerald)
                    )
                }
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NeonEmerald,
                        inactiveTrackColor = ObsidianBorder,
                        thumbColor = NeonEmerald
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Color Picker
                Text(
                    text = "Display Accent Color",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { cHex ->
                        val isSelected = selectedColorHex == cHex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(cHex)))
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) TextPrimary else Color.Transparent
                                    ),
                                    CircleShape
                                )
                                .clickable { selectedColorHex = cHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    task.copy(
                                        title = title,
                                        phase = phase,
                                        startDayOffset = startDayOffset.toInt(),
                                        durationDays = durationDays.toInt(),
                                        progress = progress / 100f,
                                        colorHex = selectedColorHex,
                                        dependsOnTaskId = dependsOnTaskId
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_task_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Timeline")
                    }
                }
            }
        }
    }
}

@Composable
fun ExportDialog(
    project: GanttProject?,
    tasks: List<GanttTask>,
    viewModel: GanttViewModel,
    onDismiss: () -> Unit
) {
    val exportType by viewModel.exportType.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulatingExport.collectAsStateWithLifecycle()
    val progress by viewModel.exportProgress.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val exportCode = remember(project, tasks) {
        viewModel.generateExportCode(project, tasks)
    }

    var simulationSuccessful by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .glassmorphic(cornerRadius = 24.dp, alpha = 0.85f, borderAlpha = 0.4f)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Export Center Sandbox",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Deliver your Gantt charts in premium formats.",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle tabs: HTML vs PDF
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurfaceElevated)
                        .padding(4.dp)
                ) {
                    listOf("HTML", "PDF").forEach { type ->
                        val isSelected = exportType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ObsidianSurface else Color.Transparent)
                                .clickable {
                                    viewModel.setExportType(type)
                                    simulationSuccessful = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (type == "HTML") "HTML/CSS Code" else "PDF Print Layout",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) NeonViolet else TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic display content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (exportType == "HTML") {
                        // Display HTML source code box
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Raw Responsive Source Code",
                                    style = TextStyle(fontSize = 11.sp, color = TextMuted)
                                )

                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(exportCode))
                                        Toast.makeText(context, "HTML/CSS Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("copy_code_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = NeonViolet,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Code", color = NeonViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF07080D))
                                    .border(BorderStroke(1.dp, ObsidianBorder), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                val scroll = rememberScrollState()
                                Text(
                                    text = exportCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA5B4FC),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scroll)
                                )
                            }
                        }
                    } else {
                        // Display PDF Simulation preview sheet
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "A4 Page Live Print Rendering Simulator",
                                style = TextStyle(fontSize = 11.sp, color = TextMuted)
                            )

                            // Mock vertical A4 paper container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Mock header on paper
                                    Text(
                                        text = project?.name ?: "Proposal Timeline",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = project?.description ?: "Proposal Scope Document",
                                        color = Color(0xFF475569),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )

                                    // Mock timeline rows
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        tasks.take(4).forEach { task ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // mock label
                                                Text(
                                                    text = task.title,
                                                    color = Color(0xFF1E293B),
                                                    fontSize = 8.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.width(64.dp)
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // mock gantt bar
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(12.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Color(0xFFF1F5F9))
                                                ) {
                                                    // bar colored progress representation
                                                    val offsetFraction = task.startDayOffset / 15f
                                                    val lengthFraction = task.durationDays / 15f
                                                    val progressFraction = task.progress

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(lengthFraction)
                                                            .offset(x = (offsetFraction * 140f).dp)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(Color(android.graphics.Color.parseColor(task.colorHex)).copy(alpha = 0.85f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .fillMaxWidth(progressFraction)
                                                                .background(Color.White.copy(alpha = 0.2f))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Mock footer on paper
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Page 1 of 1", color = Color(0xFF94A3B8), fontSize = 7.sp)
                                        Text("Rendered via Gantt Timeline Systems", color = Color(0xFF94A3B8), fontSize = 7.sp)
                                    }
                                }
                            }

                            if (simulationSuccessful) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PDF successfully generated! (Export Simulation Mockup Complete)",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions at the bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSimulating) {
                        // Loading simulator
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (exportType == "HTML") "Compiling gradient styles..." else "Rendering vector PDF columns...",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonViolet
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = NeonViolet,
                                trackColor = ObsidianBorder
                            )
                        }
                    } else {
                        // Normal operational actions
                        OutlinedButton(
                            onClick = onDismiss,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Dismiss")
                        }

                        if (exportType == "PDF") {
                            Button(
                                onClick = {
                                    viewModel.runExportSimulation {
                                        simulationSuccessful = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("simulate_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate PDF")
                            }
                        } else {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(exportCode))
                                    Toast.makeText(context, "HTML/CSS Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy HTML/CSS")
                            }
                        }
                    }
                }
            }
        }
    }
}

