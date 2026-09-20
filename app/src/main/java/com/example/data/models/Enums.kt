package com.example.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class MacroStatus(
    val displayName: String,
    val dotColor: Color,
    val icon: ImageVector
) {
    IDEA("Idea", Color(0xFF94A3B8), Icons.Default.Lightbulb),
    DRAFT("Draft", Color(0xFFF59E0B), Icons.Default.EditNote),
    BUILT("Built", Color(0xFF3B82F6), Icons.Default.Construction),
    DEPLOYED("Deployed", Color(0xFF10B981), Icons.Default.RocketLaunch);

    val isWorkbench: Boolean get() = this == IDEA || this == DRAFT
    val isArchive: Boolean get() = this == BUILT || this == DEPLOYED
}

enum class TriggerType(
    val displayName: String,
    val icon: ImageVector
) {
    EVENT("Event", Icons.Default.FlashOn),
    TIME_ALARM("Time / Alarm", Icons.Default.Alarm),
    LOCATION("Location", Icons.Default.LocationOn),
    NOTIFICATION("Notification", Icons.Default.Notifications),
    MANUAL("Manual", Icons.Default.TouchApp),
    CONNECTIVITY("Connectivity", Icons.Default.Wifi),
    CALL_SMS("Call / SMS", Icons.Default.Call),
    BATTERY_POWER("Battery / Power", Icons.Default.BatteryChargingFull),
    WEBHOOK("Webhook", Icons.Default.Http),
    OTHER("Other", Icons.Default.Sensors)
}

enum class WebhookDirection(
    val displayName: String
) {
    INCOMING("Incoming"),
    OUTGOING("Outgoing")
}
