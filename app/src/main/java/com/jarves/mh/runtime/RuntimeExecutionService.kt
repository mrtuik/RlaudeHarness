package com.jarves.mh.runtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jarves.mh.MainActivity
import com.jarves.mh.R

internal object RuntimeTaskController {
    @Volatile var stopAction: (() -> Unit)? = null

    fun requestStop() {
        stopAction?.invoke()
    }
}

class RuntimeExecutionService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var projectName: String = "your project"
    private var notificationTitle: String = "Coding agent is working"
    private var canStop: Boolean = true
    private var taskRunning: Boolean = false
    private var isForeground: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // MUST be the very first thing this method does, before touching intent extras or
        // anything else: the OS starts its "did you call startForeground() in time?" watchdog
        // the instant startForegroundService() was invoked on the caller side, so every extra
        // line of work here before promoting to foreground eats into that budget.
        // justPromoted tracks whether *this* onStartCommand call is the one that performed the
        // promotion, so a task that finishes almost instantly (ACTION_COMPLETE/FAILED/CANCELLED
        // arriving in the very same delivery, or a fast-failing task queued right behind START)
        // doesn't call stopForeground()/stopSelf() before the platform has fully registered the
        // service as foreground — a known source of a *spurious*
        // ForegroundServiceDidNotStartInTimeException even though startForeground() itself
        // returned successfully.
        var justPromoted = false
        if (!isForeground) {
            try {
                startForeground(
                    RUNNING_NOTIFICATION_ID,
                    runningNotification("$notificationTitle in $projectName", includeStop = canStop),
                )
                isForeground = true
                justPromoted = true
            } catch (t: Throwable) {
                // We could not become a real foreground service. Swallowing this silently is
                // exactly what previously let the process limp along and then get killed by the
                // watchdog anyway (with no clue why). Log it and stop cleanly instead — there is
                // nothing useful we can do without foreground status.
                Log.e(TAG, "startForeground() failed; stopping service", t)
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }

        intent?.getStringExtra(EXTRA_PROJECT_NAME)?.takeIf(String::isNotBlank)?.let { projectName = it }
        intent?.getStringExtra(EXTRA_TITLE)?.takeIf(String::isNotBlank)?.let { notificationTitle = it }
        if (intent?.hasExtra(EXTRA_CAN_STOP) == true) canStop = intent.getBooleanExtra(EXTRA_CAN_STOP, true)

        // Now that project/title extras are known, refresh the notification we posted above with
        // the real text (it may have been posted with stale/default values a moment ago).
        if (justPromoted) {
            getSystemService(NotificationManager::class.java).notify(
                RUNNING_NOTIFICATION_ID,
                runningNotification(
                    intent?.getStringExtra(EXTRA_DETAIL)?.takeIf { it.isNotBlank() }
                        ?: "$notificationTitle in $projectName",
                    includeStop = canStop,
                ),
            )
        }

        when (intent?.action ?: ACTION_START) {
            ACTION_STOP -> {
                RuntimeTaskController.requestStop()
                getSystemService(NotificationManager::class.java).notify(
                    RUNNING_NOTIFICATION_ID,
                    runningNotification("Stopping safely…", includeStop = false),
                )
            }
            ACTION_PROGRESS -> {
                // Live step updates only matter while a task is actually running.
                if (!taskRunning) return START_NOT_STICKY
                val detail = intent?.getStringExtra(EXTRA_DETAIL)?.takeIf { it.isNotBlank() }
                    ?: "$notificationTitle in $projectName"
                getSystemService(NotificationManager::class.java).notify(
                    RUNNING_NOTIFICATION_ID,
                    runningNotification(detail, includeStop = canStop),
                )
            }
            ACTION_COMPLETE -> finishTask(
                title = "Task completed",
                detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "Finished working in $projectName.",
                failed = false,
                justPromoted = justPromoted,
            )
            ACTION_FAILED -> finishTask(
                title = "Task needs attention",
                detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "Could not finish the task in $projectName.",
                failed = true,
                justPromoted = justPromoted,
            )
            ACTION_CANCELLED -> stopRuntime(justPromoted)
            else -> {
                taskRunning = true
                acquireWakeLock()
            }
        }
        return START_NOT_STICKY
    }

    private fun runningNotification(detail: String, includeStop: Boolean): android.app.Notification {
        val builder = NotificationCompat.Builder(this, RUNNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(detail)
            .setContentIntent(openAppIntent())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (includeStop) {
            val stopIntent = PendingIntent.getService(
                this,
                2,
                Intent(this, RuntimeExecutionService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            builder.addAction(0, "Stop task", stopIntent)
        }
        return builder.build()
    }

    private fun finishTask(title: String, detail: String, failed: Boolean, justPromoted: Boolean) {
        taskRunning = false
        val notification = NotificationCompat.Builder(this, RESULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .setCategory(if (failed) NotificationCompat.CATEGORY_ERROR else NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        stopRuntimeAndPost(justPromoted) {
            getSystemService(NotificationManager::class.java).notify(RESULT_NOTIFICATION_ID, notification)
        }
    }

    private fun stopRuntime(justPromoted: Boolean) {
        taskRunning = false
        stopRuntimeAndPost(justPromoted, onStopped = null)
    }

    /**
     * Tears the service down (stopForeground + stopSelf), optionally running [onStopped] right
     * before stopSelf(). If [justPromoted] is true — meaning this very onStartCommand delivery is
     * the one that just called startForeground() — the actual teardown is pushed a beat onto the
     * main looper instead of happening inline. On several OEM builds, calling stopForeground()/
     * stopSelf() in the same event-loop pass as startForeground() races the platform's internal
     * bookkeeping and can still surface as ForegroundServiceDidNotStartInTimeException, even
     * though startForeground() itself succeeded. A short post-to-main gives the system time to
     * finish registering the service as foreground first.
     */
    private fun stopRuntimeAndPost(justPromoted: Boolean, onStopped: (() -> Unit)?) {
        val teardown = {
            releaseWakeLock()
            onStopped?.invoke()
            runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
            isForeground = false
            stopSelf()
        }
        if (justPromoted) {
            mainHandler.postDelayed(teardown, FAST_FINISH_GUARD_MS)
        } else {
            teardown()
        }
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        1,
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.jarves.mh:active-coding-task")
            .apply { acquire(MAX_WAKE_LOCK_MS) }
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.jarves.mh.START_RUNTIME"
        const val ACTION_STOP = "com.jarves.mh.STOP_RUNTIME"
        const val ACTION_PROGRESS = "com.jarves.mh.PROGRESS_RUNTIME"
        const val ACTION_COMPLETE = "com.jarves.mh.COMPLETE_RUNTIME"
        const val ACTION_FAILED = "com.jarves.mh.FAIL_RUNTIME"
        const val ACTION_CANCELLED = "com.jarves.mh.CANCEL_RUNTIME"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_DETAIL = "detail"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CAN_STOP = "can_stop"

        private const val RUNNING_CHANNEL_ID = "runtime"
        private const val RESULT_CHANNEL_ID = "task-results"
        private const val RUNNING_NOTIFICATION_ID = 41
        private const val RESULT_NOTIFICATION_ID = 42
        private const val MAX_WAKE_LOCK_MS = 90 * 60 * 1_000L
        private const val FAST_FINISH_GUARD_MS = 300L
        private const val TAG = "RuntimeExecutionSvc"

        fun ensureNotificationChannels(context: android.content.Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(RUNNING_CHANNEL_ID, "Running coding tasks", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Shows progress while coding agent is working in the background"
                },
            )
            manager.createNotificationChannel(
                NotificationChannel(RESULT_CHANNEL_ID, "Task results", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Notifies you when a coding task finishes or needs attention"
                },
            )
        }
    }
}
