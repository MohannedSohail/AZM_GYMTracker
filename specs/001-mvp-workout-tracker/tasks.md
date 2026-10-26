# Task Breakdown: AZM / عزم MVP

**Feature**: `specs/001-mvp-workout-tracker`  
**Document**: `specs/001-mvp-workout-tracker/tasks.md`  
**Status**: Ready for Human Approval (Gate E)  
**Governing Documents**: [`.specify/memory/constitution.md`](file:///e:/AI%20Projects/AZM_GYMTracker/.specify/memory/constitution.md), [`specs/001-mvp-workout-tracker/plan.md`](file:///e:/AI%20Projects/AZM_GYMTracker/specs/001-mvp-workout-tracker/plan.md)  
**Date**: 2026-10-24  

---

## Milestone 0: Amendments & Baseline Alignment (Current Phase)

- [x] **TASK-0.1**: Update `constitution.md`, `spec.md`, and `plan.md` with multi-participant shared session architecture, Schema v2, and locked decisions (D1–D5).
- [x] **TASK-0.2**: Inspect Figma (Node `87:125`), verify green emerald/neon color palette (`#06110D`, `#3FE779`, `#8CFF68`), verify steppers are strictly `±2.5` for weight and `±1` for reps, and document screen-to-composable gap matrix.
- [ ] **TASK-0.3**: Add Thmanyah (ثمانية) font family (`400/500/700/900`) to `res/font/` with license documentation, and wire into `Type.kt`.

---

## Milestone 1: Data Layer v2 (Room Persistence & Migrations)

- [x] **TASK-1.1**: Create `SessionParticipantEntity` and `AppSettingEntity`; update `WorkoutSessionEntity` (remove `member_id`, add `paused_total_ms`, `notes`, `CANCELLED` status) and `WorkoutSetEntity` (add non-nullable `member_id`, `completed_at`, `is_completed`). Update `MemberEntity` with `collate = ColumnInfo.NOCASE` on `name`.
- [x] **TASK-1.2**: Implement `Migration(1, 2)` inside an explicit SQLite transaction (creating `session_participants` from existing sessions, rebuilding `workout_sets` and `workout_sessions`, creating `app_settings`) and test migration with realistic v1 data without destructive fallback.
- [x] **TASK-1.3**: Fix exercise pre-population using `RoomDatabase.Callback.onCreate(db)` with direct SQLite `INSERT` statements; add unit test proving catalog exercises exist on initial DB creation.
- [x] **TASK-1.4**: Update DAOs with real `UPDATE` statements for finish/cancel (`ended_at`), real `DELETE` for sessions, atomic `@Transaction` calculations for `nextOrderIndex` and `nextSetNumber`, `@Relation` models (`SessionWithExercisesAndSets` with participants), and aggregate queries (total volume, per-participant volume, PRs, weekly session counts, weight progression timelines).
- [x] **TASK-1.5**: Implement executable DAO tests covering participant isolation, foreign key cascades, orphan session deletion when all participants are removed, set re-indexing, and DB reopening across process death.

---

## Milestone 2: Repositories & ViewModels

- [x] **TASK-2.1**: Update `WorkoutRepository` contract and implementation to handle multi-participant session lifecycle (`startSession(participantIds)`, `addSet(memberId, ...)`, reactive full-session flows, and member-filtered history/progress statistics).
- [x] **TASK-2.2**: Rebuild `ActiveSessionViewModel` with comprehensive immutable state: participant list, turn indicator, current exercise, set queue, stepper values initialized from active participant's last set, debounce, elapsed timer calculated from `started_at - paused_total_ms`, and "Done" action (save set + trigger rest timer + advance turn).
- [x] **TASK-2.3**: Implement / overhaul `DashboardViewModel`, `SummaryViewModel`, `ProgressViewModel`, `ExercisesViewModel`, and `HistoryViewModel` (participant filter, expandable cards, confirmable deletion with undo snackbar).
- [x] **TASK-2.4**: Persist active member context in `app_settings` Room table (zero extra dependencies).
- [x] **TASK-2.5**: Replace all `collectAsState` with `collectAsStateWithLifecycle` across all screens.
- [x] **TASK-2.6**: Write automated unit tests for all ViewModels using `StandardTestDispatcher` and `kotlinx-coroutines-test`.

---

## Milestone 3: Navigation Flow & State Recovery

- [x] **TASK-3.1**: Fix splash routing: `Splash` -> (0 members ? `Onboarding` : `Dashboard`) using `rememberUpdatedState` to avoid capturing stale navigation callbacks.
- [x] **TASK-3.2**: Enforce validation in `OnboardingScreen` before navigation, showing localized inline errors for blank, too long, or duplicate names.
- [x] **TASK-3.3**: Update `ParticipantSelectionScreen` to pass all selected participant IDs into `startSession` instead of only the first ID.
- [x] **TASK-3.4**: Connect finish workout action to navigate to `WorkoutSummaryScreen` with live aggregate data.
- [x] **TASK-3.5**: Add resumption support: opening the app during an `IN_PROGRESS` session shows the resume banner on Dashboard with elapsed duration and exercise count.
- [x] **TASK-3.6**: Implement discard/cancel confirmation dialogs for empty or active sessions and back-press protection during active workouts.

---

## Milestone 4: Elimination of Mock Data & Live Functionality

- [x] **TASK-4.1**: Remove all hardcoded dummy data (`RecentSessionCardMock`, fixed names "أحمد/محمد/سلمان", fixed volume `8,640`, fixed set values `72.5×8`) across Dashboard, ActiveSession, Summary, History, Progress, Exercises, and ParticipantSelection.
- [x] **TASK-4.2**: Overhaul `ExercisePickerSheet` to use Azm design theme and Arabic categories; hide "فيديو" badge in MVP and keep only "مخصص" (Custom).
- [x] **TASK-4.3**: Implement participant profile editing and permanent deletion with strong warning dialog explaining history cascade.
- [x] **TASK-4.4**: Verify that no hardcoded strings or numeric literals remain outside test fixtures and preview providers.

---

## Milestone 5: Figma Pixel-Perfect Polish & Arabic RTL

- [x] **TASK-5.1**: Apply Figma layout specifications screen-by-screen: 16dp corner radiuses, emerald/neon green gradients, 48dp touch targets, visually distinct disabled button states (opacity 0.4).
- [x] **TASK-5.2**: Refactor all Compose layouts to use logical alignment (`Alignment.Start`, `Alignment.End`, `Arrangement.Start`, `Arrangement.End`) and `Icons.AutoMirrored` icons, removing artificial manual inversion.
- [x] **TASK-5.3**: Extract all user-facing strings into `values/strings.xml` and `values-ar/strings.xml` with 100% parity.
- [x] **TASK-5.4**: Implement all UI state variations across screens: Loading (shimmer/spinner), Empty (localized graphics and text), Error (inline / snackbar), and Success.

---

## Milestone 6: Quality, Security & Production Verification

- [x] **TASK-6.1**: Set `android:allowBackup="false"` in `AndroidManifest.xml` and update backup rules for offline privacy.
- [x] **TASK-6.2**: Reconcile lifecycle dependencies (`2.8.7` vs `2.11.0`) and document `compileSdk` alignment.
- [x] **TASK-6.3**: Write Compose UI tests for RTL mirroring, isolated LTR numeric formatting (`72.5 كغ`), and 48dp touch targets.
- [x] **TASK-6.4**: Run `./gradlew test` and `./gradlew assembleDebug` and verify 100% pass rate.
- [x] **TASK-6.5**: Perform physical/emulator verification (Airplane mode, process death during session, reboot, Arabic RTL, Light/Dark mode) and submit Final Delivery Report.
