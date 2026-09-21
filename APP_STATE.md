# APP_STATE.md

## [Implemented]
- **Fixed Build & Dependency Resolution Errors**:
  - Restored `gradle/libs.versions.toml` with version catalog entries.
  - Enabled `android.useAndroidX=true` in `gradle.properties`.
  - Fixed `compileSdk = 35` and `targetSdk = 35` in `app/build.gradle.kts`.
- **Fixed App Launch Crash**:
  - Added missing `MainActivity.kt` entry point linked in `AndroidManifest.xml` with `enableEdgeToEdge()` and `WorkshopApp()` UI root initialization.
- **5 Configurable Navigation Layout Modes (`NavigationLayout`)**:
  - **Bottom Bar**: Classic material bottom navigation tabs with quick layout switcher item.
  - **Top Segmented Tabs**: IDE & Workbench horizontal header tab bar with crisp active/inactive states.
  - **Slide-out Navigation Drawer**: Clean side menu hiding all navigation chrome, with header branding and quick layout option.
  - **Side Navigation Rail**: Vertical side column pinned to left, ideal for landscape, DeX, or desktop density.
  - **Floating Capsule / Island**: Modern floating pill island centered at the bottom with active pill indicators.
- **Navigation Layout Configurator Sheet**:
  - Modal sheet accessible from any navigation mode via the `ViewQuilt` layout button.
  - Instant 1-tap live preview and persistent layout switching across all screens.
- **Three-Level Flat Visual Hierarchy**:
  - Page Background: `#121212` (near-black)
  - Card Background: `#1E1E1E` (flat surface, 0dp elevation)
  - Nested / Inset Elements: `#181818` (token value chips and inputs)
  - Primary Accent: `#5B8DEF` used on active states, token code values, and primary buttons
  - Body Text: `#E8E8E8` / Secondary Text: `#8A8A8A`
  - Borders: 1px solid `#2A2A2A` hairline borders across all cards and bars
- **Magic Text Presentation & Dual Libraries**:
  - All Magic Text Library (atomic tokens) + Combinations & Formulas Library (multi-token formulas with sub-token extraction chips).
  - Bracket preference switcher (`{ Curly }` vs `[ Square ]`) in settings sheet.
- **Unified DateTime Grouping & Custom DateTime Builder**:
  - Consolidated all date, time, clock, calendar, and timestamp tokens into a single unified `Date & Time` (`DATE_TIME`) category.
  - Interactive "Build Your Own DateTime" block builder where users assemble custom date/time Magic Text formulas by tapping modular pieces (Date, Time, Separators).
  - Live result evaluation, one-tap "Copy Magic Text", quick preset recipes, and direct "Save as Preset" integration.
  - Sub-grouping layout under Date & Time category with section headers (`Date & Calendar`, `Time & Clock`, `System Timestamps`).
- **Local Room Persistence Engine & Webhooks Dispatcher**:
  - Full Room database with `Macro`, `Preset`, `Webhook`, `WebhookLog`, and relations.

## [Next Up]
- JSON data backup export & import utility for device transfer.
- Batch action tagging and bulk status updates.

## [Out of Scope]
- Cloud-hosted remote sync (strictly local-first Room database).
- Background native service execution engine.

## [Files]
- `gradle/libs.versions.toml`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/ui/WorkshopApp.kt`
- `app/src/main/java/com/example/ui/WorkshopViewModel.kt`
- `app/src/main/java/com/example/ui/screens/WorkbenchScreen.kt`
- `app/src/main/java/com/example/ui/screens/ArchiveScreen.kt`
- `app/src/main/java/com/example/ui/screens/MacroDetailScreen.kt`
- `app/src/main/java/com/example/ui/screens/PresetsScreen.kt`
- `app/src/main/java/com/example/ui/screens/WebhooksScreen.kt`
- `app/src/main/java/com/example/ui/components/CommonComponents.kt`
- `app/src/main/java/com/example/ui/components/MacroComponents.kt`
- `app/src/main/java/com/example/ui/components/DateTimeBuilder.kt`
- `app/src/main/java/com/example/ui/theme/Color.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/theme/Type.kt`
- `app/src/main/java/com/example/data/AppDatabase.kt`
- `app/src/main/java/com/example/data/Converters.kt`
- `app/src/main/java/com/example/data/WorkshopRepository.kt`
- `app/src/main/java/com/example/data/dao/MacroDao.kt`
- `app/src/main/java/com/example/data/dao/PresetDao.kt`
- `app/src/main/java/com/example/data/dao/WebhookDao.kt`
- `app/src/main/java/com/example/data/models/Enums.kt`
- `app/src/main/java/com/example/data/models/Macro.kt`
- `app/src/main/java/com/example/data/models/Preset.kt`
- `app/src/main/java/com/example/data/models/Webhook.kt`
- `app/src/main/java/com/example/data/models/WebhookLog.kt`
- `app/src/main/java/com/example/data/models/MacroPresetCrossRef.kt`
- `app/src/main/java/com/example/data/models/MacroWebhookCrossRef.kt`
- `app/src/main/java/com/example/data/models/Relations.kt`
- `app/src/main/java/com/example/util/MagicTextEvaluator.kt`
- `app/src/main/java/com/example/util/MacroDroidParser.kt`
