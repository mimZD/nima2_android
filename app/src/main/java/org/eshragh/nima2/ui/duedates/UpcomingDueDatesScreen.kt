package org.eshragh.nima2.ui.duedates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.remote.model.extractThumbnailUrl
import org.eshragh.nima2.data.remote.model.extractUrl
import org.eshragh.nima2.data.remote.model.extractMimeType
import org.eshragh.nima2.data.repository.ServerKartablCard
import org.eshragh.nima2.ui.home.DestinationPathChips
import org.eshragh.nima2.ui.home.DueDateSelectorButton
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.home.getPlankaColor
import org.eshragh.nima2.ui.theme.BrandCyan
import org.eshragh.nima2.ui.theme.PrimaryBlue
import org.eshragh.nima2.ui.theme.PrimaryDarkBlue
import org.eshragh.nima2.util.JalaliCalendarHelper
import org.eshragh.nima2.util.NetworkUtils
import org.eshragh.nima2.util.toPersianDigits
import org.eshragh.nima2.util.FileUtils

private enum class DueCategory {
    OVERDUE, TODAY, UPCOMING
}

private data class ServerDueGroup(
    val category: DueCategory,
    val title: String,
    val color: Color,
    val cards: List<ServerKartablCard>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingDueDatesScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastSnackbarTime by remember { mutableStateOf(0L) }

    viewModel.userMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadServerKartablCards()
        // Reset to default tab when screen opens
        viewModel.kartablTab = viewModel.defaultKartablTabPreference
    }

    val todayJalali = remember { JalaliCalendarHelper.currentDateInJalali() }

    val dueGroups = remember(viewModel.serverKartablCards, todayJalali) {
        val serverCards = viewModel.serverKartablCards.sortedBy { it.dueDate }
        val overdue = mutableListOf<ServerKartablCard>()
        val today = mutableListOf<ServerKartablCard>()
        val upcoming = mutableListOf<ServerKartablCard>()

        for (card in serverCards) {
            val j = JalaliCalendarHelper.iso8601ToJalali(card.dueDate)
            if (j != null) {
                when {
                    j.year < todayJalali.year ||
                            (j.year == todayJalali.year && j.month < todayJalali.month) ||
                            (j.year == todayJalali.year && j.month == todayJalali.month && j.day < todayJalali.day) -> {
                        overdue.add(card)
                    }
                    j.year == todayJalali.year && j.month == todayJalali.month && j.day == todayJalali.day -> {
                        today.add(card)
                    }
                    else -> upcoming.add(card)
                }
            }
        }

        listOf(
            ServerDueGroup(DueCategory.OVERDUE, "منقضی‌شده (گذشته)", Color(0xFFEF4444), overdue),
            ServerDueGroup(DueCategory.TODAY, "امروز", Color(0xFFD97706), today),
            ServerDueGroup(DueCategory.UPCOMING, "روزهای آینده", Color(0xFF307A00), upcoming)
        ).filter { it.cards.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            Column {
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
                        title = { Text("کارتابل", fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                            }
                        },
                        actions = {
                            viewModel.lastSyncTimeState?.let { time ->
                                Text(
                                    text = "بروزرسانی: ${time.toPersianDigits()}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        }
                    )
                }
                
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SecondaryTabRow(
                        selectedTabIndex = viewModel.kartablTab,
                        containerColor = Color.White,
                        contentColor = PrimaryBlue,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(viewModel.kartablTab),
                                color = PrimaryBlue
                            )
                        }
                    ) {
                        Tab(
                            selected = viewModel.kartablTab == 0,
                            onClick = { viewModel.kartablTab = 0 },
                            text = { Text("مهلت‌های انجام", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = viewModel.kartablTab == 1,
                            onClick = { viewModel.kartablTab = 1 },
                            text = { Text("نمایش لیست", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = viewModel.kartablTab == 2,
                            onClick = { viewModel.kartablTab = 2 },
                            text = { Text("جستجو", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        var isRefreshing by remember { mutableStateOf(false) }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    if (viewModel.kartablTab == 0) viewModel.loadServerKartablCards()
                    else if (viewModel.kartablTab == 1) viewModel.viewListSelectedList?.let { viewModel.loadFullListCards(it.id) }
                    else viewModel.loadServerKartablCards()
                    kotlinx.coroutines.delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (viewModel.kartablTab) {
                    0 -> DueDatesTabContent(viewModel, dueGroups, lastSnackbarTime, { lastSnackbarTime = it }, snackbarHostState, scope)
                    1 -> ViewListTabContent(viewModel, lastSnackbarTime, { lastSnackbarTime = it }, snackbarHostState, scope)
                    2 -> SearchTabContent(viewModel, lastSnackbarTime, { lastSnackbarTime = it }, snackbarHostState, scope)
                }
            }
        }
    }

    // Delete Server Card Dialog
    viewModel.serverCardToDelete?.let { card ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { viewModel.serverCardToDeleteId = null },
                title = {
                    Text("حذف کارت از سرور", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                },
                text = { Text("آیا از حذف این کارت از سرور Planka اطمینان دارید؟") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteServerCard(card) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("حذف کارت", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.serverCardToDeleteId = null }) { Text("انصراف") }
                }
            )
        }
    }

    // Edit Server Card Dialog
    val context = LocalContext.current
    viewModel.serverCardToEdit?.let { card ->
        var editTitleText by remember(card) { mutableStateOf(card.name) }
        var editDueDateISO by remember(card) { mutableStateOf<String?>(card.dueDate) }
        var showEditDatePicker by remember(card) { mutableStateOf(false) }

        var selProj by remember(card) {
            mutableStateOf<org.eshragh.nima2.data.remote.model.PlankaProject?>(
                viewModel.allProjects.find { it.id == card.projectId }
                ?: org.eshragh.nima2.data.remote.model.PlankaProject(card.projectId, card.projectName)
            )
        }
        var selBoard by remember(card) {
            mutableStateOf<org.eshragh.nima2.data.remote.model.PlankaBoard?>(
                viewModel.allBoards.find { it.id == card.boardId }
                ?: org.eshragh.nima2.data.remote.model.PlankaBoard(card.boardId, card.projectId, card.boardName)
            )
        }
        val listsForSelectedBoard = remember(selBoard, viewModel.boardListsMap) {
            val bId = selBoard?.id
            val lists = if (bId != null) viewModel.boardListsMap[bId] ?: emptyList() else emptyList()
            lists
        }
        var selList by remember(card, listsForSelectedBoard) {
            mutableStateOf<org.eshragh.nima2.data.remote.model.PlankaList?>(
                listsForSelectedBoard.find { it.id == card.listId }
                ?: org.eshragh.nima2.data.remote.model.PlankaList(card.listId, card.boardId, card.listName)
            )
        }

        var showServerProjectPicker by remember { mutableStateOf(false) }
        var showServerBoardPicker by remember { mutableStateOf(false) }
        var showServerListPicker by remember { mutableStateOf(false) }
        var editLabelIds by remember(card) { mutableStateOf(card.labels.map { it.id }.toSet()) }

        val availableForBoard = remember(selBoard, viewModel.boardLabelsMap) {
            selBoard?.id?.let { viewModel.boardLabelsMap[it] } ?: emptyList()
        }

        LaunchedEffect(selBoard?.id) {
            selBoard?.id?.let { if (!viewModel.boardListsMap.containsKey(it)) viewModel.fetchListsForBoardId(it) }
        }

        if (showEditDatePicker) {
            org.eshragh.nima2.ui.composable.PersianDatePickerDialog(
                initialIsoDate = editDueDateISO,
                onDateSelected = { editDueDateISO = it; showEditDatePicker = false },
                onDismiss = { showEditDatePicker = false }
            )
        }

        if (showServerProjectPicker) {
            org.eshragh.nima2.ui.home.OptionPickerDialog(
                title = "انتخاب پروژه",
                options = viewModel.allProjects.map { it.name },
                selectedOption = selProj?.name,
                onOptionSelected = { index -> selProj = viewModel.allProjects[index]; selBoard = null; selList = null; showServerProjectPicker = false },
                onDismiss = { showServerProjectPicker = false }
            )
        }

        val availableBoardsForProj = remember(selProj) {
            selProj?.let { p -> viewModel.allBoards.filter { it.projectId == p.id } } ?: viewModel.allBoards
        }

        if (showServerBoardPicker) {
            org.eshragh.nima2.ui.home.OptionPickerDialog(
                title = "انتخاب بورد",
                options = availableBoardsForProj.map { it.name },
                selectedOption = selBoard?.name,
                onOptionSelected = { index -> val b = availableBoardsForProj[index]; selBoard = b; selList = null; showServerBoardPicker = false; viewModel.selectBoard(b) },
                onDismiss = { showServerBoardPicker = false }
            )
        }

        if (showServerListPicker) {
            org.eshragh.nima2.ui.home.OptionPickerDialog(
                title = "انتخاب لیست",
                options = listsForSelectedBoard.map { it.name ?: "بدون نام" },
                selectedOption = selList?.name,
                onOptionSelected = { index -> selList = listsForSelectedBoard[index]; showServerListPicker = false },
                onDismiss = { showServerListPicker = false }
            )
        }

        val isServerEditValid = selProj != null && selBoard != null && selList != null && editTitleText.trim().isNotEmpty()

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { viewModel.serverCardToEditId = null },
                title = { Text("ویرایش کارت در سرور", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                text = {
                    Column {
                        org.eshragh.nima2.ui.home.EditCardDestinationSelector(
                            selectedProject = selProj, selectedBoard = selBoard, selectedList = selList,
                            onSelectProjectClick = { showServerProjectPicker = true },
                            onSelectBoardClick = { showServerBoardPicker = true },
                            onSelectListClick = { showServerListPicker = true }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editTitleText, onValueChange = { editTitleText = it },
                            label = { Text("عنوان جدید کارت") }, singleLine = false, maxLines = 4,
                            textStyle = TextStyle(textDirection = TextDirection.Rtl, textAlign = TextAlign.Right),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DueDateSelectorButton(dueDateIso = editDueDateISO, onClick = { showEditDatePicker = true }, onClear = { editDueDateISO = null })
                        
                        // Server Attachments Section
                        val serverAttachments = viewModel.boardAttachmentsMap[card.boardId]?.filter { it.cardId == card.id } ?: emptyList()
                        if (serverAttachments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("ضمیمه‌های سرور:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            serverAttachments.forEach { att ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                        .clickable { 
                                            val fileUrl = att.extractUrl()
                                            android.util.Log.d("NIMA2_NETWORK", "User clicked attachment: ${att.name} | URL: $fileUrl")
                                            if (fileUrl != null) {
                                                viewModel.openServerFile(fileUrl, att.name, context)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val thumbUrl = att.extractThumbnailUrl()
                                                
                                                if (thumbUrl != null) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(viewModel.cardRepository.getFullUrlSync(thumbUrl))
                                                            .crossfade(true)
                                                            .build(),
                                                        imageLoader = viewModel.cardRepository.getImageLoader(),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        error = androidx.compose.ui.graphics.painter.ColorPainter(Color.LightGray)
                                                    )
                                                } else {
                                                    val fileIcon = FileUtils.getFileIcon(att.name, att.extractMimeType())
                                                    Icon(fileIcon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = att.name,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteServerAttachment(att.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        val serverFilePicker = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let { viewModel.addServerAttachment(card.id, it) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { serverFilePicker.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(org.eshragh.nima2.ui.home.AttachmentIcon, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("آپلود فایل به این کارت")
                        }

                        if (availableForBoard.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            org.eshragh.nima2.ui.home.LabelChipsSelector(
                                availableLabels = availableForBoard, selectedLabelIds = editLabelIds,
                                onLabelToggle = { id -> editLabelIds = if (editLabelIds.contains(id)) editLabelIds - id else editLabelIds + id }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateServerCard(
                                card, editTitleText, selList?.id, selList?.name, selBoard?.id, selBoard?.name,
                                selProj?.id, selProj?.name, editDueDateISO, editLabelIds
                            )
                        },
                        enabled = isServerEditValid, shape = RoundedCornerShape(10.dp)
                    ) { Text("ذخیره تغییرات سرور", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.serverCardToEditId = null }) { Text("انصراف") }
                }
            )
        }
    }
}

@Composable
private fun DueDatesTabContent(
    viewModel: HomeViewModel,
    dueGroups: List<ServerDueGroup>,
    lastSnackbarTime: Long,
    onSnackbarTimeChange: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    var isOverdueExpanded by remember { mutableStateOf(true) }
    var isUpcomingExpanded by remember { mutableStateOf(true) }

    if (viewModel.isFetchingKartabl && viewModel.serverKartablCards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("در حال دریافت کارت‌های سرور...", fontSize = 13.sp)
            }
        }
    } else {
        Column {
            if (viewModel.isFetchingKartabl) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            if (dueGroups.isEmpty() && !viewModel.isFetchingKartabl) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ کارتی با مهلت انجام در سرور یافت نشد")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    dueGroups.forEach { group ->
                        val isExpanded = when (group.category) {
                            DueCategory.OVERDUE -> isOverdueExpanded
                            DueCategory.TODAY -> true
                            DueCategory.UPCOMING -> isUpcomingExpanded
                        }
                        item {
                            Surface(
                                color = group.color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (group.category == DueCategory.OVERDUE) isOverdueExpanded = !isOverdueExpanded
                                    if (group.category == DueCategory.UPCOMING) isUpcomingExpanded = !isUpcomingExpanded
                                }
                            ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (group.category) {
                                                DueCategory.OVERDUE -> Icons.Default.Warning
                                                DueCategory.TODAY -> Icons.Default.Info
                                                DueCategory.UPCOMING -> Icons.Default.DateRange
                                            },
                                            contentDescription = null,
                                            tint = group.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${group.title} (${group.cards.size.toPersianDigits()})", fontWeight = FontWeight.Bold, color = group.color)
                                    }
                                    if (group.category != DueCategory.TODAY) {
                                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = group.color)
                                    }
                                }
                            }
                        }
                        if (isExpanded) {
                            items(group.cards, key = { it.id }) { card ->
                                SwipeableServerKartablCardItem(
                                    viewModel = viewModel,
                                    card = card,
                                    showPath = true,
                                    onDelete = { viewModel.serverCardToDeleteId = card.id },
                                    onEdit = { 
                                        viewModel.serverCardToEditId = card.id
                                    },
                                    onOfflineAction = {
                                        val now = System.currentTimeMillis()
                                        if (now - lastSnackbarTime > 2500) { onSnackbarTimeChange(now); scope.launch { snackbarHostState.showSnackbar("فقط در حالت آنلاین ممکن است") } }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewListTabContent(
    viewModel: HomeViewModel,
    lastSnackbarTime: Long,
    onSnackbarTimeChange: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    var showProjectPicker by remember { mutableStateOf(false) }
    var showBoardPicker by remember { mutableStateOf(false) }
    var showListPicker by remember { mutableStateOf(false) }

    val availableBoardsForProj = remember(viewModel.viewListSelectedProject) {
        viewModel.viewListSelectedProject?.let { p -> viewModel.allBoards.filter { it.projectId == p.id } } ?: emptyList()
    }
    val availableListsForBoard = remember(viewModel.viewListSelectedBoard, viewModel.boardListsMap) {
        viewModel.viewListSelectedBoard?.id?.let { viewModel.boardListsMap[it] } ?: emptyList()
    }

    if (showProjectPicker) org.eshragh.nima2.ui.home.OptionPickerDialog("انتخاب پروژه", viewModel.allProjects.map { it.name }, viewModel.viewListSelectedProject?.name, { viewModel.selectViewListProject(viewModel.allProjects[it]); showProjectPicker = false }, { showProjectPicker = false })
    if (showBoardPicker) org.eshragh.nima2.ui.home.OptionPickerDialog("انتخاب بورد", availableBoardsForProj.map { it.name }, viewModel.viewListSelectedBoard?.name, { viewModel.selectViewListBoard(availableBoardsForProj[it]); showBoardPicker = false }, { showBoardPicker = false })
    if (showListPicker) org.eshragh.nima2.ui.home.OptionPickerDialog("انتخاب لیست", availableListsForBoard.map { it.name ?: "بدون نام" }, viewModel.viewListSelectedList?.name, { viewModel.selectViewList(availableListsForBoard[it]); showListPicker = false }, { showListPicker = false })

    Column(modifier = Modifier.fillMaxSize()) {
        org.eshragh.nima2.ui.home.EditCardDestinationSelector(
            viewModel.viewListSelectedProject, viewModel.viewListSelectedBoard, viewModel.viewListSelectedList,
            { showProjectPicker = true }, { showBoardPicker = true }, { showListPicker = true }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.viewListSelectedList == null) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لطفاً یک لیست را انتخاب کنید") }
        else if (viewModel.isFetchingViewList && viewModel.viewListCards.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        else {
            if (viewModel.isFetchingViewList) LinearProgressIndicator(Modifier.fillMaxWidth().padding(bottom = 8.dp))
            if (viewModel.viewListCards.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("این لیست خالی است") }
            else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.viewListCards, key = { it.id }) { card ->
                        SwipeableServerKartablCardItem(
                            viewModel = viewModel,
                            card = card,
                            showPath = false,
                            onDelete = { viewModel.serverCardToDeleteId = card.id },
                            onEdit = {
                                viewModel.serverCardToEditId = card.id
                            },
                            onOfflineAction = {
                                val now = System.currentTimeMillis()
                                if (now - lastSnackbarTime > 2500) { onSnackbarTimeChange(now); scope.launch { snackbarHostState.showSnackbar("فقط در حالت آنلاین ممکن است") } }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTabContent(
    viewModel: HomeViewModel,
    lastSnackbarTime: Long,
    onSnackbarTimeChange: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val offlineCards by viewModel.offlineCards.collectAsState()
    val serverCards = viewModel.serverKartablCards
    
    val filteredCards = remember(viewModel.searchQuery, offlineCards, serverCards) {
        val query = viewModel.searchQuery.trim().lowercase()
        if (query.isEmpty()) emptyList<Any>()
        else {
            val results = mutableListOf<Any>()
            // Search in offline cards
            results.addAll(offlineCards.filter { it.title.lowercase().contains(query) })
            // Search in server cards
            results.addAll(serverCards.filter { it.name.lowercase().contains(query) })
            results
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("جستجو در کارت‌ها...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery = "" }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (viewModel.searchQuery.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("عبارتی را برای جستجو وارد کنید", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (filteredCards.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("هیچ نتیجه‌ای یافت نشد", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredCards) { item ->
                    when (item) {
                        is OfflineCard -> {
                            org.eshragh.nima2.ui.home.OfflineCardItem(card = item, isAnySyncing = false, onSyncedAnimationFinished = {})
                        }
                        is ServerKartablCard -> {
                            SwipeableServerKartablCardItem(
                                viewModel = viewModel,
                                card = item,
                                showPath = true,
                                onDelete = { viewModel.serverCardToDeleteId = item.id },
                                onEdit = { viewModel.serverCardToEditId = item.id },
                                onOfflineAction = {
                                    val now = System.currentTimeMillis()
                                    if (now - lastSnackbarTime > 2500) { onSnackbarTimeChange(now); scope.launch { snackbarHostState.showSnackbar("فقط در حالت آنلاین ممکن است") } }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableServerKartablCardItem(
    viewModel: HomeViewModel,
    card: ServerKartablCard,
    showPath: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onOfflineAction: () -> Unit
) {
    val context = LocalContext.current
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.Settled) return@rememberSwipeToDismissBoxState false

            if (!NetworkUtils.isNetworkAvailable(context)) {
                onOfflineAction()
                return@rememberSwipeToDismissBoxState false
            }
            
            // FINAL RTL CORRECTION:
            // Swiping LEFT (hand moves Right -> Left) is StartToEnd
            // Swiping RIGHT (hand moves Left -> Right) is EndToStart
            
            val action = if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                // Visual LEFT
                viewModel.leftSwipeActionPreference
            } else {
                // Visual RIGHT
                viewModel.rightSwipeActionPreference
            }
            
            android.util.Log.d("NIMA2_EDIT_DEBUG", "Swipe confirmed! Value: $dismissValue, Action: $action")
            
            when (action) {
                0 -> onEdit()
                1 -> onDelete()
                2 -> viewModel.quickMoveServerCard(card)
            }
            false // always snap back
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) {
                val isVisualLeft = direction == SwipeToDismissBoxValue.StartToEnd
                val action = if (isVisualLeft) viewModel.leftSwipeActionPreference else viewModel.rightSwipeActionPreference
                
                val config = when (action) {
                    0 -> ActionConfig(MaterialTheme.colorScheme.primaryContainer, Icons.Default.Edit, "ویرایش", MaterialTheme.colorScheme.onPrimaryContainer)
                    1 -> ActionConfig(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, "حذف", MaterialTheme.colorScheme.onErrorContainer)
                    else -> ActionConfig(Color(0xFFE0FBF7), Icons.Default.Send, "انتقال سریع", Color(0xFF00858A))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(config.bgColor)
                        .padding(horizontal = 16.dp),
                    // In RTL: CenterStart is RIGHT, CenterEnd is LEFT
                    // If card moves LEFT (revealing RIGHT side), we show action on the RIGHT (CenterStart)
                    // If card moves RIGHT (revealing LEFT side), we show action on the LEFT (CenterEnd)
                    contentAlignment = if (isVisualLeft) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isVisualLeft) {
                            Text(config.label, fontWeight = FontWeight.Bold, color = config.textColor)
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = config.icon, 
                            contentDescription = null, 
                            tint = config.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                        if (isVisualLeft) {
                            Spacer(Modifier.width(8.dp))
                            Text(config.label, fontWeight = FontWeight.Bold, color = config.textColor)
                        }
                    }
                }
            }
        }
    ) { ServerKartablCardItem(viewModel, card, showPath, onEdit, onDelete, onOfflineAction) }
}

private data class ActionConfig(
    val bgColor: Color,
    val icon: ImageVector,
    val label: String,
    val textColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerKartablCardItem(
    viewModel: HomeViewModel,
    card: ServerKartablCard,
    showPath: Boolean = true,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onOfflineAction: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    val hasDueDate = !card.dueDate.isNullOrBlank()
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = card.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            style = TextStyle(
                                textAlign = TextAlign.Right,
                                textIndent = if (hasDueDate) TextIndent(firstLine = 85.sp) else TextIndent.None
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (card.labels.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            card.labels.forEach {
                                Surface(shape = RoundedCornerShape(6.dp), color = getPlankaColor(it.color, MaterialTheme.colorScheme.primaryContainer), contentColor = Color.White) {
                                    Text(it.name ?: "برچسب", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                    if (showPath) {
                        val pathString = "${card.projectName} ← ${card.boardName} ← ${card.listName}"
                        if (pathString.length > 5) {
                            Spacer(Modifier.height(6.dp))
                            DestinationPathChips(pathString)
                        }
                    }
                }

                val jalali = JalaliCalendarHelper.iso8601ToJalali(card.dueDate)
                if (jalali != null) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 10.dp, topEnd = 14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = jalali.toShortPersianDisplay().toPersianDigits(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (card.attachmentCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 10.dp, end = 12.dp)
                    ) {
                        Text(
                            text = card.attachmentCount.toPersianDigits(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = org.eshragh.nima2.ui.home.AttachmentIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // Show all 3 actions in the long-press menu for convenience
            DropdownMenuItem(
                text = { Text("ویرایش") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    if (!NetworkUtils.isNetworkAvailable(context)) onOfflineAction()
                    else onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("حذف") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    showMenu = false
                    if (!NetworkUtils.isNetworkAvailable(context)) onOfflineAction()
                    else onDelete()
                }
            )
            DropdownMenuItem(
                text = { Text("انتقال سریع") },
                leadingIcon = { Icon(Icons.Default.Send, contentDescription = null) },
                onClick = {
                    showMenu = false
                    if (!NetworkUtils.isNetworkAvailable(context)) onOfflineAction()
                    else viewModel.quickMoveServerCard(card)
                }
            )
        }
    }
}
