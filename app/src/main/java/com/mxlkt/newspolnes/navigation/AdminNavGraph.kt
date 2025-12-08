package com.mxlkt.newspolnes.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.ui.admin.*
import com.mxlkt.newspolnes.ui.common.AboutScreen
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val adminNavController = rememberNavController()
    val context = LocalContext.current

    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Dashboard"

    val adminMainScreens = listOf("Dashboard", "News", "Categories", "Users", "Settings")

    // Logika menyembunyikan TopBar dan BottomBar di halaman detail/form
    // PERBAIKAN: "add_category" dihapus dari pengecualian karena route-nya sudah tidak ada
    val showBars = adminMainScreens.any { currentRoute.startsWith(it) } &&
            !currentRoute.startsWith("edit_article") &&
            !currentRoute.startsWith("add_article")

    val topBarTitle = when {
        currentRoute.startsWith("Dashboard") -> "Admin Dashboard"
        currentRoute.startsWith("News") -> "Manage News"
        currentRoute.startsWith("Categories") -> "Manage Categories"
        currentRoute.startsWith("Users") -> "Manage Users"
        currentRoute.startsWith("Settings") -> "Settings"
        currentRoute == "PrivacyPolicy" -> "Privacy Policy"
        currentRoute == "About" -> "About"
        else -> ""
    }

    Scaffold(
        topBar = {
            if (showBars || currentRoute == "PrivacyPolicy" || currentRoute == "About") {
                TitleOnlyTopAppBar(title = topBarTitle)
            }
        },
        bottomBar = {
            if (showBars) {
                AdminBottomNav(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        adminNavController.navigate(route) {
                            popUpTo(adminNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = adminNavController,
            startDestination = "Dashboard",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            // 1. Dashboard
            composable("Dashboard") { AdminDashboardScreen() }

            // 2. Manage News (Screen Utama)
            composable("News") {
                ManageNewsScreen(
                    onAddArticleClick = {
                        adminNavController.navigate("add_article")
                    },
                    onEditArticleClick = { articleId ->
                        adminNavController.navigate("edit_article/$articleId")
                    }
                )
            }

            // 3. Form Tambah Artikel
            composable("add_article") {
                AdminEditArticleScreen(
                    articleId = -1,
                    onBackClick = { adminNavController.popBackStack() },
                    onSaveClick = { adminNavController.popBackStack() }
                )
            }

            // 4. Form Edit Artikel
            composable(
                route = "edit_article/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("articleId") ?: -1
                AdminEditArticleScreen(
                    articleId = articleId,
                    onBackClick = { adminNavController.popBackStack() },
                    onSaveClick = { adminNavController.popBackStack() }
                )
            }

            // 5. Manage Categories (FOKUS PERBAIKAN DISINI)
            // Karena sekarang pakai Modal/Dialog, kita tidak butuh navigasi tambahan
            composable("Categories") {
                ManageCategoriesScreen()
            }

            // 6. Form Category (HAPUS)
            // Route "add_category" dihapus karena fungsinya sudah pindah ke dalam Modal di ManageCategoriesScreen

            // 7. Manage Users
            composable("Users") { ManageUsersScreen() }

            // 8. Settings
            composable("Settings") {
                AdminSettingsScreen(
                    onLogout = onLogout,
                    onPrivacyClick = { adminNavController.navigate("PrivacyPolicy") },
                    onAboutClick = { adminNavController.navigate("About") }
                )
            }

            // 9. Common Pages
            composable("PrivacyPolicy") {
                PrivacyPolicyScreen(onNavigateBack = { adminNavController.popBackStack() })
            }

            composable("About") {
                AboutScreen(onNavigateBack = { adminNavController.popBackStack() })
            }
        }
    }
}