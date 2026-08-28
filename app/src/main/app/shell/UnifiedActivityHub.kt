package com.winlator.cmod.app.shell
import com.winlator.cmod.app.shell.UnifiedActivity.TabDef

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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
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

// Main hub scaffold + top bar + glasses sheet + library carousel, split out of UnifiedActivity.kt (behavior-identical).

@Composable
internal fun UnifiedActivity.UnifiedHub() {
    val horizontalNavigationInsets =
        WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    // Library is intentionally a single adaptive console layout.
    // Landscape and portrait are selected automatically from the device orientation.
    val initialStoreVisible = startupStoreVisible ?: mapOf("steam" to true, "epic" to true, "gog" to true)
    val initialContentFilters = startupContentFilters ?: mapOf("games" to true, "dlc" to false, "applications" to false, "tools" to false)
    if (!startupBootstrapReady) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .windowInsetsPadding(horizontalNavigationInsets),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(color = Accent)
                Text(
                    text = stringResource(R.string.common_ui_app_name),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        return
    }

    val storeVisible = remember { mutableStateMapOf(*initialStoreVisible.entries.map { it.key to it.value }.toTypedArray()) }
    var showAddCustomGame by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var searchQueryTfv by remember { mutableStateOf(TextFieldValue("")) }
    val searchQuery = searchQueryTfv.text
    var localLibraryRefreshKey by remember { mutableIntStateOf(0) }
    var shortcutDataRefreshKey by remember { mutableIntStateOf(0) }
    var iconRefreshKey by remember { mutableIntStateOf(0) }

    val currentRefreshSignal = this@UnifiedHub.libraryRefreshSignal
    val libraryRefreshKey = currentRefreshSignal + localLibraryRefreshKey
    val shortcutRefreshKey = libraryRefreshKey + shortcutDataRefreshKey
    val playtimeRefreshKey = this@UnifiedHub.libraryPlaytimeRefreshSignal

    val contentFilters = remember { mutableStateMapOf(*initialContentFilters.entries.map { it.key to it.value }.toTypedArray()) }
    // No user-selectable library layout. The library always uses the adaptive
    // Nintendo-landscape / Windows-Phone-portrait presentation.
    val libraryLayoutMode = LibraryLayoutMode.CONSOLE_TILE
    var immersiveMode by remember { mutableStateOf(PrefManager.libraryImmersiveMode) }
    var immersiveBlur by remember { mutableStateOf(PrefManager.libraryImmersiveBlur) }
    val tabs = remember(storeVisible.toMap()) { buildTabs(storeVisible) }
    var selectedIdx by rememberSaveable { mutableIntStateOf(0) }
    var selectedDownloadId by remember { mutableStateOf<String?>(null) }
    var activeStore by remember { mutableStateOf<Any?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    LaunchedEffect(drawerState.isOpen) {
        drawerOpen = drawerState.isOpen
        if (!drawerState.isOpen) drawerNavBridge.controllerActive = false
    }
    val isLoggedIn by SteamService.isLoggedInFlow.collectAsState()
    val chatServiceEnabled by SteamService.chatServiceEnabledFlow.collectAsState()
    val isEpicLoggedIn by EpicAuthManager.isLoggedInFlow.collectAsState()
    val isGogLoggedIn by GOGAuthManager.isLoggedInFlow.collectAsState()
    val steamApps by db.steamAppDao().getAllOwnedApps().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val persona by SteamService.instance?.localPersona?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val rightDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val friends by SteamService.instance?.friendsList?.collectAsState()
        ?: remember { mutableStateOf(emptyList<com.winlator.cmod.feature.stores.steam.data.SteamFriendEntry>()) }
    var chatFriend by remember { mutableStateOf<com.winlator.cmod.feature.stores.steam.data.SteamFriendEntry?>(null) }
    val friendsDrawerOpen = rightDrawerState.isOpen
    LaunchedEffect(rightDrawerState.isOpen) {
        rightDrawerOpen = rightDrawerState.isOpen
        if (!rightDrawerState.isOpen) friendsDrawerNavBridge.controllerActive = false
    }
    LaunchedEffect(Unit) {
        (context as? UnifiedActivity)?.openFriendsSignal?.collect {
            if (rightDrawerState.isOpen) rightDrawerState.close() else rightDrawerState.open()
        }
    }
    var installedFriendGameIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    LaunchedEffect(friends) {
        val ids = friends.map { it.gameAppId }.filter { it > 0 }.distinct()
        installedFriendGameIds =
            withContext(Dispatchers.IO) { ids.filter { SteamService.isAppInstalled(it) }.toSet() }
    }
    LaunchedEffect(isLoggedIn, chatServiceEnabled) {
        if (isLoggedIn && chatServiceEnabled) {
            while (true) {
                runCatching { SteamService.instance?.refreshFriends() }
                kotlinx.coroutines.delay(30_000L)
            }
        }
    }
    LaunchedEffect(isLoggedIn, friendsDrawerOpen, chatServiceEnabled) {
        if (isLoggedIn && friendsDrawerOpen && chatServiceEnabled) {
            while (true) {
                runCatching { SteamService.instance?.syncFriendsPresence() }
                kotlinx.coroutines.delay(5_000L)
            }
        }
    }
    LaunchedEffect(isLoggedIn, chatServiceEnabled) {
        if (isLoggedIn && chatServiceEnabled) {
            runCatching { com.winlator.cmod.feature.stores.steam.chat.ChatOverlayService.start(context) }
        }
    }

    val epicApps by db.epicGameDao().getAll().collectAsState(initial = emptyList())
    val gogApps by db.gogGameDao().getAll().collectAsState(initial = emptyList())

    val controllerState = rememberControllerConnectionState()
    val isControllerConnected = controllerState.isConnected
    val isPS = controllerState.isPlayStation
    val isNintendo = rememberIsNintendoController()
    val isLibraryTab = tabs.getOrNull(selectedIdx)?.key == "library"

    val libraryRefreshListener =
        remember {
            object : EventDispatcher.JavaEventListener {
                override fun onEvent(event: Any) {
                    when (event) {
                        is AndroidEvent.LibraryInstallStatusChanged -> {
                            localLibraryRefreshKey++
                            shortcutDataRefreshKey++
                            iconRefreshKey++
                        }
                        is AndroidEvent.LibraryArtworkChanged -> {
                            shortcutDataRefreshKey++
                            iconRefreshKey++
                        }
                    }
                }
            }
        }
    DisposableEffect(libraryRefreshListener) {
        PluviaApp.events.onJava(AndroidEvent.LibraryInstallStatusChanged::class, libraryRefreshListener)
        PluviaApp.events.onJava(AndroidEvent.LibraryArtworkChanged::class, libraryRefreshListener)
        onDispose {
            PluviaApp.events.offJava(AndroidEvent.LibraryInstallStatusChanged::class, libraryRefreshListener)
            PluviaApp.events.offJava(AndroidEvent.LibraryArtworkChanged::class, libraryRefreshListener)
        }
    }

    LaunchedEffect(isEpicLoggedIn) {
        if (isEpicLoggedIn) {
            EpicService.start(context)
        }
    }

    LaunchedEffect(isGogLoggedIn) {
        if (isGogLoggedIn) {
            GOGService.start(context)
        }
    }

    val epicLoginLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val code = result.data?.getStringExtra(EpicOAuthActivity.EXTRA_AUTH_CODE)
                if (code != null) {
                    scope.launch {
                        val authResult = EpicAuthManager.authenticateWithCode(context, code)
                        if (authResult.isSuccess) {
                            com.winlator.cmod.shared.ui.toast.WinToast.show(
                                context,
                                R.string.stores_accounts_logged_in_epic,
                                android.widget.Toast.LENGTH_SHORT,
                            )
                        } else {
                            com.winlator.cmod.shared.ui.toast.WinToast.show(
                                context,
                                getString(R.string.stores_accounts_epic_login_failed, authResult.exceptionOrNull()?.message),
                                android.widget.Toast.LENGTH_LONG,
                            )
                        }
                    }
                }
            }
        }

    val gogLoginLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val code = result.data?.getStringExtra(GOGOAuthActivity.EXTRA_AUTH_CODE)
                if (!code.isNullOrBlank()) {
                    scope.launch {
                        val authResult = GOGAuthManager.authenticateWithCode(context, code)
                        if (authResult.isSuccess) {
                            GOGService.start(context)
                            com.winlator.cmod.shared.ui.toast.WinToast.show(
                                context,
                                R.string.stores_accounts_logged_in_gog,
                                android.widget.Toast.LENGTH_SHORT,
                            )
                        } else {
                            com.winlator.cmod.shared.ui.toast.WinToast.show(
                                context,
                                getString(R.string.stores_accounts_gog_login_failed, authResult.exceptionOrNull()?.message),
                                android.widget.Toast.LENGTH_LONG,
                            )
                        }
                    }
                }
            }
        }

    val filteredSteamApps =
        remember(steamApps, contentFilters.toMap()) {
            steamApps.filter { app ->
                when (app.type) {
                    com.winlator.cmod.feature.stores.steam.enums.AppType.game -> contentFilters["games"] == true
                    com.winlator.cmod.feature.stores.steam.enums.AppType.demo -> contentFilters["games"] == true
                    com.winlator.cmod.feature.stores.steam.enums.AppType.dlc -> contentFilters["dlc"] == true
                    com.winlator.cmod.feature.stores.steam.enums.AppType.application -> contentFilters["applications"] == true
                    com.winlator.cmod.feature.stores.steam.enums.AppType.tool -> contentFilters["tools"] == true
                    com.winlator.cmod.feature.stores.steam.enums.AppType.config -> contentFilters["tools"] == true
                    else -> contentFilters["games"] == true
                }
            }
        }

    var globalSettingsApp by remember { mutableStateOf<SteamApp?>(null) }
    var globalSettingsGogGame by remember { mutableStateOf<GOGGame?>(null) }

    LaunchedEffect(tabs.size) { if (selectedIdx >= tabs.size) selectedIdx = 0 }
    LaunchedEffect(isLoggedIn, persona) {
        if (isLoggedIn && persona == null) {
            SteamService.requestUserPersona()
        }
    }

    val activity = LocalContext.current as? UnifiedActivity

    LaunchedEffect(tabs) {
        activity?.keyEventFlow?.collect { event ->
            val key = tabs.getOrNull(selectedIdx)?.key ?: "library"
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_BUTTON_L1 -> {
                    activeStore = null
                    selectedIdx = if (selectedIdx > 0) selectedIdx - 1 else tabs.size - 1
                }

                android.view.KeyEvent.KEYCODE_BUTTON_R1 -> {
                    activeStore = null
                    selectedIdx = (selectedIdx + 1) % tabs.size
                }

                android.view.KeyEvent.KEYCODE_BUTTON_START -> {
                    navigateToSettings(SettingsNavItem.STORES)
                }

                android.view.KeyEvent.KEYCODE_BUTTON_SELECT -> {
                    if (key != "downloads") {
                        if (drawerState.isOpen) drawerState.close() else drawerState.open()
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_X -> {
                    if (key == "library" && (selectedSteamAppId != 0 || selectedGogGameId.isNotEmpty())) {
                        activity?.openHeroForFocusedSignal?.tryEmit(Unit)
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                    if (key == "library") {
                        activity?.openSearchSignal?.tryEmit(Unit)
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                    if (key == "library") {
                        showAddCustomGame = true
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_B -> {
                    if (chatFriend != null) {
                        chatFriend = null
                    } else if (rightDrawerState.isOpen) {
                        rightDrawerState.close()
                    } else if (drawerState.isOpen) {
                        drawerState.close()
                    } else if (globalSettingsApp != null) {
                        globalSettingsApp = null
                    } else if (globalSettingsGogGame != null) {
                        globalSettingsGogGame = null
                    } else if (showAddCustomGame) {
                        showAddCustomGame = false
                    } else {
                        showExitDialog = true
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_Y -> {
                    if (key == "library" && (selectedSteamAppId != 0 || selectedGogGameId.isNotEmpty())) {
                        if (selectedLibrarySource == "GOG") {
                            globalSettingsGogGame = gogApps.find { it.id == selectedGogGameId }
                            return@collect
                        }
                        val isCustom = selectedSteamAppId < 0
                        val epicId = if (selectedSteamAppId >= 2000000000) selectedSteamAppId - 2000000000 else 0

                        globalSettingsApp = (
                            steamApps.find { it.id == selectedSteamAppId }
                                ?: if (isCustom) {
                                    SteamApp(id = selectedSteamAppId, name = selectedSteamAppName, developer = "Custom")
                                } else if (epicId > 0) {
                                    val epic = epicApps.find { it.id == epicId }
                                    SteamApp(
                                        id = selectedSteamAppId,
                                        name = selectedSteamAppName,
                                        developer = epic?.developer ?: "Epic Games",
                                        gameDir = epic?.installPath ?: "",
                                    )
                                } else {
                                    null
                                }
                        )
                    }
                }

                android.view.KeyEvent.KEYCODE_BUTTON_A, android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (key == "library" && (selectedSteamAppId != 0 || selectedGogGameId.isNotEmpty())) {
                        val isCustom = selectedSteamAppId < 0
                        val epicId = if (selectedSteamAppId >= 2000000000) selectedSteamAppId - 2000000000 else 0
                        val containerManager = ContainerManager(context)
                        if (isCustom) {
                            launchCustomGame(context, containerManager, selectedSteamAppName)
                        } else if (selectedLibrarySource == "GOG") {
                            gogApps.find { it.id == selectedGogGameId }?.let {
                                launchGogGame(context, containerManager, it)
                            }
                        } else if (epicId > 0) {
                            val epic = epicApps.find { it.id == epicId }
                            if (epic != null && epic.isInstalled) {
                                val dummyApp =
                                    SteamApp(id = selectedSteamAppId, name = selectedSteamAppName, gameDir = epic.installPath)
                                launchSteamGame(context, containerManager, dummyApp)
                            }
                        } else {
                            val steam = steamApps.find { it.id == selectedSteamAppId }
                            if (steam != null) {
                                launchSteamGame(context, containerManager, steam)
                            }
                        }
                    } else if (key != "library" && key != "downloads") {
                        storeItemClickCallback?.invoke(storeFocusIndex.value)
                    }
                }

            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl,
    ) {
    ModalNavigationDrawer(
        drawerState = rightDrawerState,
        drawerContent = {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr,
            ) {
                com.winlator.cmod.feature.stores.steam.friends.FriendsDrawerContent(
                    isOpen = rightDrawerState.isOpen,
                    self = persona ?: com.winlator.cmod.feature.stores.steam.data.SteamFriend(),
                    friends = friends,
                    installedGameIds = installedFriendGameIds,
                    chatEnabled = chatServiceEnabled,
                    onSetState = { st -> scope.launch { SteamService.setPersonaState(st) } },
                    onOpenChat = { f -> chatFriend = f; scope.launch { rightDrawerState.close() } },
                    onJoinGame = { f ->
                        scope.launch { rightDrawerState.close() }
                        scope.launch {
                            val app = withContext(Dispatchers.IO) { SteamService.getAppInfoOf(f.gameAppId) }
                            val installed = withContext(Dispatchers.IO) { SteamService.getInstalledApp(f.gameAppId) }
                            val label = f.gameName.ifBlank { context.getString(R.string.steam_join_the_game) }
                            if (app != null && installed != null) {
                                android.widget.Toast.makeText(
                                    context, context.getString(R.string.steam_join_joining, f.name, label), android.widget.Toast.LENGTH_SHORT,
                                ).show()
                                launchSteamGame(context, ContainerManager(context), app, f.connectString)
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    if (app != null) context.getString(R.string.steam_join_install, label, f.name)
                                    else context.getString(R.string.steam_join_not_owned, label),
                                    android.widget.Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                    },
                    onPlayGame = { f ->
                        scope.launch { rightDrawerState.close() }
                        scope.launch {
                            val app = withContext(Dispatchers.IO) { SteamService.getAppInfoOf(f.gameAppId) }
                            if (app != null) {
                                launchSteamGame(context, ContainerManager(context), app, null)
                            }
                        }
                    },
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.5f),
        gesturesEnabled = rightDrawerState.isOpen,
    ) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr,
    ) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                persona = persona,
                isOpen = drawerState.isOpen,
                context = context,
                scope = scope,
                storeVisible = storeVisible,
                contentFilters = contentFilters,
                immersiveMode = immersiveMode,
                immersiveBlur = immersiveBlur,
                onStoreVisibleChanged = { key, value ->
                    storeVisible[key] = value
                    PrefManager.libraryStoreVisible = storeVisible.entries.filter { it.value }.joinToString(",") { it.key }
                },
                onContentFiltersChanged = { key, value ->
                    contentFilters[key] = value
                    PrefManager.libraryContentFilters = contentFilters.entries.filter { it.value }.joinToString(",") { it.key }
                },
                onImmersiveModeChanged = {
                    immersiveMode = it
                    PrefManager.libraryImmersiveMode = it
                },
                onImmersiveBlurChanged = {
                    immersiveBlur = it
                    PrefManager.libraryImmersiveBlur = it
                },
                onExportAll = {
                    scope.launch {
                        val count =
                            withContext(Dispatchers.IO) {
                                com.winlator.cmod.feature.shortcuts.FrontendExporter.exportAll(context)
                            }
                        val dir = com.winlator.cmod.feature.shortcuts.FrontendExporter.resolveExportDir(context)
                        com.winlator.cmod.shared.ui.toast.WinToast.show(
                            context,
                            if (count > 0) {
                                context.getString(R.string.shortcuts_export_all_done, count, dir?.path ?: "")
                            } else {
                                context.getString(R.string.shortcuts_export_all_none)
                            },
                        )
                    }
                },
                onExitApp = {
                    AppTerminationHelper.exitApplication(this@UnifiedHub, "hub_drawer_exit")
                },
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.5f),
        gesturesEnabled = drawerState.isOpen,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .smoothScreenEnter()
                .background(BgDark)
                .windowInsetsPadding(horizontalNavigationInsets),
        ) {
            val currentTabKeyForImmersive = tabs.getOrNull(selectedIdx)?.key ?: "library"
            val immersiveActive = immersiveMode && currentTabKeyForImmersive == "library"
            DisposableEffect(immersiveActive) {
                applyImmersiveSystemBars(immersiveActive)
                onDispose { applyImmersiveSystemBars(false) }
            }
            if (immersiveMode && currentTabKeyForImmersive == "library") {
                val immersiveModel by immersiveBackgroundRef.collectAsState()
                val immersiveRequest =
                    remember(immersiveModel, immersiveBlur, context) {
                        val builder = ImageRequest.Builder(context).data(immersiveModel)
                        (immersiveModel as? java.io.File)?.takeIf { it.isFile }?.let { file ->
                            // Custom uploads can be overwritten in place.
                            val key = "library_immersive_bg:${file.absolutePath}:${file.lastModified()}"
                            builder.memoryCacheKey(if (immersiveBlur) "$key:blur" else key).diskCacheKey(key)
                        }
                        if (immersiveBlur) {
                            // Blur baked into the bitmap at decode (quarter-res + radius 2 ≈ 8px on screen), so drawing costs the same as a plain image.
                            val dm = context.resources.displayMetrics
                            builder
                                .size(dm.widthPixels / 4, dm.heightPixels / 4)
                                .scale(coil.size.Scale.FILL)
                                .transformations(BoxBlurTransformation(radius = 2))
                        }
                        builder.crossfade(false).build()
                    }
                if (immersiveModel != null) {
                    Box(Modifier.matchParentSize()) {
                        AsyncImage(
                            model = immersiveRequest,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(BgDark.copy(alpha = 0.5f)),
                        )
                    }
                }
            }
            val scaffoldContainer = if (immersiveMode && currentTabKeyForImmersive == "library") Color.Transparent else BgDark
            val openFileManager: () -> Unit = {
                val internalPath = android.os.Environment.getExternalStorageDirectory().absolutePath
                val managedRoots = driveRoots(includeInternal = true)
                val containerManager = com.winlator.cmod.runtime.container.ContainerManager(context)
                val containers =
                    containerManager.getContainers().map {
                        DirectoryPickerDialog.ManagedContainer(it.id, it.getName())
                    }
                DirectoryPickerDialog.showManager(
                    activity = this@UnifiedHub,
                    initialPath = internalPath,
                    managedRoots = managedRoots,
                    containers = containers,
                    onRunFile = { exePath, containerId ->
                        val container = containerManager.getContainerById(containerId)
                        if (container != null) {
                            val winePath =
                                com.winlator.cmod.runtime.wine.WineUtils
                                    .hostPathToMappedWinePath(container, exePath)
                            startActivity(
                                android.content.Intent(
                                    this@UnifiedHub,
                                    com.winlator.cmod.runtime.display.XServerDisplayActivity::class.java,
                                ).apply {
                                    putExtra("container_id", container.id)
                                    putExtra("boot_exe", winePath)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                },
                            )
                        }
                    },
                    onCreateShortcut = { exePath ->
                        val exeFile = java.io.File(exePath)
                        addCustomGame(
                            context,
                            exeFile.nameWithoutExtension,
                            exePath,
                            exeFile.parent ?: exePath,
                        )
                        localLibraryRefreshKey++
                    },
                )
            }
            Scaffold(
                containerColor = scaffoldContainer,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopBar(tabs, selectedIdx, {
                        selectedIdx = it
                    }, persona, context, scope, isControllerConnected, isPS, isNintendo, isLibraryTab, searchQueryTfv, {
                        searchQueryTfv =
                            it
                    }, onFilterClicked = { scope.launch { drawerState.open() } }, onFriendsClicked = { scope.launch { rightDrawerState.open() } }) {
                        if (selectedLibrarySource == "GOG") {
                            globalSettingsGogGame = gogApps.find { it.id == selectedGogGameId }
                        } else {
                            globalSettingsApp = (
                                steamApps.find { it.id == selectedSteamAppId }
                                    ?: if (selectedSteamAppId < 0) {
                                        SteamApp(
                                            id = selectedSteamAppId,
                                            name = selectedSteamAppName,
                                            developer = "Custom",
                                        )
                                    } else if (selectedSteamAppId >= 2000000000) {
                                        val epicId = selectedSteamAppId - 2000000000
                                        val epic = epicApps.find { it.id == epicId }
                                        SteamApp(
                                            id = selectedSteamAppId,
                                            name = selectedSteamAppName,
                                            developer = epic?.developer ?: "Epic Games",
                                            gameDir = epic?.installPath ?: "",
                                        )
                                    } else {
                                        null
                                    }
                            )
                        }
                    }
                },
            ) { padding ->
                LaunchedEffect(selectedIdx, tabs) {
                    currentTabKey = tabs.getOrNull(selectedIdx)?.key ?: "library"
                    storeFocusIndex.value = 0
                    downloadsNavBridge.controllerActive = false
                }

                val key = tabs.getOrNull(selectedIdx)?.key ?: "library"
                val innerBoxBg = if (immersiveMode && key == "library") Color.Transparent else BgDark

                Box(Modifier.padding(padding).fillMaxSize().background(innerBoxBg)) {

                    LaunchedEffect(key) { libraryTabActive.value = (key == "library") }

                    // Keep Library composed so its state survives tab switches.
                    Box(
                        Modifier.fillMaxSize().let {
                            if (key == "library") {
                                it
                            } else {
                                it.alpha(0f).pointerInput(Unit) { /* block ghost taps */ }
                            }
                        },
                    ) {
                        LibraryCarousel(
                            isLoggedIn = isLoggedIn,
                            steamApps = filteredSteamApps,
                            epicApps = epicApps,
                            gogApps = gogApps,
                            libraryRefreshKey = libraryRefreshKey,
                            shortcutRefreshKey = shortcutRefreshKey,
                            playtimeRefreshKey = playtimeRefreshKey,
                            iconRefreshKey = iconRefreshKey,
                            searchQuery = searchQuery,
                            isControllerConnected = isControllerConnected,
                        )
                    }

                    AnimatedContent(
                        targetState = key,
                        transitionSpec = {
                            val forward = targetState.hashCode() >= initialState.hashCode()
                            (
                                fadeIn(animationSpec = tween(140)) +
                                    slideInHorizontally(
                                        animationSpec = tween(90),
                                        initialOffsetX = { width -> if (forward) width / 24 else -width / 24 },
                                    )
                            ) togetherWith (
                                fadeOut(animationSpec = tween(90)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(90),
                                        targetOffsetX = { width -> if (forward) -width / 36 else width / 36 },
                                    )
                            )
                        },
                        label = "hubTabContent",
                    ) { targetKey ->
                        if (targetKey != "library") {
                            when (targetKey) {
                                "downloads" -> DownloadsTab(
                                    selectedDownloadId,
                                    animationsActive = true,
                                    onSelectDownload = { selectedDownloadId = it },
                                )
                                "steam" -> SteamStoreTab(isLoggedIn, filteredSteamApps, searchQuery, LibraryLayoutMode.GRID_4)
                                "epic" -> EpicStoreTab(isEpicLoggedIn, epicApps, searchQuery, LibraryLayoutMode.GRID_4) {
                                    epicLoginLauncher.launch(Intent(this@UnifiedHub, EpicOAuthActivity::class.java))
                                }
                                "gog" -> GOGStoreTab(isGogLoggedIn, gogApps, searchQuery, LibraryLayoutMode.GRID_4) {
                                    gogLoginLauncher.launch(Intent(this@UnifiedHub, GOGOAuthActivity::class.java))
                                }
                                else -> {}
                            }
                        }
                    }

                    if (drawerState.isClosed) {
                        DrawerSwipeHotZone(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                        )
                    }
                    if (rightDrawerState.isClosed) {
                        DrawerSwipeHotZone(
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp),
                            isRightSide = true,
                            onOpenDrawer = { scope.launch { rightDrawerState.open() } },
                        )
                    }

                    // Files, Add-Game, and Glasses live in their own cluster anchored to the
                    // bottom-right corner - previously-empty space - so they're not piled in
                    // with the rest of the top bar's controls.
                    val cornerAnchorScale = rememberUiScale()
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp * cornerAnchorScale, bottom = 20.dp * cornerAnchorScale),
                    ) {
                        LibraryCornerActions(
                            isLibraryTab = isLibraryTab,
                            onFilesClicked = { openFileManager() },
                            onAddGameClicked = { showAddCustomGame = true },
                        )
                    }
                }
            }
        }
    } // end ModalNavigationDrawer
    } // end inner LTR
    } // end right friends ModalNavigationDrawer
    } // end RTL provider

    if (globalSettingsApp != null) {
        GameSettingsDialog(
            app = globalSettingsApp!!,
            onDismissRequest = { globalSettingsApp = null },
        )
    }
    if (globalSettingsGogGame != null) {
        GOGGameSettingsDialog(
            app = globalSettingsGogGame!!,
            onDismissRequest = { globalSettingsGogGame = null },
        )
    }

    if (showAddCustomGame) {
        AddCustomGameDialog(onDismiss = {
            showAddCustomGame = false
            localLibraryRefreshKey++
        })
    }

    chatFriend?.let { cf ->
        com.winlator.cmod.feature.stores.steam.friends.SteamChatScreen(
            friend = friends.firstOrNull { it.steamId == cf.steamId } ?: cf,
            onClose = { chatFriend = null },
        )
    }

    BackHandler(enabled = true) {
        if (chatFriend != null) {
            chatFriend = null
        } else if (rightDrawerState.isOpen) {
            scope.launch { rightDrawerState.close() }
        } else if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (globalSettingsApp != null) {
            globalSettingsApp = null
        } else if (globalSettingsGogGame != null) {
            globalSettingsGogGame = null
        } else if (showAddCustomGame) {
            showAddCustomGame = false
        } else if (activeStore != null) {
            activeStore = null
            selectedIdx = 0
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        Dialog(
            onDismissRequest = { showExitDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        ) {
            Box(
                modifier =
                    Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.common_ui_exit_app_confirm),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showExitDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.common_ui_cancel), fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = {
                                AppTerminationHelper.exitApplication(this@UnifiedHub, "hub_exit_menu")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.common_ui_exit), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun UnifiedActivity.DrawerSwipeHotZone(
    modifier: Modifier = Modifier,
    isRightSide: Boolean = false,
    onOpenDrawer: () -> Unit,
) {
    val density = LocalDensity.current
    val openThresholdPx = with(density) { 36.dp.toPx() }

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .width(if (isRightSide) 30.dp else 40.dp)
                .pointerInput(openThresholdPx, isRightSide) {
                    var accumulatedDrag = 0f
                    var opened = false

                    detectHorizontalDragGestures(
                        onDragStart = {
                            accumulatedDrag = 0f
                            opened = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            val delta = if (isRightSide) -dragAmount else dragAmount
                            if (delta <= 0f || opened) return@detectHorizontalDragGestures

                            accumulatedDrag += delta
                            change.consume()

                            if (accumulatedDrag >= openThresholdPx) {
                                opened = true
                                onOpenDrawer()
                            }
                        },
                    )
                },
    )
}

@Composable
internal fun UnifiedActivity.GlassesSettingsSheet(onDismiss: () -> Unit) {
    val gm = com.winlator.cmod.runtime.display.GlassesManager
    val settings by gm.settings.collectAsState()
    val brightnessMax = gm.brightnessMax()
    val volumeMax = gm.volumeMax()
    val brightness = if (settings.brightness < 0) brightnessMax else settings.brightness
    val volume = if (settings.volume < 0) volumeMax else settings.volume
    val registry = remember { PaneNavRegistry() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(LocalPaneNav provides registry) {
        DialogPaneNav(registry, onDismiss = onDismiss)
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth(0.82f),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Eyeglasses2Icon, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(gm.modelName(), color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassesLabel(stringResource(R.string.glasses_panel_refresh))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(60, 90, 120).forEach { hz ->
                                    val selected = settings.refreshHz == hz
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(if (selected) Accent else TextSecondary.copy(alpha = 0.12f))
                                            .paneNavItem(cornerRadius = 11.dp, onActivate = { gm.setRefreshHz(hz) }, isEntry = hz == 60)
                                            .clickable { gm.setRefreshHz(hz) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("$hz", color = if (selected) SurfaceDark else TextPrimary,
                                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GlassesToggleTile(stringResource(R.string.glasses_panel_sunblock),
                                settings.sunblock, Modifier.weight(1f)) { gm.setSunblock(it) }
                            GlassesToggleTile(stringResource(R.string.session_drawer_output_3d),
                                settings.threeD, Modifier.weight(1f)) { gm.set3D(it) }
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassesPercentSlider(stringResource(R.string.session_drawer_output_brightness),
                            brightness, brightnessMax) { gm.setBrightness(it) }
                        GlassesPercentSlider(stringResource(R.string.session_drawer_output_volume),
                            volume, volumeMax) { gm.setVolume(it) }
                    }
                }
            }
        }
        }
    }
}

@Composable
internal fun UnifiedActivity.GlassesLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
internal fun UnifiedActivity.GlassesPercentSlider(label: String, level: Int, max: Int, onChange: (Int) -> Unit) {
    val pct = if (max > 0) Math.round(level * 100f / max) else 0
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GlassesLabel(label)
            Text("$pct%", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        androidx.compose.material3.Slider(
            value = level.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..max.toFloat(),
            steps = (max - 1).coerceAtLeast(0),
            modifier = Modifier.paneNavItem(
                cornerRadius = 8.dp,
                onAdjust = { dir -> onChange((level + dir).coerceIn(0, max)) },
            ),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = TextSecondary.copy(alpha = 0.2f),
            ),
        )
    }
}

@Composable
internal fun UnifiedActivity.GlassesToggleTile(label: String, checked: Boolean, modifier: Modifier = Modifier, onChange: (Boolean) -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) Accent.copy(alpha = 0.16f) else TextSecondary.copy(alpha = 0.08f))
            .paneNavItem(cornerRadius = 13.dp, onActivate = { onChange(!checked) })
            .clickable { onChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
            ),
        )
    }
}

