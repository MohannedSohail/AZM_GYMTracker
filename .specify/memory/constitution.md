<!--
Sync Impact Analysis:
- Storage path: .specify/memory/constitution.md
- Integration: Antigravity (agy) / GitHub Spec Kit
- Version: 1.0.0
- Governs: All specs, plans, tasks, verification suites, and implementation milestones.
-->

# Engineering Constitution: AZM / عزم

**Project**: AZM / عزم — Offline-First Android Gym Tracker  
**Document**: `.specify/memory/constitution.md`  
**Status**: Active Architectural Contract  
**Established**: 2026-10-24  

---

## Foundational Principles

### 1. User Value First
Optimize the workout recording workflow for speed, clarity, and minimal distraction. A user standing in the gym between sets must be able to log exercises, reps, and weights in seconds without unnecessary dialogs, complex navigation, or intrusive UI elements.

### 2. Offline-First
Core workout recording, member management, exercise catalogs, and historical review must never require internet access or remote network connectivity. The application must function with 100% feature parity in airplane mode.

### 3. Data Integrity
Each workout session has one or more participants (members). Every logged set belongs strictly to exactly one member **and** to the correct exercise within that session. Deletions and updates must follow non-nullable foreign keys and cascading rules so that one member's sets never appear in another member's records, even when both trained in the same session. Deleting a member removes their participation and sets; sessions left with zero participants are deleted.

### 4. Privacy
Minimize stored personal information. The MVP strictly avoids user accounts, email/password registrations, cloud uploads, telemetry, external analytics, and background network calls. All personal fitness records remain solely on the physical device.

### 5. Native Android Quality
Follow official Android architecture guidelines using Kotlin and Jetpack Compose with Material 3. Preserve compatibility with the established project build environment (AGP 9.2.1, Gradle 9.4.1, Kotlin 2.2.10, SDK 36, Min SDK 26).

### 6. Maintainability
Maintain strict separation of concerns following Unidirectional Data Flow (UDF):
- **UI Layer**: Stateless composables consuming immutable `UiState`.
- **State Holder**: Android `ViewModel` exposing lifecycle-aware `StateFlow`.
- **Domain Layer**: Pure Kotlin business models and validation logic.
- **Data Layer**: Room (SQLite) repositories with Kotlin Flow.

### 7. Testability
All functional requirements must have clear, verifiable acceptance criteria. Business logic, input validation, and Room DAOs must be covered by automated unit and persistence tests.

### 8. Accessibility & Arabic RTL
Full accessibility and natural ergonomics must be maintained across all device orientations and locales:
- Native Arabic RTL layout mirroring with `android:supportsRtl="true"`.
- Isolated LTR numeric formatting for repetitions and weights to prevent inverted decimals (e.g., `85.5 kg` / `85.5 كغ`).
- Touch targets for core gym interactions must meet or exceed 48x48 dp.
- 100% localized string resources in `values/strings.xml` and `values-ar/strings.xml`.
- Official font Thmanyah (ثمانية) is bundled in the application with weights 400/500/700/900 in `res/font`, with license documentation preserved in the repository.

### 9. Incremental Delivery
Implement work through small, traceable, and reviewable tasks. Never proceed past SDLC approval gates (Gates A through G) without explicit human review and verification.

### 10. Security
Never commit secrets, tokens, or credentials. Destructive operations (such as dropping database tables or deleting user profiles) require explicit user confirmation and architectural approval.

### 11. Compatibility
Preserve the existing working project structure and build configurations unless a documented and approved technical decision justifies a modification.

### 12. Evidence-Based Completion
Report only builds, test executions, and verifications that were genuinely executed. Honest reporting of test results, file diffs, and unresolved issues is mandatory.
