# AGENTS.md — Habit Tracker

## Project Identity

| Field | Value |
|---|---|
| App name | Habit Tracker |
| Package | `com.haruma.habit.tracker` |
| Min SDK | 29 (Android 10) |
| Compile / Target SDK | 36 |
| Kotlin | 2.2.10 |
| AGP | 9.2.1 |
| Compose BOM | 2026.02.01 |

| Hilt | 2.59.2 — minimum version with AGP 9 support (`BaseExtension` was removed in AGP 9; versions < 2.59.2 throw `Android BaseExtension not found`) |

- `kotlin-android` plugin is **not** declared in `app/build.gradle.kts` — AGP 9.x bundles it automatically. Adding it explicitly causes a classpath conflict error.
- `kotlinOptions { jvmTarget }` block must **not** be used with AGP 9 — it has been removed from the Android DSL. JVM target is set via `compileOptions` only.
- `android.disallowKotlinSourceSets=false` must be set in `gradle.properties` because AGP 9's built-in Kotlin disallows plugins (like KSP) from using the `kotlin.sourceSets` DSL by default.
- KSP version must match the Kotlin version prefix: `2.2.10-2.0.2` for Kotlin `2.2.10`. Note the KSP 2.x versioning scheme — it is **not** `1.0.x` for Kotlin 2.x.


```
View   →  Composable screens + components
ViewModel  →  StateFlow<UiState>, viewModelScope coroutines
Model  →  HabitRepository (concrete @Singleton, no interface)
```

There is **no domain layer**. No use cases. No repository interfaces.
ViewModels inject `HabitRepository` and `UserPreferencesRepository` directly via Hilt.

### Folder structure

```
com.haruma.habit.tracker/
├── data/           Room entities, DAOs, AppDatabase, HabitRepository, UserPreferencesRepository
├── di/             AppModule.kt (single Hilt module — DB + repos)
├── navigation/     AppNavHost.kt, route @Serializable objects
├── notifications/  HabitReminderWorker, ReminderScheduler
└── ui/
    ├── theme/      HabitTrackerTheme, Color, Type (already exists)
    ├── splash/     SplashViewModel
    ├── onboarding/ OnboardingScreen, OnboardingViewModel, OnboardingPage enum
    ├── today/      TodayScreen, TodayViewModel, components/
    ├── form/       HabitFormSheet, HabitFormViewModel          ← NOT add_edit
    ├── stats/      StatsScreen, StatsViewModel, components/
    └── settings/   SettingsScreen, SettingsViewModel
```

---

## Strict Rules

### No comments
Zero code comments anywhere. No `//`, no `/* */`, no KDoc. Clean production code only.

### No hardcoded strings
Every user-visible string must come from `res/values/strings.xml` via `stringResource(R.string.*)`.
Never inline text in Kotlin or Compose files.

### Localization
Default locale: English (`res/values/strings.xml`).
Vietnamese: `res/values-vi/strings.xml` — every key in the default file must have a corresponding key here.
Adding a new string = add it to **both** files immediately.

### Naming
The form/add-edit screen is always called **`form`** — package `ui/form/`, files `HabitFormSheet.kt` and `HabitFormViewModel.kt`. Never `add_edit`, `AddEditSheet`, or similar.

### No interface for Repository
`HabitRepository` is a concrete `@Singleton` class, not an interface. Do not create `HabitRepositoryImpl`.

---

## Key Libraries & Patterns

