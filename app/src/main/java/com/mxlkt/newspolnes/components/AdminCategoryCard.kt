package com.mxlkt.newspolnes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.ui.theme.*

@Composable
fun AdminCategoryCard(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Lingkaran Inisial (Avatar Kategori) ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    // 🟢 GANTI BACKGROUND JADI HIJAU MUDA (Sama kayak User Card)
                    .background(Color(0xFFA3E5A6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    // 🟢 GANTI TEKS JADI HIJAU TUA (Sama kayak User Card)
                    color = Color(0xFF50AE5E)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- Nama Kategori ---
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // --- Tombol Edit ---
            FilledIconButton(
                onClick = onEditClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ActionEditBg,
                    contentColor = ActionEditIcon
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // --- Tombol Delete ---
            FilledIconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ActionDeleteBg,
                    contentColor = ActionDeleteIcon
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminCategoryCardPreview() {
    NewsPolnesTheme {
        val dummyCategory = Category(1, "Technology", "a")
        Box(modifier = Modifier.padding(16.dp)) {
            AdminCategoryCard(category = dummyCategory, onEditClick = {}, onDeleteClick = {})
        }
    }
}