package com.onserver1.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.onserver1.app.navigation.bottomNavItems
import com.onserver1.app.ui.theme.AccentYellow
import com.onserver1.app.ui.theme.LocalDimens

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String?
) {
    val d = LocalDimens.current

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier.height(d.bottomNavHeight)
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            val title = stringResource(item.titleRes)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = title,
                        modifier = Modifier.size(d.icon20)
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = d.font10
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentYellow,
                    selectedTextColor = AccentYellow,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
