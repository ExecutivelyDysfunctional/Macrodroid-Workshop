package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webhooks")
data class Webhook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val endpointUrl: String,
    val direction: WebhookDirection = WebhookDirection.INCOMING,
    val enabled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
