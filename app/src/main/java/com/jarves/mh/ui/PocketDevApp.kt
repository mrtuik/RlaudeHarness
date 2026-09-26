package com.jarves.mh.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.jarves.mh.R
import androidx.compose.material.icons.outlined.Add as AddOutlined
import androidx.compose.material.icons.outlined.Description as DescriptionOutlined
import androidx.compose.material.icons.outlined.Folder as FolderOutlined
import androidx.compose.material.icons.outlined.FolderOpen as FolderOpenOutlined
import androidx.compose.material.icons.outlined.Language as LanguageOutlined
import androidx.compose.material.icons.outlined.Settings as SettingsOutlined
import androidx.compose.material.icons.outlined.SmartToy as SmartToyOutlined
import androidx.compose.material.icons.outlined.Terminal as TerminalOutlined
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.net.Uri
import android.provider.Settings
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Toast
import com.jarves.mh.BuildConfig
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.model.ActivityItem
import com.jarves.mh.model.AgentKind
import com.jarves.mh.model.ChangeItem
import com.jarves.mh.model.ChatMessage
import com.jarves.mh.model.ChatAttachment
import com.jarves.mh.model.DevStack
import com.jarves.mh.model.DEEPSEEK_HARNESS_PROVIDERS
import com.jarves.mh.model.DSH_PROTOCOL_PROVIDERS
import com.jarves.mh.model.DiffLine
import com.jarves.mh.model.DiffLineType
import com.jarves.mh.model.Project
import com.jarves.mh.model.ProjectKind
import com.jarves.mh.model.ProjectChat
import com.jarves.mh.model.ProviderKind
import com.jarves.mh.model.ProviderProfile
import com.jarves.mh.model.inferredDshApiForUrl
import com.jarves.mh.model.providersForAgent
import com.jarves.mh.model.ToolRequest
import com.jarves.mh.model.QuestionRequest
import com.jarves.mh.model.WorkspaceEntry
import com.jarves.mh.model.projectSlug
import com.jarves.mh.model.stripEmojis
import com.jarves.mh.runtime.ProjectVersion
import com.jarves.mh.runtime.RuntimeExecutionService
import com.jarves.mh.runtime.RuntimeSetupService
import com.jarves.mh.runtime.supportsArm64Runtime
import com.jarves.mh.runtime.AntigravityAuthStatus
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.isActive
import com.jarves.mh.network.ConnectionValidation
import com.jarves.mh.network.DiscoveredModel
import com.jarves.mh.network.ModelDiscoveryResult
import com.jarves.mh.network.GitHubRepository
import com.jarves.mh.ui.theme.PocketAccent
import java.io.ByteArrayInputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import com.jarves.mh.ui.theme.AppThemeMode
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExtendedFloatingActionButton

private val RobotFilledIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "RobotFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 1024f,
        viewportHeight = 1024f,
    ).addPath(
        pathData = PathParser().parsePathString(
            "M852 64H172c-17.7 0-32 14.3-32 32v660c0 17.7 14.3 32 32 32h680c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32M300 328c0-33.1 26.9-60 60-60s60 26.9 60 60s-26.9 60-60 60s-60-26.9-60-60m372 248c0 4.4-3.6 8-8 8H360c-4.4 0-8-3.6-8-8v-60c0-4.4 3.6-8 8-8h304c4.4 0 8 3.6 8 8zm-8-188c-33.1 0-60-26.9-60-60s26.9-60 60-60s60 26.9 60 60s-26.9 60-60 60m135 476H225c-13.8 0-25 14.3-25 32v56c0 4.4 2.8 8 6.2 8h611.5c3.4 0 6.2-3.6 6.2-8v-56c.1-17.7-11.1-32-24.9-32",
        ).toNodes(),
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero,
    ).build()
}

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val poppinsFontName = GoogleFont("Poppins")

val PoppinsFontFamily = FontFamily(
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

private val ChatAltFillIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ChatAltFill",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(
            "M20.543 6.704C21 7.807 21 9.204 21 12s0 4.194-.457 5.296a6 6 0 0 1-3.247 3.247C16.194 21 14.796 21 12 21H9c-2.828 0-4.243 0-5.121-.879C3 19.243 3 17.828 3 15v-3c0-2.796 0-4.193.457-5.296a6 6 0 0 1 3.247-3.247C7.807 3 9.204 3 12 3s4.194 0 5.296.457a6 6 0 0 1 3.247 3.247M8 10a1 1 0 0 1 1-1h6a1 1 0 1 1 0 2H9a1 1 0 0 1-1-1m0 4a1 1 0 0 1 1-1h3a1 1 0 1 1 0 2H9a1 1 0 0 1-1-1",
        ).toNodes(),
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
    ).build()
}

private enum class RootScreen(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    PROJECTS("Projects", Icons.Outlined.FolderOutlined, Icons.Default.Folder),
    AGENT("Agent", RobotFilledIcon, RobotFilledIcon),
    SETTINGS("Settings", Icons.Outlined.SettingsOutlined, Icons.Default.Settings),
}
private enum class WorkspaceTab(val label: String, val icon: ImageVector) {
    CHAT("Chat", ChatAltFillIcon),
    FILES("Files", Icons.Default.Folder),
    TERMINAL("Terminal", Icons.Default.Terminal),
    CHANGES("Changes", Icons.Default.Code),
    PREVIEW("Preview", Icons.Default.Preview),
}

