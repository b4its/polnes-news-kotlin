package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
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
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.AdminUserCard
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import kotlinx.coroutines.launch

@Composable
fun ManageUsersScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. State Data User
    val usersState = remember {
        mutableStateListOf<User>().apply {
            addAll(DummyData.userList.filter { it.role != UserRole.ADMIN })
        }
    }

    // 2. UI State & Pager
    val tabTitles = listOf("All", "User", "Editor")
    // 🟢 Pager State untuk swipe tabs
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

    var searchQuery by remember { mutableStateOf("") }

    // 3. State untuk Dialog Edit & Hapus
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToEdit by remember { mutableStateOf<User?>(null) }

    // --- DIALOG KONFIRMASI HAPUS ---
    if (userToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            onDismiss = { userToDelete = null },
            onConfirm = {
                usersState.remove(userToDelete)
                Toast.makeText(context, "User ${userToDelete?.name} deleted", Toast.LENGTH_SHORT).show()
                userToDelete = null
            }
        )
    }

    // --- DIALOG EDIT ROLE ---
    if (userToEdit != null) {
        EditUserRoleDialog(
            user = userToEdit!!,
            onDismiss = { userToEdit = null },
            onSave = { newRole ->
                val index = usersState.indexOfFirst { it.id == userToEdit!!.id }
                if (index != -1) {
                    val updatedUser = usersState[index].copy(role = newRole)
                    usersState[index] = updatedUser
                    Toast.makeText(context, "Role updated to ${newRole.name}", Toast.LENGTH_SHORT).show()
                }
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
        PaddingValues(16.dp).let { padding ->
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
        }

        // 🟢 HORIZONTAL PAGER
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

            if (displayedUsers.isEmpty()) {
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

@Composable
fun EditUserRoleDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Role for ${user.name}") },
        text = {
            Column {
                Text("Select new role:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(UserRole.USER, UserRole.EDITOR).forEach { role ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (role == selectedRole),
                                onClick = { selectedRole = role },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == selectedRole),
                            onClick = null
                        )
                        Text(
                            text = role.name,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedRole) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

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
                ManageUsersScreen()
            }
        }
    }
}