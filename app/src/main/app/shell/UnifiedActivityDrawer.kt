package com.winlator.cmod.app.shell
import com.winlator.cmod.app.shell.UnifiedActivity.ControllerConnectionState

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Configuration
import android.hardware.input.InputManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.winlator.cmod.BuildConfig
import com.winlator.cmod.R
import com.winlator.cmod.app.PluviaApp
import com.winlator.cmod.app.db.PluviaDatabase
import com.winlator.cmod.app.service.DownloadService
import com.winlator.cmod.app.service.download.DownloadCoordinator
import com.winlator.cmod.app.update.UpdateChecker
import com.winlator.cmod.feature.settings.InputControlsFragment
import com.winlator.cmod.feature.settings.SettingsFocusZone
import com.winlator.cmod.feature.settings.SettingsHost
import com.winlator.cmod.feature.settings.SettingsNavBridge
import com.winlator.cmod.feature.settings.SettingsNavItem
import com.winlator.cmod.feature.setup.SetupWizardActivity
import com.winlator.cmod.feature.shortcuts.LibraryShortcutUtils
import com.winlator.cmod.feature.shortcuts.LibraryShortcutArtwork
import com.winlator.cmod.feature.artwork.SteamArtworkScraper
import com.winlator.cmod.runtime.container.Container
import com.winlator.cmod.feature.shortcuts.ShortcutBroadcastReceiver
import com.winlator.cmod.feature.shortcuts.ShortcutSettingsComposeDialog
import com.winlator.cmod.feature.shortcuts.ShortcutsFragment
import com.winlator.cmod.feature.stores.common.StoreArtworkCache
import com.winlator.cmod.feature.stores.epic.data.EpicCredentials
import com.winlator.cmod.feature.stores.epic.data.EpicGame
import com.winlator.cmod.feature.stores.epic.data.EpicGameToken
import com.winlator.cmod.feature.stores.epic.service.EpicAuthManager
import com.winlator.cmod.feature.stores.epic.service.EpicCloudSavesManager
import com.winlator.cmod.feature.stores.epic.service.EpicConstants
import com.winlator.cmod.feature.stores.epic.service.EpicDownloadManager
import com.winlator.cmod.feature.stores.epic.service.EpicGameLauncher
import com.winlator.cmod.feature.stores.epic.service.EpicManager
import com.winlator.cmod.feature.stores.epic.service.EpicService
import com.winlator.cmod.feature.stores.epic.service.EpicUpdateInfo
import com.winlator.cmod.feature.stores.epic.ui.auth.EpicOAuthActivity
import com.winlator.cmod.feature.stores.gog.data.GOGDlcInfo
import com.winlator.cmod.feature.stores.gog.data.GOGGame
import com.winlator.cmod.feature.stores.gog.data.LibraryItem
import com.winlator.cmod.feature.stores.gog.service.GOGAuthManager
import com.winlator.cmod.feature.stores.gog.service.GOGConstants
import com.winlator.cmod.feature.stores.gog.service.GOGManifestSizes
import com.winlator.cmod.feature.stores.gog.service.GOGService
import com.winlator.cmod.feature.stores.gog.service.GOGUpdateInfo
import com.winlator.cmod.feature.stores.gog.ui.auth.GOGOAuthActivity
import com.winlator.cmod.feature.stores.steam.SteamLoginActivity
import com.winlator.cmod.feature.stores.steam.data.DepotInfo
import com.winlator.cmod.feature.stores.steam.data.DownloadInfo
import com.winlator.cmod.feature.stores.steam.data.SteamApp
import com.winlator.cmod.feature.stores.steam.enums.DownloadPhase
import com.winlator.cmod.feature.stores.steam.events.AndroidEvent
import com.winlator.cmod.feature.stores.steam.events.EventDispatcher
import com.winlator.cmod.feature.stores.steam.service.SteamService
import com.winlator.cmod.feature.stores.steam.utils.PrefManager
import com.winlator.cmod.feature.stores.steam.utils.getAvatarURL
import com.winlator.cmod.feature.sync.CloudSyncHelper
import com.winlator.cmod.feature.sync.SaveBackupManager
import com.winlator.cmod.feature.sync.ui.CloudSavesContent
import com.winlator.cmod.runtime.container.ContainerManager
import com.winlator.cmod.runtime.container.Shortcut
import com.winlator.cmod.runtime.display.XServerDisplayActivity
import com.winlator.cmod.runtime.display.environment.ImageFs
import com.winlator.cmod.runtime.input.ControllerHelper
import com.winlator.cmod.runtime.wine.PeIconExtractor
import com.winlator.cmod.shared.android.ActivityResultHost
import com.winlator.cmod.shared.android.AppTerminationHelper
import com.winlator.cmod.shared.android.DirectoryPickerDialog
import com.winlator.cmod.shared.android.FixedFontScaleAppCompatActivity
import com.winlator.cmod.shared.android.RefreshRateUtils
import com.winlator.cmod.shared.io.StorageUtils
import com.winlator.cmod.shared.io.FileUtils
import com.winlator.cmod.shared.ui.CarouselView
import com.winlator.cmod.shared.ui.dialog.PopupDialog
import com.winlator.cmod.shared.ui.dialog.PopupTextAction
import androidx.compose.foundation.focusGroup
import com.winlator.cmod.shared.ui.focus.controllerFocusGlow
import com.winlator.cmod.shared.ui.focus.controllerMenuInput
import com.winlator.cmod.shared.ui.focus.controllerTextFieldEscape
import com.winlator.cmod.shared.ui.nav.DialogPaneNav
import com.winlator.cmod.shared.ui.nav.LocalPaneNav
import com.winlator.cmod.shared.ui.nav.PANE_DIR_ACTIVATE
import com.winlator.cmod.shared.ui.nav.PANE_DIR_DOWN
import com.winlator.cmod.shared.ui.nav.PANE_DIR_LEFT
import com.winlator.cmod.shared.ui.nav.PANE_DIR_RIGHT
import com.winlator.cmod.shared.ui.nav.PANE_DIR_SECONDARY
import com.winlator.cmod.shared.ui.nav.PANE_DIR_UP
import com.winlator.cmod.shared.ui.nav.PaneNavRegistry
import com.winlator.cmod.shared.ui.nav.paneNavItem
import kotlinx.coroutines.CoroutineScope
import com.winlator.cmod.shared.ui.FourByTwoGridView
import com.winlator.cmod.shared.ui.JoystickGridScroll
import com.winlator.cmod.shared.ui.JoystickListScroll
import com.winlator.cmod.shared.ui.ListView
import com.winlator.cmod.shared.ui.widget.chasingBorder
import com.winlator.cmod.shared.theme.WinNativeTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.Lazy
import com.winlator.cmod.feature.stores.steam.enums.EPersonaState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

