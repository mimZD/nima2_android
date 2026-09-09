package org.eshragh.nima2.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eshragh.nima2.ui.home.ApartmentIcon
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.home.OptionPickerDialog
import org.eshragh.nima2.ui.theme.BrandCyan
import org.eshragh.nima2.ui.theme.PrimaryBlue
import org.eshragh.nima2.ui.theme.PrimaryDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var showQuickProjPicker by remember { mutableStateOf(false) }
    var showQuickBoardPicker by remember { mutableStateOf(false) }
    var showQuickListPicker by remember { mutableStateOf(false) }

    val availableBoardsForQuick = remember(viewModel.quickMoveSelectedProject, viewModel.allBoards) {
        viewModel.quickMoveSelectedProject?.let { p -> viewModel.allBoards.filter { it.projectId == p.id } } ?: emptyList()
    }
    val availableListsForQuick = remember(viewModel.quickMoveSelectedBoard, viewModel.boardListsMap) {
        viewModel.quickMoveSelectedBoard?.id?.let { viewModel.boardListsMap[it] } ?: emptyList()
    }

    if (showQuickProjPicker) {
        OptionPickerDialog(
            title = "انتخاب پروژه مقصد",
            options = viewModel.allProjects.map { it.name },
            selectedOption = viewModel.quickMoveSelectedProject?.name,
            onOptionSelected = { index ->
                val p = viewModel.allProjects[index]
                viewModel.quickMoveSelectedProject = p
                viewModel.quickMoveSelectedBoard = null
                viewModel.quickMoveSelectedList = null
                viewModel.saveQuickMoveTarget(p, null, null)
                showQuickProjPicker = false
            },
            onDismiss = { showQuickProjPicker = false }
        )
    }

    if (showQuickBoardPicker) {
        OptionPickerDialog(
            title = "انتخاب بورد مقصد",
            options = availableBoardsForQuick.map { it.name },
            selectedOption = viewModel.quickMoveSelectedBoard?.name,
            onOptionSelected = { index ->
                val b = availableBoardsForQuick[index]
                viewModel.quickMoveSelectedBoard = b
                viewModel.quickMoveSelectedList = null
                viewModel.saveQuickMoveTarget(viewModel.quickMoveSelectedProject, b, null)
                viewModel.fetchListsForBoardId(b.id)
                showQuickBoardPicker = false
            },
            onDismiss = { showQuickBoardPicker = false }
        )
    }

    if (showQuickListPicker) {
        OptionPickerDialog(
            title = "انتخاب لیست مقصد",
            options = availableListsForQuick.map { it.name ?: "بدون نام" },
            selectedOption = viewModel.quickMoveSelectedList?.name,
            onOptionSelected = { index ->
                val l = availableListsForQuick[index]
                viewModel.quickMoveSelectedList = l
                viewModel.saveQuickMoveTarget(viewModel.quickMoveSelectedProject, viewModel.quickMoveSelectedBoard, l)
                showQuickListPicker = false
            },
            onDismiss = { showQuickListPicker = false }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryDarkBlue, PrimaryBlue, BrandCyan)
                        )
                    )
            ) {
                TopAppBar(
                    title = { Text("تنظیمات برنامه", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section: Default Tab
                SettingsSection(title = "تب پیش‌فرض کارتابل") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.saveDefaultKartablTab(0) }) {
                        RadioButton(selected = viewModel.defaultKartablTabPreference == 0, onClick = { viewModel.saveDefaultKartablTab(0) })
                        Text("مهلت‌های انجام", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.saveDefaultKartablTab(1) }) {
                        RadioButton(selected = viewModel.defaultKartablTabPreference == 1, onClick = { viewModel.saveDefaultKartablTab(1) })
                        Text("نمایش لیست", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                // Section: Swipe Actions
                SettingsSection(title = "اکشن‌های کشیدن کارت در کارتابل") {
                    Text("کشیدن به راست:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    SwipeActionSelector(
                        selected = viewModel.rightSwipeActionPreference,
                        onSelect = { viewModel.saveSwipeActions(it, viewModel.leftSwipeActionPreference) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("کشیدن به چپ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    SwipeActionSelector(
                        selected = viewModel.leftSwipeActionPreference,
                        onSelect = { viewModel.saveSwipeActions(viewModel.rightSwipeActionPreference, it) }
                    )
                }

                // Section: Quick Move Destination
                val isQuickMoveActive = viewModel.rightSwipeActionPreference == 2 || viewModel.leftSwipeActionPreference == 2
                if (isQuickMoveActive) {
                    SettingsSection(title = "مقصد انتقال سریع (Quick Move)") {
                        Text("کارت‌ها با کشیدن به سمت مشخص شده، مستقیماً به این لیست منتقل می‌شوند:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        org.eshragh.nima2.ui.home.EditCardDestinationSelector(
                            selectedProject = viewModel.quickMoveSelectedProject,
                            selectedBoard = viewModel.quickMoveSelectedBoard,
                            selectedList = viewModel.quickMoveSelectedList,
                            onSelectProjectClick = { showQuickProjPicker = true },
                            onSelectBoardClick = { showQuickBoardPicker = true },
                            onSelectListClick = { showQuickListPicker = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SwipeActionSelector(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf("ویرایش", "حذف", "انتقال سریع")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            val isSelected = selected == index
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(index) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
