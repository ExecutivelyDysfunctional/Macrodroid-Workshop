package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MacroDao
import com.example.data.dao.PresetDao
import com.example.data.dao.WebhookDao
import com.example.data.models.Macro
import com.example.data.models.MacroPresetCrossRef
import com.example.data.models.MacroStatus
import com.example.data.models.MacroWebhookCrossRef
import com.example.data.models.Preset
import com.example.data.models.TriggerType
import com.example.data.models.Webhook
import com.example.data.models.WebhookDirection
import com.example.data.models.WebhookLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Macro::class,
        Preset::class,
        Webhook::class,
        WebhookLog::class,
        MacroWebhookCrossRef::class,
        MacroPresetCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun macroDao(): MacroDao
    abstract fun presetDao(): PresetDao
    abstract fun webhookDao(): WebhookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "macrodroid_workshop.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val presetDao = database.presetDao()
            val webhookDao = database.webhookDao()
            val macroDao = database.macroDao()

            // 1. Initial Magic Text Presets
            val presets = listOf(
                Preset(
                    name = "[date_month_short] [date_day], [date_year]",
                    content = "Outputs formatted current calendar date (e.g., Jan 2, 1975 or live date). Standard timestamp for logs, SMS, and announcements.",
                    tags = "Date, Calendar, Format"
                ),
                Preset(
                    name = "[hour_24]:[minute]",
                    content = "Outputs current time in 24-hour format e.g. 14:05. Great for logging timestamps or filenames.",
                    tags = "Time, Date, Format"
                ),
                Preset(
                    name = "[battery]",
                    content = "Current battery percentage number (0-100). Useful in low battery speech alerts and auto-power save.",
                    tags = "Battery, Hardware, System"
                ),
                Preset(
                    name = "[wifi_ssid]",
                    content = "Name of the currently connected Wi-Fi network or '<unknown>' if disconnected. Used for home/work context switching.",
                    tags = "Connectivity, Wi-Fi, Location"
                ),
                Preset(
                    name = "[device_model]",
                    content = "Device hardware model name e.g. Pixel 8 Pro. Useful for multi-device sync and logs.",
                    tags = "Device, Hardware, System"
                ),
                Preset(
                    name = "[call_name]",
                    content = "Contact name of current caller or number if unsaved. Used for auto-reply SMS and speak caller announcement.",
                    tags = "Phone, Call, Contact"
                ),
                Preset(
                    name = "[sms_body]",
                    content = "Full text body of the received SMS notification. Perfect for 2FA extraction or forwarding.",
                    tags = "SMS, Messaging, Regex"
                ),
                Preset(
                    name = "[not_title] / [not_text]",
                    content = "Notification title and body from the target application. Essential for WhatsApp / Telegram notification bridges.",
                    tags = "Notification, Parser"
                ),
                Preset(
                    name = "[webhook_param=id]",
                    content = "Extract query parameter 'id' from incoming HTTP webhook request e.g. /trigger?id=value",
                    tags = "Webhook, HTTP, Params"
                ),
                Preset(
                    name = "[clipboard]",
                    content = "Current text content from the Android system clipboard. Handy for quick note saving or URL dispatch.",
                    tags = "Clipboard, System, Utility"
                ),
                Preset(
                    name = "[day_of_week]",
                    content = "Current day of week name (e.g. Sunday, Monday). Great for weekday vs weekend scheduling logic.",
                    tags = "Date, Time, Schedule"
                ),
                Preset(
                    name = "[device_uptime_hours]",
                    content = "Device uptime in hours. Used for scheduled weekly reboot reminders.",
                    tags = "System, Maintenance"
                )
            )
            presetDao.insertPresets(presets)

            // 2. Initial Webhooks
            val webhook1 = Webhook(
                endpointUrl = "https://trigger.macrodroid.com/v1/user-id-abc1234/home_arrive",
                direction = WebhookDirection.INCOMING,
                enabled = true,
                notes = "Fired by Home Assistant when entering geofence perimeter"
            )
            val webhook2 = Webhook(
                endpointUrl = "https://api.telegram.org/botTOKEN/sendMessage",
                direction = WebhookDirection.OUTGOING,
                enabled = true,
                notes = "Push alert notifications to personal Telegram channel"
            )
            val webhook3 = Webhook(
                endpointUrl = "https://ntfy.sh/macrodroid-workshop-staging",
                direction = WebhookDirection.OUTGOING,
                enabled = false,
                notes = "Staging notification dispatcher for testing remote device triggers"
            )

            val w1Id = webhookDao.insertWebhook(webhook1)
            val w2Id = webhookDao.insertWebhook(webhook2)
            webhookDao.insertWebhook(webhook3)

            // Seed sample log
            webhookDao.insertWebhookLog(
                WebhookLog(
                    webhookId = w1Id,
                    timestamp = System.currentTimeMillis() - 3600000,
                    payload = "{\"event\":\"geofence_enter\",\"location\":\"home_zone\",\"accuracy\":14.2}",
                    statusCode = 200,
                    note = "Test simulation response OK"
                )
            )

            // 3. Initial Macros (Workbench & Archive)
            val m1 = Macro(
                name = "Smart Charging 80% Bell & TTS",
                description = "Speaks battery percentage and sounds chime when battery level reaches 80% on fast charger.",
                status = MacroStatus.DRAFT,
                tags = "Battery, Audio, Power",
                triggerType = TriggerType.BATTERY_POWER,
                blockingIssue = "Need volume override when Do Not Disturb is active",
                notes = "Trigger: Battery Level >= 80% (while Power Connected)\nAction 1: Set Media Volume to 60%\nAction 2: Speak Text 'Battery reached [battery]%'\nAction 3: Play notification sound",
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis() - 10000000
            )

            val m2 = Macro(
                name = "Car Bluetooth Auto Navigation & Spotify",
                description = "When connected to car head unit Bluetooth, disable Wi-Fi, launch Spotify and start Google Maps navigation.",
                status = MacroStatus.IDEA,
                tags = "Car, Bluetooth, Audio, Maps",
                triggerType = TriggerType.CONNECTIVITY,
                blockingIssue = "Check if Spotify can auto-resume playlist via intent without unlocking screen",
                notes = "Trigger: Bluetooth Device Connected [Car Audio]\nAction: Wait 2s -> Launch Spotify -> Send Play Media Intent",
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis() - 40000000
            )

            val m3 = Macro(
                name = "Home Assistant Geofence Welcome Gate",
                description = "Triggered by incoming webhook when entering home perimeter. Toggles Wi-Fi on and turns on hallway lights.",
                status = MacroStatus.DRAFT,
                tags = "Home Automation, Webhook, Wi-Fi",
                triggerType = TriggerType.WEBHOOK,
                blockingIssue = null,
                notes = "Uses webhook /home_arrive with device location coordinates payload",
                createdAt = System.currentTimeMillis() - 50000000,
                updatedAt = System.currentTimeMillis() - 5000000
            )

            val m4 = Macro(
                name = "Night Silent Mode & Screen Dimmer",
                description = "At 23:00, turn on Do Not Disturb, set brightness to minimum, and disable vibrations.",
                status = MacroStatus.BUILT,
                tags = "Night, Schedule, Display",
                triggerType = TriggerType.TIME_ALARM,
                blockingIssue = null,
                notes = "Tested and working on Pixel 8 Pro. Deployed to production phone profile.",
                createdAt = System.currentTimeMillis() - 600000000,
                updatedAt = System.currentTimeMillis() - 300000000
            )

            val m5 = Macro(
                name = "Emergency SMS Location Auto-Reply",
                description = "Replies with current GPS coordinates and battery level when receiving specific keyword from trusted contact.",
                status = MacroStatus.DEPLOYED,
                tags = "Security, Location, SMS",
                triggerType = TriggerType.CALL_SMS,
                blockingIssue = null,
                notes = "Trigger: SMS Received from [Trusted Contact] with text 'WhereAreYou'\nAction: Send SMS with '[battery]% battery at https://maps.google.com/?q=[lat],[long]'",
                createdAt = System.currentTimeMillis() - 1000000000,
                updatedAt = System.currentTimeMillis() - 200000000
            )

            val m1Id = macroDao.insertMacro(m1)
            val m2Id = macroDao.insertMacro(m2)
            val m3Id = macroDao.insertMacro(m3)
            macroDao.insertMacro(m4)
            macroDao.insertMacro(m5)

            // Link cross-refs
            macroDao.insertMacroPresetCrossRef(MacroPresetCrossRef(m1Id, 2)) // [battery]
            macroDao.insertMacroPresetCrossRef(MacroPresetCrossRef(m2Id, 3)) // [wifi_ssid]
            macroDao.insertMacroWebhookCrossRef(MacroWebhookCrossRef(m3Id, w1Id))
            macroDao.insertMacroPresetCrossRef(MacroPresetCrossRef(m3Id, 7)) // [webhook_param]
        }
    }
}
