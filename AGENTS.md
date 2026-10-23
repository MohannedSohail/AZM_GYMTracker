# AGENTS.md — AZM Gym Tracker (عزم)

## 1. Project Mission & Scope
**AZM / عزم** is an offline-first Android gym tracking application designed for multiple people sharing a single physical Android device. It enables members to independently record and review their gym workouts with exercises, sets, repetitions, and optional weights without requiring internet connectivity, user accounts, or external services.

### Core MVP Capabilities
- **Member Profiles**: Create, view, edit, and switch between member profiles on the same device.
- **Exercise Catalog**: Categorized library of exercises (by muscle group/type), supporting preloaded standard exercises and user-added custom exercises.
- **Active Workout Session**: Start a workout session for a selected member, select exercises, log multiple sets per exercise, record repetitions independently for each set, and record optional weights (in kilograms).
- **Workout History & Review**: Persistent, member-isolated workout logs with date/time summaries, exercise breakdowns, and editing/deletion complying with explicit integrity rules.
- **Local-First & Offline**: 100% functional without network permissions or remote servers.
- **Arabic-Friendly**: Full RTL layout support, localized Arabic typography and strings, and appropriate Android touch ergonomics.

---

## 2. Kotlin & Jetpack Compose Engineering Rules
- **UI Framework**: Modern Jetpack Compose with Material 3.
- **Language**: Kotlin with official style conventions and idiomatic coroutine usage.
- **Architecture**: Unidirectional Data Flow (UDF) with strict separation of concerns:
  - **UI / Presentation Layer**: Stateless composables driven by immutable `UiState` data classes.
  - **State Holder**: Android `ViewModel` exposing `StateFlow` collected with lifecycle awareness (`collectAsStateWithLifecycle`).
  - **Domain / Business Layer**: Pure Kotlin models, validation logic, and use cases.
  - **Data / Persistence Layer**: Room (SQLite) with Repository pattern and Kotlin Coroutines / Flow.
- **Input Validation**: All numeric inputs (reps, weight, profile names) must be strictly validated with friendly localized inline error messages. Handle empty, loading, success, and error UI states exhaustively.

---

## 3. Repository Safety Rules
- **Source of Truth**: The existing repository is the source of truth. Always inspect files before proposing or making changes.
- **Preserve Baseline**: Never overwrite or delete existing functional code without an approved architectural plan.
- **No Destructive Git**: Never execute destructive Git commands (`reset --hard`, `clean -fd`, force push).
- **Secrets & Privacy**: Never introduce, log, or commit passwords, credentials, API keys, or private user data.
- **Zero Network Permissions**: Do not introduce `android.permission.INTERNET`, external telemetry, analytics, or third-party cloud SDKs.
- **Dependency Discipline**: Do not add dependencies without prior technical justification and approval.

---

## 4. Testing & Build Expectations
- **Unit Testing**: Automated unit tests for all domain models, validation logic, formatters, and ViewModels (using JUnit 4/5, Kotlin Coroutines Test).
- **Persistence Testing**: In-memory Room database tests for DAOs verifying CRUD operations, foreign key cascades, and member isolation.
- **UI / RTL Testing**: Compose UI tests verifying screen rendering, input actions, and RTL layout integrity.
- **Build Verification**: Run Gradle tasks (`gradlew test`, `gradlew assembleDebug`) at every milestone.

---

## 5. Arabic RTL & Localization Requirements
- **RTL Support**: Maintain `android:supportsRtl="true"` in [`AndroidManifest.xml`](file:///e:/AI%20Projects/AZM_GYMTracker/app/src/main/AndroidManifest.xml).
- **String Resources**: All user-facing strings must reside in resource bundles (`res/values/strings.xml` and `res/values-ar/strings.xml`). Never hardcode text in composables.
- **Numeric & LTR Isolation**: Repetitions and weight numbers must use explicit LTR formatting within RTL layouts to prevent inverted decimals (e.g., `72.5 kg`) or swapped digits.
- **Ergonomics**: Buttons and numeric input fields must be optimized for one-handed gym usage with adequate touch targets (minimum 48dp).

---

## 6. Data Integrity & Persistence Rules
- **Database Engine**: Room with SQLite.
- **Strict Member Isolation**: Every workout session, exercise log, and set record must have a non-nullable foreign key referencing its owner `member_id`.
- **Relational Integrity**: Use foreign key constraints with explicit `ON DELETE CASCADE` or `RESTRICT` rules so deleting a member or workout does not leave orphaned records.
- **Atomicity**: Active session finalization and bulk set updates must execute inside database transactions (`@Transaction`).
- **Migrations**: Incremental schema migrations must be tested; never silently drop tables in production builds.

---

## 7. Spec Kit & SDLC Human Approval Gates
Every phase in the AI SDLC requires an explicit human checkpoint before proceeding:
- **Gate A**: Repository audit & baseline verification approved. *(Completed)*
- **Gate B**: `AGENTS.md` and `intent.md` approved. *(Current Gate)*
- **Gate C**: Feature specification (`spec.md`) approved.
- **Gate D**: Technical implementation plan (`plan.md`) approved.
- **Gate E**: Requirements checklist (`checklist.md`), task breakdown (`tasks.md`), and automated test design approved.
- **Gate F**: Each incremental implementation milestone reviewed with passing tests.
- **Gate G**: Device / emulator acceptance testing and final review approved.

---

## 8. Definition of Done
A task or milestone is complete only when:
1. All acceptance criteria in the approved specification are satisfied.
2. Architecture adheres to UDF and separation of concerns.
3. Unit and persistence tests pass cleanly.
4. The debug APK builds successfully (`./gradlew assembleDebug`).
5. Arabic RTL layout and numeric inputs render correctly.
6. Local persistence and member isolation survive process death and app restarts.
7. File diffs are reviewed and accepted by the user.
