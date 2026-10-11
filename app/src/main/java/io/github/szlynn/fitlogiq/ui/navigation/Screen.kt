package io.github.szlynn.fitlogiq.ui.navigation

/**
 * Defines all navigation routes in the app.
 * Routes with parameters use "/" separator, e.g. "log_run/42".
 */
sealed class Screen(val route: String) {
    // Running module
    data object RunLog : Screen("run_log")
    data object LogRun : Screen("log_run")
    data class LogRunEdit(val runId: Long) : Screen("log_run/$runId") {
        companion object {
            const val ROUTE = "log_run/{runId}"
            const val ARG = "runId"
        }
    }
    data object TrainingPlans : Screen("training_plans")
    data class TrainingPlanDetail(val planId: Long) : Screen("training_plan/$planId") {
        companion object {
            const val ROUTE = "training_plan/{planId}"
            const val ARG = "planId"
        }
    }
    data class TrainingPlanEditor(val planId: Long?) : Screen("training_plan_editor/${planId ?: "new"}") {
        companion object {
            const val ROUTE = "training_plan_editor/{planId}"
            const val ARG = "planId"
        }
    }
    data object Stats : Screen("stats")
}
