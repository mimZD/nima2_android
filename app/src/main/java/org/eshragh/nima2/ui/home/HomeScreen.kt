package org.eshragh.nima2.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.eshragh.nima2.ui.theme.BrandCyan
import org.eshragh.nima2.ui.theme.PrimaryBlue
import org.eshragh.nima2.ui.theme.PrimaryDarkBlue
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import org.eshragh.nima2.R
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.local.SyncStatus
import org.eshragh.nima2.data.remote.model.PlankaBoard
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject
import org.eshragh.nima2.util.FileUtils
import org.eshragh.nima2.util.toPersianDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private var _apartmentIcon: ImageVector? = null
val ApartmentIcon: ImageVector
    get() {
        if (_apartmentIcon != null) return _apartmentIcon!!
        _apartmentIcon = ImageVector.Builder(
            name = "ApartmentIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(17f, 11f)
                lineTo(17f, 3f)
                lineTo(7f, 3f)
                lineTo(7f, 11f)
                lineTo(3f, 11f)
                lineTo(3f, 21f)
                lineTo(21f, 21f)
                lineTo(21f, 11f)
                lineTo(17f, 11f)
                close()
                moveTo(9f, 5f)
                lineTo(11f, 5f)
                lineTo(11f, 7f)
                lineTo(9f, 7f)
                lineTo(9f, 5f)
                close()
                moveTo(9f, 9f)
                lineTo(11f, 9f)
                lineTo(11f, 11f)
                lineTo(9f, 11f)
                lineTo(9f, 9f)
                close()
                moveTo(13f, 5f)
                lineTo(15f, 5f)
                lineTo(15f, 7f)
                lineTo(13f, 7f)
                lineTo(13f, 5f)
                close()
                moveTo(13f, 9f)
                lineTo(15f, 9f)
                lineTo(15f, 11f)
                lineTo(13f, 11f)
                lineTo(13f, 9f)
                close()
            }
        }.build()
        return _apartmentIcon!!
    }



private var _customUploadIcon: ImageVector? = null
val CustomUploadIcon: ImageVector
    get() {
        if (_customUploadIcon != null) return _customUploadIcon!!
        _customUploadIcon = ImageVector.Builder(
            name = "CustomUploadIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9f, 16f)
                lineTo(15f, 16f)
                lineTo(15f, 10f)
                lineTo(19f, 10f)
                lineTo(12f, 3f)
                lineTo(5f, 10f)
                lineTo(9f, 10f)
                close()
                moveTo(5f, 18f)
                lineTo(19f, 18f)
                lineTo(19f, 20f)
                lineTo(5f, 20f)
                close()
            }
        }.build()
        return _customUploadIcon!!
    }

