package com.example.azm_gymtracker.ui.navigation

/**
 * Type-safe navigation routes for AZM based on Figma Flows.
 */
sealed class NavRoutes(val route: String) {
    data object Splash : NavRoutes("splash")
    data object Onboarding : NavRoutes("onboarding")
    data object Dashboard : NavRoutes("dashboard")
    data object ParticipantSelection : NavRoutes("participant_selection")
    data object Exercises : NavRoutes("exercises")
    data object History : NavRoutes("history")
    data object Progress : NavRoutes("progress")
    data object WorkoutSummary : NavRoutes("workout_summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout_summary/$sessionId"
    }

    data object ActiveSession : NavRoutes("active_session") {
        fun createRoute(memberId: Long = 0L) = "active_session"
    }
}
