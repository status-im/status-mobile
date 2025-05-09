package im.status.ethereum.pushnotifications

import android.os.Build
import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONException
import org.json.JSONObject
import im.status.ethereum.pushnotifications.PushNotification.Companion.LOG_TAG

class PushNotificationJsDelivery(
    private val reactContext: ReactContext
) {
    /**
     * Converts a Bundle to its JSON string representation.
     * Returns null if conversion fails.
     */
    fun convertJSON(bundle: Bundle): String? {
        return try {
            convertJSONObject(bundle).toString()
        } catch (e: JSONException) {
            null
        }
    }

    /**
     * Converts a Bundle to a JSONObject, handling nested bundles and different value types.
     * This explicit conversion is necessary because Bundle is not a standard map.
     */
    @Throws(JSONException::class)
    private fun convertJSONObject(bundle: Bundle): JSONObject {
        return JSONObject().apply {
            // Using Kotlin's for loop syntax and 'apply' scope function
            bundle.keySet().forEach { key ->
                bundle.get(key)?.let { value ->
                    when (value) {
                        is Bundle -> put(key, convertJSONObject(value))
                        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            put(key, JSONObject.wrap(value))
                        } else {
                            put(key, value)
                        }
                    }
                }
            }
        }
    }

    /**
     * Notifies about a received remote notification by sending an event to JavaScript
     */
    fun notifyNotification(bundle: Bundle) {
        convertJSON(bundle)?.let { bundleString ->
            Arguments.createMap().apply {
                putString("dataJSON", bundleString)
                sendEvent("remoteNotificationReceived", this)
            }
        }
    }

    /**
     * Notifies about a notification action by sending an event to JavaScript
     */
    fun notifyNotificationAction(bundle: Bundle) {
        convertJSON(bundle)?.let { bundleString ->
            Arguments.createMap().apply {
                putString("dataJSON", bundleString)
                sendEvent("notificationActionReceived", this)
            }
        }
    }

    /**
     * Sends an event to JavaScript if there's an active Catalyst instance
     */
    fun sendEvent(eventName: String, params: Any) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }
}
