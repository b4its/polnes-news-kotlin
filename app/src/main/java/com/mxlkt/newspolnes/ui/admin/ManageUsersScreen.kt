package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.AdminUserCard
import com.mxlkt.newspolnes.model.UpdateUserRequest
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.view.AuthState
import com.mxlkt.newspolnes.view.AuthViewModel
import com.mxlkt.newspolnes.view.UserListState
import com.mxlkt.newspolnes.view.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageUsersScreen(
    userViewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State dari ViewModels ---
    val userListState by userViewModel.userListState
    val authState by authViewModel.authState

    // --- Local UI State ---
    val usersState = remember { mutableStateListOf<User>() }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Modal/Dialog
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToEdit by remember { mutableStateOf<User?>(null) }

    // 1. Initial Data Load
    LaunchedEffect(Unit) {
        userViewModel.fetchAllUsers()
    }

    // 2. Observer User List
    LaunchedEffect(userListState) {
        when (val state = userListState) {
            is UserListState.Loading -> isLoading = true
            is UserListState.Success -> {
                usersState.clear()
                usersState.addAll(state.users.filter { it.role != UserRole.ADMIN })
                isLoading = false
            }
            is UserListState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // 3. Observer Auth State
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.SuccessUpdate -> {
                Toast.makeText(context, "Data pengguna berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                userToEdit = null
                authViewModel.resetState()
                userViewModel.fetchAllUsers()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    // --- LOGIC UI UTAMA ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Layout
        val tabTitles = listOf("All", "User", "Editor")
        val pagerState = rememberPagerState(pageCount = { tabTitles.size })

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nama pengguna...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        // Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top // PERUBAHAN UTAMA: Agar konten rata atas (di bawah search bar)
        ) { page ->

            // Filter Logic
            val displayedUsers = remember(page, searchQuery, usersState.toList()) {
                val byRole = when (page) {
                    1 -> usersState.filter { it.role == UserRole.USER }
                    2 -> usersState.filter { it.role == UserRole.EDITOR }
                    else -> usersState
                }
                if (searchQuery.isBlank()) byRole else byRole.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            if (isLoading) {
                // Loading tetap di tengah layar agar terlihat jelas
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (displayedUsers.isEmpty()) {
                // Pesan kosong tetap di tengah layar
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada data pengguna.", color = Color.Gray) }
            } else {
                // List Data (Sekarang akan rata atas)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize() // Pastikan LazyColumn mengisi ruang yang tersedia
                ) {
                    items(displayedUsers) { user ->
                        AdminUserCard(
                            user = user,
                            onEditClick = { userToEdit = user },
                            onDeleteClick = { userToDelete = user }
                        )
                    }
                }
            }
        }
    }

    // --- MODAL / DIALOG SECTION ---

    // 1. Modal Delete
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah anda ingin menghapus akun ini? (${userToDelete?.name})") },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Fitur Delete belum diimplementasikan di API", Toast.LENGTH_SHORT).show()
                        usersState.remove(userToDelete)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Batal") }
            }
        )
    }

    // 2. Modal Edit
    if (userToEdit != null) {
        EditUserDialog(
            user = userToEdit!!,
            isLoading = authState is AuthState.Loading,
            onDismiss = { userToEdit = null },
            onSave = { request ->
                authViewModel.updateUser(userToEdit!!.id, request)
            }
        )
    }
}

// Komponen EditUserDialog tetap sama
@Composable
fun EditUserDialog(
    user: User,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (UpdateUserRequest) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(user.role) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Pengguna", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nama Lengkap") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password Baru") }, placeholder = { Text("Biarkan kosong jika tetap") },
                    singleLine = true, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Role Pengguna:", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == UserRole.USER, onClick = { selectedRole = UserRole.USER })
                        Text("User", modifier = Modifier.clickable { selectedRole = UserRole.USER })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = selectedRole == UserRole.EDITOR, onClick = { selectedRole = UserRole.EDITOR })
                        Text("Editor", modifier = Modifier.clickable { selectedRole = UserRole.EDITOR })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val request = UpdateUserRequest(
                                name = name, email = email,
                                password = if (password.isNotBlank()) password else null,
                                role = selectedRole.name
                            )
                            onSave(request)
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}