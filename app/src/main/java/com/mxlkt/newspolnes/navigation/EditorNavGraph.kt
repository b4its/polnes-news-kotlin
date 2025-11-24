package com.mxlkt.newspolnes.navigation

import android.widget.Toast
// 🟢 Tambahkan Import Animasi
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mxlkt.newspolnes.components.EditorBottomNav
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.ui.editor.EditorDashboardScreen
import com.mxlkt.newspolnes.ui.editor.EditorSettingsScreen
import com.mxlkt.newspolnes.ui.editor.YourArticleScreen
import com.mxlkt.newspolnes.ui.editor.AddANewArticleScreen
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen
import com.mxlkt.newspolnes.ui.common.AboutScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorNavGraph(
    rootNavController: NavHostController,
    currentUser: User?,
    onLogout: () -> Unit
) {
    val editorNavController = rememberNavController()
    val context = LocalContext.current

    val navBackStackEntry by editorNavController.currentBackStackEntryAsState()
    val fullRoute = navBackStackEntry?.destination?.route
    val currentRoute = fullRoute?.substringBefore("?") ?: "editor_dashboard"

    val mainRoutes = listOf("editor_dashboard", "editor_articles", "editor_settings")
    val showMainBars = currentRoute in mainRoutes

    val title = getScreenTitle(currentRoute)

    Scaffold(
        topBar = {
            if (showMainBars) {
                TitleOnlyTopAppBar(title = title)
            }
        },
        bottomBar = {
            if (showMainBars) {
                EditorBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        editorNavController.navigate(route) {
                            popUpTo(editorNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = editorNavController,
            startDestination = "editor_dashboard",
            modifier = Modifier.padding(innerPadding),
            // 🟢 ANIMASI TRANSISI (Sama dengan UserNavGraph)
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            // 1. DASHBOARD
            composable("editor_dashboard") {
                EditorDashboardScreen(
                    currentRoute = "editor_dashboard",
                    onNavigate = { /* ... */ }
                )
            }

            // 2. YOUR ARTICLES
            composable("editor_articles") {
                YourArticleScreen(navController = editorNavController)
            }

            // 3. SETTINGS
            composable("editor_settings") {
                EditorSettingsScreen(
                    navController = editorNavController,
                    onLogout = onLogout,
                    onPrivacyClick = { editorNavController.navigate("PrivacyPolicy") },
                    onAboutClick = { editorNavController.navigate("About") }
                )
            }

            // 4. PRIVACY POLICY
            composable("PrivacyPolicy") {
                PrivacyPolicyScreen(
                    onNavigateBack = { editorNavController.popBackStack() }
                )
            }

            // 5. ABOUT
            composable("About") {
                AboutScreen(
                    onNavigateBack = { editorNavController.popBackStack() }
                )
            }

            // 6. ADD/EDIT ARTICLE
            composable(
                route = "article_form?articleId={articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val argId = backStackEntry.arguments?.getInt("articleId") ?: -1
                val finalArticleId = if (argId == -1) null else argId

                AddANewArticleScreen(
                    articleId = finalArticleId,
                    onBackClick = { editorNavController.popBackStack() },
                    onSubmitClick = {
                        Toast.makeText(context, "Article Submitted!", Toast.LENGTH_SHORT).show()
                        editorNavController.popBackStack()
                    }
                )
            }
        }
    }
}

private fun getScreenTitle(route: String): String {
    return when (route) {
        "editor_dashboard" -> "Dashboard"
        "editor_articles" -> "Your Articles"
        "editor_settings" -> "Settings"
        else -> ""
    }
}