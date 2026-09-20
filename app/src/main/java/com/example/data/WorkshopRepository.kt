package com.example.data

import com.example.data.dao.MacroDao
import com.example.data.dao.PresetDao
import com.example.data.dao.WebhookDao
import com.example.data.models.Macro
import com.example.data.models.MacroPresetCrossRef
import com.example.data.models.MacroStatus
import com.example.data.models.MacroWebhookCrossRef
import com.example.data.models.MacroWithRelations
import com.example.data.models.Preset
import com.example.data.models.PresetWithMacros
import com.example.data.models.TriggerType
import com.example.data.models.Webhook
import com.example.data.models.WebhookDirection
import com.example.data.models.WebhookLog
import com.example.data.models.WebhookWithRelations
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class WorkshopRepository(
    private val macroDao: MacroDao,
    private val presetDao: PresetDao,
    private val webhookDao: WebhookDao
) {
    // Macro flows & operations
    val allMacros: Flow<List<Macro>> = macroDao.getAllMacros()

    val workbenchMacros: Flow<List<Macro>> = macroDao.getMacrosByStatuses(
        listOf(MacroStatus.IDEA, MacroStatus.DRAFT)
    )

    val archiveMacros: Flow<List<Macro>> = macroDao.getMacrosByStatuses(
        listOf(MacroStatus.BUILT, MacroStatus.DEPLOYED)
    )

    fun getMacroWithRelations(id: Long): Flow<MacroWithRelations?> =
        macroDao.getMacroWithRelations(id)

    fun getMacroById(id: Long): Flow<Macro?> =
        macroDao.getMacroById(id)

    suspend fun saveMacro(
        macro: Macro,
        presetIds: List<Long>,
        webhookIds: List<Long>
    ): Long = macroDao.saveMacroWithRelations(macro, presetIds, webhookIds)

    suspend fun updateMacro(macro: Macro) {
        macroDao.updateMacro(macro.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateMacroStatus(id: Long, newStatus: MacroStatus) {
        val macro = macroDao.getMacroByIdDirect(id) ?: return
        macroDao.updateMacro(macro.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMacro(id: Long) {
        macroDao.deleteMacroById(id)
    }

    // Preset flows & operations
    val allPresets: Flow<List<Preset>> = presetDao.getAllPresets()

    fun getPresetWithMacros(id: Long): Flow<PresetWithMacros?> =
        presetDao.getPresetWithMacros(id)

    suspend fun insertPreset(preset: Preset): Long =
        presetDao.insertPreset(preset)

    suspend fun updatePreset(preset: Preset) =
        presetDao.updatePreset(preset)

    suspend fun deletePreset(id: Long) =
        presetDao.deletePresetById(id)

    // Webhook flows & operations
    val allWebhooks: Flow<List<Webhook>> = webhookDao.getAllWebhooks()

    fun getWebhookWithRelations(id: Long): Flow<WebhookWithRelations?> =
        webhookDao.getWebhookWithRelations(id)

    suspend fun insertWebhook(webhook: Webhook): Long =
        webhookDao.insertWebhook(webhook)

    suspend fun updateWebhook(webhook: Webhook) =
        webhookDao.updateWebhook(webhook)

    suspend fun setWebhookEnabled(id: Long, enabled: Boolean) =
        webhookDao.setWebhookEnabled(id, enabled)

    suspend fun deleteWebhook(id: Long) =
        webhookDao.deleteWebhookById(id)

    suspend fun addWebhookLog(webhookId: Long, payload: String?, statusCode: Int? = 200, note: String? = null): Long {
        return webhookDao.insertWebhookLog(
            WebhookLog(
                webhookId = webhookId,
                timestamp = System.currentTimeMillis(),
                payload = payload,
                statusCode = statusCode,
                note = note
            )
        )
    }

    suspend fun clearWebhookLogs(webhookId: Long) {
        webhookDao.clearLogsForWebhook(webhookId)
    }
}