// Detects a connected Nintendo controller (Switch Pro Controller / Joy-Con, wired
// or paired over Bluetooth) so the top-bar badges can switch from the Xbox-style
// L1/R1/Select/Start glyphs to the Nintendo-native L/R/-/+ glyphs. This mirrors
// controllerState.isPlayStation but is resolved independently here via InputManager
// device name sniffing, since Nintendo detection isn't part of that state class yet.
@Composable
private fun rememberIsNintendoController(): Boolean {
    val nintendoDetectContext = LocalContext.current
    var isNintendo by remember { mutableStateOf(false) }

    fun refreshNintendoDetection() {
        val inputManager = nintendoDetectContext.getSystemService(InputManager::class.java)
        val deviceIds: IntArray = inputManager?.inputDeviceIds ?: IntArray(0)
        isNintendo = deviceIds.any { deviceId ->
            val device = inputManager?.getInputDevice(deviceId)
            val sources = device?.sources ?: 0
            val isGameController =
                (sources and android.view.InputDevice.SOURCE_GAMEPAD) == android.view.InputDevice.SOURCE_GAMEPAD ||
                    (sources and android.view.InputDevice.SOURCE_JOYSTICK) == android.view.InputDevice.SOURCE_JOYSTICK
            val deviceName = device?.name?.lowercase().orEmpty()
            isGameController && (
                deviceName.contains("nintendo") ||
                    deviceName.contains("switch") ||
                    deviceName.contains("joy-con") ||
                    deviceName.contains("joycon") ||
                    deviceName.contains("pro controller")
            )
        }
    }

    DisposableEffect(Unit) {
        refreshNintendoDetection()
        val inputManager = nintendoDetectContext.getSystemService(InputManager::class.java)
        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) = refreshNintendoDetection()
            override fun onInputDeviceRemoved(deviceId: Int) = refreshNintendoDetection()
            override fun onInputDeviceChanged(deviceId: Int) = refreshNintendoDetection()
        }
        inputManager?.registerInputDeviceListener(listener, null)
        onDispose { inputManager?.unregisterInputDeviceListener(listener) }
    }

    return isNintendo
}

