package com.example.util

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BracketStyle(
    val label: String,
    val openChar: Char,
    val closeChar: Char
) {
    CURLY("Curly { }", '{', '}'),
    SQUARE("Square [ ]", '[', ']');

    fun formatToken(rawToken: String): String {
        val clean = rawToken.trim()
            .removePrefix("[").removeSuffix("]")
            .removePrefix("{").removeSuffix("}")
        return "$openChar$clean$closeChar"
    }

    fun formatTemplate(template: String): String {
        // Replaces all [token] and {token} with active bracket style
        val regex = Regex("[\\[{]([^\\]}]+)[\\]}]")
        return regex.replace(template) { matchResult ->
            val tokenContent = matchResult.groupValues[1]
            "$openChar$tokenContent$closeChar"
        }
    }
}

enum class MagicTextCategory(val displayName: String) {
    ALL("All"),
    DATE_TIME("Date & Time"),
    BATTERY("Battery & Power"),
    DEVICE("Device & System"),
    CONNECTIVITY("Connectivity"),
    NOTIFICATIONS("Notifications"),
    PHONE_SMS("Phone & SMS"),
    LOCATION("Location & GPS"),
    VARIABLES("Variables & Flow")
}

enum class CombinationCategory(val displayName: String) {
    ALL("All"),
    DATE_TIME("Date & Time"),
    SYSTEM_POWER("System & Battery"),
    NETWORK_LOCATION("Network & GPS"),
    ALERTS_MESSAGES("Alerts & Messages"),
    LOGS_WEBHOOKS("Logs & Webhooks")
}

data class EvaluatedTokenInfo(
    val rawName: String,
    val formattedName: String,
    val label: String,
    val liveValue: String,
    val isLiveEvaluated: Boolean,
    val subTokens: List<String>
)

data class MagicTextItem(
    val token: String,
    val label: String,
    val description: String,
    val category: MagicTextCategory,
    val exampleValue: String? = null,
    val subGroup: String = ""
)

data class DateTimeBlockPiece(
    val id: String,
    val label: String,
    val tokenValue: String,
    val isLiteral: Boolean = false,
    val description: String = ""
)

object DateTimeBlockLibrary {
    val datePieces = listOf(
        DateTimeBlockPiece("date_year", "Year (2026)", "[date_year]", description = "Full 4-digit year"),
        DateTimeBlockPiece("date_year_short", "Year Short (26)", "[date_year_short]", description = "2-digit shorthand year"),
        DateTimeBlockPiece("date_month_name", "Month Name (September)", "[date_month_name]", description = "Full month name"),
        DateTimeBlockPiece("date_month_short", "Month Short (Sep)", "[date_month_short]", description = "Abbreviated month name"),
        DateTimeBlockPiece("date_month_pad", "Month (01-12)", "[date_month_pad]", description = "2-digit month number"),
        DateTimeBlockPiece("date_month", "Month (1-12)", "[date_month]", description = "Month number"),
        DateTimeBlockPiece("date_day_pad", "Day (01-31)", "[date_day_pad]", description = "2-digit day of month"),
        DateTimeBlockPiece("date_day", "Day (1-31)", "[date_day]", description = "Day of month"),
        DateTimeBlockPiece("day_of_week", "Day of Week (Sunday)", "[day_of_week]", description = "Full weekday name"),
        DateTimeBlockPiece("day_of_year", "Day of Year (263)", "[day_of_year]", description = "Ordinal day count of year")
    )

    val timePieces = listOf(
        DateTimeBlockPiece("hour_12_pad", "Hour 12h (01-12)", "[hour_12_pad]", description = "2-digit 12-hour format"),
        DateTimeBlockPiece("hour_12", "Hour 12h (1-12)", "[hour_12]", description = "12-hour format"),
        DateTimeBlockPiece("hour_24_pad", "Hour 24h (00-23)", "[hour_24_pad]", description = "2-digit 24-hour military format"),
        DateTimeBlockPiece("hour_24", "Hour 24h (0-23)", "[hour_24]", description = "24-hour format"),
        DateTimeBlockPiece("minute_pad", "Minute (00-59)", "[minute_pad]", description = "2-digit minute with zero padding"),
        DateTimeBlockPiece("minute", "Minute (0-59)", "[minute]", description = "Minute without zero padding"),
        DateTimeBlockPiece("second_pad", "Second (00-59)", "[second_pad]", description = "2-digit second with zero padding"),
        DateTimeBlockPiece("second", "Second (0-59)", "[second]", description = "Second without zero padding"),
        DateTimeBlockPiece("am_pm", "AM / PM", "[am_pm]", description = "Upper case AM or PM designation"),
        DateTimeBlockPiece("system_time", "Unix Timestamp", "[system_time]", description = "Current epoch timestamp in ms")
    )

