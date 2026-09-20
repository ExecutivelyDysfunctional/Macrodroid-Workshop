package com.example.data.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MacroWithRelations(
    @Embedded
    val macro: Macro,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MacroPresetCrossRef::class,
            parentColumn = "macroId",
            entityColumn = "presetId"
        )
    )
    val presets: List<Preset> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MacroWebhookCrossRef::class,
            parentColumn = "macroId",
            entityColumn = "webhookId"
        )
    )
    val webhooks: List<Webhook> = emptyList()
)

data class PresetWithMacros(
    @Embedded
    val preset: Preset,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MacroPresetCrossRef::class,
            parentColumn = "presetId",
            entityColumn = "macroId"
        )
    )
    val macros: List<Macro> = emptyList()
)

data class WebhookWithRelations(
    @Embedded
    val webhook: Webhook,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MacroWebhookCrossRef::class,
            parentColumn = "webhookId",
            entityColumn = "macroId"
        )
    )
    val macros: List<Macro> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "webhookId"
    )
    val logs: List<WebhookLog> = emptyList()
)