// Scales button sizing to the device's actual screen size instead of a single
// fixed dp constant. Based on the smallest screen dimension (stable across
// rotation) relative to a ~411dp baseline phone width - a small phone shrinks
// slightly, a tablet grows - clamped so extreme screen sizes can't produce
// absurdly tiny or oversized touch targets.
@Composable
private fun rememberUiScale(): Float {
    val configuration = LocalConfiguration.current
    val smallestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp).toFloat()
    return (smallestWidthDp / 411f).coerceIn(0.85f, 1.6f)
}

// A thin vertical rule used inside the unified top-bar action pill to separate
// logical button groups (settings/search, filter/friends, files/add, glasses)
// without giving each group its own background or border.
@Composable
private fun ActionBarDivider() {
    Box(
        Modifier
            .height(20.dp)
            .width(1.dp)
            .background(CardBorder),
    )
}

@Composable
internal fun UnifiedActivity.TopBar(
    tabs: List<TabDef>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
    persona: com.winlator.cmod.feature.stores.steam.data.SteamFriend?,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    isControllerConnected: Boolean,
    isPS: Boolean,
    isNintendo: Boolean,
    isLibraryTab: Boolean,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onFilterClicked: () -> Unit,
    onFriendsClicked: () -> Unit = {},
    onGameSettingsClicked: () -> Unit,
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDownloadsTab = tabs.getOrNull(selectedIdx)?.key == "downloads"
    val downloadRecords by DownloadCoordinator.records.collectAsState(
        initial = DownloadCoordinator.snapshotRecords(),
    )
    val hasActiveStoreDownload = downloadRecords.any {
        it.store in setOf(
            com.winlator.cmod.app.db.download.DownloadRecord.STORE_STEAM,
            com.winlator.cmod.app.db.download.DownloadRecord.STORE_EPIC,
            com.winlator.cmod.app.db.download.DownloadRecord.STORE_GOG,
        ) && it.status == com.winlator.cmod.app.db.download.DownloadRecord.STATUS_DOWNLOADING
    }

    LaunchedEffect(selectedIdx) {
        if (isSearchExpanded) {
            onSearchQueryChange(TextFieldValue(""))
            isSearchExpanded = false
        }
    }

    // Auto-focus the search field when expanded
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            kotlinx.coroutines.delay(150)
            searchFocusRequester.requestFocus()
        } else if (searchQuery.text.isNotEmpty()) {
            onSearchQueryChange(TextFieldValue(""))
        }
    }

    val controllerSearchActivity = LocalContext.current as? UnifiedActivity
    LaunchedEffect(Unit) {
        controllerSearchActivity?.openSearchSignal?.collect {
            if (!isDownloadsTab) {
                if (isSearchExpanded) {
                    onSearchQueryChange(TextFieldValue(""))
                    isSearchExpanded = false
                } else {
                    isSearchExpanded = true
                }
            }
        }
    }

    // The hub has two deliberately different control geometries:
    //  - Landscape/tablet: wide, rounded console controls.
    //  - Portrait/phone: compact Windows-Phone-like square controls.
    // Keep the two layouts independent so controls never collide when the window changes
    // orientation.
    val topBarConfiguration = LocalConfiguration.current
    val isLandscapeHub = topBarConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isNarrowTopBar = !isLandscapeHub
    val uiScale = rememberUiScale()
    val actionButtonSize = (if (isNarrowTopBar) 40 else 44).dp * uiScale
    val actionIconSize = actionButtonSize * 0.5f
    val actionShape = if (isNarrowTopBar) RoundedCornerShape(11.dp) else CircleShape
    val tabShape = if (isNarrowTopBar) RoundedCornerShape(10.dp) else RoundedCornerShape(16.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = UnifiedTopBarHorizontalPadding,
                        end = UnifiedTopBarHorizontalPadding,
                        top = UnifiedTopBarTopPadding,
                    )
                    .height(UnifiedTopBarHeight),
        ) {
            // Tabs and the action bar are laid out one after another in a single Row instead
            // of being independently positioned with align(Center) / align(CenterEnd) + zIndex.
            // The old approach never reserved space for either block, so a wide action bar
            // (every optional icon visible) could slide left over the tabs - or, on narrow
            // phones, cover them completely since it was drawn on top. A sequential Row makes
            // that impossible: Compose measures the tabs first, then gives the action bar
            // whatever space is left, so the two can never render on top of one another in
            // portrait or landscape.
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Settings used to live inside the crowded right-side action pill along with
                // six other icons. It now gets its own spot - the top-left corner in landscape
                // (previously just dead space reserved to center the tabs) or directly ahead
                // of the tabs in portrait - so controls aren't all piled on top of each other
                // while the rest of the bar sits empty.
                @Suppress("DEPRECATION")
                val settingsCornerButton: @Composable () -> Unit = {
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalRippleConfiguration provides
                            androidx.compose.material3.RippleConfiguration(color = Accent),
                    ) {
                        val settingsInteractionSource = remember { MutableInteractionSource() }
                        val settingsPressed by settingsInteractionSource.collectIsPressedAsState()
                        Box(
                            modifier =
                                Modifier
                                    .size(actionButtonSize)
                                    .graphicsLayer {
                                        val s = if (settingsPressed) 0.95f else 1f
                                        scaleX = s; scaleY = s
                                    }
                                    .clip(actionShape)
                                    .background(CardDark)
                                    .border(1.dp, CardBorder, actionShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(
                                onClick = { navigateToSettings(SettingsNavItem.STORES) },
                                interactionSource = settingsInteractionSource,
                                modifier = Modifier.size(actionButtonSize).focusProperties { canFocus = !isLibraryTab },
                            ) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Menu", tint = Accent, modifier = Modifier.size(actionIconSize))
                            }
                        }
                        if (isControllerConnected) {
                            Spacer(Modifier.width(6.dp))
                            ControllerBadge(if (isNintendo) "+" else if (isPS) "\u2261" else "Start")
                        }
                    }
                }

                if (!isNarrowTopBar) {
                    // Landscape has room to spare: mirror the action bar's flexible space on
                    // the left so the tab pill still lands at the true horizontal center, and
                    // put the settings button in what used to be an empty box.
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) { settingsCornerButton() }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) { settingsCornerButton() }
                    Spacer(Modifier.width(8.dp))
                }

                // Library / Downloads are standalone navigation buttons, not tabs.
                // Store providers are available only from the Store action below.
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    @Suppress("DEPRECATION")
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalRippleConfiguration provides null,
                    ) {
                        listOf("library" to "LIBRARY", "downloads" to "DOWNLOADS").forEach { (key, label) ->
                            val selected = tabs.getOrNull(selectedIdx)?.key == key
                            val interactionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .height(if (isNarrowTopBar) 40.dp else 44.dp)
                                    .widthIn(min = if (isNarrowTopBar) 82.dp else 100.dp)
                                    .clip(if (isNarrowTopBar) RoundedCornerShape(10.dp) else RoundedCornerShape(16.dp))
                                    .background(if (selected) Accent.copy(alpha = 0.10f) else CardDark)
                                    .border(1.dp, if (selected) Accent.copy(alpha = 0.55f) else CardBorder, if (isNarrowTopBar) RoundedCornerShape(10.dp) else RoundedCornerShape(16.dp))
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        val index = tabs.indexOfFirst { it.key == key }
                                        if (index >= 0) onSelect(index)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selected) Accent else TextSecondary,
                                )
                                if (key == "downloads" && hasActiveStoreDownload) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 5.dp, end = 7.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(DangerRed),
                                    )
                                }
                            }
                        }
                    }
                }

                val topBarView = androidx.compose.ui.platform.LocalView.current
                val topBarDensity = androidx.compose.ui.platform.LocalDensity.current
                val topBarOrientation = androidx.compose.ui.platform.LocalConfiguration.current.orientation
                val navRightInset = remember(topBarOrientation, topBarView) {
                    val px = androidx.core.view.ViewCompat.getRootWindowInsets(topBarView)
                        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())?.right ?: 0
                    with(topBarDensity) { px.toDp() }
                }

                // Single unified action bar: every top-bar control (settings, search, filter,
                // friends, files, add, glasses) lives in one pill instead of split groups.
                // It now occupies its own flexible slot next to the tabs (instead of floating
                // independently on top of them), so on narrow phones it simply shrinks and
                // horizontally scrolls within its own bounds rather than covering anything.
                val actionBarScrollState = rememberScrollState()
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .height(actionButtonSize)
                                .widthIn(max = 320.dp)
                                .clip(actionShape)
                                .background(CardDark)
                                .border(1.dp, CardBorder, actionShape)
                                .horizontalScroll(actionBarScrollState),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        @Suppress("DEPRECATION")
                        CompositionLocalProvider(
                            androidx.compose.material3.LocalRippleConfiguration provides
                                androidx.compose.material3.RippleConfiguration(color = Accent),
                        ) {
                            IconButton(
                                onClick = {
                                    if (!isDownloadsTab) {
                                        if (isSearchExpanded) {
                                            onSearchQueryChange(TextFieldValue(""))
                                            isSearchExpanded = false
                                        } else {
                                            isSearchExpanded = true
                                        }
                                    }
                                },
                                modifier = Modifier.size(actionButtonSize).focusProperties { canFocus = !isLibraryTab },
                                enabled = !isDownloadsTab,
                            ) {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint =
                                        if (isDownloadsTab) {
                                            TextSecondary.copy(alpha = 0.4f)
                                        } else {
                                            Accent
                                        },
                                    modifier = Modifier.size(actionIconSize),
                                )
                            }
                            if (isControllerConnected) {
                                ControllerBadge("L3")
                            }

                            ActionBarDivider()

                            IconButton(
                                onClick = onFilterClicked,
                                modifier = Modifier.size(actionButtonSize).focusProperties { canFocus = !isLibraryTab },
                            ) {
                                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = Accent, modifier = Modifier.size(actionIconSize))
                            }
                            if (isControllerConnected) {
                                ControllerBadge(if (isNintendo) "-" else "Select")
                            }

                            ActionBarDivider()

                            var showStoreMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showStoreMenu = true },
                                    modifier = Modifier.size(actionButtonSize),
                                ) {
                                    Icon(
                                        Icons.Outlined.Storefront,
                                        contentDescription = "Store",
                                        tint = Accent,
                                        modifier = Modifier.size(actionIconSize),
                                    )
                                }
                                DropdownMenu(
                                    expanded = showStoreMenu,
                                    onDismissRequest = { showStoreMenu = false },
                                ) {
                                    listOf(
                                        "steam" to "Steam",
                                        "epic" to "Epic Games",
                                        "gog" to "GOG",
                                    ).forEach { (storeKey, storeLabel) ->
                                        DropdownMenuItem(
                                            text = { Text(storeLabel) },
                                            onClick = {
                                                showStoreMenu = false
                                                // Store destinations are not tabs. Keep the existing
                                                // internal destination model while exposing them only
                                                // through the Store menu.
                                                val storeIndex = tabs.indexOfFirst { it.key == storeKey }
                                                if (storeIndex >= 0) onSelect(storeIndex)
                                            },
                                        )
                                    }
                                }
                            }

                            ActionBarDivider()

                            IconButton(
                                onClick = onFriendsClicked,
                                modifier = Modifier.size(actionButtonSize),
                            ) {
                                Icon(Icons.Outlined.People, contentDescription = "Friends", tint = Accent, modifier = Modifier.size(actionIconSize))
                            }
                        }

                        if (isControllerConnected && navRightInset <= 0.dp) {
                            ActionBarDivider()
                            Box(
                                modifier =
                                    Modifier
                                        .background(Color(0xFF394048), RoundedCornerShape(15.dp))
                                        .border(1.dp, Color(0xFF8B949E).copy(alpha = 0.5f), RoundedCornerShape(15.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.SportsEsports,
                                    contentDescription = "Guide",
                                    tint = Color(0xFFE6EDF3),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (isControllerConnected && navRightInset > 0.dp) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 38.dp)
                                    .zIndex(2f)
                                    .background(Color(0xFF394048), RoundedCornerShape(15.dp))
                                    .border(1.dp, Color(0xFF8B949E).copy(alpha = 0.5f), RoundedCornerShape(15.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.SportsEsports,
                                contentDescription = "Guide",
                                tint = Color(0xFFE6EDF3),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        // Expanding search used to just pop this row in instantly, which read as the
        // header snapping/collapsing into place. Animating it as a vertical expand makes
        // the window below visibly slide down to make room instead.
        AnimatedVisibility(
            visible = isSearchExpanded && !isDownloadsTab,
            enter = expandVertically(animationSpec = tween(150), expandFrom = Alignment.Top) + fadeIn(tween(150)),
            exit = shrinkVertically(animationSpec = tween(90), shrinkTowards = Alignment.Top) + fadeOut(tween(140)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(0.7f)
                            .height(if (isNarrowTopBar) 40.dp else 44.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, if (isNarrowTopBar) RoundedCornerShape(10.dp) else RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle =
                                TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            cursorBrush = SolidColor(Accent),
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .focusRequester(searchFocusRequester),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.text.isEmpty()) {
                                        Text(
                                            "Search games",
                                            style =
                                                TextStyle(
                                                    color = TextSecondary,
                                                    fontSize = 15.sp,
                                                ),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        if (searchQuery.text.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange(TextFieldValue("")) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    } // end Column
}

// Files, Add-Custom-Game, and Glasses used to live at the tail end of the crowded
// top-right action pill. They're now a separate small cluster anchored to the
// bottom-right corner of the screen - that corner used to just be empty space,
// and splitting the seven top-bar controls into two groups keeps either from
// feeling like a pile of icons stacked on top of each other.
@Composable
internal fun UnifiedActivity.LibraryCornerActions(
    isLibraryTab: Boolean,
    onFilesClicked: () -> Unit,
    onAddGameClicked: () -> Unit,
) {
    val glassesConnected by com.winlator.cmod.runtime.display.GlassesManager.connected.collectAsState()
    var showGlassesPanel by remember { mutableStateOf(false) }

    val cornerActionsActivity = LocalContext.current as? UnifiedActivity
    LaunchedEffect(Unit) {
        cornerActionsActivity?.openGlassesSignal?.collect {
            if (glassesConnected) showGlassesPanel = true
        }
    }

    if (isLibraryTab || glassesConnected) {
        val clusterUiScale = rememberUiScale()
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp * clusterUiScale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            @Suppress("DEPRECATION")
            CompositionLocalProvider(
                androidx.compose.material3.LocalRippleConfiguration provides
                    androidx.compose.material3.RippleConfiguration(color = Accent),
            ) {
                if (isLibraryTab) {
                    CornerActionButton(
                        onClick = onFilesClicked,
                        icon = Icons.Outlined.FolderOpen,
                        contentDescription = "Files",
                    )
                    CornerActionButton(
                        onClick = onAddGameClicked,
                        icon = Icons.Outlined.Add,
                        contentDescription = "Add Custom Game",
                    )
                }

                if (glassesConnected) {
                    CornerActionButton(
                        onClick = { showGlassesPanel = true },
                        icon = Eyeglasses2Icon,
                        contentDescription = "Glasses",
                    )
                }
            }
        }
    }

    if (showGlassesPanel) GlassesSettingsSheet(onDismiss = { showGlassesPanel = false })
}

// A single round button used by the bottom-right corner cluster. Stacked vertically
// instead of packed into one horizontal pill, so each button can be a bit bigger
// (52dp vs. the old 44dp) without the group getting wider than a thumb can reach.
@Composable
private fun CornerActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
) {
    val uiScale = rememberUiScale()
    val buttonSize = 48.dp * uiScale
    val iconSize = 20.dp * uiScale
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .size(buttonSize)
                .smoothPress(interactionSource, pressedScale = 0.95f)
                .clip(CircleShape)
                .background(CardDark)
                .border(1.dp, CardBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.size(buttonSize).focusProperties { canFocus = false },
        ) {
            Icon(icon, contentDescription = contentDescription, tint = Accent, modifier = Modifier.size(iconSize))
        }
    }
}


@Composable
internal fun UnifiedActivity.LibraryCarousel(
    isLoggedIn: Boolean,
    steamApps: List<SteamApp>,
    epicApps: List<EpicGame>,
    gogApps: List<GOGGame>,
    libraryRefreshKey: Int = 0,
    shortcutRefreshKey: Int = 0,
    playtimeRefreshKey: Int = 0,
    iconRefreshKey: Int = 0,
    searchQuery: String = "",
    isControllerConnected: Boolean = false,
) {
    val context = LocalContext.current
    val layoutMode = LibraryLayoutMode.CONSOLE_TILE

    var cachedShortcuts by remember { mutableStateOf<List<Shortcut>>(emptyList()) }
    var customApps by remember { mutableStateOf<List<SteamApp>>(emptyList()) }
    var localLibraryRefreshKey by remember { mutableIntStateOf(0) }
    var shortcutsLoaded by remember { mutableStateOf(false) }
    var pullRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(shortcutRefreshKey, localLibraryRefreshKey) {
        shortcutsLoaded = false

        // Pull-to-refresh only: rescan disk so a manually moved game is picked up without faking a re-download.
        // Skipped on the initial pass because the scan walks every known app.
        if (pullRefreshing) {
            runCatching {
                withContext(Dispatchers.IO) { SteamService.repairInstalledMetadataFromDisk() }
            }.onFailure { Log.w("UnifiedActivity", "Pull-to-refresh install repair failed", it) }
        }

        val shortcutScanResult =
            runCatching {
                withContext(Dispatchers.IO) {
                    val cm = ContainerManager(context)
                    cm.upgradeShortcuts {
                        localLibraryRefreshKey++
                    }
                    val allShortcuts = cm.loadShortcuts()
                    val badges = HashMap<Int, String>()
                    val apps =
                        allShortcuts
                            .mapNotNull { shortcut ->
                                if (!LibraryShortcutUtils.isCustomLibraryShortcut(shortcut)) {
                                    return@mapNotNull null
                                }

                                val displayName =
                                    shortcut
                                        .getExtra("custom_name", shortcut.name)
                                        .ifBlank { shortcut.name }

                                val uuid = shortcut.getExtra("uuid")
                                val customId = if (uuid.isNotEmpty()) {
                                    -(uuid.hashCode().and(0x7FFFFFFF) + 1)
                                } else {
                                    -(displayName.hashCode().and(0x7FFFFFFF) + 1)
                                }

                                SteamApp(
                                    id = customId,
                                    name = displayName,
                                    developer = "Custom",
                                    gameDir =
                                        shortcut.getExtra(
                                            "game_install_path",
                                            shortcut.getExtra("custom_game_folder", ""),
                                        ),
                                )
                            }

                    Triple(allShortcuts, apps, badges)
                }
            }.getOrNull()

        if (shortcutScanResult != null) {
            cachedShortcuts = shortcutScanResult.first
            customApps = shortcutScanResult.second
        }

        shortcutsLoaded = true
    }

    // Move library filtering and file checks off the main thread.
    var mergedInstalledApps by remember { mutableStateOf<List<SteamApp>>(emptyList()) }
    var installedApps by remember { mutableStateOf<List<SteamApp>>(emptyList()) }
    var stableInstalledApps by remember { mutableStateOf<List<SteamApp>>(emptyList()) }
    var gogByPseudoId by remember { mutableStateOf<Map<Int, GOGGame>>(emptyMap()) }
    var epicByPseudoId by remember { mutableStateOf<Map<Int, EpicGame>>(emptyMap()) }
    var stableGogByPseudoId by remember { mutableStateOf<Map<Int, GOGGame>>(emptyMap()) }
    var stableEpicByPseudoId by remember { mutableStateOf<Map<Int, EpicGame>>(emptyMap()) }
    var customListArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var customHeroArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var customCarouselArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var customArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var customIconArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var customIconPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomIconArtworkPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomIconPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomHeroPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomCarouselPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var stableCustomListPathByAppId by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var artworkCacheRefreshKey by remember { mutableIntStateOf(0) }
    var libraryLoaded by remember { mutableStateOf(false) }
    // Suppress transient empty states before background recomputation starts.
    val scanInputToken =
        remember(steamApps, epicApps, gogApps, customApps, libraryRefreshKey, localLibraryRefreshKey) { Any() }
    var processedScanToken by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(scanInputToken) {
        withContext(Dispatchers.IO) {
            val steamInstalled = steamApps.filter { SteamService.isAppInstalled(it.id) }

            val epicInstalled = epicApps.filter { it.isInstalled }

            // Match Epic's DB-backed install filter during verify/update.
            val gogInstalled = gogApps.filter { it.isInstalled }

            val gogMap = gogInstalled.associateBy { gogPseudoId(it.id) }
            val epicMap = epicInstalled.associateBy { 2000000000 + it.id }

            val playtimePrefs = context.getSharedPreferences("playtime_stats", android.content.Context.MODE_PRIVATE)
            val allPlaytime = playtimePrefs.all
            val mappedEpic =
                epicInstalled.map { epic ->
                    SteamApp(
                        id = 2000000000 + epic.id,
                        name = epic.title,
                        developer = epic.developer,
                        gameDir = epic.installPath,
                    )
                }
            val mappedGog =
                gogInstalled.map { gog ->
                    SteamApp(
                        id = gogPseudoId(gog.id),
                        name = gog.title,
                        developer = gog.developer,
                        gameDir = gog.installPath,
                    )
                }
            val merged = steamInstalled + customApps + mappedEpic + mappedGog
            val sorted =
                merged.sortedByDescending { app ->
                    val searchKey =
                        if (app.id >= 2000000000 || app.id < 0) {
                            app.name
                        } else {
                            app.name.replace(LIBRARY_NAME_SANITIZE_REGEX, "")
                        }
                    (allPlaytime["${searchKey}_last_played"] as? Long) ?: 0L
                }

            withContext(Dispatchers.Main) {
                gogByPseudoId = gogMap
                epicByPseudoId = epicMap
                mergedInstalledApps = merged
                installedApps = sorted
                if (sorted.isNotEmpty()) {
                    stableInstalledApps = sorted
                    stableGogByPseudoId = gogMap
                    stableEpicByPseudoId = epicMap
                }
                libraryLoaded = true
                processedScanToken = scanInputToken
                pullRefreshing = false
            }
        }
    }

    LaunchedEffect(installedApps, gogByPseudoId, cachedShortcuts, iconRefreshKey) {
        val appsSnapshot = installedApps
        val gogSnapshot = gogByPseudoId
        val shortcutsSnapshot = cachedShortcuts

        val artworkPaths =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        val gogGame = gogSnapshot[app.id]
                        val isCustom = app.id < 0
                        val isEpic = app.id >= 2000000000
                        val epicId = if (isEpic) app.id - 2000000000 else 0
                        val shortcut =
                            if (gogGame != null) {
                                shortcutsSnapshot.find {
                                    it.getExtra("game_source") == "GOG" && it.getExtra("gog_id") == gogGame.id
                                }
                            } else {
                                findShortcutForGame(shortcutsSnapshot, app, isCustom, isEpic, epicId)
                            }
                        val customPath =
                            shortcut
                                ?.getExtra("customLibraryIconPath")
                                ?.ifBlank { shortcut.getExtra("customCoverArtPath") }
                        if (!customPath.isNullOrBlank() && java.io.File(customPath).exists()) {
                            put(app.id, customPath)
                        }
                    }
                }
            }

        val iconArtworkPaths =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        val gogGame = gogSnapshot[app.id]
                        val isCustom = app.id < 0
                        val isEpic = app.id >= 2000000000
                        val epicId = if (isEpic) app.id - 2000000000 else 0
                        val shortcut =
                            if (gogGame != null) {
                                shortcutsSnapshot.find {
                                    it.getExtra("game_source") == "GOG" && it.getExtra("gog_id") == gogGame.id
                                }
                            } else {
                                findShortcutForGame(shortcutsSnapshot, app, isCustom, isEpic, epicId)
                            }
                        val customPath = shortcut?.let(LibraryShortcutArtwork::findIconArtworkPath)
                        if (customPath != null) {
                            put(app.id, customPath)
                        }
                    }
                }
            }

        val customHeroPath =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        if (app.id >= 0) return@forEach
                        val shortcut = findShortcutForGame(shortcutsSnapshot, app, true, false, 0) ?: return@forEach
                        val heroPath = shortcut.getExtra("customLibraryHeroArtPath")
                        if (heroPath.isNullOrBlank() || !java.io.File(heroPath).isFile)
                            return@forEach
                        put(app.id, heroPath)
                    }
                }
            }

        val customCarouselPath =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        if (app.id >= 0) return@forEach
                        val shortcut = findShortcutForGame(shortcutsSnapshot, app, true, false, 0) ?: return@forEach
                        val carouselPath = shortcut.getExtra("customLibraryCarouselArtPath")
                        if (carouselPath.isNullOrBlank() || !java.io.File(carouselPath).isFile)
                            return@forEach
                        put(app.id, carouselPath)
                    }
                }
            }

        val customListPath =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        if (app.id >= 0) return@forEach
                        val shortcut = findShortcutForGame(shortcutsSnapshot, app, true, false, 0) ?: return@forEach
                        val listPath = shortcut.getExtra("customLibraryListArtPath")
                        if (listPath.isNullOrBlank() || !java.io.File(listPath).isFile)
                            return@forEach
                        put(app.id, listPath)
                    }
                }
            }

        val customIconPaths =
            withContext(Dispatchers.IO) {
                buildMap<Int, String> {
                    appsSnapshot.forEach { app ->
                        if (app.id >= 0) return@forEach
                        val safeName = app.name.replace("/", "_").replace("\\", "_")
                        val iconFile = java.io.File(context.filesDir, "custom_icons/$safeName.png")
                        if (iconFile.exists()) {
                            put(app.id, iconFile.absolutePath)
                        }
                    }
                }
            }

        customArtworkPathByAppId = artworkPaths
        customIconArtworkPathByAppId = iconArtworkPaths
        customIconPathByAppId = customIconPaths
        customHeroArtworkPathByAppId = customHeroPath
        customCarouselArtworkPathByAppId = customCarouselPath
        customListArtworkPathByAppId = customListPath
        if (appsSnapshot.isNotEmpty()) {
            stableCustomArtworkPathByAppId = artworkPaths
            stableCustomIconArtworkPathByAppId = iconArtworkPaths
            stableCustomIconPathByAppId = customIconPaths
            stableCustomHeroPathByAppId = customHeroPath
            stableCustomCarouselPathByAppId = customCarouselPath
            stableCustomListPathByAppId = customListPath
        }
    }

    LaunchedEffect(mergedInstalledApps, playtimeRefreshKey) {
        if (mergedInstalledApps.isEmpty()) {
            installedApps = emptyList()
            return@LaunchedEffect
        }

        val sorted =
            withContext(Dispatchers.IO) {
                val playtimePrefs = context.getSharedPreferences("playtime_stats", android.content.Context.MODE_PRIVATE)
                val allPlaytime = playtimePrefs.all
                mergedInstalledApps.sortedByDescending { app ->
                    val searchKey =
                        if (app.id >= 2000000000 || app.id < 0) {
                            app.name
                        } else {
                            app.name.replace(LIBRARY_NAME_SANITIZE_REGEX, "")
                        }
                    (allPlaytime["${searchKey}_last_played"] as? Long) ?: 0L
                }
            }

        installedApps = sorted
    }

    val awaitingShortcutScan = installedApps.isEmpty() && !shortcutsLoaded
    val keepPreviousLibraryVisible =
        installedApps.isEmpty() &&
            stableInstalledApps.isNotEmpty() &&
            (processedScanToken !== scanInputToken || awaitingShortcutScan)
    val visibleInstalledApps = if (keepPreviousLibraryVisible) stableInstalledApps else installedApps
    val visibleGogByPseudoId = if (keepPreviousLibraryVisible) stableGogByPseudoId else gogByPseudoId
    val visibleEpicByPseudoId = if (keepPreviousLibraryVisible) stableEpicByPseudoId else epicByPseudoId
    val visibleCustomArtworkPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomArtworkPathByAppId else customArtworkPathByAppId
    val visibleCustomIconArtworkPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomIconArtworkPathByAppId else customIconArtworkPathByAppId
    val visibleCustomIconPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomIconPathByAppId else customIconPathByAppId
    val visibleCustomListPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomListPathByAppId else customListArtworkPathByAppId
    val visibleCustomHeroPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomHeroPathByAppId else customHeroArtworkPathByAppId
    val visibleCustomCarouselPathByAppId =
        if (keepPreviousLibraryVisible) stableCustomCarouselPathByAppId else customCarouselArtworkPathByAppId

    val displayedApps =
        remember(visibleInstalledApps, searchQuery) {
            if (searchQuery.isBlank()) {
                visibleInstalledApps
            } else {
                visibleInstalledApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }

    LaunchedEffect(
        visibleInstalledApps,
        visibleGogByPseudoId,
        visibleEpicByPseudoId,
        visibleCustomArtworkPathByAppId,
        visibleCustomIconArtworkPathByAppId,
        cachedShortcuts,
    ) {
        var deletedCustomOverrides = false
        val refs =
            visibleInstalledApps.flatMap { app ->
                val gogGame = visibleGogByPseudoId[app.id]
                val epicGame = visibleEpicByPseudoId[app.id]
                val overriddenSlots =
                    customArtworkOverrideSlots(
                        app = app,
                        gogGame = gogGame,
                        epicGame = epicGame,
                        hasDefaultCustomArt = visibleCustomArtworkPathByAppId[app.id] != null,
                        hasIconCustomArt = visibleCustomIconArtworkPathByAppId[app.id] != null,
                        hasHeroCustomArt =
                            findLibraryArtworkShortcut(cachedShortcuts, app, gogGame, epicGame)
                                ?.hasExistingArtwork(LibraryShortcutArtwork.LibraryArtworkSlot.GAME_CARD.extraKey) == true,
                    )

                if (overriddenSlots.isNotEmpty()) {
                    val cacheId = artworkCacheId(app, gogGame, epicGame)
                    if (cacheId != null) {
                        val deleted =
                            withContext(Dispatchers.IO) {
                                StoreArtworkCache.deleteSlots(context, cacheId.store, cacheId.gameId, overriddenSlots)
                            }
                        deletedCustomOverrides = deletedCustomOverrides || deleted
                    }
                }

                StoreArtworkCache
                    .libraryRefs(
                        app = app,
                        gogGame = gogGame,
                        epicGame = epicGame,
                    ).filterNot { it.slot in overriddenSlots }
            }
        val cachedAny =
            withContext(Dispatchers.IO) {
                StoreArtworkCache.cacheAll(context, refs)
            }
        if (cachedAny || deletedCustomOverrides) artworkCacheRefreshKey++
    }

    // The startup bootstrap screen already masks the first frame. Do not
    // force an extra minimum spinner duration here or the library visibly
    // bounces through two loading states on launch.
    // A logged-in store whose owned-apps list is still empty hasn't finished
    // its initial library fetch yet — keep the spinner up instead of flashing
    // "No games installed". This resolves itself once the store populates its
    // DB (steamApps/epicApps/gogApps become non-empty) or if other sources
    // (custom apps, other stores) already have installed games.
    val awaitingStoreSync =
        installedApps.isEmpty() && (
            (isLoggedIn && steamApps.isEmpty()) ||
                (epicApps.isEmpty() && EpicService.hasStoredCredentials(context)) ||
                (gogApps.isEmpty() && GOGAuthManager.isLoggedIn(context))
        )
    // Only block the surface while the first library result is unresolved.
    // After that, keep the current content/empty state visible during
    // background refreshes so the UI does not flicker back to a spinner.
    val initialLibraryLoadPending = !libraryLoaded
    val waitingForFirstEmptyStateResolution =
        installedApps.isEmpty() && (processedScanToken !== scanInputToken || awaitingStoreSync || awaitingShortcutScan)
    val showLoading = initialLibraryLoadPending || waitingForFirstEmptyStateResolution
    if (showLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val spinAlpha = 1f
            CircularProgressIndicator(
                color = Accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp).alpha(spinAlpha),
            )
        }
        return
    }

    if (visibleInstalledApps.isEmpty()) {
        val epicLoggedIn by EpicAuthManager.isLoggedInFlow.collectAsState()
        val gogLoggedIn by GOGAuthManager.isLoggedInFlow.collectAsState()
        val anyLoggedIn = isLoggedIn || epicLoggedIn || gogLoggedIn
        val hasAnyCredentials =
            anyLoggedIn ||
                SteamService.hasStoredCredentials(context) ||
                EpicService.hasStoredCredentials(context) ||
                GOGAuthManager.isLoggedIn(context)
        if (!anyLoggedIn && !hasAnyCredentials) {
            LoginRequiredScreen("Library") {
                navigateToSettings(SettingsNavItem.STORES)
            }
        } else if (anyLoggedIn) {
            PullToRefreshBox(
                isRefreshing = pullRefreshing,
                onRefresh = {
                    pullRefreshing = true
                    localLibraryRefreshKey++
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateMessage(stringResource(R.string.library_games_no_games_installed))
                }
            }
        }
        return
    }

    var selectedAppForSettings by remember { mutableStateOf<SteamApp?>(null) }
    var selectedGogGameForSettings by remember { mutableStateOf<GOGGame?>(null) }
    var detailApp by remember { mutableStateOf<SteamApp?>(null) }
    var detailGogGame by remember { mutableStateOf<GOGGame?>(null) }
    val gridState = rememberLazyGridState()
    val carouselState = rememberLazyListState()
    val activity = LocalContext.current as? UnifiedActivity

    // Pause chasing borders on library cards while any dialog is open.
    LaunchedEffect(selectedAppForSettings, selectedGogGameForSettings, detailApp) {
        chasingBordersPaused.value =
            selectedAppForSettings != null || selectedGogGameForSettings != null || detailApp != null
    }
    DisposableEffect(Unit) {
        onDispose { chasingBordersPaused.value = false }
    }

    LaunchedEffect(layoutMode) {
        currentLibraryLayoutMode = layoutMode
    }

    // Keep activity's item count in sync
    LaunchedEffect(displayedApps.size) {
        activity?.libraryItemCount = displayedApps.size
        val lastIndex = (displayedApps.size - 1).coerceAtLeast(0)
        if (activity != null && displayedApps.isNotEmpty() && activity.libraryFocusIndex.value > lastIndex) {
            activity.libraryFocusIndex.value = lastIndex
        }
    }

    // Only the currently focused item needs a FocusRequester. Keeping one requester per
    // game makes large libraries unnecessarily heavy and increases composition pressure.
    val focusRequester = remember { FocusRequester() }

    // Observe focus index changes from the activity and request focus on the target item
    val focusIndex by (activity?.libraryFocusIndex ?: kotlinx.coroutines.flow.MutableStateFlow(0)).collectAsState()
    LaunchedEffect(focusIndex, displayedApps.size, layoutMode) {
        if (searchQuery.isEmpty() &&
            layoutMode == LibraryLayoutMode.GRID_4 &&
            displayedApps.isNotEmpty() &&
            focusIndex in displayedApps.indices
        ) {
            gridState.scrollToItem(focusIndex)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    // Track selected app for the top-right Game Settings button
    LaunchedEffect(focusIndex, displayedApps) {
        val app = displayedApps.getOrNull(focusIndex) ?: displayedApps.firstOrNull()
        selectedSteamAppId = app?.id ?: 0
        selectedSteamAppName = app?.name ?: ""
        val gogGame = app?.let { visibleGogByPseudoId[it.id] }
        selectedLibrarySource =
            when {
                gogGame != null -> "GOG"
                app == null -> ""
                app.id >= 2000000000 -> "EPIC"
                app.id < 0 -> "CUSTOM"
                else -> "STEAM"
            }
        selectedGogGameId = gogGame?.id.orEmpty()
    }

    val heroApps = rememberUpdatedState(displayedApps)
    val heroFocus = rememberUpdatedState(focusIndex)
    val heroGogMap = rememberUpdatedState(visibleGogByPseudoId)
    LaunchedEffect(Unit) {
        activity?.openHeroForFocusedSignal?.collect {
            val list = heroApps.value
            val app = list.getOrNull(heroFocus.value) ?: list.firstOrNull()
            if (app != null) {
                detailGogGame = heroGogMap.value[app.id]
                detailApp = app
            }
        }
    }

    // Publish the focused game's hero art (custom card > store hero > grid capsule) for the immersive background; shortcuts load once per refresh signal, not per focus move.
    var immersiveShortcuts by remember { mutableStateOf<List<Shortcut>?>(null) }
    LaunchedEffect(shortcutRefreshKey, libraryRefreshKey, artworkCacheRefreshKey) {
        immersiveShortcuts =
            withContext(Dispatchers.IO) { ContainerManager(context).loadShortcuts() }
    }

    LaunchedEffect(focusIndex, displayedApps, immersiveShortcuts) {
        val shortcuts = immersiveShortcuts ?: return@LaunchedEffect
        val app = displayedApps.getOrNull(focusIndex) ?: displayedApps.firstOrNull()
        if (app == null) {
            activity?.immersiveBackgroundRef?.value = null
            return@LaunchedEffect
        }
        // Debounce so scrubbing the grid doesn't decode every intermediate hero.
        delay(200)
        val gogGame = visibleGogByPseudoId[app.id]
        val epicGame = visibleEpicByPseudoId[app.id]
        val isCustom = app.id < 0
        val isEpic = app.id >= 2000000000
        val epicId = if (isEpic) app.id - 2000000000 else 0

        val shortcut =
            when {
                gogGame != null ->
                    shortcuts.find {
                        it.getExtra("game_source") == "GOG" && it.getExtra("gog_id") == gogGame.id
                    }
                else -> findShortcutForGame(shortcuts, app, isCustom, isEpic, epicId)
            }
        val customHeroFile =
            withContext(Dispatchers.IO) {
                shortcut
                    ?.getExtra(LibraryShortcutArtwork.LibraryArtworkSlot.GAME_CARD.extraKey)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { java.io.File(it) }
                    ?.takeIf { it.isFile }
            }

        activity?.immersiveBackgroundRef?.value =
            customHeroFile
                ?: run {
                    val ref =
                        StoreArtworkCache.heroRef(app, gogGame, epicGame)
                            ?: StoreArtworkCache.primaryRef(
                                app,
                                gogGame,
                                epicGame,
                                useLibraryCapsule = false,
                                listMode = false,
                            )
                    StoreArtworkCache.imageModel(context, ref)
                }
    }

    val openSettingsForApp: (Int, SteamApp) -> Unit = { index, app ->
        activity?.libraryFocusIndex?.value = index
        selectedSteamAppId = app.id
        selectedSteamAppName = app.name
        val gogGame = visibleGogByPseudoId[app.id]
        selectedLibrarySource =
            when {
                gogGame != null -> "GOG"
                app.id >= 2000000000 -> "EPIC"
                app.id < 0 -> "CUSTOM"
                else -> "STEAM"
            }
        selectedGogGameId = gogGame?.id.orEmpty()

        if (gogGame != null) {
            selectedGogGameForSettings = gogGame
        } else {
            selectedAppForSettings = app
        }
    }

    PullToRefreshBox(
        isRefreshing = pullRefreshing,
        onRefresh = {
            pullRefreshing = true
            localLibraryRefreshKey++
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        when (layoutMode) {
            LibraryLayoutMode.GRID_4 -> {
                FourByTwoGridView(
                    items = displayedApps,
                    modifier = Modifier.tabScreenPadding(),
                    gridState = gridState,
                    contentPadding = TabGridContentPadding,
                    clipContent = false,
                    keyOf = { it.id },
                ) { app, index, rowHeight ->
                    GameCapsule(
                        app = app,
                        gogGame = visibleGogByPseudoId[app.id],
                        epicGame = visibleEpicByPseudoId[app.id],
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        isFocusedOverride = index == focusIndex,
                        isControllerActive = isControllerConnected,
                        customArtworkPath = visibleCustomIconArtworkPathByAppId[app.id] ?: visibleCustomArtworkPathByAppId[app.id],
                        customIconPath = visibleCustomIconPathByAppId[app.id],
                        customListPath = visibleCustomListPathByAppId[app.id],
                        customHeroPath = visibleCustomHeroPathByAppId[app.id],
                        onClick = {
                            // Keeps the immersive background on the opened game after backing out.
                            activity?.libraryFocusIndex?.value = index
                            detailGogGame = visibleGogByPseudoId[app.id]
                            detailApp = app
                        },
                        onLongClick = {
                            openSettingsForApp(index, app)
                        },
                        modifier =
                            Modifier
                                .height(rowHeight)
                                .then(
                                    if (index == focusIndex) {
                                        Modifier.focusRequester(focusRequester)
                                    } else {
                                        Modifier
                                    },
                                ),
                    )
                }
            }

            LibraryLayoutMode.CAROUSEL -> {
                // Host the horizontal carousel in a same-height vertical scroll so a downward finger pull reaches the shared PullToRefreshBox.
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val carouselViewportHeight = maxHeight
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Box(Modifier.fillMaxWidth().height(carouselViewportHeight)) {
                            CarouselView(
                                items = displayedApps,
                                modifier = Modifier.tabScreenPadding(top = TabCarouselTopPadding, bottom = TabCarouselBottomPadding),
                                listState = carouselState,
                                selectedIndex = focusIndex,
                                onCenteredIndexChanged = { centeredIndex ->
                                    if (activity != null && activity.libraryFocusIndex.value != centeredIndex) {
                                        activity.libraryFocusIndex.value = centeredIndex
                                    }
                                },
                            ) { app, index, isSelected, cardWidth, cardHeight ->
                                GameCapsule(
                                    app = app,
                                    gogGame = visibleGogByPseudoId[app.id],
                                    epicGame = visibleEpicByPseudoId[app.id],
                                    iconRefreshKey = iconRefreshKey,
                                    artworkCacheRefreshKey = artworkCacheRefreshKey,
                                    isFocusedOverride = isSelected,
                                    isControllerActive = isControllerConnected,
                                    customArtworkPath = visibleCustomIconArtworkPathByAppId[app.id] ?: visibleCustomArtworkPathByAppId[app.id],
                                    customIconPath = visibleCustomIconPathByAppId[app.id],
                                    customListPath = visibleCustomListPathByAppId[app.id],
                                    customCarouselPath = visibleCustomCarouselPathByAppId[app.id],
                                    customHeroPath = visibleCustomHeroPathByAppId[app.id],
                                    onClick = {
                                        detailGogGame = visibleGogByPseudoId[app.id]
                                        detailApp = app
                                    },
                                    onLongClick = { openSettingsForApp(index, app) },
                                    useLibraryCapsule = true,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .then(
                                                if (index == focusIndex) {
                                                    Modifier.focusRequester(focusRequester)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                )
                            }
                        }
                    }
                }
            }

            LibraryLayoutMode.CONSOLE_TILE -> {
                val configOrientation = LocalConfiguration.current.orientation
                if (configOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ConsoleRowLibrary(
                        apps = displayedApps,
                        gogByPseudoId = visibleGogByPseudoId,
                        epicByPseudoId = visibleEpicByPseudoId,
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        focusIndex = focusIndex,
                        isControllerConnected = isControllerConnected,
                        customArtworkPathByAppId = visibleCustomIconArtworkPathByAppId,
                        fallbackArtworkPathByAppId = visibleCustomArtworkPathByAppId,
                        customIconPathByAppId = visibleCustomIconPathByAppId,
                        customListPathByAppId = visibleCustomListPathByAppId,
                        customHeroPathByAppId = visibleCustomHeroPathByAppId,
                        gridState = carouselState,
                        onAppFocused = { index -> activity?.libraryFocusIndex?.value = index },
                        onClick = { app, index ->
                            activity?.libraryFocusIndex?.value = index
                            detailGogGame = visibleGogByPseudoId[app.id]
                            detailApp = app
                        },
                        onLongClick = { app, index -> openSettingsForApp(index, app) },
                    )
                } else {
                    TileGridLibrary(
                        apps = displayedApps,
                        gogByPseudoId = visibleGogByPseudoId,
                        epicByPseudoId = visibleEpicByPseudoId,
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        focusIndex = focusIndex,
                        isControllerConnected = isControllerConnected,
                        customArtworkPathByAppId = visibleCustomIconArtworkPathByAppId,
                        fallbackArtworkPathByAppId = visibleCustomArtworkPathByAppId,
                        customIconPathByAppId = visibleCustomIconPathByAppId,
                        customListPathByAppId = visibleCustomListPathByAppId,
                        customHeroPathByAppId = visibleCustomHeroPathByAppId,
                        gridState = gridState,
                        onClick = { app, index ->
                            activity?.libraryFocusIndex?.value = index
                            detailGogGame = visibleGogByPseudoId[app.id]
                            detailApp = app
                        },
                        onLongClick = { app, index -> openSettingsForApp(index, app) },
                    )
                }
            }

            LibraryLayoutMode.LIST -> {
                val listViewState = rememberLazyListState()
                ListView(
                    items = displayedApps,
                    modifier = Modifier.tabScreenPadding(),
                    listState = listViewState,
                    contentPadding = TabListContentPadding,
                    selectedIndex = focusIndex,
                    onSelectedIndexChanged = { newIdx ->
                        activity?.libraryFocusIndex?.value = newIdx
                    },
                    keyOf = { it.id },
                ) { app, index, isSelected ->
                    GameCapsule(
                        app = app,
                        gogGame = visibleGogByPseudoId[app.id],
                        epicGame = visibleEpicByPseudoId[app.id],
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        isFocusedOverride = isSelected,
                        isControllerActive = isControllerConnected,
                        customArtworkPath = visibleCustomIconArtworkPathByAppId[app.id] ?: visibleCustomArtworkPathByAppId[app.id],
                        customIconPath = visibleCustomIconPathByAppId[app.id],
                        customListPath = visibleCustomListPathByAppId[app.id],
                        customHeroPath = visibleCustomHeroPathByAppId[app.id],
                        onClick = {
                            // Keeps the immersive background on the opened game after backing out.
                            activity?.libraryFocusIndex?.value = index
                            detailGogGame = visibleGogByPseudoId[app.id]
                            detailApp = app
                        },
                        onLongClick = { openSettingsForApp(index, app) },
                        listMode = true,
                        modifier =
                            Modifier
                                .then(
                                    if (index == focusIndex) {
                                        Modifier.focusRequester(focusRequester)
                                    } else {
                                        Modifier
                                    },
                                ),
                    )
                }
                JoystickListScroll(
                    listState = listViewState,
                    stickFlow = activity?.rightStickScrollState,
                    minSpeed = 2.5f,
                    maxSpeed = 16f,
                    quadratic = true,
                )
            }
        }
    }

    if (selectedAppForSettings != null) {
        GameSettingsDialog(
            app = selectedAppForSettings!!,
            onDismissRequest = { selectedAppForSettings = null },
        )
    }
    if (selectedGogGameForSettings != null) {
        GOGGameSettingsDialog(
            app = selectedGogGameForSettings!!,
            onDismissRequest = { selectedGogGameForSettings = null },
        )
    }
    if (detailApp != null) {
        LibraryGameDetailDialog(
            app = detailApp!!,
            gogGame = detailGogGame,
            onDismissRequest = {
                detailApp = null
                detailGogGame = null
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// CONSOLE_TILE layout mode: an additional, opt-in library layout.
// Landscape -> horizontal cover row (Nintendo Switch style).
// Portrait  -> two-column tile grid (Windows Phone style).
// Both reuse GameCapsule for artwork loading so caching/custom-art behavior
// stays identical to the other layout modes. No ripple/fade/crossfade -
// GameCapsule already renders with indication = null and crossfade = false,
// so taps register with zero animation delay.
// ─────────────────────────────────────────────────────────────────────────

@Composable
internal fun UnifiedActivity.ConsoleRowLibrary(
    apps: List<SteamApp>,
    gogByPseudoId: Map<Int, GOGGame>,
    epicByPseudoId: Map<Int, EpicGame>,
    iconRefreshKey: Int,
    artworkCacheRefreshKey: Int,
    focusIndex: Int,
    isControllerConnected: Boolean,
    customArtworkPathByAppId: Map<Int, String>,
    fallbackArtworkPathByAppId: Map<Int, String>,
    customIconPathByAppId: Map<Int, String>,
    customListPathByAppId: Map<Int, String>,
    customHeroPathByAppId: Map<Int, String>,
    gridState: androidx.compose.foundation.lazy.LazyListState,
    onAppFocused: (Int) -> Unit,
    onClick: (SteamApp, Int) -> Unit,
    onLongClick: (SteamApp, Int) -> Unit,
) {
    val focusedApp = apps.getOrNull(focusIndex) ?: apps.firstOrNull()

    Column(
        Modifier
            .fillMaxSize()
            .background(BgDark),
    ) {
        LazyRow(
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(apps, key = { _, app -> app.id }) { index, app ->
                val isSelected = index == focusIndex
                Box(
                    Modifier
                        .width(if (isSelected) 232.dp else 184.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(if (isSelected) 14.dp else 10.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Accent else CardBorder,
                            shape = RoundedCornerShape(if (isSelected) 14.dp else 10.dp),
                        ),
                ) {
                    GameCapsule(
                        app = app,
                        gogGame = gogByPseudoId[app.id],
                        epicGame = epicByPseudoId[app.id],
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        isFocusedOverride = isSelected,
                        isControllerActive = isControllerConnected,
                        customArtworkPath = customArtworkPathByAppId[app.id] ?: fallbackArtworkPathByAppId[app.id],
                        customIconPath = customIconPathByAppId[app.id],
                        customListPath = customListPathByAppId[app.id],
                        customHeroPath = customHeroPathByAppId[app.id],
                        onClick = {
                            onAppFocused(index)
                            onClick(app, index)
                        },
                        onLongClick = { onLongClick(app, index) },
                        useLibraryCapsule = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            Text(
                text = focusedApp?.name.orEmpty(),
                color = Accent,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(
                    focusedApp?.developer?.takeIf { it.isNotBlank() },
                    focusedApp?.let { "PC Game" },
                ).joinToString("  •  "),
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}

@Composable
internal fun UnifiedActivity.TileGridLibrary(
    apps: List<SteamApp>,
    gogByPseudoId: Map<Int, GOGGame>,
    epicByPseudoId: Map<Int, EpicGame>,
    iconRefreshKey: Int,
    artworkCacheRefreshKey: Int,
    focusIndex: Int,
    isControllerConnected: Boolean,
    customArtworkPathByAppId: Map<Int, String>,
    fallbackArtworkPathByAppId: Map<Int, String>,
    customIconPathByAppId: Map<Int, String>,
    customListPathByAppId: Map<Int, String>,
    customHeroPathByAppId: Map<Int, String>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onClick: (SteamApp, Int) -> Unit,
    onLongClick: (SteamApp, Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(apps, key = { _, app -> app.id }) { index, app ->
                val isSelected = index == focusIndex
                Box(
                    Modifier
                        .aspectRatio(0.92f)
                        .clip(RoundedCornerShape(3.dp))
                        .border(
                            width = if (isSelected && isControllerConnected) 3.dp else 0.dp,
                            color = Accent,
                            shape = RoundedCornerShape(3.dp),
                        ),
                ) {
                    GameCapsule(
                        app = app,
                        gogGame = gogByPseudoId[app.id],
                        epicGame = epicByPseudoId[app.id],
                        iconRefreshKey = iconRefreshKey,
                        artworkCacheRefreshKey = artworkCacheRefreshKey,
                        isFocusedOverride = isSelected,
                        isControllerActive = isControllerConnected,
                        customArtworkPath = customArtworkPathByAppId[app.id] ?: fallbackArtworkPathByAppId[app.id],
                        customIconPath = customIconPathByAppId[app.id],
                        customListPath = customListPathByAppId[app.id],
                        customHeroPath = customHeroPathByAppId[app.id],
                        onClick = { onClick(app, index) },
                        onLongClick = { onLongClick(app, index) },
                        useLibraryCapsule = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
