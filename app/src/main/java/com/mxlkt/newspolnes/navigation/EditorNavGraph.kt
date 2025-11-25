package com.mxlkt.newspolnes.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.* // � Import ini penting untuk remember dan collectAsState
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
import com.mxlkt.newspolnes.model.StoreData // � Tambahkan import untuk StoreData
import com.mxlkt.newspolnes.model.UserRole // � Tambahkan import untuk UserRole
import com.mxlkt.newspolnes.ui.editor.EditorDashboardScreen
import com.mxlkt.newspolnes.ui.editor.EditorSettingsScreen
import com.mxlkt.newspolnes.ui.editor.YourArticleScreen
import com.mxlkt.newspolnes.ui.editor.AddANewArticleScreen
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen
import com.mxlkt.newspolnes.ui.common.AboutScreen
import com.mxlkt.newspolnes.utils.SessionManager // � Tambahkan import untuk SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorNavGraph(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val editorNavController = rememberNavController()
    val context = LocalContext.current

    // � LOGIKA PENGAMBILAN DATA SESI (SessionManager)
    val sessionManager = remember { SessionManager(context) }
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // Cari objek User yang sesuai dengan ID
    val currentUser = remember(loggedInUserId) {
        loggedInUserId?.let { id ->
            StoreData.userList.find { it.id == id && it.role == UserRole.EDITOR }
        }
    }
    // ------------------------------------------------

    val navBackStackEntry by editorNavController.currentBackStackEntryAsState()
    val fullRoute = navBackStackEntry?.destination?.route
    val startDestinationRoute = "editor_dashboard"
    val currentRoute = fullRoute?.substringBefore("?") ?: startDestinationRoute

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
            startDestination = startDestinationRoute,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            // 1. DASHBOARD
            composable("editor_dashboard") {
                EditorDashboardScreen()
            }

            // 2. YOUR ARTICLES
            composable("editor_articles") {
                YourArticleScreen(navController = editorNavController)
            }

            // 3. SETTINGS
            composable("editor_settings") {
                EditorSettingsScreen(
                    navController = editorNavController,
                    // � PERBAIKAN: Oper data pengguna yang sudah dikoleksi
                    userName = currentUser?.name ?: "Editor",
                    userRole = currentUser?.role?.name ?: UserRole.EDITOR.name, // Gunakan nama role default jika kosong

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
        "PrivacyPolicy" -> "Privacy Policy"
        "About" -> "About"
        else -> ""
    }
}