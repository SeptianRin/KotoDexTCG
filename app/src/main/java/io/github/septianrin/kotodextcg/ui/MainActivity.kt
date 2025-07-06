@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.septianrin.kotodextcg.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.septianrin.kotodextcg.R
import io.github.septianrin.kotodextcg.ui.feature.carddetail.CardDetailScreen
import io.github.septianrin.kotodextcg.ui.feature.cardlist.CardListScreen
import io.github.septianrin.kotodextcg.ui.feature.collection.CollectionScreen
import io.github.septianrin.kotodextcg.ui.feature.gacha.GachaScreen
import io.github.septianrin.kotodextcg.ui.theme.KotoDexTCGTheme
import io.github.septianrin.kotodextcg.util.AppRoutes

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomNavItem(AppRoutes.HOME, "Home", Icons.Default.Home)
    data object OpenPack : BottomNavItem(AppRoutes.GACHA, "Open Pack", Icons.Default.CardGiftcard)
    data object Collection : BottomNavItem(AppRoutes.COLLECTION, "Collection", Icons.Default.Style)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            KotoDexTCGTheme(darkTheme = isDarkTheme) {
                MainApp(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun MainApp(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val homeListState = rememberLazyGridState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when {
        currentRoute?.startsWith("detail/") == true -> stringResource(R.string.card_details)
        currentRoute == AppRoutes.GACHA -> stringResource(R.string.gacha_simulator)
        currentRoute == AppRoutes.COLLECTION -> stringResource(R.string.my_collection)
        else -> stringResource(R.string.kotodex)
    }

    val showBackButton = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(
                                    R.string.back
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = stringResource(R.string.toggle_theme)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    val navigationItems =
                        listOf(BottomNavItem.Home, BottomNavItem.OpenPack, BottomNavItem.Collection)
                    navigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, textAlign = TextAlign.Center) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            homeListState = homeListState
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    homeListState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier
    ) {
        composable(AppRoutes.HOME) {
            CardListScreen(
                listState = homeListState,
                onCardClicked = { cardId ->
                    navController.navigate(AppRoutes.detailRoute(cardId))
                }
            )
        }
        composable(AppRoutes.COLLECTION) {
            CollectionScreen(
                onCardClicked = { cardId ->
                    navController.navigate(AppRoutes.detailRoute(cardId))
                },
                onGoToGachaClicked = {
                    navController.navigate(AppRoutes.GACHA)
                }
            )
        }
        composable(AppRoutes.GACHA) {
            GachaScreen(
                onCardClicked = { cardId ->
                    navController.navigate(AppRoutes.detailRoute(cardId))
                }
            )
        }
        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) {
            CardDetailScreen()
        }
    }
}
