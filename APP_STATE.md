# APP_STATE.md

## [Implemented]
- **Three-Level Flat Visual Hierarchy**:
  - Page Background: `#121212` (near-black)
  - Card Background: `#1E1E1E` (flat surface, 0dp elevation)
  - Nested / Inset Elements: `#181818` (token value chips and inputs)
  - Primary Accent: `#5B8DEF` used sparingly on active states, token code values, and primary buttons
  - Body Text: `#E8E8E8` / Secondary & Muted Text: `#8A8A8A`
  - Borders: 1px solid `#2A2A2A` hairline borders across all cards, chips, and search bars
  - Removed all drop shadows and glow effects in favor of clean, distinct surface levels
- **Typography Calibration**:
  - UI Text: Clean system sans-serif across labels, headers, category filters, and body descriptions
  - Token Values / Code: Monospace font (Roboto Mono) tinted in `#5B8DEF` accent
  - Hierarchy Per Card:
    1. Token / Macro name — 15sp medium weight, primary text color
    2. Category tag — 11sp muted color in top-right corner, text only (no background pill)
    3. Value chip — inset `#181818` background, monospace font, single copy icon on right (no "Tap to copy" label)
    4. Description — 12sp muted text below the value chip
- **Magic Text Presentation & Live Evaluation**:
  - Live data evaluation format e.g., `Date: (Jan 2, 1975 [using live data])`
  - 1-tap full-card and value chip click-to-copy to clipboard with instant confirmation
  - Bracket preference switcher (`{ Curly }` vs `[ Square ]`) moved from floating overlay to clean settings modal sheet
- **Dual Magic Text Libraries**:
  - **All Magic Text Library**: Comprehensive atomic token catalog (Date & Time, Battery, Device, Connectivity, Notification, Phone, GPS, Flow) with live device evaluation
  - **Combinations & Formulas Library**: Built-in multi-token formulas + user-saved Room database formulas with sub-token extraction chips
- **Category Filter Pills**: Active state with solid `#5B8DEF` accent fill and dark text; inactive state with transparent fill, 1px border, and muted text.
- **Local Room Persistence Engine**: Full Room database with `Macro`, `Preset`, `Webhook`, `WebhookLog`, and relations.
- **Webhooks Dispatcher**: Incoming/outgoing webhook manager with leading toggle switches, relation inspection, log history, and manual simulator.
- **Macro Staging Detail & Auto-Parser**: Staging editor with status badges, trigger selector, blocking issue alerts, linked presets, and auto-parser for MacroDroid exports.

## [Next Up]
- JSON data backup export & import utility for device transfer.
- Batch action tagging and bulk status updates.

## [Out of Scope]
- Cloud-hosted remote sync (strictly local-first Room database).
- Background native service execution engine.

## [Files]
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
