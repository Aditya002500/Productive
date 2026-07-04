package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.TexturedBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()

            // Share-sheet target: if launched with a shared image, run it through
            // the capture pipeline immediately.
            LaunchedEffect(Unit) {
                val sharedUri: Uri? = if (intent?.action == Intent.ACTION_SEND &&
                    intent.type?.startsWith("image/") == true
                ) {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                } else null
                if (sharedUri != null) {
                    appViewModel.importAndProcess(sharedUri, sourceType = "shared image")
                }
            }

            MyApplicationTheme(themeMode = themeMode) {
                TexturedBackground {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            val onNavigateBottomBar: (String) -> Unit = { tab ->
                                val route = when (tab) {
                                    "home" -> "home"
                                    "planner" -> "planner/day"
                                    "capture" -> "capture_inbox"
                                    "notes" -> "notes"
                                    "search" -> "search"
                                    else -> "home"
                                }
                                navController.navigate(route) {
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }

                            // Splash Screen
                            composable("splash") {
                                SplashScreen(
                                    onTimeout = {
                                        navController.navigate("welcome") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Welcome Screen
                            composable("welcome") {
                                WelcomeScreen(
                                    onGetStarted = {
                                        navController.navigate("register")
                                    },
                                    onSignIn = {
                                        navController.navigate("login")
                                    }
                                )
                            }

                            // Login Screen
                            composable("login") {
                                LoginScreen(
                                    viewModel = appViewModel,
                                    onLoginSuccess = {
                                        navController.navigate("onboarding") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate("register")
                                    }
                                )
                            }

                            // Register Screen
                            composable("register") {
                                RegisterScreen(
                                    viewModel = appViewModel,
                                    onRegisterSuccess = {
                                        navController.navigate("onboarding") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate("login")
                                    }
                                )
                            }

                            // Onboarding Flow Step 1-3
                            composable("onboarding") {
                                OnboardingFlow(
                                    onComplete = {
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    },
                                    onConsentGiven = { given ->
                                        appViewModel.setConsentGiven(given)
                                    }
                                )
                            }

                            // Home Screen Dashboard
                            composable("home") {
                                HomeScreen(
                                    viewModel = appViewModel,
                                    onNavigateToSearch = { onNavigateBottomBar("search") },
                                    onNavigateBottomBar = onNavigateBottomBar,
                                    onNavigateToTaskDetail = { taskId ->
                                        navController.navigate("task_detail/$taskId")
                                    },
                                    onNavigateToCreateTask = {
                                        navController.navigate("create_task")
                                    },
                                    onNavigateToCollections = {
                                        navController.navigate("collections")
                                    },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToNotifications = { navController.navigate("notifications") }
                                )
                            }

                            // Planner Screen (Day, Week, Month calendar agendas)
                            composable(
                                route = "planner/{tab}",
                                arguments = listOf(navArgument("tab") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val tab = backStackEntry.arguments?.getString("tab") ?: "day"
                                PlannerScreen(
                                    viewModel = appViewModel,
                                    initialTab = tab,
                                    onNavigateBottomBar = onNavigateBottomBar,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // Search Screen
                            composable("search") {
                                SearchScreen(
                                    viewModel = appViewModel,
                                    onNavigateToTaskDetail = { taskId ->
                                        navController.navigate("task_detail/$taskId")
                                    },
                                    onNavigateBottomBar = onNavigateBottomBar,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // Task Details Screen
                            composable(
                                route = "task_detail/{taskId}",
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
                                TaskDetailScreen(
                                    taskId = taskId,
                                    viewModel = appViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // Create Task Screen
                            composable("create_task") {
                                CreateTaskScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Notes List
                            composable("notes") {
                                NotesListScreen(
                                    viewModel = appViewModel,
                                    onNavigateToEditor = { noteId ->
                                        if (noteId == null) navController.navigate("note_editor/-1")
                                        else navController.navigate("note_editor/$noteId")
                                    },
                                    onNavigateToDetail = { noteId ->
                                        navController.navigate("note_detail/$noteId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Note Editor
                            composable(
                                route = "note_editor/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                                NoteEditorScreen(
                                    noteId = if (noteId == -1) null else noteId,
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Note Detail
                            composable(
                                route = "note_detail/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                NoteDetailScreen(
                                    noteId = noteId,
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() },
                                    onEdit = { id -> navController.navigate("note_editor/$id") }
                                )
                            }

                            // Capture Inbox
                            composable("capture_inbox") {
                                CaptureInboxScreen(
                                    viewModel = appViewModel,
                                    onNavigateToDetail = { captureId -> navController.navigate("capture_detail/$captureId") },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Capture Detail
                            composable(
                                route = "capture_detail/{captureId}",
                                arguments = listOf(navArgument("captureId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val captureId = backStackEntry.arguments?.getInt("captureId") ?: 0
                                CaptureDetailScreen(
                                    captureId = captureId,
                                    viewModel = appViewModel,
                                    onNavigateToOcr = { id -> navController.navigate("ocr_review/$id") },
                                    onNavigateToSummary = { id -> navController.navigate("ai_summary/$id") },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // OCR Review
                            composable(
                                route = "ocr_review/{captureId}",
                                arguments = listOf(navArgument("captureId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val captureId = backStackEntry.arguments?.getInt("captureId") ?: 0
                                OcrReviewScreen(
                                    captureId = captureId,
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // AI Summary
                            composable(
                                route = "ai_summary/{captureId}",
                                arguments = listOf(navArgument("captureId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val captureId = backStackEntry.arguments?.getInt("captureId") ?: 0
                                AiSummaryScreen(
                                    captureId = captureId,
                                    viewModel = appViewModel,
                                    onNavigateToCreateTask = { navController.navigate("create_task") },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Collections
                            composable("collections") {
                                CollectionsScreen(
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSearch = { query ->
                                        appViewModel.setSearchQuery(query)
                                        appViewModel.setFilter("All")
                                        navController.navigate("search") { launchSingleTop = true }
                                    }
                                )
                            }

                            // Settings Hub
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = appViewModel,
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onNavigateToAiPrivacy = { navController.navigate("ai_privacy") },
                                    onNavigateToSubscription = { navController.navigate("subscription") },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // AI & Privacy
                            composable("ai_privacy") {
                                AiPrivacyScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Subscription
                            composable("subscription") {
                                SubscriptionScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Notifications
                            composable("notifications") {
                                NotificationScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Profile
                            composable("profile") {
                                ProfileScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() },
                                    onLogout = {
                                        navController.navigate("welcome") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
