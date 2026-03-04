package com.onserver1.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.onserver1.app.data.api.TokenManager
import com.onserver1.app.navigation.AppNavigation
import com.onserver1.app.ui.screens.maintenance.MaintenanceScreen
import com.onserver1.app.ui.screens.maintenance.MaintenanceViewModel
import com.onserver1.app.ui.theme.OnServer1Theme
import com.onserver1.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen and dismiss immediately (no icon)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        // Force dark transparent system bars before super.onCreate
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            OnServer1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val maintenanceViewModel: MaintenanceViewModel = hiltViewModel()
                    val isMaintenanceMode by maintenanceViewModel.isMaintenanceMode.collectAsState()
                    val isChecking by maintenanceViewModel.isChecking.collectAsState()
                    val announcement by maintenanceViewModel.announcement.collectAsState()

                    // Track if splash has finished (give it enough time)
                    var splashFinished by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(4000) // Wait for splash animation to complete
                        splashFinished = true
                    }

                    // Re-check maintenance mode when app resumes
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                maintenanceViewModel.checkMaintenanceMode()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Always show AppNavigation (splash plays first)
                        AppNavigation(tokenManager = tokenManager)

                        // Overlay maintenance screen AFTER splash finishes
                        if (splashFinished && !isChecking && isMaintenanceMode) {
                            MaintenanceScreen(
                                onRetry = { maintenanceViewModel.checkMaintenanceMode() },
                                isChecking = isChecking,
                                announcement = announcement
                            )
                        }
                    }
                }
            }
        }
    }
}