private var _attachmentIcon: ImageVector? = null
val AttachmentIcon: ImageVector
    get() {
        if (_attachmentIcon != null) return _attachmentIcon!!
        _attachmentIcon = ImageVector.Builder(
            name = "AttachmentIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16.5f, 6f)
                verticalLineToRelative(11.5f)
                curveToRelative(0f, 2.21f, -1.79f, 4f, -4f, 4f)
                reflectiveCurveToRelative(-4f, -1.79f, -4f, -4f)
                verticalLineTo(5f)
                curveToRelative(0f, -1.38f, 1.12f, -2.5f, 2.5f, -2.5f)
                reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f)
                verticalLineToRelative(10.5f)
                curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
                reflectiveCurveToRelative(-1f, -0.45f, -1f, -1f)
                verticalLineTo(6f)
                horizontalLineTo(10f)
                verticalLineToRelative(9.5f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                verticalLineTo(5f)
                curveToRelative(0f, -2.48f, -2.02f, -4.5f, -4.5f, -4.5f)
                reflectiveCurveTo(7f, 2.52f, 7f, 5f)
                verticalLineToRelative(12.5f)
                curveToRelative(0f, 3.31f, 2.69f, 6f, 6f, 6f)
                reflectiveCurveToRelative(6f, -2.69f, 6f, -6f)
                verticalLineTo(6f)
                horizontalLineTo(16.5f)
                close()
            }
        }.build()
        return _attachmentIcon!!
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDueDates: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val cards = viewModel.offlineCardsState
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // THE ONLY PLACE where Share is handled
    LaunchedEffect(Unit) {
        org.eshragh.nima2.util.ShareManager.pendingShare.collect { data ->
            if (data != null && !viewModel.showAddCardDialog) {
                android.util.Log.d("NIMA2_SHARE", "[Step 0] HomeScreen UI detected pending data. Title: ${data.text?.take(10)}")
                viewModel.handleIncomingShare(data.text, data.uris)
                // We DO NOT consume yet. We will consume when the dialog is closed.
            }
        }
    }

    var isSpeedDialExpanded by remember { mutableStateOf(false) }
    val pendingCount = cards.count { it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED }
    val isAnySyncing = remember(cards) { cards.any { it.status == SyncStatus.UPLOADING || it.status == SyncStatus.SYNCED } }

    var isRefreshing by remember { mutableStateOf(false) }

    val todayJalali = remember { org.eshragh.nima2.util.JalaliCalendarHelper.currentDateInJalali() }
    val todayOrOverdueCount = remember(cards) {
        cards.count { card ->
            if (card.dueDate.isNullOrBlank()) false
            else {
                val j = org.eshragh.nima2.util.JalaliCalendarHelper.iso8601ToJalali(card.dueDate)
                if (j != null) {
                    (j.year < todayJalali.year) ||
                    (j.year == todayJalali.year && j.month < todayJalali.month) ||
                    (j.year == todayJalali.year && j.month == todayJalali.month && j.day <= todayJalali.day)
                } else false
            }
        }
    }

    viewModel.userMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        title = { Text("نیما ۲", fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "بیشتر")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("تنظیمات") },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            onOpenSettings()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("خروج از حساب") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.logout(onLogout)
                                        }
                                    )
                                }
                            }
                        },
                        actions = {
                            // Refresh button removed, using pull-to-refresh
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.loadInitialDataAndRestoreSelections()
                        delay(1000)
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
                    // Offline Cards List
                    if (cards.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "هیچ کارتی ثبت نشده است",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "برای ثبت کارت جدید روی دکمه + کلیک کنید",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cards, key = { it.id }) { card ->
                                AnimatedVisibility(
                                    visible = true,
                                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                                ) {
                                    SwipeableOfflineCardItem(
                                        card = card,
                                        isAnySyncing = isAnySyncing,
                                        onDelete = { viewModel.cardToDelete = card },
                                        onEdit = { viewModel.cardToEdit = card },
                                        onSyncedAnimationFinished = { viewModel.deleteCard(card) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Backdrop Scrim when Speed Dial is Expanded
        if (isSpeedDialExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isSpeedDialExpanded = false }
            )
        }

        // Speed Dial Arc Options & Main FAB Container
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary FAB: Kartabl (Clipboard Icon)
                BadgedBox(
                    badge = {
                        if (todayOrOverdueCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ) {
                                Text(todayOrOverdueCount.toPersianDigits(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    SmallFloatingActionButton(
                        onClick = onOpenDueDates,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "کارتابل",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Main Speed Dial Arc Options & FAB
                Box(contentAlignment = Alignment.BottomStart) {
                    val progress by animateFloatAsState(
                        targetValue = if (isSpeedDialExpanded) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "SpeedDialProgress"
                    )

                    val fabRotation by animateFloatAsState(
                        targetValue = if (isSpeedDialExpanded) 45f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "FabRotation"
                    )

                    if (progress > 0.01f) {
                        val radius = 110.dp.value
                        // 4 Arc Actions (Angles: 90°, 60°, 30°, 0°)
                        val actions = listOf(
                            Triple(CustomUploadIcon, "آپلود", 90f),
                            Triple(ApartmentIcon, viewModel.selectedProject?.name ?: "پروژه", 60f),
                            Triple(Icons.Default.Home, viewModel.selectedBoard?.name ?: "بورد", 30f),
                            Triple(Icons.Default.Menu, viewModel.selectedList?.name ?: "لیست", 0f)
                        )

                        actions.forEachIndexed { index, (icon, label, angleDeg) ->
                            val angleRad = Math.toRadians(angleDeg.toDouble())
                            val xOffset = (radius * cos(angleRad) * progress).dp
                            val yOffset = (-radius * sin(angleRad) * progress).dp

                            Row(
                                modifier = Modifier
                                    .offset(x = xOffset, y = yOffset)
                                    .scale(progress)
                                    .alpha(progress),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (index == 0) { // Upload Action with Badge only
                                    BadgedBox(
                                        badge = {
                                            if (pendingCount > 0) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError
                                                ) {
                                                    Text(pendingCount.toPersianDigits(), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    ) {
                                        SmallFloatingActionButton(
                                            onClick = {
                                                isSpeedDialExpanded = false
                                                viewModel.uploadCards()
                                            },
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        ) {
                                            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                } else {
                                    SmallFloatingActionButton(
                                        onClick = {
                                            // Do NOT close Speed Dial so user can select all 3
                                            when (index) {
                                                1 -> viewModel.showProjectPicker = true
                                                2 -> viewModel.showBoardPicker = true
                                                3 -> viewModel.showListPicker = true
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) {
                                        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Main FAB (Add Card on Tap, Speed Dial on Long Press)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .combinedClickable(
                                onClick = {
                                    if (isSpeedDialExpanded) {
                                        isSpeedDialExpanded = false
                                    } else {
                                        viewModel.showAddCardDialog = true
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSpeedDialExpanded = !isSpeedDialExpanded
                                }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "ثبت کارت / گزینه‌ها",
                                modifier = Modifier
                                    .size(26.dp)
                                    .rotate(fabRotation)
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (viewModel.showSettingsDialog) {
        SettingsDialog(
            defaultTab = viewModel.defaultKartablTabPreference,
            onSave = { tab ->
                viewModel.saveDefaultKartablTab(tab)
                viewModel.showSettingsDialog = false
            },
            onDismiss = { viewModel.showSettingsDialog = false }
        )
    }

    if (viewModel.showDatePickerDialog) {
        org.eshragh.nima2.ui.composable.PersianDatePickerDialog(
            initialIsoDate = viewModel.selectedDueDateISO,
            onDateSelected = { iso ->
                viewModel.selectedDueDateISO = iso
                viewModel.showDatePickerDialog = false
            },
            onDismiss = { viewModel.showDatePickerDialog = false }
        )
    }

    // Multiline Prompt Dialog
    if (viewModel.showMultilinePromptDialog) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { viewModel.showMultilinePromptDialog = false },
                title = {
                    Text(
                        text = "نحوه ثبت کارت‌های چندخطی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "متن شما شامل ${viewModel.multilineLines.size.toPersianDigits()} سطر غیرخالی است. مایلید چگونه ذخیره شود؟",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (viewModel.selectedAttachmentUris.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "توجه: فایل‌های پیوست فقط به کارت اول اضافه خواهند شد.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.saveOfflineCardsBatch(splitPerLine = true) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("هر سطر به عنوان یک کارت جداگانه (${viewModel.multilineLines.size.toPersianDigits()} کارت)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.saveOfflineCardsBatch(splitPerLine = false) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("کل متن به عنوان یک کارت واحد", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.showMultilinePromptDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }

    // Add Card Dialog (Keyboard opens with input field)
    if (viewModel.showAddCardDialog) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            viewModel.addSelectedAttachments(uris)
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.showAddCardDialog = false 
                    viewModel.consumeIncomingShare() // Clean up on cancel
                },
                title = {
                    Text(
                        text = "ثبت کارت آفلاین جدید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EditCardDestinationSelector(
                            selectedProject = viewModel.selectedProject,
                            selectedBoard = viewModel.selectedBoard,
                            selectedList = viewModel.selectedList,
                            onSelectProjectClick = { viewModel.showProjectPicker = true },
                            onSelectBoardClick = { viewModel.showBoardPicker = true },
                            onSelectListClick = { viewModel.showListPicker = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = viewModel.cardTitle,
                            onValueChange = viewModel::onCardTitleChange,
                            label = { Text("عنوان کارت") },
                            singleLine = false,
                            maxLines = 4,
                            textStyle = TextStyle(
                                textDirection = TextDirection.ContentOrRtl,
                                textAlign = TextAlign.Right
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DueDateSelectorButton(
                            dueDateIso = viewModel.selectedDueDateISO,
                            onClick = { viewModel.showDatePickerDialog = true },
                            onClear = { viewModel.selectedDueDateISO = null }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(AttachmentIcon, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("افزودن ضمیمه (فایل)")
                        }
                        
                        if (viewModel.selectedAttachmentUris.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AttachmentList(
                                uris = viewModel.selectedAttachmentUris,
                                onRemove = viewModel::removeSelectedAttachment
                            )
                        }

                        if (viewModel.availableLabels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LabelChipsSelector(
                                availableLabels = viewModel.availableLabels,
                                selectedLabelIds = viewModel.selectedLabelIds,
                                onLabelToggle = viewModel::toggleLabelSelection
                            )
                        } else if (!viewModel.isFetchingLists) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "برچسبی برای این بورد در سرور ثبت نشده است",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                confirmButton = {
                    val isAddValid = viewModel.selectedProject != null && viewModel.selectedBoard != null && viewModel.selectedList != null && viewModel.cardTitle.trim().isNotEmpty()
                    Button(
                        onClick = viewModel::addCard,
                        enabled = isAddValid,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره کارت", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showAddCardDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }

    // Delete Confirmation Dialog
    viewModel.cardToDelete?.let { card ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { viewModel.cardToDelete = null },
                title = {
                    Text(
                        text = "حذف کارت آفلاین",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "آیا از حذف این کارت اطمینان دارید؟",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCard(card)
                            viewModel.cardToDelete = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حذف کارت", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cardToDelete = null }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }

    // Edit Card Dialog
    viewModel.cardToEdit?.let { card ->
        var editTitleText by remember(card) { mutableStateOf(card.title) }
        var editDueDateISO by remember(card) { mutableStateOf(card.dueDate) }
        var showEditDatePicker by remember(card) { mutableStateOf(false) }

        var editLabelIds by remember(card) {
            mutableStateOf(
                card.labelIds?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
            )
        }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(card) {
            focusRequester.requestFocus()
        }

        if (showEditDatePicker) {
            org.eshragh.nima2.ui.composable.PersianDatePickerDialog(
                initialIsoDate = editDueDateISO,
                onDateSelected = { iso ->
                    editDueDateISO = iso
                    showEditDatePicker = false
                },
                onDismiss = { showEditDatePicker = false }
            )
        }

        val isEditValid = viewModel.selectedProject != null && viewModel.selectedBoard != null && viewModel.selectedList != null && editTitleText.trim().isNotEmpty()

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { viewModel.cardToEdit = null },
                title = {
                    Text(
                        text = "ویرایش کارت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EditCardDestinationSelector(
                            selectedProject = viewModel.selectedProject,
                            selectedBoard = viewModel.selectedBoard,
                            selectedList = viewModel.selectedList,
                            onSelectProjectClick = { viewModel.showProjectPicker = true },
                            onSelectBoardClick = { viewModel.showBoardPicker = true },
                            onSelectListClick = { viewModel.showListPicker = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editTitleText,
                            onValueChange = { editTitleText = it },
                            label = { Text("عنوان جدید کارت") },
                            singleLine = false,
                            maxLines = 4,
                            textStyle = TextStyle(
                                textDirection = TextDirection.ContentOrRtl,
                                textAlign = TextAlign.Right
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DueDateSelectorButton(
                            dueDateIso = editDueDateISO,
                            onClick = { showEditDatePicker = true },
                            onClear = { editDueDateISO = null }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Offline edit doesn't support adding/removing files easily yet in this UI,
                        // but let's show the count or placeholder
                        Text(
                            text = "این کارت دارای ${card.attachmentCount.toPersianDigits()} ضمیمه است",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (viewModel.availableLabels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LabelChipsSelector(
                                availableLabels = viewModel.availableLabels,
                                selectedLabelIds = editLabelIds,
                                onLabelToggle = { id: String ->
                                    editLabelIds = if (editLabelIds.contains(id)) editLabelIds - id else editLabelIds + id
                                }
                            )
                        } else if (!viewModel.isFetchingLists) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "برچسبی برای این بورد در سرور ثبت نشده است",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val selProj = viewModel.selectedProject
                            val selBoard = viewModel.selectedBoard
                            val selList = viewModel.selectedList
                            val fullPathName = "${selProj?.name ?: "پروژه"} ← ${selBoard?.name ?: "بورد"} ← ${selList?.name ?: "لیست"}"
                            viewModel.updateCardFull(
                                card = card,
                                newTitle = editTitleText,
                                newListId = selList?.id,
                                newListName = fullPathName,
                                newLabelIds = editLabelIds,
                                newDueDateISO = editDueDateISO
                            )
                        },
                        enabled = isEditValid,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cardToEdit = null }) {
                        Text("انصراف")
                    }
                }
            )
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
fun SettingsDialog(
    defaultTab: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(defaultTab) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "تنظیمات برنامه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تب پیش‌فرض کارتابل:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            Text(text = "مهلت‌های انجام", modifier = Modifier.padding(start = 8.dp))
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            Text(text = "نمایش لیست", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onSave(selectedTab) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تنظیمات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun EditCardDestinationSelector(
    selectedProject: PlankaProject?,
    selectedBoard: PlankaBoard?,
    selectedList: PlankaList?,
    onSelectProjectClick: () -> Unit,
    onSelectBoardClick: () -> Unit,
    onSelectListClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "مسیر کارت:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedProject != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectProjectClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(ApartmentIcon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedProject?.name ?: "پروژه",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedBoard != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectBoardClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedBoard?.name ?: "بورد",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedList != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectListClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedList?.name ?: "لیست",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DestinationPathChips(pathString: String) {
    val parts = pathString.split(" ← ", " > ", " » ").map { it.trim() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (parts.size >= 3) {
            Icon(ApartmentIcon, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
            Text(parts[0], fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(2.dp))

            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
            Text(parts[1], fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(2.dp))

            Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
            Text(parts[2], fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
            Text(pathString, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                if (options.isEmpty()) {
                    Text("هیچ گزینه‌ای یافت نشد.")
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        options.forEachIndexed { index, optionName ->
                            val isSelected = optionName == selectedOption
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onOptionSelected(index) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = optionName,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("بستن", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableOfflineCardItem(
    card: OfflineCard,
    isAnySyncing: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSyncedAnimationFinished: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> { // Swiped Right -> Edit
                    onEdit()
                    false // snap back
                }
                SwipeToDismissBoxValue.EndToStart -> { // Swiped Left -> Delete
                    onDelete()
                    false // snap back
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Background container displaying Edit (Blue) on Left and Delete (Red) on Right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit background (Left side in RTL - revealed when dragging Right to Edit)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ویرایش",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Delete background (Right side in RTL - revealed when dragging Left to Delete)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "حذف",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) {
        OfflineCardItem(
            card = card,
            isAnySyncing = isAnySyncing,
            onSyncedAnimationFinished = onSyncedAnimationFinished
        )
    }
}

@Composable
fun DueDateSelectorButton(
    dueDateIso: String?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val jalali = remember(dueDateIso) { org.eshragh.nima2.util.JalaliCalendarHelper.iso8601ToJalali(dueDateIso) }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (jalali != null) "مهلت: ${jalali.toPersianDisplay().toPersianDigits()}" else "تعیین مهلت انجام (شمسی)",
                    fontSize = 12.sp,
                    fontWeight = if (jalali != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (jalali != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (jalali != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف مهلت",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineCardItem(
    card: OfflineCard,
    isAnySyncing: Boolean,
    onSyncedAnimationFinished: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                val hasDueDate = !card.dueDate.isNullOrBlank()
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = card.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            textAlign = TextAlign.Right,
                            textIndent = if (hasDueDate) TextIndent(firstLine = 85.sp) else TextIndent.None
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!card.labelNames.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val names = card.labelNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val colors = card.labelColors?.split(",")?.map { it.trim() } ?: emptyList()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        names.forEachIndexed { idx, labelName ->
                            val colorName = colors.getOrNull(idx)
                            val bg = getPlankaColor(colorName, MaterialTheme.colorScheme.primaryContainer)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = bg,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = labelName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (!card.listName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DestinationPathChips(pathString = card.listName)
                }

                if (card.errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.errorMessage,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!card.dueDate.isNullOrBlank()) {
                val jalali = org.eshragh.nima2.util.JalaliCalendarHelper.iso8601ToJalali(card.dueDate)
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
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = jalali.toShortPersianDisplay().toPersianDigits(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        imageVector = AttachmentIcon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            val isUploading = card.status == SyncStatus.UPLOADING
            val isPending = card.status == SyncStatus.PENDING
            val isSynced = card.status == SyncStatus.SYNCED

            if (isUploading || (isPending && isAnySyncing) || isSynced) {
                val lottieRes = if (isSynced) R.raw.success else R.raw.list_loading
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                
                // Adjust speed to make success animation last exactly 2 seconds if requested
                val speed = if (isSynced && composition != null) {
                    composition!!.duration / 2000f
                } else 1f

                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = if (isSynced) 1 else LottieConstants.IterateForever,
                    speed = speed,
                    restartOnPlay = true
                )

                // When success animation finishes, trigger deletion
                if (isSynced && progress >= 1f) {
                    LaunchedEffect(card.id) {
                        onSyncedAnimationFinished()
                    }
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    val brandGradient = Brush.horizontalGradient(
                        colors = listOf(PrimaryDarkBlue, PrimaryBlue, BrandCyan)
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(if (isSynced) 60.dp else 90.dp)
                            .padding(vertical = 8.dp)
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithContent {
                                drawContent()
                                if (!isPending) {
                                    drawRect(
                                        brush = brandGradient,
                                        blendMode = BlendMode.SrcIn
                                    )
                                } else {
                                    drawRect(
                                        color = Color.Gray.copy(alpha = 0.5f),
                                        blendMode = BlendMode.SrcIn
                                    )
                                }
                            }
                    )
                }
            }
        }
    }
}

private val PLANKA_COLOR_MAP = mapOf(
    "muddy-grey" to Color(0xFF69655A),
    "autumn-leafs" to Color(0xFFC9B037),
    "morning-sky" to Color(0xFF52B9D5),
    "antique-blue" to Color(0xFF6C99BB),
    "egg-yellow" to Color(0xFFF9C423),
    "desert-sand" to Color(0xFFFAD371),
    "dark-granite" to Color(0xFF8B8680),
    "fresh-salad" to Color(0xFFCED85E),
    "lagoon-blue" to Color(0xFF109DC0),
    "midnight-blue" to Color(0xFF0A63A0),
    "light-orange" to Color(0xFFFDAE5F),
    "pumpkin-orange" to Color(0xFFED9223),
    "light-concrete" to Color(0xFFAFB0A4),
    "sunny-grass" to Color(0xFFBECA02),
    "navy-blue" to Color(0xFF1D7299),
    "lilac-eyes" to Color(0xFF406CBD),
    "apricot-red" to Color(0xFFFC736C),
    "orange-peel" to Color(0xFFDE692F),
    "bright-moss" to Color(0xFF96B352),
    "deep-ocean" to Color(0xFF004C70),
    "summer-sky" to Color(0xFF5D9CEC),
    "berry-red" to Color(0xFFE83855),
    "light-cocoa" to Color(0xFFA85540),
    "grey-stone" to Color(0xFFAAB2BD),
    "tank-green" to Color(0xFF8AA177),
    "coral-green" to Color(0xFF2B6A6C),
    "sugar-plum" to Color(0xFF7E86C7),
    "pink-tulip" to Color(0xFFE34F7C),
    "shady-rust" to Color(0xFF87564A),
    "wet-rock" to Color(0xFF83949B),
    "wet-moss" to Color(0xFF4A8753),
    "turquoise-sea" to Color(0xFF00858A),
    "lavender-fields" to Color(0xFFB287BD),
    "piggy-red" to Color(0xFFF97394),
    "light-mud" to Color(0xFFC7A57A),
    "gun-metal" to Color(0xFF4F6573),
    "modern-green" to Color(0xFF77CE87),
    "french-coast" to Color(0xFF00B4B1),
    "sweet-lilac" to Color(0xFF975298),
    "red-burgundy" to Color(0xFFAD5F7D)
)

fun getPlankaColor(colorName: String?, defaultColor: Color): Color {
    if (colorName == null) return defaultColor
    val exactColor = PLANKA_COLOR_MAP[colorName.lowercase().trim()]
    if (exactColor != null) return exactColor

    val lower = colorName.lowercase()
    return when {
        lower.contains("red") || lower.contains("ruby") || lower.contains("coral") || lower.contains("berry") -> Color(0xFFE83855)
        lower.contains("blue") || lower.contains("sky") || lower.contains("ocean") || lower.contains("lagoon") -> Color(0xFF109DC0)
        lower.contains("yellow") || lower.contains("amber") || lower.contains("gold") || lower.contains("scream") -> Color(0xFFF9C423)
        lower.contains("green") || lower.contains("shamrock") || lower.contains("emerald") || lower.contains("moss") -> Color(0xFF77CE87)
        lower.contains("purple") || lower.contains("violet") || lower.contains("lavender") || lower.contains("lilac") -> Color(0xFFB287BD)
        lower.contains("pink") -> Color(0xFFE34F7C)
        lower.contains("orange") -> Color(0xFFED9223)
        else -> defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelChipsSelector(
    availableLabels: List<org.eshragh.nima2.data.remote.model.PlankaLabel>,
    selectedLabelIds: Set<String>,
    onLabelToggle: (String) -> Unit
) {
    if (availableLabels.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "برچسب‌ها:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableLabels.forEach { label ->
                    val isSelected = selectedLabelIds.contains(label.id)
                    val labelText = label.name ?: "برچسب"
                    val bg = getPlankaColor(label.color, MaterialTheme.colorScheme.primaryContainer)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onLabelToggle(label.id) },
                        label = {
                            Text(
                                labelText,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        } else null,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = bg,
                            selectedBorderColor = bg
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = bg.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = bg,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
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
fun AttachmentList(
    uris: List<android.net.Uri>,
    onRemove: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current
    if (uris.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "فایل‌های پیوست:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uris.forEach { uri ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val mimeType = context.contentResolver.getType(uri)
                                    val fileName = uri.lastPathSegment ?: ""
                                    val isImage = mimeType?.startsWith("image") == true || 
                                                 fileName.lowercase().let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp") }
                                    
                                    if (isImage) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        val fileName = uri.lastPathSegment ?: ""
                                        val fileIcon = FileUtils.getFileIcon(fileName, mimeType)
                                        Icon(
                                            imageVector = fileIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uri.lastPathSegment ?: "فایل",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onRemove(uri) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: SyncStatus) {
    val config = when (status) {
        SyncStatus.PENDING -> StatusConfig(
            Color(0xFFFFF3CD),
            Color(0xFF856404),
            "منتظر",
            Icons.Default.Info
        )
        SyncStatus.UPLOADING -> StatusConfig(
            Color(0xFFCCE5FF),
            Color(0xFF004085),
            "ارسال",
            Icons.Default.Refresh
        )
        SyncStatus.SYNCED -> StatusConfig(
            Color(0xFFD4EDDA),
            Color(0xFF155724),
            "ثبت‌شده",
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
            .background(config.bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = config.text,
            color = config.textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
