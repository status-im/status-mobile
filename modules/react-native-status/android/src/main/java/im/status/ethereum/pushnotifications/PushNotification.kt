package im.status.ethereum.pushnotifications

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import android.util.Log
import im.status.ethereum.pushnotifications.PushNotificationJsDelivery

class PushNotification(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {
    companion object {
        const val LOG_TAG = "PushNotification"
    }

    private val mRandomNumberGenerator: java.security.SecureRandom = java.security.SecureRandom()
    private val pushNotificationHelper: PushNotificationHelper
    private val delivery: PushNotificationJsDelivery
    private val reactContext: ReactApplicationContext
    private var started = false

    init {
        this.reactContext = reactContext
        reactContext.addActivityEventListener(this)
        val applicationContext: Application = reactContext.getApplicationContext() as Application
        val intentFilter = IntentFilter()
        pushNotificationHelper = PushNotificationHelper(applicationContext, intentFilter)
        delivery = PushNotificationJsDelivery(reactContext)
    }

    override fun getName(): String {
        return "PushNotification"
    }

    // removed @Override temporarily just to get it working on different versions of RN
    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResult(requestCode, resultCode, data)
    }

    // removed @Override temporarily just to get it working on different versions of RN
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    private fun getBundleFromIntent(intent: Intent): Bundle? {
        var bundle: Bundle? = null
        if (intent.hasExtra("notification")) {
            bundle = intent.getBundleExtra("notification")
        } else if (intent.hasExtra("google.message_id")) {
            bundle = Bundle()
            bundle.putBundle("data", intent.getExtras())
        }
        if (null != bundle && !bundle.getBoolean("foreground", false) && !bundle.containsKey("userInteraction")) {
            bundle.putBoolean("userInteraction", true)
        }
        return bundle
    }

    override fun onNewIntent(intent: Intent) {
        val bundle: Bundle? = getBundleFromIntent(intent)
        if (bundle != null) {
            delivery.notifyNotification(bundle)
        }
    }

    // Creates a channel if it does not already exist. Returns whether the channel was created.
    @ReactMethod
    fun createChannel(channelInfo: ReadableMap?, callback: Callback?) {
        if (channelInfo == null) {
            return
        }
        val created: Boolean = pushNotificationHelper.createChannel(channelInfo)
        if (callback != null) {
            callback.invoke(created)
        }
    }

    @ReactMethod
    fun presentLocalNotification(details: ReadableMap?) {
        if (!started) {
            return
        }
        val bundle: Bundle? = Arguments.toBundle(details)
        if (bundle == null) {
            return
        }
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", mRandomNumberGenerator.nextInt().toString())
        }
        pushNotificationHelper.sendToNotificationCentre(bundle)
    }

    @ReactMethod
    fun clearMessageNotifications(conversationId: String?) {
        if (started && conversationId != null) {
            pushNotificationHelper.clearMessageNotifications(conversationId)
        }
    }

    @ReactMethod
    fun clearAllMessageNotifications() {
        pushNotificationHelper.clearAllMessageNotifications()
    }

    @ReactMethod
    fun enableNotifications() {
        started = true
        pushNotificationHelper.start()
    }

    @ReactMethod
    fun disableNotifications() {
        if (started) {
            started = false
            pushNotificationHelper.stop()
        }
    }
}
