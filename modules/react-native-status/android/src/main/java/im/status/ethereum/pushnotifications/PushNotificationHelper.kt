// https://github.com/zo0r/react-native-push-notification/blob/bedc8f646aab67d594f291449fbfa24e07b64fe8/android/src/main/
// java/com/dieam/reactnativepushnotification/modules/RNPushNotificationHelper.java Copy-Paste with removed firebase
package im.status.ethereum.pushnotifications

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import org.json.JSONArray
import org.json.JSONException
import im.status.ethereum.module.R
import im.status.ethereum.pushnotifications.PushNotification.Companion.LOG_TAG

class PushNotificationHelper(private val context: Application, private val intentFilter: IntentFilter) {

    companion object {
        private const val DEFAULT_VIBRATION: Long = 300L
        private const val CHANNEL_ID = "status-im-notifications"
        const val ACTION_DELETE_NOTIFICATION = "im.status.ethereum.module.DELETE_NOTIFICATION"
        const val ACTION_TAP_STOP = "im.status.ethereum.module.TAP_STOP"
    }

    private val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_CANCEL_CURRENT
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    private val persons: MutableMap<String, Person> = mutableMapOf()
    private val messageGroups: MutableMap<String, StatusMessageGroup> = mutableMapOf()

    init {
        registerBroadcastReceiver()
    }