### State management
- ViewModels expose `StateFlow<UiState>` using `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Screens collect with `collectAsStateWithLifecycle()`.
- UiState is a sealed interface with `Loading`, `Success(data)`, `Error(message)` variants.

### Dependency Injection
- Single Hilt module: `di/AppModule.kt`.
- `@HiltAndroidApp` on `HabitTrackerApplication`.
- `@AndroidEntryPoint` on `MainActivity`.
- `@HiltViewModel` + `@Inject constructor` on every ViewModel.
- WorkManager uses Hilt via `HiltWorkerFactory` and `Configuration.Provider` on the Application class.

### Navigation
Routes are `@Serializable` objects/data classes in `navigation/AppNavHost.kt`:
```
OnboardingRoute   — shown only on first launch (DataStore flag)
TodayRoute        — main tab
StatsRoute        — stats tab
SettingsRoute     — settings tab
AddHabitRoute     — navigates to HabitFormSheet
EditHabitRoute(habitId: Int)  — navigates to HabitFormSheet with pre-filled data
```
`NavigationSuiteScaffold` auto-switches `NavigationBar` ↔ `NavigationRail` ↔ `NavigationDrawer` by window size.
The nav bar is hidden on `OnboardingRoute`.

### Splash
Uses AndroidX SplashScreen API (`core-splashscreen`).
Theme `Theme.HabitTracker.Splash` is set on `MainActivity` in the manifest.
`SplashViewModel` reads `UserPreferencesRepository.hasSeenOnboarding` and resolves the start destination.
`splashScreen.setKeepOnScreenCondition { startDestination == null }` holds the splash until resolved.
No artificial delays.

### Onboarding
3-page `HorizontalPager` (`OnboardingPage` enum: `TRACK`, `STREAK`, `STATS`).
Skip and Next/Get Started buttons at the bottom.
`OnboardingViewModel.finish()` writes `hasSeenOnboarding = true` to DataStore.
After finish, nav pops `OnboardingRoute` inclusive and goes to `TodayRoute`.

### Pagination
`HabitDao.pagingSource()` returns `PagingSource<Int, HabitEntity>`.
`HabitRepository.habitsPager()` wraps it: `PagingConfig(pageSize = 20, enablePlaceholders = false)`.
`TodayViewModel` exposes `Flow<PagingData<...>>` with `.cachedIn(viewModelScope)`.
`TodayScreen` uses `collectAsLazyPagingItems()` and `items(count, key)` in `LazyColumn`.
Always handle `loadState.refresh` (initial load) and `loadState.append` (next page) with a loading indicator and retry button.

### Database
Room 2.8.4 with KSP (not kapt).
Two tables: `habits`, `habit_completions`.
`habit_completions.completedDateEpochDay` stores `LocalDate.toEpochDay()` — one row per calendar day per habit.
`ForeignKey` with `CASCADE` delete from `habits` to `habit_completions`.
`exportSchema = true` on `AppDatabase`.

### DataStore
`UserPreferencesRepository` wraps `preferencesDataStore("user_prefs")` via Kotlin property delegate.
Keys: `has_seen_onboarding`, `theme_mode`, `notifications_enabled`, `default_reminder_minutes`.
All reads are `Flow<T>`, all writes are `suspend fun`.

### Reminders
`HabitReminderWorker` is a `@HiltWorker` using `CoroutineWorker`.
WorkManager is initialized manually via `Configuration.Provider` on `HabitTrackerApplication` (the auto-init is removed from the manifest `startup` provider).

### Theme
`HabitTrackerTheme` in `ui/theme/` supports dynamic color (Material You) on API 31+.
`SettingsViewModel.themeMode: StateFlow<String>` drives `darkTheme` in `MainActivity`.
`String.isDark(isSystemDark: Boolean): Boolean` extension lives in `SettingsViewModel.kt`.

---

## What Is Already Implemented (Scaffold)

| File | Status |
|---|---|
| `gradle/libs.versions.toml` | Complete — all dependencies declared |
| `app/build.gradle.kts` | Complete — all plugins and deps wired |
| `AndroidManifest.xml` | Complete — Hilt app, splash theme, permissions, WorkManager init removed |
| `HabitTrackerApplication.kt` | Complete |
| `MainActivity.kt` | Complete |
| `data/HabitEntity.kt` | Complete |
| `data/HabitCompletionEntity.kt` | Complete |
| `data/HabitDao.kt` | Complete |
| `data/CompletionDao.kt` | Complete |
| `data/AppDatabase.kt` | Complete |
| `data/HabitRepository.kt` | Complete |
| `data/UserPreferencesRepository.kt` | Complete |
| `di/AppModule.kt` | Complete |
| `navigation/AppNavHost.kt` | Complete |
| `ui/splash/SplashViewModel.kt` | Complete |
| `ui/onboarding/OnboardingViewModel.kt` | Complete |
| `ui/onboarding/OnboardingScreen.kt` | Complete (layout done, illustrations pending) |
| `ui/today/TodayScreen.kt` | **Stub** — Scaffold shell only |
| `ui/today/TodayViewModel.kt` | **Stub** — empty body |
| `ui/form/HabitFormSheet.kt` | **Stub** — ModalBottomSheet shell only |
| `ui/form/HabitFormViewModel.kt` | **Stub** — empty body |
| `ui/stats/StatsScreen.kt` | **Stub** — Scaffold shell only |
| `ui/stats/StatsViewModel.kt` | **Stub** — empty body |
| `ui/settings/SettingsScreen.kt` | **Stub** — Scaffold shell only |
| `ui/settings/SettingsViewModel.kt` | Complete — themeMode StateFlow + setThemeMode |
| `res/values/strings.xml` | Complete — all keys defined |
| `res/values-vi/strings.xml` | Complete — all keys translated |
| `res/values/themes.xml` | Complete — base + splash theme |

---

## Build Commands

```bash
./gradlew assembleDebug    # verify build
./gradlew lint             # lint check
./gradlew test             # unit tests
```

Run `./gradlew assembleDebug` after any dependency or plugin change to confirm KSP + Hilt wiring compiles.
