package com.mxlkt.newspolnes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.isSystemInDarkTheme
import com.mxlkt.newspolnes.model.Notification
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.ui.theme.NotifBackgroundDark
import com.mxlkt.newspolnes.ui.theme.NotifBackgroundLight
import com.mxlkt.newspolnes.ui.theme.NotifIconTintDark
import com.mxlkt.newspolnes.ui.theme.NotifIconTintLight

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit // 🟢 Callback untuk klik
) {
    val isDark = isSystemInDarkTheme()
    val iconBackground = if (isDark) NotifBackgroundDark else NotifBackgroundLight
    val iconTint = if (isDark) NotifIconTintDark else NotifIconTintLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Tanggal
        Text(
            text = notification.date,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        // Card Konten
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() } // 🟢 Memicu aksi klik
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = notification.iconRes),
                        contentDescription = "Notification Icon",
                        colorFilter = ColorFilter.tint(iconTint),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "New article from ${notification.category}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationCardPreview() {
    val sampleNotification = StoreData.notifications[0]
    NewsPolnesTheme {
        NotificationCard(
            notification = sampleNotification,
            onClick = {}
        )
    }
}