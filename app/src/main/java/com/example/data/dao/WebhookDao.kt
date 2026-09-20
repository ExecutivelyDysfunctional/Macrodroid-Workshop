package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.models.Webhook
import com.example.data.models.WebhookLog
import com.example.data.models.WebhookWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookDao {

    @Query("SELECT * FROM webhooks ORDER BY id DESC")
    fun getAllWebhooks(): Flow<List<Webhook>>

    @Query("SELECT * FROM webhooks WHERE id = :id")
    fun getWebhookById(id: Long): Flow<Webhook?>

    @Query("SELECT * FROM webhooks WHERE id = :id")
    suspend fun getWebhookByIdDirect(id: Long): Webhook?

    @Transaction
    @Query("SELECT * FROM webhooks WHERE id = :id")
    fun getWebhookWithRelations(id: Long): Flow<WebhookWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhook(webhook: Webhook): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhooks(webhooks: List<Webhook>)

    @Update
    suspend fun updateWebhook(webhook: Webhook)

    @Query("UPDATE webhooks SET enabled = :enabled WHERE id = :id")
    suspend fun setWebhookEnabled(id: Long, enabled: Boolean)

    @Delete
    suspend fun deleteWebhook(webhook: Webhook)

    @Query("DELETE FROM webhooks WHERE id = :id")
    suspend fun deleteWebhookById(id: Long)

    // Log methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhookLog(log: WebhookLog): Long

    @Query("SELECT * FROM webhook_logs WHERE webhookId = :webhookId ORDER BY timestamp DESC")
    fun getLogsForWebhook(webhookId: Long): Flow<List<WebhookLog>>

    @Query("DELETE FROM webhook_logs WHERE webhookId = :webhookId")
    suspend fun clearLogsForWebhook(webhookId: Long)
}
