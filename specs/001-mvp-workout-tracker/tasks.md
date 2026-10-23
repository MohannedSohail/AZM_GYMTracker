# Task Breakdown: AZM / عزم MVP

**Feature**: `specs/001-mvp-workout-tracker`  
**Document**: `specs/001-mvp-workout-tracker/tasks.md`  
**Status**: Ready for Human Approval (Gate E)  
**Governing Documents**: [`.specify/memory/constitution.md`](file:///e:/AI%20Projects/AZM_GYMTracker/.specify/memory/constitution.md), [`specs/001-mvp-workout-tracker/plan.md`](file:///e:/AI%20Projects/AZM_GYMTracker/specs/001-mvp-workout-tracker/plan.md)  
**Date**: 2026-10-24  

---

## Milestone 1: Toolchain & Dependency Integration

- [ ] **TASK-1.1**: Update version catalog [`gradle/libs.versions.toml`](file:///e:/AI%20Projects/AZM_GYMTracker/gradle/libs.versions.toml) with Room (`2.6.1`), KSP (`2.2.10-2.0.2`), Navigation Compose (`2.8.8`), Lifecycle ViewModel Compose (`2.8.7`), and Coroutines Test (`1.10.1`).
- [ ] **TASK-1.2**: Update root [`build.gradle.kts`](file:///e:/AI%20Projects/AZM_GYMTracker/build.gradle.kts) to register KSP plugin.
- [ ] **TASK-1.3**: Update [`app/build.gradle.kts`](file:///e:/AI%20Projects/AZM_GYMTracker/app/build.gradle.kts) to apply KSP plugin and add Room, Navigation, ViewModel, and Coroutines Test dependencies.
- [ ] **TASK-1.4**: Run `./gradlew help` to verify clean Gradle configuration without classpath or version conflicts.

---

## Milestone 2: Domain Layer & Room Persistence

- [ ] **TASK-2.1**: Implement pure domain models:
  - `Member.kt` (id, name, colorHex, createdAt).
  - `Exercise.kt` (id, nameEn, nameAr, category, isCustom, creatorMemberId) and `MuscleCategory` enum.
  - `WorkoutSession.kt`, `WorkoutExercise.kt`, `WorkoutSet.kt`.
- [ ] **TASK-2.2**: Implement pure domain validators:
  - `MemberNameValidator.kt` (1–50 trimmed chars, non-blank).
  - `RepValidator.kt` (1–999 positive integer).
  - `WeightValidator.kt` (0.0–999.5 kg or null for bodyweight).
  - `WeightFormatter.kt` (bilingual set formatting, bodyweight reps-only display, LTR decimal formatting).
- [ ] **TASK-2.3**: Write unit tests for domain validators in `app/src/test/java/com/example/azm_gymtracker/domain/`:
  - `MemberNameValidatorTest.kt`
  - `RepValidatorTest.kt`
  - `WeightValidatorTest.kt`
  - `WeightFormatterTest.kt`
- [ ] **TASK-2.4**: Create Room Entities:
  - `MemberEntity.kt` (unique index on name).
  - `ExerciseEntity.kt`.
  - `WorkoutSessionEntity.kt` (foreign key to member with `CASCADE`, index on `member_id, status`).
  - `WorkoutExerciseEntity.kt` (foreign keys to session with `CASCADE` and exercise with `RESTRICT`).
  - `WorkoutSetEntity.kt` (foreign key to workout exercise with `CASCADE`).
- [ ] **TASK-2.5**: Create Room DAOs:
  - `MemberDao.kt` (getAll, insert, update, delete).
  - `ExerciseDao.kt` (getAll, getByCategory, insert, search).
  - `WorkoutDao.kt` (getActiveSession, getHistory, insertSession, insertSet, deleteSetWithReindex, finishWorkout).
- [ ] **TASK-2.6**: Create `AZMDatabase.kt` with Room pre-population callback bundling standard gym exercises (Bench Press, Squat, Deadlift, Overhead Press, Pull-ups, Bicep Curls, etc. in English and Arabic).
- [ ] **TASK-2.7**: Write in-memory Room DAO tests in `app/src/test/java/com/example/azm_gymtracker/data/`:
  - `MemberDaoTest.kt` (CRUD, unique name check).
  - `WorkoutDaoTest.kt` (Concurrent `IN_PROGRESS` session queries, cascade deletion, set re-indexing).

---

## Milestone 3: Repository Pattern & ViewModels

- [ ] **TASK-3.1**: Create Domain Repository interfaces (`MemberRepository`, `ExerciseRepository`, `WorkoutRepository`).
- [ ] **TASK-3.2**: Implement Repository classes in `data/repository/` (`MemberRepositoryImpl`, `ExerciseRepositoryImpl`, `WorkoutRepositoryImpl`).
- [ ] **TASK-3.3**: Create Manual Dependency Injection Container (`AppContainer.kt`) inside application class.
- [ ] **TASK-3.4**: Implement `MemberViewModel.kt` with immutable `MemberUiState` (active member selection, creation, permanent cascading deletion).
- [ ] **TASK-3.5**: Implement `ActiveSessionViewModel.kt` with immutable `ActiveSessionUiState` (start workout, add exercise, add/update/delete set, previous set pre-filling, finish, discard).
- [ ] **TASK-3.6**: Implement `HistoryViewModel.kt` with immutable `HistoryUiState` (query completed workouts, calculate total volume, delete past workout).
- [ ] **TASK-3.7**: Write unit tests for ViewModels using `StandardTestDispatcher` and `kotlinx-coroutines-test`.

---

## Milestone 4: Jetpack Compose UI & Navigation Flow

- [ ] **TASK-4.1**: Create type-safe navigation graph (`NavRoutes.kt`, `AppNavHost.kt`).
- [ ] **TASK-4.2**: Build `OnboardingScreen.kt` for initial launch when 0 members exist.
- [ ] **TASK-4.3**: Build `DashboardScreen.kt`:
  - Active member top bar with quick switcher dialog.
  - "Resume In-Progress Workout" hero banner (when an active session exists).
  - "Start Workout" primary hero button.
  - Recent workout summary card.
- [ ] **TASK-4.4**: Build `ActiveSessionScreen.kt`:
  - Elapsed workout timer.
  - Exercise cards list.
  - Set rows (Set #, previous set values, reps field, weight field, checkmark complete button).
  - Finish and Discard dialogs.
- [ ] **TASK-4.5**: Build `ExercisePickerSheet.kt`:
  - Muscle group category filter chips (Chest, Back, Legs, Shoulders, Arms, Core).
  - Search bar.
  - Exercise list with localized names.
  - Custom exercise creation dialog.
- [ ] **TASK-4.6**: Build `HistoryScreen.kt` and `HistoryDetailScreen.kt`:
  - Chronological workout list.
  - Total volume (kg) and duration badges.
  - Exercise and set detail view with inline set editing and delete workout option.

---

## Milestone 5: Localization & Arabic RTL Polish

- [ ] **TASK-5.1**: Define all string resources in `res/values/strings.xml` (English).
- [ ] **TASK-5.2**: Define 100% matching Arabic translations in `res/values-ar/strings.xml`.
- [ ] **TASK-5.3**: Apply explicit LTR layout direction wrappers (`CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`) to all weight and repetition text displays.
- [ ] **TASK-5.4**: Verify touch targets meet or exceed 48x48 dp across all buttons and inputs.
- [ ] **TASK-5.5**: Write Compose UI tests verifying RTL layout direction and number formatting.

---

## Milestone 6: Verification & End-to-End Build

- [ ] **TASK-6.1**: Run complete automated test suite (`./gradlew test`).
- [ ] **TASK-6.2**: Execute debug APK build (`./gradlew assembleDebug`).
- [ ] **TASK-6.3**: Verify airplane mode offline operation.
- [ ] **TASK-6.4**: Verify process termination and reboot session recovery.
- [ ] **TASK-6.5**: Review file diffs and produce Final Delivery Report.
