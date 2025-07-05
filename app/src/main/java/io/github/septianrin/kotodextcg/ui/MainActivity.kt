package io.github.septianrin.kotodextcg.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.septianrin.kotodextcg.ui.screen.CardListScreen
import io.github.septianrin.kotodextcg.ui.theme.KotoDexTCGTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import io.github.septianrin.kotodextcg.ui.screen.GachaScreen

// Sealed class to represent our navigation items
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Gacha : Screen("gacha", "Gacha", Icons.Default.Star)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotoDexTCGTheme {
                MainApp()
            }
        }
    }
}
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ForcePortraitOrientation() {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        val original = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = original ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val homeListState = rememberLazyGridState()

    ForcePortraitOrientation()
    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navigationItems = listOf(Screen.Home, Screen.Gacha)
                navigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentScreen.route == screen.route,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            is Screen.Home -> CardListScreen(
                modifier = Modifier.padding(innerPadding),
                listState = homeListState
            )
            is Screen.Gacha -> GachaScreen(Modifier.padding(innerPadding))
        }
    }
}
