package im.status.ethereum.module

import android.app.Activity
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import statusgo.SignalHandler
import statusgo.Statusgo
import org.json.JSONException
import android.view.WindowManager
import org.json.JSONObject

class StatusModule(private val reactContext: ReactApplicationContext, private val rootedDevice: Boolean) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener, SignalHandler {

    companion object {
        private const val TAG = "StatusModule"
        private var module: StatusModule? = null

        fun getInstance(): StatusModule? {
            return module
        }
    }

    private val utils: Utils = Utils(reactContext)
    private var background: Boolean = false

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun getName(): String {
        return "Status"
    }

    override fun onHostResume() {
        module = this
        background = false
        Statusgo.setMobileSignalHandler(this)
    }

    override fun onHostPause() {
        background = true
    }

    override fun onHostDestroy() {
        Log.d(TAG, "******************* ON HOST DESTROY *************************")
    }

    override fun handleSignal(jsonEventString: String) {
        val params = Arguments.createMap()
        params.putString("jsonEvent", jsonEventString)
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("gethEvent", params)
    }

    @ReactMethod
    fun closeApplication() {
        System.exit(0)
    }

    @ReactMethod
    fun connectionChange(type: String, isExpensive: Boolean) {
        Log.d(TAG, "ConnectionChange: $type, is expensive $isExpensive")
        val params = JSONObject().apply {
            put("type", type)
            put("expensive", isExpensive)
        }

        val jsonString = params.toString()
        StatusBackendClient.executeStatusGoRequest(
            endpoint = "ConnectionChangeV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.connectionChangeV2(jsonString) }
        )
    }

    @ReactMethod
    fun appStateChange(state: String) {
        Log.d(TAG, "AppStateChange: $state")
        val params = JSONObject().apply {
            put("state", state)
        }
        val jsonString = params.toString()
        StatusBackendClient.executeStatusGoRequest(
            endpoint = "AppStateChangeV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.appStateChangeV2(jsonString) }
        )
    }

    @ReactMethod
    fun startLocalNotifications() {
        Log.d(TAG, "startLocalNotifications")
        StatusBackendClient.executeStatusGoRequest(
            endpoint = "StartLocalNotifications",
            requestBody = "",
            statusgoFunction = { Statusgo.startLocalNotifications() }
        )
    }

    @ReactMethod
    fun getNodeConfig(callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "GetNodeConfig",
            requestBody = "",
            statusgoFunction = { Statusgo.getNodeConfig() },
            callback = callback
        )
    }

    @ReactMethod
    fun addCentralizedMetric(request: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "AddCentralizedMetric",
            requestBody = request,
            statusgoFunction = { Statusgo.addCentralizedMetric(request) },
            callback
        )
    }

    @ReactMethod
    fun toggleCentralizedMetrics(request: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "ToggleCentralizedMetrics",
            requestBody = request,
            statusgoFunction = { Statusgo.toggleCentralizedMetrics(request) },
            callback
        )
    }

    @ReactMethod
    fun deleteImportedKey(keyUID: String, address: String, password: String, callback: Callback) {
        val keyStoreDir = utils.getKeyStorePath(keyUID)
        val params = JSONObject().apply {
            put("address", address)
            put("password", password)
            put("keyStoreDir", keyStoreDir)
        }
        val jsonString = params.toString()
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "DeleteImportedKeyV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.deleteImportedKeyV2(jsonString) },
            callback
        )
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun fleets(): String {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            endpoint = "Fleets",
            requestBody = "",
            statusgoFunction = { Statusgo.fleets() }
        )
    }

    override fun getConstants(): Map<String, Any>? {
        return hashMapOf(
            "is24Hour" to utils.is24Hour(),
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "buildId" to Build.ID,
            "deviceId" to Build.BOARD
        )
    }

    @ReactMethod
    fun isDeviceRooted(callback: Callback) {
        callback.invoke(rootedDevice)
    }

    @ReactMethod
    fun deactivateKeepAwake() {
        val activity = currentActivity

        activity?.runOnUiThread {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
