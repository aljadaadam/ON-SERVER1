package com.onserver1.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onserver1.app.ui.screens.auth.LoginScreen
import com.onserver1.app.ui.screens.auth.RegisterScreen
import com.onserver1.app.ui.screens.auth.OtpScreen
import com.onserver1.app.ui.screens.auth.ForgotPasswordScreen
import com.onserver1.app.ui.screens.auth.ResetPasswordScreen
import com.onserver1.app.ui.screens.splash.SplashScreen
import com.onserver1.app.ui.screens.home.HomeScreen
import com.onserver1.app.ui.screens.services.ServicesScreen
import com.onserver1.app.ui.screens.games.GamesScreen
import com.onserver1.app.ui.screens.remote.RemoteServicesScreen
import com.onserver1.app.ui.screens.profile.ProfileScreen
import com.onserver1.app.ui.screens.orders.OrdersScreen
import com.onserver1.app.ui.screens.transactions.TransactionsScreen
import com.onserver1.app.ui.screens.settings.SettingsScreen
import com.onserver1.app.ui.screens.settings.EditProfileScreen
import com.onserver1.app.ui.screens.settings.ChangePasswordScreen
import com.onserver1.app.ui.screens.settings.PrivacyPolicyScreen
import com.onserver1.app.ui.screens.settings.TermsOfServiceScreen
import com.onserver1.app.ui.screens.help.HelpSupportScreen
import com.onserver1.app.ui.screens.about.AboutScreen
import com.onserver1.app.ui.components.BottomNavBar
import com.onserver1.app.ui.screens.deposit.AddBalanceScreen
import com.onserver1.app.ui.screens.deposit.UsdtPaymentScreen
import com.onserver1.app.ui.screens.deposit.BankakPaymentScreen
import com.onserver1.app.ui.screens.product.ProductDetailScreen
import com.onserver1.app.webview.WebViewScreen
import com.onserver1.app.data.api.TokenManager
import androidx.navigation.NavType
import androidx.navigation.navArgument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(tokenManager: TokenManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }
    val nextDestination = if (tokenManager.isLoggedIn()) Screen.Home.route else Screen.Login.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Splash Screen
            composable(
                Screen.Splash.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(400)) }
            ) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(nextDestination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth Screens
            composable(
                Screen.Login.route,
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { userId ->
                        navController.navigate(Screen.OtpVerification.createRoute(userId)) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.OtpVerification.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                OtpScreen(
                    userId = userId,
                    onVerified = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onResetCodeSent = { userId ->
                        navController.navigate(Screen.ResetPassword.createRoute(userId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                Screen.ResetPassword.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ResetPasswordScreen(
                    userId = userId,
                    onPasswordReset = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main Screens
            composable(
                Screen.Home.route,
                enterTransition = { fadeIn(tween(500)) + slideInVertically(tween(400)) { it / 6 } },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                HomeScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onViewAll = {
                        navController.navigate(Screen.Services.route)
                    },
                    onAddBalance = {
                        navController.navigate(Screen.AddBalance.route)
                    },
                    onNavigateToScreen = { route ->
                        navController.navigate(route)
                    }
                )
            }

            composable(Screen.Services.route) {
                ServicesScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }

            composable(Screen.Games.route) {
                GamesScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }

            composable(Screen.RemoteServices.route) {
                RemoteServicesScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToOrders = {
                        navController.navigate(Screen.Orders.route)
                    },
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Transactions.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToHelp = {
                        navController.navigate(Screen.HelpSupport.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToAddBalance = {
                        navController.navigate(Screen.AddBalance.route)
                    }
                )
            }

            // Profile Sub-Screens
            composable(Screen.Orders.route) {
                OrdersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    },
                    onNavigateToTermsOfService = {
                        navController.navigate(Screen.TermsOfService.route)
                    }
                )
            }

            composable(Screen.HelpSupport.route) {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    },
                    onNavigateToTermsOfService = {
                        navController.navigate(Screen.TermsOfService.route)
                    }
                )
            }

            // Settings Sub-Screens
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Deposit Screens
            composable(Screen.AddBalance.route) {
                AddBalanceScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToUsdt = { amount ->
                        navController.navigate(Screen.UsdtPayment.createRoute(amount))
                    },
                    onNavigateToBankak = { amount ->
                        navController.navigate(Screen.BankakPayment.createRoute(amount))
                    }
                )
            }

            composable(
                route = Screen.UsdtPayment.route,
                arguments = listOf(navArgument("amount") { type = NavType.FloatType })
            ) { backStackEntry ->
                val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                UsdtPaymentScreen(
                    amount = amount,
                    onBack = { navController.popBackStack() },
                    onDone = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }

            composable(
                route = Screen.BankakPayment.route,
                arguments = listOf(navArgument("amount") { type = NavType.FloatType })
            ) { backStackEntry ->
                val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                BankakPaymentScreen(
                    amount = amount,
                    onBack = { navController.popBackStack() },
                    onDone = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }

            // WebView
            composable(Screen.WebView.route) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                val decodedUrl = java.net.URLDecoder.decode(url, "UTF-8")
                WebViewScreen(
                    url = decodedUrl,
                    onBack = { navController.popBackStack() }
                )
            }

            // Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOrderSuccess = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