    private val notificationActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_DELETE_NOTIFICATION -> {
                    intent.extras?.getString("im.status.ethereum.groupId")?.let { groupId ->
                        cleanGroup(groupId)
                    }
                }
                ACTION_TAP_STOP -> {
                    stop()
                    System.exit(0)
                }
            }
            Log.e(LOG_TAG, "intent received: ${intent.action}")
        }
    }

    private fun registerBroadcastReceiver() {
        intentFilter.apply {
            addAction(ACTION_DELETE_NOTIFICATION)
            addAction(ACTION_TAP_STOP)
        }
        context.registerReceiver(notificationActionReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        Log.e(LOG_TAG, "Broadcast Receiver registered")
    }

    fun getOpenAppIntent(deepLink: String?): Intent? {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent?.component?.className ?: return null

        return try {
            val intentClass = Class.forName(className)
            Intent(context, intentClass).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                deepLink?.let { data = Uri.parse(it) }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun invokeApp(bundle: Bundle?) {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent?.component?.className ?: return

        try {
            val activityClass = Class.forName(className)
            Intent(context, activityClass).apply {
                bundle?.let { putExtra("notification", it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        } catch (e: ClassNotFoundException) {
            Log.e(LOG_TAG, "Class not found", e)
        }
    }

    fun getMainActivityClass(): Class<*>? {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent?.component?.className ?: return null

        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun isApplicationInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = activityManager.runningAppProcesses ?: return false

        return processInfos.any { processInfo ->
            processInfo.processName == context.packageName &&
            processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            processInfo.pkgList.isNotEmpty()
        }
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
        }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        bitmap.recycle()
        return output
    }

    // Represents a message in a Status conversation
    data class StatusMessage(
        val id: String,
        val author: Person,
        val timestamp: Long,
        val text: String
    )

    // Represents a group of messages in a Status conversation
    inner class StatusMessageGroup(val id: String) {
        private val _messages = mutableListOf<StatusMessage>()
        val messages: List<StatusMessage> get() = _messages.toList()

        fun addMessage(message: StatusMessage) {
            _messages.add(message)
        }

        fun removeMessage(id: String) {
            _messages.removeAll { it.id == id }
        }
    }

    private fun getPerson(bundle: Bundle): Person {
        return Person.Builder()
            .setName(bundle.getString("name"))
            .build()
    }

    private fun createMessage(data: Bundle): StatusMessage {
        val author = getPerson(data.getBundle("notificationAuthor") ?: Bundle())
        val timeStampLongValue = data.getDouble("timestamp").toLong()
        return StatusMessage(
            id = data.getString("id") ?: "",
            author = author,
            timestamp = timeStampLongValue,
            text = data.getString("message") ?: ""
        )
    }

    private fun createGroupOnDismissedIntent(
        context: Context,
        notificationId: Int,
        groupId: String,
        deepLink: String?
    ): PendingIntent {
        return Intent(ACTION_DELETE_NOTIFICATION).apply {
            putExtra("im.status.ethereum.deepLink", deepLink)
            putExtra("im.status.ethereum.groupId", groupId)
        }.let { intent ->
            PendingIntent.getBroadcast(
                context.applicationContext,
                notificationId,
                intent,
                flag
            )
        }
    }

    private fun createGroupOnTapIntent(
        context: Context,
        notificationId: Int,
        groupId: String,
        deepLink: String?
    ): PendingIntent {
        return getOpenAppIntent(deepLink)?.let { intent ->
            PendingIntent.getActivity(
                context.applicationContext,
                notificationId,
                intent,
                flag
            )
        } ?: throw IllegalStateException("Could not create open app intent")
    }

    private fun createOnTapIntent(
        context: Context,
        notificationId: Int,
        deepLink: String?
    ): PendingIntent {
        return getOpenAppIntent(deepLink)?.let { intent ->
            PendingIntent.getActivity(
                context.applicationContext,
                notificationId,
                intent,
                flag
            )
        } ?: throw IllegalStateException("Could not create open app intent")
    }

    private fun createOnDismissedIntent(
        context: Context,
        notificationId: Int,
        deepLink: String?
    ): PendingIntent {
        return Intent(ACTION_DELETE_NOTIFICATION).apply {
            putExtra("im.status.ethereum.deepLink", deepLink)
        }.let { intent ->
            PendingIntent.getBroadcast(
                context.applicationContext,
                notificationId,
                intent,
                flag
            )
        }
    }

    fun removeStatusMessage(bundle: Bundle) {
        val conversationId = bundle.getString("conversationId") ?: return
        val group = messageGroups.getOrPut(conversationId) {
            StatusMessageGroup(conversationId)
        }

        bundle.getString("id")?.let { id ->
            group.removeMessage(id)
        }

        showMessages(bundle)
    }

    fun getMessageGroup(conversationId: String): StatusMessageGroup? {
        return messageGroups[conversationId]
    }

    fun addStatusMessage(bundle: Bundle) {
        val conversationId = bundle.getString("conversationId") ?: return
        val group = messageGroups.getOrPut(conversationId) {
            StatusMessageGroup(conversationId)
        }

        group.addMessage(createMessage(bundle))
        showMessages(bundle)
    }

    fun showMessages(bundle: Bundle) {
        val conversationId = bundle.getString("conversationId") ?: return
        val group = messageGroups[conversationId] ?: return

        val messagingStyle = NotificationCompat.MessagingStyle("Me")

        // If there are no messages, cancel the notification and return
        if (group.messages.isEmpty()) {
            notificationManager.cancel(conversationId.hashCode())
            return
        }

        group.messages.forEach { message ->
            messagingStyle.addMessage(
                message.text,
                message.timestamp,
                message.author
            )
        }

        // Set conversation title if available
        bundle.getString("title")?.let { title ->
            messagingStyle.conversationTitle = title
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_stat_notify_status)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setStyle(messagingStyle)
            setGroup(conversationId)
            setOnlyAlertOnce(true)
            setGroupSummary(true)
            setContentIntent(createGroupOnTapIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
            setDeleteIntent(createGroupOnDismissedIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
            setNumber(group.messages.size + 1)
            setAutoCancel(true)

            // Set empty vibration pattern for Android 5.0 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setVibrate(LongArray(0))
            }
        }

        // Show the notification
        notificationManager.notify(conversationId.hashCode(), builder.build())
    }

    fun checkOrCreateChannel(
        manager: NotificationManager?,
        channelId: String,
        channelName: String?,
        channelDescription: String?,
        soundUri: Uri?,
        importance: Int,
        vibratePattern: LongArray?,
        showBadge: Boolean
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || manager == null) {
            return false
        }

        val existingChannel = manager.getNotificationChannel(channelId)
        val shouldCreateOrUpdate = when {
            // Channel doesn't exist and we have required info
            existingChannel == null && channelName != null && channelDescription != null -> true

            // Channel exists but needs update
            existingChannel != null && (
                (channelName != null && existingChannel.name != channelName) ||
                (channelDescription != null && existingChannel.description != channelDescription)
            ) -> true

            else -> false
        }

        if (shouldCreateOrUpdate) {
            NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(vibratePattern != null)
                vibrationPattern = vibratePattern
                setShowBadge(showBadge)

                // Configure sound
                if (soundUri != null) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }

                manager.createNotificationChannel(this)
            }
            return true
        }

        return false
    }

    fun createChannel(channelInfo: ReadableMap): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        return checkOrCreateChannel(
            manager = notificationManager,
            channelId = channelInfo.getString("channelId") ?: return false,
            channelName = channelInfo.getString("channelName"),
            channelDescription = if (channelInfo.hasKey("channelDescription")) {
                channelInfo.getString("channelDescription")
            } else "",
            soundUri = getSoundUri(
                if (channelInfo.hasKey("soundName")) {
                    channelInfo.getString("soundName")
                } else "default"
            ),
            importance = if (channelInfo.hasKey("importance")) {
                channelInfo.getInt("importance")
            } else 4,
            vibratePattern = if (channelInfo.hasKey("vibrate") && channelInfo.getBoolean("vibrate")) {
                longArrayOf(DEFAULT_VIBRATION)
            } else null,
            showBadge = channelInfo.hasKey("showBadge") && channelInfo.getBoolean("showBadge")
        )
    }

    fun getNotificationDefaultChannelId(): String = CHANNEL_ID

    private fun getSoundUri(soundName: String?): Uri {
        return when {
            soundName == null || soundName.equals("default", ignoreCase = true) -> {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            else -> {
                val resources = context.resources
                val packageName = context.packageName
                val resId = when {
                    // Try to find the resource as is
                    resources.getIdentifier(soundName, "raw", packageName) != 0 -> {
                        resources.getIdentifier(soundName, "raw", packageName)
                    }
                    // Try without file extension
                    else -> {
                        val nameWithoutExtension = soundName.substringBeforeLast('.')
                        resources.getIdentifier(nameWithoutExtension, "raw", packageName)
                    }
                }
                Uri.parse("android.resource://$packageName/$resId")
            }
        }
    }

private fun removeGroup(groupId: String) {
        messageGroups.remove(groupId)
    }

    private fun cleanGroup(groupId: String) {
        removeGroup(groupId)
        if (messageGroups.isEmpty()) {
            notificationManager.cancelAll()
        }
    }

    fun start() {
        Log.e(LOG_TAG, "Starting Foreground Service")
        Intent(context, ForegroundService::class.java).also { serviceIntent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
        registerBroadcastReceiver()
    }

    fun stop() {
        Log.e(LOG_TAG, "Stopping Foreground Service")
        // Cancel all current notifications since their intents won't be valid
        // after the broadcast receiver is killed
        notificationManager.cancelAll()

        Intent(context, ForegroundService::class.java).also { serviceIntent ->
            context.stopService(serviceIntent)
        }
        context.unregisterReceiver(notificationActionReceiver)
    }

    fun sendToNotificationCentre(bundle: Bundle) {
        val aggregator = PushNotificationPicturesAggregator { largeIconImage, bigPictureImage ->
            sendToNotificationCentreWithPicture(bundle, largeIconImage, bigPictureImage)
        }

        aggregator.apply {
            setLargeIconUrl(context, bundle.getString("largeIconUrl"))
            setBigPictureUrl(context, bundle.getString("bigPictureUrl"))
        }
    }

    fun handleConversation(bundle: Bundle) {
        if (bundle.getBoolean("deleted")) {
            removeStatusMessage(bundle)
        } else {
            addStatusMessage(bundle)
        }
    }

    fun clearMessageNotifications(conversationId: String) {
        notificationManager.cancel(conversationId.hashCode())
        cleanGroup(conversationId)
    }

    fun clearAllMessageNotifications() {
        notificationManager.cancelAll()
    }

    fun sendToNotificationCentreWithPicture(
        bundle: Bundle,
        largeIconBitmap: Bitmap?,
        bigPictureBitmap: Bitmap?
    ) {
        try {
            val intentClass = getMainActivityClass() ?: run {
                Log.e(LOG_TAG, "No activity class found for the notification")
                return
            }

            if (bundle.getBoolean("isConversation")) {
                handleConversation(bundle)
                return
            }

            if (bundle.getString("message") == null) {
                Log.d(LOG_TAG, "Ignore this message if you sent data-only notification. " +
                     "Cannot send to notification centre because there is no 'message' field in: $bundle")
                return
            }

            val notificationIdString = bundle.getString("id") ?: run {
                Log.e(LOG_TAG, "No notification ID specified for the notification")
                return
            }

            val resources = context.resources
            val packageName = context.packageName

            val title = bundle.getString("title") ?: run {
                val appInfo = context.applicationInfo
                context.packageManager.getApplicationLabel(appInfo).toString()
            }

            val priority = when (bundle.getString("priority")?.toLowerCase()) {
                "max" -> NotificationCompat.PRIORITY_MAX
                "high" -> NotificationCompat.PRIORITY_HIGH
                "low" -> NotificationCompat.PRIORITY_LOW
                "min" -> NotificationCompat.PRIORITY_MIN
                "default" -> NotificationCompat.PRIORITY_DEFAULT
                else -> NotificationCompat.PRIORITY_HIGH
            }

            val visibility = when (bundle.getString("visibility")?.toLowerCase()) {
                "private" -> NotificationCompat.VISIBILITY_PRIVATE
                "public" -> NotificationCompat.VISIBILITY_PUBLIC
                "secret" -> NotificationCompat.VISIBILITY_SECRET
                else -> NotificationCompat.VISIBILITY_PRIVATE
            }

            val channelId = bundle.getString("channelId") ?: getNotificationDefaultChannelId()

            NotificationCompat.Builder(context, channelId).apply {
                setContentTitle(title)
                setTicker(bundle.getString("ticker"))
                setVisibility(visibility)
                setPriority(priority)
                setAutoCancel(bundle.getBoolean("autoCancel", true))
                setOnlyAlertOnce(bundle.getBoolean("onlyAlertOnce", false))

                // Configure notification based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setShowWhen(bundle.getBoolean("showWhen", true))
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setDefaults(Notification.DEFAULT_LIGHTS)
                }

                // Handle group settings for Android KitKat Watch and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    bundle.getString("group")?.let { group ->
                        setGroup(group)
                    }

                    if (bundle.containsKey("groupSummary") || bundle.getBoolean("groupSummary")) {
                        setGroupSummary(bundle.getBoolean("groupSummary"))
                    }
                }

                // Set notification number if provided
                bundle.getString("number")?.toIntOrNull()?.let { number ->
                    setNumber(number)
                }

                val smallIconResId = findSmallIconResourceId(resources, packageName, bundle)
                setSmallIcon(smallIconResId)

                configureLargeIcon(this, largeIconBitmap, bundle, resources, packageName)

                val message = bundle.getString("message") ?: ""
                setContentText(message)

                bundle.getString("subText")?.let { subText ->
                    setSubText(subText)
                }

                val bigText = bundle.getString("bigText") ?: message
                val style = when {
                    bigPictureBitmap != null -> NotificationCompat.BigPictureStyle()
                        .bigPicture(bigPictureBitmap)
                        .setBigContentTitle(title)
                        .setSummaryText(message)
                    else -> NotificationCompat.BigTextStyle().bigText(bigText)
                }
                setStyle(style)

                val notificationId = notificationIdString.hashCode()
                setContentIntent(createOnTapIntent(context, notificationId, bundle.getString("deepLink")))
                setDeleteIntent(createOnDismissedIntent(context, notificationId, bundle.getString("deepLink")))

                configureSoundAndVibration(this, bundle)

                setOngoing(bundle.getBoolean("ongoing", false))

                // Handle color for Lollipop and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCategory(NotificationCompat.CATEGORY_CALL)
                    bundle.getString("color")?.let { color ->
                        setColor(Color.parseColor(color))
                    }
                }

                // Finally show the notification if conditions are met
                if (!(isApplicationInForeground() && bundle.getBoolean("ignoreInForeground"))) {
                    val notification = build()
                    notification.defaults = notification.defaults or Notification.DEFAULT_LIGHTS

                    if (bundle.containsKey("tag")) {
                        notificationManager.notify(bundle.getString("tag"), notificationId, notification)
                    } else {
                        notificationManager.notify(notificationId, notification)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to send push notification", e)
        }
    }

    private fun findSmallIconResourceId(resources: Resources, packageName: String, bundle: Bundle): Int {
        val smallIcon = bundle.getString("smallIcon")
        return when {
            !smallIcon.isNullOrEmpty() ->
                resources.getIdentifier(smallIcon, "mipmap", packageName)
            smallIcon == null ->
                resources.getIdentifier("ic_stat_notify_status", "drawable", packageName)
            else -> {
                val defaultIcon = resources.getIdentifier("ic_launcher", "mipmap", packageName)
                defaultIcon.takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info
            }
        }
    }

    private fun configureLargeIcon(
        builder: NotificationCompat.Builder,
        largeIconBitmap: Bitmap?,
        bundle: Bundle,
        resources: Resources,
        packageName: String
    ) {
        var finalLargeIcon = largeIconBitmap

        if (finalLargeIcon == null) {
            val largeIcon = bundle.getString("largeIcon")
            val largeIconResId = when {
                !largeIcon.isNullOrEmpty() ->
                    resources.getIdentifier(largeIcon, "mipmap", packageName)
                largeIcon == null ->
                    resources.getIdentifier("ic_launcher", "mipmap", packageName)
                else -> 0
            }

            if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                finalLargeIcon = BitmapFactory.decodeResource(resources, largeIconResId)
            }
        }

        bundle.getBundle("notificationAuthor")?.getString("icon")?.let { icon ->
            val base64Image = icon.split(",")[1]
            val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            builder.setLargeIcon(getCircleBitmap(decodedByte))
        } ?: finalLargeIcon?.let { icon ->
            builder.setLargeIcon(icon)
        }
    }

    private fun configureSoundAndVibration(
        builder: NotificationCompat.Builder,
        bundle: Bundle
    ) {
        var soundUri: Uri? = null

        if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
            val soundName = bundle.getString("soundName") ?: "default"
            soundUri = getSoundUri(soundName)
            builder.setSound(soundUri)
        }

        if (soundUri == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSound(null)
        }

        if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
            val vibration = if (bundle.containsKey("vibration")) {
                bundle.getDouble("vibration").toLong()
            } else DEFAULT_VIBRATION

            val finalVibration = if (vibration == 0L) DEFAULT_VIBRATION else vibration
            builder.setVibrate(longArrayOf(0, finalVibration))
        }
    }
}
