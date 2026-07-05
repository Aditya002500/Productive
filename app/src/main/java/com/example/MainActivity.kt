package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

                        Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {


                            // Splash Screen
                            composable("splash") {
                                SplashScreen(
                                    onTimeout = {
                                        val destination = if (appViewModel.isLoggedIn) "main" else "welcome"
                                        navController.navigate(destination) {
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
                                        navController.navigate("main") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    },
                                    onConsentGiven = { given ->
                                        appViewModel.setConsentGiven(given)
                                    }
                                )
                            }

                            // Main Tabs Screen
                            composable("main") {
                                MainTabsScreen(
                                    viewModel = appViewModel,
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
                                    onNavigateToNotifications = { navController.navigate("notifications") },
                                    onNavigateToNoteEditor = { noteId ->
                                        if (noteId == null) navController.navigate("note_editor/-1")
                                        else navController.navigate("note_editor/$noteId")
                                    },
                                    onNavigateToNoteDetail = { noteId ->
                                        navController.navigate("note_editor/$noteId")
                                    },
                                    onNavigateToCaptureDetail = { captureId ->
                                        navController.navigate("capture_detail/$captureId")
                                    },
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onNavigateToHabits = { navController.navigate("habits") },
                                    onNavigateToFocusTimer = { navController.navigate("focus_timer") },
                                    onNavigateToFriends = { navController.navigate("friends") },
                                    onSignOut = {
                                        appViewModel.signOut()
                                        navController.navigate("welcome") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Habits
                            composable("habits") {
                                HabitsScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToTracking = { habitId ->
                                        navController.navigate("track_habit/$habitId")
                                    }
                                )
                            }

                            // Track Habit (live GPS distance). No-op transitions: the embedded
                            // GoogleMap is a SurfaceView that doesn't participate in Compose's
                            // default crossfade/predictive-back transition, so animating this
                            // route flashes the window's black background mid-transition.
                            composable(
                                route = "track_habit/{habitId}",
                                arguments = listOf(navArgument("habitId") { type = NavType.IntType }),
                                enterTransition = { EnterTransition.None },
                                exitTransition = { ExitTransition.None },
                                popEnterTransition = { EnterTransition.None },
                                popExitTransition = { ExitTransition.None }
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getInt("habitId") ?: 0
                                TrackHabitScreen(
                                    habitId = habitId,
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Focus Timer
                            composable("focus_timer") {
                                FocusTimerScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Friends / compete
                            composable("friends") {
                                FriendsScreen(
                                    viewModel = appViewModel,
                                    onBack = { navController.popBackStack() }
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
                                    onNavigateToSubscription = { navController.navigate("subscription") },
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
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    onNavigateToSubscription = { navController.navigate("subscription") }
                                )
                            }
                        }

                        // Live habit GPS tracking survives navigating away from TrackHabitScreen
                        // (state lives on appViewModel) — this bar lets the user get back to it.
                        val trackingHabitId by appViewModel.trackingHabitId.collectAsStateWithLifecycle()
                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                        if (trackingHabitId != null && currentRoute != "track_habit/{habitId}") {
                            val habits by appViewModel.habits.collectAsStateWithLifecycle()
                            val isRunning by appViewModel.trackingIsRunning.collectAsStateWithLifecycle()
                            val seconds by appViewModel.trackingSeconds.collectAsStateWithLifecycle()
                            val distanceMeters by appViewModel.trackingDistanceMeters.collectAsStateWithLifecycle()
                            TrackingFloatingBar(
                                habitTitle = habits.find { it.id == trackingHabitId }?.title ?: "Habit",
                                isRunning = isRunning,
                                distanceMeters = distanceMeters,
                                seconds = seconds,
                                onClick = { navController.navigate("track_habit/${trackingHabitId}") },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            )
                        }

                        // Floating Assistant entry point, visible on every authenticated screen.
                        val authRoutes = setOf("splash", "welcome", "login", "register", "onboarding")
                        if (currentRoute !in authRoutes) {
                            com.example.ui.components.AssistantBubble(
                                viewModel = appViewModel,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        end = 16.dp,
                                        bottom = if (trackingHabitId != null && currentRoute != "track_habit/{habitId}") 160.dp else 88.dp
                                    )
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
private fun TrackingFloatingBar(
    habitTitle: String,
    isRunning: Boolean,
    distanceMeters: Float,
    seconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tracking $habitTitle", fontWeight = FontWeight.SemiBold)
                Text(
                    "%.2f km · %02d:%02d%s".format(
                        distanceMeters / 1000f,
                        seconds / 60,
                        seconds % 60,
                        if (isRunning) "" else " · Paused"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                if (isRunning) "View" else "Resume",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
