// https://github.com/zo0r/react-native-push-notification/blob/bedc8f646aab67d594f291449fbfa24e07b64fe8/android/src/main/java/com/dieam/reactnativepushnotification/modules/RNPushNotificationHelper.java Copy-Paste with removed firebase
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

class PushNotificationHelper(context: Application, intentFilter: IntentFilter) {
    companion object {
        const val LOG_TAG = "PushNotification"
        private const val DEFAULT_VIBRATION: Long = 300L
        private const val CHANNEL_ID = "status-im-notifications"
        const val ACTION_DELETE_NOTIFICATION = "im.status.ethereum.module.DELETE_NOTIFICATION"
        const val ACTION_TAP_STOP = "im.status.ethereum.module.TAP_STOP"
    }

    private val context: Context
    val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_CANCEL_CURRENT
    private val notificationManager: NotificationManager
    private val persons: HashMap<String, Person>
    private val messageGroups: HashMap<String, StatusMessageGroup>
    private val intentFilter: IntentFilter
    fun getOpenAppIntent(deepLink: String?): Intent? {
        val intentClass: java.lang.Class<*>
        val packageName: String = context.getPackageName()
        val launchIntent: Intent? = context.getPackageManager().getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            return null
        }
        val className: String? = launchIntent.getComponent()?.getClassName()
        if (className == null) {
            return null
        }
        intentClass = try {
            java.lang.Class.forName(className)
        } catch (e: java.lang.ClassNotFoundException) {
            e.printStackTrace()
            return null
        }
        val intent = Intent(context, intentClass)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.setAction(Intent.ACTION_VIEW)
        //NOTE: you might wonder, why the heck did he decide to set these flags in particular. Well,
        //the answer is a simple as it can get in the Android native development world. I noticed
        //that my initial setup was opening the app but wasn't triggering any events on the js side, like
        //the links do from the browser. So I compared both intents and noticed that the link from
        //the browser produces an intent with the flag 0x14000000. I found out that it was the following
        //flags in this link:
        //https://stackoverflow.com/questions/52390129/android-intent-setflags-issue
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (deepLink != null) {
            intent.setData(Uri.parse(deepLink))
        }
        return intent
    }

    //NOTE: we use a dynamically created BroadcastReceiver here so that we can capture
    //intents from notifications and act on them. For instance when tapping/dismissing
    //a chat notification we want to clear the chat so that next messages don't show
    //the messages that we have seen again
    private val notificationActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.getAction() === ACTION_DELETE_NOTIFICATION) {
                val groupId: String? = intent.getExtras()?.getString("im.status.ethereum.groupId")
                if (groupId != null) {
                    cleanGroup(groupId)
                }
            }
            if (intent.getAction() === ACTION_TAP_STOP) {
                stop()
                java.lang.System.exit(0)
            }
            Log.e(LOG_TAG, "intent received: " + intent.getAction())
        }
    }

    init {
        this.context = context
        this.intentFilter = intentFilter
        persons = HashMap<String, Person>()
        messageGroups = HashMap<String, StatusMessageGroup>()
        notificationManager = context.getSystemService(NotificationManager::class.java)
        registerBroadcastReceiver()
    }

    fun registerBroadcastReceiver() {
        intentFilter.addAction(ACTION_DELETE_NOTIFICATION)
        intentFilter.addAction(ACTION_TAP_STOP)
        context.registerReceiver(notificationActionReceiver, intentFilter)
        Log.e(LOG_TAG, "Broadcast Receiver registered")
    }

    private fun notificationManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun invokeApp(bundle: Bundle?) {
        val packageName: String = context.getPackageName()
        val launchIntent: Intent? = context.getPackageManager().getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            return
        }
        val className: String? = launchIntent.getComponent()?.getClassName()
        if (className == null) {
            return
        }
        try {
            val activityClass: java.lang.Class<*> = java.lang.Class.forName(className)
            val activityIntent = Intent(context, activityClass)
            if (bundle != null) {
                activityIntent.putExtra("notification", bundle)
            }
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)
        } catch (e: java.lang.Exception) {
            Log.e(LOG_TAG, "Class not found", e)
            return
        }
    }

    fun sendToNotificationCentre(bundle: Bundle) {
        val aggregator = PushNotificationPicturesAggregator(object : PushNotificationPicturesAggregator.Callback {
            override fun call(largeIconImage: Bitmap?, bigPictureImage: Bitmap?) {
                sendToNotificationCentreWithPicture(bundle, largeIconImage, bigPictureImage)
            }
        })
        aggregator.setLargeIconUrl(context, bundle.getString("largeIconUrl"))
        aggregator.setBigPictureUrl(context, bundle.getString("bigPictureUrl"))
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

    fun sendToNotificationCentreWithPicture(bundle: Bundle, largeIconBitmap: Bitmap?, bigPictureBitmap: Bitmap?) {
        var largeIconBitmap: Bitmap? = largeIconBitmap
        try {
            val intentClass: java.lang.Class<*>? = mainActivityClass
            if (intentClass == null) {
                Log.e(LOG_TAG, "No activity class found for the notification")
                return
            }
            if (bundle.getBoolean("isConversation")) {
                handleConversation(bundle)
                return
            }
            if (bundle.getString("message") == null) {
                // this happens when a 'data' notification is received - we do not synthesize a local notification in this case
                Log.d(LOG_TAG, "Ignore this message if you sent data-only notification. Cannot send to notification centre because there is no 'message' field in: $bundle")
                return
            }
            val notificationIdString: String? = bundle.getString("id")
            if (notificationIdString == null) {
                Log.e(LOG_TAG, "No notification ID specified for the notification")
                return
            }
            val res: Resources = context.getResources()
            val packageName: String = context.getPackageName()
            var title: String? = bundle.getString("title")
            if (title == null) {
                val appInfo: ApplicationInfo = context.getApplicationInfo()
                title = context.getPackageManager().getApplicationLabel(appInfo).toString()
            }
            var priority: Int = NotificationCompat.PRIORITY_HIGH
            val priorityString: String? = bundle.getString("priority")
            if (priorityString != null) {
                priority = when (priorityString.lowercase()) {
                    "max" -> NotificationCompat.PRIORITY_MAX
                    "high" -> NotificationCompat.PRIORITY_HIGH
                    "low" -> NotificationCompat.PRIORITY_LOW
                    "min" -> NotificationCompat.PRIORITY_MIN
                    "default" -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_HIGH
                }
            }
            var visibility: Int = NotificationCompat.VISIBILITY_PRIVATE
            val visibilityString: String? = bundle.getString("visibility")
            if (visibilityString != null) {
                visibility = when (visibilityString.lowercase()) {
                    "private" -> NotificationCompat.VISIBILITY_PRIVATE
                    "public" -> NotificationCompat.VISIBILITY_PUBLIC
                    "secret" -> NotificationCompat.VISIBILITY_SECRET
                    else -> NotificationCompat.VISIBILITY_PRIVATE
                }
            }
            var channel_id: String? = bundle.getString("channelId")
            if (channel_id == null) {
                channel_id = notificationDefaultChannelId
            }
            val notification: NotificationCompat.Builder = NotificationCompat.Builder(context, channel_id)
                    .setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(visibility)
                    .setPriority(priority)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true))
                    .setOnlyAlertOnce(bundle.getBoolean("onlyAlertOnce", false))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24 and higher
                // Restore showing timestamp on Android 7+
                // Source: https://developer.android.com/reference/android/app/Notification.Builder.html#setShowWhen(boolean)
                val showWhen: Boolean = bundle.getBoolean("showWhen", true)
                notification.setShowWhen(showWhen)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26 and higher
                // Changing Default mode of notification
                notification.setDefaults(Notification.DEFAULT_LIGHTS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { // API 20 and higher
                val group: String? = bundle.getString("group")
                if (group != null) {
                    notification.setGroup(group)
                }
                if (bundle.containsKey("groupSummary") || bundle.getBoolean("groupSummary")) {
                    notification.setGroupSummary(bundle.getBoolean("groupSummary"))
                }
            }
            val numberString: String? = bundle.getString("number")
            if (numberString != null) {
                notification.setNumber(numberString.toInt())
            }

            // Small icon
            var smallIconResId = 0
            val smallIcon: String? = bundle.getString("smallIcon")
            if (smallIcon != null && !smallIcon.isEmpty()) {
                smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName)
            } else if (smallIcon == null) {
                smallIconResId = res.getIdentifier("ic_stat_notify_status", "drawable", packageName)
            }
            if (smallIconResId == 0) {
                smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName)
                if (smallIconResId == 0) {
                    smallIconResId = android.R.drawable.ic_dialog_info
                }
            }
            notification.setSmallIcon(smallIconResId)

            // Large icon
            if (largeIconBitmap == null) {
                var largeIconResId = 0
                val largeIcon: String? = bundle.getString("largeIcon")
                if (largeIcon != null && !largeIcon.isEmpty()) {
                    largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName)
                } else if (largeIcon == null) {
                    largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName)
                }

                // Before Lolipop there was no large icon for notifications.
                if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                    largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId)
                }
            }
            val author: Bundle? = bundle.getBundle("notificationAuthor")
            if (largeIconBitmap == null && author != null) {
                val base64Image: String? = author.getString("icon")?.split(",")?.get(1)
                if (base64Image != null) {
                    val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
                    val decodedByte: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    notification.setLargeIcon(getCircleBitmap(decodedByte))
                }
            } else if (largeIconBitmap != null) {
                notification.setLargeIcon(largeIconBitmap)
            }
            val message: String? = bundle.getString("message")
            if (message != null) {
                notification.setContentText(message)
            }
            val subText: String? = bundle.getString("subText")
            if (subText != null) {
                notification.setSubText(subText)
            }
            var bigText: String? = bundle.getString("bigText")
            if (bigText == null) {
                bigText = message
            }
            val style: NotificationCompat.Style
            style = if (bigPictureBitmap != null) {
                NotificationCompat.BigPictureStyle()
                        .bigPicture(bigPictureBitmap)
                        .setBigContentTitle(title)
                        .setSummaryText(message)
            } else {
                NotificationCompat.BigTextStyle().bigText(bigText)
            }
            notification.setStyle(style)
            val intent = Intent(context, intentClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            bundle.putBoolean("foreground", isApplicationInForeground)
            bundle.putBoolean("userInteraction", true)
            intent.putExtra("notification", bundle)
            var soundUri: Uri? = null
            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                var soundName: String? = bundle.getString("soundName")
                if (soundName == null) {
                    soundName = "default"
                }
                soundUri = getSoundUri(soundName)
                notification.setSound(soundUri)
            }
            if (soundUri == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.setSound(null)
            }
            if (bundle.containsKey("ongoing") || bundle.getBoolean("ongoing")) {
                notification.setOngoing(bundle.getBoolean("ongoing"))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL)
                val color: String? = bundle.getString("color")
                val defaultColor = -1
                if (color != null) {
                    notification.setColor(Color.parseColor(color))
                } else if (defaultColor != -1) {
                    notification.setColor(defaultColor)
                }
            }
            val notificationID = notificationIdString.hashCode()
            var deepLink: String? = bundle.getString("deepLink")
            if (deepLink != null) {
                notification
                        .setContentIntent(createOnTapIntent(context, notificationID, deepLink))
                        .setDeleteIntent(createOnDismissedIntent(context, notificationID, deepLink))
            }
            val notificationManager: NotificationManager = notificationManager()
            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                var vibration = if (bundle.containsKey("vibration")) bundle.getDouble("vibration") as Long else DEFAULT_VIBRATION
                if (vibration == 0L) {
                    vibration = DEFAULT_VIBRATION
                }
                val vibrationPattern: LongArray = longArrayOf(0L, vibration)
                notification.setVibrate(vibrationPattern)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Define the shortcutId
                val shortcutId: String? = bundle.getString("shortcutId")
                if (shortcutId != null) {
                    notification.setShortcutId(shortcutId)
                }
                val timeoutAfter = bundle.getDouble("timeoutAfter") as Long
                if (timeoutAfter != null && timeoutAfter >= 0) {
                    notification.setTimeoutAfter(timeoutAfter)
                }
            }
            val `when` = bundle.getDouble("when") as Long
            if (`when` != null && `when` >= 0) {
                notification.setWhen(`when`)
            }
            notification.setUsesChronometer(bundle.getBoolean("usesChronometer", false))
            notification.setChannelId(channel_id)
            var actionsArray: JSONArray? = null
            try {
                actionsArray = if (bundle.getString("actions") != null) JSONArray(bundle.getString("actions")) else null
            } catch (e: JSONException) {
                Log.e(LOG_TAG, "Exception while converting actions to JSON object.", e)
            }
            if (actionsArray != null) {
                // No icon for now. The icon value of 0 shows no icon.
                val icon = 0

                // Add button for each actions.
                for (i in 0 until actionsArray.length()) {
                    var action: String
                    action = try {
                        actionsArray.getString(i)
                    } catch (e: JSONException) {
                        Log.e(LOG_TAG, "Exception while getting action from actionsArray.", e)
                        continue
                    }
                    val actionIntent = Intent(context, PushNotificationActions::class.java)
                    actionIntent.setAction("$packageName.ACTION_$i")
                    actionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    // Add "action" for later identifying which button gets pressed.
                    bundle.putString("action", action)
                    actionIntent.putExtra("notification", bundle)
                    actionIntent.setPackage(packageName)
                    val pendingActionIntent: PendingIntent = PendingIntent.getBroadcast(context, notificationID, actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notification.addAction(NotificationCompat.Action.Builder(icon, action, pendingActionIntent).build())
                    } else {
                        notification.addAction(icon, action, pendingActionIntent)
                    }
                }
            }
            if (!(isApplicationInForeground && bundle.getBoolean("ignoreInForeground"))) {
                val info: Notification = notification.build()
                info.defaults = info.defaults or Notification.DEFAULT_LIGHTS
                if (bundle.containsKey("tag")) {
                    val tag: String? = bundle.getString("tag")
                    if (tag != null) {
                        notificationManager.notify(tag, notificationID, info)
                    }
                } else {
                    notificationManager.notify(notificationID, info)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(LOG_TAG, "failed to send push notification", e)
        }
    }

    private fun checkOrCreateChannel(manager: NotificationManager?, channel_id: String, channel_name: String?, channel_description: String?, soundUri: Uri?, importance: Int, vibratePattern: LongArray?, showBadge: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        if (manager == null) return false
        var channel: NotificationChannel? = manager.getNotificationChannel(channel_id)
        if (channel == null && channel_name != null && channel_description != null ||
                channel != null &&
                (channel_name != null && !channel.getName().equals(channel_name) ||
                        channel_description != null && !channel.getDescription().equals(channel_description))) {
            // If channel doesn't exist create a new one.
            // If channel name or description is updated then update the existing channel.
            channel = NotificationChannel(channel_id, channel_name, importance)
            channel.setDescription(channel_description)
            channel.enableLights(true)
            channel.enableVibration(vibratePattern != null)
            channel.setVibrationPattern(vibratePattern)
            channel.setShowBadge(showBadge)
            if (soundUri != null) {
                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                channel.setSound(soundUri, audioAttributes)
            } else {
                channel.setSound(null, null)
            }
            manager.createNotificationChannel(channel)
            return true
        }
        return false
    }

    fun createChannel(channelInfo: ReadableMap): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        val channelId: String? = channelInfo.getString("channelId")
        val channelName: String? = channelInfo.getString("channelName")
        val channelDescription = if (channelInfo.hasKey("channelDescription")) channelInfo.getString("channelDescription") else ""
        val soundName = if (channelInfo.hasKey("soundName")) channelInfo.getString("soundName") else "default"
        val importance = if (channelInfo.hasKey("importance")) channelInfo.getInt("importance") else 4
        val vibrate = channelInfo.hasKey("vibrate") && channelInfo.getBoolean("vibrate")
        val vibratePattern = if (vibrate) longArrayOf(DEFAULT_VIBRATION) else null
        val showBadge = channelInfo.hasKey("showBadge") && channelInfo.getBoolean("showBadge")
        val manager: NotificationManager = notificationManager()
        val soundUri: Uri? = if (soundName != null) getSoundUri(soundName) else null
        if (channelId != null) {
            return checkOrCreateChannel(manager, channelId, channelName, channelDescription, soundUri, importance, vibratePattern, showBadge)
        }
        return false
    }

    val notificationDefaultChannelId: String
        get() = CHANNEL_ID

    private fun getSoundUri(soundName: String): Uri {
        var soundName: String? = soundName
        return if (soundName == null || "default".equals(soundName, ignoreCase = true)) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {

            // sound name can be full filename, or just the resource name.
            // So the strings 'my_sound.mp3' AND 'my_sound' are accepted
            // The reason is to make the iOS and android javascript interfaces compatible
            val resId: Int
            if (context.getResources().getIdentifier(soundName, "raw", context.getPackageName()) !== 0) {
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName())
            } else {
                soundName = soundName.substring(0, soundName.lastIndexOf('.'))
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName())
            }
            Uri.parse("android.resource://" + context.getPackageName() + "/" + resId)
        }
    }

    val mainActivityClass: java.lang.Class<*>?
        get() {
            val packageName: String = context.getPackageName()
            val launchIntent: Intent? = context.getPackageManager().getLaunchIntentForPackage(packageName)
            if (launchIntent == null) {
                return null
            }
            val className: String? = launchIntent.getComponent()?.getClassName()
            if (className == null) {
                return null
            }
            return try {
                java.lang.Class.forName(className)
            } catch (e: java.lang.ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }
    val isApplicationInForeground: Boolean
        get() {
            val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfos: List<RunningAppProcessInfo> = activityManager.getRunningAppProcesses()
            if (processInfos != null) {
                for (processInfo in processInfos) {
                    if (processInfo.processName.equals(context.getPackageName()) && processInfo.importance === RunningAppProcessInfo.IMPORTANCE_FOREGROUND && processInfo.pkgList.size > 0) {
                        return true
                    }
                }
            }
            return false
        }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output: Bitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color: Int = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        val rectF = RectF(rect)
        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawOval(rectF, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        bitmap.recycle()
        return output
    }

    private fun getPerson(bundle: Bundle?): Person {
        val builder = Person.Builder()
        val name: String? = bundle?.getString("name")
        if (name != null) {
            builder.setName(name)
        }
        return builder.build()
    }

    private fun createMessage(data: Bundle): StatusMessage {
        val notificationAuthor: Bundle? = data.getBundle("notificationAuthor")
        val author: Person = getPerson(notificationAuthor)
        val timeStampLongValue = data.getDouble("timestamp") as Long
        val id: String? = data.getString("id")
        val message: String? = data.getString("message")
        return StatusMessage(id ?: "", author, timeStampLongValue, message ?: "")
    }

    private fun createGroupOnDismissedIntent(context: Context, notificationId: Int, groupId: String, deepLink: String?): PendingIntent {
        val intent = Intent(ACTION_DELETE_NOTIFICATION)
        if (deepLink != null) {
            intent.putExtra("im.status.ethereum.deepLink", deepLink)
        }
        intent.putExtra("im.status.ethereum.groupId", groupId)
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, flag)
    }

    private fun createGroupOnTapIntent(context: Context, notificationId: Int, groupId: String, deepLink: String?): PendingIntent {
        val intent: Intent? = getOpenAppIntent(deepLink)
        return PendingIntent.getActivity(context.getApplicationContext(), notificationId, intent, flag)
    }

    private fun createOnTapIntent(context: Context, notificationId: Int, deepLink: String?): PendingIntent {
        val intent: Intent? = getOpenAppIntent(deepLink)
        return PendingIntent.getActivity(context.getApplicationContext(), notificationId, intent, flag)
    }

    private fun createOnDismissedIntent(context: Context, notificationId: Int, deepLink: String): PendingIntent {
        val intent = Intent(ACTION_DELETE_NOTIFICATION)
        intent.putExtra("im.status.ethereum.deepLink", deepLink)
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, flag)
    }

    fun removeStatusMessage(bundle: Bundle) {
        val conversationId: String? = bundle.getString("conversationId")
        if (conversationId == null) {
            return
        }
        var group: StatusMessageGroup? = if (conversationId != null) messageGroups.get(conversationId) else null
        val notificationManager: NotificationManager = notificationManager()

        if (group == null) {
            group = StatusMessageGroup(conversationId)
        }
        messageGroups.put(conversationId, group)

        val id: String? = bundle.getString("id")
        if (id != null) {
            group.removeMessage(id)
        }
        showMessages(bundle)
    }

    fun getMessageGroup(conversationId: String?): StatusMessageGroup? {
        return messageGroups.get(conversationId)
    }

    fun addStatusMessage(bundle: Bundle) {
        val conversationId: String? = bundle.getString("conversationId")
        if (conversationId == null) {
            return
        }
        var group: StatusMessageGroup? = if (conversationId != null) messageGroups.get(conversationId) else null
        val notificationManager: NotificationManager = notificationManager()

        if (group == null) {
            group = StatusMessageGroup(conversationId)
        }
        messageGroups.put(conversationId, group)

        group.addMessage(createMessage(bundle))
        showMessages(bundle)
    }

    fun showMessages(bundle: Bundle) {
        val conversationId: String? = bundle.getString("conversationId")
        if (conversationId == null) {
            return
        }
        val group: StatusMessageGroup? = if (conversationId != null) messageGroups.get(conversationId) else null
        val notificationManager: NotificationManager = notificationManager()
        val messagingStyle: NotificationCompat.MessagingStyle = NotificationCompat.MessagingStyle("Me")
        var messages = if (group != null) group.getMessages() else null
        if (messages == null || messages.size == 0) {
            notificationManager.cancel(conversationId.hashCode())
            return
        }
        for (i in messages.indices) {
            val message: StatusMessage = messages.get(i)
            messagingStyle.addMessage(message.text, message.timestamp, message.getAuthor())
        }
        val title: String? = bundle.getString("title")
        if (title != null) {
            messagingStyle.setConversationTitle(title)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notify_status)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(messagingStyle)
                .setGroup(conversationId)
                .setOnlyAlertOnce(true)
                .setGroupSummary(true)
                .setContentIntent(createGroupOnTapIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
                .setDeleteIntent(createGroupOnDismissedIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
                .setNumber(messages.size + 1)
                .setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setVibrate(LongArray(0))
        }
        notificationManager.notify(conversationId.hashCode(), builder.build())
    }

    inner class StatusMessageGroup(val id: String) {
        private var messages: java.util.ArrayList<StatusMessage>

        init {
            messages = java.util.ArrayList<StatusMessage>()
        }

        fun getMessages(): java.util.ArrayList<StatusMessage> {
            return messages
        }

        fun addMessage(message: StatusMessage) {
            messages.add(message)
        }

        fun removeMessage(id: String) {
            val newMessages: java.util.ArrayList<StatusMessage> = java.util.ArrayList<StatusMessage>()
            for (message in messages) {
                if (message.id != id) {
                    newMessages.add(message)
                }
            }
            messages = newMessages
        }
    }

    inner class StatusMessage(val id: String, author: Person, timestamp: Long, text: String) {
        fun getAuthor(): Person {
            return author
        }

        private val author: Person
        val timestamp: Long
        val text: String

        init {
            this.author = author
            this.timestamp = timestamp
            this.text = text
        }
    }

    private fun removeGroup(groupId: String) {
        messageGroups.remove(groupId)
    }

    private fun cleanGroup(groupId: String) {
        removeGroup(groupId)
        if (messageGroups.size == 0) {
            notificationManager.cancelAll()
        }
    }

    fun start() {
        Log.e(LOG_TAG, "Starting Foreground Service")
        val serviceIntent = Intent(context, ForegroundService::class.java)
        context.startService(serviceIntent)
        registerBroadcastReceiver()
    }

    fun stop() {
        Log.e(LOG_TAG, "Stopping Foreground Service")
        //NOTE: we cancel all the current notifications, because the intents can't be used anymore
        //since the broadcast receiver will be killed as well and won't be able to handle any intent
        notificationManager.cancelAll()
        val serviceIntent = Intent(context, ForegroundService::class.java)
        context.stopService(serviceIntent)
        context.unregisterReceiver(notificationActionReceiver)
    }
}
