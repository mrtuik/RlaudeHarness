package com.jarves.mh.ui

import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.jarves.mh.R
import com.jarves.mh.model.WorkspaceEntry
import com.jarves.mh.runtime.PreviewEntryResolver
import com.jarves.mh.runtime.StaticPreviewServer
import java.io.File

/** Lets deep chat UI (file cards' three-dot menu) open the Preview page for a file path. */
val LocalOpenPreview = staticCompositionLocalOf<(String) -> Unit> { {} }

/** Previewable files from the Files page, most recently changed first. */
private fun previewCandidates(files: List<WorkspaceEntry>): List<WorkspaceEntry> =
    files.filter { !it.isDirectory && PreviewEntryResolver.isPreviewable(it.name) }
        .sortedByDescending { it.lastModifiedMillis }

/** Popup listing every previewable file (latest first). Tap one to preview it. */
@Composable
fun PreviewPickerDialog(
    files: List<WorkspaceEntry>,
    currentPath: String?,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val items = remember(files) { previewCandidates(files) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(1.dp),
            color = Color.White,
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
                Text(
                    "Preview with tuik",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(14.dp))
                if (items.isEmpty()) {
                    Text(
                        "No previewable files yet (.html)",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                } else {
                    LazyColumn(Modifier.fillMaxWidth().height((minOf(items.size, 7) * 76).dp)) {
                        items(items, key = { it.path }) { entry ->
                            val selected = entry.path == currentPath
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(entry.path) }
                                    .background(if (selected) Color(0xFFF3F4F6) else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_custom_file),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.name, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        entry.path,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

/**
 * iPhone/Safari-style preview page: file name on top, the running page in the middle,
 * bottom bar = [change file] [url pill] [three dots -> Refresh / Close].
 */
@Composable
fun PreviewScreen(
    path: String,
    files: List<WorkspaceEntry>,
    fileRoot: File?,
    onPathChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    BackHandler(onBack = onClose)

    val resolved = remember(path, fileRoot) { resolveWorkspaceFile(fileRoot, path, context) }
    val rootDir = remember(resolved, path) { resolved?.let { PreviewEntryResolver.archiveRootDir(it, path) } }
    val server = remember(rootDir) { rootDir?.let { StaticPreviewServer(it).start() } }
    DisposableEffect(server) { onDispose { server?.close() } }

    var webView by remember { mutableStateOf<WebView?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showPicker by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val url = server?.urlFor(path.trimStart('/'))

    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Text(
            path.substringAfterLast('/'),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (url == null) {
                Text(
                    "Couldn't read this file",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webView = this
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
                                    return !target.isLoopbackPreviewUrl()
                                }
                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    val target = request?.url ?: return blockedPreviewResponse()
                                    return if (target.isLoopbackPreviewUrl()) null else blockedPreviewResponse()
                                }
                            }
                            loadUrl(url)
                        }
                    },
                    update = { current ->
                        webView = current
                        if (current.url != url) current.loadUrl(url)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF2F2F7)).clickable { showPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_tuikchange), "Change file", Modifier.size(22.dp), tint = Color.Black)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFF2F2F7))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_tuikurl), null, Modifier.size(16.dp), tint = Color(0xFF6B7280))
                Spacer(Modifier.width(8.dp))
                Text(
                    url ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF2F2F7)).clickable { menuOpen = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MoreVert, "Preview options", Modifier.size(22.dp), tint = Color.Black)
                }
                if (menuOpen) {
                    // Drop-UP: the menu opens above the button.
                    Popup(
                        popupPositionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize,
                            ) = IntOffset(
                                x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0),
                                y = (anchorBounds.top - popupContentSize.height - 8).coerceAtLeast(0),
                            )
                        },
                        onDismissRequest = { menuOpen = false },
                        properties = PopupProperties(focusable = true),
                    ) {
                        Surface(shape = RoundedCornerShape(2.dp), color = Color.White, shadowElevation = 8.dp) {
                            Column(Modifier.width(150.dp)) {
                                Text(
                                    "Refresh",
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { menuOpen = false; webView?.reload() }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                )
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Text(
                                    "Close",
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { menuOpen = false; onClose() }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        PreviewPickerDialog(
            files = files,
            currentPath = path,
            onPick = { showPicker = false; onPathChange(it) },
            onDismiss = { showPicker = false },
        )
    }
}
