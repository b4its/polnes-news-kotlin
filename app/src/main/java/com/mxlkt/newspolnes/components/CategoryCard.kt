package com.mxlkt.newspolnes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.R
// Import untuk Coil AsyncImage (Asumsi Anda menggunakan Coil)
import coil.compose.AsyncImage

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    // URL dasar untuk gambar kategori (Ganti jika perlu)
    val BASE_IMAGE_URL = "https://polnes-news.b4its.tech/public/"
    // URL Fallback/Default
    val FALLBACK_IMAGE_URL = "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"

    // Tentukan URL gambar
    val imageUrl = if (category.gambar.isNullOrEmpty() || category.gambar == "0") {
        FALLBACK_IMAGE_URL
    } else {
        "$BASE_IMAGE_URL${category.gambar}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Menggunakan tinggi tetap daripada aspectRatio agar lebih stabil
            .height(180.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        // Gambar background menggunakan Coil
        AsyncImage(
            // KOREKSI: Menggunakan variabel 'imageUrl' yang sudah diperiksa
            model = imageUrl,
            // KOREKSI: contentDescription seharusnya menggunakan nama kategori
            contentDescription = "Background for category ${category.name}",
            contentScale = ContentScale.Crop,
            // Fallback/Placeholder
            placeholder = painterResource(id = R.drawable.category_tech), // Pastikan drawable ada
            error = painterResource(id = R.drawable.category_economy), // Pastikan drawable ada
            modifier = Modifier.fillMaxSize()
        )

        // Overlay hitam semi transparan di bawah 25%
        // Ini menciptakan efek Vignette di bagian bawah untuk membuat teks lebih mudah dibaca
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Menggunakan tinggi tetap jika aspectRatio dihapus, atau tetap 25% tinggi Box
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                // Menggunakan Color.Black dengan alpha langsung, lebih sederhana daripada graphicsLayer
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Teks kategori di tengah overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                // KOREKSI: Menggunakan 'category.name'
                text = category.name,
                color = Color.White,
                fontSize = 18.sp, // Sedikit lebih besar agar lebih jelas
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCardPreview() {
    // Ambil data kategori pertama dari DummyData
    // Asumsi: categoryList berisi objek Category dengan properti name & gambar
    val sampleCategory = StoreData.categoryList.firstOrNull() ?: Category(id = 1, name = "Teknologi & Sains", gambar = "0")

    // Panggil CategoryCard dengan data itu
    CategoryCard(
        category = sampleCategory,
        onClick = {} // Biarkan kosong untuk preview
    )
}