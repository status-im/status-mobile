package im.status.ethereum.pushnotifications

import android.content.Context
import android.content.Intent
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import im.status.ethereum.module.R

class ForegroundService : Service() {
    companion object {
        private const val CHANNEL_ID = "status-service"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(i: Intent?, flags: Int, startId: Int): Int {
        // NOTE: recent versions of Android require the service to display
        // a sticky notification to inform the user that the service is running
        val context = applicationContext
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.resources.getString(R.string.status_service),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Initialize intent and notification text based on Android version
        val notificationIntent = createNotificationIntent(context)
        val notificationText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.resources.getString(R.string.tap_to_hide_notification)
        } else {
            context.resources.getString(R.string.keep_status_running)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(PushNotificationHelper.ACTION_TAP_STOP)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify_status)
            .setContentTitle(context.resources.getString(R.string.background_service_opened))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setNumber(0)
            .addAction(
                R.drawable.ic_stat_notify_status,
                context.resources.getString(R.string.stop),
                stopPendingIntent
            )
            .build()

        // the id of the foreground notification MUST NOT be 0
        if (Build.VERSION.SDK_INT >= 33) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        return START_STICKY
    }

    private fun createNotificationIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create intent that takes the user to the notification channel settings
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            }
        } else {
            // For older versions of android, intent takes the user to the Status app
            val packageName = context.packageName
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            val className = launchIntent?.component?.className

            try {
                val intentClass = Class.forName(className!!)
                Intent(context, intentClass).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                // Return an empty intent as fallback
                Intent()
            }
        }
    }
}
