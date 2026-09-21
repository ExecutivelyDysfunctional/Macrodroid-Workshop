package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.MacroDetailScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.WebhooksScreen
import com.example.ui.screens.WorkbenchScreen
import com.example.ui.theme.StatusDeployed
import com.example.ui.theme.WorkbenchBg
import com.example.ui.theme.WorkbenchBorder
import com.example.ui.theme.WorkbenchInset
import com.example.ui.theme.WorkbenchOnPrimary
import com.example.ui.theme.WorkbenchPrimary
import com.example.ui.theme.WorkbenchSurface
import com.example.ui.theme.WorkbenchTextMuted
import com.example.ui.theme.WorkbenchTextPrimary
import com.example.ui.theme.WorkbenchTextSecondary
import kotlinx.coroutines.launch

enum class NavigationLayout(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    BOTTOM_BAR("Bottom Bar", "Classic bottom navigation tabs for thumb access", Icons.Default.ViewStream),
    TOP_TABS("Top Segmented Tabs", "IDE & Workbench horizontal header tab bar", Icons.Default.Tab),
    NAV_DRAWER("Slide-out Drawer", "Clean side menu hiding all navigation chrome", Icons.Default.Menu),
    NAV_RAIL("Side Navigation Rail", "Vertical side bar for desktop & high density", Icons.Default.ViewColumn),
    FLOATING_CAPSULE("Floating Capsule", "Modern floating pill island anchored at bottom", Icons.Default.MoreHoriz)
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopApp(
    viewModel: WorkshopViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf<ScreenDestination>(ScreenDestination.TabView(WorkshopTab.WORKBENCH)) }
    var currentTab by remember { mutableStateOf(WorkshopTab.WORKBENCH) }

    val currentNavLayout by viewModel.navigationLayout.collectAsStateWithLifecycle()
    var showLayoutSettingsSheet by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val onTabSelect: (WorkshopTab) -> Unit = { tab ->
        currentTab = tab
        currentDestination = ScreenDestination.TabView(tab)
    }

    val onNavigateToDetail: (Long) -> Unit = { id ->
        currentDestination = ScreenDestination.MacroDetail(id)
    }

    // Modal Bottom Sheet for Navigation Layout Configurator
    if (showLayoutSettingsSheet) {
        NavigationLayoutConfigSheet(
            currentLayout = currentNavLayout,
            onSelectLayout = { layout ->
                viewModel.setNavigationLayout(layout)
                showLayoutSettingsSheet = false
            },
            onDismiss = { showLayoutSettingsSheet = false }
        )
    }

    // Main Layout Shell depending on active NavigationLayout mode
    when (currentNavLayout) {
        NavigationLayout.BOTTOM_BAR -> {
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
                                    onClick = { onTabSelect(tab) },
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
                                        selectedIconColor = WorkbenchOnPrimary,
                                        selectedTextColor = WorkbenchPrimary,
                                        indicatorColor = WorkbenchPrimary,
                                        unselectedIconColor = WorkbenchTextMuted,
                                        unselectedTextColor = WorkbenchTextMuted
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }

                            // Quick trigger icon for Layout Configurator inside bottom bar
                            NavigationBarItem(
                                selected = false,
                                onClick = { showLayoutSettingsSheet = true },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.ViewQuilt,
                                        contentDescription = "Change Layout",
                                        tint = WorkbenchTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Layout",
                                        fontSize = 10.sp,
                                        color = WorkbenchTextSecondary
                                    )
                                },
                                modifier = Modifier.testTag("nav_layout_configurator_btn")
                            )
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                MainContentArea(
                    currentDestination = currentDestination,
                    currentTab = currentTab,
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onBack = { currentDestination = ScreenDestination.TabView(currentTab) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        NavigationLayout.TOP_TABS -> {
            Scaffold(
                topBar = {
                    if (currentDestination is ScreenDestination.TabView) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WorkbenchSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = WorkbenchPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MacroDroid Workshop",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkbenchTextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { showLayoutSettingsSheet = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewQuilt,
                                        contentDescription = "Layout Settings",
                                        tint = WorkbenchTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Segmented Top Tab Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                WorkshopTab.values().forEach { tab ->
                                    val isSelected = currentTab == tab
                                    Surface(
                                        color = if (isSelected) WorkbenchPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) WorkbenchPrimary else WorkbenchBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onTabSelect(tab) }
                                            .testTag(tab.testTag)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = null,
                                                tint = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tab.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                MainContentArea(
                    currentDestination = currentDestination,
                    currentTab = currentTab,
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onBack = { currentDestination = ScreenDestination.TabView(currentTab) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        NavigationLayout.NAV_DRAWER -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = WorkbenchSurface,
                        drawerContentColor = WorkbenchTextPrimary,
                        modifier = Modifier.width(280.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = WorkbenchPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "MacroDroid",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkbenchTextPrimary
                                    )
                                    Text(
                                        text = "Automation Workbench",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WorkbenchTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Navigation Items
                            WorkshopTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        onTabSelect(tab)
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = WorkbenchPrimary,
                                        selectedIconColor = WorkbenchOnPrimary,
                                        selectedTextColor = WorkbenchOnPrimary,
                                        unselectedIconColor = WorkbenchTextSecondary,
                                        unselectedTextColor = WorkbenchTextSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Bottom Layout Configurator trigger inside Drawer
                            Surface(
                                color = WorkbenchInset,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, WorkbenchBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        showLayoutSettingsSheet = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ViewQuilt,
                                            contentDescription = null,
                                            tint = WorkbenchPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Navigation Layout",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = WorkbenchTextPrimary
                                            )
                                            Text(
                                                text = currentNavLayout.title,
                                                fontSize = 11.sp,
                                                color = WorkbenchTextSecondary
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = WorkbenchTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) {
                Scaffold(
                    topBar = {
                        if (currentDestination is ScreenDestination.TabView) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = currentTab.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = WorkbenchTextPrimary
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open Drawer",
                                            tint = WorkbenchTextPrimary
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showLayoutSettingsSheet = true }) {
                                        Icon(
                                            imageVector = Icons.Default.ViewQuilt,
                                            contentDescription = "Layout Settings",
                                            tint = WorkbenchTextSecondary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = WorkbenchSurface)
                            )
                        }
                    }
                ) { innerPadding ->
                    MainContentArea(
                        currentDestination = currentDestination,
                        currentTab = currentTab,
                        viewModel = viewModel,
                        onNavigateToDetail = onNavigateToDetail,
                        onBack = { currentDestination = ScreenDestination.TabView(currentTab) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        NavigationLayout.NAV_RAIL -> {
            Row(modifier = modifier.fillMaxSize()) {
                if (currentDestination is ScreenDestination.TabView) {
                    NavigationRail(
                        containerColor = WorkbenchSurface,
                        header = {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = WorkbenchPrimary,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        WorkshopTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { onTabSelect(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = WorkbenchOnPrimary,
                                    selectedTextColor = WorkbenchPrimary,
                                    indicatorColor = WorkbenchPrimary,
                                    unselectedIconColor = WorkbenchTextMuted,
                                    unselectedTextColor = WorkbenchTextMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Bottom Layout Settings Button on Rail
                        IconButton(
                            onClick = { showLayoutSettingsSheet = true },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewQuilt,
                                contentDescription = "Layout Settings",
                                tint = WorkbenchTextSecondary
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    MainContentArea(
                        currentDestination = currentDestination,
                        currentTab = currentTab,
                        viewModel = viewModel,
                        onNavigateToDetail = onNavigateToDetail,
                        onBack = { currentDestination = ScreenDestination.TabView(currentTab) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        NavigationLayout.FLOATING_CAPSULE -> {
            Box(modifier = modifier.fillMaxSize()) {
                MainContentArea(
                    currentDestination = currentDestination,
                    currentTab = currentTab,
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onBack = { currentDestination = ScreenDestination.TabView(currentTab) },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Island Pill docked near screen bottom center
                if (currentDestination is ScreenDestination.TabView) {
                    Surface(
                        color = WorkbenchSurface,
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, WorkbenchBorder),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .testTag("floating_capsule_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkshopTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                Surface(
                                    color = if (isSelected) WorkbenchPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .clickable { onTabSelect(tab) }
                                        .testTag(tab.testTag)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            tint = if (isSelected) WorkbenchOnPrimary else WorkbenchTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tab.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkbenchOnPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Divider dot
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(4.dp)
                                    .background(WorkbenchBorder, CircleShape)
                            )

                            // Quick Layout Settings Button
                            IconButton(
                                onClick = { showLayoutSettingsSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewQuilt,
                                    contentDescription = "Layout",
                                    tint = WorkbenchTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContentArea(
    currentDestination: ScreenDestination,
    currentTab: WorkshopTab,
    viewModel: WorkshopViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
    ) { destination ->
        when (destination) {
            is ScreenDestination.TabView -> {
                when (destination.tab) {
                    WorkshopTab.WORKBENCH -> WorkbenchScreen(
                        viewModel = viewModel,
                        onMacroClick = onNavigateToDetail,
                        onAddMacroClick = { onNavigateToDetail(0L) }
                    )
                    WorkshopTab.ARCHIVE -> ArchiveScreen(
                        viewModel = viewModel,
                        onMacroClick = onNavigateToDetail
                    )
                    WorkshopTab.PRESETS -> PresetsScreen(
                        viewModel = viewModel,
                        onMacroClick = onNavigateToDetail
                    )
                    WorkshopTab.WEBHOOKS -> WebhooksScreen(
                        viewModel = viewModel,
                        onMacroClick = onNavigateToDetail
                    )
                }
            }
            is ScreenDestination.MacroDetail -> {
                MacroDetailScreen(
                    macroId = destination.macroId,
                    viewModel = viewModel,
                    onBack = onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationLayoutConfigSheet(
    currentLayout: NavigationLayout,
    onSelectLayout: (NavigationLayout) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WorkbenchSurface,
        contentColor = WorkbenchTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Navigation Layout Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WorkbenchTextPrimary
                    )
                    Text(
                        text = "Select your preferred navigation ergonomics",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkbenchTextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Close",
                        tint = WorkbenchTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                NavigationLayout.values().forEach { layout ->
                    val isSelected = currentLayout == layout

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) WorkbenchInset else WorkbenchSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) WorkbenchPrimary else WorkbenchBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLayout(layout) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isSelected) WorkbenchPrimary else WorkbenchInset,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = layout.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) WorkbenchOnPrimary else WorkbenchTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = layout.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WorkbenchTextPrimary
                                )
                                Text(
                                    text = layout.description,
                                    fontSize = 12.sp,
                                    color = WorkbenchTextSecondary
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectLayout(layout) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = WorkbenchPrimary,
                                    unselectedColor = WorkbenchTextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
