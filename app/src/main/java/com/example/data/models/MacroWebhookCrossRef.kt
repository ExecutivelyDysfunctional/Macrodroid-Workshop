package com.example.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "macro_webhook_cross_ref",
    primaryKeys = ["macroId", "webhookId"],
    foreignKeys = [
        ForeignKey(
            entity = Macro::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Webhook::class,
            parentColumns = ["id"],
            childColumns = ["webhookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("macroId"),
        Index("webhookId")
    ]
)
data class MacroWebhookCrossRef(
    val macroId: Long,
    val webhookId: Long
)
