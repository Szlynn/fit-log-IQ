package io.github.szlynn.fitlogiq.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.szlynn.fitlogiq.ui.running.LogRunScreen
import io.github.szlynn.fitlogiq.ui.running.RunLogScreen
import io.github.szlynn.fitlogiq.ui.running.StatsScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlanDetailScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlanEditorScreen
import io.github.szlynn.fitlogiq.ui.running.TrainingPlansScreen

@Composable
fun FitLogIQNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RunLog.route,
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
