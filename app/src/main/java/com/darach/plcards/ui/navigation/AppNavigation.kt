@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalSharedTransitionApi::class)

package com.darach.plcards.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darach.plcards.R
import com.darach.plcards.ui.card_detail.CardDetailScreen
import com.darach.plcards.ui.grid.GenericGridScreen
import com.darach.plcards.ui.home.HomeScreen
import com.darach.plcards.ui.home.ShelfType
import com.darach.plcards.ui.my_xi.MyXIScreen
import com.darach.plcards.ui.search.SearchScreen
import com.darach.plcards.ui.settings.SettingsScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun AppNavigation(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isCardDetailScreen = currentRoute?.startsWith("cardDetail/") == true

    val showNavRail =
        windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact && !isCardDetailScreen

    val navItems = listOf(Screen.Home, Screen.Search, Screen.MyXI, Screen.Settings)

    Row(modifier = Modifier.fillMaxSize()) {
        // NavigationRail for larger screens (hidden on card detail) with smooth transition
        AnimatedVisibility(
            visible = showNavRail,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val currentDestination = navBackStackEntry?.destination

                // Add spacer to center the navigation items
                Box(modifier = Modifier.weight(1f))

                navItems.forEach { screen ->
                    NavigationRailItem(
                        icon = {
                            when {
                                screen.iconRes != null -> {
                                    Icon(
                                        painter = painterResource(id = screen.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                screen.icon != null -> {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = null, // Remove labels
                        selected = currentDestination?.hierarchy?.any {
                            it.route?.startsWith(
                                screen.route.split(
                                    "?"
                                )[0]
                            ) == true
                        } == true,
                        onClick = {
                            val navigationRoute =
                                if (screen is Screen.Search) screen.createRoute() else screen.route
                            if (screen is Screen.Home) {
                                // Special handling for Home to ensure clean navigation
                                navController.navigate(navigationRoute) {
                                    popUpTo(0) {
                                        saveState = false
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            } else {
                                navController.navigate(navigationRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true // Saves the state of the start destination
                                        inclusive = false
                                    }
                                    launchSingleTop =
                                        true // Avoids multiple copies of the same destination
                                    restoreState = screen !is Screen.Search
                                }
                            }
                        }
                    )
                }

                // Add spacer to center the navigation items
                Box(modifier = Modifier.weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            // Main content - NavHost with SharedTransitionLayout
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            navController = navController,
                            windowSizeClass = windowSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable
                        )
                    }
                    composable(
                        Screen.Search.route,
                        arguments = listOf(navArgument("teamFilter") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) {
                        SearchScreen(
                            navController = navController,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable
                        )
                    }
                    composable(Screen.MyXI.route) {
                        MyXIScreen(
                            navController = navController,
                            windowSizeClass = windowSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(windowSizeClass)
                    }
                    composable(
                        Screen.CardDetail.route,
                        arguments = listOf(navArgument("cardId") { type = NavType.StringType })
                    ) {
                        CardDetailScreen(
                            navController = navController,
                            windowSizeClass = windowSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable
                        )
                    }
                    composable(
                        Screen.GenericGrid.route,
                        arguments = listOf(navArgument("shelfType") { type = NavType.StringType }),
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        GenericGridScreen(
                            navController = navController,
                            //windowSizeClass = windowSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable
                        )
                    }
                }
            }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            // Only show bottom nav bar on main screens
            val showBottomNav = currentRoute?.let { route ->
                navItems.any { screen ->
                    route.startsWith(screen.route.split("?")[0].split("/")[0])
                }
            } ?: false

            if (!showNavRail && showBottomNav) {
                // Add a clickable background to prevent click-through
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Do nothing */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .clip(RoundedCornerShape(63.dp))
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thin()
                            )
                            .background(Color.Transparent)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        navItems.forEach { screen ->
                            val isSelected = currentDestination.hierarchy.any {
                                it.route?.startsWith(
                                    screen.route.split("?")[0]
                                ) == true
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false, radius = 24.dp)
                                    ) {
                                        val navigationRoute =
                                            if (screen is Screen.Search) screen.createRoute() else screen.route
                                        if (screen is Screen.Home) {
                                            // Special handling for Home to ensure clean navigation
                                            navController.navigate(navigationRoute) {
                                                popUpTo(0) {
                                                    saveState = false
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                                restoreState = false
                                            }
                                        } else {
                                            navController.navigate(navigationRoute) {
                                                // For other nav items, clear back stack to home and navigate
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                                // Don't restore state for search to prevent filter loops
                                                restoreState = screen !is Screen.Search
                                            }
                                        }
                                    }
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    screen.iconRes != null -> {
                                        Icon(
                                            painter = painterResource(id = screen.iconRes),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    screen.icon != null -> {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val iconRes: Int? = null, val icon: ImageVector? = null) {
    object Home : Screen("Home", iconRes = R.drawable.ic_home)
    object Search : Screen("search?teamFilter={teamFilter}", iconRes = R.drawable.ic_search) {
        fun createRoute(teamFilter: String? = null): String {
            return if (teamFilter != null) {
                "search?teamFilter=${java.net.URLEncoder.encode(teamFilter, "UTF-8")}"
            } else {
                "search"
            }
        }
    }

    object MyXI : Screen("XI", iconRes = R.drawable.ic_xi)
    object Settings : Screen("Settings", iconRes = R.drawable.ic_settings)
    object CardDetail : Screen("cardDetail/{cardId}", icon = Icons.Default.Home) {
        fun createRoute(cardId: String) = "cardDetail/$cardId"
    }

    object GenericGrid : Screen("grid/{shelfType}", icon = Icons.Default.Home) {
        fun createRoute(type: ShelfType) = "grid/${type.name}"
    }
}