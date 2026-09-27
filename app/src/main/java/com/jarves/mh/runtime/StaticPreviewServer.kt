package com.jarves.mh.runtime

import com.jarves.mh.model.WorkspaceEntry
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight on-demand static file server for the "Preview" action on an
 * imported zip's HTML/TSX/JSX files (see Part 4 of the Files-workflow
 * redesign). Rooted at the archive's extracted folder on disk and reachable
 * only from this device (127.0.0.1), same loopback-only pattern as
 * [LocalFormatGateway] — a fresh instance per Preview session, closed when
 * the preview sheet is dismissed.
 *
 * This is intentionally separate from the existing PreviewTab/detectPreviewUrl
 * flow (MainViewModel.kt), which targets a running `npm run dev`-style dev
 * server; this class never touches that flow.
 */
internal class StaticPreviewServer(private val root: File) : AutoCloseable {
    private val running = AtomicBoolean(true)
    private val server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
    val port: Int get() = server.localPort
    val baseUrl: String = "http://127.0.0.1:${server.localPort}"

    fun start(): StaticPreviewServer = apply {
        Thread({ acceptLoop() }, "mh-preview-server").apply { isDaemon = true; start() }
    }

    /** Public URL for [relativePath] (e.g. "index.html" or "src/App.tsx"). */
    fun urlFor(relativePath: String): String = "$baseUrl/${relativePath.trimStart('/')}"

    private fun acceptLoop() {
        while (running.get()) {
            runCatching { server.accept() }.getOrNull()?.let { socket ->
                Thread({ socket.use(::handle) }, "mh-preview-request").apply { isDaemon = true; start() }
            }
        }
    }

    private fun handle(socket: Socket) {
        val input = BufferedInputStream(socket.getInputStream())
        val requestLine = readLine(input) ?: return
        // Drain headers; this server only ever serves local static GETs.
        while (true) {
            val line = readLine(input) ?: return
            if (line.isEmpty()) break
        }
        val output = BufferedOutputStream(socket.getOutputStream())
        val rawPath = requestLine.split(' ').getOrNull(1).orEmpty().substringBefore('?')
        val relative = java.net.URLDecoder.decode(rawPath, "UTF-8").trimStart('/')
        if (relative.contains("..")) {
            writeText(output, 403, "text/plain", "Forbidden")
            return
        }
        val target = File(root, relative).canonicalFile
        val rootPath = root.canonicalFile.toPath()
        if (!target.toPath().startsWith(rootPath) || !target.isFile) {
            writeText(output, 404, "text/plain", "Not found: $relative")
            return
        }
        runCatching {
            val bytes = target.readBytes()
            val headers = "HTTP/1.1 200 OK\r\nContent-Type: ${mimeTypeFor(target.name)}\r\n" +
                "Content-Length: ${bytes.size}\r\nCache-Control: no-cache\r\nConnection: close\r\n\r\n"
            output.write(headers.toByteArray())
            output.write(bytes)
            output.flush()
        }
    }

    private fun writeText(output: BufferedOutputStream, code: Int, contentType: String, body: String) {
        val bytes = body.toByteArray()
        val reason = if (code in 200..299) "OK" else "Error"
        output.write(
            "HTTP/1.1 $code $reason\r\nContent-Type: $contentType\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray(),
        )
        output.write(bytes)
        output.flush()
    }

    private fun readLine(input: BufferedInputStream): String? {
        val bytes = ArrayList<Byte>()
        while (true) {
            val value = input.read()
            if (value < 0) return if (bytes.isEmpty()) null else bytes.toByteArray().decodeToString()
            if (value == '\n'.code) return bytes.toByteArray().decodeToString().trimEnd('\r')
            bytes += value.toByte()
        }
    }

    private fun mimeTypeFor(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "html", "htm" -> "text/html; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "js", "mjs", "jsx" -> "text/javascript; charset=utf-8"
        "ts", "tsx" -> "text/plain; charset=utf-8"
        "json" -> "application/json; charset=utf-8"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "ico" -> "image/x-icon"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        "ttf" -> "font/ttf"
        "map" -> "application/json; charset=utf-8"
        else -> "application/octet-stream"
    }

    override fun close() {
        running.set(false)
        runCatching { server.close() }
    }
}

/** Resolves which files inside an imported archive are eligible for Preview and which one is the entry point. */
internal object PreviewEntryResolver {
    private val ENTRY_HTML_NAMES = listOf("index.html", "index.htm")
    private val ENTRY_JS_NAMES = listOf(
        "index.tsx", "index.jsx", "app.tsx", "app.jsx", "main.tsx", "main.jsx",
    )

    fun isPreviewable(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext == "html" || ext == "htm" || ext == "tsx" || ext == "jsx"
    }

    /** All previewable files in the archive, for the file-switcher sheet. */
    fun previewableFiles(files: List<WorkspaceEntry>): List<WorkspaceEntry> =
        files.filter { !it.isDirectory && isPreviewable(it.name) }
            .sortedWith(compareBy({ it.path.count { c -> c == '/' } }, { it.path.lowercase() }))

    /**
     * Root-then-subfolder entry resolution (Part 4): an entry file at the
     * archive root wins; otherwise the first one found exactly one level
     * down. Falls back to the first previewable file found anywhere.
     */
    fun findEntryFile(files: List<WorkspaceEntry>): WorkspaceEntry? {
        val previewable = previewableFiles(files)
        val atRoot = previewable.filter { !it.path.contains('/') }
        findPreferredEntry(atRoot)?.let { return it }

        val oneLevelDown = previewable.filter { it.path.count { c -> c == '/' } == 1 }
        findPreferredEntry(oneLevelDown)?.let { return it }

        return previewable.firstOrNull()
    }

    private fun findPreferredEntry(candidates: List<WorkspaceEntry>): WorkspaceEntry? {
        if (candidates.isEmpty()) return null
        ENTRY_HTML_NAMES.forEach { wanted -> candidates.firstOrNull { it.name.equals(wanted, ignoreCase = true) }?.let { return it } }
        candidates.firstOrNull { it.name.substringAfterLast('.', "").lowercase().let { ext -> ext == "html" || ext == "htm" } }?.let { return it }
        ENTRY_JS_NAMES.forEach { wanted -> candidates.firstOrNull { it.name.equals(wanted, ignoreCase = true) }?.let { return it } }
        return candidates.firstOrNull()
    }

    /**
     * The archive's extracted root directory on disk, derived from one
     * resolved file inside it plus its known path relative to the archive.
     * (Ascend one directory per '/' in [relativePath].)
     */
    fun archiveRootDir(resolvedFile: File, relativePath: String): File {
        var dir = resolvedFile.parentFile ?: resolvedFile
        val ascendCount = relativePath.trim('/').count { it == '/' }
        repeat(ascendCount) { dir = dir.parentFile ?: dir }
        return dir
    }
}
