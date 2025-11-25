package com.mxlkt.newspolnes.ui.admin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.AdminUserCard
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.view.UpdateRoleState // DITAMBAHKAN
import com.mxlkt.newspolnes.view.UserListState
import com.mxlkt.newspolnes.view.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageUsersScreen(viewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


// 1. Ambil state dari ViewModel
    val userListState by viewModel.userListState
    // � State BARU untuk Update Role
    val updateRoleState by viewModel.updateRoleState

    // 2. State Compose lokal untuk menampilkan data yang difilter
    val usersState = remember { mutableStateListOf<User>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToEdit by remember { mutableStateOf<User?>(null) } // Pengguna yang akan diedit

    // 3. � Pemicu Pemuatan Data (Executed Once)
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    // 4. �️ Pengamatan User List State (Memuat data awal/refresh)
    LaunchedEffect(userListState) {
        when (val state = userListState) {
            is UserListState.Loading -> {
                isLoading = true
            }

            is UserListState.Success -> {
                // KOSONGKAN dan ISI ULANG usersState
                usersState.clear()
                usersState.addAll(
                    state.users
                        .filter { it.role != UserRole.ADMIN } // Filter ADMIN
                )
                isLoading = false
            }

            is UserListState.Error -> {
                isLoading = false
                Log.e("UserList", "Gagal memuat pengguna: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }

            UserListState.Idle -> { /* Biarkan idle */ }
        }
    }

    // 5. � Pengamatan Update Role State (Memproses hasil update role)
    LaunchedEffect(updateRoleState) {
        when (val state = updateRoleState) {
            is UpdateRoleState.Success -> {
                // 1. Tampilkan notifikasi
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()

                // 2. Update list lokal secara reaktif
                val updatedUser = state.user
                val index = usersState.indexOfFirst { it.id == updatedUser.id }
                if (index != -1) {
                    // Cek jika role baru bukan ADMIN (karena list ini difilter)
                    if (updatedUser.role != UserRole.ADMIN) {
                        usersState[index] = updatedUser // Ganti objek lama dengan yang baru
                    } else {
                        usersState.removeAt(index) // Hapus dari list jika role diubah menjadi ADMIN
                    }
                } else if (updatedUser.role != UserRole.ADMIN) {
                    // Kasus jarang: Jika user belum ada di list tapi berhasil diupdate (misal baru dibuat), tambahkan.
                    usersState.add(updatedUser)
                }

                // 3. Reset state agar tidak terpicu lagi
                viewModel.resetUpdateRoleState()

            }
            is UpdateRoleState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateRoleState()
            }
            is UpdateRoleState.Loading -> {
                // Opsional: Tampilkan loading global jika perlu
            }
            UpdateRoleState.Idle -> { /* Biarkan idle */ }
        }
    }


    // 2. UI State & Pager
    val tabTitles = listOf("All", "User", "Editor")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })


    // --- DIALOG KONFIRMASI HAPUS ---
    if (userToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            onDismiss = { userToDelete = null },
            onConfirm = {
                usersState.remove(userToDelete)
                Toast.makeText(context, "User ${userToDelete?.name} deleted (Local)", Toast.LENGTH_SHORT).show()
                userToDelete = null
                // TODO: Panggil API DELETE di sini
            }
        )
    }

    // --- DIALOG EDIT ROLE (Diperbaiki untuk memanggil ViewModel) ---
    if (userToEdit != null) {
        // Cek status loading dari operasi update role
        val isUpdating = updateRoleState is UpdateRoleState.Loading

        EditUserRoleDialog(
            user = userToEdit!!,
            onDismiss = { userToEdit = null },
            isUpdating = isUpdating, // Kirim status loading ke dialog
            onSave = { targetRole ->
                // Jika role sama, batalkan operasi
                if (targetRole == userToEdit!!.role) {
                    Toast.makeText(context, "Role tidak berubah.", Toast.LENGTH_SHORT).show()
                    userToEdit = null
                    return@EditUserRoleDialog
                }

                // Panggil fungsi update role di ViewModel
                // Karena route Laravel hanya untuk EDITOR, kita panggil ini
                viewModel.updateRole(userToEdit!!.id)

                // Tutup dialog (Status SUCCESS/ERROR akan ditangani oleh LaunchedEffect di atas)
                userToEdit = null
            }
        )
    }

    // --- LAYOUT UTAMA ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Search Bar (Global untuk semua tab)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // � HORIZONTAL PAGER
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->

            // Logika Filter per Halaman (Page)
            val displayedUsers = remember(page, searchQuery, usersState.toList()) {
                // 1. Filter Role
                val byRole = when (page) {
                    0 -> usersState // All
                    1 -> usersState.filter { it.role == UserRole.USER } // User only
                    2 -> usersState.filter { it.role == UserRole.EDITOR } // Editor only
                    else -> usersState
                }
                // 2. Filter Search
                if (searchQuery.isBlank()) byRole
                else byRole.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }
            }

            if (isLoading && usersState.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (displayedUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedUsers) { user ->
                        AdminUserCard(
                            user = user,
                            onEditClick = { userToEdit = user },
                            onDeleteClick = { userToDelete = user }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// EditUserRoleDialog Disesuaikan
// -------------------------------------------------------------------------------------------------

@Composable
fun EditUserRoleDialog(
    user: User,
    onDismiss: () -> Unit,
    isUpdating: Boolean, // Status loading BARU
    onSave: (UserRole) -> Unit
) {
    // Role target berdasarkan endpoint yang hanya mengubah ke EDITOR
    val targetRole = UserRole.EDITOR
    // Tombol hanya aktif jika role saat ini adalah USER dan tidak sedang loading
    val isUserAndNotUpdating = user.role == UserRole.USER && !isUpdating

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Role ${user.name}") },
        text = {
            Column {
                Text("Role saat ini:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = user.role.name,
                    fontWeight = FontWeight.Bold,
                    color = if (user.role == UserRole.EDITOR) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (user.role == targetRole) {
                    Text("Pengguna ini sudah menjadi ${targetRole.name}.", color = Color.Gray)
                } else if (user.role == UserRole.ADMIN) {
                    Text("Role ADMIN tidak dapat diubah di sini.", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Aksi yang tersedia:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tingkatkan role menjadi ${targetRole.name}")
                }

                if (isUpdating) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Memperbarui role...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(targetRole) },
                enabled = isUserAndNotUpdating
            ) {
                if (isUpdating) {
                    Text("Loading...")
                } else {
                    Text(if (user.role == targetRole) "Sudah EDITOR" else "Upgrade ke EDITOR")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text("Batal")
            }
        }
    )
}

// -------------------------------------------------------------------------------------------------
// Preview
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ManageUsersFullPreview() {
    NewsPolnesTheme {
        Scaffold(
            topBar = { TitleOnlyTopAppBar(title = "Manage Users") },
            bottomBar = { AdminBottomNav(currentRoute = "Users", onItemClick = {}) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Di Preview, kita menggunakan ViewModel dummy atau tidak memanggil API
                ManageUsersScreen()
            }
        }
    }
}