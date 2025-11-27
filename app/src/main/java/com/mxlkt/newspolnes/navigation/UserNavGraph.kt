package com.mxlkt.newspolnes.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mxlkt.newspolnes.ui.user.LiveNewsDetailScreen
import com.mxlkt.newspolnes.ui.user.NewsDetailScreen
import com.mxlkt.newspolnes.components.PolnesTopAppBar
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.ui.user.*
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen
import com.mxlkt.newspolnes.ui.common.AboutScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNavGraph(
    rootNavController: NavHostController,
    onLogout: () -> Unit // Parameter Logout
) {
    val userNavController = rememberNavController()
    val navBackStackEntry by userNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Home"

    val userScreens = listOf("Home", "Categories", "Notifications", "Settings")
    val showBars = userScreens.any { currentRoute.startsWith(it) }

    Scaffold(
        topBar = {
            if (showBars) {
                when (currentRoute) {
                    "Home" -> PolnesTopAppBar()
                    "Categories" -> TitleOnlyTopAppBar(title = "Categories")
                    "Notifications" -> TitleOnlyTopAppBar(title = "Notifications")
                    "Settings" -> TitleOnlyTopAppBar(title = "Settings")
                }
            }
        },
        bottomBar = {
            if (showBars) {
                UserBottomNav(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        userNavController.navigate(route) {
                            popUpTo(userNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = userNavController,
            startDestination = "Home",
            modifier = if (showBars) Modifier.padding(innerPadding) else Modifier.fillMaxSize(),
            // Animasi Transisi Halaman
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            // 1. HOME
            composable("Home") {
                HomeScreen(
                    onViewAllRecent = { userNavController.navigate("RecentNews") },
                    onViewAllMostViewed = { userNavController.navigate("MostViewedNews") },
                    onViewAllMostRated = { userNavController.navigate("MostRatedNews") },
                    onNewsClick = { newsId -> userNavController.navigate("NewsDetail/$newsId") }
                )
            }

            // 2. CATEGORIES
            composable("Categories") {
                CategoriesScreen(
                    onCategoryClick = { categoryName ->
                        userNavController.navigate("CategorySelected/$categoryName")
                    }
                )
            }

            // 3. NOTIFICATIONS
            composable("Notifications") {
                NotificationsScreen(
                    onNewsClick = { newsId ->
                        userNavController.navigate("NewsDetail/$newsId")
                    }
                )
            }

            // 4. SETTINGS
            composable("Settings") {
                SettingsScreen(
                    onLogout = onLogout, // Panggil fungsi onLogout yang diteruskan
                    onPrivacyClick = { userNavController.navigate("PrivacyPolicy") },
                    onAboutClick = { userNavController.navigate("About") }
                )
            }

            // --- Sub-Screens ---

            // Detail Kategori
            composable(
                route = "CategorySelected/{categoryName}",
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                CategorySelectedScreen(
                    categoryName = categoryName,
                    onNavigateBack = { userNavController.popBackStack() },
                    onNewsClick = { newsId -> userNavController.navigate("NewsDetail/$newsId") }
                )
            }

            // List Berita: Recent
            composable("RecentNews") {
                RecentNewsScreen(
                    onNavigateBack = { userNavController.popBackStack() },
                    onNewsClick = { newsId -> userNavController.navigate("NewsDetail/$newsId") }
                )
            }

            // List Berita: Most Viewed
            composable("MostViewedNews") {
                MostViewedNewsScreen(
                    onNavigateBack = { userNavController.popBackStack() },
                    onNewsClick = { newsId -> userNavController.navigate("LiveNewsDetail/$newsId") }
                )
            }

            // List Berita: Most Rated
            composable("MostRatedNews") {
                MostRatedNewsScreen(
                    onNavigateBack = { userNavController.popBackStack() },
                    onNewsClick = { newsId -> userNavController.navigate("NewsDetail/$newsId") }
                )
            }

            // Halaman Umum: Privacy & About
            composable("PrivacyPolicy") {
                PrivacyPolicyScreen(
                    onNavigateBack = { userNavController.popBackStack() }
                )
            }

            composable("About") {
                AboutScreen(
                    onNavigateBack = { userNavController.popBackStack() }
                )
            }

            // Halaman Detail Berita (Tujuan Akhir)
            composable(
                route = "NewsDetail/{newsId}",
                arguments = listOf(navArgument("newsId") { type = NavType.IntType })
            ) { backStackEntry ->
                val newsId = backStackEntry.arguments?.getInt("newsId") ?: 0
                NewsDetailScreen(
                    onNavigateBack = { userNavController.popBackStack() },
                    newsId = newsId
                )
            }
            // Halaman Detail Berita (Tujuan Akhir)
            composable(
                route = "LiveNewsDetail/{newsId}",
                arguments = listOf(navArgument("newsId") { type = NavType.IntType })
            ) { backStackEntry ->
                val newsId = backStackEntry.arguments?.getInt("newsId") ?: 0
                LiveNewsDetailScreen(
                    onNavigateBack = { userNavController.popBackStack() },
                    newsId = newsId
                )
            }
        }
    }
}