package com.example.azm_gymtracker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.azm_gymtracker.AppContainer
import com.example.azm_gymtracker.presentation.history.HistoryViewModel
import com.example.azm_gymtracker.presentation.member.MemberViewModel
import com.example.azm_gymtracker.presentation.session.ActiveSessionViewModel
import com.example.azm_gymtracker.ui.component.AzmNavTab
import com.example.azm_gymtracker.ui.screen.ActiveSessionScreen
import com.example.azm_gymtracker.ui.screen.DashboardScreen
import com.example.azm_gymtracker.ui.screen.ExercisesScreen
import com.example.azm_gymtracker.ui.screen.HistoryScreen
import com.example.azm_gymtracker.ui.screen.OnboardingScreen
import com.example.azm_gymtracker.ui.screen.ParticipantSelectionScreen
import com.example.azm_gymtracker.ui.screen.ParticipantSummaryItem
import com.example.azm_gymtracker.ui.screen.ProgressScreen
import com.example.azm_gymtracker.ui.screen.SplashScreen
import com.example.azm_gymtracker.ui.screen.WorkoutSummaryScreen

@Composable
fun AppNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val memberViewModel: MemberViewModel = viewModel(
        factory = MemberViewModel.Factory(container.memberRepository)
    )
    val memberState by memberViewModel.uiState.collectAsState()

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(container.workoutRepository)
    )
    val historyState by historyViewModel.uiState.collectAsState()

    // Smooth navigation specs based on Figma motion guidelines (280ms)
    val animationDuration = 280

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(animationDuration))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing),
                targetOffset = { it / 3 },
            ) + fadeOut(animationSpec = tween(animationDuration))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing),
                initialOffset = { -it / 3 },
            ) + fadeIn(animationSpec = tween(animationDuration))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(animationDuration))
        },
    ) {
        // Splash Screen
        composable(
            route = NavRoutes.Splash.route,
            exitTransition = {
                fadeOut(animationSpec = tween(350)) + scaleOut(targetScale = 1.05f)
            },
        ) {
            SplashScreen(
                onSplashFinished = {
                    val destination = if (memberState.members.isEmpty() && !memberState.isLoading) {
                        NavRoutes.Onboarding.route
                    } else {
                        NavRoutes.Dashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        // Onboarding Screen
        composable(
            route = NavRoutes.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f) },
        ) {
            OnboardingScreen(
                nameError = memberState.nameError,
                onCreateMember = { name, colorHex ->
                    memberViewModel.createMember(name, colorHex)
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                    }
                },
                onClearNameError = { memberViewModel.clearNameError() },
            )
        }

        // Dashboard / Home Screen
        composable(
            route = NavRoutes.Dashboard.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
        ) {
            val activeMember = memberState.activeMember
            val activeSessionFlow = remember(activeMember?.id) {
                activeMember?.let { container.workoutRepository.getActiveSession(it.id) }
            }
            val activeSession by (activeSessionFlow?.collectAsState(initial = null)
                ?: remember { androidx.compose.runtime.mutableStateOf(null) })

            val recentSessionsFlow = remember(activeMember?.id) {
                activeMember?.let { container.workoutRepository.getCompletedSessions(it.id) }
            }
            val recentSessions by (recentSessionsFlow?.collectAsState(initial = emptyList())
                ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) })

            DashboardScreen(
                activeMember = activeMember,
                members = memberState.members,
                activeSession = activeSession,
                recentSessions = recentSessions.take(5),
                onStartWorkout = {
                    navController.navigate(NavRoutes.ParticipantSelection.route)
                },
                onResumeWorkout = {
                    activeMember?.let { member ->
                        navController.navigate(NavRoutes.ActiveSession.createRoute(member.id))
                    }
                },
                onTabSelected = { tab ->
                    when (tab) {
                        AzmNavTab.HOME -> { /* Already here */ }
                        AzmNavTab.EXERCISES -> navController.navigate(NavRoutes.Exercises.route)
                        AzmNavTab.HISTORY -> navController.navigate(NavRoutes.History.route)
                        AzmNavTab.PROGRESS -> navController.navigate(NavRoutes.Progress.route)
                    }
                },
            )
        }

        // Participant Selection Screen ("من يتمرن اليوم؟")
        composable(
            route = NavRoutes.ParticipantSelection.route,
        ) {
            ParticipantSelectionScreen(
                members = memberState.members,
                initialSelectedMemberIds = memberState.activeMember?.let { setOf(it.id) } ?: emptySet(),
                onStartWorkout = { selectedMemberIds ->
                    val primaryId = selectedMemberIds.firstOrNull() ?: memberState.activeMember?.id ?: 1L
                    navController.navigate(NavRoutes.ActiveSession.createRoute(primaryId))
                },
                onCreateMember = { name, colorHex ->
                    memberViewModel.createMember(name, colorHex)
                },
                onDeleteMember = { member ->
                    memberViewModel.deleteMember(member)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Active Session Screen ("التمرين النشط")
        composable(
            route = NavRoutes.ActiveSession.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getLong("memberId") ?: return@composable

            val sessionViewModel: ActiveSessionViewModel = viewModel(
                factory = ActiveSessionViewModel.Factory(
                    workoutRepository = container.workoutRepository,
                    exerciseRepository = container.exerciseRepository,
                    memberId = memberId,
                ),
            )
            val sessionState by sessionViewModel.uiState.collectAsState()

            LaunchedEffect(sessionState.isSessionFinished) {
                if (sessionState.isSessionFinished) {
                    navController.navigate(NavRoutes.WorkoutSummary.route) {
                        popUpTo(NavRoutes.Dashboard.route)
                    }
                }
            }

            ActiveSessionScreen(
                uiState = sessionState,
                onAddExercise = { exercise -> sessionViewModel.addExercise(exercise) },
                onCreateCustomExercise = { name, category ->
                    sessionViewModel.createCustomExercise(name, category)
                },
                onAddSet = { workoutExerciseId ->
                    sessionViewModel.addSet(workoutExerciseId, repsRaw = "8", weightRaw = "72.5")
                },
                onUpdateSet = { set, repsStr, weightStr ->
                    sessionViewModel.updateSet(set, repsStr, weightStr)
                },
                onDeleteSet = { setId, workoutExerciseId ->
                    sessionViewModel.deleteSet(setId, workoutExerciseId)
                },
                onFinishSession = { sessionViewModel.finishSession() },
                onDiscardSession = {
                    sessionViewModel.discardSession()
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Workout Summary Screen ("ملخص التمرين")
        composable(
            route = NavRoutes.WorkoutSummary.route,
        ) {
            val sampleSummaries = listOf(
                ParticipantSummaryItem(1L, "أحمد", "#3FE779", 3240.0, 6),
                ParticipantSummaryItem(2L, "محمد", "#FFD177", 2880.0, 6),
                ParticipantSummaryItem(3L, "سلمان", "#8E9CFF", 2520.0, 6),
            )

            WorkoutSummaryScreen(
                totalVolumeKg = 8640.0,
                totalSets = 18,
                totalExercises = 3,
                durationSeconds = 2538, // 42:18
                participantSummaries = sampleSummaries,
                newPersonalRecordText = "رقم قياسي جديد • أحمد • بنش 72.5 كجم",
                onReturnHome = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                },
                onViewDetails = {
                    navController.navigate(NavRoutes.History.route)
                },
            )
        }

        // Exercises Screen ("التمارين")
        composable(
            route = NavRoutes.Exercises.route,
        ) {
            val exercisesFlow = remember { container.exerciseRepository.getAllExercises() }
            val exercises by exercisesFlow.collectAsState(initial = emptyList())

            ExercisesScreen(
                exercises = exercises,
                onSelectExercise = { /* View exercise details */ },
                onCreateCustomExercise = { /* Add custom exercise */ },
                onTabSelected = { tab ->
                    when (tab) {
                        AzmNavTab.HOME -> navController.navigate(NavRoutes.Dashboard.route)
                        AzmNavTab.EXERCISES -> { /* Already here */ }
                        AzmNavTab.HISTORY -> navController.navigate(NavRoutes.History.route)
                        AzmNavTab.PROGRESS -> navController.navigate(NavRoutes.Progress.route)
                    }
                },
            )
        }

        // History Screen ("السجل")
        composable(
            route = NavRoutes.History.route,
        ) {
            LaunchedEffect(Unit) {
                memberState.activeMember?.let { historyViewModel.loadHistory(it.id) }
            }

            HistoryScreen(
                uiState = historyState,
                onSelectSession = { historyViewModel.selectSession(it) },
                onDeleteSession = { historyViewModel.deleteSession(it) },
                onNavigateBack = { navController.popBackStack() },
                onTabSelected = { tab ->
                    when (tab) {
                        AzmNavTab.HOME -> navController.navigate(NavRoutes.Dashboard.route)
                        AzmNavTab.EXERCISES -> navController.navigate(NavRoutes.Exercises.route)
                        AzmNavTab.HISTORY -> { /* Already here */ }
                        AzmNavTab.PROGRESS -> navController.navigate(NavRoutes.Progress.route)
                    }
                },
            )
        }

        // Progress Screen ("التقدم")
        composable(
            route = NavRoutes.Progress.route,
        ) {
            ProgressScreen(
                members = memberState.members,
                activeMember = memberState.activeMember,
                onSelectMember = { memberViewModel.selectMember(it) },
                onTabSelected = { tab ->
                    when (tab) {
                        AzmNavTab.HOME -> navController.navigate(NavRoutes.Dashboard.route)
                        AzmNavTab.EXERCISES -> navController.navigate(NavRoutes.Exercises.route)
                        AzmNavTab.HISTORY -> navController.navigate(NavRoutes.History.route)
                        AzmNavTab.PROGRESS -> { /* Already here */ }
                    }
                },
            )
        }
    }
}
