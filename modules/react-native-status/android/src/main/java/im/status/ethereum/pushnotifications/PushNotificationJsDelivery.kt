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

internal class PushNotificationJsDelivery(context: ReactContext) {
    companion object {
        const val LOG_TAG = "PushNotification"
    }

    private val reactContext: ReactContext

    init {
        reactContext = context
    }

    fun convertJSON(bundle: Bundle): String? {
        return try {
            val json: JSONObject = convertJSONObject(bundle)
            json.toString()
        } catch (e: JSONException) {
            null
        }
    }

    // a Bundle is not a map, so we have to convert it explicitly
    @kotlin.Throws(JSONException::class)
    private fun convertJSONObject(bundle: Bundle): JSONObject {
        val json = JSONObject()
        val keys: Set<String> = bundle.keySet()
        for (key in keys) {
            val value: Any? = bundle.get(key)
            if (value == null) {
                continue
            } else if (value is Bundle) {
                json.put(key, convertJSONObject(value as Bundle))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                json.put(key, JSONObject.wrap(value))
            } else {
                json.put(key, value)
            }
        }
        return json
    }

    fun notifyNotification(bundle: Bundle) {
        val bundleString = convertJSON(bundle)
        val params: WritableMap = Arguments.createMap()
        params.putString("dataJSON", bundleString)
        sendEvent("remoteNotificationReceived", params)
    }

    fun notifyNotificationAction(bundle: Bundle) {
        val bundleString = convertJSON(bundle)
        val params: WritableMap = Arguments.createMap()
        params.putString("dataJSON", bundleString)
        sendEvent("notificationActionReceived", params)
    }

    fun sendEvent(eventName: String, params: Any?) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(eventName, params)
        }
    }
}
