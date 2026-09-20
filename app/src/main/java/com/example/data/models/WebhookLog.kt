package com.example.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "webhook_logs",
    foreignKeys = [
        ForeignKey(
            entity = Webhook::class,
            parentColumns = ["id"],
            childColumns = ["webhookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("webhookId")]
)
data class WebhookLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val webhookId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String? = null,
    val statusCode: Int? = 200,
    val note: String? = null
)
