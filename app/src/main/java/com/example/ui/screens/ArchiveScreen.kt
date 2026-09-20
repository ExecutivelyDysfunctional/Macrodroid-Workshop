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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.MacroStatus
import com.example.ui.WorkshopViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipRow
import com.example.ui.components.MacroCard
import com.example.ui.components.SearchBar
import com.example.ui.components.TagPill
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary

@Composable
fun ArchiveScreen(
    viewModel: WorkshopViewModel,
    onMacroClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val macros by viewModel.archiveMacros.collectAsStateWithLifecycle()
    val searchQuery by viewModel.archiveSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.archiveStatusFilter.collectAsStateWithLifecycle()
    val selectedTag by viewModel.archiveSelectedTag.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = WorkbenchBg,
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
                        text = "Archive",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WorkbenchTextPrimary
                    )
                    Text(
                        text = "Built & Deployed Automations",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary
                    )
                }

                // Completed badge
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
                                .background(StatusDeployed, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "${macros.size} Archived",
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
                onQueryChange = { viewModel.archiveSearchQuery.value = it },
                placeholder = "Search built & deployed macros...",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            val statusOptions = listOf(
                "All Archived (${macros.size})" to (statusFilter == null),
                "Built Only" to (statusFilter == MacroStatus.BUILT),
                "Deployed Only" to (statusFilter == MacroStatus.DEPLOYED)
            )

            FilterChipRow(
                options = statusOptions,
                onSelect = { index ->
                    viewModel.archiveStatusFilter.value = when (index) {
                        1 -> MacroStatus.BUILT
                        2 -> MacroStatus.DEPLOYED
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
                        onClick = { viewModel.archiveSelectedTag.value = null }
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

            // List or empty state
            if (macros.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Archive,
                    title = if (searchQuery.isNotBlank() || statusFilter != null) "No matching macros found" else "Archive is Empty",
                    description = if (searchQuery.isNotBlank() || statusFilter != null) {
                        "Try changing your search query or status filter."
                    } else {
                        "When you finish building and deploying macros from your Workbench, they will appear here as reference records."
                    },
                    actionLabel = if (searchQuery.isNotBlank() || statusFilter != null || selectedTag != null) "Clear Filters" else null,
                    onAction = {
                        viewModel.archiveSearchQuery.value = ""
                        viewModel.archiveStatusFilter.value = null
                        viewModel.archiveSelectedTag.value = null
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
