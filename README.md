<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Luxury Gantt Chart & Timeline Planner (Android App)

This is a premium, native Android application designed to help users plan, visualize, and manage complex project timelines using interactive Gantt charts. The app is crafted with a highly responsive, modern glassmorphic theme ("Obsidian & Neon Glow") and features local persistence along with an interactive HTML export engine.

---

## 🌟 Key Features

*   **Dynamic Gantt Chart Grid**: A visual, grid-based calendar view mapping task timelines, duration, progress, and color categories across project days. Supports toggling between **Daily** and **Weekly** zoom granularities.
*   **Project Management**: Create, switch, and delete multiple independent projects with detailed titles and descriptions.
*   **Comprehensive Task CRUD**: Create, edit, and delete tasks under each project. Configure:
    *   **Scheduling**: Start day offset and duration in days.
    *   **Phases**: Categorize tasks into phases (e.g., *Strategy*, *Design*, *Development*, *Launch*).
    *   **Progress**: Track task completion percentages (0% to 100%).
    *   **Visual Styling**: Choose custom color codes/hex for distinct task categories.
    *   **Dependencies**: Model dependencies between tasks.
*   **Premium Glassmorphic UI/UX**: Designed to look stunning out-of-the-box, with:
    *   Ambient neon background glows (Neon Violet, Neon Pink).
    *   Sleek glassmorphism effects (blur, custom border gradients).
    *   Smooth micro-animations and transitions.
*   **Interactive HTML & PDF Export Engine**: Generate standalone, fully-styled HTML code for your Gantt chart. Features:
    *   Responsive CSS grid timelines.
    *   Plus Jakarta Sans modern typography.
    *   Custom print styles optimizing the Gantt chart for physical PDF printing.
    *   An interactive in-app copy/preview system.
*   **Offline First**: Powered by Room Database for robust local persistence of all project configuration data.
*   **Automated Seed Data**: Auto-populates the database with template projects (such as "Interactive HTML Proposal" and "Mobile App Design System") upon the first run to showcase the application's capabilities.

---

## 🛠️ Technology Stack

*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose (Material 3)
*   **Database & Persistence**: Room ORM (SQLite) + Kotlin Symbol Processing (KSP)
*   **Dependency Management**: Gradle Version Catalogs (`libs.versions.toml`)
*   **AI Support**: Integrated with the Firebase AI SDK (`libs.firebase.ai`)
*   **Testing Suite**: JUnit 4, Robolectric, and Roborazzi (for Compose screenshot testing)

---

## 📂 Project Architecture & Structure

```
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example
│   │   │   │   ├── MainActivity.kt         # Main Compose UI, controls, dialogs, and export sheet
│   │   │   │   ├── data
│   │   │   │   │   ├── GanttDao.kt         # Database Access Object interface
│   │   │   │   │   ├── GanttDatabase.kt    # Room database setup & initialization
│   │   │   │   │   ├── GanttModels.kt      # GanttProject & GanttTask schema entities
│   │   │   │   │   └── GanttRepository.kt  # Repository layer mapping Room queries
│   │   │   │   └── ui
│   │   │   │       ├── GanttViewModel.kt   # App state holder & HTML generation logic
│   │   │   │       └── theme               # Premium luxury color schemes, shapes, and typography
│   │   │   └── res/                        # App resources (drawables, mipmaps, XML configurations)
│   │   └── test/java/com/example           # Unit and screenshot tests (Roborazzi)
│   └── build.gradle.kts                    # App build configuration and dependencies
├── build.gradle.kts                        # Root project build file
├── settings.gradle.kts                     # Gradle multi-project structures
└── .env.example                            # Template for API keys
```

---

## 🚀 Run Locally

### Prerequisites

*   [Android Studio](https://developer.android.com/studio) (latest stable version recommended)
*   JDK 11 or higher configured in Android Studio

### Getting Started

1.  **Clone the Repository**: Clone this project directory.
2.  **Open in Android Studio**:
    *   Open Android Studio.
    *   Select **Open** and choose the root directory of this project.
3.  **Sync & Configure**:
    *   Let Android Studio resolve dependencies and perform initial Gradle sync.
    *   Allow Android Studio to fix any target SDK or build compatibility prompts.
4.  **Configure API Keys**:
    *   Create a file named `.env` in the root directory.
    *   Set your API key using the following format (refer to `.env.example`):
        ```env
        GEMINI_API_KEY=your_gemini_api_key_here
        ```
5.  **Clean build configuration**:
    *   Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")` (if running custom debug configurations).
6.  **Run the App**:
    *   Select your connected device or emulator.
    *   Click the **Run** button (green play icon) to deploy.
