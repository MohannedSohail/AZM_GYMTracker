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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.azm_gymtracker.AppContainer
import com.example.azm_gymtracker.presentation.dashboard.DashboardViewModel
import com.example.azm_gymtracker.presentation.exercises.ExercisesViewModel
import com.example.azm_gymtracker.presentation.history.HistoryViewModel
import com.example.azm_gymtracker.presentation.member.MemberViewModel
import com.example.azm_gymtracker.presentation.progress.ProgressViewModel
import com.example.azm_gymtracker.presentation.session.ActiveSessionViewModel
import com.example.azm_gymtracker.presentation.summary.SummaryViewModel
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
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val memberViewModel: MemberViewModel = viewModel(
        factory = MemberViewModel.Factory(container.memberRepository)
    )
    val memberState by memberViewModel.uiState.collectAsStateWithLifecycle()

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(container.workoutRepository, container.memberRepository)
    )
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()

    val navigateToTab: (AzmNavTab) -> Unit = { tab ->
        val targetRoute = when (tab) {
            AzmNavTab.HOME -> NavRoutes.Dashboard.route
            AzmNavTab.EXERCISES -> NavRoutes.Exercises.route
            AzmNavTab.HISTORY -> NavRoutes.History.route
            AzmNavTab.PROGRESS -> NavRoutes.Progress.route
        }
        navController.navigate(targetRoute) {
            popUpTo(NavRoutes.Dashboard.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                    memberViewModel.createMember(name, colorHex) {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
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
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(container.memberRepository, container.workoutRepository)
            )
            val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

            DashboardScreen(
                activeMember = dashboardState.activeMember,
                members = dashboardState.allMembers,
                activeSession = dashboardState.activeSession?.session?.let { s ->
                    com.example.azm_gymtracker.domain.model.WorkoutSession(
                        id = s.id,
                        startedAt = s.startedAt,
                        endedAt = s.endedAt,
                        pausedTotalMs = s.pausedTotalMs,
                        status = when (s.status) {
                            "COMPLETED" -> com.example.azm_gymtracker.domain.model.SessionStatus.COMPLETED
                            "CANCELLED", "DISCARDED" -> com.example.azm_gymtracker.domain.model.SessionStatus.CANCELLED
                            else -> com.example.azm_gymtracker.domain.model.SessionStatus.IN_PROGRESS
                        },
                        notes = s.notes,
                        exercises = dashboardState.activeSession?.exercises?.map { ex ->
                            com.example.azm_gymtracker.domain.model.WorkoutExercise(
                                id = ex.workoutExercise.id,
                                sessionId = ex.workoutExercise.sessionId,
                                exerciseId = ex.workoutExercise.exerciseId,
                                exerciseName = ex.workoutExercise.exerciseNameSnapshot,
                                orderIndex = ex.workoutExercise.orderIndex,
                            )
                        } ?: emptyList(),
                    )
                },
                recentSessions = dashboardState.recentSessions,
                onStartWorkout = {
                    navController.navigate(NavRoutes.ParticipantSelection.route)
                },
                onResumeWorkout = {
                    navController.navigate(NavRoutes.ActiveSession.route)
                },
                onSelectRecentSession = { session ->
                    navController.navigate(NavRoutes.WorkoutSummary.createRoute(session.id))
                },
                onTabSelected = navigateToTab,
            )
        }

        // Participant Selection Screen ("من يتمرن اليوم؟")
        composable(
            route = NavRoutes.ParticipantSelection.route,
        ) {
            val coroutineScope = rememberCoroutineScope()

            ParticipantSelectionScreen(
                members = memberState.members,
                initialSelectedMemberIds = memberState.activeMember?.let { setOf(it.id) } ?: emptySet(),
                onStartWorkout = { selectedMemberIds ->
                    coroutineScope.launch {
                        val ids = if (selectedMemberIds.isNotEmpty()) {
                            selectedMemberIds
                        } else {
                            listOfNotNull(memberState.activeMember?.id)
                        }
                        if (ids.isNotEmpty()) {
                            container.workoutRepository.startSession(ids)
                            navController.navigate(NavRoutes.ActiveSession.route) {
                                popUpTo(NavRoutes.Dashboard.route)
                            }
                        }
                    }
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
        ) {
            val sessionViewModel: ActiveSessionViewModel = viewModel(
                factory = ActiveSessionViewModel.Factory(
                    workoutRepository = container.workoutRepository,
                    exerciseRepository = container.exerciseRepository,
                ),
            )
            val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(sessionState.isSessionFinished) {
                if (sessionState.isSessionFinished) {
                    val sessionId = sessionState.finishedSessionId ?: 0L
                    navController.navigate(NavRoutes.WorkoutSummary.createRoute(sessionId)) {
                        popUpTo(NavRoutes.Dashboard.route)
                    }
                }
            }

            ActiveSessionScreen(
                uiState = sessionState,
                onAddExercise = { exercise -> sessionViewModel.addExercise(exercise) },
                onAddExercises = { exercises -> sessionViewModel.addExercises(exercises) },
                onStartWorkout = { sessionViewModel.startWorkout() },
                onCreateCustomExercise = { name, category ->
                    sessionViewModel.createCustomExercise(name, category)
                },
                onSelectExercise = { sessionViewModel.selectExercise(it) },
                onRemoveExercise = { sessionViewModel.removeExercise(it) },
                onAddSet = { workoutExerciseId ->
                    sessionViewModel.addSet(workoutExerciseId, repsRaw = "8", weightRaw = "72.5")
                },
                onUpdateSet = { set, repsStr, weightStr ->
                    sessionViewModel.updateSet(set, repsStr, weightStr)
                },
                onDeleteSet = { setId, workoutExerciseId ->
                    sessionViewModel.deleteSet(setId, workoutExerciseId)
                },
                onDoneSet = { sessionViewModel.onDoneSet() },
                onAdjustWeight = { sessionViewModel.adjustWeight(it) },
                onAdjustReps = { sessionViewModel.adjustReps(it) },
                onSelectTurn = { sessionViewModel.selectTurn(it) },
                onSkipRestTimer = { sessionViewModel.skipRestTimer() },
                onAdjustRestTimer = { sessionViewModel.adjustRestTimer(it) },
                onPauseSession = { sessionViewModel.pauseSession() },
                onResumeSession = { sessionViewModel.resumeSession() },
                onFinishSession = { sessionViewModel.finishSession() },
                onDiscardSession = {
                    sessionViewModel.discardSession()
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Workout Summary Screen ("ملخص التمرين")
        composable(
            route = NavRoutes.WorkoutSummary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val summaryViewModel: SummaryViewModel = viewModel(
                factory = SummaryViewModel.Factory(container.workoutRepository, sessionId)
            )
            val summaryState by summaryViewModel.uiState.collectAsStateWithLifecycle()

            val participantSummaries = summaryState.sessionWithDetails?.participants?.map { member ->
                val breakdown = summaryState.participantBreakdowns.find { it.memberId == member.id }
                ParticipantSummaryItem(
                    memberId = member.id,
                    name = member.name,
                    colorHex = member.colorHex,
                    volumeKg = breakdown?.totalVolume ?: 0.0,
                    totalSets = breakdown?.setsCount ?: 0,
                )
            } ?: emptyList()

            WorkoutSummaryScreen(
                totalVolumeKg = summaryState.totalVolumeKg,
                totalSets = summaryState.totalSetsCount,
                totalExercises = summaryState.totalExercisesCount,
                durationSeconds = summaryState.durationMinutes * 60,
                participantSummaries = participantSummaries,
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
            val exercisesViewModel: ExercisesViewModel = viewModel(
                factory = ExercisesViewModel.Factory(container.exerciseRepository)
            )
            val exercisesState by exercisesViewModel.uiState.collectAsStateWithLifecycle()

            ExercisesScreen(
                exercises = exercisesState.exercises,
                onSelectExercise = { /* View exercise details */ },
                onCreateCustomExercise = { nameAr, nameEn, category ->
                    exercisesViewModel.createCustomExercise(
                        nameAr = nameAr,
                        nameEn = nameEn,
                        category = category,
                        creatorMemberId = memberState.activeMember?.id,
                    )
                },
                onTabSelected = navigateToTab,
            )
        }

        // History Screen ("السجل")
        composable(
            route = NavRoutes.History.route,
        ) {
            HistoryScreen(
                uiState = historyState,
                onSelectMemberFilter = { historyViewModel.filterByMember(it) },
                onToggleSessionExpanded = { historyViewModel.toggleSessionExpanded(it) },
                onSelectSession = { historyViewModel.selectSession(it) },
                onDeleteSession = { historyViewModel.deleteSession(it) },
                onNavigateBack = { navController.popBackStack() },
                onTabSelected = navigateToTab,
            )
        }

        // Progress Screen ("التقدم")
        composable(
            route = NavRoutes.Progress.route,
        ) {
            val progressViewModel: ProgressViewModel = viewModel(
                factory = ProgressViewModel.Factory(container.memberRepository, container.workoutRepository)
            )
            val progressState by progressViewModel.uiState.collectAsStateWithLifecycle()

            ProgressScreen(
                uiState = progressState,
                onSelectMember = { progressViewModel.selectMember(it) },
                onPeriodSelected = { progressViewModel.selectPeriod(it) },
                onTabSelected = navigateToTab,
            )
        }
    }
}
