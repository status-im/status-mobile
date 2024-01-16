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

class PushNotificationActions : BroadcastReceiver() {
    companion object {
        const val LOG_TAG = "PushNotification"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val intentActionPrefix: String = context.getPackageName() + ".ACTION_"
        Log.i(LOG_TAG, "PushNotificationBootEventReceiver loading scheduled notifications")
        var intentAction: String? = intent.getAction()
        if (intentAction == null || !intentAction.startsWith(intentActionPrefix)) {
            return
        }
        val bundle: Bundle? = intent.getBundleExtra("notification")
        if (bundle == null) {
            return
        }

        // Dismiss the notification popup.
        val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID: Int? = bundle.getString("id")?.toInt()
        val autoCancel: Boolean = bundle.getBoolean("autoCancel", true)
        if (notificationID != null && autoCancel) {
            if (bundle.containsKey("tag")) {
                val tag: String? = bundle.getString("tag")
                if (tag != null) {
                    manager.cancel(tag, notificationID)
                }
            } else {
                manager.cancel(notificationID)
            }
        }
        val invokeApp: Boolean = bundle.getBoolean("invokeApp", true)

        // Notify the action.
        if (invokeApp) {
            val intentFilter = IntentFilter()
            val helper = PushNotificationHelper(context.getApplicationContext() as Application, intentFilter)
            helper.invokeApp(bundle)
        } else {
            // We need to run this on the main thread, as the React code assumes that is true.
            // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
            // "Can't create handler inside thread that has not called Looper.prepare()"
            val handler = Handler(Looper.getMainLooper())
            handler.post(object : java.lang.Runnable {
                override fun run() {
                    // Construct and load our normal React JS code bundle
                    val mReactInstanceManager: ReactInstanceManager = (context.getApplicationContext() as ReactApplication).getReactNativeHost().getReactInstanceManager()
                    val context: ReactContext? = mReactInstanceManager.getCurrentReactContext()
                    // If it's constructed, send a notification
                    if (context != null) {
                        val delivery = PushNotificationJsDelivery(context)
                        delivery.notifyNotificationAction(bundle)
                    } else {
                        // Otherwise wait for construction, then send the notification
                        mReactInstanceManager.addReactInstanceEventListener(object : ReactInstanceManager.ReactInstanceEventListener {
                            override fun onReactContextInitialized(context: ReactContext) {
                                val delivery = PushNotificationJsDelivery(context)
                                delivery.notifyNotificationAction(bundle)
                                mReactInstanceManager.removeReactInstanceEventListener(this)
                            }
                        })
                        if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                            // Construct it in the background
                            mReactInstanceManager.createReactContextInBackground()
                        }
                    }
                }
            })
        }
    }
}
