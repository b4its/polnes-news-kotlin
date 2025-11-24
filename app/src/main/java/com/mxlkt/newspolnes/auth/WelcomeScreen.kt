package com.mxlkt.newspolnes.auth

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.mxlkt.newspolnes.R
import androidx.compose.ui.graphics.ColorFilter
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme

@Composable
fun WelcomeScreen(
    // 'authNavController' untuk pindah layar di dalam flow auth (Welcome -> Login/SignUp).
    // 'rootNavController' untuk keluar dari flow auth (setelah login/daftar berhasil),
    // meski di layar ini kita belum pakai 'rootNavController'.
    rootNavController: NavHostController,
    authNavController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bagian Judul
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bagian Logo/Ilustrasi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .paint( // Kasih background doodle tipis-tipis
                        painter = painterResource(id = R.drawable.ic_doodle_background),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_polnes_news),
                    contentDescription = "Welcome Image",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(220.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }

            // Spacer ini sengaja untuk memberi jarak sebelum area tombol
            Spacer(modifier = Modifier.height(16.dp))

            // Bagian Tombol Aksi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    // Arahkan ke layar 'login' menggunakan NavController internal (auth)
                    onClick = { authNavController.navigate("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    // Arahkan ke layar 'signup' menggunakan NavController internal (auth)
                    onClick = { authNavController.navigate("signup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        // 'surface' biasanya adalah White di LightMode
                        containerColor = MaterialTheme.colorScheme.surface,
                        // 'primary' adalah warna hijau Polnes
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    // Siapkan NavController bohongan (dummy) agar preview bisa dirender
    val dummyNavController = rememberNavController()
    NewsPolnesTheme {
    WelcomeScreen(
        rootNavController = dummyNavController,
        authNavController = dummyNavController
    )
        }
}