    val separatorPieces = listOf(
        DateTimeBlockPiece("sep_space", "Space ( )", " ", isLiteral = true, description = "Single whitespace separator"),
        DateTimeBlockPiece("sep_hyphen", "Hyphen (-)", "-", isLiteral = true, description = "Hyphen divider"),
        DateTimeBlockPiece("sep_slash", "Slash (/)", "/", isLiteral = true, description = "Forward slash date separator"),
        DateTimeBlockPiece("sep_colon", "Colon (:)", ":", isLiteral = true, description = "Colon time separator"),
        DateTimeBlockPiece("sep_comma", "Comma (,)", ",", isLiteral = true, description = "Comma separator"),
        DateTimeBlockPiece("sep_dot", "Dot (.)", ".", isLiteral = true, description = "Period dot separator"),
        DateTimeBlockPiece("sep_underscore", "Underscore (_)", "_", isLiteral = true, description = "Underscore separator"),
        DateTimeBlockPiece("sep_at", "Text ' at '", " at ", isLiteral = true, description = "Literal ' at ' string"),
        DateTimeBlockPiece("sep_t", "Text 'T'", "T", isLiteral = true, description = "ISO 8601 'T' separator")
    )

    val presetTemplates = listOf(
        "12h Clock" to "[hour_12]:[minute_pad] [am_pm]",
        "24h Military" to "[hour_24_pad]:[minute_pad]:[second_pad]",
        "Friendly Date" to "[date_month_short] [date_day], [date_year]",
        "ISO 8601" to "[date_year]-[date_month_pad]-[date_day_pad] [hour_24_pad]:[minute_pad]:[second_pad]",
        "File Stamp" to "[date_year][date_month_pad][date_day_pad]_[hour_24_pad][minute_pad]",
        "Full Banner" to "[day_of_week], [date_month_name] [date_day], [date_year] at [hour_12]:[minute_pad] [am_pm]"
    )
}

data class MagicCombinationItem(
    val name: String,
    val template: String,
    val description: String,
    val category: CombinationCategory,
    val exampleOutput: String? = null
)

object MagicTextEvaluator {

