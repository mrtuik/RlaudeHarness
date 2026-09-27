package com.jarves.mh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.ui.MainViewModel
import com.jarves.mh.ui.PocketDevApp
import com.jarves.mh.ui.theme.PocketTheme
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashLogger()
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            PocketTheme(themeMode = state.themeMode) {
                PocketDevApp(vm)
            }
        }
    }

    /**
     * Writes any uncaught exception (the thing that silently kicks the user back to the
     * previous screen or kills the app) to a plain-text log under external app storage,
     * then hands off to the previous default handler so normal crash behavior is unchanged.
     * Pull the file after a crash — no adb/logcat needed:
     *   Android/data/com.jarves.mh/files/crash_log.txt (any file manager can see it, or
     *   "Files > On this phone > Android > data > com.jarves.mh > files" in most file apps).
     */
    private fun installCrashLogger() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val logFile = File(getExternalFilesDir(null), "crash_log.txt")
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val stackTrace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
                logFile.appendText("\n===== $timestamp — thread ${thread.name} =====\n$stackTrace")
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
