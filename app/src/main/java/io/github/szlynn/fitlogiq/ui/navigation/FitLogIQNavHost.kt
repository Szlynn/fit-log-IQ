package io.github.szlynn.fitlogiq.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.szlynn.fitlogiq.ui.running.LogRunScreen
import io.github.szlynn.fitlogiq.ui.running.RunLogScreen
import io.github.szlynn.fitlogiq.ui.running.StatsScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlanDetailScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlanEditorScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlansScreen

/** Top-level routes where the bottom nav bar is visible. */
private val topLevelRoutes = topLevelDestinations.map { it.screen.route }.toSet()

@Composable
fun FitLogIQNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == destination.screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.screen.route) {
                                    // Pop up to the start destination to avoid building up a large back stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.RunLog.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.RunLog.route) {
                RunLogScreen(navController)
            }

            composable(Screen.LogRun.route) {
                LogRunScreen(navController)
            }

            composable(
                route = Screen.LogRunEdit.ROUTE,
                arguments = listOf(navArgument(Screen.LogRunEdit.ARG) { type = NavType.LongType }),
            ) {
                LogRunScreen(navController)
            }

            composable(Screen.TrainingPlans.route) {
                TrainingPlansScreen(navController)
            }

            composable(
                route = Screen.TrainingPlanDetail.ROUTE,
                arguments = listOf(navArgument(Screen.TrainingPlanDetail.ARG) { type = NavType.LongType }),
            ) {
                TrainingPlanDetailScreen(navController)
            }

            composable(
                route = Screen.TrainingPlanEditor.ROUTE,
                arguments = listOf(navArgument(Screen.TrainingPlanEditor.ARG) { nullable = true }),
            ) {
                TrainingPlanEditorScreen(navController)
            }

            composable(Screen.Stats.route) {
                StatsScreen()
            }
        }
    }
}
