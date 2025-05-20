package im.status.ethereum.pushnotifications

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.*
import im.status.ethereum.pushnotifications.PushNotificationJsDelivery
import java.security.SecureRandom

class PushNotification(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        const val LOG_TAG = "PushNotification"
    }

    private val mRandomNumberGenerator = SecureRandom()
    private val pushNotificationHelper: PushNotificationHelper
    private val delivery: PushNotificationJsDelivery
    private val reactContext: ReactApplicationContext = reactContext
    private var started: Boolean = false

    init {
        reactContext.addActivityEventListener(this)
        val applicationContext = reactContext.applicationContext as Application

        val intentFilter = IntentFilter()
        pushNotificationHelper = PushNotificationHelper(applicationContext, intentFilter)

        delivery = PushNotificationJsDelivery(reactContext)
    }

    override fun getName(): String = "PushNotification"

    // Primary implementation of ActivityEventListener interface
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        // Call the helper method with the correct parameter order
        handleActivityResult(requestCode, resultCode, data)
    }

    // Private helper method for handling activity results
    private fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    private fun getBundleFromIntent(intent: Intent): Bundle? {
        return when {
            intent.hasExtra("notification") -> intent.getBundleExtra("notification")
            intent.hasExtra("google.message_id") -> Bundle().apply {
                putBundle("data", intent.extras)
            }
            else -> null
        }?.apply {
            if (!getBoolean("foreground", false) && !containsKey("userInteraction")) {
                putBoolean("userInteraction", true)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        getBundleFromIntent(intent)?.let { bundle ->
            delivery.notifyNotification(bundle)
        }
    }

    @ReactMethod
    fun createChannel(channelInfo: ReadableMap, callback: Callback?) {
        val created = pushNotificationHelper.createChannel(channelInfo)
        callback?.invoke(created)
    }

    @ReactMethod
    fun presentLocalNotification(details: ReadableMap) {
        if (!started) return

        Arguments.toBundle(details)?.apply {
            // If notification ID is not provided by the user, generate one at random
            if (getString("id") == null) {
                putString("id", mRandomNumberGenerator.nextInt().toString())
            }
            pushNotificationHelper.sendToNotificationCentre(this)
        }
    }

    @ReactMethod
    fun clearMessageNotifications(conversationId: String) {
        if (started) {
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
