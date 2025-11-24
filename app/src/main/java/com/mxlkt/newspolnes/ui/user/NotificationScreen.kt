package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.components.NotificationCard
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.model.DummyData

@Composable
fun NotificationsScreen(
    onNewsClick: (Int) -> Unit // 🟢 Menerima fungsi navigasi dari NavGraph
) {
    val notifications = DummyData.notifications

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onClick = {
                    // 🟢 Mengirim ID notifikasi (berita) ke NavGraph
                    onNewsClick(notification.id)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Tampilan Penuh (Full App)")
@Composable
private fun FullNotificationsScreenPreview() {
    Scaffold(
        topBar = { TitleOnlyTopAppBar(title = "Notifications") },
        bottomBar = { UserBottomNav(currentRoute = "Notifications", onItemClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NotificationsScreen(onNewsClick = {})
        }
    }
}