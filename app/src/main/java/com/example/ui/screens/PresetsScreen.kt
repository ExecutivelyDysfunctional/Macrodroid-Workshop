package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Preset
import com.example.ui.WorkshopViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchBar
import com.example.ui.components.TagPill
import com.example.ui.theme.BlockingWarningText
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchBorderSubtle
import com.example.ui.theme.WorkbenchInset
import com.example.ui.theme.WorkbenchOnPrimary
import com.example.ui.theme.WorkbenchPrimary
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchSurfaceElevated
import com.example.ui.theme.WorkbenchSurfaceVariant
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import com.example.util.BracketStyle
import com.example.util.CombinationCategory
import com.example.util.MagicTextCategory
import com.example.util.MagicTextEvaluator
import kotlinx.coroutines.delay

enum class MagicTextTab {
    ALL_TOKENS,
    COMBINATIONS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresetsScreen(
    viewModel: WorkshopViewModel,
    onMacroClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val userPresets by viewModel.allPresets.collectAsStateWithLifecycle()
    val selectedPresetWithMacros by viewModel.selectedPresetWithMacros.collectAsStateWithLifecycle()
    val bracketStyle by viewModel.bracketStyle.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(MagicTextTab.ALL_TOKENS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTokenCategory by remember { mutableStateOf(MagicTextCategory.ALL) }
    var selectedComboCategory by remember { mutableStateOf(CombinationCategory.ALL) }

    var showEditDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<Preset?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var presetToDeleteId by remember { mutableStateOf<Long?>(null) }
    var recentlyCopiedToken by remember { mutableStateOf<String?>(null) }

    // Auto clear recent copy feedback badge after 2 seconds
    LaunchedEffect(recentlyCopiedToken) {
        if (recentlyCopiedToken != null) {
            delay(2000)
            recentlyCopiedToken = null
        }
    }

    fun copyToClipboard(textToCopy: String, label: String = "Magic Text") {
        val clip = ClipData.newPlainText(label, textToCopy)
        clipboardManager.setPrimaryClip(clip)
        recentlyCopiedToken = textToCopy
        Toast.makeText(context, "Copied $textToCopy", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = WorkbenchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    presetToEdit = null
                    showEditDialog = true
                },
                containerColor = WorkbenchPrimary,
                contentColor = WorkbenchOnPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_preset_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Magic Text Preset"
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
                        text = "Magic Text",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WorkbenchTextPrimary
                    )
                    Text(
                        text = "Variables & Formula Library",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary
                    )
                }

                // Settings icon for Bracket preference
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Bracket Settings",
                        tint = WorkbenchTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Segmented Primary Library Tabs: "All Magic Text" vs "Combinations"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllTokensActive = activeTab == MagicTextTab.ALL_TOKENS
                Surface(
                    color = if (isAllTokensActive) WorkbenchPrimary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isAllTokensActive) WorkbenchPrimary else WorkbenchBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = MagicTextTab.ALL_TOKENS }
                        .testTag("tab_all_magic_text")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = if (isAllTokensActive) WorkbenchOnPrimary else WorkbenchTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All Tokens (${MagicTextEvaluator.allMagicTextTokens.size})",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isAllTokensActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAllTokensActive) WorkbenchOnPrimary else WorkbenchTextSecondary
                        )
                    }
                }

                val isComboActive = activeTab == MagicTextTab.COMBINATIONS
                val totalCombos = MagicTextEvaluator.commonCombinations.size + userPresets.size
                Surface(
                    color = if (isComboActive) WorkbenchPrimary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isComboActive) WorkbenchPrimary else WorkbenchBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = MagicTextTab.COMBINATIONS }
                        .testTag("tab_combinations")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isComboActive) WorkbenchOnPrimary else WorkbenchTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Combinations ($totalCombos)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isComboActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isComboActive) WorkbenchOnPrimary else WorkbenchTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = if (activeTab == MagicTextTab.ALL_TOKENS)
                    "Search tokens (e.g. date, battery, wifi)..."
                else
                    "Search combinations (e.g. timestamp, full date)...",
                modifier = Modifier.padding(horizontal = 16.dp),
                testTag = "magic_text_search_input"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Pills: Active solid #5B8DEF with dark text, Inactive transparent with 1px border and muted text
            if (activeTab == MagicTextTab.ALL_TOKENS) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MagicTextCategory.values()) { cat ->
                        val isSelected = selectedTokenCategory == cat
                        Surface(
                            color = if (isSelected) WorkbenchPrimary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) WorkbenchPrimary else WorkbenchBorder
                            ),
                            modifier = Modifier.clickable { selectedTokenCategory = cat }
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CombinationCategory.values()) { cat ->
                        val isSelected = selectedComboCategory == cat
                        Surface(
                            color = if (isSelected) WorkbenchPrimary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) WorkbenchPrimary else WorkbenchBorder
                            ),
                            modifier = Modifier.clickable { selectedComboCategory = cat }
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB 1: ALL MAGIC TEXT TOKENS
            if (activeTab == MagicTextTab.ALL_TOKENS) {
                val filteredTokens = remember(searchQuery, selectedTokenCategory, bracketStyle) {
                    MagicTextEvaluator.allMagicTextTokens.filter { item ->
                        val matchesCategory = selectedTokenCategory == MagicTextCategory.ALL || item.category == selectedTokenCategory
                        val matchesSearch = searchQuery.isBlank() ||
                                item.token.contains(searchQuery, ignoreCase = true) ||
                                item.label.contains(searchQuery, ignoreCase = true) ||
                                item.description.contains(searchQuery, ignoreCase = true) ||
                                item.category.displayName.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                }

                if (filteredTokens.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Code,
                        title = "No Magic Text tokens found",
                        description = "Try selecting a different category or clearing your search term.",
                        actionLabel = "Clear Filter",
                        onAction = {
                            searchQuery = ""
                            selectedTokenCategory = MagicTextCategory.ALL
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTokens, key = { it.token }) { item ->
                            val formattedToken = bracketStyle.formatToken(item.token)
                            val (liveSample, isLive) = remember(item.token, bracketStyle) {
                                MagicTextEvaluator.evaluateLiveSample(item.token, context)
                            }
                            val isJustCopied = recentlyCopiedToken == formattedToken

                            // Card Hierarchy:
                            // 1. Token/macro name — 15sp, medium weight, primary text color
                            // 2. Category tag — 11sp, muted color, top-right corner, no background pill
                            // 3. Value chip — inset background, monospace, single copy icon on the right
                            // 4. Description — 12sp, secondary text color, below the chip
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { copyToClipboard(formattedToken, item.label) }
                                    .testTag("token_card_${item.token}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Row 1: Left = Token Name (15sp medium) + Live Sample; Right = Category Tag (11sp muted, NO background pill)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${item.label}:",
                                                fontSize = 15.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                fontWeight = FontWeight.Medium,
                                                color = WorkbenchTextPrimary
                                            )
                                            Text(
                                                text = "($liveSample ${if (isLive) "[using live data]" else "[sample]"})",
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                fontWeight = FontWeight.Normal,
                                                color = WorkbenchPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Category Tag: 11sp, muted color, top-right corner, no background pill (just text)
                                        Text(
                                            text = item.category.displayName,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = WorkbenchTextSecondary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Item 3: Value chip — inset background (#181818), monospace, single copy icon on right (no "Tap to copy" label)
                                    Surface(
                                        color = WorkbenchInset,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, WorkbenchBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { copyToClipboard(formattedToken, item.label) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formattedToken,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 14.sp,
                                                color = WorkbenchPrimary,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (isJustCopied) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Copied",
                                                    tint = StatusDeployed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = WorkbenchTextSecondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Item 4: Description — 12sp, secondary text color
                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = WorkbenchTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: COMBINATIONS & FORMULAS
            else {
                val filteredBuiltinCombos = remember(searchQuery, selectedComboCategory, bracketStyle) {
                    MagicTextEvaluator.commonCombinations.filter { combo ->
                        val matchesCategory = selectedComboCategory == CombinationCategory.ALL || combo.category == selectedComboCategory
                        val matchesSearch = searchQuery.isBlank() ||
                                combo.name.contains(searchQuery, ignoreCase = true) ||
                                combo.template.contains(searchQuery, ignoreCase = true) ||
                                combo.description.contains(searchQuery, ignoreCase = true) ||
                                combo.category.displayName.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                }

                val filteredUserPresets = remember(userPresets, searchQuery) {
                    userPresets.filter { preset ->
                        searchQuery.isBlank() ||
                                preset.name.contains(searchQuery, ignoreCase = true) ||
                                preset.content.contains(searchQuery, ignoreCase = true) ||
                                preset.tags.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredBuiltinCombos.isEmpty() && filteredUserPresets.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.AutoAwesome,
                        title = "No combinations found",
                        description = "Try a different search keyword or create your own custom formula preset.",
                        actionLabel = "Create Formula Preset",
                        onAction = {
                            presetToEdit = null
                            showEditDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Section: User Custom Presets (if any)
                        if (filteredUserPresets.isNotEmpty()) {
                            item {
                                Text(
                                    text = "CUSTOM PRESETS (${filteredUserPresets.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WorkbenchTextSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(filteredUserPresets, key = { "custom_${it.id}" }) { preset ->
                                val evaluated = remember(preset.name, preset.tags, bracketStyle) {
                                    MagicTextEvaluator.getEvaluatedInfo(
                                        rawName = preset.name,
                                        tags = preset.tags,
                                        bracketStyle = bracketStyle,
                                        context = context
                                    )
                                }
                                val isJustCopied = recentlyCopiedToken == evaluated.formattedName

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = BorderStroke(1.dp, WorkbenchBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { copyToClipboard(evaluated.formattedName, evaluated.label) }
                                        .testTag("custom_preset_card_${preset.id}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header Row: Left = Name + Live value; Right = Inspect / Edit actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${evaluated.label}:",
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Medium,
                                                    color = WorkbenchTextPrimary
                                                )
                                                Text(
                                                    text = "(${evaluated.liveValue} ${if (evaluated.isLiveEvaluated) "[using live data]" else "[sample]"})",
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Normal,
                                                    color = WorkbenchPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.selectPreset(preset.id)
                                                        showDetailSheet = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "Inspect preset",
                                                        tint = WorkbenchTextSecondary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        presetToEdit = preset
                                                        showEditDialog = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit preset",
                                                        tint = WorkbenchTextSecondary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Item 3: Value chip
                                        Surface(
                                            color = WorkbenchInset,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, WorkbenchBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { copyToClipboard(evaluated.formattedName, evaluated.label) }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = evaluated.formattedName,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = WorkbenchPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (isJustCopied) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Copied",
                                                        tint = StatusDeployed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = WorkbenchTextSecondary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Sub-tokens chips if multiple tokens exist
                                        if (evaluated.subTokens.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Tokens:",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = WorkbenchTextSecondary
                                                )
                                                evaluated.subTokens.forEach { tokenName ->
                                                    val formattedSub = bracketStyle.formatToken(tokenName)
                                                    Surface(
                                                        color = WorkbenchInset,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(1.dp, WorkbenchBorder),
                                                        modifier = Modifier.clickable { copyToClipboard(formattedSub, tokenName) }
                                                    ) {
                                                        Text(
                                                            text = formattedSub,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp,
                                                            color = WorkbenchPrimary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Item 4: Description
                                        Text(
                                            text = preset.content,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = WorkbenchTextSecondary
                                        )

                                        if (preset.tagList.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                preset.tagList.forEach { tag -> TagPill(tag = tag) }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Standard Common Combinations Catalog
                        if (filteredBuiltinCombos.isNotEmpty()) {
                            item {
                                Text(
                                    text = "STANDARD COMBINATIONS (${filteredBuiltinCombos.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WorkbenchTextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(filteredBuiltinCombos, key = { "builtin_${it.name}" }) { combo ->
                                val formattedFormula = bracketStyle.formatTemplate(combo.template)
                                val (liveSample, isLive) = remember(combo.template, bracketStyle) {
                                    MagicTextEvaluator.evaluateLiveSample(combo.template, context)
                                }
                                val subTokens = remember(combo.template) {
                                    MagicTextEvaluator.extractSubTokens(combo.template)
                                }
                                val isJustCopied = recentlyCopiedToken == formattedFormula

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkbenchSurface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = BorderStroke(1.dp, WorkbenchBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { copyToClipboard(formattedFormula, combo.name) }
                                        .testTag("combo_card_${combo.name}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Row 1: Left = Name + Live Value; Right = Category Tag (11sp muted, NO background pill)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${combo.name}:",
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Medium,
                                                    color = WorkbenchTextPrimary
                                                )
                                                Text(
                                                    text = "($liveSample ${if (isLive) "[using live data]" else "[sample]"})",
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Normal,
                                                    color = WorkbenchPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Category Tag: 11sp, muted color, top-right corner, no background pill (just text)
                                            Text(
                                                text = combo.category.displayName,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                color = WorkbenchTextSecondary,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Item 3: Value chip
                                        Surface(
                                            color = WorkbenchInset,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, WorkbenchBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { copyToClipboard(formattedFormula, combo.name) }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = formattedFormula,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = WorkbenchPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (isJustCopied) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Copied",
                                                        tint = StatusDeployed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = WorkbenchTextSecondary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Sub-tokens
                                        if (subTokens.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Tokens:",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = WorkbenchTextSecondary
                                                )
                                                subTokens.forEach { tokenName ->
                                                    val formattedSub = bracketStyle.formatToken(tokenName)
                                                    Surface(
                                                        color = WorkbenchInset,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(1.dp, WorkbenchBorder),
                                                        modifier = Modifier.clickable { copyToClipboard(formattedSub, tokenName) }
                                                    ) {
                                                        Text(
                                                            text = formattedSub,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp,
                                                            color = WorkbenchPrimary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Item 4: Description
                                        Text(
                                            text = combo.description,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = WorkbenchTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Bracket Preferences Settings
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = WorkbenchSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Magic Text Bracket Preference",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkbenchTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose the formatting syntax for copying variables and formulas.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = WorkbenchTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isCurly = bracketStyle == BracketStyle.CURLY
                    Surface(
                        color = if (isCurly) WorkbenchPrimary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isCurly) WorkbenchPrimary else WorkbenchBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setBracketStyle(BracketStyle.CURLY)
                                showSettingsSheet = false
                            }
                            .testTag("select_curly_brackets")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "{ } Curly",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isCurly) WorkbenchOnPrimary else WorkbenchTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "e.g. {battery}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (isCurly) WorkbenchOnPrimary.copy(alpha = 0.8f) else WorkbenchPrimary
                            )
                        }
                    }

                    val isSquare = bracketStyle == BracketStyle.SQUARE
                    Surface(
                        color = if (isSquare) WorkbenchPrimary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSquare) WorkbenchPrimary else WorkbenchBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setBracketStyle(BracketStyle.SQUARE)
                                showSettingsSheet = false
                            }
                            .testTag("select_square_brackets")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "[ ] Square",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSquare) WorkbenchOnPrimary else WorkbenchTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "e.g. [battery]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (isSquare) WorkbenchOnPrimary.copy(alpha = 0.8f) else WorkbenchPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Modal Bottom Sheet: Preset Detail & Referencing Macros
    if (showDetailSheet && selectedPresetWithMacros != null) {
        val rel = selectedPresetWithMacros!!
        val preset = rel.preset
        val referencingMacros = rel.macros

        val evaluated = remember(preset.name, preset.tags, bracketStyle) {
            MagicTextEvaluator.getEvaluatedInfo(
                rawName = preset.name,
                tags = preset.tags,
                bracketStyle = bracketStyle,
                context = context
            )
        }

        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                viewModel.selectPreset(null)
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
                // Header with Live evaluated preview
                Text(
                    text = "${evaluated.label}: (${evaluated.liveValue} ${if (evaluated.isLiveEvaluated) "[using live data]" else "[sample]"})",
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = WorkbenchPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = WorkbenchInset,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, WorkbenchBorder),
                        modifier = Modifier.clickable {
                            copyToClipboard(evaluated.formattedName, evaluated.label)
                        }
                    ) {
                        Text(
                            text = evaluated.formattedName,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = WorkbenchPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                copyToClipboard(evaluated.formattedName, evaluated.label)
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WorkbenchTextPrimary)
                        }

                        IconButton(
                            onClick = {
                                presetToEdit = preset
                                showDetailSheet = false
                                showEditDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WorkbenchTextSecondary)
                        }

                        IconButton(
                            onClick = {
                                presetToDeleteId = preset.id
                                showDeleteConfirmDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BlockingWarningText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DESCRIPTION & FORMULA",
                    style = MaterialTheme.typography.labelSmall,
                    color = WorkbenchTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WorkbenchTextPrimary
                )

                if (preset.tagList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TAGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkbenchTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        preset.tagList.forEach { tag -> TagPill(tag = tag) }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Referencing Macros section
                Text(
                    text = "REFERENCED BY MACROS (${referencingMacros.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = WorkbenchTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (referencingMacros.isEmpty()) {
                    Text(
                        text = "No macros are currently linked to this preset. Link it in Macro Detail -> Linked Presets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        referencingMacros.forEach { macro ->
                            Surface(
                                color = WorkbenchInset,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDetailSheet = false
                                        viewModel.selectPreset(null)
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
                                            fontWeight = FontWeight.Medium,
                                            color = WorkbenchTextPrimary,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                        Text(
                                            text = macro.status.displayName,
                                            color = macro.status.dotColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View Macro",
                                        tint = WorkbenchTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add / Edit Preset Dialog
    if (showEditDialog) {
        var tokenName by remember(presetToEdit) { mutableStateOf(presetToEdit?.name ?: "") }
        var tokenContent by remember(presetToEdit) { mutableStateOf(presetToEdit?.content ?: "") }
        var tokenTags by remember(presetToEdit) { mutableStateOf(presetToEdit?.tags ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (presetToEdit == null) "New Magic Text Preset" else "Edit Preset",
                    color = WorkbenchTextPrimary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tokenName,
                        onValueChange = { tokenName = it },
                        label = { Text("Token / Formula (e.g. {date_year}-{date_month})", color = WorkbenchTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchInset,
                            unfocusedContainerColor = WorkbenchInset,
                            focusedBorderColor = WorkbenchPrimary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tokenContent,
                        onValueChange = { tokenContent = it },
                        label = { Text("Description / Output Notes", color = WorkbenchTextSecondary) },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchInset,
                            unfocusedContainerColor = WorkbenchInset,
                            focusedBorderColor = WorkbenchPrimary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tokenTags,
                        onValueChange = { tokenTags = it },
                        label = { Text("Tags / Category (comma-separated)", color = WorkbenchTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WorkbenchInset,
                            unfocusedContainerColor = WorkbenchInset,
                            focusedBorderColor = WorkbenchPrimary,
                            unfocusedBorderColor = WorkbenchBorder,
                            focusedTextColor = WorkbenchTextPrimary,
                            unfocusedTextColor = WorkbenchTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tokenName.isBlank()) {
                            Toast.makeText(context, "Please enter a token or formula name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val preset = Preset(
                            id = presetToEdit?.id ?: 0L,
                            name = tokenName.trim(),
                            content = tokenContent.trim(),
                            tags = tokenTags.trim(),
                            createdAt = presetToEdit?.createdAt ?: System.currentTimeMillis()
                        )

                        viewModel.savePreset(preset) {
                            showEditDialog = false
                            Toast.makeText(context, "Preset saved to library", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkbenchPrimary,
                        contentColor = WorkbenchOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface,
            shape = RoundedCornerShape(8.dp)
        )
    }

    // Delete Confirmation
    if (showDeleteConfirmDialog && presetToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Preset?", color = WorkbenchTextPrimary) },
            text = { Text("Are you sure you want to delete this preset from your library?", color = WorkbenchTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deletePreset(presetToDeleteId!!)
                        showDetailSheet = false
                    }
                ) {
                    Text("Delete", color = BlockingWarningText, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = WorkbenchTextSecondary)
                }
            },
            containerColor = WorkbenchSurface,
            shape = RoundedCornerShape(8.dp)
        )
    }
}
