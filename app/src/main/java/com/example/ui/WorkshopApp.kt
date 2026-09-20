package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.MacroDetailScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.WebhooksScreen
import com.example.ui.screens.WorkbenchScreen
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary

enum class WorkshopTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    WORKBENCH("Workbench", Icons.Filled.Build, Icons.Outlined.Build, "nav_workbench"),
    ARCHIVE("Archive", Icons.Filled.Archive, Icons.Outlined.Archive, "nav_archive"),
    PRESETS("Presets", Icons.Filled.Code, Icons.Outlined.Code, "nav_presets"),
    WEBHOOKS("Webhooks", Icons.Filled.Http, Icons.Outlined.Http, "nav_webhooks")
}

sealed class ScreenDestination {
    data class TabView(val tab: WorkshopTab) : ScreenDestination()
    data class MacroDetail(val macroId: Long) : ScreenDestination()
}

@Composable
fun WorkshopApp(
    viewModel: WorkshopViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf<ScreenDestination>(ScreenDestination.TabView(WorkshopTab.WORKBENCH)) }
    var currentTab by remember { mutableStateOf(WorkshopTab.WORKBENCH) }

    Scaffold(
        bottomBar = {
            if (currentDestination is ScreenDestination.TabView) {
                NavigationBar(
                    containerColor = WorkbenchSurface,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    WorkshopTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                currentDestination = ScreenDestination.TabView(tab)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = WorkbenchTextMuted,
                                unselectedTextColor = WorkbenchTextMuted
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                if (targetState is ScreenDestination.MacroDetail) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "screen_navigation",
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                is ScreenDestination.TabView -> {
                    when (destination.tab) {
                        WorkshopTab.WORKBENCH -> WorkbenchScreen(
                            viewModel = viewModel,
                            onMacroClick = { id ->
                                currentDestination = ScreenDestination.MacroDetail(id)
                            },
                            onAddMacroClick = {
                                currentDestination = ScreenDestination.MacroDetail(0L)
                            }
                        )
                        WorkshopTab.ARCHIVE -> ArchiveScreen(
                            viewModel = viewModel,
                            onMacroClick = { id ->
                                currentDestination = ScreenDestination.MacroDetail(id)
                            }
                        )
                        WorkshopTab.PRESETS -> PresetsScreen(
                            viewModel = viewModel,
                            onMacroClick = { id ->
                                currentDestination = ScreenDestination.MacroDetail(id)
                            }
                        )
                        WorkshopTab.WEBHOOKS -> WebhooksScreen(
                            viewModel = viewModel,
                            onMacroClick = { id ->
                                currentDestination = ScreenDestination.MacroDetail(id)
                            }
                        )
                    }
                }
                is ScreenDestination.MacroDetail -> {
                    MacroDetailScreen(
                        macroId = destination.macroId,
                        viewModel = viewModel,
                        onBack = {
                            currentDestination = ScreenDestination.TabView(currentTab)
                        }
                    )
                }
            }
        }
    }
}
