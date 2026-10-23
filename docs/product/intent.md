# Product Intent: AZM (عزم) Gym Tracker

**Status**: Draft for Human Review (Gate B)  
**Date**: 2026-10-24  
**Author**: Product & Architecture Team  

---

## 1. Product Vision

**AZM / عزم** is a dependable, offline-first Android application designed to track gym workouts with zero friction for multiple individuals sharing a single mobile device. It empowers workout partners, families, or gym buddies to log every exercise, set, repetition, and weight with high speed, data integrity, and natural Arabic RTL ergonomics—completely free of cloud accounts, paywalls, or internet dependencies.

---

## 2. User Problem

Gym-goers currently face significant friction when trying to track workouts:
1. **Cloud & Login Barrier**: Most gym apps mandate online account creation, email verification, and continuous connectivity, failing in gym basements or low-reception facilities.
2. **Device Sharing Incompatibility**: Existing apps assume a strict 1-user-per-phone model. Workout partners or friends training together who share a device have to mix up their workout weights, reps, and history into a single profile.
3. **Complex, Distracting UIs**: Bloated social feeds, AI recommendations, and subscription upsells distract from the fast-paced nature of lifting weights between rest periods.
4. **Poor Arabic / RTL Ergonomics**: Arabic interfaces in fitness apps are often poorly translated machine strings with broken RTL number displays that invert weights and decimals.

---

## 3. Target Users

- **Workout Partners & Gym Buddies**: Two or more people training together who share one mobile phone during sessions.
- **Gym Enthusiasts & Powerlifters**: Individuals who value raw performance logging (sets, reps, weight) over social features.
- **Arabic-Speaking Trainees**: Users in the MENA region who expect native RTL interfaces and culturally appropriate UI design.
- **Privacy-Conscious Users**: People who want their personal fitness and physical progression records stored strictly on their device without third-party tracking.

---

## 4. Product Goals

1. **Zero-Latency Logging**: Enable adding a set with reps and weight in under 5 seconds.
2. **Complete Data Isolation**: Guarantee that Member A's workouts, history, and exercises never bleed into Member B's records.
3. **Offline Reliability**: Ensure 100% functionality with airplane mode active.
4. **Flawless Arabic RTL**: Ensure natural Arabic typography, intuitive directional flows, and non-inverted decimal weights (`72.5 kg`).
5. **Session Durability**: Active workout state must survive backgrounding, app restarts, and process recreation without data loss.

---

## 5. MVP Scope

The AZM MVP encompasses the following essential bounded features:

### A. Member Management
- Create, view, edit, and delete member profiles (name, avatar/color identifier).
- Quick member switching from the top app bar or home dashboard.
- Display active member context prominently across all workout logging screens.

### B. Exercise Catalog
- Organized catalog grouped by muscle category (e.g., Chest, Back, Legs, Shoulders, Arms, Core).
- Pre-populated standard gym exercises (with English & Arabic names).
- Capability to add custom exercises per catalog.

### C. Active Workout Session
- Start a workout session tied to the currently active member.
- Add one or multiple exercises to the active session.
- Add sets sequentially for each exercise.
- Independently record:
  - Repetition count (mandatory integer).
  - Weight in kg (optional decimal/integer, e.g. `85` or `87.5`).
- Remove or modify set rows during active logging.
- Clear finish/cancel session controls with confirmation dialogs.

### D. Workout History & Review
- Chronological list of completed workout sessions filtered strictly by the active member.
- Detailed session view showing timestamp, duration, exercise list, and all sets logged.
- Edit/Delete past session capabilities adhering to explicit data integrity rules.

### E. Local Persistence & Settings
- Room (SQLite) database holding all members, exercises, sessions, and sets.
- Offline-first local backup/export capability (or automated local SQLite safety).
- Seamless UI theme and language switching (English / Arabic).

---

## 6. Non-Goals (Explicitly Excluded from MVP)

To maintain focus and avoid scope creep, the following are strictly **OUT OF SCOPE** for the MVP:
- ❌ User authentication, cloud sync, or server-backed databases (Firebase, AWS, Supabase).
- ❌ AI-generated workout recommendations or automated coaching.
- ❌ Social sharing, friend lists, or workout feeds.
- ❌ In-app subscriptions, ads, or monetization systems.
- ❌ Bluetooth integrations with wearable heart-rate monitors or smart barbells.
- ❌ Cloud backup or cross-device account synchronization.

---

## 7. Success Criteria

| Metric | Target | Verification Method |
| :--- | :--- | :--- |
| **Offline Operation** | 100% | Verified by running complete flows in Airplane Mode |
| **Member Isolation** | 0% Data Leakage | Verified by automated DAO & database integration tests |
| **Set Logging Speed** | < 5 seconds | Manual usability testing on physical device |
| **RTL Usability** | 0 layout distortions | Verified on Android RTL developer mode with Arabic locale |
| **Persistence Survival** | 100% session recovery | Verified after app process kill and device reboot |
| **Crash Rate** | 0 unhandled crashes | Zero crashes in critical paths (create member, log session, view history) |

---

## 8. Assumptions & Unresolved Questions

### Explicit Assumptions
1. **Weight Unit**: Kilograms (kg) is the default unit for MVP.
2. **Single Active Session**: Only one member can have an active workout in progress simultaneously on the shared device.
3. **Data Deletion Policy**: Deleting a member profile will delete their associated workout history (via cascading delete) after a strong user confirmation warning.

### Open Questions for Review
1. *Concurrent Alternating Sets*: Do training partners alternate sets within the same active session (e.g., Partner A does Bench set 1, Partner B does Bench set 1 on the same screen), or does each person start and finish their own session separately?
   - *Default MVP Assumption*: Each member runs their own session separately, with the ability to pause and switch active members if needed.
2. *Default Catalog Content*: Should the initial preloaded catalog be bundled locally in a pre-populated Room SQLite asset database?
   - *Recommendation*: Yes, ship an initial list of standard gym exercises in both Arabic and English.
