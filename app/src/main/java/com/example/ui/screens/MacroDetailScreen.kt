package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Macro
import com.example.data.models.MacroStatus
import com.example.data.models.Preset
import com.example.data.models.TriggerType
import com.example.data.models.Webhook
import com.example.data.models.WebhookDirection
import com.example.ui.WorkshopViewModel
import com.example.ui.components.BlockingIssueAlert
import com.example.ui.components.StatusBadge
import com.example.ui.components.TriggerBadge
import com.example.ui.theme.BlockingWarningBg
import com.example.ui.theme.BlockingWarningBorder
import com.example.ui.theme.BlockingWarningText
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchBorderSubtle
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchSurfaceElevated
import com.example.ui.theme.WorkbenchSurfaceVariant
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import com.example.util.BracketStyle
import com.example.util.MagicTextEvaluator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MacroDetailScreen(
    macroId: Long,
    viewModel: WorkshopViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    LaunchedEffect(macroId) {
        viewModel.selectMacro(macroId)
    }

    val macroWithRelations by viewModel.selectedMacroWithRelations.collectAsStateWithLifecycle()
    val allPresets by viewModel.allPresets.collectAsStateWithLifecycle()
    val allWebhooks by viewModel.allWebhooks.collectAsStateWithLifecycle()
    val bracketStyle by viewModel.bracketStyle.collectAsStateWithLifecycle()

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(MacroStatus.IDEA) }
    var triggerType by remember { mutableStateOf(TriggerType.EVENT) }
    var tagsInput by remember { mutableStateOf("") }
    var blockingIssue by remember { mutableStateOf("") }
    var exportDataInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var parseFeedback by remember { mutableStateOf<String?>(null) }

    val selectedPresetIds = remember { mutableStateListOf<Long>() }
    val selectedWebhookIds = remember { mutableStateListOf<Long>() }

    var isInitialized by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPresetPickerSheet by remember { mutableStateOf(false) }
    var showWebhookPickerSheet by remember { mutableStateOf(false) }
    var triggerDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Load initial values from database
    LaunchedEffect(macroWithRelations) {
        macroWithRelations?.let { rel ->
            if (!isInitialized) {
                val m = rel.macro
                name = m.name
                description = m.description
                status = m.status
                triggerType = m.triggerType
                tagsInput = m.tags
                blockingIssue = m.blockingIssue ?: ""
                exportDataInput = m.exportData ?: ""
                notes = m.notes

                selectedPresetIds.clear()
                selectedPresetIds.addAll(rel.presets.map { it.id })

                selectedWebhookIds.clear()
                selectedWebhookIds.addAll(rel.webhooks.map { it.id })

                isInitialized = true
            }
        }
    }

    // Function to run auto-parser
    fun performAutoParse(rawText: String) {
        val result = viewModel.parseMacroDroidExport(rawText)
        if (!result.name.isNullOrBlank()) {
            name = result.name
        }
        if (!result.description.isNullOrBlank()) {
            description = result.description
        }
        if (result.triggerType != null) {
            triggerType = result.triggerType
        }
        if (!result.notes.isNullOrBlank()) {
            notes = if (notes.isBlank()) result.notes else "$notes\n\n${result.notes}"
        }
        exportDataInput = result.exportData
        parseFeedback = "Detected: ${result.formatDescription}"
        Toast.makeText(context, "Parsed: ${result.formatDescription}", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = WorkbenchBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (macroId == 0L) "New Staged Macro" else "Macro Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = WorkbenchTextPrimary
                        )
                        if (macroWithRelations != null) {
                            val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                            Text(
                                text = "Updated ${sdf.format(Date(macroWithRelations!!.macro.updatedAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = WorkbenchTextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WorkbenchTextPrimary
                        )
                    }
                },
                actions = {
                    if (macroId != 0L) {
                        // Copy share summary
                        IconButton(
                            onClick = {
                                val summary = buildString {
                                    appendLine("Macro: $name")
                                    appendLine("Status: ${status.displayName}")
                                    appendLine("Trigger: ${triggerType.displayName}")
                                    if (description.isNotBlank()) appendLine("Description: $description")
                                    if (blockingIssue.isNotBlank()) appendLine("Blocking: $blockingIssue")
                                    if (notes.isNotBlank()) appendLine("Notes:\n$notes")
                                    if (exportDataInput.isNotBlank()) appendLine("Export Data:\n$exportDataInput")
                                }
                                val clip = ClipData.newPlainText("MacroDroid Workshop", summary)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied macro details to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Macro Summary",
                                tint = WorkbenchTextSecondary
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_macro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Macro",
                                tint = BlockingWarningText
                            )
                        }
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Please enter a macro name", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }

                            val macroToSave = Macro(
                                id = macroId,
                                name = name.trim(),
                                description = description.trim(),
                                status = status,
                                tags = tagsInput.trim(),
                                triggerType = triggerType,
                                blockingIssue = blockingIssue.trim().ifEmpty { null },
                                exportData = exportDataInput.trim().ifEmpty { null },
                                notes = notes.trim(),
                                createdAt = macroWithRelations?.macro?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )

                            viewModel.saveMacro(
                                macro = macroToSave,
                                presetIds = selectedPresetIds.toList(),
                                webhookIds = selectedWebhookIds.toList()
                            ) {
                                Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("save_macro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Macro",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WorkbenchSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Status Selector & Trigger Selector
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STAGE & TRIGGER",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkbenchTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status selection row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroStatus.values().forEach { st ->
                            val isSelected = status == st
                            Surface(
                                color = if (isSelected) st.dotColor.copy(alpha = 0.25f) else WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) st.dotColor else WorkbenchBorderSubtle
                                ),
                                modifier = Modifier.clickable { status = st }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(st.dotColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = st.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) WorkbenchTextPrimary else WorkbenchTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trigger Type Selector (Dropdown)
                    ExposedDropdownMenuBox(
                        expanded = triggerDropdownExpanded,
                        onExpandedChange = { triggerDropdownExpanded = !triggerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = triggerType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trigger Type", color = WorkbenchTextMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = triggerType.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = triggerDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = WorkbenchSurfaceVariant,
                                unfocusedContainerColor = WorkbenchSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = WorkbenchBorder,
                                focusedTextColor = WorkbenchTextPrimary,
                                unfocusedTextColor = WorkbenchTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = triggerDropdownExpanded,
                            onDismissRequest = { triggerDropdownExpanded = false },
                            modifier = Modifier.background(WorkbenchSurfaceElevated)
                        ) {
                            TriggerType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = type.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = type.displayName, color = WorkbenchTextPrimary)
                                        }
                                    },
                                    onClick = {
                                        triggerType = type
                                        triggerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Macro Name & Description
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AUTOMATION IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkbenchTextMuted
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Macro Name *", color = WorkbenchTextMuted) },
                        placeholder = { Text("e.g. Car Bluetooth Auto Navigation", color = WorkbenchTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurfaceVariant,
                            unfocusedContainerColor = WorkbenchSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_name_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Summary / Intent", color = WorkbenchTextMuted) },
                        placeholder = { Text("What should this automation accomplish?", color = WorkbenchTextMuted) },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurfaceVariant,
                            unfocusedContainerColor = WorkbenchSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_description_input")
                    )

                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Tags (comma-separated)", color = WorkbenchTextMuted) },
                        placeholder = { Text("Car, Audio, Wi-Fi, Security", color = WorkbenchTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurfaceVariant,
                            unfocusedContainerColor = WorkbenchSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_tags_input")
                    )
                }
            }

            // Blocking Issue Section (Stalling / What's next)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (blockingIssue.isNotBlank()) BlockingWarningBorder else WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (blockingIssue.isNotBlank()) BlockingWarningText else WorkbenchTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BLOCKING ISSUE / WHAT'S NEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (blockingIssue.isNotBlank()) BlockingWarningText else WorkbenchTextMuted
                        )
                    }

                    OutlinedTextField(
                        value = blockingIssue,
                        onValueChange = { blockingIssue = it },
                        placeholder = { Text("e.g. Need permission helper intent, or check Spotify URI format...", color = WorkbenchTextMuted) },
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (blockingIssue.isNotBlank()) BlockingWarningBg else WorkbenchSurfaceVariant,
                            unfocusedContainerColor = if (blockingIssue.isNotBlank()) BlockingWarningBg else WorkbenchSurfaceVariant,
                            focusedBorderColor = if (blockingIssue.isNotBlank()) BlockingWarningText else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (blockingIssue.isNotBlank()) BlockingWarningBorder else WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_blocking_issue_input")
                    )
                }
            }

            // Notes / Logic Blueprint
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NOTES & ACTION BLUEPRINT",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkbenchTextMuted
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = {
                            Text(
                                "Outline triggers, actions, constraints, or test notes here:\n- Trigger: Battery 80%\n- Action 1: Speak text '[battery]%'\n- Action 2: Play chime",
                                color = WorkbenchTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurfaceVariant,
                            unfocusedContainerColor = WorkbenchSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_notes_input")
                    )
                }
            }

            // Linked Presets (Magic Text Library)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LINKED MAGIC TEXT PRESETS (${selectedPresetIds.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = WorkbenchTextMuted
                        )

                        TextButton(
                            onClick = { showPresetPickerSheet = true },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manage",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val linkedPresets = allPresets.filter { it.id in selectedPresetIds }

                    if (linkedPresets.isEmpty()) {
                        Text(
                            text = "No magic text presets linked yet. Tap Manage to link reusable Magic Text tokens like {battery} or [battery].",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkbenchTextMuted
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            linkedPresets.forEach { preset ->
                                val formattedToken = bracketStyle.formatTemplate(preset.name)
                                Surface(
                                    color = WorkbenchSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                                    modifier = Modifier.clickable {
                                        val clip = ClipData.newPlainText("Magic Text", formattedToken)
                                        clipboardManager.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied $formattedToken", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = formattedToken,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Preset",
                                            modifier = Modifier.size(12.dp),
                                            tint = WorkbenchTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Linked Webhooks
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LINKED WEBHOOKS (${selectedWebhookIds.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = WorkbenchTextMuted
                        )

                        TextButton(
                            onClick = { showWebhookPickerSheet = true },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manage",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val linkedWebhooks = allWebhooks.filter { it.id in selectedWebhookIds }

                    if (linkedWebhooks.isEmpty()) {
                        Text(
                            text = "No webhooks linked. Tap Manage to link incoming trigger endpoints or outgoing alert webhooks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkbenchTextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            linkedWebhooks.forEach { webhook ->
                                Surface(
                                    color = WorkbenchSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = if (webhook.direction == WebhookDirection.INCOMING)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = webhook.direction.displayName,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (webhook.direction == WebhookDirection.INCOMING)
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        else MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (webhook.enabled) "Enabled" else "Disabled",
                                                    fontSize = 11.sp,
                                                    color = if (webhook.enabled) StatusDeployed else WorkbenchTextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = webhook.endpointUrl,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = WorkbenchTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val clip = ClipData.newPlainText("Webhook URL", webhook.endpointUrl)
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(context, "Copied Webhook URL", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy URL",
                                                tint = WorkbenchTextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Paste-to-Import MacroDroid Export Box (Auto-detection)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MACRODROID EXPORT & AUTO-IMPORT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Paste from clipboard button
                        TextButton(
                            onClick = {
                                val item = clipboardManager.primaryClip?.getItemAt(0)
                                val pasted = item?.text?.toString() ?: ""
                                if (pasted.isNotBlank()) {
                                    exportDataInput = pasted
                                    performAutoParse(pasted)
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste & Parse",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (parseFeedback != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = parseFeedback!!,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = exportDataInput,
                        onValueChange = { exportDataInput = it },
                        placeholder = {
                            Text(
                                "Paste MacroDroid XML, JSON, or text export snippet here. Tap 'Auto-Detect & Fill' to parse into fields.",
                                color = WorkbenchTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        minLines = 4,
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchSurfaceVariant,
                            unfocusedContainerColor = WorkbenchSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_export_data_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (exportDataInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("MacroDroid Export", exportDataInput)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Export copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Export", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { performAutoParse(exportDataInput) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Detect & Fill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Save CTA at bottom
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter a macro name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val macroToSave = Macro(
                        id = macroId,
                        name = name.trim(),
                        description = description.trim(),
                        status = status,
                        tags = tagsInput.trim(),
                        triggerType = triggerType,
                        blockingIssue = blockingIssue.trim().ifEmpty { null },
                        exportData = exportDataInput.trim().ifEmpty { null },
                        notes = notes.trim(),
                        createdAt = macroWithRelations?.macro?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    viewModel.saveMacro(
                        macro = macroToSave,
                        presetIds = selectedPresetIds.toList(),
                        webhookIds = selectedWebhookIds.toList()
                    ) {
                        Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_macro_bottom_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (macroId == 0L) "Stage Macro Idea" else "Save Macro Changes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Modal Sheet: Preset Picker
    if (showPresetPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetPickerSheet = false },
            containerColor = WorkbenchSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Link Magic Text Presets",
                    style = MaterialTheme.typography.titleLarge,
                    color = WorkbenchTextPrimary
                )
                Text(
                    text = "Select presets to reference in this automation",
                    style = MaterialTheme.typography.bodySmall,
                    color = WorkbenchTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (allPresets.isEmpty()) {
                    Text(
                        text = "No presets found. Add presets from the Presets tab first.",
                        color = WorkbenchTextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        allPresets.forEach { preset ->
                            val isSelected = preset.id in selectedPresetIds
                            val evaluated = MagicTextEvaluator.getEvaluatedInfo(
                                rawName = preset.name,
                                tags = preset.tags,
                                bracketStyle = bracketStyle,
                                context = context
                            )

                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else WorkbenchBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedPresetIds.remove(preset.id)
                                        else selectedPresetIds.add(preset.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${evaluated.label}:",
                                                fontWeight = FontWeight.Bold,
                                                color = WorkbenchTextPrimary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "(${evaluated.liveValue})",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = evaluated.formattedName,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp
                                        )

                                        Text(
                                            text = preset.content,
                                            color = WorkbenchTextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showPresetPickerSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done (${selectedPresetIds.size} Selected)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet: Webhook Picker
    if (showWebhookPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWebhookPickerSheet = false },
            containerColor = WorkbenchSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Link Webhooks",
                    style = MaterialTheme.typography.titleLarge,
                    color = WorkbenchTextPrimary
                )
                Text(
                    text = "Associate incoming triggers or outgoing dispatch endpoints",
                    style = MaterialTheme.typography.bodySmall,
                    color = WorkbenchTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (allWebhooks.isEmpty()) {
                    Text(
                        text = "No webhooks found. Add endpoints in the Webhooks tab first.",
                        color = WorkbenchTextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        allWebhooks.forEach { webhook ->
                            val isSelected = webhook.id in selectedWebhookIds
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else WorkbenchBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedWebhookIds.remove(webhook.id)
                                        else selectedWebhookIds.add(webhook.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (webhook.direction == WebhookDirection.INCOMING)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = webhook.direction.displayName,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (webhook.direction == WebhookDirection.INCOMING)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = webhook.endpointUrl,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = WorkbenchTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showWebhookPickerSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done (${selectedWebhookIds.size} Selected)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Macro?", color = WorkbenchTextPrimary) },
            text = { Text("Are you sure you want to remove '$name' from your workshop?", color = WorkbenchTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMacro(macroId)
                        onBack()
                    }
                ) {
                    Text("Delete", color = BlockingWarningText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
