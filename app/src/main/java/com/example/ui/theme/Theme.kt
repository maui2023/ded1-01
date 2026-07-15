package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SleekColorScheme =
  lightColorScheme(
    primary = NeonViolet,
    secondary = NeonIndigo,
    tertiary = NeonPink,
    background = ObsidianBg,
    surface = ObsidianSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Set to false for our Light "Sleek Interface" look
  dynamicColor: Boolean = false, // Disable dynamic colors to keep design exact
  content: @Composable () -> Unit,
) {
  val colorScheme = SleekColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
