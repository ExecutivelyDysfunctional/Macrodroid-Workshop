package com.example.data

import androidx.room.TypeConverter
import com.example.data.models.MacroStatus
import com.example.data.models.TriggerType
import com.example.data.models.WebhookDirection

class Converters {
    @TypeConverter
    fun fromMacroStatus(status: MacroStatus?): String? = status?.name

    @TypeConverter
    fun toMacroStatus(value: String?): MacroStatus =
        value?.let { runCatching { MacroStatus.valueOf(it) }.getOrNull() } ?: MacroStatus.IDEA

    @TypeConverter
    fun fromTriggerType(triggerType: TriggerType?): String? = triggerType?.name

    @TypeConverter
    fun toTriggerType(value: String?): TriggerType =
        value?.let { runCatching { TriggerType.valueOf(it) }.getOrNull() } ?: TriggerType.EVENT

    @TypeConverter
    fun fromWebhookDirection(direction: WebhookDirection?): String? = direction?.name

    @TypeConverter
    fun toWebhookDirection(value: String?): WebhookDirection =
        value?.let { runCatching { WebhookDirection.valueOf(it) }.getOrNull() } ?: WebhookDirection.INCOMING
}
