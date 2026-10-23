# Feature Specification: AZM / عزم MVP Gym Tracker

**Feature**: `specs/001-mvp-workout-tracker`  
**Status**: Clarified & Approved Specification  
**Governing Documents**: [`.specify/memory/constitution.md`](file:///e:/AI%20Projects/AZM_GYMTracker/.specify/memory/constitution.md), [`docs/product/intent.md`](file:///e:/AI%20Projects/AZM_GYMTracker/docs/product/intent.md)  
**Clarification Date**: 2026-10-24  

---

## 1. Overview & Business Goals

**AZM / عزم** is an offline-first Android application that enables multiple people sharing a single physical mobile device to independently record and review their gym workouts. The MVP provides fast, distraction-free logging of exercises, sets, repetitions, and optional weights in kilograms with full Arabic RTL ergonomics.

---

## 2. User Stories & Priority Matrix

| ID | Priority | User Story | Value Rationale |
| :--- | :---: | :--- | :--- |
| **US-1** | **P1** | As a gym trainee sharing a phone, I want to create and switch between member profiles so that my workout history remains isolated from others. | Prevents data contamination among workout partners. |
| **US-2** | **P1** | As a trainee, I want to select exercises from a categorized catalog or add custom ones so that I can track any movement I perform. | Ensures the app fits various workout splits and exercises. |
| **US-3** | **P1** | As an active trainee, I want to start a session and quickly log sequential sets with repetitions and optional weight so that logging takes seconds between sets. | Minimizes friction during rest periods. |
| **US-4** | **P1** | As a trainee, I want my active session to persist across app backgrounding, app restart, or crash so that I never lose in-progress logs. | Protects user effort against OS process death. |
| **US-5** | **P1** | As a trainee, I want to view my past completed workouts in chronological order so that I can review my progress over time. | Provides historical feedback and progression insights. |
| **US-6** | **P2** | As an Arabic-speaking trainee, I want a natural Arabic RTL interface with correctly oriented numbers and units so that decimal weights are easy to read. | Delivers culturally native ergonomics and eliminates flipped decimals. |
| **US-7** | **P2** | As a trainee, I want to edit or delete logged sets and past sessions to correct accidental logging mistakes. | Preserves data accuracy over time. |

---

## 3. Clarified Functional Requirements

### 3.1 Member Profiles (`REQ-MEM`)
- `REQ-MEM-001`: The system shall support creating multiple member profiles on the same device.
- `REQ-MEM-002`: A member profile shall have a required display name (1–50 characters, trimmed of leading/trailing whitespace) and a visual color/avatar identifier.
- `REQ-MEM-003`: Duplicate active member names (case-insensitive) shall be rejected with an inline validation error.
- `REQ-MEM-004`: The system shall maintain one active member context at any given time.
- `REQ-MEM-005`: Switching the active member shall instantly update the active dashboard, session state, and history views to only display data belonging to that member.
- `REQ-MEM-006`: When the application is launched for the first time with zero members, the system shall show an onboarding screen requiring the creation of the initial member profile before navigating to the main dashboard.
- `REQ-MEM-007`: **Clarified Deletion Behavior**: Deleting a member profile requires explicit confirmation via a destructive alert dialog. Upon confirmation, the system executes an immediate permanent hard delete with cascading removal of all workout sessions, workout exercises, and sets owned by that member. If other members remain, the active context automatically switches to another profile; if zero members remain, the onboarding flow is triggered.

### 3.2 Exercise Catalog (`REQ-CAT`)
- `REQ-CAT-001`: The system shall provide a categorized catalog of exercises grouped by muscle target:
  - Chest (الصدر)
  - Back (الظهر)
  - Legs (الأرجل)
  - Shoulders (الأكتاف)
  - Arms (الذراعين)
  - Core (البطن والوسط)
- `REQ-CAT-002`: The catalog shall ship with preloaded standard gym exercises featuring bilingual names (English & Arabic).
- `REQ-CAT-003`: Preloaded standard exercises shall be protected from deletion or accidental modification.
- `REQ-CAT-004`: The system shall allow users to add custom exercises by supplying a required name (1–50 characters) and selecting a target muscle group.
- `REQ-CAT-005`: Custom exercises shall be searchable and filterable alongside standard exercises.

### 3.3 Active Workout Session & Concurrent Multi-Member Lifecycle (`REQ-SES`)
- `REQ-SES-001`: A workout session shall be explicitly started by tapping "Start Workout" under the active member profile.
- `REQ-SES-002`: The session shall record a `started_at` timestamp and maintain an active lifecycle status (`IN_PROGRESS`).
- `REQ-SES-003`: **Clarified Multi-Member Concurrency**: The system shall permit concurrent in-progress sessions across different members (at most one `IN_PROGRESS` session per member). If Member A has an active session and the user switches to Member B, Member A's session remains saved in `IN_PROGRESS` state. Member B can start or resume their own independent workout session without interfering with Member A.
- `REQ-SES-004`: **Clarified Restart & Interruption Landing**: When the application launches after being closed, terminated by the OS, or rebooted while a workout is `IN_PROGRESS` for the active member, the app shall land on the **Dashboard** displaying a prominent hero card: *"Resume In-Progress Workout"* with elapsed duration, current exercise count, and a direct "Resume Workout" button.
- `REQ-SES-005`: Exercises added to a session shall be rendered in the order added, each appearing as a distinct card with an option to remove the exercise from the active session.
- `REQ-SES-006`: Users shall be able to finalize a session ("Finish Workout"), which sets the `ended_at` timestamp, changes the status to `COMPLETED`, and saves all logged sets.
- `REQ-SES-007`: Users shall be able to cancel/discard an in-progress session with confirmation; discarding shall permanently remove all uncompleted sets and prevent the session from appearing in history.

### 3.4 Set Logging, Bodyweight Sets & Validation (`REQ-SET`)
- `REQ-SET-001`: Under each exercise card in an active session, users shall be able to add sets sequentially (Set 1, Set 2, Set 3...).
- `REQ-SET-002`: Each set shall record:
  - `set_number`: Sequential integer (1, 2, 3...).
  - `repetitions`: Required positive integer between `1` and `999`.
  - `weight_kg`: Optional positive decimal/integer between `0.0` and `999.5` kg (rounded to nearest 0.25/0.5 kg).
  - `is_completed`: Boolean flag indicating whether the set was finished.
- `REQ-SET-003`: **Clarified Bodyweight Handling**: If `weight_kg` is left blank or entered as `0` kg, the set is treated as a bodyweight set. In set rows, historical summaries, and volume calculations, it shall display repetitions only (e.g. `"12 reps"` / `"12 تكرار"`), omitting `0.0 kg` to avoid clutter.
- `REQ-SET-004`: When tapping "Add Set", the input fields shall pre-fill values from the immediately preceding set of that exercise to enable rapid one-tap logging.
- `REQ-SET-005`: Non-numeric characters or negative values in repetitions and weight inputs shall be rejected with immediate visual feedback.
- `REQ-SET-006`: Users shall be able to delete an individual set from an exercise card. Upon deletion, subsequent set numbers shall automatically re-index (e.g., deleting Set 2 changes Set 3 into Set 2).
- `REQ-SET-007`: Users shall be able to edit repetitions or weight of an existing logged set before finalizing the workout.

### 3.5 Workout History & Review (`REQ-HIS`)
- `REQ-HIS-001`: The system shall present a chronological timeline of completed workouts filtered strictly by the currently active member.
- `REQ-HIS-002`: Each historical session item shall display the workout date, start/end time, duration in minutes, total volume (calculated as the sum of `reps * weight_kg` across completed weighted sets), and exercise badges.
- `REQ-HIS-003`: Tapping a session shall open a detailed review screen showing every exercise and set logged.
- `REQ-HIS-004`: Users shall be able to delete a past session with explicit confirmation.
- `REQ-HIS-005`: Users shall be able to edit set repetitions or weights in past sessions to rectify historical recording errors.

### 3.6 Arabic RTL & Accessibility (`REQ-RTL`)
- `REQ-RTL-001`: The application shall support full bidirectional mirroring when the device or app language is set to Arabic (`ar`).
- `REQ-RTL-002`: All user-facing strings must be localized in both English and Arabic.
- `REQ-RTL-003`: Numeric repetitions and weight displays must be isolated with LTR text direction so that numbers and decimal separators do not invert in RTL mode (e.g., `72.5 kg` or `72.5 كغ`).
- `REQ-RTL-004`: All interactive touch targets (Add Set, Complete, Delete, Member Switcher) must have a minimum interactive dimension of 48x48 dp.
- `REQ-RTL-005`: Number input fields must trigger dedicated numeric keyboards with decimal support.

---

## 4. Entity Relationships & Field Specifications

```
Member (1) ────< (N) WorkoutSession (1) ────< (N) WorkoutExercise (1) ────< (N) WorkoutSet
                      ^
                      | (references)
Exercise (1) ─────────┘
```

### Entity: Member
| Field | Type | Required? | Constraints / Notes |
| :--- | :--- | :---: | :--- |
| `id` | Identifier | Yes | Unique system identifier |
| `name` | String | Yes | 1–50 characters, trimmed, unique |
| `color_hex` | String | Yes | Hex color string (e.g. `#4CAF50`) |
| `created_at` | Timestamp | Yes | Creation time in ms |

### Entity: Exercise
| Field | Type | Required? | Constraints / Notes |
| :--- | :--- | :---: | :--- |
| `id` | Identifier | Yes | Unique system identifier |
| `name_en` | String | Yes | English display name |
| `name_ar` | String | Yes | Arabic display name |
| `category` | Enum | Yes | CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE |
| `is_custom` | Boolean | Yes | `false` for preloaded standard items |
| `creator_member_id` | Identifier | No | Present if created by a specific member |

### Entity: WorkoutSession
| Field | Type | Required? | Constraints / Notes |
| :--- | :--- | :---: | :--- |
| `id` | Identifier | Yes | Unique system identifier |
| `member_id` | Identifier | Yes | Owner member (Cascade on member delete) |
| `status` | Enum | Yes | `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `started_at` | Timestamp | Yes | Session start time in ms |
| `ended_at` | Timestamp | No | Set upon workout completion |
| `notes` | String | No | Optional workout notes |

### Entity: WorkoutExercise
| Field | Type | Required? | Constraints / Notes |
| :--- | :--- | :---: | :--- |
| `id` | Identifier | Yes | Unique system identifier |
| `workout_id` | Identifier | Yes | Parent session (Cascade on session delete) |
| `exercise_id` | Identifier | Yes | Catalog exercise reference |
| `order_index` | Integer | Yes | Display sequence in workout |

### Entity: WorkoutSet
| Field | Type | Required? | Constraints / Notes |
| :--- | :--- | :---: | :--- |
| `id` | Identifier | Yes | Unique system identifier |
| `workout_exercise_id` | Identifier | Yes | Parent workout exercise (Cascade) |
| `set_number` | Integer | Yes | 1-indexed sequential number |
| `repetitions` | Integer | Yes | Min: 1, Max: 999 |
| `weight_kg` | Decimal | No | Min: 0.0, Max: 999.5. Null or 0 = Bodyweight |
| `is_completed` | Boolean | Yes | Default: `false` until logged |

---

## 5. Acceptance Scenarios (Given / When / Then)

### Scenario A: Multi-Member Concurrent In-Progress Sessions
- **Given** Member "Ahmed" has an in-progress workout with 2 completed sets.
- **When** Ahmed switches the active member to "Omar" and Omar starts a workout session.
- **Then** Omar's new session is started without affecting Ahmed's session; when switching back to Ahmed, his in-progress workout is ready to be resumed with its 2 completed sets intact.

### Scenario B: Interrupted App Reopen
- **Given** an active workout session is in progress for the active member.
- **When** the app process is killed and reopened.
- **Then** the user lands on the Dashboard showing a prominent "Resume In-Progress Workout" hero banner with the elapsed timer and active exercise summary.

### Scenario C: Bodyweight Exercise Logging
- **Given** an active workout with exercise "Pull-ups".
- **When** the user logs Set 1 with 12 reps and leaves the weight field blank.
- **Then** Set 1 is saved as completed and displays as `"12 reps"` / `"12 تكرار"` without any awkward `0.0 kg` label.

### Scenario D: Member Permanent Cascade Deletion
- **Given** Member "Layla" has 4 past workouts and 1 active in-progress workout.
- **When** the user deletes Layla's profile and confirms the permanent deletion dialog.
- **Then** Layla's profile, all 4 past workouts, the active workout, and all associated sets are permanently deleted from the database.

### Scenario E: Arabic RTL Decimal Weight Formatting
- **Given** the app language is set to Arabic (`ar`).
- **When** reviewing a set logged with `87.5 kg`.
- **Then** the weight is displayed with LTR numeric formatting as `87.5 كغ` with the decimal separator correctly positioned between 87 and 5.

---

## 6. Edge Cases & Resilience Rules

1. **Debouncing Taps**: Double-tapping "Finish Workout" or "Add Set" must be debounced to prevent duplicate session records or duplicate set rows.
2. **Empty Sessions**: Tapping "Finish Workout" on a session with zero logged sets presents a confirmation dialog: *"No sets logged. Do you want to discard this session?"*
3. **Zero Members State**: Deleting the last remaining member forces the app back to the initial Member Creation onboarding flow.
4. **Re-indexing on Delete**: When a set is deleted, all subsequent sets in that exercise immediately decrement their visible `set_number` by 1.

---

## 7. Explicit Out-of-Scope Exclusions

The following features are strictly **NON-GOALS** for the MVP:
- ❌ Cloud synchronization, user accounts, passwords, or backend servers.
- ❌ AI workout generation, automated weight progression algorithms, or coaching bots.
- ❌ Social sharing, feeds, comments, or external community leaderboards.
- ❌ In-app subscriptions, ads, or payment integrations.
- ❌ External sensor integrations (Bluetooth heart-rate monitors, smart watch syncing).
