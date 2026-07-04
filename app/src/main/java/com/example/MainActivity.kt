package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val appViewModel: AppViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
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
                                }
                            )
                        }

                        // Home Screen Dashboard
                        composable("home") {
                            HomeScreen(
                                viewModel = appViewModel,
                                onNavigateToPlanner = { tabKey ->
                                    navController.navigate("planner/$tabKey")
                                },
                                onNavigateToSearch = {
                                    navController.navigate("search")
                                },
                                onNavigateToTaskDetail = { taskId ->
                                    navController.navigate("task_detail/$taskId")
                                },
                                onLogout = {
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
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
                    }
                }
            }
        }
    }
}