@Composable
fun PocketDevApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val projectsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeToast()
        }
    }
    when {
        state.startupStage == StartupStage.CHECKING -> StartupLoadingScreen(
            state = state,
            themeMode = state.themeMode,
            onToggleTheme = viewModel::toggleTheme,
        )
        !state.backgroundSetupComplete && state.startupStage == StartupStage.SETUP_REQUIRED ->
            BackgroundTaskSetupScreen(
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
                onContinue = viewModel::finishBackgroundSetup,
            )
        state.startupStage == StartupStage.SETUP_REQUIRED -> RuntimeSetupPromptScreen(
            selectedStacks = state.selectedDevStacks,
            selectedAgent = state.agentKind,
            themeMode = state.themeMode,
            onToggleTheme = viewModel::toggleTheme,
            onToggleStack = viewModel::toggleDevStack,
            onSelectAgent = viewModel::selectAgent,
            onDownload = viewModel::startRuntimeSetup,
        )
        state.startupStage == StartupStage.INSTALLING && state.showDetailedSetupProgress ->
            RuntimeInstallationScreen(
                state = state,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.INSTALLING || state.startupStage == StartupStage.INITIALIZING ->
            StartupLoadingScreen(
                state = state,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.ERROR -> StartupErrorScreen(
            message = state.startupError,
            isOffline = state.startupErrorIsOffline,
            logs = state.startupLogs,
            themeMode = state.themeMode,
            onToggleTheme = viewModel::toggleTheme,
            onRetry = viewModel::retryStartup,
        )
        state.startupStage == StartupStage.MODEL_SETUP && state.agentKind == AgentKind.ANTIGRAVITY ->
            AntigravityOnboardingScreen(
                state = state,
                onStartLogin = viewModel::startAntigravityLogin,
                onSubmitCode = viewModel::submitAntigravityCode,
                onContinue = viewModel::finishAntigravityOnboarding,
                onSelectAgent = viewModel::chooseOnboardingAgent,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.MODEL_SETUP -> ProviderSetupScreen(
            initial = state.provider,
            onboarding = true,
            agentKind = state.agentKind,
            initialStep = 1,
            onSave = viewModel::finishOnboarding,
            onDiscover = viewModel::discoverModels,
            onValidate = viewModel::validateProvider,
            onSelectAgent = viewModel::chooseOnboardingAgent,
            onToggleTheme = viewModel::toggleTheme,
            themeMode = state.themeMode,
        )
        state.startupStage == StartupStage.READY && !state.backgroundSetupComplete ->
            BackgroundTaskSetupScreen(
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
                onContinue = viewModel::finishBackgroundSetup,
            )
        state.readOnlyProject != null -> ReadOnlyProjectScreen(
            state = state,
            onBack = viewModel::closeReadOnlyProject,
            onSwitchChat = viewModel::switchReadOnlyChat,
            onContinueHere = viewModel::activateReadOnlyProject,
        )
        state.activeProject != null && state.workspaceVisible -> WorkspaceScreen(
            state = state,
            onBack = viewModel::closeProject,
            onSend = viewModel::sendPrompt,
            onStop = viewModel::stopTask,
            onApproval = viewModel::answerApproval,
            onAnswerQuestion = viewModel::answerQuestion,
            onRefreshFiles = viewModel::refreshProjectFiles,
            onOpenFile = viewModel::openFile,
            onCloseFile = viewModel::closeFile,
            onUndoChanges = viewModel::undoLastChanges,
            onKeepChanges = viewModel::keepLastChanges,
            onUndoFileChange = viewModel::undoFileChange,
            onKeepFileChange = viewModel::keepFileChange,
            onCreateChat = viewModel::createChat,
            onSwitchChat = viewModel::switchChat,
            onTerminalRun = viewModel::requestProjectTerminalCommand,
            onTerminalInput = viewModel::sendProjectTerminalInput,
            onTerminalInterrupt = viewModel::interruptProjectTerminalCommand,
            onTerminalPrepare = viewModel::prepareProjectTerminalCommand,
            onTerminalDraftConsumed = viewModel::consumeProjectTerminalDraft,
            onTerminalOpened = viewModel::openProjectTerminal,
            onTerminalStop = viewModel::stopProjectTerminalCommand,
            onTerminalClear = viewModel::clearProjectTerminal,
            onTerminalConfirm = viewModel::confirmProjectTerminalCommand,
            onTerminalCancel = viewModel::cancelProjectTerminalCommand,
            onUseSuggestedProjectRoot = viewModel::useSuggestedProjectRoot,
            onExportProject = viewModel::exportActiveProject,
            onExportFile = viewModel::exportWorkspaceFile,
            onExportChangedFilesZip = viewModel::exportChangedFilesZip,
            onAddAttachments = viewModel::addChatAttachments,
            onRemoveAttachment = viewModel::removePendingAttachment,
            onRenameAttachment = viewModel::renameAttachment,
            onOpenAttachment = viewModel::openChatAttachment,
            onEditMessage = viewModel::restoreMessageForEdit,
            onAddTextAttachment = viewModel::addPastedTextAttachment,
            onBuildAndRunAndroid = viewModel::buildAndRunAndroidApp,
            onDraftChange = viewModel::updateDraftPrompt,
            onGetVersionFiles = viewModel::getVersionFiles,
            onExportVersionZip = viewModel::exportVersionZip,
        )
        else -> RootScreenHost(state, viewModel, projectsListState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AntigravityOnboardingScreen(
    state: AppUiState,
    onStartLogin: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onContinue: () -> Unit,
    onSelectAgent: (AgentKind) -> Unit,
    onToggleTheme: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var code by rememberSaveable { mutableStateOf("") }
    var showAgentPicker by rememberSaveable { mutableStateOf(false) }
    if (showAgentPicker) {
        AgentSwitchSheet(
            selected = AgentKind.ANTIGRAVITY,
            onSelect = { agent ->
                showAgentPicker = false
                onSelectAgent(agent)
            },
            onDismiss = { showAgentPicker = false },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set up Antigravity") },
                actions = { IconButton(onClick = onToggleTheme) { Icon(Icons.Default.DarkMode, "Toggle theme") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Connect your Google account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Rlaude Harness runs Google's official agy CLI inside its private Linux environment. Google handles authentication and agy owns the saved session.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (state.antigravityAuth.status) {
                AntigravityAuthStatus.SIGNED_OUT, AntigravityAuthStatus.ERROR -> {
                    state.antigravityAuth.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = onStartLogin, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Sign in with Google")
                    }
                }
                AntigravityAuthStatus.STARTING -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Starting the official Antigravity login…")
                }
                AntigravityAuthStatus.COMPLETING -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Completing Google sign-in…")
                }
                AntigravityAuthStatus.AWAITING_CODE -> {
                    Text("Google sign-in opened in your browser. Copy the one-time code shown after approval.")
                    state.antigravityAuth.authorizationUrl?.let { url ->
                        OutlinedButton(
                            onClick = { clipboard.setText(AnnotatedString(url)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(painterResource(R.drawable.ic_custom_copy), null, Modifier.size(18.dp), tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy sign-in URL")
                        }
                    }
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Authorization code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onSubmitCode(code); code = "" },
                        enabled = code.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Complete sign-in") }
                }
                AntigravityAuthStatus.SIGNED_IN -> {
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)) {
                        Text(
                            state.antigravityAuth.accountEmail?.let { "Connected as $it" } ?: "Google account connected",
                            Modifier.fillMaxWidth().padding(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Continue")
                    }
                }
            }
            TextButton(
                onClick = { showAgentPicker = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Use another coding agent", fontSize = 12.sp)
            }
            Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(14.dp)) {
                Text(
                    "Automatic tool approval is enabled for Antigravity. It can edit project files and run commands without confirmation. Changes remain reviewable in Rlaude Harness.",
                    Modifier.fillMaxWidth().padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundTaskSetupScreen(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val powerManager = context.getSystemService(PowerManager::class.java)
    fun notificationsAllowed(): Boolean {
        val runtimeGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        return runtimeGranted && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    fun batteryUnrestricted(): Boolean = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var notificationGranted by remember { mutableStateOf(notificationsAllowed()) }
    var batteryGranted by remember { mutableStateOf(batteryUnrestricted()) }
    var notificationDenied by rememberSaveable { mutableStateOf(false) }
    var taskProtectionConfirmed by rememberSaveable { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationGranted = notificationsAllowed()
        notificationDenied = !granted
        if (notificationGranted) currentStep = 1
    }
    val notificationSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        notificationGranted = notificationsAllowed()
        if (notificationGranted) currentStep = 1
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        batteryGranted = batteryUnrestricted()
        if (batteryGranted) currentStep = 2
    }

    LaunchedEffect(Unit) {
        notificationGranted = notificationsAllowed()
        batteryGranted = batteryUnrestricted()
    }

    val currentIcon = when (currentStep) {
        0 -> Icons.Default.Notifications
        1 -> Icons.Default.BatterySaver
        else -> Icons.Default.Shield
    }
    val currentTitle = when (currentStep) {
        0 -> "Task notifications"
        1 -> "Background reliability"
        else -> "Task protection"
    }
    val currentDescription = when (currentStep) {
        0 -> "See live progress and receive an alert when Claude finishes or needs your attention."
        1 -> "Allow Rlaude Harness to continue a task when you lock the phone or switch to another app."
        else -> "Keep the CPU awake only while a visible coding task is running, then release it automatically."
    }
    val currentPrivacyNote = when (currentStep) {
        0 -> "Only task progress, completion, and error notifications are sent."
        1 -> "You remain in control and can stop every task from its notification."
        else -> "The screen stays off. Protection is capped at 90 minutes and stops with the task."
    }
    val currentGranted = when (currentStep) {
        0 -> notificationGranted
        1 -> batteryGranted
        else -> taskProtectionConfirmed
    }

    Scaffold(
        containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(Modifier.width(9.dp))
                        Text("Rlaude Harness", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Prepare for reliable setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Setup time depends on the toolchains you choose next. You may leave Rlaude Harness in the background while it works.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(18.dp))
            StepDots(currentStep)
            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
            ) {
                Column {
                    PermissionSummaryRow(Icons.Default.Notifications, "Notifications", notificationGranted, currentStep == 0)
                    HorizontalDivider(modifier = Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PermissionSummaryRow(Icons.Default.BatterySaver, "Background", batteryGranted, currentStep == 1)
                    HorizontalDivider(modifier = Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PermissionSummaryRow(Icons.Default.Shield, "Task protection", taskProtectionConfirmed, currentStep == 2)
                }
            }

            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).background(if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.White, RoundedCornerShape(10.dp)).border(1.dp, if (themeMode == AppThemeMode.DARK) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(currentIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("STEP ${currentStep + 1} OF 3", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            Text(currentTitle, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (currentGranted) Icon(Icons.Default.Check, "Granted", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(currentDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(currentPrivacyNote, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            when (currentStep) {
                                0 -> when {
                                    notificationGranted -> currentStep = 1
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationDenied -> {
                                        // Targets below API 33 can have notification prompts tied to
                                        // channel creation. Create channels only after this explicit tap.
                                        RuntimeExecutionService.ensureNotificationChannels(context)
                                        RuntimeSetupService.ensureNotificationChannel(context)
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    else -> notificationSettingsLauncher.launch(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
                                            Settings.EXTRA_APP_PACKAGE,
                                            context.packageName,
                                        ),
                                    )
                                }
                                1 -> if (batteryGranted) {
                                    currentStep = 2
                                } else {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    runCatching { batteryLauncher.launch(intent) }
                                        .onFailure {
                                            batteryLauncher.launch(
                                                Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.parse("package:${context.packageName}"),
                                                ),
                                            )
                                        }
                                }
                                else -> {
                                    taskProtectionConfirmed = true
                                    onContinue()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            when (currentStep) {
                                0 -> if (notificationGranted) "Next" else if (notificationDenied) "Open notification settings" else "Allow notifications"
                                1 -> if (batteryGranted) "Next" else "Open battery settings"
                                else -> "Enable and finish"
                            },
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                    if (currentStep < 2 && !currentGranted) {
                        TextButton(
                            onClick = { currentStep += 1 },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (currentStep == 0) "Continue without notifications" else "Continue without battery exemption")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "You can change these settings later. Android may still stop exceptionally heavy work when the device is low on memory.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PermissionSummaryRow(
    icon: ImageVector,
    title: String,
    complete: Boolean,
    active: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            null,
            tint = if (active) MaterialTheme.colorScheme.primary else if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
        when {
            complete -> Icon(Icons.Default.Check, "Complete", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            active -> Text("Required", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            else -> Text("Next", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

private data class DevStackVisuals(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color,
    val tag: String,
)

private fun getDevStackVisuals(stack: DevStack): DevStackVisuals = when (stack) {
    DevStack.WEB -> DevStackVisuals(
        icon = Icons.Default.Language,
        accentColor = Color(0xFF9C9C9C),
        tag = "HTML · CSS · JS · TS",
    )
    DevStack.PYTHON -> DevStackVisuals(
        icon = Icons.Default.Terminal,
        accentColor = Color(0xFFBFBFBF),
        tag = "python3 + pip + venv",
    )
    DevStack.ANDROID -> DevStackVisuals(
        icon = Icons.Default.Android,
        accentColor = Color(0xFFA7A7A7),
        tag = "OpenJDK build tools",
    )
    DevStack.CPP -> DevStackVisuals(
        icon = Icons.Default.Memory,
        accentColor = Color(0xFFA0A0A0),
        tag = "gcc + g++ + cmake",
    )
    DevStack.PHP -> DevStackVisuals(
        icon = Icons.Default.Dns,
        accentColor = Color(0xFF959595),
        tag = "php-cli + Composer",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuntimeSetupPromptScreen(
    selectedStacks: Set<DevStack>,
    selectedAgent: AgentKind = AgentKind.CLAUDE_CODE,
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
    onToggleStack: (DevStack) -> Unit,
    onSelectAgent: (AgentKind) -> Unit = {},
    onDownload: () -> Unit,
) {
    val context = LocalContext.current
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val memoryInfo = remember { ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo) }
    val totalRamGb = memoryInfo.totalMem.toDouble() / 1_073_741_824.0
    val totalRamLabel = String.format(java.util.Locale.US, "%.1f", totalRamGb)
    val arm64 = supportsArm64Runtime(Build.SUPPORTED_ABIS, System.getProperty("os.arch"))
    // Android reports usable physical memory after hardware/GPU reservations.
    // RAM is therefore informational; it must not reject nominal 4 GB phones.
    val compatible = arm64

    var currentStep by remember { mutableIntStateOf(0) }
    val setupScrollState = rememberScrollState()

    LaunchedEffect(currentStep) {
        setupScrollState.scrollTo(0)
    }

    if (currentStep > 0) {
        BackHandler { currentStep = 0 }
    }

    Scaffold(
        containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(Modifier.width(9.dp))
                        Text("Rlaude Harness", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { currentStep = 0 }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp)
                .verticalScroll(setupScrollState),
        ) {
            Spacer(Modifier.height(8.dp))

            if (currentStep == 0) {
                // Step 0: Device Compatibility & Verification
                Text(
                    text = "DEVICE CHECK",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ready to build on this phone",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your phone meets the requirements. Choose your coding tools next and Rlaude Harness will handle the setup.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                )

                Spacer(Modifier.height(20.dp))

                // Hardware & Compatibility Specs Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                    border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Speed,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "System compatibility",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        if (compatible) "Your device is ready" else "This device is unsupported",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (compatible) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, if (compatible) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                            ) {
                                Text(
                                    text = if (compatible) "Ready" else "Unsupported",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (compatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)

                        SpecRow(
                            icon = Icons.Default.Memory,
                            label = "Memory (RAM)",
                            value = "$totalRamLabel GB usable",
                            statusOk = true,
                        )

                        SpecRow(
                            icon = Icons.Default.Code,
                            label = "Processor",
                            value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
                            statusOk = arm64,
                        )

                        SpecRow(
                            icon = Icons.Default.Storage,
                            label = "Required download",
                            value = "149–774 MB",
                            statusOk = true,
                        )
                        Text(
                            "Based on the tools you select",
                            modifier = Modifier.padding(start = 26.dp),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { currentStep = 1 },
                    enabled = compatible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (compatible) "Continue to tool setup" else "Device not supported",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "You can change tools later",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "TOOLCHAIN SETUP",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Choose your tools",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Start lightweight. You can install more toolchains later from Settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )

                Spacer(Modifier.height(18.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                    border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surfaceVariant else Color.White, RoundedCornerShape(10.dp)).border(1.dp, if (themeMode == AppThemeMode.DARK) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Terminal, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (BuildConfig.OFFLINE_RUNTIME_BUNDLES) "Core runtime · 68.8 MB" else "Core runtime · 68.8 MB download",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                            )
                            Text("Ubuntu  ·  Node.js  ·  npm  ·  Git", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.Check, "Included", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("CODING AGENT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp)
                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                    border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                ) {
                    Column {
                        AgentKind.entries.forEachIndexed { index, agent ->
                            AgentChoiceRow(
                                agent = agent,
                                selected = selectedAgent == agent,
                                onClick = { onSelectAgent(agent) },
                            )
                            if (index != AgentKind.entries.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 62.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
                Text(
                    "Only the selected optional agent is downloaded. You can install or switch agents later from Settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 2.dp, end = 2.dp),
                )

                Spacer(Modifier.height(18.dp))
                Text("OPTIONAL TOOLCHAINS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp)
                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                    border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                ) {
                    Column {
                        DevStack.entries.forEachIndexed { index, stack ->
                            DevStackChoiceRow(
                                stack = stack,
                                selected = stack == DevStack.WEB || stack in selectedStacks,
                                locked = stack == DevStack.WEB,
                                onClick = { onToggleStack(stack) },
                            )
                            if (index != DevStack.entries.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(start = 62.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        toolchainDownloadSummary(selectedStacks, selectedAgent),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onDownload,
                    enabled = compatible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (compatible) "Install Rlaude Harness" else "Device not supported",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        if (compatible) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

private const val CORE_RUNTIME_DOWNLOAD_MB = 69
private const val CLAUDE_RUNTIME_DOWNLOAD_MB = 72
private const val DSH_RUNTIME_DOWNLOAD_MB = 27
private const val AGY_RUNTIME_DOWNLOAD_MB = 40
private const val PYTHON_RUNTIME_DOWNLOAD_MB = 55
private const val ANDROID_RUNTIME_DOWNLOAD_MB = 570

private fun setupTimeEstimate(selected: Set<DevStack>): String {
    var minimumMinutes = 3
    var maximumMinutes = 5
    if (DevStack.PYTHON in selected) {
        minimumMinutes += 1
        maximumMinutes += 2
    }
    if (DevStack.ANDROID in selected) {
        minimumMinutes += 7
        maximumMinutes += 10
    }
    if (DevStack.CPP in selected) {
        minimumMinutes += 3
        maximumMinutes += 5
    }
    if (DevStack.PHP in selected) {
        minimumMinutes += 2
        maximumMinutes += 4
    }
    return "$minimumMinutes–$maximumMinutes minutes"
}

private fun stackDownloadLabel(stack: DevStack): String = when {
    stack == DevStack.WEB -> " · included"
    BuildConfig.OFFLINE_RUNTIME_BUNDLES && stack in setOf(DevStack.PYTHON, DevStack.ANDROID) -> " · included"
    !BuildConfig.OFFLINE_RUNTIME_BUNDLES && stack == DevStack.PYTHON -> " · 55 MB"
    !BuildConfig.OFFLINE_RUNTIME_BUNDLES && stack == DevStack.ANDROID -> " · 570 MB"
    else -> ""
}

private fun toolchainDownloadSummary(selected: Set<DevStack>, agent: AgentKind): String {
    if (BuildConfig.OFFLINE_RUNTIME_BUNDLES) return "All selected bundles are included in this offline app"
    val total = CORE_RUNTIME_DOWNLOAD_MB +
        when (agent) {
            AgentKind.CLAUDE_CODE -> CLAUDE_RUNTIME_DOWNLOAD_MB
            AgentKind.DEEPSEEK_HARNESS -> DSH_RUNTIME_DOWNLOAD_MB
            AgentKind.ANTIGRAVITY -> AGY_RUNTIME_DOWNLOAD_MB
        } +
        (if (DevStack.PYTHON in selected) PYTHON_RUNTIME_DOWNLOAD_MB else 0) +
        (if (DevStack.ANDROID in selected) ANDROID_RUNTIME_DOWNLOAD_MB else 0)
    val laterPackages = selected.intersect(setOf(DevStack.CPP, DevStack.PHP))
    return buildString {
        append("Download: ")
        append(total)
        append(" MB")
        if (laterPackages.isNotEmpty()) append(" · C/PHP packages download later")
        if (total >= 500) append(" · Wi-Fi recommended")
    }
}

@Composable
private fun DevStackChoiceRow(
    stack: DevStack,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
) {
    val visuals = getDevStackVisuals(stack)
    val conciseDescription = when (stack) {
        DevStack.WEB -> "Included with the Core runtime"
        DevStack.PYTHON -> "Scripts, automation and backends"
        DevStack.ANDROID -> "Java and Kotlin build tools"
        DevStack.CPP -> "Native apps and command-line tools"
        DevStack.PHP -> "PHP sites and Laravel projects"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(enabled = !locked, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(visuals.icon, null, tint = visuals.accentColor, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stack.label + stackDownloadLabel(stack),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(1.dp))
            Text(conciseDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(21.dp)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                .border(
                    1.5.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AgentChoiceRow(
    agent: AgentKind,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = when (agent) {
        AgentKind.CLAUDE_CODE -> Color(0xFF919191)
        AgentKind.DEEPSEEK_HARNESS -> Color(0xFF737373)
        AgentKind.ANTIGRAVITY -> Color(0xFF7E7E7E)
    }
    val mark = when (agent) {
        AgentKind.CLAUDE_CODE -> "CC"
        AgentKind.DEEPSEEK_HARNESS -> "DS"
        AgentKind.ANTIGRAVITY -> "AG"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(mark, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    agent.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                if (agent == AgentKind.DEEPSEEK_HARNESS) {
                    Spacer(Modifier.width(7.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            "Recommended",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
            Spacer(Modifier.height(1.dp))
            Text(agent.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(1.dp))
            Text(agent.downloadNote, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(21.dp)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentSwitchSheet(
    selected: AgentKind,
    onSelect: (AgentKind) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                "Choose coding agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Switch if the current service is unavailable. Your existing login and credentials stay saved.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column {
                    AgentKind.entries.forEachIndexed { index, agent ->
                        AgentChoiceRow(
                            agent = agent,
                            selected = agent == selected,
                            onClick = { onSelect(agent) },
                        )
                        if (index != AgentKind.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 62.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SpecRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    statusOk: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (statusOk) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RuntimeInstallationScreen(
    state: AppUiState,
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(Modifier.width(9.dp))
                        Text("Set up Rlaude Harness", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.background else Color.White),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            StepDots(0)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "STEP 1 OF 3",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.1.sp,
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) Color.Transparent else Color(0xFFE2E8F0)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Local setup", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Build your workspace",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(42.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    state.startupMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.surface else Color.White,
                border = BorderStroke(1.dp, if (themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Installation progress", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("${(state.startupProgress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.startupProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().height(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Estimated ${setupTimeEstimate(state.selectedDevStacks)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                        )
                        Spacer(Modifier.weight(1f))
                        state.startupBytes?.let { (downloaded, total) ->
                            Text(
                                "${formatMegabytes(downloaded)} / ${formatMegabytes(total)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            SetupLogPanel(
                logs = state.startupLogs.ifEmpty { listOf("$ ${state.startupMessage}") },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "You can leave Rlaude Harness in the background and follow setup from the notification.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StartupLoadingScreen(
    state: AppUiState,
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
) {
    val view = LocalView.current
    // Runtime download + install can take 10+ minutes; keep the screen on while this
    // screen is visible. Released automatically when setup finishes or leaves.
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    val messages = remember {
        listOf(
            "Setting up your workspace",
            "Preparing your coding tools",
            "Almost ready",
        )
    }
    var messageIndex by remember(state.startupStage) { mutableIntStateOf(0) }
    LaunchedEffect(messages) {
        while (true) {
            delay(3_000)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }
    val logoTransition = rememberInfiniteTransition(label = "startup logo")
    val logoPulse by logoTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startup logo pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(
                Modifier.graphicsLayer {
                    scaleX = logoPulse
                    scaleY = logoPulse
                    alpha = 0.82f + ((logoPulse - 0.96f) / 0.08f) * 0.18f
                },
            )
            Spacer(Modifier.height(22.dp))
            AnimatedContent(
                targetState = messages[messageIndex],
                label = "startup message",
            ) { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
            AnimatedThinkingDots(dotColor = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun WorkspaceLaunchExperience(state: AppUiState) {
    val pulseTransition = rememberInfiniteTransition(label = "workspace launch")
    val glow by pulseTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1_400 },
            repeatMode = RepeatMode.Reverse,
        ),
        label = "workspace glow",
    )
    val agentVersion = state.installedAgentVersions[state.agentKind]
    val agentReady = state.startupMessage.contains("ready", ignoreCase = true) || state.startupProgress >= 0.75f

    Text(
        "PRIVATE MOBILE WORKSPACE",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.35.sp,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        "Getting everything ready",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(7.dp))
    Text(
        "Restoring your projects and reconnecting your local coding agent.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    )
    Spacer(Modifier.height(22.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        tonalElevation = 3.dp,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(58.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = glow), RoundedCornerShape(18.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(27.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        state.agentKind.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        agentVersion?.let { "Verified CLI · v$it" } ?: "Connecting local agent",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Text(
                        if (agentReady) "READY" else "STARTING",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            LaunchStatusRow(Icons.Default.Shield, "Private Linux environment", "Verified", complete = true)
            Spacer(Modifier.height(13.dp))
            LaunchStatusRow(Icons.Default.Terminal, state.agentKind.title, if (agentReady) "Ready" else "Connecting", complete = agentReady)
            Spacer(Modifier.height(13.dp))
            LaunchStatusRow(Icons.Default.Folder, "Project workspace", "Restoring", complete = false)
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(17.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(11.dp))
        Text(
            state.startupMessage,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Spacer(Modifier.height(12.dp))
    Text(
        "Runs locally on this device · Your project files stay private",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontSize = 10.5.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LaunchStatusRow(icon: ImageVector, label: String, status: String, complete: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(28.dp)
                .background(
                    if (complete) MaterialTheme.colorScheme.primary.copy(alpha = 0.11f) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(9.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (complete) Icons.Default.Check else icon,
                contentDescription = null,
                tint = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(
            status,
            color = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetupLogPanel(logs: List<String>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var followLatest by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(logs.size, logs.lastOrNull()) {
        if (expanded && followLatest) {
            delay(20)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && expanded) {
            followLatest = scrollState.maxValue - scrollState.value < 32
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (expanded) "Live setup terminal" else logs.lastOrNull().orEmpty(),
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse setup details" else "Expand setup details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 170.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    logs.forEach { line ->
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "▌",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (!followLatest) {
                    TextButton(
                        onClick = {
                            followLatest = true
                            scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Jump to latest") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartupErrorScreen(
    message: String?,
    isOffline: Boolean,
    logs: List<String>,
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(Modifier.width(9.dp))
                        Text("Rlaude Harness", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.Warning, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(20.dp))
            Text(
                if (isOffline) "You're offline" else "Rlaude Harness couldn't finish starting",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                message ?: "Please try again.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (logs.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(logs.joinToString("\n")))
                        Toast.makeText(context, "Setup log copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(painterResource(R.drawable.ic_custom_copy), null, modifier = Modifier.size(17.dp), tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy setup logs")
                }
            }
            Spacer(Modifier.height(24.dp))
            if (isOffline) {
                Button(
                    onClick = {
                        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Settings.Panel.ACTION_INTERNET_CONNECTIVITY
                        } else {
                            Settings.ACTION_WIRELESS_SETTINGS
                        }
                        context.startActivity(Intent(action))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open internet settings")
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text("Try again")
                }
            } else {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Try again") }
            }
        }
    }
}

private fun formatMegabytes(bytes: Long): String = "%.1f MB".format(bytes / 1_048_576.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootScreenHost(
    state: AppUiState,
    viewModel: MainViewModel,
    projectsListState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
) {
    var screen by rememberSaveable { mutableStateOf(RootScreen.PROJECTS) }
    var showQuickTerminal by rememberSaveable { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val isTerminalRunning by viewModel.isTerminalRunning.collectAsStateWithLifecycle()
    val terminalLiveOutput by viewModel.terminalLiveOutput.collectAsStateWithLifecycle()
    val terminalCurrentCommand by viewModel.terminalCurrentCommand.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Settings lives in the top header (see ProjectsScreen's TopAppBar) instead of the
            // bottom nav. The bottom nav is a single floating pill with three items: Projects
            // and Agent (icon-only), and Terminal (icon + label) merged in as the third item
            // instead of a separate overlapping FAB.
            if (!keyboardVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            RootScreen.entries.filter { it != RootScreen.SETTINGS }.forEach { tab ->
                                val selected = screen == tab
                                val tabIndicatorColor by animateColorAsState(
                                    targetValue = if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    } else {
                                        Color.Transparent
                                    },
                                    animationSpec = tween(durationMillis = 220),
                                    label = "tabIndicator",
                                )
                                val tabTint by animateColorAsState(
                                    targetValue = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    animationSpec = tween(durationMillis = 220),
                                    label = "tabTint",
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tabIndicatorColor)
                                        .clickable(onClick = { screen = tab }),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (tab == RootScreen.PROJECTS) {
                                        Icon(
                                            painterResource(R.drawable.ic_custom_folder),
                                            contentDescription = tab.label,
                                            modifier = Modifier.size(24.dp),
                                            tint = tabTint,
                                        )
                                    } else {
                                        Icon(
                                            if (selected) tab.selectedIcon else tab.icon,
                                            contentDescription = tab.label,
                                            tint = tabTint,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(2.dp))
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = Color.Black,
                            )
                            Spacer(Modifier.width(2.dp))
                            val terminalSelected = showQuickTerminal
                            val terminalTint by animateColorAsState(
                                targetValue = if (terminalSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                animationSpec = tween(durationMillis = 220),
                                label = "terminalTint",
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .clickable(onClick = { showQuickTerminal = true })
                                    .padding(horizontal = 12.dp),
                            ) {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = "Terminal",
                                    modifier = Modifier.size(20.dp),
                                    tint = terminalTint,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Terminal",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = terminalTint,
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                RootScreen.PROJECTS -> ProjectsScreen(
                    state = state,
                    listState = projectsListState,
                    onOpen = viewModel::openProject,
                    onCreate = viewModel::createProject,
                    onCreateQuickProject = viewModel::createQuickProject,
                    onImportZip = viewModel::importZipProject,
                    onCloneGit = viewModel::clonePublicGitRepository,
                    onStartGitHubLogin = viewModel::startGitHubLogin,
                    onGenerateNewGitHubCode = viewModel::generateNewGitHubCode,
                    onRefreshGitHub = viewModel::refreshGitHubRepositories,
                    onDisconnectGitHub = viewModel::disconnectGitHub,
                    onCloneGitHub = viewModel::cloneGitHubRepository,
                    onRenameProject = viewModel::renameProject,
                    onDeleteProject = viewModel::deleteProject,
                    onSettings = { screen = RootScreen.SETTINGS },
                    onPing = viewModel::pingApi,
                    onToggleTheme = viewModel::toggleTheme,
                    onInstallUpdate = viewModel::installAppUpdate,
                )
                RootScreen.AGENT -> AgentScreen(
                    state = state,
                    onSaveProvider = { profile, key ->
                        viewModel.updateProvider(profile, key)
                    },
                    onDiscoverModels = viewModel::discoverModels,
                    onValidateProvider = viewModel::validateProvider,
                    onPing = viewModel::pingApi,
                    getSavedApiKey = viewModel::getSavedApiKey,
                    getSavedApiKeys = viewModel::getSavedApiKeys,
                    onAddApiKey = viewModel::addApiKey,
                    onActivateApiKey = viewModel::activateApiKey,
                    onRemoveApiKey = viewModel::removeApiKey,
                    onSelectAgent = viewModel::selectAgent,
                    onInstallAgent = viewModel::installAgent,
                    onCheckAgentUpdates = viewModel::checkAgentUpdates,
                    onUpdateAgent = viewModel::updateAgent,
                    onStartAntigravityLogin = viewModel::startAntigravityLogin,
                    onSubmitAntigravityCode = viewModel::submitAntigravityCode,
                    onLogoutAntigravity = viewModel::logoutAntigravity,
                    onRefreshAntigravityModels = viewModel::refreshAntigravityModels,
                    onSetAntigravityModel = viewModel::setAntigravityModel,
                    onSetAntigravityEffort = viewModel::setAntigravityEffort,
                )
                RootScreen.SETTINGS -> SettingsScreen(
                    state = state,
                    onSaveProvider = { profile, key ->
                        viewModel.updateProvider(profile, key)
                    },
                    onDiscoverModels = viewModel::discoverModels,
                    onValidateProvider = viewModel::validateProvider,
                    onSetThemeMode = viewModel::setThemeMode,
                    onPing = viewModel::pingApi,
                    onClearTerminal = viewModel::clearTerminal,
                    getSavedApiKey = viewModel::getSavedApiKey,
                    getSavedApiKeys = viewModel::getSavedApiKeys,
                    onAddApiKey = viewModel::addApiKey,
                    onActivateApiKey = viewModel::activateApiKey,
                    onRemoveApiKey = viewModel::removeApiKey,
                    onInstallDevStack = viewModel::installDevStack,
                    onRemoveDevStack = viewModel::removeDevStack,
                    onInstallAgent = viewModel::installAgent,
                    onCheckAgentUpdates = viewModel::checkAgentUpdates,
                    onUpdateAgent = viewModel::updateAgent,
                    onStartAntigravityLogin = viewModel::startAntigravityLogin,
                    onSubmitAntigravityCode = viewModel::submitAntigravityCode,
                    onLogoutAntigravity = viewModel::logoutAntigravity,
                    onRefreshAntigravityModels = viewModel::refreshAntigravityModels,
                    onSetAntigravityModel = viewModel::setAntigravityModel,
                    onSetAntigravityEffort = viewModel::setAntigravityEffort,
                    initialDebugUpdateManifestUrl = viewModel.debugUpdateManifestUrl(),
                    onSetDebugUpdateManifestUrl = viewModel::setDebugUpdateManifestUrl,
                    onClearDebugUpdateManifestUrl = viewModel::clearDebugUpdateManifestUrl,
                    onSaveGithubLessonsPat = viewModel::saveGithubLessonsPat,
                    onClearGithubLessonsPat = viewModel::clearGithubLessonsPat,
                    onRefreshLessons = viewModel::refreshLessons,
                    onSetGithubLessonsRepo = viewModel::setGithubLessonsRepo,
                )
            }
        }
    }
    if (showQuickTerminal) {
        QuickTerminalSheet(
            onDismiss = { showQuickTerminal = false },
        ) {
            TerminalScreen(
                lines = terminalLines,
                isRunning = isTerminalRunning,
                onRun = viewModel::runTerminalCommand,
                onInput = viewModel::sendTerminalInput,
                onInterrupt = viewModel::interruptTerminalCommand,
                onClear = viewModel::clearTerminal,
                onToggleTheme = viewModel::toggleTheme,
                themeMode = state.themeMode,
                liveOutput = terminalLiveOutput,
                currentCommand = terminalCurrentCommand,
                showThemeAction = false,
                showQuickCommands = true,
                compactHeader = true,
            )
        }
    }
}

@Composable
private fun QuickTerminalSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    var sheetFraction by rememberSaveable { mutableFloatStateOf(0.45f) }
    BackHandler(onBack = onDismiss)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
        LaunchedEffect(keyboardVisible) {
            if (keyboardVisible) sheetFraction = 0.85f
        }
        val dragState = rememberDraggableState { dragAmount ->
            sheetFraction = (sheetFraction - dragAmount / maxHeightPx)
                .coerceIn(0.40f, 0.85f)
        }
        Box(Modifier.fillMaxSize()) {
            // Dimmed backdrop — tap to dismiss.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
            // This container consumes the keyboard inset outside the sheet rather
            // than turning it into empty padding inside the terminal.
            Box(Modifier.fillMaxSize().imePadding()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(sheetFraction)
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            startDragImmediately = false,
                        ),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 28.dp,
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .background(Color(0xFFE2E8F0), CircleShape),
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color(0xFFE2E8F0),
                        )
                        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSetupScreen(
    initial: ProviderProfile,
    onboarding: Boolean,
    agentKind: AgentKind = AgentKind.CLAUDE_CODE,
    initialStep: Int = if (onboarding) 0 else 1,
    onBack: (() -> Unit)? = null,
    onSave: (ProviderProfile, String) -> Unit,
    onDiscover: suspend (ProviderProfile, String) -> ModelDiscoveryResult,
    onValidate: suspend (ProviderProfile, String, List<DiscoveredModel>) -> ConnectionValidation,
    onSelectAgent: (AgentKind) -> Unit,
    onToggleTheme: (() -> Unit)? = null,
    themeMode: AppThemeMode = AppThemeMode.DARK,
) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(initialStep) }
    var selected by rememberSaveable { mutableStateOf(initial.kind) }
    var baseUrl by rememberSaveable { mutableStateOf(initial.baseUrl.ifBlank { initial.kind.defaultBaseUrl }) }
    var model by rememberSaveable { mutableStateOf(initial.model.ifBlank { initial.kind.defaultModel }) }
    var dshApi by rememberSaveable { mutableStateOf(initial.dshApi.ifBlank { "anthropic-messages" }) }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var showAgentPicker by rememberSaveable { mutableStateOf(false) }

    if (showAgentPicker) {
        AgentSwitchSheet(
            selected = agentKind,
            onSelect = { agent ->
                showAgentPicker = false
                onSelectAgent(agent)
            },
            onDismiss = { showAgentPicker = false },
        )
    }

    val handleBack: (() -> Unit)? = when {
        step > 1 -> { { step = 1 } }
        !onboarding && onBack != null -> onBack
        else -> null
    }

    if (handleBack != null) {
        BackHandler(onBack = handleBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (onboarding) "Set up Rlaude Harness" else "AI Provider & Settings") },
                navigationIcon = {
                    if (handleBack != null) {
                        IconButton(onClick = handleBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (onToggleTheme != null) {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            if (onboarding) StepDots(step)
            Spacer(Modifier.height(16.dp))
            when (step) {
                0 -> DeviceCheckStep(context, onContinue = { step = 1 })
                1 -> ProviderChoiceStep(
                    selected = selected,
                    agentKind = agentKind,
                    onSelected = {
                        if (selected != it) {
                            selected = it
                            baseUrl = it.defaultBaseUrl
                            model = it.defaultModel
                            apiKey = ""
                        }
                    },
                    onContinue = { step = 2 },
                    onChangeAgent = { showAgentPicker = true },
                )
                else -> ProviderCredentialsStep(
                    provider = selected,
                    agentKind = agentKind,
                    baseUrl = baseUrl,
                    model = model,
                    dshApi = dshApi,
                    apiKey = apiKey,
                    onBaseUrl = {
                        baseUrl = it
                        if (agentKind == AgentKind.DEEPSEEK_HARNESS && selected == ProviderKind.CUSTOM) {
                            dshApi = inferredDshApiForUrl(it)
                        }
                    },
                    onModel = { model = it },
                    onDshApi = { dshApi = it },
                    onApiKey = { apiKey = it },
                    hasStoredSecret = initial.kind == selected && initial.hasSecret,
                    onDiscover = {
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onDiscover(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey)
                    },
                    onValidate = { models ->
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onValidate(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey, models)
                    },
                    onSave = {
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onSave(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey)
                    },
                    onChangeAgent = { showAgentPicker = true },
                )
            }
        }
    }
}

@Composable
private fun DshApiProtocolPicker(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("anthropic-messages", "openai-completions", "openai-responses")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Gateway protocol",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        options.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSelected(option) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .border(
                            width = if (selected == option) 2.dp else 1.dp,
                            color = if (selected == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected == option) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
                Spacer(Modifier.width(10.dp))
                Text(option, fontSize = 13.sp)
            }
        }
        Text(
            "Pick the protocol your gateway speaks; DeepSeek Harness routes it directly.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun StepDots(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            Box(
                Modifier.height(5.dp).weight(1f)
                    .background(if (index <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
        }
    }
}

@Composable
private fun DeviceCheckStep(context: Context, onContinue: () -> Unit) {
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val memoryInfo = remember { ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo) }
    val totalRamGb = memoryInfo.totalMem.toDouble() / 1_073_741_824.0
    val totalRamLabel = String.format(java.util.Locale.US, "%.1f", totalRamGb)
    val arm64 = supportsArm64Runtime(Build.SUPPORTED_ABIS, System.getProperty("os.arch"))
    val compatible = arm64
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BrandMark()
        Text("Your phone is the workspace", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Rlaude Harness checks compatibility before downloading the private Linux runtime.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        CheckRow(Icons.Default.Memory, "Memory", "$totalRamLabel GB usable · ${if (totalRamGb >= 7.5) "Full mode" else "Lite mode"}", true)
        CheckRow(Icons.Default.Code, "Processor", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown", arm64)
        CheckRow(Icons.Default.Storage, "Android", "Android ${Build.VERSION.RELEASE}", true)
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
            Text(
                "Only open projects you trust. The local Linux environment is a compatibility layer, not a hardened security sandbox.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(onClick = onContinue, enabled = compatible, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (compatible) "Continue" else "This device is not supported")
        }
    }
}

@Composable
private fun CheckRow(icon: ImageVector, title: String, value: String, passed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(icon, null, Modifier.padding(11.dp).size(22.dp), tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Icon(if (passed) Icons.Default.Check else Icons.Default.Warning, null, tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ProviderChoiceStep(
    selected: ProviderKind,
    agentKind: AgentKind,
    onSelected: (ProviderKind) -> Unit,
    onContinue: () -> Unit,
    onChangeAgent: () -> Unit,
) {
    val visibleProviders = remember(agentKind) { providersForAgent(agentKind) }
    Column(Modifier.fillMaxHeight()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "STEP 2 OF 3",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.1.sp,
            )
            Spacer(Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Secure setup", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Connect your AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose how Rlaude Harness should access your coding model.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(visibleProviders) { index, provider ->
                    ProviderChoiceRow(
                        provider = provider,
                        selected = selected == provider,
                        onClick = { onSelected(provider) },
                    )
                    if (index != visibleProviders.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 68.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 12.dp, start = 2.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                "API keys are encrypted in Android secure storage.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
        TextButton(
            onClick = onChangeAgent,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp),
        ) {
            Text("Use another coding agent", fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProviderChoiceRow(
    provider: ProviderKind,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = when (provider) {
        ProviderKind.CLAUDE -> Color(0xFF919191)
        ProviderKind.ANTHROPIC -> Color(0xFFB1B1B1)
        ProviderKind.LLM_ROUTER -> Color(0xFF898989)
        ProviderKind.DEEPSEEK -> Color(0xFF737373)
        ProviderKind.KIMI -> Color(0xFF8E8E8E)
        ProviderKind.OPENCODE_ZEN -> Color(0xFF898989)
        ProviderKind.NVIDIA_NIM -> Color(0xFF909090)
        ProviderKind.CUSTOM -> MaterialTheme.colorScheme.primary
    }
    val mark = when (provider) {
        ProviderKind.CLAUDE -> "C"
        ProviderKind.ANTHROPIC -> "A"
        ProviderKind.LLM_ROUTER -> "OR"
        ProviderKind.DEEPSEEK -> "DS"
        ProviderKind.KIMI -> "K"
        ProviderKind.OPENCODE_ZEN -> "Z"
        ProviderKind.NVIDIA_NIM -> "NV"
        ProviderKind.CUSTOM -> "<>"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(9.dp))
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(mark, color = accent, fontSize = if (mark.length > 1) 9.sp else 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    provider.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (provider.experimental) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text(
                            "Beta",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(1.dp))
            Text(
                provider.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(19.dp)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderCredentialsStep(
    provider: ProviderKind,
    agentKind: AgentKind = AgentKind.CLAUDE_CODE,
    baseUrl: String,
    model: String,
    dshApi: String = "anthropic-messages",
    apiKey: String,
    onBaseUrl: (String) -> Unit,
    onModel: (String) -> Unit,
    onDshApi: (String) -> Unit = {},
    onApiKey: (String) -> Unit,
    hasStoredSecret: Boolean,
    onDiscover: suspend () -> ModelDiscoveryResult,
    onValidate: suspend (List<DiscoveredModel>) -> ConnectionValidation,
    onSave: () -> Unit,
    onChangeAgent: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var models by remember(baseUrl) { mutableStateOf(emptyList<DiscoveredModel>()) }
    var isDiscovering by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusDetails by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    var showModels by rememberSaveable { mutableStateOf(false) }
    var modelSearch by rememberSaveable { mutableStateOf("") }
    val modelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasKey = apiKey.isNotBlank() || hasStoredSecret
    val filteredModels = remember(models, modelSearch) {
        val query = modelSearch.trim()
        if (query.isEmpty()) models else models.filter {
            it.id.contains(query, ignoreCase = true) || it.displayName.contains(query, ignoreCase = true)
        }
    }

    if (provider == ProviderKind.CLAUDE) {
        ClaudeSubscriptionCredentialsStep(
            token = apiKey,
            hasStoredToken = hasStoredSecret,
            onToken = onApiKey,
            onSave = onSave,
            onChangeAgent = onChangeAgent,
        )
        return
    }

    fun discoverModels(openWhenReady: Boolean = true) {
        scope.launch {
            isDiscovering = true
            status = null
            statusDetails = null
            when (val result = onDiscover()) {
                is ModelDiscoveryResult.Success -> {
                    models = result.models
                    statusOk = true
                    status = "Found ${result.models.size} available model${if (result.models.size == 1) "" else "s"}."
                    if (model.isBlank() && result.models.isNotEmpty()) onModel(result.models.first().id)
                    if (openWhenReady && result.models.isNotEmpty()) showModels = true
                }
                is ModelDiscoveryResult.Failure -> {
                    statusOk = false
                    status = result.message
                    statusDetails = result.providerMessage
                }
            }
            isDiscovering = false
        }
    }

    if (showModels) {
        ModalBottomSheet(
            onDismissRequest = { showModels = false },
            sheetState = modelSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f).padding(horizontal = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Available models", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${filteredModels.size} of ${models.size} models",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                    IconButton(
                        onClick = { discoverModels(openWhenReady = false) },
                        enabled = !isDiscovering,
                    ) {
                        if (isDiscovering) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, "Refresh models")
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = modelSearch,
                    onValueChange = { modelSearch = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search model name or ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                if (filteredModels.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching models", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(filteredModels, key = { it.id }) { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onModel(option.id)
                                        status = null
                                        modelSearch = ""
                                        showModels = false
                                    }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.displayName, modifier = Modifier.weight(1f, fill = false), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (option.isFree) Text("  FREE", color = Color(0xFFA2A2A2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (option.displayName != option.id) {
                                        Text(option.id, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Box(
                                    Modifier.size(20.dp).border(
                                        if (model == option.id) 2.dp else 1.dp,
                                        if (model == option.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        CircleShape,
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (model == option.id) Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("STEP 3 OF 3", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text("Encrypted locally", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(provider.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(
                when {
                    agentKind == AgentKind.DEEPSEEK_HARNESS -> "DeepSeek Harness will connect through this API endpoint."
                    provider.protocol.name.startsWith("OPENAI") -> "Rlaude Harness will translate Claude Code requests for this provider."
                    else -> "Claude Code will connect through this API endpoint."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        baseUrl,
                        { onBaseUrl(it); status = null; statusDetails = null; models = emptyList() },
                        label = { Text("Base URL") },
                        supportingText = {
                            if (provider.fixedBaseUrl) Text("Fixed by ${provider.title}")
                        },
                        readOnly = provider.fixedBaseUrl,
                        enabled = !provider.fixedBaseUrl,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (agentKind == AgentKind.DEEPSEEK_HARNESS && provider in DSH_PROTOCOL_PROVIDERS && !provider.fixedProtocol) {
                        DshApiProtocolPicker(selected = dshApi, onSelected = { onDshApi(it); status = null })
                    }
                    OutlinedTextField(
                        apiKey,
                        { onApiKey(it); status = null; statusDetails = null },
                        label = { Text("API key") },
                        placeholder = { Text(if (hasStoredSecret) "Saved securely — leave blank to keep it" else "Enter your API key") },
                        supportingText = {
                            if (hasStoredSecret && apiKey.isBlank()) Text("A saved key is ready to use")
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        model,
                        { onModel(it); status = null; statusDetails = null },
                        label = { Text("Model name") },
                        supportingText = { Text("Select an available model or enter an exact model ID.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    if (models.isEmpty()) discoverModels() else showModels = true
                },
                enabled = baseUrl.isNotBlank() && hasKey && !isDiscovering && !isValidating,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (isDiscovering) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(if (models.isEmpty()) Icons.Default.Search else Icons.Default.KeyboardArrowDown, null, Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (models.isEmpty()) "Find available models" else "Available models (${models.size})")
            }
        }
        if (status != null) {
            item {
                Text(
                    status.orEmpty(),
                    color = if (statusOk) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                )
                statusDetails?.takeIf(String::isNotBlank)?.let { details ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        details,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
        item {
            Button(
                    onClick = {
                        scope.launch {
                            isValidating = true
                            status = "Checking API key, model, and Claude Code settings…"
                            statusDetails = null
                            statusOk = true
                            when (val result = onValidate(models)) {
                                is ConnectionValidation.Success -> {
                                    status = result.message
                                    statusOk = true
                                    onSave()
                                }
                                is ConnectionValidation.Failure -> {
                                    status = result.message
                                    statusDetails = result.providerMessage
                                    statusOk = false
                                }
                            }
                            isValidating = false
                        }
                    },
                    enabled = baseUrl.isNotBlank() && model.isNotBlank() && hasKey && !isDiscovering && !isValidating,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(7.dp))
                    }
                    Text(if (isValidating) "Checking" else "Continue")
            }
        }
        item {
            TextButton(
                onClick = onChangeAgent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Use another coding agent", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ClaudeSubscriptionCredentialsStep(
    token: String,
    hasStoredToken: Boolean,
    onToken: (String) -> Unit,
    onSave: () -> Unit,
    onChangeAgent: () -> Unit,
) {
    var tokenVisible by rememberSaveable { mutableStateOf(false) }
    val hasToken = token.isNotBlank() || hasStoredToken

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("STEP 3 OF 3", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text("Encrypted locally", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Claude subscription", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Connect a Claude Pro, Max, Team, or Enterprise subscription to Claude Code.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. On a computer where Claude Code is installed, run:", fontSize = 13.sp)
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "claude setup-token",
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text("2. Sign in to Claude and paste the generated token here.", fontSize = 13.sp)
                    OutlinedTextField(
                        value = token,
                        onValueChange = onToken,
                        label = { Text("Claude setup token") },
                        placeholder = { Text(if (hasStoredToken) "Saved securely — leave blank to keep it" else "Paste token") },
                        supportingText = if (hasStoredToken && token.isBlank()) ({ Text("A saved subscription token is ready to use") }) else null,
                        singleLine = true,
                        visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                Icon(if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle token visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        item {
            Button(
                onClick = onSave,
                enabled = hasToken,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Text("Save and continue")
            }
        }
        item {
            TextButton(onClick = onChangeAgent, modifier = Modifier.fillMaxWidth()) {
                Text("Use another coding agent", fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectsScreen(
    state: AppUiState,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    onOpen: (Project) -> Unit,
    onCreate: (String) -> Unit,
    onCreateQuickProject: () -> Unit,
    onImportZip: (Uri) -> Unit,
    onCloneGit: (String) -> Unit,
    onStartGitHubLogin: () -> Unit,
    onGenerateNewGitHubCode: () -> Unit,
    onRefreshGitHub: () -> Unit,
    onDisconnectGitHub: () -> Unit,
    onCloneGitHub: (GitHubRepository) -> Unit,
    onRenameProject: (String, String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onSettings: () -> Unit,
    onPing: () -> Unit,
    onToggleTheme: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    var showCreate by rememberSaveable { mutableStateOf(false) }
    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var showGitDialog by rememberSaveable { mutableStateOf(false) }
    var showGitHubDialog by rememberSaveable { mutableStateOf(false) }
    var importExpanded by rememberSaveable { mutableStateOf(false) }
    var gitUrl by rememberSaveable { mutableStateOf("") }
    var repositorySearch by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    val projects = remember(state.projects, state.isRunning, state.projectTerminalRunning, state.activeProject?.id) {
        state.projects.sortedWith(
            compareByDescending<Project> { project ->
                (state.isRunning || state.projectTerminalRunning) && state.activeProject?.id == project.id
            }.thenByDescending { it.updatedAtMillis }
        )
    }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onInstallUpdate()
    }
    val importZipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImportZip(uri)
    }
    LaunchedEffect(state.appUpdate?.versionCode) {
        if (state.appUpdate != null) showUpdateDialog = true
    }
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 8.dp),
                title = { Row(verticalAlignment = Alignment.CenterVertically) { BrandMark(compact = true); Spacer(Modifier.width(9.dp)); Text("Rlaude Harness", fontWeight = FontWeight.Bold) } },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color.White).padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Build from your phone", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Chat, review changes, and preview your project.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                val isImportExpanded = importExpanded || state.projectImporting || state.gitCloneRunning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onCreateQuickProject,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_quick_project),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "Quick project",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { importExpanded = !importExpanded },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(4.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isImportExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_custom_import),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "Import",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showCreate = true },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "New project",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = isImportExpanded) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(0.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("Bring an existing project", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "ZIP file, Git repository, or GitHub",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                ImportSourceButton(
                                    painter = painterResource(R.drawable.ic_custom_import),
                                    title = if (state.projectImporting) "Importing…" else "ZIP file",
                                    enabled = !state.projectImporting && !state.gitCloneRunning,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(0.dp),
                                    onClick = { importZipLauncher.launch("*/*") },
                                    loading = state.projectImporting,
                                )
                                ImportSourceButton(
                                    painter = painterResource(R.drawable.ic_custom_link),
                                    title = if (state.gitCloneRunning) "Cloning…" else "Git URL",
                                    enabled = !state.projectImporting && !state.gitCloneRunning,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(0.dp),
                                    onClick = { showGitDialog = true },
                                    loading = state.gitCloneRunning,
                                )
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable(enabled = !state.gitCloneRunning) {
                                    showGitHubDialog = true
                                    if (state.githubAuthStatus == GitHubAuthStatus.CONNECTED && state.githubRepositories.isEmpty()) onRefreshGitHub()
                                },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_custom_github),
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            state.githubLogin?.let { "GitHub · @$it" } ?: "Connect GitHub",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            if (state.githubLogin != null) "Browse public and private repositories" else "Sign in to access your repositories",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            (state.projectImportMessage ?: state.gitCloneMessage)?.let { message ->
                                Text(message, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            state.appUpdate?.let { update ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showUpdateDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), modifier = Modifier.size(46.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Rlaude Harness ${update.versionName}", fontWeight = FontWeight.Bold)
                                Text("A new update is ready", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Update", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            item { Text("Your projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (projects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(56.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "No projects yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                "Create a named project or start instantly with a Quick Project.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        taskRunning = state.isRunning && state.activeProject?.id == project.id,
                        terminalRunning = state.projectTerminalRunning && state.activeProject?.id == project.id,
                        onOpen = { onOpen(project) },
                        onRename = { onRenameProject(project.id, it) },
                        onDelete = { onDeleteProject(project.id) },
                    )
                }
            }
        }
    }
    if (showCreate) AlertDialog(
        onDismissRequest = { showCreate = false },
        title = { Text("Create a starter project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project name") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                )
                if (name.isNotBlank()) {
                    Text(
                        "Terminal folder: /workspace/${projectSlug(name)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name); showCreate = false; name = "" },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(4.dp),
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(
                onClick = { showCreate = false },
                shape = RoundedCornerShape(4.dp),
            ) { Text("Cancel") }
        },
        shape = RoundedCornerShape(4.dp),
    )
    if (showGitDialog) AlertDialog(
        onDismissRequest = { if (!state.gitCloneRunning) showGitDialog = false },
        icon = { Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Clone Git repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Paste a public HTTPS repository URL. Its complete Git history and current branch will be kept.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.5.sp)
                OutlinedTextField(
                    value = gitUrl,
                    onValueChange = { gitUrl = it },
                    label = { Text("HTTPS Git URL") },
                    placeholder = { Text("https://github.com/owner/repository.git") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                )
                state.gitCloneMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                enabled = gitUrl.isNotBlank() && !state.gitCloneRunning,
                onClick = { onCloneGit(gitUrl); showGitDialog = false; gitUrl = "" },
                shape = RoundedCornerShape(4.dp),
            ) { Text(if (state.gitCloneRunning) "Cloning…" else "Clone project") }
        },
        dismissButton = {
            TextButton(
                onClick = { showGitDialog = false },
                enabled = !state.gitCloneRunning,
                shape = RoundedCornerShape(4.dp),
            ) { Text("Cancel") }
        },
        shape = RoundedCornerShape(4.dp),
    )
    if (showGitHubDialog) {
        val clipboard = LocalClipboardManager.current
        val filteredRepositories = state.githubRepositories.filter { repository ->
            repositorySearch.isBlank() || repository.fullName.contains(repositorySearch, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { if (!state.gitCloneRunning) showGitHubDialog = false },
            icon = { Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(state.githubLogin?.let { "GitHub · @$it" } ?: "Connect GitHub") },
            text = {
                when (state.githubAuthStatus) {
                    GitHubAuthStatus.DISCONNECTED, GitHubAuthStatus.ERROR -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            state.githubMessage ?: "Sign in with GitHub's official CLI to browse public and private repositories.",
                            color = if (state.githubAuthStatus == GitHubAuthStatus.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        Button(onClick = onStartGitHubLogin, modifier = Modifier.fillMaxWidth()) { Text("Sign in with GitHub") }
                    }
                    GitHubAuthStatus.STARTING -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(state.githubMessage ?: "Starting GitHub sign-in…")
                    }
                    GitHubAuthStatus.AWAITING_USER -> Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enter this one-time code in the GitHub page opened in your browser.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { state.githubUserCode?.let { clipboard.setText(AnnotatedString(it)) } },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                state.githubUserCode.orEmpty(),
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = 2.sp,
                            )
                        }
                        Text("Tap the code to copy it. Rlaude Harness will connect automatically after approval.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(
                            onClick = onGenerateNewGitHubCode,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generate new code")
                        }
                    }
                    GitHubAuthStatus.CONNECTED -> Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.githubMessage ?: "Select a repository", modifier = Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(onClick = onRefreshGitHub, enabled = !state.githubRepositoriesLoading) {
                                Icon(Icons.Default.Refresh, "Refresh repositories")
                            }
                        }
                        OutlinedTextField(
                            value = repositorySearch,
                            onValueChange = { repositorySearch = it },
                            placeholder = { Text("Search repositories") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.githubRepositoriesLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filteredRepositories, key = { it.fullName }) { repository ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = !state.gitCloneRunning) {
                                        showGitHubDialog = false
                                        onCloneGitHub(repository)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                ) {
                                    Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if (repository.private) Icons.Default.Key else Icons.Default.Code, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(9.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(repository.fullName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${if (repository.private) "Private" else "Public"} · ${repository.defaultBranch}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (state.githubAuthStatus == GitHubAuthStatus.CONNECTED) {
                    TextButton(onClick = { showGitHubDialog = false }) { Text("Close") }
                }
            },
            dismissButton = {
                if (state.githubAuthStatus == GitHubAuthStatus.CONNECTED) {
                    TextButton(onClick = { onDisconnectGitHub(); showGitHubDialog = false }) { Text("Disconnect") }
                } else if (state.githubAuthStatus != GitHubAuthStatus.STARTING) {
                    TextButton(onClick = { showGitHubDialog = false }) { Text("Cancel") }
                }
            },
        )
    }
    val update = state.appUpdate
    if (showUpdateDialog && update != null) {
        val canInstall = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()
        val downloading = state.appUpdateStatus == AppUpdateStatus.DOWNLOADING
        val installing = state.appUpdateStatus == AppUpdateStatus.INSTALLING
        val total = state.appUpdateTotalBytes
        val downloaded = state.appUpdateDownloadedBytes
        val progress = if (total > 0) (downloaded.toFloat() / total).coerceIn(0f, 1f) else 0f
        AlertDialog(
            onDismissRequest = { if (!installing) showUpdateDialog = false },
            icon = { Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) },
            title = { Text("Update to ${update.versionName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(update.notes.ifBlank { "Get the latest improvements and fixes for Rlaude Harness." })
                    if (update.sizeBytes > 0) Text("Download size: ${formatMegabytes(update.sizeBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    if (!canInstall) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Allow ‘Install unknown apps’ for Rlaude Harness. Without this permission, Android will not install the update.", fontSize = 13.sp)
                            }
                        }
                    }
                    if (downloading) {
                        if (total > 0) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        else LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            if (total > 0) "Downloading ${formatMegabytes(downloaded)} / ${formatMegabytes(total)} · ${(progress * 100).toInt()}%" else "Downloading ${formatMegabytes(downloaded)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (installing) Text("Download verified. Opening Android installer…", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    state.appUpdateError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !downloading && !installing,
                    onClick = {
                        if (!canInstall && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            permissionLauncher.launch(
                                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")),
                            )
                        } else {
                            onInstallUpdate()
                        }
                    },
                ) {
                    Text(when { !canInstall -> "Grant permission"; downloading -> "Downloading…"; installing -> "Installing…"; else -> "Download and install" })
                }
            },
            dismissButton = { if (!installing) TextButton(onClick = { showUpdateDialog = false }) { Text("Later") } },
        )
    }
}

@Composable
private fun ImportSourceButton(
    painter: Painter,
    title: String,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(46.dp),
        shape = shape,
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
        ),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
        else Icon(painter, null, Modifier.size(17.dp), tint = Color.Black)
        Spacer(Modifier.width(7.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun ImportSourceButton(
    icon: ImageVector,
    title: String,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    onClick: () -> Unit,
) = ImportSourceButton(rememberVectorPainter(icon), title, enabled, loading, modifier, shape, onClick)

@Composable
private fun ApiStatusChip(state: AppUiState, onSettings: () -> Unit, onPing: () -> Unit) {
    val dotColor = when (state.apiPingStatus) {
        ApiPingStatus.OK -> MaterialTheme.colorScheme.primary
        ApiPingStatus.FAILED -> MaterialTheme.colorScheme.error
        ApiPingStatus.PINGING -> MaterialTheme.colorScheme.primary
        ApiPingStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val providerLabel = when {
        state.agentKind == AgentKind.ANTIGRAVITY ->
            state.antigravityModel.ifBlank { state.agentKind.title }
        state.provider.model.isNotBlank() -> state.provider.model
        state.provider.baseUrl.isNotBlank() -> {
            runCatching { java.net.URI(state.provider.baseUrl).host ?: state.provider.kind.title }
                .getOrDefault(state.provider.kind.title)
        }
        else -> state.provider.kind.title
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onSettings).padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            // Model / provider name
            Text(
                text = providerLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            // Ping button
            IconButton(
                onClick = onPing,
                modifier = Modifier.size(28.dp),
            ) {
                if (state.apiPingStatus == ApiPingStatus.PINGING) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Ping API",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}



@Composable
private fun ProjectCard(
    project: Project,
    taskRunning: Boolean,
    terminalRunning: Boolean,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by rememberSaveable(project.id) { mutableStateOf(false) }
    var showRename by rememberSaveable(project.id) { mutableStateOf(false) }
    var showDelete by rememberSaveable(project.id) { mutableStateOf(false) }
    var renameText by rememberSaveable(project.id) { mutableStateOf(project.name) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(Modifier.padding(start = 14.dp, top = 7.dp, bottom = 7.dp, end = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(11.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                Icon(Icons.Default.Folder, null, Modifier.padding(8.dp).size(18.dp), tint = Color.Black)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        project.name,
                        modifier = Modifier.weight(1f, fill = false),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (taskRunning || terminalRunning) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (taskRunning) "Task running" else "Terminal running",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    if (project.kind == ProjectKind.QUICK_PROJECT) "Quick project" else project.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
                Text(
                    "/workspace/${project.slug} · ${project.formattedUpdatedAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                Surface(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(width = 26.dp, height = 16.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.Black,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Project options",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = Color.White) {
                    DropdownMenuItem(
                        text = { Text("Rename project") },
                        leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_write), null) },
                        onClick = { menuOpen = false; renameText = project.name; showRename = true },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete project") },
                        leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_delete), null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuOpen = false; showDelete = true },
                    )
                }
            }
        }
    }
    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Rename project") },
            text = { OutlinedTextField(renameText, { renameText = it }, label = { Text("Project name") }, singleLine = true, shape = RoundedCornerShape(4.dp)) },
            confirmButton = { TextButton(onClick = { onRename(renameText); showRename = false }, enabled = renameText.isNotBlank(), shape = RoundedCornerShape(4.dp)) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showRename = false }, shape = RoundedCornerShape(4.dp)) { Text("Cancel") } },
            shape = RoundedCornerShape(4.dp),
        )
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this project?") },
            text = { Text("Its chats, files, attachments, changes, and terminal history will be permanently removed.") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }, shape = RoundedCornerShape(4.dp)) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDelete = false }, shape = RoundedCornerShape(4.dp)) { Text("Cancel") } },
            shape = RoundedCornerShape(4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadOnlyProjectScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onSwitchChat: (String) -> Unit,
    onContinueHere: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val project = state.readOnlyProject ?: return
    val activeChat = state.readOnlyProjectChats.firstOrNull { it.id == state.readOnlyChatId }
    val listState = rememberLazyListState()
    var showChats by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.readOnlyChatId) {
        if (state.readOnlyMessages.isNotEmpty()) listState.scrollToItem(state.readOnlyMessages.lastIndex)
    }

    if (showChats) {
        ChatSwitcherDialog(
            chats = state.readOnlyProjectChats,
            activeChatId = state.readOnlyChatId,
            switchingEnabled = true,
            allowCreate = false,
            onDismiss = { showChats = false },
            onCreate = {},
            onSwitch = { chatId ->
                onSwitchChat(chatId)
                showChats = false
            },
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${activeChat?.title ?: "Chat"} · History",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Projects") }
                },
                actions = {
                    IconButton(onClick = { showChats = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_history),
                            contentDescription = "Project chats",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Color.White).padding(padding)) {
            CompositionLocalProvider(LocalAttachmentRoot provides attachmentRootFor(LocalContext.current, project)) {
            ChatTab(
                messages = state.readOnlyMessages,
                approval = null,
                liveProcess = emptyList(),
                isRunning = false,
                onSend = {},
                onStop = {},
                onApproval = {},
                listState = listState,
                taskStartedAtMillis = null,
                taskFinishedAtMillis = null,
                thinkingActive = false,
                agentKind = state.agentKind,
                pendingAttachments = emptyList(),
                onAttach = {},
                onRemoveAttachment = {},
                onOpenAttachment = {},
                onRunInTerminal = {},
                readOnly = true,
                readOnlyBlocked = state.isRunning || state.projectTerminalRunning,
                onContinueHere = onContinueHere,
            )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WorkspaceScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onApproval: (Boolean) -> Unit,
    onAnswerQuestion: (String, String?, Boolean) -> Unit = { _, _, _ -> },
    onRefreshFiles: () -> Unit,
    onOpenFile: (WorkspaceEntry) -> Unit,
    onCloseFile: () -> Unit,
    onUndoChanges: () -> Unit,
    onKeepChanges: () -> Unit,
    onUndoFileChange: (String) -> Unit,
    onKeepFileChange: (String) -> Unit,
    onCreateChat: () -> Unit,
    onSwitchChat: (String) -> Unit,
    onTerminalRun: (String) -> Unit,
    onTerminalInput: (String) -> Unit,
    onTerminalInterrupt: () -> Unit,
    onTerminalPrepare: (String) -> Unit,
    onTerminalDraftConsumed: () -> Unit,
    onTerminalOpened: () -> Unit,
    onTerminalStop: () -> Unit,
    onTerminalClear: () -> Unit,
    onTerminalConfirm: () -> Unit,
    onTerminalCancel: () -> Unit,
    onUseSuggestedProjectRoot: () -> Unit,
    onExportProject: (Uri) -> Unit,
    onExportFile: (WorkspaceEntry, Uri) -> Unit,
    onExportChangedFilesZip: (List<String>, Uri) -> Unit = { _, _ -> },
    onAddAttachments: (List<Uri>) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onRenameAttachment: (String, String) -> Unit = { _, _ -> },
    onOpenAttachment: (ChatAttachment) -> Unit,
    onEditMessage: (ChatMessage) -> Unit = {},
    onAddTextAttachment: (String) -> Unit = {},
    onBuildAndRunAndroid: () -> Unit,
    onDraftChange: (String) -> Unit = {},
    onGetVersionFiles: (Int) -> List<WorkspaceEntry> = { emptyList() },
    onExportVersionZip: (Int, Uri) -> Unit = { _, _ -> },
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val isAndroidProject = state.androidProjectDetected
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val exportProjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> if (uri != null) onExportProject(uri) },
    )
    var versionNumberToExport by remember { mutableStateOf<Int?>(null) }
    val exportVersionZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            val vNum = versionNumberToExport
            if (uri != null && vNum != null) {
                onExportVersionZip(vNum, uri)
            }
            versionNumberToExport = null
        },
    )
    var fileToExport by remember { mutableStateOf<WorkspaceEntry?>(null) }
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            val entry = fileToExport
            if (uri != null && entry != null) onExportFile(entry, uri)
            fileToExport = null
        },
    )
    var pathsToExportAsZip by remember { mutableStateOf<List<String>?>(null) }
    val exportChangedZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            val paths = pathsToExportAsZip
            if (uri != null && !paths.isNullOrEmpty()) {
                onExportChangedFilesZip(paths, uri)
            }
            pathsToExportAsZip = null
        },
    )
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = onAddAttachments,
    )
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10),
        onResult = { uris -> if (uris.isNotEmpty()) onAddAttachments(uris) },
    )
    val attachmentRoot = remember(state.activeProject?.id, state.activeProject?.rootPath) {
        attachmentRootFor(context, state.activeProject)
    }
    val unknownAppsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()) {
                onBuildAndRunAndroid()
            } else {
                Toast.makeText(context, "Allow app installs to run Android projects", Toast.LENGTH_LONG).show()
            }
        },
    )
    val chatListState = rememberLazyListState()
    var userScrolledUp by rememberSaveable { mutableStateOf(false) }

    val chatItemCount = state.messages.size +
        (if (state.liveProcess.isNotEmpty() || state.liveThinking) 1 else 0) +
        (if (state.pendingApproval != null) 1 else 0)

    LaunchedEffect(state.activeChatId) {
        userScrolledUp = false
        if (chatItemCount > 0) chatListState.scrollToItem(chatItemCount - 1)
    }

    val isUserDragging by chatListState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isUserDragging) {
        if (isUserDragging) {
            userScrolledUp = true
        } else {
            userScrolledUp = chatListState.canScrollForward
        }
    }

    LaunchedEffect(chatListState.isScrollInProgress) {
        if (!chatListState.isScrollInProgress && !chatListState.canScrollForward) {
            userScrolledUp = false
        }
    }

    // Follow new tokens/updates continuously when user has not scrolled away manually.
    LaunchedEffect(
        state.messages.size,
        state.messages.lastOrNull()?.text?.length,
        state.liveProcess.size,
        state.liveProcess.lastOrNull()?.detail,
        state.pendingApproval,
        userScrolledUp,
    ) {
        if (!state.isRunning || chatItemCount <= 0 || userScrolledUp || isUserDragging) return@LaunchedEffect
        chatListState.scrollToItem((chatItemCount - 1).coerceAtLeast(0), scrollOffset = 100000)
    }

    var selectedTab by rememberSaveable { mutableStateOf(WorkspaceTab.CHAT) }
    BackHandler(enabled = selectedTab != WorkspaceTab.CHAT) { selectedTab = WorkspaceTab.CHAT }
    var showChats by rememberSaveable { mutableStateOf(false) }
    var activeZipScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var activeZipVersionNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    val activeChat = state.projectChats.firstOrNull { it.id == state.activeChatId }

    // If a file is open, show the FileViewerScreen on top
    if (state.openedFilePath != null) {
        BackHandler(onBack = {
            onCloseFile()
            selectedTab = WorkspaceTab.FILES
        })
        FileViewerScreen(
            filePath = state.openedFilePath,
            content = state.openedFileContent,
            loading = state.fileContentLoading,
            onClose = {
                onCloseFile()
                selectedTab = WorkspaceTab.FILES
            },
        )
        return
    }

    if (activeZipScreen != null) {
        val vNum = activeZipVersionNumber
        val zipFiles = remember(vNum, state.workspaceFiles) {
            if (vNum != null) {
                onGetVersionFiles(vNum)
            } else {
                state.workspaceFiles
            }
        }
        ZipContentsScreen(
            zipName = activeZipScreen ?: "${state.activeProject?.slug ?: "project"}.zip",
            files = zipFiles,
            onBack = {
                activeZipScreen = null
                activeZipVersionNumber = null
            },
            onExport = {
                val exportName = activeZipScreen ?: "${state.activeProject?.slug ?: "project"}.zip"
                if (vNum != null) {
                    versionNumberToExport = vNum
                    exportVersionZipLauncher.launch(exportName)
                } else {
                    exportProjectLauncher.launch(exportName)
                }
            },
            onOpenFile = onOpenFile,
            onDownloadFile = { entry ->
                fileToExport = entry
                exportFileLauncher.launch(entry.name)
            },
        )
        return
    }

    if (showChats) {
        ChatSwitcherDialog(
            chats = state.projectChats,
            activeChatId = state.activeChatId,
            switchingEnabled = !state.isRunning,
            onDismiss = { showChats = false },
            onCreate = {
                onCreateChat()
                showChats = false
                selectedTab = WorkspaceTab.CHAT
            },
            onSwitch = { chatId ->
                onSwitchChat(chatId)
                showChats = false
                selectedTab = WorkspaceTab.CHAT
            },
        )
    }
    state.pendingTerminalCommand?.let { command ->
        AlertDialog(
            onDismissRequest = onTerminalCancel,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Run potentially destructive command?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This command can delete files, rewrite Git history, or change the project significantly.")
                    Surface(color = Color(0xFF171717), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            command,
                            Modifier.fillMaxWidth().padding(10.dp),
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE7E7E7),
                        )
                    }
                }
            },
            confirmButton = { Button(onClick = onTerminalConfirm) { Text("Run anyway") } },
            dismissButton = { TextButton(onClick = onTerminalCancel) { Text("Cancel") } },
        )
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.White,
        topBar = {
            if (selectedTab != WorkspaceTab.CHAT) TopAppBar(
                title = { Text(selectedTab.label, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { selectedTab = WorkspaceTab.CHAT }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            ) else TopAppBar(
                title = {
                    Text(
                        state.activeProject?.name.orEmpty(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                Toast.makeText(context, state.activeProject?.name.orEmpty(), Toast.LENGTH_LONG).show()
                            },
                        ),
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Projects") } },
                actions = {
                    if (isAndroidProject) {
                        IconButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    !context.packageManager.canRequestPackageInstalls()) {
                                    unknownAppsLauncher.launch(
                                        Intent(
                                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                            Uri.parse("package:${context.packageName}"),
                                        ),
                                    )
                                } else {
                                    onBuildAndRunAndroid()
                                }
                            },
                            enabled = !state.androidBuildRunning && !state.isRunning && !state.projectTerminalRunning,
                        ) {
                            if (state.androidBuildRunning) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.PlayArrow, "Build and run Android app")
                        }
                    }
                    IconButton(onClick = { showChats = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_history),
                            contentDescription = "Project chats",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black,
                        )
                    }
                    if (state.isRunning) CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            if (selectedTab == WorkspaceTab.CHAT && !keyboardVisible) WorkspaceDock(
                onTerminal = {
                    selectedTab = WorkspaceTab.TERMINAL
                    onTerminalOpened()
                },
                onFiles = {
                    selectedTab = WorkspaceTab.FILES
                    onRefreshFiles()
                },
                onPreview = { selectedTab = WorkspaceTab.PREVIEW },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            CompositionLocalProvider(LocalAttachmentRoot provides attachmentRoot) {
            when (selectedTab) {
                WorkspaceTab.CHAT -> ChatTab(
                    state.messages,
                    state.pendingApproval,
                    state.pendingQuestions,
                    state.liveProcess,
                    state.isRunning,
                    onSend,
                    onStop,
                    onApproval,
                    onAnswerQuestion,
                    listState = chatListState,
                    taskStartedAtMillis = state.workSegmentStartedAtMillis ?: state.taskStartedAtMillis,
                    taskFinishedAtMillis = state.taskFinishedAtMillis,
                    thinkingActive = state.liveThinking,
                    agentKind = state.agentKind,
                    pendingAttachments = state.pendingAttachments,
                    onAttach = {
                        attachmentLauncher.launch(arrayOf("*/*"))
                    },
                    onPickPhotos = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onRemoveAttachment = onRemoveAttachment,
                    onOpenAttachment = onOpenAttachment,
                    onRunInTerminal = { command ->
                        selectedTab = WorkspaceTab.TERMINAL
                        onTerminalOpened()
                        onTerminalPrepare(command)
                    },
                    onOpenFiles = {
                        selectedTab = WorkspaceTab.FILES
                        onRefreshFiles()
                    },
                    onAddAttachments = onAddAttachments,
                    onEditMessage = onEditMessage,
                    onAddTextAttachment = onAddTextAttachment,
                    onRenameAttachment = onRenameAttachment,
                    onJumpToLatest = { userScrolledUp = false },
                    draftPrompt = state.draftPrompt,
                    draftKey = state.draftKey,
                    onDraftChange = onDraftChange,
                    projectSlug = state.activeProject?.slug ?: "project",
                    onDownloadZip = { paths, fileName ->
                        pathsToExportAsZip = paths
                        exportChangedZipLauncher.launch(fileName)
                    },
                )
                WorkspaceTab.FILES -> Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    FilesTab(
                        files = state.workspaceFiles,
                        loading = state.filesLoading,
                        suggestedProjectRoot = state.suggestedProjectRoot,
                        onRefresh = onRefreshFiles,
                        onOpenFile = onOpenFile,
                        onUseSuggestedProjectRoot = onUseSuggestedProjectRoot,
                        onExport = {
                            val exportName = state.activeProject?.name?.let { projectSlug(it) } ?: state.activeProject?.slug ?: "project"
                            exportProjectLauncher.launch("$exportName.zip")
                        },
                        onDownloadFile = { entry ->
                            fileToExport = entry
                            exportFileLauncher.launch(entry.name)
                        },
                        isImportedZipProject = state.activeProject?.description?.startsWith("Imported", ignoreCase = true) == true,
                        projectSlug = state.activeProject?.slug ?: "project",
                        latestVersionTag = state.latestVersionTag,
                        isVersionInProgress = state.isVersionInProgress,
                        inProgressVersionTag = state.inProgressVersionTag,
                        onOpenZip = { zipName ->
                            activeZipScreen = zipName
                            activeZipVersionNumber = null
                        },
                        projectVersions = state.projectVersions,
                        onOpenVersionZip = { version, zipName ->
                            activeZipScreen = zipName
                            activeZipVersionNumber = version.versionNumber
                        },
                        onExportVersionZip = { version ->
                            versionNumberToExport = version.versionNumber
                            val exportName = state.activeProject?.name?.let { projectSlug(it) } ?: state.activeProject?.slug ?: "project"
                            exportVersionZipLauncher.launch("$exportName.zip")
                        },
                    )
                }
                WorkspaceTab.TERMINAL -> Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    TerminalScreen(
                        lines = state.projectTerminalLines,
                        isRunning = state.projectTerminalRunning,
                        onRun = onTerminalRun,
                        onInput = onTerminalInput,
                        onInterrupt = onTerminalInterrupt,
                        onClear = onTerminalClear,
                        onToggleTheme = {},
                        themeMode = state.themeMode,
                        title = "Project Terminal",
                        subtitle = "${state.projectTerminalCwd} · Ubuntu PRoot",
                        liveOutput = state.projectTerminalLiveOutput,
                        currentCommand = state.projectTerminalCommand,
                        commandDraft = state.projectTerminalDraft,
                        onCommandDraftConsumed = onTerminalDraftConsumed,
                        promptPath = state.projectTerminalCwd,
                        onStop = onTerminalStop,
                        showThemeAction = false,
                        showQuickCommands = false,
                        compactHeader = true,
                    )
                }
                WorkspaceTab.CHANGES -> Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    ChangesTab(
                        state.changes,
                        onUndoChanges,
                        onKeepChanges,
                        onUndoFileChange,
                        onKeepFileChange,
                    )
                }
                WorkspaceTab.PREVIEW -> Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    PreviewTab(state.previewReady, state.previewUrl)
                }
            }
            }
        }
    }
}

@Composable
private fun ChatSwitcherDialog(
    chats: List<ProjectChat>,
    activeChatId: String?,
    switchingEnabled: Boolean,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onSwitch: (String) -> Unit,
    allowCreate: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Project chats") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (allowCreate) {
                    Button(onClick = onCreate, enabled = switchingEnabled, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("New chat")
                    }
                }
                if (!switchingEnabled) {
                    Text("Finish the running task before switching chats.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(chats, key = { it.id }) { chat ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = switchingEnabled) { onSwitch(chat.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (chat.id == activeChatId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(ChatAltFillIcon, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(chat.title, fontWeight = if (chat.id == activeChatId) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
                                    Text(
                                        if (chat.id == activeChatId) "Current chat" else "Saved conversation",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (chat.id == activeChatId) Icon(Icons.Default.Check, "Current", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileViewerScreen(
    filePath: String,
    content: String?,
    loading: Boolean,
    onClose: () -> Unit,
) {
    val fileName = filePath.substringAfterLast('/')
    val ext = fileName.substringAfterLast('.', "")
    val isMarkdown = ext == "md"
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(fileName, fontWeight = FontWeight.SemiBold)
                        Text(filePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close file") }
                },
                actions = {
                    if (!content.isNullOrEmpty()) {
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(content))
                            copied = true
                            scope.launch { delay(2000); copied = false }
                        }) {
                            if (copied) {
                                Icon(
                                    Icons.Default.Check,
                                    "File contents copied",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_custom_copy),
                                    contentDescription = "Copy file contents",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                content == null -> {
                    EmptyState(Icons.Default.Description, "No content", "The file could not be read.")
                }
                isMarkdown -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item { MarkdownText(markdown = content, color = MaterialTheme.colorScheme.onSurface) }
                    }
                }
                else -> {
                    // Code / plain-text viewer
                    LazyColumn(
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF101010)),
                    ) {
                        val lines = content.lines()
                        items(lines.size) { idx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    modifier = Modifier
                                        .width(42.dp)
                                        .padding(start = 8.dp, end = 6.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFF545454),
                                    textAlign = TextAlign.End,
                                )
                                Text(
                                    text = lines[idx],
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = Color(0xFFE7E7E7),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildExpandedFileTree(
    files: List<WorkspaceEntry>,
    expandedSet: Set<String>,
): List<WorkspaceEntry> {
    val byParent = files.groupBy { entry ->
        if (entry.path.contains('/')) entry.path.substringBeforeLast('/') else ""
    }
    val result = mutableListOf<WorkspaceEntry>()
    fun appendChildren(parentPath: String) {
        val children = byParent[parentPath].orEmpty().sortedWith(
            compareByDescending<WorkspaceEntry> { it.isDirectory }.thenBy { it.name.lowercase() }
        )
        for (child in children) {
            result.add(child)
            if (child.isDirectory && child.path in expandedSet) {
                appendChildren(child.path)
            }
        }
    }
    appendChildren("")
    return result
}

@Composable
private fun VersionShimmerPlaceholderCard(
    projectSlug: String,
    versionTag: String,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    val cardShape = RoundedCornerShape(2.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = Color.White.copy(alpha = alpha),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_custom_zip),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Black.copy(alpha = alpha),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${projectSlug}.zip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${versionTag.lowercase()} · preparing...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZipContentsScreen(
    zipName: String,
    files: List<WorkspaceEntry>,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onOpenFile: (WorkspaceEntry) -> Unit,
    onDownloadFile: (WorkspaceEntry) -> Unit,
) {
    BackHandler(onBack = onBack)

    var expandedDirectories by rememberSaveable(zipName) { mutableStateOf(emptyList<String>()) }
    val expandedSet = expandedDirectories.toSet()
    val visibleFiles = remember(files, expandedSet) {
        buildExpandedFileTree(files, expandedSet)
    }
    val directChildCounts = remember(files) {
        files.filter { it.path.contains('/') }
            .groupingBy { it.path.substringBeforeLast('/') }
            .eachCount()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = zipName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to files",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onExport) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_download),
                            contentDescription = "Download zip",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(visibleFiles, key = { it.path }) { entry ->
                val rowShape = RoundedCornerShape(2.dp)
                if (entry.isDirectory) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_folder),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${entry.name} (${directChildCounts[entry.path] ?: 0})",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    expandedDirectories = if (entry.path in expandedSet) {
                                        expandedDirectories.filterNot { it == entry.path || it.startsWith("${entry.path}/") }
                                    } else {
                                        expandedDirectories + entry.path
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (entry.path in expandedSet) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (entry.path in expandedSet) "Collapse folder" else "Expand folder",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    val isChanged = entry.isNewInCurrentVersion
                    val itemColor = if (isChanged) Color(0xFF2E7D32) else Color.Black
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .clip(rowShape)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                rowShape,
                            )
                            .background(Color.White, rowShape)
                            .clickable {
                                onOpenFile(entry)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(
                                if (entry.name.endsWith(".zip", ignoreCase = true)) R.drawable.ic_custom_zip else R.drawable.ic_custom_file
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = itemColor,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = entry.name,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.5.sp,
                            color = itemColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formatFileSize(entry.sizeBytes),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (entry.name.endsWith(".zip", ignoreCase = true)) {
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { onDownloadFile(entry) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    painterResource(R.drawable.ic_custom_download),
                                    "Download ${entry.name}",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilesTab(
    files: List<WorkspaceEntry>,
    loading: Boolean,
    suggestedProjectRoot: String?,
    onRefresh: () -> Unit,
    onOpenFile: (WorkspaceEntry) -> Unit,
    onUseSuggestedProjectRoot: () -> Unit,
    onExport: () -> Unit,
    onDownloadFile: (WorkspaceEntry) -> Unit,
    isImportedZipProject: Boolean = false,
    projectSlug: String = "project",
    latestVersionTag: String = "v1.0",
    isVersionInProgress: Boolean = false,
    inProgressVersionTag: String? = null,
    onOpenZip: (String) -> Unit = {},
    projectVersions: List<ProjectVersion> = emptyList(),
    onOpenVersionZip: (ProjectVersion, String) -> Unit = { _, _ -> },
    onExportVersionZip: (ProjectVersion) -> Unit = {},
) {
    val hasFiles = files.any { !it.isDirectory }
    val zipVersionLabel = latestVersionTag.lowercase()

    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Files",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!loading && hasFiles) {
                    IconButton(onClick = onExport, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_custom_download),
                            "Export project as ZIP",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                if (loading) {
                    CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_custom_refresh),
                            "Refresh files",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        if (suggestedProjectRoot != null) {
            item(key = "suggested-project-root") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Project folder detected", fontWeight = FontWeight.Bold)
                        Text(
                            "Use $suggestedProjectRoot as the project root so Chat, Terminal, Changes, and Preview all run from the same folder.",
                            fontSize = 13.sp,
                        )
                        Button(onClick = onUseSuggestedProjectRoot, modifier = Modifier.fillMaxWidth()) {
                            Text("Use $suggestedProjectRoot as project root")
                        }
                    }
                }
            }
        }
        if (!loading && files.isEmpty() && !isVersionInProgress) {
            item { EmptyState(Icons.Default.Folder, "No files yet", "Ask your coding agent to create something in this project.") }
        }

        if (isVersionInProgress) {
            item(key = "in-progress-version-shimmer") {
                VersionShimmerPlaceholderCard(
                    projectSlug = projectSlug,
                    versionTag = inProgressVersionTag ?: "Next version",
                )
            }
        }

        if (projectVersions.isNotEmpty()) {
            val sortedVersions = projectVersions.sortedByDescending { it.versionNumber }
            items(sortedVersions, key = { "version-${it.versionNumber}" }) { version ->
                val cardShape = RoundedCornerShape(2.dp)
                val count = if (version.filesCount > 0) version.filesCount else files.count { !it.isDirectory }
                val fileLabel = if (count == 1) "1 file" else "$count files"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVersionZip(version, "${projectSlug}.zip") },
                    shape = cardShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_zip),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${projectSlug}.zip",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${version.versionTag.lowercase()} · $fileLabel",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { onExportVersionZip(version) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_custom_download),
                                contentDescription = "Download project zip ${version.versionTag}",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Black,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open zip contents",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else if (hasFiles) {
            item(key = "project-zip-card") {
                val cardShape = RoundedCornerShape(2.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenZip("${projectSlug}.zip") },
                    shape = cardShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_zip),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${projectSlug}.zip",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "$zipVersionLabel · ${files.count { !it.isDirectory }} files",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = onExport,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_custom_download),
                                contentDescription = "Download project zip",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Black,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open zip contents",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatTab(
    messages: List<ChatMessage>,
    approval: ToolRequest?,
    pendingQuestions: List<QuestionRequest> = emptyList(),
    liveProcess: List<ActivityItem>,
    isRunning: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onApproval: (Boolean) -> Unit,
    onAnswerQuestion: (String, String?, Boolean) -> Unit = { _, _, _ -> },
    listState: LazyListState,
    taskStartedAtMillis: Long?,
    taskFinishedAtMillis: Long?,
    thinkingActive: Boolean,
    agentKind: AgentKind,
    pendingAttachments: List<ChatAttachment>,
    onAttach: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onOpenAttachment: (ChatAttachment) -> Unit,
    onRunInTerminal: (String) -> Unit,
    readOnly: Boolean = false,
    readOnlyBlocked: Boolean = false,
    onContinueHere: () -> Unit = {},
    onPickPhotos: () -> Unit = {},
    onOpenFiles: () -> Unit = {},
    onAddAttachments: (List<Uri>) -> Unit = {},
    onEditMessage: (ChatMessage) -> Unit = {},
    onAddTextAttachment: (String) -> Unit = {},
    onJumpToLatest: () -> Unit = {},
    draftPrompt: String = "",
    draftKey: String = "",
    onDraftChange: (String) -> Unit = {},
    projectSlug: String = "project",
    onDownloadZip: (List<String>, String) -> Unit = { _, _ -> },
    onRenameAttachment: (String, String) -> Unit = { _, _ -> },
) {
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    // Keep the screen on while the selected agent is working in this chat. Released automatically
    // when the task finishes or the user leaves the chat tab.
    DisposableEffect(isRunning) {
        view.keepScreenOn = isRunning
        onDispose { view.keepScreenOn = false }
    }
    var prompt by rememberSaveable(draftKey) { mutableStateOf(draftPrompt) }
    LaunchedEffect(draftKey) {
        if (prompt != draftPrompt) {
            prompt = draftPrompt
        }
    }
    LaunchedEffect(prompt) {
        if (prompt.isEmpty()) {
            onDraftChange(prompt)
        } else {
            delay(200)
            onDraftChange(prompt)
        }
    }
    DisposableEffect(draftKey) {
        onDispose {
            onDraftChange(prompt)
        }
    }
    val focusRequester = remember { FocusRequester() }
    val chatScope = rememberCoroutineScope()
    var viewingTextAttachment by remember { mutableStateOf<ChatAttachment?>(null) }
    var viewingImageAttachment by remember { mutableStateOf<ChatAttachment?>(null) }
    var viewingImageAllowMarkup by remember { mutableStateOf(false) }
    val displayMessages = remember(messages) {
        messages.filterNot { !it.fromUser && it.text.startsWith("Hi! Tell me") }
    }
    val atMentionQuery = remember(prompt) {
        val lastAt = prompt.lastIndexOf('@')
        if (lastAt >= 0 && (lastAt == 0 || prompt[lastAt - 1].isWhitespace())) {
            val query = prompt.substring(lastAt + 1)
            if (!query.contains(' ') && !query.contains('\n')) query else null
        } else null
    }
    val matchingMentionAttachments = remember(atMentionQuery, pendingAttachments) {
        if (atMentionQuery == null || pendingAttachments.isEmpty()) emptyList()
        else pendingAttachments.filter { it.displayName.contains(atMentionQuery, ignoreCase = true) }
    }
    // True while the newest item (message, live panel, or approval card) is on screen.
    val readerAtBottom by remember {
        derivedStateOf {
            !listState.canScrollForward
        }
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Box(Modifier.weight(1f)) {
            if (displayMessages.isEmpty() && liveProcess.isEmpty() && !thinkingActive && !isRunning && approval == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Hi! Tell me what you want to build or change.",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(displayMessages, key = { _, message -> message.id }) { index, message ->
                        if (message.workItems.isNotEmpty()) {
                            WorkBlockCard(message, projectSlug, onOpenFiles, onDownloadZip)
                        }
                        if (message.text.isNotBlank() || (message.workItems.isEmpty() && message.fromUser)) {
                            MessageBubble(
                                message = message,
                                onRunInTerminal = onRunInTerminal,
                                onOpenAttachment = { attachment ->
                                    if (isImageAttachment(attachment)) {
                                        viewingImageAllowMarkup = false
                                        viewingImageAttachment = attachment
                                    } else {
                                        viewingTextAttachment = attachment
                                    }
                                },
                                onEdit = if (!readOnly) {
                                    { editedMessage ->
                                        if (!isRunning) {
                                            prompt = editedMessage.text
                                            onEditMessage(editedMessage)
                                            chatScope.launch {
                                                focusRequester.requestFocus()
                                                val totalCount = listState.layoutInfo.totalItemsCount
                                                if (totalCount > 0) {
                                                    listState.animateScrollToItem((totalCount - 1).coerceAtLeast(0), scrollOffset = 100000)
                                                }
                                            }
                                        }
                                    }
                                } else null,
                                canEdit = !isRunning && !readOnly,
                            )
                        }

                        val isTurnEnd = !message.fromUser && (
                            (index == displayMessages.lastIndex && !isRunning) ||
                            (index < displayMessages.lastIndex && displayMessages[index + 1].fromUser)
                        )
                        if (isTurnEnd) {
                            val turnChangedPaths = remember(displayMessages, index) {
                                var startIdx = index
                                while (startIdx > 0 && !displayMessages[startIdx - 1].fromUser) {
                                    startIdx--
                                }
                                val hasActualWritesInTurn = (startIdx..index).any { i ->
                                    displayMessages[i].workItems.any { isWriteToolName(it.title) }
                                }
                                if (!hasActualWritesInTurn) {
                                    emptyList()
                                } else {
                                    val paths = mutableListOf<String>()
                                    for (i in startIdx..index) {
                                        val m = displayMessages[i]
                                        paths.addAll(extractTurnChangedPaths(m))
                                    }
                                    paths.distinct()
                                }
                            }
                            if (turnChangedPaths.isNotEmpty()) {
                                TurnCompletionSummaryCard(
                                    changedPaths = turnChangedPaths,
                                    projectSlug = projectSlug,
                                    onDownloadZip = onDownloadZip,
                                )
                            }
                        }
                    }
                    if (liveProcess.isNotEmpty() || thinkingActive) {
                        item(key = "live-claude-process") {
                            LiveClaudeProcess(
                                processItems = liveProcess,
                                isRunning = isRunning,
                                startedAtMillis = taskStartedAtMillis,
                                finishedAtMillis = taskFinishedAtMillis,
                                thinkingActive = thinkingActive,
                            )
                        }
                    }
                    if (isRunning) {
                        item(key = "running-rabbit-indicator") {
                            RunningRabbitIndicator(
                                isRunning = isRunning,
                                streamingLength = messages.lastOrNull()?.takeIf { !it.fromUser }?.text?.length ?: 0,
                                processItems = liveProcess,
                                startedAtMillis = taskStartedAtMillis,
                                agentKind = agentKind,
                            )
                        }
                    }
                    approval?.let { request -> item { ApprovalCard(request, onApproval) } }
                }
            }
            if (!readerAtBottom) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clickable {
                            onJumpToLatest()
                            chatScope.launch {
                                val totalItems = listState.layoutInfo.totalItemsCount
                                if (totalItems > 0) {
                                    val targetIndex = totalItems - 1
                                    listState.scrollToItem(targetIndex, scrollOffset = 100000)
                                }
                            }
                        },
                    shape = CircleShape,
                    shadowElevation = 3.dp,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Row(
                        Modifier.padding(start = 13.dp, end = 15.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Latest",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            viewingTextAttachment?.let { attachment ->
                TextAttachmentBottomSheet(
                    attachment = attachment,
                    onDismiss = { viewingTextAttachment = null },
                    onRename = { newName ->
                        onRenameAttachment(attachment.id, newName)
                    },
                )
            }
            viewingImageAttachment?.let { attachment ->
                ImageAttachmentBottomSheet(
                    attachment = attachment,
                    allowMarkup = viewingImageAllowMarkup,
                    onDismiss = { viewingImageAttachment = null },
                    onRename = { newName ->
                        onRenameAttachment(attachment.id, newName)
                    },
                )
            }
        }
        val activeQuestion = pendingQuestions.firstOrNull()
        if (activeQuestion != null && !readOnly) {
            QuestionCard(
                request = activeQuestion,
                onAnswer = { answer -> onAnswerQuestion(activeQuestion.questionId, answer, false) },
                onSkip = { onAnswerQuestion(activeQuestion.questionId, null, true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }
        if (readOnly) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_custom_history),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Read-only history", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (readOnlyBlocked) {
                                "Another project has a running task. You can read this chat, but cannot send a message."
                            } else {
                                "The other task finished. Open this project to continue chatting."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!readOnlyBlocked) {
                        TextButton(onClick = onContinueHere) { Text("Open") }
                    }
                }
            }
        } else {
            val canSend = prompt.isNotBlank() || pendingAttachments.isNotEmpty()
            var attachMenuOpen by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp, top = if (activeQuestion != null) 6.dp else 4.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                ) {
                    if (pendingAttachments.isNotEmpty()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            pendingAttachments.forEach { attachment ->
                                AttachmentPreview(
                                    attachment = attachment,
                                    onOpen = {
                                        if (isImageAttachment(attachment)) {
                                            viewingImageAllowMarkup = true
                                            viewingImageAttachment = attachment
                                        } else {
                                            viewingTextAttachment = attachment
                                        }
                                    },
                                    onRemove = { onRemoveAttachment(attachment.id) },
                                )
                            }
                            if (pendingAttachments.size < MainViewModel.MAX_ATTACHMENTS_PER_MESSAGE) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clickable { attachMenuOpen = true },
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.AddOutlined,
                                            contentDescription = "Add another attachment",
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val context = LocalContext.current

                    if (matchingMentionAttachments.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 3.dp,
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "ATTACHMENTS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                                matchingMentionAttachments.take(5).forEach { att ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val lastAt = prompt.lastIndexOf('@')
                                                if (lastAt >= 0) {
                                                    val prefix = prompt.substring(0, lastAt)
                                                    prompt = "$prefix@${att.displayName} "
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                if (att.mimeType.startsWith("image/")) R.drawable.ic_photos else R.drawable.ic_custom_file
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "@${att.displayName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BasicTextField(
                        value = prompt,
                        onValueChange = { input ->
                            val isImageUri = (input.startsWith("content://") || input.startsWith("file://")) &&
                                (input.contains("image") || input.endsWith(".png") || input.endsWith(".jpg") || input.endsWith(".jpeg") || input.endsWith(".webp") || input.endsWith(".gif"))
                            if (isImageUri) {
                                runCatching {
                                    val uri = Uri.parse(input.trim())
                                    onAddAttachments(listOf(uri))
                                }.onSuccess {
                                    prompt = ""
                                }.onFailure {
                                    prompt = input
                                }
                            } else {
                                val isVeryLong = input.length >= 800 || (input.length >= 350 && input.count { it == '\n' } >= 8)
                                if (isVeryLong && pendingAttachments.size < MainViewModel.MAX_ATTACHMENTS_PER_MESSAGE) {
                                    val chunk = if (prompt.isNotBlank() && input.startsWith(prompt)) {
                                        input.substring(prompt.length).trim()
                                    } else if (prompt.isNotBlank() && input.endsWith(prompt)) {
                                        input.substring(0, input.length - prompt.length).trim()
                                    } else {
                                        input.trim()
                                    }
                                    if (chunk.length >= 500 || (chunk.length >= 250 && chunk.count { it == '\n' } >= 6)) {
                                        onAddTextAttachment(chunk)
                                        prompt = if (prompt.isNotBlank() && (input.startsWith(prompt) || input.endsWith(prompt))) prompt else ""
                                        Toast.makeText(context, "Converted long text to attachment", Toast.LENGTH_SHORT).show()
                                    } else {
                                        prompt = input
                                    }
                                } else {
                                    prompt = input
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 160.dp)
                            .focusRequester(focusRequester)
                            .contentReceiver { transferable ->
                                if (transferable.hasMediaType(MediaType.Image)) {
                                    val clipData = transferable.clipEntry.clipData
                                    val uris = (0 until clipData.itemCount).mapNotNull { clipData.getItemAt(it).uri }
                                    if (uris.isNotEmpty()) {
                                        onAddAttachments(uris)
                                        transferable.consume { it.uri != null }
                                    } else {
                                        transferable
                                    }
                                } else if (transferable.hasMediaType(MediaType.Text)) {
                                    val clipData = transferable.clipEntry.clipData
                                    val text = (0 until clipData.itemCount).mapNotNull { clipData.getItemAt(it).text?.toString() }.joinToString("\n")
                                    if ((text.length >= 800 || (text.length >= 350 && text.count { it == '\n' } >= 8)) && pendingAttachments.size < MainViewModel.MAX_ATTACHMENTS_PER_MESSAGE) {
                                        onAddTextAttachment(text)
                                        Toast.makeText(context, "Converted long text to attachment", Toast.LENGTH_SHORT).show()
                                        transferable.consume { true }
                                    } else {
                                        transferable
                                    }
                                } else {
                                    transferable
                                }
                            },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (prompt.isEmpty()) {
                                    Text(
                                        text = "Message ${agentKind.title}…",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp,
                                        lineHeight = 21.sp,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            IconButton(
                                onClick = { attachMenuOpen = true },
                                enabled = pendingAttachments.size < MainViewModel.MAX_ATTACHMENTS_PER_MESSAGE,
                                modifier = Modifier.size(38.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddOutlined,
                                    contentDescription = "Add photos or files",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black,
                                )
                            }
                            DropdownMenu(
                                expanded = attachMenuOpen,
                                onDismissRequest = { attachMenuOpen = false },
                                containerColor = Color.White,
                                properties = PopupProperties(focusable = false),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Photos") },
                                    leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_photos), null, tint = Color.Black) },
                                    onClick = {
                                        attachMenuOpen = false
                                        onPickPhotos()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Files") },
                                    leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_files), null, tint = Color.Black) },
                                    onClick = {
                                        attachMenuOpen = false
                                        onAttach()
                                    },
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                                    .clickable(onClick = onStop),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop AI task",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape,
                                    )
                                    .clickable(
                                        enabled = canSend,
                                        onClick = {
                                            if (canSend) {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                                onSend(prompt)
                                                prompt = ""
                                            }
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(2.dp))
                    }
                }
            }
        }
    }
}

private fun pasteClipboardContent(
    context: Context,
    onAddAttachments: (List<Uri>) -> Unit,
    onAddTextAttachment: (String) -> Unit,
    onSetPrompt: ((String) -> Unit)? = null,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    val clip = clipboard?.primaryClip
    if (clip != null && clip.itemCount > 0) {
        val uris = mutableListOf<Uri>()
        var textContent: String? = null
        for (i in 0 until clip.itemCount) {
            val item = clip.getItemAt(i)
            if (item.uri != null) {
                uris.add(item.uri)
            } else {
                val text = item.text?.toString()?.trim()
                if (!text.isNullOrBlank()) {
                    if (text.startsWith("content://") || text.startsWith("file://")) {
                        runCatching { Uri.parse(text) }.getOrNull()?.let { uris.add(it) }
                    } else if (textContent == null) {
                        textContent = text
                    }
                }
            }
        }
        if (uris.isNotEmpty()) {
            onAddAttachments(uris)
            Toast.makeText(context, "Image pasted", Toast.LENGTH_SHORT).show()
        } else if (!textContent.isNullOrBlank()) {
            val txt = textContent!!
            if (txt.length >= 600 || txt.lines().size >= 8) {
                onAddTextAttachment(txt)
                Toast.makeText(context, "Text pasted as attachment", Toast.LENGTH_SHORT).show()
            } else if (onSetPrompt != null) {
                onSetPrompt(txt)
                Toast.makeText(context, "Pasted text", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No image or long text found in clipboard", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No content found in clipboard", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun RunningRabbit(
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1f,
    rabbitSize: Dp = 26.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
                lastTime = now
                val hz = 1.6f * speedMultiplier
                phase = (phase + dt * hz * 2f * Math.PI.toFloat()) % (2f * Math.PI.toFloat())
            }
        }
    }

    val hop = kotlin.math.abs(kotlin.math.sin(phase)).coerceIn(0f, 1f)
    val bounceY = -(5.dp * hop)
    val tilt = (kotlin.math.sin(phase) * 6f) - 6f
    val scaleX = if (hop < 0.25f) 1.08f else 0.94f
    val scaleY = if (hop < 0.25f) 0.92f else 1.06f

    Box(
        modifier = modifier.size(rabbitSize + 8.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.width(rabbitSize + 4.dp).height(4.dp)) {
            val shadowWidth = size.width * (0.85f - hop * 0.35f)
            val shadowAlpha = 0.25f * (1f - hop * 0.5f)
            drawOval(
                color = tint.copy(alpha = shadowAlpha),
                topLeft = Offset((size.width - shadowWidth) / 2f, size.height / 2f),
                size = Size(shadowWidth, 3f),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_rabbit),
            contentDescription = "Thinking...",
            tint = tint,
            modifier = Modifier
                .size(rabbitSize)
                .graphicsLayer {
                    translationY = bounceY.toPx()
                    rotationZ = tilt
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                },
        )
    }
}

@Composable
private fun RunningRabbitIndicator(
    isRunning: Boolean,
    streamingLength: Int = 0,
    processItems: List<ActivityItem> = emptyList(),
    startedAtMillis: Long? = null,
    agentKind: AgentKind = AgentKind.ANTIGRAVITY,
) {
    if (!isRunning) return

    val textProgress = (streamingLength / 800f).coerceIn(0f, 1f)
    val stepsProgress = (processItems.size / 6f).coerceIn(0f, 1f)
    val combinedProgress = (textProgress * 0.75f + stepsProgress * 0.25f).coerceIn(0f, 1f)
    val speedMultiplier = 1.0f + (combinedProgress * 1.8f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 2.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        RunningRabbit(
            speedMultiplier = speedMultiplier,
            rabbitSize = 18.dp,
            tint = Color.Black,
        )
    }
}

private fun readAttachmentText(root: java.io.File?, attachment: ChatAttachment, context: Context? = null): String? {
    return runCatching {
        val candidates = mutableListOf<java.io.File>()
        if (root != null) {
            candidates.add(java.io.File(root, attachment.relativePath))
            root.parentFile?.let { candidates.add(java.io.File(it, attachment.relativePath)) }
        }
        if (context != null) {
            candidates.add(java.io.File(context.filesDir, "workspaces/${attachment.relativePath}"))
            candidates.add(java.io.File(context.filesDir, attachment.relativePath))
        }
        candidates.add(java.io.File(attachment.relativePath))
        val file = candidates.firstOrNull { it.isFile && it.canRead() }
        file?.readText(Charsets.UTF_8)
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextAttachmentBottomSheet(
    attachment: ChatAttachment,
    onDismiss: () -> Unit,
    onRename: ((String) -> Unit)? = null,
) {
    val root = LocalAttachmentRoot.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val textScrollState = rememberScrollState()

    var currentDisplayName by remember(attachment.id, attachment.displayName) { mutableStateOf(attachment.displayName) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameDraft by remember(showRenameDialog) { mutableStateOf(currentDisplayName) }

    val content by produceState<String?>(initialValue = null, attachment.relativePath, root) {
        value = withContext(Dispatchers.IO) {
            readAttachmentText(root, attachment, context)
        }
    }

    LaunchedEffect(attachment.id) {
        textScrollState.scrollTo(0)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .clickable {
                            val text = content
                            if (!text.isNullOrBlank()) {
                                clipboard.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_custom_copy),
                        contentDescription = "Copy text",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            if (content == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.Black)
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                        .verticalScroll(textScrollState)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    SelectionContainer {
                        MarkdownText(
                            markdown = content.orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            renameDraft = currentDisplayName
                            showRenameDialog = true
                        },
                    shape = RoundedCornerShape(3.dp),
                    color = Color.Black,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currentDisplayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFontFamily,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_write),
                            contentDescription = "Rename attachment",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = {
                        Text("Rename attachment", fontWeight = FontWeight.Bold, color = Color.Black)
                    },
                    text = {
                        OutlinedTextField(
                            value = renameDraft,
                            onValueChange = { renameDraft = it },
                            singleLine = true,
                            label = { Text("Display name") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val trimmed = renameDraft.trim()
                                if (trimmed.isNotBlank()) {
                                    currentDisplayName = trimmed
                                    onRename?.invoke(trimmed)
                                }
                                showRenameDialog = false
                            }
                        ) {
                            Text("Rename", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}

private data class MarkupStroke(
    val points: List<Offset>,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageAttachmentBottomSheet(
    attachment: ChatAttachment,
    allowMarkup: Boolean = true,
    onDismiss: () -> Unit,
    onRename: ((String) -> Unit)? = null,
) {
    val root = LocalAttachmentRoot.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentDisplayName by remember(attachment.id, attachment.displayName) { mutableStateOf(attachment.displayName) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameDraft by remember(showRenameDialog) { mutableStateOf(currentDisplayName) }

    val initialBitmap by produceState<ImageBitmap?>(initialValue = null, attachment.relativePath, root) {
        value = withContext(Dispatchers.IO) {
            decodeAttachmentBitmap(root, attachment, context, 2048)
        }
    }
    var currentBitmap by remember(attachment.id) { mutableStateOf<ImageBitmap?>(null) }
    val displayBitmap = currentBitmap ?: initialBitmap

    val markupColors = remember { listOf(Color.Black, Color(0xFFFF2A2A), Color(0xFF2563EB), Color.White) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF2A2A)) }
    var isAnnotating by remember { mutableStateOf(false) }
    var strokes by remember(attachment.id) { mutableStateOf(listOf<MarkupStroke>()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun saveMarkup() {
        val bmp = displayBitmap ?: return
        if (strokes.isEmpty() || containerSize.width <= 0 || containerSize.height <= 0) {
            isAnnotating = false
            return
        }
        val imgW = bmp.width
        val imgH = bmp.height
        val scale = minOf(
            containerSize.width.toFloat() / imgW.toFloat(),
            containerSize.height.toFloat() / imgH.toFloat(),
        )
        val renderedW = imgW * scale
        val renderedH = imgH * scale
        val offsetX = (containerSize.width - renderedW) / 2f
        val offsetY = (containerSize.height - renderedH) / 2f

        val currentStrokes = strokes
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val candidates = mutableListOf<java.io.File>()
                if (root != null) {
                    candidates.add(java.io.File(root, attachment.relativePath))
                    root.parentFile?.let { candidates.add(java.io.File(it, attachment.relativePath)) }
                }
                if (context != null) {
                    candidates.add(java.io.File(context.filesDir, "workspaces/${attachment.relativePath}"))
                    candidates.add(java.io.File(context.filesDir, attachment.relativePath))
                }
                candidates.add(java.io.File(attachment.relativePath))
                val file = candidates.firstOrNull { it.isFile && it.canWrite() } ?: candidates.firstOrNull { it.isFile }
                if (file == null) {
                    withContext(Dispatchers.Main) {
                        isAnnotating = false
                    }
                    return@launch
                }

                val fullBitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@launch
                val mutableBmp = fullBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutableBmp)

                val fullScaleX = mutableBmp.width.toFloat() / imgW.toFloat()
                val fullScaleY = mutableBmp.height.toFloat() / imgH.toFloat()
                val strokeScale = (fullScaleX + fullScaleY) / 2f

                for (strokeItem in currentStrokes) {
                    val strokeColorInt = android.graphics.Color.rgb(
                        (strokeItem.color.red * 255).toInt(),
                        (strokeItem.color.green * 255).toInt(),
                        (strokeItem.color.blue * 255).toInt(),
                    )
                    val paint = android.graphics.Paint().apply {
                        color = strokeColorInt
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = (6f / scale * strokeScale).coerceAtLeast(2f)
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        strokeJoin = android.graphics.Paint.Join.ROUND
                        isAntiAlias = true
                    }
                    val dotPaint = android.graphics.Paint().apply {
                        color = strokeColorInt
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val strokePoints = strokeItem.points
                    if (strokePoints.size >= 2) {
                        val path = android.graphics.Path()
                        val first = strokePoints.first()
                        val fx = (first.x - offsetX) / scale * fullScaleX
                        val fy = (first.y - offsetY) / scale * fullScaleY
                        path.moveTo(fx, fy)
                        for (i in 1 until strokePoints.size) {
                            val pt = strokePoints[i]
                            val px = (pt.x - offsetX) / scale * fullScaleX
                            val py = (pt.y - offsetY) / scale * fullScaleY
                            path.lineTo(px, py)
                        }
                        canvas.drawPath(path, paint)
                    } else if (strokePoints.size == 1) {
                        val pt = strokePoints[0]
                        val px = (pt.x - offsetX) / scale * fullScaleX
                        val py = (pt.y - offsetY) / scale * fullScaleY
                        canvas.drawCircle(px, py, (3f / scale * strokeScale).coerceAtLeast(1f), dotPaint)
                    }
                }

                val isPng = file.extension.equals("png", ignoreCase = true)
                val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                val quality = if (isPng) 100 else 92
                file.outputStream().use { out ->
                    mutableBmp.compress(format, quality, out)
                }

                withContext(Dispatchers.Main) {
                    currentBitmap = mutableBmp.asImageBitmap()
                    strokes = emptyList()
                    currentPath = emptyList()
                    isAnnotating = false
                    Toast.makeText(context, "Markup saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (allowMarkup) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAnnotating) MaterialTheme.colorScheme.primary else Color(0xFF1F1F1F))
                            .clickable { isAnnotating = !isAnnotating },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_custom_markup),
                            contentDescription = if (isAnnotating) "Exit markup mode" else "Mark up image",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White,
                        )
                    }

                    if (isAnnotating && (strokes.isNotEmpty() || currentPath.isNotEmpty())) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1F1F1F))
                                .clickable {
                                    strokes = emptyList()
                                    currentPath = emptyList()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear markup",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White,
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (allowMarkup && isAnnotating) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = Color.White,
                        modifier = Modifier.height(36.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            markupColors.forEach { color ->
                                val isSelected = color == selectedColor
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (color == Color.White) Modifier.border(0.5.dp, Color(0x33000000), CircleShape)
                                            else Modifier
                                        )
                                        .clickable { selectedColor = color },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected color",
                                            modifier = Modifier.size(14.dp),
                                            tint = if (color == Color.White) Color.Black else Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0xFF1F1F1F))
                            .clickable { saveMarkup() }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PoppinsFontFamily,
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                val image = displayBitmap
                if (image != null) {
                    val drawModifier = if (allowMarkup && isAnnotating) {
                        Modifier.pointerInput(attachment.id) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath = currentPath + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        strokes = strokes + listOf(MarkupStroke(currentPath, selectedColor))
                                        currentPath = emptyList()
                                    }
                                },
                                onDragCancel = {
                                    currentPath = emptyList()
                                },
                            )
                        }
                    } else {
                        Modifier
                    }

                    val stroke = remember {
                        Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .onSizeChanged { containerSize = it }
                            .then(drawModifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = image,
                            contentDescription = attachment.displayName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (allowMarkup) {
                            Canvas(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                for (strokeItem in strokes) {
                                    val strokePoints = strokeItem.points
                                    if (strokePoints.size >= 2) {
                                        val path = Path().apply {
                                            moveTo(strokePoints.first().x, strokePoints.first().y)
                                            for (i in 1 until strokePoints.size) {
                                                lineTo(strokePoints[i].x, strokePoints[i].y)
                                            }
                                        }
                                        drawPath(path = path, color = strokeItem.color, style = stroke)
                                    } else if (strokePoints.size == 1) {
                                        drawCircle(color = strokeItem.color, radius = 3f, center = strokePoints[0])
                                    }
                                }
                                if (currentPath.size >= 2) {
                                    val path = Path().apply {
                                        moveTo(currentPath.first().x, currentPath.first().y)
                                        for (i in 1 until currentPath.size) {
                                            lineTo(currentPath[i].x, currentPath[i].y)
                                        }
                                    }
                                    drawPath(path = path, color = selectedColor, style = stroke)
                                } else if (currentPath.size == 1) {
                                    drawCircle(color = selectedColor, radius = 3f, center = currentPath[0])
                                }
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            renameDraft = currentDisplayName
                            showRenameDialog = true
                        },
                    shape = RoundedCornerShape(3.dp),
                    color = Color.Black,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currentDisplayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFontFamily,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_write),
                            contentDescription = "Rename attachment",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = {
                        Text("Rename attachment", fontWeight = FontWeight.Bold, color = Color.Black)
                    },
                    text = {
                        OutlinedTextField(
                            value = renameDraft,
                            onValueChange = { renameDraft = it },
                            singleLine = true,
                            label = { Text("Display name") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val trimmed = renameDraft.trim()
                                if (trimmed.isNotBlank()) {
                                    currentDisplayName = trimmed
                                    onRename?.invoke(trimmed)
                                }
                                showRenameDialog = false
                            }
                        ) {
                            Text("Rename", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}

@Composable
private fun LiveClaudeProcess(
    processItems: List<ActivityItem>,
    isRunning: Boolean,
    startedAtMillis: Long?,
    finishedAtMillis: Long?,
    thinkingActive: Boolean,
) {
    val elapsedSeconds = startedAtMillis?.let { rememberLiveElapsedSeconds(it).toLong() } ?: 0L
    ClaudeActivityDisclosure(
        items = processItems,
        headline = activityHeadline(processItems, elapsedSeconds, thinkingActive),
        isRunning = isRunning,
    )
}

private fun isWriteToolName(title: String): Boolean {
    val task = title.removePrefix("Running ").removeSuffix(" completed").trim()
    return task.equals("Write", ignoreCase = true) ||
        task.equals("Edit", ignoreCase = true) ||
        task.equals("NotebookEdit", ignoreCase = true) ||
        task.equals("write_to_file", ignoreCase = true) ||
        task.equals("replace_file_content", ignoreCase = true) ||
        task.equals("multi_replace_file_content", ignoreCase = true)
}

private fun extractTurnChangedPaths(message: ChatMessage): List<String> {
    val fromItems = message.workItems
        .filter { isWriteToolName(it.title) }
        .map {
            val raw = it.detail.trim().trim('`')
            if (raw.contains(" · ")) raw.substringAfter(" · ").trim() else raw
        }
        .filter { it.isNotBlank() }
        .distinct()
    if (fromItems.isNotEmpty()) return fromItems
    val hasWriteWorkItem = message.workItems.any { isWriteToolName(it.title) }
    if (hasWriteWorkItem && message.changedFiles.isNotEmpty()) {
        return message.changedFiles
    }
    return emptyList()
}

@Composable
private fun WorkBlockCard(
    message: ChatMessage,
    projectSlug: String = "project",
    onOpenFiles: () -> Unit = {},
    onDownloadZip: (List<String>, String) -> Unit = { _, _ -> },
) {
    val seconds = (message.workedMillis / 1_000L).coerceAtLeast(1L)
    val changedPaths = remember(message.id, message.workItems, message.changedFiles) {
        extractTurnChangedPaths(message)
    }
    val changedFileNames = remember(changedPaths) {
        changedPaths.map { it.substringAfterLast('/') }.distinct()
    }
    var isExpanded by rememberSaveable(message.id) { mutableStateOf(false) }

    Column {
        ClaudeActivityDisclosure(
            items = message.workItems,
            headline = activityHeadline(message.workItems, seconds, message.workItems.isEmpty()),
        )
        if (message.workItems.lastOrNull()?.title?.startsWith("Task stopped") == true) {
            Text(
                text = "Worked for ${formatDuration(seconds)}",
                modifier = Modifier.padding(start = 29.dp, bottom = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        if (changedFileNames.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp)
                    .clickable { isExpanded = !isExpanded },
                shape = RoundedCornerShape(2.dp),
                color = Color.White,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_custom_file),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Files changed (${changedFileNames.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            changedFileNames.joinToString(", "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse summary" else "Expand summary",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                        changedPaths.forEach { path ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (path.endsWith(".zip", ignoreCase = true)) R.drawable.ic_custom_zip else R.drawable.ic_custom_file
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = path,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
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
private fun TurnCompletionSummaryCard(
    changedPaths: List<String>,
    projectSlug: String = "project",
    onDownloadZip: (List<String>, String) -> Unit = { _, _ -> },
) {
    var showTreeSheet by rememberSaveable { mutableStateOf(false) }
    val zipFileName = remember(projectSlug) { "${projectSlug}-changes.zip" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            .clickable { showTreeSheet = true },
        shape = RoundedCornerShape(2.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_custom_zip),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = zipFileName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${changedPaths.size} changed file" + if (changedPaths.size > 1) "s" else "",
                    fontSize = 11.sp,
                    color = Color.Black,
                )
            }
            IconButton(
                onClick = { onDownloadZip(changedPaths, zipFileName) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_custom_download),
                    contentDescription = "Download zip",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black,
                )
            }
        }
    }

    if (showTreeSheet) {
        ZipContentsSheet(
            zipName = zipFileName,
            changedPaths = changedPaths,
            onDownload = {
                showTreeSheet = false
                onDownloadZip(changedPaths, zipFileName)
            },
            onDismiss = { showTreeSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZipContentsSheet(
    zipName: String,
    changedPaths: List<String>,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val entries = remember(changedPaths) { buildZipTreeEntries(changedPaths) }
    var expandedDirs by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val expandedSet = expandedDirs.toSet()
    val visibleEntries = remember(entries, expandedSet) {
        buildExpandedFileTree(entries, expandedSet)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_custom_zip),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = zipName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${changedPaths.size} changed file" + if (changedPaths.size > 1) "s" else "",
                        fontSize = 12.sp,
                        color = Color.Black,
                    )
                }
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_custom_download),
                        contentDescription = "Download zip",
                        modifier = Modifier.size(22.dp),
                        tint = Color.Black,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(visibleEntries, key = { it.path }) { entry ->
                    val rowShape = RoundedCornerShape(2.dp)
                    if (entry.isDirectory) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    expandedDirs = if (entry.path in expandedSet) {
                                        expandedDirs.filterNot { it == entry.path || it.startsWith("${entry.path}/") }
                                    } else {
                                        expandedDirs + entry.path
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_custom_folder),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = entry.name,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        expandedDirs = if (entry.path in expandedSet) {
                                            expandedDirs.filterNot { it == entry.path || it.startsWith("${entry.path}/") }
                                        } else {
                                            expandedDirs + entry.path
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (entry.path in expandedSet) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = if (entry.path in expandedSet) "Collapse folder" else "Expand folder",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rowShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, rowShape)
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painterResource(if (entry.name.endsWith(".zip", ignoreCase = true)) R.drawable.ic_custom_zip else R.drawable.ic_custom_file),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Black,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = entry.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_custom_download),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White,
                )
                Spacer(Modifier.width(8.dp))
                Text("Download ZIP", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun buildZipTreeEntries(paths: List<String>): List<WorkspaceEntry> {
    val entries = mutableMapOf<String, WorkspaceEntry>()
    paths.forEach { rawPath ->
        val clean = rawPath.trim().trimStart('/')
        if (clean.isBlank()) return@forEach
        val parts = clean.split('/')
        var currentPath = ""
        parts.forEachIndexed { index, part ->
            val isDir = index < parts.lastIndex
            currentPath = if (currentPath.isEmpty()) part else "$currentPath/$part"
            if (!entries.containsKey(currentPath)) {
                entries[currentPath] = WorkspaceEntry(
                    path = currentPath,
                    name = part,
                    isDirectory = isDir,
                    depth = currentPath.count { it == '/' },
                )
            }
        }
    }
    return entries.values.toList()
}

@Composable
private fun ClaudeActivityDisclosure(
    items: List<ActivityItem>,
    headline: String,
    isRunning: Boolean = false,
) {
    var expandedItems by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
        if (items.isEmpty()) {
            ActivitySummaryRow(
                item = null,
                text = headline,
                expanded = 0 in expandedItems,
                showProgress = isRunning,
                onToggle = {
                    expandedItems = if (0 in expandedItems) expandedItems - 0 else expandedItems + 0
                },
            )
            if (0 in expandedItems) ActivityExpandedDetail(null, "Reviewing the request and planning the next action.")
        } else {
            items.forEachIndexed { index, item ->
                ActivitySummaryRow(
                    item = item,
                    text = compactActivityText(item),
                    expanded = index in expandedItems,
                    showProgress = isRunning && !item.isComplete,
                    onToggle = {
                        expandedItems = if (index in expandedItems) expandedItems - index else expandedItems + index
                    },
                )
                if (index in expandedItems) ActivityExpandedDetail(item, activityDetail(item))
            }
        }
    }
}

@Composable
private fun AnimatedThinkingDots(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val transition = rememberInfiniteTransition(label = "thinking_dots")
    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0f at 0
                -3.5f at 220
                0f at 440
                0f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0),
        ),
        label = "dot1",
    )
    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0f at 0
                -3.5f at 220
                0f at 440
                0f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(180),
        ),
        label = "dot2",
    )
    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0f at 0
                -3.5f at 220
                0f at 440
                0f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(360),
        ),
        label = "dot3",
    )

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0.35f at 0
                1f at 220
                0.35f at 440
                0.35f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0),
        ),
        label = "dot1_alpha",
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0.35f at 0
                1f at 220
                0.35f at 440
                0.35f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(180),
        ),
        label = "dot2_alpha",
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                0.35f at 0
                1f at 220
                0.35f at 440
                0.35f at 1100
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(360),
        ),
        label = "dot3_alpha",
    )

    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(3.5.dp)
                .graphicsLayer { translationY = dot1Offset * density }
                .background(dotColor.copy(alpha = dot1Alpha), CircleShape),
        )
        Box(
            Modifier
                .size(3.5.dp)
                .graphicsLayer { translationY = dot2Offset * density }
                .background(dotColor.copy(alpha = dot2Alpha), CircleShape),
        )
        Box(
            Modifier
                .size(3.5.dp)
                .graphicsLayer { translationY = dot3Offset * density }
                .background(dotColor.copy(alpha = dot3Alpha), CircleShape),
        )
    }
}

@Composable
private fun ActivitySummaryRow(
    item: ActivityItem?,
    text: String,
    expanded: Boolean,
    showProgress: Boolean,
    onToggle: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    // Every step (Think, Task stopped, Write, Read, Web search, Bash, ...) gets the same
    // outlined box around its row, with a near-square corner radius.
    val outlineShape = RoundedCornerShape(2.dp)
    val rowModifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)
    val content: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                activityIcon(item),
                null,
                Modifier.size(16.dp),
                tint = muted,
            )
            Spacer(Modifier.width(9.dp))
            Text(text, Modifier.weight(1f), fontSize = 13.sp, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (showProgress) {
                AnimatedThinkingDots(dotColor = muted)
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                if (expanded) "Collapse activity" else "Expand activity",
                Modifier.size(18.dp),
                tint = muted,
            )
        }
    }
    Surface(
        modifier = rowModifier.padding(vertical = 3.dp),
        shape = outlineShape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) { content() }
}

@Composable
private fun activityIcon(item: ActivityItem?): ImageVector {
    if (item == null) return ImageVector.vectorResource(R.drawable.ic_think)
    val task = item.title
        .removePrefix("Running ")
        .removeSuffix(" completed")
        .trim()
    return when {
        item.isCommand || task.equals("Bash", ignoreCase = true) -> Icons.Default.Terminal
        task.equals("Think", ignoreCase = true) -> ImageVector.vectorResource(R.drawable.ic_think)
        task.equals("Write", ignoreCase = true) ||
            task.equals("Edit", ignoreCase = true) ||
            task.equals("NotebookEdit", ignoreCase = true) -> ImageVector.vectorResource(R.drawable.ic_write)
        task.equals("Read", ignoreCase = true) -> ImageVector.vectorResource(R.drawable.ic_read)
        task.equals("WebSearch", ignoreCase = true) -> ImageVector.vectorResource(R.drawable.ic_search_web)
        task.equals("Glob", ignoreCase = true) ||
            task.equals("Grep", ignoreCase = true) -> Icons.Default.Search
        task.contains("file", ignoreCase = true) -> Icons.Default.Description
        else -> ImageVector.vectorResource(R.drawable.ic_think)
    }
}

@Composable
private fun ActivityExpandedDetail(item: ActivityItem?, detail: String) {
    if (item?.isCommand == true) {
        Text(
            detail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, end = 8.dp, bottom = 8.dp),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    } else {
        MarkdownText(
            markdown = detail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, end = 8.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun compactActivityText(item: ActivityItem): String = "${activityName(item)} · ${activityDetail(item).replace(Regex("\\s+"), " ").take(105)}"

private fun activityDetail(item: ActivityItem): String {
    if (item.title == "Think" && item.detail.contains("reasoning tokens processed", true)) {
        return "Reviewed the request and planned the next action"
    }
    return item.detail.ifBlank { item.title }
}

private fun activityHeadline(items: List<ActivityItem>, seconds: Long, thinking: Boolean): String {
    val latest = items.lastOrNull()
    if (latest == null) return "Think · Analyzing the request · ${formatDuration(seconds)}"
    if (thinking && latest.title == "Think") return "Think · ${latest.detail} · ${formatDuration(seconds)}"
    val detail = latest.detail.replace(Regex("\\s+"), " ").trim().ifBlank { latest.title }
    return "${activityName(latest)} · ${detail.take(100)} · ${formatDuration(seconds)}"
}

private fun activityName(item: ActivityItem): String = item.title
    .removePrefix("Running ")
    .removeSuffix(" completed")
    .replaceFirstChar { it.uppercase() }

private fun completedProcessSummary(
    processItems: List<ActivityItem>,
    startedAtMillis: Long?,
    finishedAtMillis: Long?,
): String {
    val stopped = processItems.lastOrNull()?.title?.startsWith("Task stopped") == true
    val outcome = if (stopped) "Task stopped" else "Task completed"
    val steps = "${processItems.size} step${if (processItems.size == 1) "" else "s"}"
    val duration = startedAtMillis?.let { start ->
        val end = finishedAtMillis ?: System.currentTimeMillis()
        formatDuration(((end - start) / 1000L).coerceAtLeast(0))
    }
    return if (duration != null) "$outcome · $duration · $steps" else "$outcome · $steps"
}

@Composable
private fun rememberLiveElapsedSeconds(startedAtMillis: Long): Int {
    var seconds by remember(startedAtMillis) {
        mutableIntStateOf(((System.currentTimeMillis() - startedAtMillis) / 1000L).toInt().coerceAtLeast(0))
    }
    LaunchedEffect(startedAtMillis) {
        while (true) {
            delay(1_000)
            seconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).toInt().coerceAtLeast(0)
        }
    }
    return seconds
}

private fun formatDuration(totalSeconds: Long): String = when {
    totalSeconds >= 3_600 -> "${totalSeconds / 3_600}h ${(totalSeconds % 3_600) / 60}m"
    totalSeconds >= 60 -> "${totalSeconds / 60}m ${totalSeconds % 60}s"
    else -> "${totalSeconds}s"
}

private val LocalAttachmentRoot = compositionLocalOf<java.io.File?> { null }

private fun attachmentRootFor(context: Context, project: Project?): java.io.File? {
    if (project == null) return null
    val base = java.io.File(context.filesDir, "workspaces/${project.id}")
    return if (project.rootPath.isBlank()) base else java.io.File(base, project.rootPath)
}

private fun isImageAttachment(attachment: ChatAttachment): Boolean {
    if (attachment.mimeType.startsWith("image/")) return true
    val ext = attachment.displayName.substringAfterLast('.', "").lowercase()
    if (ext in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "heic", "heif", "svg")) return true
    val pathExt = attachment.relativePath.substringAfterLast('.', "").lowercase()
    return pathExt in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "heic", "heif", "svg")
}

private fun decodeAttachmentBitmap(root: java.io.File?, attachment: ChatAttachment, context: Context? = null, targetDim: Int = 2048): ImageBitmap? {
    return runCatching {
        val candidates = mutableListOf<java.io.File>()
        if (root != null) {
            candidates.add(java.io.File(root, attachment.relativePath))
            root.parentFile?.let { candidates.add(java.io.File(it, attachment.relativePath)) }
        }
        if (context != null) {
            candidates.add(java.io.File(context.filesDir, "workspaces/${attachment.relativePath}"))
            candidates.add(java.io.File(context.filesDir, attachment.relativePath))
        }
        candidates.add(java.io.File(attachment.relativePath))

        val file = candidates.firstOrNull { it.isFile && it.canRead() } ?: return@runCatching null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
        if (maxDim > 0) {
            while (maxDim / (sample * 2) >= targetDim) sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
    }.getOrNull()
}

@Composable
private fun AttachmentPreview(
    attachment: ChatAttachment,
    onOpen: (() -> Unit)?,
    onRemove: (() -> Unit)?,
    imageSize: Dp = 72.dp,
) {
    val shape = RoundedCornerShape(12.dp)
    val cardBorder = BorderStroke(1.dp, Color(0xFFE2E8F0))
    if (isImageAttachment(attachment)) {
        val root = LocalAttachmentRoot.current
        val context = LocalContext.current
        val bitmap by produceState<ImageBitmap?>(initialValue = null, attachment.relativePath, root) {
            value = withContext(Dispatchers.IO) { decodeAttachmentBitmap(root, attachment, context, 300) }
        }
        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(shape)
                .background(Color.White)
                .border(cardBorder, shape)
                .then(if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        ) {
            val image = bitmap
            if (image != null) {
                androidx.compose.foundation.Image(
                    bitmap = image,
                    contentDescription = attachment.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp,
                    )
                }
            }
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(Color(0xCC000000), CircleShape)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attachment",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(shape)
                .background(Color.White)
                .border(cardBorder, shape)
                .then(if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_custom_file),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Color.Black,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = attachment.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(Color(0xCC000000), CircleShape)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attachment",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceDock(onTerminal: () -> Unit, onFiles: () -> Unit, onPreview: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockButton(Modifier.size(48.dp), onTerminal) {
            Icon(Icons.Outlined.TerminalOutlined, "Terminal", Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        DockButton(Modifier.weight(1f).height(48.dp), onFiles) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_custom_folder), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                Text("Files", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        DockButton(Modifier.size(48.dp), onPreview) {
            Icon(Icons.Outlined.LanguageOutlined, "Preview", Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DockButton(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    onRunInTerminal: (String) -> Unit,
    onOpenAttachment: (ChatAttachment) -> Unit,
    onEdit: ((ChatMessage) -> Unit)? = null,
    canEdit: Boolean = true,
) {
    if (message.fromUser) {
        var menuExpanded by remember { mutableStateOf(false) }
        val clipboard = LocalClipboardManager.current
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (message.attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    message.attachments.forEach { attachment ->
                        AttachmentPreview(
                            attachment = attachment,
                            onOpen = { onOpenAttachment(attachment) },
                            onRemove = null,
                            imageSize = 80.dp,
                        )
                    }
                }
            }
            Box {
                if (message.text.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { menuExpanded = true },
                            ),
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color.White,
                ) {
                    if (message.text.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text("Copy message") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_custom_copy),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                clipboard.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = if (canEdit) Color.Black else Color.Gray) },
                            enabled = canEdit,
                            leadingIcon = {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_write),
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (canEdit) Color.Black else Color.Gray,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                if (canEdit) onEdit(message)
                            },
                        )
                    }
                }
            }
            if (onEdit != null || message.text.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    if (onEdit != null) {
                        IconButton(
                            onClick = { if (canEdit) onEdit(message) },
                            enabled = canEdit,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_write),
                                contentDescription = "Edit message",
                                modifier = Modifier.size(14.dp),
                                tint = if (canEdit) Color.Black else Color.Gray,
                            )
                        }
                    }
                    if (message.text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_custom_copy),
                                contentDescription = "Copy message",
                                modifier = Modifier.size(15.dp),
                                tint = Color.Black,
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxWidth()) {
            SelectionContainer {
                MarkdownText(
                    markdown = stripEmojis(message.text),
                    modifier = Modifier.padding(horizontal = 2.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    onRunCode = onRunInTerminal,
                )
            }
            if (message.workedMillis > 0L) {
                Text(
                    text = "Worked for ${formatDuration((message.workedMillis / 1_000L).coerceAtLeast(1L))}",
                    modifier = Modifier.padding(start = 2.dp, top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
            if (message.attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    message.attachments.forEach { attachment ->
                        AttachmentPreview(
                            attachment = attachment,
                            onOpen = { onOpenAttachment(attachment) },
                            onRemove = null,
                            imageSize = 80.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentChip(
    attachment: ChatAttachment,
    onOpen: (() -> Unit)?,
    onRemove: (() -> Unit)?,
) {
    val icon = when {
        attachment.mimeType.startsWith("image/") -> Icons.Default.Image
        else -> Icons.Outlined.DescriptionOutlined
    }
    Surface(
        modifier = Modifier.then(if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(Modifier.padding(start = 9.dp, end = if (onRemove == null) 10.dp else 3.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(7.dp))
            Column(Modifier.widthIn(max = 180.dp)) {
                Text(attachment.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatFileSize(attachment.sizeBytes), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, "Remove attachment", Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun ApprovalCard(request: ToolRequest, onApproval: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp)); Text("Review this action", fontWeight = FontWeight.Bold)
            }
            Text(request.explanation)
            request.affectedPaths.forEach { Text("• $it", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onApproval(false) }, Modifier.weight(1f)) { Text("Reject") }
                Button(onClick = { onApproval(true) }, Modifier.weight(1f)) { Text("Allow once") }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    request: QuestionRequest,
    onAnswer: (String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var customText by rememberSaveable(request.questionId) { mutableStateOf("") }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "Clarifying question",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                request.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (request.options.isNotEmpty()) {
                val labels = listOf("A", "B", "C", "D")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    request.options.take(4).forEachIndexed { index, option ->
                        val prefix = labels.getOrElse(index) { "${index + 1}" }
                        OutlinedButton(
                            onClick = { onAnswer(option) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                "$prefix: $option",
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            if (request.allowFreeText) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        placeholder = { Text("Type custom answer...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Button(
                        onClick = {
                            if (customText.isNotBlank()) {
                                onAnswer(customText.trim())
                                customText = ""
                            }
                        },
                        enabled = customText.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Send", fontSize = 12.sp)
                    }
                }
            }

            if (request.allowSkip) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onSkip) {
                        Text(
                            "Skip (proceed with best judgment)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}


private fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "%.1f MB".format(bytes / 1_048_576.0)
}

@Composable
private fun ChangesTab(
    changes: List<ChangeItem>,
    onUndo: () -> Unit,
    onKeep: () -> Unit,
    onUndoFile: (String) -> Unit,
    onKeepFile: (String) -> Unit,
) {
    var expandedPath by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text("Changes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Review everything the AI changed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (changes.isEmpty()) item { EmptyState(Icons.Default.Code, "No changes yet", "Ask Rlaude Harness to update your project.") }
        items(changes, key = { it.path }) { change ->
            val expanded = expandedPath == change.path
            Card(Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().clickable { expandedPath = if (expanded) null else change.path }.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(change.path, fontWeight = FontWeight.Medium, maxLines = 1)
                            Text(
                                if (expanded) "Hide line-by-line diff" else "Tap to review diff",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text("+${change.additions}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(7.dp))
                        Text("-${change.deletions}", color = MaterialTheme.colorScheme.error)
                    }
                    if (expanded) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(
                            Modifier.fillMaxWidth().background(Color(0xFF0E0E0E)).horizontalScroll(rememberScrollState()),
                        ) {
                            change.diffLines.forEach { line -> DiffLineRow(line) }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    expandedPath = null
                                    onUndoFile(change.path)
                                },
                                modifier = Modifier.weight(1f),
                            ) { Text("Undo file") }
                            Button(
                                onClick = {
                                    expandedPath = null
                                    onKeepFile(change.path)
                                },
                                modifier = Modifier.weight(1f),
                            ) { Text("Keep file") }
                        }
                    }
                }
            }
        }
        if (changes.isNotEmpty()) item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = onUndo, Modifier.weight(1f)) { Text("Undo task") }
                Button(onClick = onKeep, Modifier.weight(1f)) { Text("Keep changes") }
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: DiffLine) {
    val marker = when (line.type) {
        DiffLineType.ADDITION -> "+"
        DiffLineType.DELETION -> "-"
        DiffLineType.CONTEXT -> " "
        DiffLineType.INFO -> "·"
    }
    val background = when (line.type) {
        DiffLineType.ADDITION -> Color(0xFF272727)
        DiffLineType.DELETION -> Color(0xFF262626)
        else -> Color.Transparent
    }
    val foreground = when (line.type) {
        DiffLineType.ADDITION -> Color(0xFFC3C3C3)
        DiffLineType.DELETION -> Color(0xFFBFBFBF)
        DiffLineType.INFO -> Color(0xFF929292)
        DiffLineType.CONTEXT -> Color(0xFFDADADA)
    }
    val oldNumber = line.oldLine?.toString().orEmpty().padStart(4)
    val newNumber = line.newLine?.toString().orEmpty().padStart(4)
    Text(
        text = "$oldNumber $newNumber  $marker ${line.text}",
        modifier = Modifier.fillMaxWidth().background(background).padding(horizontal = 8.dp, vertical = 2.dp),
        color = foreground,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        softWrap = false,
    )
}

@Composable
private fun PreviewTab(ready: Boolean, url: String?) {
    var address by rememberSaveable(url) { mutableStateOf(if (ready) url.orEmpty() else "") }
    var activeUrl by rememberSaveable(url) { mutableStateOf(if (ready) url else null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val navigate = {
        val normalized = normalizePreviewUrl(address)
        if (normalized == null) {
            addressError = "Use a local URL such as localhost:3000"
        } else {
            addressError = null
            address = normalized
            activeUrl = normalized
        }
    }

    LaunchedEffect(ready, url) {
        if (ready && !url.isNullOrBlank() && activeUrl == null) {
            normalizePreviewUrl(url)?.let {
                address = it
                activeUrl = it
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = 1.dp,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Preview URL") },
                        placeholder = { Text("localhost:3000") },
                        leadingIcon = {
                            Box(
                                Modifier.size(8.dp).background(
                                    if (activeUrl != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    CircleShape,
                                ),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = navigate) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open URL")
                            }
                        },
                        isError = addressError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go,
                        ),
                        keyboardActions = KeyboardActions(onGo = { navigate() }),
                    )
                    IconButton(
                        onClick = { webView?.reload() ?: navigate() },
                        enabled = address.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh preview")
                    }
                }
                if (addressError != null) {
                    Text(
                        addressError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 3.dp),
                    )
                } else if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 5.dp))
                }
            }
        }
        val targetUrl = activeUrl
        if (targetUrl == null) {
            EmptyState(Icons.Default.PlayArrow, "Preview not running", "Enter a localhost URL above, or start a local web server in the project Terminal.")
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loading = newProgress < 100
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val target = request?.url ?: return true
                                if (!target.isLoopbackPreviewUrl()) {
                                    addressError = "External navigation is blocked in project preview"
                                    return true
                                }
                                address = target.toString()
                                return false
                            }

                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val target = request?.url ?: return blockedPreviewResponse()
                                return if (target.isLoopbackPreviewUrl()) null else blockedPreviewResponse()
                            }
                        }
                        loadUrl(targetUrl)
                    }
                },
                update = { current ->
                    webView = current
                    if (current.url != targetUrl) current.loadUrl(targetUrl)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun normalizePreviewUrl(input: String): String? {
    val raw = input.trim()
    if (raw.isBlank()) return null
    val withScheme = if ("://" in raw) raw else "http://$raw"
    val parsed = runCatching { Uri.parse(withScheme) }.getOrNull() ?: return null
    if (!parsed.isLoopbackPreviewUrl() || parsed.host.isNullOrBlank()) return null
    return if (parsed.host == "0.0.0.0") {
        parsed.buildUpon().encodedAuthority(
            buildString {
                append("127.0.0.1")
                if (parsed.port >= 0) append(":${parsed.port}")
            },
        ).build().toString()
    } else {
        parsed.toString()
    }
}

private fun Uri.isLoopbackPreviewUrl(): Boolean =
    scheme in setOf("data", "blob", "about") ||
        (scheme in setOf("http", "https", "ws", "wss") && host in setOf("127.0.0.1", "localhost", "0.0.0.0"))

private fun blockedPreviewResponse(): WebResourceResponse =
    WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)))

@Composable
private fun EmptyState(icon: ImageVector, title: String, body: String) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    val size = if (compact) 32.dp else 50.dp
    val iconSize = if (compact) 17.dp else 24.dp
    val cornerRadius = if (compact) 9.dp else 14.dp
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(size)
            .background(
                color = primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(cornerRadius),
            )
            .border(
                width = 1.dp,
                color = primary.copy(alpha = 0.32f),
                shape = RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_rabbit),
            contentDescription = "Rlaude Harness",
            modifier = Modifier.size(iconSize),
            tint = primary,
        )
    }
}
