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
import android.widget.Toast
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
// import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.ui.admin.*
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen
import com.mxlkt.newspolnes.ui.common.AboutScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph(
    rootNavController: NavHostController,
    // currentUser: User?, // Dihapus
    onLogout: () -> Unit
) {
    val adminNavController = rememberNavController()
    val context = LocalContext.current

    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Dashboard"

    val adminMainScreens = listOf("Dashboard", "News", "Categories", "Users", "Settings")

    val showBars = adminMainScreens.any { currentRoute.startsWith(it) } &&
            !currentRoute.startsWith("add_category") &&
            !currentRoute.startsWith("edit_article")

    val topBarTitle = when (currentRoute) {
        "Dashboard" -> "Admin Dashboard"
        "News" -> "Manage News"
        "Categories" -> "Manage Categories"
        "Users" -> "Manage Users"
        "Settings" -> "Settings"
        "PrivacyPolicy" -> "Privacy Policy"
        "About" -> "About"
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
            composable("Dashboard") { AdminDashboardScreen() }

            composable("News") {
                ManageNewsScreen(
                    onEditArticleClick = { articleId -> adminNavController.navigate("edit_article/$articleId") }
                )
            }

            composable("edit_article/{articleId}", arguments = listOf(navArgument("articleId") { type = NavType.IntType })) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("articleId") ?: -1
                AdminEditArticleScreen(
                    articleId = articleId,
                    onBackClick = { adminNavController.popBackStack() },
                    onSaveClick = {
                        Toast.makeText(context, "Article Updated Successfully!", Toast.LENGTH_SHORT).show()
                        adminNavController.popBackStack()
                    }
                )
            }

            composable("Categories") {
                ManageCategoriesScreen(
                    onAddCategoryClick = { adminNavController.navigate("add_category") },
                    onEditCategoryClick = { categoryId -> adminNavController.navigate("add_category?categoryId=$categoryId") }
                )
            }

            composable("add_category?categoryId={categoryId}", arguments = listOf(navArgument("categoryId") { type = NavType.IntType; defaultValue = -1 })) { backStackEntry ->
                val argId = backStackEntry.arguments?.getInt("categoryId") ?: -1
                val finalId = if (argId == -1) null else argId
                AddANewCategoryScreen(
                    categoryId = finalId,
                    onBackClick = { adminNavController.popBackStack() },
                    onSubmitClick = {
                        val msg = if (finalId == null) "Category Added!" else "Category Updated!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        adminNavController.popBackStack()
                    }
                )
            }

            composable("Users") { ManageUsersScreen() }

            composable("Settings") {
                AdminSettingsScreen(
                    onLogout = onLogout,
                    onPrivacyClick = { adminNavController.navigate("PrivacyPolicy") },
                    onAboutClick = { adminNavController.navigate("About") }
                )
            }

            composable("PrivacyPolicy") {
                PrivacyPolicyScreen(
                    onNavigateBack = { adminNavController.popBackStack() }
                )
            }

            composable("About") {
                AboutScreen(
                    onNavigateBack = { adminNavController.popBackStack() }
                )
            }
        }
    }
}