package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WorkshopRepository
import com.example.data.models.Macro
import com.example.data.models.MacroStatus
import com.example.data.models.MacroWithRelations
import com.example.data.models.Preset
import com.example.data.models.PresetWithMacros
import com.example.data.models.TriggerType
import com.example.data.models.Webhook
import com.example.data.models.WebhookDirection
import com.example.data.models.WebhookLog
import com.example.data.models.WebhookWithRelations
import com.example.util.MacroDroidParser
import com.example.util.ParsedMacroResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkshopViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = WorkshopRepository(
        database.macroDao(),
        database.presetDao(),
        database.webhookDao()
    )

    // Filter states
    val workbenchSearchQuery = MutableStateFlow("")
    val workbenchStatusFilter = MutableStateFlow<MacroStatus?>(null) // null = all active (Idea + Draft)
    val workbenchSelectedTag = MutableStateFlow<String?>(null)

    val archiveSearchQuery = MutableStateFlow("")
    val archiveStatusFilter = MutableStateFlow<MacroStatus?>(null) // null = all archive (Built + Deployed)
    val archiveSelectedTag = MutableStateFlow<String?>(null)

    val presetSearchQuery = MutableStateFlow("")
    val presetSelectedTag = MutableStateFlow<String?>(null)
    val bracketStyle = MutableStateFlow(com.example.util.BracketStyle.CURLY)

    fun toggleBracketStyle() {
        bracketStyle.value = if (bracketStyle.value == com.example.util.BracketStyle.CURLY) {
            com.example.util.BracketStyle.SQUARE
        } else {
            com.example.util.BracketStyle.CURLY
        }
    }

    fun setBracketStyle(style: com.example.util.BracketStyle) {
        bracketStyle.value = style
    }

    val webhookSearchQuery = MutableStateFlow("")

    // Raw sources from Room
    val allPresets: StateFlow<List<Preset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWebhooks: StateFlow<List<Webhook>> = repository.allWebhooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Workbench Macros (Idea & Draft)
    val workbenchMacros: StateFlow<List<Macro>> = combine(
        repository.workbenchMacros,
        workbenchSearchQuery,
        workbenchStatusFilter,
        workbenchSelectedTag
    ) { macros, query, status, tag ->
        macros.filter { macro ->
            val matchesQuery = query.isBlank() ||
                macro.name.contains(query, ignoreCase = true) ||
                macro.description.contains(query, ignoreCase = true) ||
                macro.notes.contains(query, ignoreCase = true) ||
                macro.blockingIssue?.contains(query, ignoreCase = true) == true ||
                macro.tags.contains(query, ignoreCase = true)

            val matchesStatus = status == null || macro.status == status
            val matchesTag = tag == null || macro.tagList.any { it.equals(tag, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Archive Macros (Built & Deployed)
    val archiveMacros: StateFlow<List<Macro>> = combine(
        repository.archiveMacros,
        archiveSearchQuery,
        archiveStatusFilter,
        archiveSelectedTag
    ) { macros, query, status, tag ->
        macros.filter { macro ->
            val matchesQuery = query.isBlank() ||
                macro.name.contains(query, ignoreCase = true) ||
                macro.description.contains(query, ignoreCase = true) ||
                macro.notes.contains(query, ignoreCase = true) ||
                macro.tags.contains(query, ignoreCase = true)

            val matchesStatus = status == null || macro.status == status
            val matchesTag = tag == null || macro.tagList.any { it.equals(tag, ignoreCase = true) }

            matchesQuery && matchesStatus && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Presets
    val filteredPresets: StateFlow<List<Preset>> = combine(
        repository.allPresets,
        presetSearchQuery,
        presetSelectedTag
    ) { presets, query, tag ->
        presets.filter { preset ->
            val matchesQuery = query.isBlank() ||
                preset.name.contains(query, ignoreCase = true) ||
                preset.content.contains(query, ignoreCase = true) ||
                preset.tags.contains(query, ignoreCase = true)

            val matchesTag = tag == null || preset.tagList.any { it.equals(tag, ignoreCase = true) }

            matchesQuery && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Webhooks
    val filteredWebhooks: StateFlow<List<Webhook>> = combine(
        repository.allWebhooks,
        webhookSearchQuery
    ) { webhooks, query ->
        if (query.isBlank()) webhooks
        else webhooks.filter {
            it.endpointUrl.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true) ||
            it.direction.displayName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected items for detail observation
    private val _selectedMacroId = MutableStateFlow<Long?>(null)
    val selectedMacroId: StateFlow<Long?> = _selectedMacroId.asStateFlow()

    val selectedMacroWithRelations: StateFlow<MacroWithRelations?> = _selectedMacroId
        .flatMapLatest { id ->
            if (id != null) repository.getMacroWithRelations(id)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedPresetId = MutableStateFlow<Long?>(null)
    val selectedPresetId: StateFlow<Long?> = _selectedPresetId.asStateFlow()

    val selectedPresetWithMacros: StateFlow<PresetWithMacros?> = _selectedPresetId
        .flatMapLatest { id ->
            if (id != null) repository.getPresetWithMacros(id)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedWebhookId = MutableStateFlow<Long?>(null)
    val selectedWebhookId: StateFlow<Long?> = _selectedWebhookId.asStateFlow()

    val selectedWebhookWithRelations: StateFlow<WebhookWithRelations?> = _selectedWebhookId
        .flatMapLatest { id ->
            if (id != null) repository.getWebhookWithRelations(id)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectMacro(id: Long?) {
        _selectedMacroId.value = id
    }

    fun selectPreset(id: Long?) {
        _selectedPresetId.value = id
    }

    fun selectWebhook(id: Long?) {
        _selectedWebhookId.value = id
    }

    // Macro operations
    fun saveMacro(
        macro: Macro,
        presetIds: List<Long>,
        webhookIds: List<Long>,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.saveMacro(macro, presetIds, webhookIds)
            onComplete(id)
        }
    }

    fun updateMacroStatus(id: Long, newStatus: MacroStatus) {
        viewModelScope.launch {
            repository.updateMacroStatus(id, newStatus)
        }
    }

    fun deleteMacro(id: Long) {
        viewModelScope.launch {
            repository.deleteMacro(id)
            if (_selectedMacroId.value == id) {
                _selectedMacroId.value = null
            }
        }
    }

    // Preset operations
    fun savePreset(preset: Preset, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (preset.id == 0L) {
                repository.insertPreset(preset)
            } else {
                repository.updatePreset(preset)
            }
            onComplete()
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
            if (_selectedPresetId.value == id) {
                _selectedPresetId.value = null
            }
        }
    }

    // Webhook operations
    fun saveWebhook(webhook: Webhook, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (webhook.id == 0L) {
                repository.insertWebhook(webhook)
            } else {
                repository.updateWebhook(webhook)
            }
            onComplete()
        }
    }

    fun toggleWebhookEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setWebhookEnabled(id, enabled)
        }
    }

    fun deleteWebhook(id: Long) {
        viewModelScope.launch {
            repository.deleteWebhook(id)
            if (_selectedWebhookId.value == id) {
                _selectedWebhookId.value = null
            }
        }
    }

    fun simulateWebhookTrigger(webhookId: Long, customPayload: String? = null) {
        viewModelScope.launch {
            val samplePayload = customPayload?.ifBlank { null }
                ?: "{\"event\":\"trigger_test\",\"timestamp\":${System.currentTimeMillis()},\"status\":\"ok\",\"source\":\"MacroDroid Workshop\"}"
            repository.addWebhookLog(
                webhookId = webhookId,
                payload = samplePayload,
                statusCode = 200,
                note = "Simulated manual trigger test"
            )
        }
    }

    fun clearWebhookLogs(webhookId: Long) {
        viewModelScope.launch {
            repository.clearWebhookLogs(webhookId)
        }
    }

    // Parser helper
    fun parseMacroDroidExport(raw: String): ParsedMacroResult {
        return MacroDroidParser.parse(raw)
    }
}
