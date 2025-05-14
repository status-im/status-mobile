package im.status.ethereum.pushnotifications

import android.os.Build
import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.ReactContext
import im.status.ethereum.pushnotifications.PushNotificationJsDelivery
import im.status.ethereum.pushnotifications.PushNotification.Companion.LOG_TAG

/**
 * BroadcastReceiver responsible for handling push notification actions.
 * This class processes notification interactions and manages their lifecycle.
 */
class PushNotificationActions : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intentActionPrefix = "${context.packageName}.ACTION_"

        Log.i(LOG_TAG, "PushNotificationBootEventReceiver loading scheduled notifications")

        if (intent.action == null || !intent.action!!.startsWith(intentActionPrefix)) {
            return
        }

        val bundle = intent.getBundleExtra("notification") ?: return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationID = bundle.getString("id")?.toInt() ?: return

        // Dismiss the notification popup.
        val autoCancel = bundle.getBoolean("autoCancel", true)
        if (autoCancel) {
            bundle.getString("tag")?.let { tag ->
                manager.cancel(tag, notificationID)
            } ?: run {
                manager.cancel(notificationID)
            }
        }

        // Notify the action.
        val invokeApp = bundle.getBoolean("invokeApp", true)

        if (invokeApp) {
            val intentFilter = IntentFilter()
            val helper = PushNotificationHelper(
                context.applicationContext as Application,
                intentFilter
            )
            helper.invokeApp(bundle)
        } else {
            // We need to run this on the main thread, as the React code assumes that is true.
            // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
            // "Can't create handler inside thread that has not called Looper.prepare()"
            Handler(Looper.getMainLooper()).post {
                // Construct and load our normal React JS code bundle
                val reactInstanceManager = (context.applicationContext as ReactApplication)
                    .reactNativeHost
                    .reactInstanceManager

                val currentContext = reactInstanceManager.currentReactContext

                when {
                    // If context exists, deliver notification immediately
                    currentContext != null -> {
                        PushNotificationJsDelivery(currentContext)
                            .notifyNotificationAction(bundle)
                    }
                     // Otherwise wait for construction, then send the notification
                    else -> {
                        val listener = object : ReactInstanceManager.ReactInstanceEventListener {
                            override fun onReactContextInitialized(reactContext: ReactContext) {
                                PushNotificationJsDelivery(reactContext)
                                    .notifyNotificationAction(bundle)
                                reactInstanceManager.removeReactInstanceEventListener(this)
                            }
                        }

                        // Add the listener and create context if needed
                        reactInstanceManager.addReactInstanceEventListener(listener)
                        if (!reactInstanceManager.hasStartedCreatingInitialContext()) {
                            // Construct it in the background
                            reactInstanceManager.createReactContextInBackground()
                        }
                    }
                }
            }
        }
    }
}
