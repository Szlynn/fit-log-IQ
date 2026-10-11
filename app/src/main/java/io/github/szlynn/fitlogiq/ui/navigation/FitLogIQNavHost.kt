package io.github.szlynn.fitlogiq.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.szlynn.fitlogiq.ui.running.LogRunScreen

@Composable
fun FitLogIQNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RunLog.route,
    ) {
        composable(Screen.RunLog.route) {
            // TODO: RunLogScreen(navController)
            Text("Run Log")
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
            // TODO: TrainingPlansScreen(navController)
            Text("Training Plans")
        }

        composable(
            route = Screen.TrainingPlanDetail.ROUTE,
            arguments = listOf(navArgument(Screen.TrainingPlanDetail.ARG) { type = NavType.LongType }),
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong(Screen.TrainingPlanDetail.ARG) ?: return@composable
            // TODO: TrainingPlanDetailScreen(navController, planId)
            Text("Training Plan #$planId")
        }

        composable(
            route = Screen.TrainingPlanEditor.ROUTE,
            arguments = listOf(navArgument(Screen.TrainingPlanEditor.ARG) { nullable = true }),
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(Screen.TrainingPlanEditor.ARG)
                ?.toLongOrNull()
            // TODO: TrainingPlanEditorScreen(navController, planId)
            Text("Plan Editor (planId=$planId)")
        }

        composable(Screen.Stats.route) {
            // TODO: StatsScreen()
            Text("Stats")
        }
    }
}
