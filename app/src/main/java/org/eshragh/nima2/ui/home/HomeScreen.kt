package org.eshragh.nima2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.local.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit
) {
    val cards by viewModel.offlineCards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingCount = cards.count { it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED }

    viewModel.userMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نیما ۲ - ثبت آفلاین", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = viewModel::loadInitialDataAndRestoreSelections) {
                        Icon(Icons.Default.Refresh, contentDescription = "به‌روزرسانی داده‌ها")
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "خروج")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Target Selection Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "مقصد ثبت کارت",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (viewModel.isFetchingData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Selector Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Project Selector Button
                        AssistChip(
                            onClick = { viewModel.showProjectPicker = true },
                            label = {
                                Text(
                                    text = viewModel.selectedProject?.name ?: "پروژه",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Board Selector Button
                        AssistChip(
                            onClick = { viewModel.showBoardPicker = true },
                            label = {
                                Text(
                                    text = viewModel.selectedBoard?.name ?: "بورد",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            enabled = viewModel.selectedProject != null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // List Selector Button
                        AssistChip(
                            onClick = { viewModel.showListPicker = true },
                            label = {
                                Text(
                                    text = viewModel.selectedList?.name ?: "لیست",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                if (viewModel.isFetchingLists) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = viewModel.selectedBoard != null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selection Path Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val projName = viewModel.selectedProject?.name ?: "انتخاب‌نشده"
                            val boardName = viewModel.selectedBoard?.name ?: "انتخاب‌نشده"
                            val listName = viewModel.selectedList?.name ?: "انتخاب‌نشده"
                            Text(
                                text = "$projName > $boardName > $listName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Card Form
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "افزودن کارت آفلاین",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.cardTitle,
                        onValueChange = viewModel::onCardTitleChange,
                        label = { Text("عنوان کارت") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = viewModel::addCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت کارت آفلاین", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload & Clear Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = viewModel::uploadCards,
                    enabled = !viewModel.isUploading && pendingCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (viewModel.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال آپلود...")
                    } else {
                        Text("آپلود کارت‌ها ($pendingCount در انتظار)", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = viewModel::clearSynced,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پاک‌سازی", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Offline Cards List
            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ کارتی ثبت نشده است",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        OfflineCardItem(
                            card = card,
                            onDelete = { viewModel.deleteCard(card) }
                        )
                    }
                }
            }
        }
    }

    // Pickers Dialogs
    if (viewModel.showProjectPicker) {
        OptionPickerDialog(
            title = "انتخاب پروژه",
            options = viewModel.allProjects.map { it.name },
            selectedOption = viewModel.selectedProject?.name,
            onOptionSelected = { index ->
                viewModel.selectProject(viewModel.allProjects[index])
            },
            onDismiss = { viewModel.showProjectPicker = false }
        )
    }

    if (viewModel.showBoardPicker) {
        OptionPickerDialog(
            title = "انتخاب بورد (${viewModel.selectedProject?.name ?: ""})",
            options = viewModel.availableBoards.map { it.name },
            selectedOption = viewModel.selectedBoard?.name,
            onOptionSelected = { index ->
                viewModel.selectBoard(viewModel.availableBoards[index])
            },
            onDismiss = { viewModel.showBoardPicker = false }
        )
    }

    if (viewModel.showListPicker) {
        OptionPickerDialog(
            title = "انتخاب لیست (${viewModel.selectedBoard?.name ?: ""})",
            options = viewModel.availableLists.map { it.name ?: "بدون نام" },
            selectedOption = viewModel.selectedList?.name,
            onOptionSelected = { index ->
                viewModel.selectList(viewModel.availableLists[index])
            },
            onDismiss = { viewModel.showListPicker = false }
        )
    }
}

@Composable
fun OptionPickerDialog(
    title: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            if (options.isEmpty()) {
                Text("هیچ گزینه‌ای یافت نشد.")
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, optionName ->
                        val isSelected = optionName == selectedOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(index) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onOptionSelected(index) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = optionName,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}

@Composable
fun OfflineCardItem(
    card: OfflineCard,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (!card.listName.isNullOrBlank()) {
                    Text(
                        text = card.listName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                card.errorMessage?.let { err ->
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusBadge(status = card.status)

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private data class StatusConfig(
    val bgColor: Color,
    val textColor: Color,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun StatusBadge(status: SyncStatus) {
    val config = when (status) {
        SyncStatus.PENDING -> StatusConfig(
            Color(0xFFFFF3CD),
            Color(0xFF856404),
            "در انتظار",
            Icons.Default.Info
        )
        SyncStatus.UPLOADING -> StatusConfig(
            Color(0xFFCCE5FF),
            Color(0xFF004085),
            "آپلود...",
            Icons.Default.Refresh
        )
        SyncStatus.SYNCED -> StatusConfig(
            Color(0xFFD4EDDA),
            Color(0xFF155724),
            "آپلود شد",
            Icons.Default.CheckCircle
        )
        SyncStatus.FAILED -> StatusConfig(
            Color(0xFFF8D7DA),
            Color(0xFF721C24),
            "خطا",
            Icons.Default.Warning
        )
    }

    Row(
        modifier = Modifier
            .background(config.bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = config.text,
            color = config.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
