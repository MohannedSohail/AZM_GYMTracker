# Requirements Quality Checklist: AZM / عزم MVP

**Feature**: `specs/001-mvp-workout-tracker`  
**Document**: `specs/001-mvp-workout-tracker/checklist.md`  
**Status**: Ready for Human Approval (Gate E)  
**Governing Documents**: [`.specify/memory/constitution.md`](file:///e:/AI%20Projects/AZM_GYMTracker/.specify/memory/constitution.md), [`specs/001-mvp-workout-tracker/spec.md`](file:///e:/AI%20Projects/AZM_GYMTracker/specs/001-mvp-workout-tracker/spec.md), [`specs/001-mvp-workout-tracker/plan.md`](file:///e:/AI%20Projects/AZM_GYMTracker/specs/001-mvp-workout-tracker/plan.md)  
**Date**: 2026-10-24  

---

## 1. Specification & Requirements Completeness

- [x] **Clear User Personas**: Ahmed & Omar (sharing one device), Layla (solo powerlifter in basement gym).
- [x] **Stable Requirement IDs**: All requirements uniquely identified (`REQ-MEM-001..007`, `REQ-CAT-001..005`, `REQ-SES-001..007`, `REQ-SET-001..007`, `REQ-HIS-001..005`, `REQ-RTL-001..005`).
- [x] **Explicit Priority Levels**: Core logging and member isolation tagged P1; RTL typography polish tagged P2.
- [x] **Zero Silent Assumptions**: All 4 high-impact ambiguities resolved during clarification (concurrency, resume banner, bodyweight, cascade delete).
- [x] **Explicit Non-Goals**: Cloud sync, user accounts, AI coaches, subscriptions, and Bluetooth devices strictly excluded.

---

## 2. Multi-Member & Device Sharing Checklist

- [ ] **CH-MEM-1**: First launch without members forces initial member creation before dashboard access.
- [ ] **CH-MEM-2**: Quick member switcher is visible in the top bar across all primary screens.
- [ ] **CH-MEM-3**: Switching active member immediately filters all history and active workout views to the selected member.
- [ ] **CH-MEM-4**: Member A and Member B can each maintain an independent `IN_PROGRESS` session simultaneously without data collision.
- [ ] **CH-MEM-5**: Deleting a member requires confirmation and cascades to permanently delete all their sessions and sets.

---

## 3. Active Workout Logging & Set Ergonomics

- [ ] **CH-SET-1**: Sequential set numbering starting at 1 under each exercise card.
- [ ] **CH-SET-2**: Repetitions input rejects blank, zero, and negative values (valid range: 1–999).
- [ ] **CH-SET-3**: Weight input accepts decimals (e.g. `82.5`) and allows blank/0 for bodyweight exercises.
- [ ] **CH-SET-4**: Bodyweight sets render repetitions only (e.g., `"12 reps"` / `"12 تكرار"`), omitting `0.0 kg`.
- [ ] **CH-SET-5**: Adding a new set pre-populates reps and weight from the preceding set of that exercise.
- [ ] **CH-SET-6**: Deleting a set automatically decrements subsequent set numbers in that exercise.
- [ ] **CH-SET-7**: Finishing a session sets `ended_at`, changes status to `COMPLETED`, and saves to history.
- [ ] **CH-SET-8**: Discarding an active session permanently removes all uncompleted sets and does not appear in history.

---

## 4. Resilience, Persistence & Process Death

- [ ] **CH-RES-1**: All active sets and session updates are committed immediately to SQLite.
- [ ] **CH-RES-2**: Terminating the app during an active session and relaunching opens the Dashboard with a prominent "Resume In-Progress Workout" hero banner.
- [ ] **CH-RES-3**: Double-tapping "Finish Workout" or "Add Set" is debounced to prevent duplicate records.
- [ ] **CH-RES-4**: Tapping "Finish Workout" on a session with 0 sets prompts a discard confirmation.

---

## 5. Arabic RTL & Usability Checklist

- [ ] **CH-RTL-1**: `android:supportsRtl="true"` is declared in [`AndroidManifest.xml`](file:///e:/AI%20Projects/AZM_GYMTracker/app/src/main/AndroidManifest.xml).
- [ ] **CH-RTL-2**: 100% of user-facing strings defined in `res/values/strings.xml` and `res/values-ar/strings.xml`. Zero hardcoded strings.
- [ ] **CH-RTL-3**: Decimal weights in Arabic RTL render correctly as `82.5 كغ` with LTR isolation, never inverted as `.582 كغ`.
- [ ] **CH-RTL-4**: All interactive touch targets (Add Set, Complete, Delete) meet or exceed 48x48 dp.
- [ ] **CH-RTL-5**: Numeric inputs trigger specialized number keyboards with decimal support.

---

## 6. Testing & CI Verification Checklist

- [ ] **CH-TST-1**: Pure Kotlin unit tests for `RepValidator`, `WeightValidator`, `MemberNameValidator`, and `WeightFormatter`.
- [ ] **CH-TST-2**: In-memory Room database tests for `MemberDao`, `WorkoutDao`, cascading deletes, and concurrent active sessions.
- [ ] **CH-TST-3**: Coroutines test dispatcher unit tests for `ActiveSessionViewModel` and `MemberViewModel`.
- [ ] **CH-TST-4**: Clean execution of `./gradlew test`.
- [ ] **CH-TST-5**: Clean execution of `./gradlew assembleDebug`.
