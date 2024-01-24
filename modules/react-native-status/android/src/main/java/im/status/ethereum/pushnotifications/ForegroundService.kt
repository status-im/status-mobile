package im.status.ethereum.pushnotifications

import android.content.Context
import android.content.Intent
import android.app.Service
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
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(i: Intent?, flags: Int, startId: Int): Int {
        // NOTE: recent versions of Android require the service to display
        // a sticky notification to inform the user that the service is running
        val context: Context = getApplicationContext()
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        var intent: Intent? = null
        var notificationContentText: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID,
                    context.getResources().getString(R.string.status_service),
                    NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)

            // Create intent that takes the user to the notification channel settings so they can hide it
            intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            notificationContentText = context.getResources().getString(R.string.tap_to_hide_notification)
        } else {
            // For older versions of android intent takes the user to the Status app
            val intentClass: java.lang.Class<*>
            val packageName: String = context.getPackageName()
            val launchIntent: Intent? = context.getPackageManager().getLaunchIntentForPackage(packageName)
            val className: String? = launchIntent?.getComponent()?.getClassName()
            if (className == null) {
                return 0
            }
            intentClass = try {
                java.lang.Class.forName(className)
            } catch (e: java.lang.ClassNotFoundException) {
                e.printStackTrace()
                return 0
            }
            intent = Intent(context, intentClass)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.setAction(Intent.ACTION_VIEW)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            notificationContentText = context.getResources().getString(R.string.keep_status_running)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val stopIntent = Intent(PushNotificationHelper.ACTION_TAP_STOP)
        val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notify_status)
                .setContentTitle(context.getResources().getString(R.string.background_service_opened))
                .setContentText(notificationContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setNumber(0)
                .addAction(R.drawable.ic_stat_notify_status,
                        context.getResources().getString(R.string.stop),
                        stopPendingIntent)
                .build()
        // the id of the foreground notification MUST NOT be 0
        startForeground(1, notification)
        return START_STICKY
    }

    companion object {
        private const val CHANNEL_ID = "status-service"
    }
}
