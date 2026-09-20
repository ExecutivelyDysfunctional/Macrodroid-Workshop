package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Macro
import com.example.data.models.MacroStatus
import com.example.ui.WorkshopViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipRow
import com.example.ui.components.MacroCard
import com.example.ui.components.SearchBar
import com.example.ui.components.TagPill
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusIdea
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary

@Composable
fun WorkbenchScreen(
    viewModel: WorkshopViewModel,
    onMacroClick: (Long) -> Unit,
    onAddMacroClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val macros by viewModel.workbenchMacros.collectAsStateWithLifecycle()
    val searchQuery by viewModel.workbenchSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.workbenchStatusFilter.collectAsStateWithLifecycle()
    val selectedTag by viewModel.workbenchSelectedTag.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = WorkbenchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMacroClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_macro_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Macro Idea"
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
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Workbench",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WorkbenchTextPrimary
                    )
                    Text(
                        text = "Active Ideas & Draft Automations",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary
                    )
                }

                // Staging counter badge
                Surface(
                    color = WorkbenchSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkbenchBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StatusDraft, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "${macros.size} Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WorkbenchTextPrimary
                        )
                    }
                }
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.workbenchSearchQuery.value = it },
                placeholder = "Search ideas, drafts, triggers, notes...",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            val statusOptions = listOf(
                "All Active (${macros.size})" to (statusFilter == null),
                "Ideas Only" to (statusFilter == MacroStatus.IDEA),
                "Drafts Only" to (statusFilter == MacroStatus.DRAFT)
            )

            FilterChipRow(
                options = statusOptions,
                onSelect = { index ->
                    viewModel.workbenchStatusFilter.value = when (index) {
                        1 -> MacroStatus.IDEA
                        2 -> MacroStatus.DRAFT
                        else -> null
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Tag filter pill row if selected
            if (selectedTag != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Tag: ",
                        fontSize = 12.sp,
                        color = WorkbenchTextMuted
                    )
                    TagPill(
                        tag = selectedTag!!,
                        onClick = { viewModel.workbenchSelectedTag.value = null }
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "(tap to clear)",
                        fontSize = 11.sp,
                        color = WorkbenchTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macro list or Empty state
            if (macros.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Lightbulb,
                    title = if (searchQuery.isNotBlank() || statusFilter != null) "No matching macros found" else "Workbench is Clear",
                    description = if (searchQuery.isNotBlank() || statusFilter != null) {
                        "Try tweaking your search query or removing the status filter."
                    } else {
                        "Stage your raw MacroDroid automation ideas and drafts here before building them in production."
                    },
                    actionLabel = if (searchQuery.isBlank() && statusFilter == null) "Create Macro Idea" else "Clear Filters",
                    onAction = {
                        if (searchQuery.isNotBlank() || statusFilter != null || selectedTag != null) {
                            viewModel.workbenchSearchQuery.value = ""
                            viewModel.workbenchStatusFilter.value = null
                            viewModel.workbenchSelectedTag.value = null
                        } else {
                            onAddMacroClick()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(macros, key = { it.id }) { macro ->
                        MacroCard(
                            macro = macro,
                            onClick = { onMacroClick(macro.id) }
                        )
                    }
                }
            }
        }
    }
}