// Navigation drawer + add-custom-game dialog + empty/login states, split out of UnifiedActivity.kt (behavior-identical).

@Composable
internal fun UnifiedActivity.EmptyStateMessage(message: String) {
    Text(message, color = TextSecondary, modifier = Modifier.padding(16.dp))
}

@Composable
internal fun UnifiedActivity.LoginRequiredScreen(
    storeName: String,
    onLoginClick: () -> Unit,
) {
    val message =
        if (storeName ==
            "Library"
        ) {
            stringResource(R.string.library_games_sign_in_prompt)
        } else {
            stringResource(R.string.stores_accounts_sign_in_store_prompt, storeName)
        }
    val buttonText =
        if (storeName ==
            "Library"
        ) {
            stringResource(R.string.stores_accounts_manage)
        } else {
            stringResource(R.string.stores_accounts_sign_into_store, storeName)
        }

    Box(Modifier.fillMaxSize().smoothScreenEnter(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(20.dp))
            val interactionSource =
                remember {
                    androidx.compose.foundation.interaction
                        .MutableInteractionSource()
                }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .smoothPress(interactionSource, pressedScale = 0.96f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onLoginClick,
                        ).border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(buttonText, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Drawer content: avatar card + filters
@Composable
internal fun UnifiedActivity.DrawerContent(
    persona: com.winlator.cmod.feature.stores.steam.data.SteamFriend?,
    isOpen: Boolean,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    storeVisible: SnapshotStateMap<String, Boolean>,
    contentFilters: SnapshotStateMap<String, Boolean>,
    immersiveMode: Boolean,
    immersiveBlur: Boolean,
    onStoreVisibleChanged: (String, Boolean) -> Unit,
    onContentFiltersChanged: (String, Boolean) -> Unit,
    onImmersiveModeChanged: (Boolean) -> Unit,
    onImmersiveBlurChanged: (Boolean) -> Unit,
    onExportAll: () -> Unit,
    onExitApp: () -> Unit,
) {
    val drawerBridge = (context as? UnifiedActivity)?.drawerNavBridge
    val navRegistry = remember(drawerBridge) { PaneNavRegistry(initialSignal = drawerBridge?.navSignal ?: -1) }
    navRegistry.controllerActive = drawerBridge?.controllerActive ?: false
    LaunchedEffect(navRegistry, drawerBridge?.navSignal) {
        navRegistry.processNav(drawerBridge?.navSignal ?: 0, drawerBridge?.navDir ?: 0)
    }
    LaunchedEffect(isOpen) { if (isOpen) navRegistry.reset() }

    ModalDrawerSheet(
        drawerShape = RectangleShape,
        drawerContainerColor = Color(0xFF12121B),
        drawerContentColor = TextPrimary,
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.width(324.dp),
    ) {
        CompositionLocalProvider(LocalPaneNav provides navRegistry) {
        Column(
            Modifier
                .fillMaxHeight()
                .smoothScreenEnter()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {

            // ── View Options ──
            Text(
                stringResource(R.string.library_games_view_options_header),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            DrawerSwitchCard(
                label = stringResource(R.string.library_games_immersive_mode),
                description = stringResource(R.string.library_games_immersive_mode_description),
                checked = immersiveMode,
                onCheckedChange = onImmersiveModeChanged,
            )

            if (immersiveMode) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    DrawerSwitchCard(
                        label = stringResource(R.string.library_games_immersive_blur),
                        description = stringResource(R.string.library_games_immersive_blur_description),
                        checked = immersiveBlur,
                        onCheckedChange = onImmersiveBlurChanged,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            DrawerActionCard(
                icon = Icons.Outlined.IosShare,
                label = stringResource(R.string.shortcuts_export_to_frontend),
                onClick = onExportAll,
            )

            Spacer(Modifier.height(16.dp))

            // ── Stores ──
            Text(
                stringResource(R.string.stores_accounts_stores_header),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawerFilterButton("Steam", storeVisible["steam"] == true, Modifier.weight(1f)) { onStoreVisibleChanged("steam", it) }
                DrawerFilterButton("Epic", storeVisible["epic"] == true, Modifier.weight(1f)) { onStoreVisibleChanged("epic", it) }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawerFilterButton("GOG", storeVisible["gog"] == true, Modifier.weight(1f)) { onStoreVisibleChanged("gog", it) }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // ── Content Types ──
            Text(
                stringResource(R.string.settings_content_types_header),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawerFilterButton("Games", contentFilters["games"] == true, Modifier.weight(1f)) { onContentFiltersChanged("games", it) }
                DrawerFilterButton("DLC", contentFilters["dlc"] == true, Modifier.weight(1f)) { onContentFiltersChanged("dlc", it) }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawerFilterButton("Applications", contentFilters["applications"] == true, Modifier.weight(1f)) { onContentFiltersChanged("applications", it) }
                DrawerFilterButton("Tools", contentFilters["tools"] == true, Modifier.weight(1f)) { onContentFiltersChanged("tools", it) }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.15f))
            Spacer(Modifier.height(16.dp))

            DrawerExitAppCard(onClick = onExitApp)
        }
        }
    }
}

@Composable
internal fun UnifiedActivity.DrawerExitAppCard(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(DangerRed.copy(alpha = 0.16f))
                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .paneNavItem(cornerRadius = 12.dp, onActivate = onClick)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DangerRed.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ExitToApp,
                contentDescription = null,
                tint = Color(0xFFFFB4B4),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.common_ui_exit_app),
            color = Color(0xFFFFD6D6),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun UnifiedActivity.DrawerActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(Accent.copy(alpha = 0.14f))
                .border(1.dp, Accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .paneNavItem(cornerRadius = 12.dp, onActivate = onClick)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun UnifiedActivity.DrawerFilterButton(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    onToggle: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = if (checked) Accent.copy(alpha = 0.2f) else CardDark
    val borderColor = if (checked) Accent else CardBorder
    val textColor = if (checked) Accent else TextSecondary
    val scale = if (isPressed) 0.92f else 1f

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .paneNavItem(cornerRadius = 8.dp, onActivate = { onToggle(!checked) })
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onToggle(!checked) }
                .padding(vertical = 10.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSize,
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
internal fun UnifiedActivity.DrawerSwitchCard(
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = if (checked) Accent.copy(alpha = 0.18f) else CardDark
    val borderColor = if (checked) Accent else CardBorder
    val labelColor = if (checked) Accent else TextPrimary
    val scale = if (isPressed) 0.97f else 1f

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .paneNavItem(cornerRadius = 10.dp, onActivate = { onCheckedChange(!checked) })
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onCheckedChange(!checked) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 2,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.focusProperties { canFocus = false },
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Accent,
                    checkedBorderColor = Accent,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardDark,
                    uncheckedBorderColor = CardBorder,
                ),
        )
    }
}

@Composable
internal fun UnifiedActivity.AddCustomGameDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addMode by remember { mutableStateOf<String?>(null) }
    var selectedExePath by remember { mutableStateOf<String?>(null) }
    var gameName by remember { mutableStateOf("") }
    var gameFolder by remember { mutableStateOf<String?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var androidApps by remember { mutableStateOf<List<InstalledAndroidLibraryApp>>(emptyList()) }
    var androidSearch by remember { mutableStateOf("") }
    var selectedAndroidApp by remember { mutableStateOf<InstalledAndroidLibraryApp?>(null) }

    LaunchedEffect(addMode) {
        if (addMode == "android") {
            androidApps = withContext(Dispatchers.IO) { queryInstalledAndroidLibraryApps(context) }
        }
    }

    fun addAndroidApp(app: InstalledAndroidLibraryApp) {
        if (isAdding) return
        isAdding = true
        scope.launch(Dispatchers.IO) {
            val saved = saveAndroidLibraryApp(context, app)
            withContext(Dispatchers.Main) {
                isAdding = false
                if (saved) {
                    com.winlator.cmod.shared.ui.toast.WinToast.show(
                        context,
                        "${app.label} added!",
                        android.widget.Toast.LENGTH_SHORT,
                    )
                    onDismiss()
                } else {
                    com.winlator.cmod.shared.ui.toast.WinToast.show(
                        context,
                        "App is already in Library",
                        android.widget.Toast.LENGTH_SHORT,
                    )
                }
            }
        }
    }

    fun selectExecutable(path: String) {
        val file = java.io.File(path)
        val launchable = file.extension.lowercase() in DirectoryPickerDialog.ExecutableExtensions
        if (!file.isFile || !launchable) {
            com.winlator.cmod.shared.ui.toast.WinToast.show(
                context,
                R.string.common_ui_select_valid_exe_file,
                android.widget.Toast.LENGTH_SHORT,
            )
            return
        }
        selectedExePath = path
        gameFolder = LibraryShortcutUtils.detectCustomGameFolder(path)
        if (gameName.isBlank()) {
            gameName = file.nameWithoutExtension.replace("_", " ").replace("-", " ")
        }
    }

    val defaultDensity = LocalDensity.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(defaultDensity.density, fontScale = 1f),
            androidx.compose.material3.LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified,
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(0.92f),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF141B24),
            ) {
                when (addMode) {
                    null -> Column(Modifier.padding(18.dp)) {
                        Text("Add App", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Choose what you want to add to Library", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(14.dp))
                        AddAppModeButton("Windows Game", "Add an .exe through the PC emulator", Icons.Outlined.Computer) { addMode = "windows" }
                        Spacer(Modifier.height(8.dp))
                        AddAppModeButton("Android App", "Add an app already installed on this phone", Icons.Outlined.Android) { addMode = "android" }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel", color = TextSecondary) }
                    }

                    "android" -> {
                        val filtered = androidApps.filter { it.label.contains(androidSearch, ignoreCase = true) }
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { addMode = null }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Accent) }
                                Text("Add Android App", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                            OutlinedTextField(
                                value = androidSearch,
                                onValueChange = { androidSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Search installed apps", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Accent) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Accent,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = Accent,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                            Spacer(Modifier.height(10.dp))
                            if (filtered.isEmpty()) {
                                Text("No installed apps found", color = TextSecondary, modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(filtered, key = { it.packageName }) { app ->
                                        val alreadyAdded = isAndroidLibraryAppSaved(context, app.packageName)
                                        Row(
                                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                                .background(if (alreadyAdded) Accent.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.04f))
                                                .clickable(enabled = !alreadyAdded) { selectedAndroidApp = app },
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (app.icon != null) {
                                                AsyncImage(model = app.icon, contentDescription = null, modifier = Modifier.padding(10.dp).size(42.dp))
                                            } else {
                                                Icon(Icons.Outlined.Android, null, tint = Accent, modifier = Modifier.padding(10.dp).size(42.dp))
                                            }
                                            Column(Modifier.weight(1f)) {
                                                Text(app.label, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                                Text(app.packageName, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            if (alreadyAdded) Text("Added", color = Accent, fontSize = 10.sp, modifier = Modifier.padding(end = 12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "windows" -> {
                        val registry = remember { PaneNavRegistry() }
                        val addEnabled = selectedExePath != null && gameName.isNotBlank() && !isAdding && gameFolder != null
                        val doAdd: () -> Unit = {
                            isAdding = true
                            scope.launch(Dispatchers.IO) {
                                addCustomGame(context, gameName.trim(), selectedExePath!!, gameFolder!!)
                                withContext(Dispatchers.Main) {
                                    isAdding = false
                                    com.winlator.cmod.shared.ui.toast.WinToast.show(context, "$gameName added!", android.widget.Toast.LENGTH_SHORT)
                                    onDismiss()
                                }
                            }
                        }
                        CompositionLocalProvider(LocalPaneNav provides registry) {
                            DialogPaneNav(registry, onDismiss = onDismiss, onStart = { if (addEnabled) doAdd() })
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { addMode = null }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Accent) }
                                    Text("Add Windows Game", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f))
                                        .paneNavItem(cornerRadius = 12.dp, tapToSelect = true, isEntry = true, onActivate = {
                                            DirectoryPickerDialog.showFile(
                                                activity = this@AddCustomGameDialog,
                                                initialPath = selectedExePath ?: gameFolder ?: android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath,
                                                title = getString(R.string.common_ui_select_exe),
                                                allowedExtensions = DirectoryPickerDialog.ExecutableExtensions,
                                                dimAmount = 0.5f,
                                                preserveBackdropBlur = true,
                                                extraRoots = driveRoots(includeInternal = true),
                                                onSelected = ::selectExecutable,
                                            )
                                        }).padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.FolderOpen, null, tint = Accent, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(selectedExePath ?: "Select Executable", color = if (selectedExePath == null) TextSecondary else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                }
                                if (selectedExePath != null) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(value = gameName, onValueChange = { gameName = it }, label = { Text("Game name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Accent))
                                    Spacer(Modifier.height(8.dp))
                                    Text(gameFolder ?: "", color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(Modifier.height(14.dp))
                                Button(onClick = doAdd, enabled = addEnabled, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text(if (isAdding) "Adding…" else "Add to Library", color = Color.Black) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedAndroidApp != null) {
        AlertDialog(
            onDismissRequest = { selectedAndroidApp = null },
            title = { Text("Add ${selectedAndroidApp!!.label}?", color = TextPrimary) },
            text = { Text("This will add the installed Android app to your Library. It will launch directly in Android, not through the PC emulator.", color = TextSecondary) },
            confirmButton = { TextButton(onClick = { val app = selectedAndroidApp!!; selectedAndroidApp = null; addAndroidApp(app) }) { Text("Add", color = Accent) } },
            dismissButton = { TextButton(onClick = { selectedAndroidApp = null }) { Text("Cancel", color = TextSecondary) } },
            containerColor = CardDark,
        )
    }
}

@Composable
private fun AddAppModeButton(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.05f)).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary)
    }
}

internal data class InstalledAndroidLibraryApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap?,
)

private const val ANDROID_LIBRARY_PREFIX = "android://"

internal fun isAndroidLibraryApp(app: SteamApp): Boolean = app.gameDir.startsWith(ANDROID_LIBRARY_PREFIX)
internal fun androidPackageName(app: SteamApp): String = app.gameDir.removePrefix(ANDROID_LIBRARY_PREFIX)

private fun queryInstalledAndroidLibraryApps(context: android.content.Context): List<InstalledAndroidLibraryApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        .asSequence()
        .filter { it.packageName != context.packageName }
        .mapNotNull { info ->
            val launchIntent = pm.getLaunchIntentForPackage(info.packageName) ?: return@mapNotNull null
            val label = runCatching { pm.getApplicationLabel(info).toString() }.getOrDefault(info.packageName)
            val icon = runCatching { drawableToBitmap(pm.getApplicationIcon(info)) }.getOrNull()
            InstalledAndroidLibraryApp(info.packageName, label, icon)
        }
        .sortedBy { it.label.lowercase() }
        .toList()
}

private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
    if (drawable is BitmapDrawable) return drawable.bitmap
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
    }
}

private fun androidAppId(packageName: String): Int = -2000000000 + (packageName.hashCode() and 0x0FFFFFFF)

private fun androidAppPrefs(context: android.content.Context) =
    context.getSharedPreferences("android_library_apps", android.content.Context.MODE_PRIVATE)

private fun isAndroidLibraryAppSaved(context: android.content.Context, packageName: String): Boolean =
    androidAppPrefs(context).getString("${androidAppId(packageName)}_package", null) == packageName

private fun saveAndroidLibraryApp(context: android.content.Context, app: InstalledAndroidLibraryApp): Boolean {
    val prefs = androidAppPrefs(context)
    if (isAndroidLibraryAppSaved(context, app.packageName)) return false
    val iconDir = java.io.File(context.filesDir, "android_library_icons")
    if (!iconDir.exists()) iconDir.mkdirs()
    val iconFile = java.io.File(iconDir, "${androidAppId(app.packageName)}.png")
    app.icon?.let { bitmap ->
        runCatching { java.io.FileOutputStream(iconFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    }
    prefs.edit()
        .putString("${androidAppId(app.packageName)}_package", app.packageName)
        .putString("${androidAppId(app.packageName)}_name", app.label)
        .putString("${androidAppId(app.packageName)}_icon", iconFile.takeIf { it.exists() }?.absolutePath ?: "")
        .apply()
    return true
}

internal fun loadAndroidLibraryApps(context: android.content.Context): List<SteamApp> {
    val prefs = androidAppPrefs(context)
    return prefs.all.keys
        .filter { it.endsWith("_package") }
        .mapNotNull { key ->
            val id = key.removeSuffix("_package").toIntOrNull() ?: return@mapNotNull null
            val pkg = prefs.getString(key, null) ?: return@mapNotNull null
            val label = prefs.getString("${id}_name", pkg) ?: pkg
            SteamApp(id = id, name = label, developer = "Android", gameDir = "$ANDROID_LIBRARY_PREFIX$pkg")
        }
        .filter { context.packageManager.getLaunchIntentForPackage(androidPackageName(it)) != null }
        .sortedBy { it.name.lowercase() }
}

internal fun launchAndroidLibraryApp(context: android.content.Context, app: SteamApp): Boolean {
    if (!isAndroidLibraryApp(app)) return false
    val intent = context.packageManager.getLaunchIntentForPackage(androidPackageName(app)) ?: return false
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    return true
}

@Composable
internal fun UnifiedActivity.CustomPathWarningDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.stores_accounts_custom_download_path),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.stores_accounts_custom_download_path_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_ui_close), color = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onProceed,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.common_ui_proceed))
                    }
                }
            }
        }
    }
}

@Composable
internal fun UnifiedActivity.rememberControllerConnectionState(): ControllerConnectionState {
    val context = LocalContext.current
    val inputManager = remember(context) { context.getSystemService(InputManager::class.java) }
    var controllerState by remember { mutableStateOf(ControllerConnectionState()) }

    DisposableEffect(inputManager) {
        fun refreshState() {
            controllerState =
                ControllerConnectionState(
                    isConnected = ControllerHelper.isControllerConnected(),
                    isPlayStation = ControllerHelper.isPlayStationController(),
                )
        }

        val listener =
            object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) = refreshState()

                override fun onInputDeviceRemoved(deviceId: Int) = refreshState()

                override fun onInputDeviceChanged(deviceId: Int) = refreshState()
            }

        refreshState()
        inputManager?.registerInputDeviceListener(listener, null)
        onDispose {
            inputManager?.unregisterInputDeviceListener(listener)
        }
    }

    return controllerState
}
