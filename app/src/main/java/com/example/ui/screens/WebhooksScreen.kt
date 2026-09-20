package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Webhook
import com.example.data.models.WebhookDirection
import com.example.ui.WorkshopViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchBar
import com.example.ui.theme.BlockingWarningText
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchSurfaceElevated
import com.example.ui.theme.WorkbenchSurfaceVariant
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhooksScreen(
    viewModel: WorkshopViewModel,
    onMacroClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val webhooks by viewModel.filteredWebhooks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.webhookSearchQuery.collectAsStateWithLifecycle()
    val selectedWebhookWithRelations by viewModel.selectedWebhookWithRelations.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var webhookToEdit by remember { mutableStateOf<Webhook?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var webhookToDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = WorkbenchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    webhookToEdit = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_webhook_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Webhook"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Webhooks",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WorkbenchTextPrimary
                    )
                    Text(
                        text = "Incoming Triggers & Outgoing Dispatches",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary
                    )
                }

                Surface(
                    color = WorkbenchSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder)
                ) {
                    Text(
                        text = "${webhooks.count { it.enabled }} Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusDeployed,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.webhookSearchQuery.value = it },
                placeholder = "Search webhooks, URLs, notes...",
                modifier = Modifier.padding(horizontal = 16.dp),
                testTag = "webhook_search_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Webhook List or Empty State
            if (webhooks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Http,
                    title = if (searchQuery.isNotBlank()) "No webhooks match query" else "No Webhooks Staged",
                    description = if (searchQuery.isNotBlank()) {
                        "Try a different URL or search query."
                    } else {
                        "Stage and test incoming HTTP trigger endpoints and outgoing API integrations for your macros."
                    },
                    actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else "Add Webhook",
                    onAction = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.webhookSearchQuery.value = ""
                        } else {
                            webhookToEdit = null
                            showEditDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(webhooks, key = { it.id }) { webhook ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectWebhook(webhook.id)
                                    showDetailSheet = true
                                }
                                .testTag("webhook_card_${webhook.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Leading toggle switch as required in spec
                                Switch(
                                    checked = webhook.enabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.toggleWebhookEnabled(webhook.id, isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = WorkbenchTextPrimary,
                                        checkedTrackColor = StatusDeployed,
                                        uncheckedThumbColor = WorkbenchTextMuted,
                                        uncheckedTrackColor = WorkbenchSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("webhook_toggle_${webhook.id}")
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = if (webhook.direction == WebhookDirection.INCOMING)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = webhook.direction.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (webhook.direction == WebhookDirection.INCOMING)
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = if (webhook.enabled) "Active" else "Disabled",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (webhook.enabled) StatusDeployed else WorkbenchTextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = webhook.endpointUrl,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkbenchTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (webhook.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = webhook.notes,
                                            fontSize = 12.sp,
                                            color = WorkbenchTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val clip = ClipData.newPlainText("Webhook URL", webhook.endpointUrl)
                                        clipboardManager.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied endpoint URL", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy URL",
                                        tint = WorkbenchTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Webhook Detail, Linked Macros & Request Log History
    if (showDetailSheet && selectedWebhookWithRelations != null) {
        val rel = selectedWebhookWithRelations!!
        val webhook = rel.webhook
        val linkedMacros = rel.macros
        val logs = rel.logs

        val sdf = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())

        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                viewModel.selectWebhook(null)
            },
            containerColor = WorkbenchSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (webhook.direction == WebhookDirection.INCOMING)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${webhook.direction.displayName} Webhook",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (webhook.direction == WebhookDirection.INCOMING)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clip = ClipData.newPlainText("Webhook URL", webhook.endpointUrl)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied URL", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WorkbenchTextPrimary)
                        }

                        IconButton(
                            onClick = {
                                webhookToEdit = webhook
                                showDetailSheet = false
                                showEditDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WorkbenchTextSecondary)
                        }

                        IconButton(
                            onClick = {
                                webhookToDeleteId = webhook.id
                                showDeleteConfirmDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BlockingWarningText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Endpoint URL & Enabled State
                Surface(
                    color = WorkbenchSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ENDPOINT URL",
                            style = MaterialTheme.typography.labelSmall,
                            color = WorkbenchTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = webhook.endpointUrl,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (webhook.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NOTES",
                                style = MaterialTheme.typography.labelSmall,
                                color = WorkbenchTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = webhook.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = WorkbenchTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trigger / Simulation action button
                Button(
                    onClick = {
                        viewModel.simulateWebhookTrigger(webhook.id)
                        Toast.makeText(context, "Logged test trigger event", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (webhook.direction == WebhookDirection.INCOMING) "Simulate Incoming Payload" else "Test Fire Outgoing Request",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Linked Macros
                Text(
                    text = "LINKED MACROS (${linkedMacros.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = WorkbenchTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (linkedMacros.isEmpty()) {
                    Text(
                        text = "No macros currently linked to this webhook. Link in Macro Detail -> Linked Webhooks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        linkedMacros.forEach { macro ->
                            Surface(
                                color = WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDetailSheet = false
                                        viewModel.selectWebhook(null)
                                        onMacroClick(macro.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = macro.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WorkbenchTextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = macro.status.displayName,
                                            color = macro.status.dotColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View Macro",
                                        tint = WorkbenchTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Append-only Request Log History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REQUEST LOG HISTORY (${logs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkbenchTextMuted
                    )

                    if (logs.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearWebhookLogs(webhook.id) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = WorkbenchTextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 11.sp, color = WorkbenchTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (logs.isEmpty()) {
                    Text(
                        text = "No logs recorded yet. Tap the test button above to simulate a request.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        logs.forEach { log ->
                            Surface(
                                color = WorkbenchSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sdf.format(Date(log.timestamp)),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = WorkbenchTextMuted
                                        )
                                        Text(
                                            text = "HTTP ${log.statusCode ?: 200}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = StatusDeployed
                                        )
                                    }

                                    if (!log.payload.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = log.payload,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = WorkbenchTextPrimary
                                        )
                                    }

                                    if (!log.note.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = log.note,
                                            fontSize = 11.sp,
                                            color = WorkbenchTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add / Edit Webhook Dialog
    if (showEditDialog) {
        var url by remember(webhookToEdit) { mutableStateOf(webhookToEdit?.endpointUrl ?: "") }
        var dir by remember(webhookToEdit) { mutableStateOf(webhookToEdit?.direction ?: WebhookDirection.INCOMING) }
        var notes by remember(webhookToEdit) { mutableStateOf(webhookToEdit?.notes ?: "") }
        var enabled by remember(webhookToEdit) { mutableStateOf(webhookToEdit?.enabled ?: true) }
        var dirDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (webhookToEdit == null) "New Webhook" else "Edit Webhook",
                    color = WorkbenchTextPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Endpoint URL", color = WorkbenchTextMuted) },
                        placeholder = { Text("https://trigger.macrodroid.com/...", color = WorkbenchTextMuted) },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Direction Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dirDropdownExpanded,
                        onExpandedChange = { dirDropdownExpanded = !dirDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = dir.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Direction", color = WorkbenchTextMuted) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dirDropdownExpanded) },
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
                            expanded = dirDropdownExpanded,
                            onDismissRequest = { dirDropdownExpanded = false },
                            modifier = Modifier.background(WorkbenchSurfaceElevated)
                        ) {
                            WebhookDirection.values().forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.displayName, color = WorkbenchTextPrimary) },
                                    onClick = {
                                        dir = d
                                        dirDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Purpose", color = WorkbenchTextMuted) },
                        placeholder = { Text("e.g. Home Assistant trigger for arrival", color = WorkbenchTextMuted) },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enabled by default",
                            color = WorkbenchTextPrimary,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WorkbenchTextPrimary,
                                checkedTrackColor = StatusDeployed
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (url.isBlank()) {
                            Toast.makeText(context, "Please enter an endpoint URL", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val webhook = Webhook(
                            id = webhookToEdit?.id ?: 0L,
                            endpointUrl = url.trim(),
                            direction = dir,
                            enabled = enabled,
                            notes = notes.trim(),
                            createdAt = webhookToEdit?.createdAt ?: System.currentTimeMillis()
                        )

                        viewModel.saveWebhook(webhook) {
                            showEditDialog = false
                            Toast.makeText(context, "Webhook saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Delete confirmation
    if (showDeleteConfirmDialog && webhookToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Webhook?", color = WorkbenchTextPrimary) },
            text = { Text("Are you sure you want to remove this webhook configuration?", color = WorkbenchTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteWebhook(webhookToDeleteId!!)
                        showDetailSheet = false
                    }
                ) {
                    Text("Delete", color = BlockingWarningText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
