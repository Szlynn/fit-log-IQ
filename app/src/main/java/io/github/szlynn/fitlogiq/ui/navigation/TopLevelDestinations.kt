package io.github.szlynn.fitlogiq.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestination(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        screen = Screen.RunLog,
        icon = Icons.Default.List,
        label = "Run Log",
    ),
    TopLevelDestination(
        screen = Screen.TrainingPlans,
        icon = Icons.Default.DateRange,
        label = "Plans",
    ),
    TopLevelDestination(
        screen = Screen.Stats,
        icon = Icons.Default.Star,
        label = "Stats",
    ),
)
