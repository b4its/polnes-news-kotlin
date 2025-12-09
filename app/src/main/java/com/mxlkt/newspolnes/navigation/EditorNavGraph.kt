package com.mxlkt.newspolnes.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- IMPORTS PENTING ---
import com.mxlkt.newspolnes.components.EditorBottomNav
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.editor.EditorDashboardScreen
import com.mxlkt.newspolnes.ui.editor.EditorSettingsScreen
import com.mxlkt.newspolnes.ui.editor.YourArticleScreen
import com.mxlkt.newspolnes.ui.editor.AddANewArticleScreen
import com.mxlkt.newspolnes.ui.common.PrivacyPolicyScreen
import com.mxlkt.newspolnes.ui.common.AboutScreen
import com.mxlkt.newspolnes.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorNavGraph(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val editorNavController = rememberNavController()
    val context = LocalContext.current

    // --- SESSION MANAGER ---
    val sessionManager = remember { SessionManager(context) }
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // Cari data user untuk ditampilkan di Settings
    val currentUser = remember(loggedInUserId) {
        loggedInUserId?.let { id ->
            StoreData.userList.find { it.id == id && it.role == UserRole.EDITOR }
        }
    }

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
            composable("editor_dashboard") { EditorDashboardScreen() }

            // 2. YOUR ARTICLES
            composable("editor_articles") { YourArticleScreen(navController = editorNavController) }

            // 3. SETTINGS
            composable("editor_settings") {
                EditorSettingsScreen(
                    navController = editorNavController,
                    userName = currentUser?.name ?: "Editor",
                    userRole = currentUser?.role?.name ?: UserRole.EDITOR.name,
                    onLogout = onLogout,
                    onPrivacyClick = { editorNavController.navigate("PrivacyPolicy") },
                    onAboutClick = { editorNavController.navigate("About") }
                )
            }

            // 4. PRIVACY POLICY
            composable("PrivacyPolicy") {
                PrivacyPolicyScreen(onNavigateBack = { editorNavController.popBackStack() })
            }

            // 5. ABOUT
            composable("About") {
                AboutScreen(onNavigateBack = { editorNavController.popBackStack() })
            }

            // 6. ADD/EDIT ARTICLE (Route with Arguments)
            composable(
                route = "article_form?articleId={articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val argId = backStackEntry.arguments?.getInt("articleId") ?: -1
                val finalArticleId = if (argId == -1) null else argId

                AddANewArticleScreen(
                    articleId = finalArticleId,
                    onBackClick = { editorNavController.popBackStack() },

                    // --- LOGIKA REQUEST DELETE ---
                    onRequestDelete = { idToDelete ->
                        val index = StoreData.newsList.indexOfFirst { it.id == idToDelete }
                        if (index != -1) {
                            val oldArticle = StoreData.newsList[index]
                            // Ubah status jadi PENDING_DELETION
                            StoreData.newsList[index] = oldArticle.copy(status = NewsStatus.PENDING_DELETION)
                            Toast.makeText(context, "Request delete sent to Admin", Toast.LENGTH_SHORT).show()
                        }
                        editorNavController.popBackStack()
                    },

                    // --- LOGIKA SIMPAN DATA (SUBMIT) ---
                    onSubmitClick = { title, content, youtubeLink, categoryId, imageUri ->

                        // 1. Tentukan ID (Baru atau Lama)
                        val currentId = finalArticleId ?: (StoreData.newsList.maxOfOrNull { it.id }?.plus(1) ?: 1)

                        // 2. Tentukan Status
                        // Jika artikel baru -> PENDING_REVIEW
                        // Jika edit artikel lama -> PENDING_UPDATE
                        val newStatus = if (finalArticleId == null) NewsStatus.PENDING_REVIEW else NewsStatus.PENDING_UPDATE

                        // 3. Buat Object News sesuai Data Class Anda
                        val newArticle = News(
                            id = currentId,
                            title = title,
                            categoryId = categoryId ?: 1, // Default kategori 1 jika null

                            // Gunakan gambar bawaan Android agar tidak error jika tidak punya file 'news_placeholder'
                            // Nanti bisa diganti dengan logic upload URI yang sebenarnya
                            imageRes = android.R.drawable.ic_menu_gallery,

                            content = content,
                            authorId = loggedInUserId, // Mengambil ID dari SessionManager

                            // Field tambahan sesuai model baru Anda:
                            date = "Just now",
                            views = 0,
                            youtubeVideoId = youtubeLink,
                            status = newStatus
                        )

                        // 4. Simpan ke StoreData (List Lokal)
                        if (finalArticleId == null) {
                            // Tambah Baru
                            StoreData.newsList.add(newArticle)
                            Toast.makeText(context, "Article submitted for review!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Update Lama
                            val index = StoreData.newsList.indexOfFirst { it.id == finalArticleId }
                            if (index != -1) {
                                StoreData.newsList[index] = newArticle
                                Toast.makeText(context, "Changes submitted for approval!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Kembali ke layar sebelumnya
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