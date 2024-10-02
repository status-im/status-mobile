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

class StatusModule(private val reactContext: ReactApplicationContext, private val rootedDevice: Boolean) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener, SignalHandler {

    companion object {
        private const val TAG = "StatusModule"
        private var module: StatusModule? = null
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
        Statusgo.connectionChange(type, if (isExpensive) 1 else 0)
    }

    @ReactMethod
    fun appStateChange(type: String) {
        Log.d(TAG, "AppStateChange: $type")
        Statusgo.appStateChange(type)
    }

    @ReactMethod
    fun startLocalNotifications() {
        Log.d(TAG, "startLocalNotifications")
        Statusgo.startLocalNotifications()
    }

    @ReactMethod
    fun getNodeConfig(callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.getNodeConfig() }, callback)
    }

    @ReactMethod
    fun addCentralizedMetric(request: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.addCentralizedMetric(request) }, callback)
    }

    @ReactMethod
    fun toggleCentralizedMetrics(request: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.toggleCentralizedMetrics(request) }, callback)
    }

    @ReactMethod
    fun deleteImportedKey(keyUID: String, address: String, password: String, callback: Callback) {
        val keyStoreDir = utils.getKeyStorePath(keyUID)
        utils.executeRunnableStatusGoMethod({ Statusgo.deleteImportedKey(address, password, keyStoreDir) }, callback)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun fleets(): String {
        return Statusgo.fleets()
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