    /**
     * Built-in Reference Library of all atomic MacroDroid Magic Text Tokens.
     */
    val allMagicTextTokens: List<MagicTextItem> = listOf(
        // Date & Time
        MagicTextItem("date_day", "Day of Month (1-31)", "Day of the month without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_day_pad", "Day of Month Padded (01-31)", "Day of the month with two digits", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_month", "Month Number (1-12)", "Month number without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_month_pad", "Month Padded (01-12)", "Two-digit month number", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_month_short", "Month Short Name (e.g. Jan)", "Three-letter abbreviated month name", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_month_name", "Month Full Name (e.g. January)", "Full name of the current month", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_year", "Four-Digit Year (e.g. 2026)", "Current full calendar year", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("date_year_short", "Two-Digit Year (e.g. 26)", "Two-digit shorthand year", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("day_of_week", "Day of Week (e.g. Monday)", "Full name of current weekday", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("day_of_week_num", "Day of Week Number (1-7)", "Weekday number (1 = Sunday / Monday depending on locale)", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("day_of_year", "Day of Year (1-366)", "Ordinal day count of the current year", MagicTextCategory.DATE_TIME, subGroup = "Date & Calendar"),
        MagicTextItem("hour_24", "Hour 24h (0-23)", "Hour in 24-hour format without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("hour_24_pad", "Hour 24h Padded (00-23)", "Two-digit hour in 24-hour format", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("hour_12", "Hour 12h (1-12)", "Hour in 12-hour clock format without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("hour_12_pad", "Hour 12h Padded (01-12)", "Two-digit hour in 12-hour clock format", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("minute", "Minute (0-59)", "Minute without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("minute_pad", "Minute Padded (00-59)", "Two-digit minute with leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("second", "Second (0-59)", "Seconds without leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("second_pad", "Second Padded (00-59)", "Two-digit second with leading zero", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("am_pm", "AM / PM Marker", "Upper case AM or PM designation", MagicTextCategory.DATE_TIME, subGroup = "Time & Clock"),
        MagicTextItem("system_time", "Unix Epoch Time", "Current system timestamp in milliseconds", MagicTextCategory.DATE_TIME, subGroup = "System Timestamps"),
        MagicTextItem("stopwatch_time", "Stopwatch Elapsed Time", "Elapsed time on the active MacroDroid stopwatch", MagicTextCategory.DATE_TIME, subGroup = "System Timestamps"),
        MagicTextItem("last_macro_run_time", "Last Macro Run Timestamp", "Time when the last macro was executed", MagicTextCategory.DATE_TIME, subGroup = "System Timestamps"),

        // Battery & Power
        MagicTextItem("battery", "Battery Level (%)", "Current battery charge percentage (0-100)", MagicTextCategory.BATTERY),
        MagicTextItem("battery_temp", "Battery Temperature (°C)", "Current battery temperature reading", MagicTextCategory.BATTERY),
        MagicTextItem("battery_voltage", "Battery Voltage (mV)", "Battery voltage in millivolts", MagicTextCategory.BATTERY),
        MagicTextItem("battery_current", "Battery Current Flow (mA)", "Instantaneous charging/discharging current in mA", MagicTextCategory.BATTERY),
        MagicTextItem("charging_status", "Charging State", "Current charge state: Charging, Discharging, Full, or Not Charging", MagicTextCategory.BATTERY),
        MagicTextItem("power_source", "Power Source", "Connection type: AC, USB, Wireless, or Battery", MagicTextCategory.BATTERY),
        MagicTextItem("battery_health", "Battery Health Status", "Health indicator: Good, Overheat, Dead, Over Voltage", MagicTextCategory.BATTERY),
        MagicTextItem("screen_on_time", "Screen On Time", "Duration the screen has remained turned on", MagicTextCategory.BATTERY),

        // Device & System
        MagicTextItem("device_model", "Device Model", "Model name of the device (e.g. Pixel 8 Pro)", MagicTextCategory.DEVICE),
        MagicTextItem("device_manufacturer", "Device Manufacturer", "Hardware manufacturer (e.g. Google, Samsung)", MagicTextCategory.DEVICE),
        MagicTextItem("device_brand", "Device Brand", "Brand marketing name", MagicTextCategory.DEVICE),
        MagicTextItem("device_name", "Device Host Name", "User-configured device Bluetooth/system name", MagicTextCategory.DEVICE),
        MagicTextItem("android_version", "Android OS Version", "Android release version number (e.g. 15)", MagicTextCategory.DEVICE),
        MagicTextItem("sdk_version", "Android SDK Level", "API level number (e.g. 35)", MagicTextCategory.DEVICE),
        MagicTextItem("device_uptime_hours", "Device Uptime (Hours)", "Number of hours since device was last booted", MagicTextCategory.DEVICE),
        MagicTextItem("device_uptime_mins", "Device Uptime (Minutes)", "Total minutes since last system startup", MagicTextCategory.DEVICE),
        MagicTextItem("clipboard", "Clipboard Content", "Current text stored in the Android system clipboard", MagicTextCategory.DEVICE),
        MagicTextItem("screen_brightness", "Screen Brightness (0-255)", "Current screen brightness slider value", MagicTextCategory.DEVICE),
        MagicTextItem("volume_media", "Media Volume", "Current media volume percentage", MagicTextCategory.DEVICE),
        MagicTextItem("volume_ring", "Ringtone Volume", "Current ringer volume percentage", MagicTextCategory.DEVICE),
        MagicTextItem("volume_alarm", "Alarm Volume", "Current alarm volume level", MagicTextCategory.DEVICE),
        MagicTextItem("volume_notification", "Notification Volume", "Current notification chime volume", MagicTextCategory.DEVICE),
        MagicTextItem("ringer_mode", "Ringer Mode", "Current audio mode: Normal, Vibrate, or Silent", MagicTextCategory.DEVICE),
        MagicTextItem("torch_state", "Flashlight / Torch State", "Whether flashlight is on or off", MagicTextCategory.DEVICE),

        // Connectivity & Network
        MagicTextItem("wifi_ssid", "Wi-Fi Network Name (SSID)", "SSID of connected Wi-Fi or '<unknown>'", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("wifi_bssid", "Wi-Fi Router MAC (BSSID)", "Hardware MAC address of access point", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("wifi_link_speed", "Wi-Fi Link Speed", "Current Wi-Fi link throughput (Mbps)", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("ip_address", "Local IP Address", "Assigned IPv4 or IPv6 local network address", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("bluetooth_device", "Connected Bluetooth Device", "Name of active Bluetooth headset or car audio", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("cell_operator", "Cellular Carrier / Operator", "Name of mobile network provider", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("cell_signal_strength", "Cell Signal Strength", "Cell signal quality bars or dBm", MagicTextCategory.CONNECTIVITY),
        MagicTextItem("network_type", "Active Network Type", "Connection type: Wi-Fi, 5G, LTE, or Disconnected", MagicTextCategory.CONNECTIVITY),

        // Notifications
        MagicTextItem("not_title", "Notification Title", "Header title of the triggering notification", MagicTextCategory.NOTIFICATIONS),
        MagicTextItem("not_text", "Notification Body Text", "Main body message of the notification", MagicTextCategory.NOTIFICATIONS),
        MagicTextItem("not_app_name", "Notification App Name", "Display name of the application posting the alert", MagicTextCategory.NOTIFICATIONS),
        MagicTextItem("not_package_name", "Notification App Package", "Package ID (e.g. com.whatsapp)", MagicTextCategory.NOTIFICATIONS),
        MagicTextItem("not_ticker", "Notification Ticker Text", "Status bar ticker preview message", MagicTextCategory.NOTIFICATIONS),
        MagicTextItem("not_sub_text", "Notification Subtitle", "Secondary info line in notification", MagicTextCategory.NOTIFICATIONS),

        // Phone & SMS
        MagicTextItem("call_name", "Caller Contact Name", "Name of contact from phonebook or number if unsaved", MagicTextCategory.PHONE_SMS),
        MagicTextItem("call_number", "Caller Phone Number", "Incoming or outgoing phone number", MagicTextCategory.PHONE_SMS),
        MagicTextItem("call_duration", "Call Duration (Seconds)", "Length of finished voice call", MagicTextCategory.PHONE_SMS),
        MagicTextItem("sms_name", "SMS Sender Contact Name", "Contact name of incoming text sender", MagicTextCategory.PHONE_SMS),
        MagicTextItem("sms_number", "SMS Sender Number", "Phone number of text message sender", MagicTextCategory.PHONE_SMS),
        MagicTextItem("sms_body", "SMS Message Body", "Full text content of received SMS message", MagicTextCategory.PHONE_SMS),
        MagicTextItem("sim_name", "Active SIM Card Name", "Carrier name on dual-SIM devices", MagicTextCategory.PHONE_SMS),

        // Location & GPS
        MagicTextItem("lat", "GPS Latitude", "Current decimal latitude coordinate", MagicTextCategory.LOCATION),
        MagicTextItem("long", "GPS Longitude", "Current decimal longitude coordinate", MagicTextCategory.LOCATION),
        MagicTextItem("location_accuracy", "GPS Accuracy (Meters)", "Horizontal accuracy radius in meters", MagicTextCategory.LOCATION),
        MagicTextItem("location_altitude", "GPS Altitude (Meters)", "Elevation above sea level in meters", MagicTextCategory.LOCATION),
        MagicTextItem("location_speed", "GPS Speed (km/h)", "Calculated movement velocity", MagicTextCategory.LOCATION),
        MagicTextItem("location_bearing", "GPS Heading / Bearing", "Direction of travel in degrees (0-359)", MagicTextCategory.LOCATION),
        MagicTextItem("geocoded_address", "Geocoded Street Address", "Reverse-geocoded reverse street address name", MagicTextCategory.LOCATION),

        // Variables & Flow
        MagicTextItem("macro_name", "Current Macro Name", "Name of the executing macro", MagicTextCategory.VARIABLES),
        MagicTextItem("last_invoked_macro", "Previous Macro Name", "Name of the preceding macro in trigger sequence", MagicTextCategory.VARIABLES),
        MagicTextItem("webhook_param=id", "Webhook Query Parameter", "Extract query parameter 'id' from incoming HTTP payload", MagicTextCategory.VARIABLES),
        MagicTextItem("lv=var_name", "Local Variable", "Value of macro local variable", MagicTextCategory.VARIABLES),
        MagicTextItem("gv=var_name", "Global Variable", "Value of user global variable", MagicTextCategory.VARIABLES),
        MagicTextItem("random=1-100", "Random Number Generator", "Generates random integer in given range", MagicTextCategory.VARIABLES)
    )

    /**
     * Built-in Library of Common Combinations & Formatted Formulas.
     */
    val commonCombinations: List<MagicCombinationItem> = listOf(
        // Date & Time
        MagicCombinationItem(
            name = "Friendly Date",
            template = "[date_month_short] [date_day], [date_year]",
            description = "Formatted calendar date (e.g. Jan 2, 1975 or live date). Standard timestamp for logs, SMS, and announcements.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "Sep 20, 2026"
        ),
        MagicCombinationItem(
            name = "Full Date & Time Banner",
            template = "[day_of_week], [date_month_name] [date_day], [date_year] at [hour_12]:[minute_pad] [am_pm]",
            description = "Complete readable announcement banner with day of week, full month name, and 12-hour clock.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "Sunday, September 20, 2026 at 1:05 PM"
        ),
        MagicCombinationItem(
            name = "ISO 8601 Timestamp",
            template = "[date_year]-[date_month_pad]-[date_day_pad] [hour_24_pad]:[minute_pad]:[second_pad]",
            description = "Standard international machine-readable timestamp for logging and database records.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "2026-09-20 13:05:22"
        ),
        MagicCombinationItem(
            name = "Standard 12-Hour Clock",
            template = "[hour_12]:[minute_pad] [am_pm]",
            description = "Everyday 12-hour clock time with AM/PM indicator for voice alerts and UI banners.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "1:05 PM"
        ),
        MagicCombinationItem(
            name = "Standard 24-Hour Clock",
            template = "[hour_24_pad]:[minute_pad]",
            description = "Standard 24-hour military timestamp for filenames and compact status displays.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "13:05"
        ),
        MagicCombinationItem(
            name = "File Safe Date Stamp",
            template = "[date_year][date_month_pad][date_day_pad]_[hour_24_pad][minute_pad]",
            description = "Compact filename prefix without spaces or colons for audio recordings, photos, and backups.",
            category = CombinationCategory.DATE_TIME,
            exampleOutput = "20260920_1305"
        ),

        // System & Battery
        MagicCombinationItem(
            name = "Battery & Power Diagnostic",
            template = "[battery]% ([power_source] - [charging_status])",
            description = "Comprehensive battery status showing charge percentage, power connection type, and charging state.",
            category = CombinationCategory.SYSTEM_POWER,
            exampleOutput = "85% (AC Charger - Charging)"
        ),
        MagicCombinationItem(
            name = "Device Telemetry Header",
            template = "[device_manufacturer] [device_model] (Android [android_version], SDK [sdk_version])",
            description = "Hardware and OS version summary for bug reports, automation logs, and multi-device routing.",
            category = CombinationCategory.SYSTEM_POWER,
            exampleOutput = "Google Pixel 8 Pro (Android 15, SDK 35)"
        ),
        MagicCombinationItem(
            name = "Audio Volume Snapshot",
            template = "Media: [volume_media]% | Ring: [volume_ring]% | Mode: [ringer_mode]",
            description = "Quick diagnostic string of media volume, ringer volume, and active ringer mode.",
            category = CombinationCategory.SYSTEM_POWER,
            exampleOutput = "Media: 65% | Ring: 80% | Mode: Normal"
        ),

        // Network & GPS
        MagicCombinationItem(
            name = "Wi-Fi & Local IP Summary",
            template = "[wifi_ssid] ([ip_address]) on [network_type]",
            description = "Network state summary including connected SSID, assigned local IP address, and interface type.",
            category = CombinationCategory.NETWORK_LOCATION,
            exampleOutput = "Home_5GHz (192.168.1.104) on Wi-Fi"
        ),
        MagicCombinationItem(
            name = "GPS Google Maps Link",
            template = "https://maps.google.com/?q=[lat],[long]",
            description = "Clickable Google Maps web link constructed from device latitude and longitude coordinates.",
            category = CombinationCategory.NETWORK_LOCATION,
            exampleOutput = "https://maps.google.com/?q=37.7749,-122.4194"
        ),
        MagicCombinationItem(
            name = "Location Geocode Summary",
            template = "[geocoded_address] (Accuracy: [location_accuracy]m)",
            description = "Street address with horizontal GPS precision rating in meters.",
            category = CombinationCategory.NETWORK_LOCATION,
            exampleOutput = "Market St, San Francisco, CA (Accuracy: 12m)"
        ),

        // Alerts & Messages
        MagicCombinationItem(
            name = "Notification Bridge Alert",
            template = "[[not_app_name]] [not_title]: [not_text]",
            description = "Standardized notification extraction string for forwarding chat or app notifications via Telegram or SMS.",
            category = CombinationCategory.ALERTS_MESSAGES,
            exampleOutput = "[WhatsApp] Austin: Hey, are you free?"
        ),
        MagicCombinationItem(
            name = "SMS Auto-Reply Stamp",
            template = "Auto-reply to [sms_name] ([sms_number]) at [hour_12]:[minute_pad] [am_pm]",
            description = "Timestamped receipt footer for automated SMS response macros.",
            category = CombinationCategory.ALERTS_MESSAGES,
            exampleOutput = "Auto-reply to John (+1 555-0192) at 1:05 PM"
        ),
        MagicCombinationItem(
            name = "Incoming Call Announcer",
            template = "Incoming call from [call_name] ([call_number])",
            description = "Formatted text string for Text-to-Speech (TTS) caller voice announcements.",
            category = CombinationCategory.ALERTS_MESSAGES,
            exampleOutput = "Incoming call from Austin Grindy (+1 555-019-2834)"
        ),

        // Logs & Webhooks
        MagicCombinationItem(
            name = "Webhook Query Payload",
            template = "macro=[macro_name]&device=[device_model]&battery=[battery]&status=[charging_status]",
            description = "URL-encoded query parameter string for outgoing HTTP GET or POST webhook dispatch.",
            category = CombinationCategory.LOGS_WEBHOOKS,
            exampleOutput = "macro=AutoBackup&device=Pixel 8 Pro&battery=85&status=Charging"
        ),
        MagicCombinationItem(
            name = "Execution Log Event",
            template = "[[date_year]-[date_month_pad]-[date_day_pad] [hour_24_pad]:[minute_pad]] [macro_name] executed successfully on [device_model]",
            description = "Formatted log line ready for appending to a local CSV or debug text log file.",
            category = CombinationCategory.LOGS_WEBHOOKS,
            exampleOutput = "[2026-09-20 13:05] AutoBackup executed successfully on Pixel 8 Pro"
        )
    )

    /**
     * Extracts individual token identifiers from a preset name or template string.
     * e.g., "[hour_24]:[minute]" -> ["hour_24", "minute"]
     */
    fun extractSubTokens(input: String): List<String> {
        val regex = Regex("[\\[{]([^\\]}]+)[\\]}]")
        val matches = regex.findAll(input).map { it.groupValues[1] }.toList()
        return if (matches.isNotEmpty()) {
            matches.distinct()
        } else {
            val clean = input.trim()
                .removePrefix("[").removeSuffix("]")
                .removePrefix("{").removeSuffix("}")
            if (clean.isNotBlank()) listOf(clean) else emptyList()
        }
    }

    /**
     * Derives a clean, readable Category / Display Label for a preset.
     */
    fun deriveLabel(presetName: String, presetTags: String = ""): String {
        val lower = presetName.lowercase()
        val tagsLower = presetTags.lowercase()

        return when {
            lower.contains("date_month_short") || (lower.contains("date") && lower.contains("year")) || lower.contains("calendar") -> "Date"
            lower.contains("iso") || (lower.contains("date_year") && lower.contains("second")) -> "ISO Timestamp"
            lower.contains("hour_12") || lower.contains("am_pm") -> "12-Hour Clock"
            lower.contains("hour_24") || (lower.contains("hour") && lower.contains("minute")) -> "24-Hour Time"
            lower.contains("battery") && (lower.contains("charging") || lower.contains("power")) -> "Battery & Power"
            lower.contains("battery") -> "Battery Level"
            lower.contains("maps.google") || (lower.contains("lat") && lower.contains("long")) -> "GPS Location Link"
            lower.contains("wifi") && lower.contains("ip") -> "Wi-Fi & IP"
            lower.contains("wifi") || lower.contains("ssid") -> "Wi-Fi SSID"
            lower.contains("call") || lower.contains("caller") -> "Call / Contact"
            lower.contains("sms") -> "SMS Message"
            lower.contains("not_") || lower.contains("notification") -> "Notification"
            lower.contains("webhook") -> "Webhook Param"
            lower.contains("uptime") -> "Device Uptime"
            lower.contains("device") || lower.contains("model") -> "Device Info"
            lower.contains("volume") || lower.contains("ringer") -> "Volume & Audio"
            lower.contains("clip") -> "Clipboard"
            tagsLower.contains("battery") -> "Battery"
            tagsLower.contains("time") -> "Time"
            tagsLower.contains("date") -> "Date"
            tagsLower.contains("wifi") -> "Connectivity"
            tagsLower.contains("sms") -> "Messaging"
            tagsLower.contains("notification") -> "Notification"
            tagsLower.contains("webhook") -> "Webhook"
            else -> {
                val clean = presetName.replace(Regex("[\\[\\]{}]"), "").replace("_", " ").trim()
                if (clean.length in 1..25) {
                    clean.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                } else {
                    "Magic Text"
                }
            }
        }
    }

    /**
     * Evaluates live or realistic sample data for given token string or combination template.
     */
    fun evaluateLiveSample(tokenTemplate: String, context: Context? = null): Pair<String, Boolean> {
        val now = Date()
        val lower = tokenTemplate.lowercase()

        // 1. Check if it's a multi-token combination template
        if (tokenTemplate.contains("[") || tokenTemplate.contains("{")) {
            var isLiveEvaluated = true
            var evaluatedString = tokenTemplate

            // Replace known tokens with evaluated live values
            val dateMonthShort = SimpleDateFormat("MMM", Locale.getDefault()).format(now)
            val dateMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(now)
            val dateMonthPad = SimpleDateFormat("MM", Locale.getDefault()).format(now)
            val dateMonthNum = SimpleDateFormat("M", Locale.getDefault()).format(now)
            val dateDay = SimpleDateFormat("d", Locale.getDefault()).format(now)
            val dateDayPad = SimpleDateFormat("dd", Locale.getDefault()).format(now)
            val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(now)
            val dateYearShort = SimpleDateFormat("yy", Locale.getDefault()).format(now)
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
            val hour24 = SimpleDateFormat("H", Locale.getDefault()).format(now)
            val hour24Pad = SimpleDateFormat("HH", Locale.getDefault()).format(now)
            val hour12 = SimpleDateFormat("h", Locale.getDefault()).format(now)
            val hour12Pad = SimpleDateFormat("hh", Locale.getDefault()).format(now)
            val minute = SimpleDateFormat("m", Locale.getDefault()).format(now)
            val minutePad = SimpleDateFormat("mm", Locale.getDefault()).format(now)
            val second = SimpleDateFormat("s", Locale.getDefault()).format(now)
            val secondPad = SimpleDateFormat("ss", Locale.getDefault()).format(now)
            val amPm = SimpleDateFormat("a", Locale.getDefault()).format(now).uppercase()

            // Battery resolution
            var batteryPct = "85"
            var chargingStatus = "Charging"
            var powerSource = "AC Charger"
            if (context != null) {
                try {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    if (level >= 0 && scale > 0) {
                        batteryPct = ((level * 100) / scale).toString()
                    }
                    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    chargingStatus = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                        BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                        else -> "Discharging"
                    }
                    val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                    powerSource = when (chargePlug) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                        else -> "Battery Power"
                    }
                } catch (_: Exception) {}
            }

            // Device resolution
            val deviceModel = Build.MODEL ?: "Pixel 8 Pro"
            val deviceMfg = Build.MANUFACTURER ?: "Google"
            val androidVer = Build.VERSION.RELEASE ?: "15"
            val sdkVer = Build.VERSION.SDK_INT.toString()

            // Replacement dictionary
            val map = mapOf(
                "date_month_short" to dateMonthShort,
                "date_month_name" to dateMonthName,
                "date_month_pad" to dateMonthPad,
                "date_month" to dateMonthNum,
                "date_day_pad" to dateDayPad,
                "date_day" to dateDay,
                "date_year_short" to dateYearShort,
                "date_year" to dateYear,
                "day_of_week" to dayOfWeek,
                "hour_24_pad" to hour24Pad,
                "hour_24" to hour24,
                "hour_12_pad" to hour12Pad,
                "hour_12" to hour12,
                "minute_pad" to minutePad,
                "minute" to minute,
                "second_pad" to secondPad,
                "second" to second,
                "am_pm" to amPm,
                "battery" to batteryPct,
                "battery_temp" to "28.5",
                "battery_voltage" to "4120",
                "charging_status" to chargingStatus,
                "power_source" to powerSource,
                "device_model" to deviceModel,
                "device_manufacturer" to deviceMfg,
                "android_version" to androidVer,
                "sdk_version" to sdkVer,
                "wifi_ssid" to "Home_5GHz",
                "ip_address" to "192.168.1.104",
                "network_type" to "Wi-Fi",
                "lat" to "37.7749",
                "long" to "-122.4194",
                "location_accuracy" to "12",
                "geocoded_address" to "Market St, San Francisco, CA",
                "not_app_name" to "WhatsApp",
                "not_title" to "Austin",
                "not_text" to "Hey, are you free?",
                "sms_name" to "John",
                "sms_number" to "+1 555-0192",
                "sms_body" to "Meeting is confirmed for 2 PM",
                "call_name" to "Austin Grindy",
                "call_number" to "+1 (555) 019-2834",
                "volume_media" to "65",
                "volume_ring" to "80",
                "ringer_mode" to "Normal",
                "macro_name" to "AutoBackup",
                "device_uptime_hours" to "48"
            )

            val tokenRegex = Regex("[\\[{]([^\\]}]+)[\\]}]")
            var matchedAny = false
            evaluatedString = tokenRegex.replace(evaluatedString) { match ->
                val key = match.groupValues[1].lowercase().trim()
                if (map.containsKey(key)) {
                    matchedAny = true
                    map[key]!!
                } else if (key.startsWith("webhook_param")) {
                    matchedAny = true
                    "42"
                } else if (key.startsWith("random")) {
                    matchedAny = true
                    "73"
                } else {
                    isLiveEvaluated = false
                    match.value
                }
            }

            if (matchedAny) {
                return Pair(evaluatedString, isLiveEvaluated)
            }
        }

        // 2. Single Atomic Token Evaluations
        if (lower.contains("date_month_short") || (lower.contains("date") && lower.contains("year")) || lower.contains("jan")) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return Pair(sdf.format(now), true)
        }
        if (lower.contains("day_of_week") || lower.contains("day_name")) {
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            return Pair(sdf.format(now), true)
        }
        if (lower.contains("date_iso") || lower.contains("yyyy-mm-dd")) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return Pair(sdf.format(now), true)
        }
        if (lower.contains("hour_24") || (lower.contains("hour") && lower.contains("minute"))) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return Pair(sdf.format(now), true)
        }
        if (lower.contains("hour_12") || lower.contains("am_pm")) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return Pair(sdf.format(now), true)
        }
        if (lower.contains("battery")) {
            if (context != null) {
                try {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    if (level >= 0 && scale > 0) {
                        val pct = (level * 100) / scale
                        return Pair("$pct%", true)
                    }
                } catch (_: Exception) {}
            }
            return Pair("85%", false)
        }
        if (lower.contains("device_model") || lower.contains("model")) {
            val model = Build.MODEL
            return if (!model.isNullOrBlank()) Pair(model, true) else Pair("Pixel 8 Pro", false)
        }
        if (lower.contains("device_manufacturer") || lower.contains("manufacturer")) {
            val mfg = Build.MANUFACTURER
            return if (!mfg.isNullOrBlank()) Pair(mfg, true) else Pair("Google", false)
        }
        if (lower.contains("android_version")) {
            val v = Build.VERSION.RELEASE
            return if (!v.isNullOrBlank()) Pair("Android $v", true) else Pair("Android 15", false)
        }
        if (lower.contains("clipboard")) {
            if (context != null) {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip = clipboard?.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val text = clip.getItemAt(0).text?.toString()
                        if (!text.isNullOrBlank()) {
                            val cleanText = if (text.length > 25) text.take(22) + "..." else text
                            return Pair("\"$cleanText\"", true)
                        }
                    }
                } catch (_: Exception) {}
            }
            return Pair("\"https://macrodroid.com\"", false)
        }
        if (lower.contains("wifi") || lower.contains("ssid")) {
            return Pair("Studio_5GHz", false)
        }
        if (lower.contains("call_name") || lower.contains("caller")) {
            return Pair("Austin Grindy", false)
        }
        if (lower.contains("sms_body")) {
            return Pair("\"Your verification code is 48291\"", false)
        }
        if (lower.contains("not_title") || lower.contains("not_text")) {
            return Pair("Slack: Project sprint review at 10 AM", false)
        }
        if (lower.contains("lat") && lower.contains("long")) {
            return Pair("37.7749, -122.4194", false)
        }

        return Pair("Live Evaluated Output", false)
    }

    /**
     * Complete evaluation packaging for UI presentation.
     */
    fun getEvaluatedInfo(
        rawName: String,
        tags: String = "",
        bracketStyle: BracketStyle = BracketStyle.CURLY,
        context: Context? = null
    ): EvaluatedTokenInfo {
        val formatted = bracketStyle.formatTemplate(rawName)
        val label = deriveLabel(rawName, tags)
        val (sampleValue, isLive) = evaluateLiveSample(rawName, context)
        val subTokens = extractSubTokens(rawName)

        return EvaluatedTokenInfo(
            rawName = rawName,
            formattedName = formatted,
            label = label,
            liveValue = sampleValue,
            isLiveEvaluated = isLive,
            subTokens = subTokens
        )
    }
}
