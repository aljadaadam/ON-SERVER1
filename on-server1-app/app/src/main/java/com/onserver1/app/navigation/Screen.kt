package com.onserver1.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.onserver1.app.R

sealed class Screen(val route: String) {
    // Splash
    data object Splash : Screen("splash")

    // Auth
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OtpVerification : Screen("otp/{userId}") {
        fun createRoute(userId: String) = "otp/$userId"
    }
    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password/{userId}") {
        fun createRoute(userId: String) = "reset_password/$userId"
    }

    // Main
    data object Home : Screen("home")
    data object Services : Screen("services")
    data object RemoteServices : Screen("remote_services")
    data object Games : Screen("games")
    data object Profile : Screen("profile")

    // Detail
    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    data object WebView : Screen("webview/{url}") {
        fun createRoute(url: String) = "webview/${java.net.URLEncoder.encode(url, "UTF-8")}"
    }
    data object Orders : Screen("orders")
    data object Transactions : Screen("transactions")
    data object Settings : Screen("settings")
    data object HelpSupport : Screen("help_support")
    data object About : Screen("about")
    data object EditProfile : Screen("edit_profile")
    data object ChangePassword : Screen("change_password")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object TermsOfService : Screen("terms_of_service")

    // Deposit
    data object AddBalance : Screen("add_balance")
    data object UsdtPayment : Screen("usdt_payment/{amount}") {
        fun createRoute(amount: Double) = "usdt_payment/$amount"
    }
    data object BankakPayment : Screen("bankak_payment/{amount}") {
        fun createRoute(amount: Double) = "bankak_payment/$amount"
    }
}

data class BottomNavItem(
    val screen: Screen,
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Services, R.string.server_services, Icons.Filled.Dns, Icons.Outlined.Dns),
    BottomNavItem(Screen.RemoteServices, R.string.remote_services, Icons.Filled.Cloud, Icons.Outlined.Cloud),
    BottomNavItem(Screen.Games, R.string.imei_services, Icons.Filled.PhoneAndroid, Icons.Outlined.PhoneAndroid),
    BottomNavItem(Screen.Profile, R.string.my_account, Icons.Filled.Person, Icons.Outlined.Person),
)
