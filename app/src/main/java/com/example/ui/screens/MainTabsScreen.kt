package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel
import com.example.ui.components.CaptureFlowBottomNavigation
import com.example.ui.components.CaptureFlowTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(
    viewModel: AppViewModel,
    onNavigateToTaskDetail: (Int) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToNoteEditor: (Int?) -> Unit,
    onNavigateToNoteDetail: (Int) -> Unit,
    onNavigateToCaptureDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToFocusTimer: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onSignOut: () -> Unit
) {
    // 0: Home, 1: Planner, 2: Capture, 3: Notes, 4: Search
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val onOpenDrawer: () -> Unit = { coroutineScope.launch { drawerState.open() } }
    val userName by viewModel.userProfileName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userProfileEmail.collectAsStateWithLifecycle()

    val currentRoute = when (pagerState.currentPage) {
        0 -> "home"
        1 -> "planner"
        2 -> "capture"
        3 -> "notes"
        4 -> "search"
        else -> "home"
    }

    val pageTitles = listOf("Home", "Planner", "Capture", "Notes", "Search & Filter")

    val onNavigateBottomBar: (String) -> Unit = { route ->
        val targetPage = when (route) {
            "home" -> 0
            "planner" -> 1
            "capture" -> 2
            "notes" -> 3
            "search" -> 4
            else -> 0
        }
        coroutineScope.launch {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = userName,
                userEmail = userEmail,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToHabits = onNavigateToHabits,
                onNavigateToFocusTimer = onNavigateToFocusTimer,
                onNavigateToFriends = onNavigateToFriends,
                onSignOut = onSignOut,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CaptureFlowTopBar(
                    title = pageTitles[pagerState.currentPage],
                    onOpenDrawer = onOpenDrawer
                )
            },
            bottomBar = {
                CaptureFlowBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = onNavigateBottomBar
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToSearch = { onNavigateBottomBar("search") },
                            onNavigateBottomBar = onNavigateBottomBar,
                            onNavigateToTaskDetail = onNavigateToTaskDetail,
                            onNavigateToCreateTask = onNavigateToCreateTask,
                            onNavigateToCollections = onNavigateToCollections,
                            onNavigateToNotifications = onNavigateToNotifications
                        )
                        1 -> PlannerScreen(
                            viewModel = viewModel,
                            initialTab = "day",
                            onNavigateBottomBar = onNavigateBottomBar
                        )
                        2 -> CaptureInboxScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = onNavigateToCaptureDetail
                        )
                        3 -> NotesListScreen(
                            viewModel = viewModel,
                            onNavigateToEditor = onNavigateToNoteEditor,
                            onNavigateToDetail = onNavigateToNoteDetail
                        )
                        4 -> SearchScreen(
                            viewModel = viewModel,
                            onNavigateToTaskDetail = onNavigateToTaskDetail,
                            onNavigateToNoteDetail = onNavigateToNoteDetail,
                            onNavigateToCaptureDetail = onNavigateToCaptureDetail,
                            onNavigateBottomBar = onNavigateBottomBar
                        )
                    }
                }
            }
        }
    }
}
