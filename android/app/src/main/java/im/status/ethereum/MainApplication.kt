package im.status.ethereum

import android.webkit.WebView
import androidx.multidex.MultiDexApplication
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.modules.network.OkHttpClientProvider
import com.reactnativenavigation.NavigationApplication
import com.reactnativenavigation.react.NavigationReactNativeHost
import com.facebook.react.bridge.JSIModulePackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint
import cl.json.RNSharePackage
import com.reactnativecommunity.blurview.BlurViewPackage
import im.status.ethereum.keycard.RNStatusKeycardPackage
import im.status.ethereum.module.StatusBackendClient
import im.status.ethereum.module.StatusPackage
import im.status.ethereum.pushnotifications.PushNotificationPackage
import im.status.ethereum.StatusOkHttpClientFactory
import org.json.JSONObject
import android.content.ComponentCallbacks2
import android.util.Log
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process

class MainApplication : NavigationApplication() {

    private val mReactNativeHost = object : NavigationReactNativeHost(this) {
        override fun getUseDeveloperSupport() = BuildConfig.DEBUG

        override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
                add(StatusPackage(RootUtil.isDeviceRooted()))
                add(RNStatusKeycardPackage())
                add(PushNotificationPackage())
                add(BlurViewPackage())
            }

        override fun getJSMainModuleName() = "index"
        override val isNewArchEnabled = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled  = BuildConfig.IS_HERMES_ENABLED
    }

    override val reactNativeHost: ReactNativeHost
        get() = mReactNativeHost

    override fun onCreate() {
        super.onCreate()
        OkHttpClientProvider.setOkHttpClientFactory(StatusOkHttpClientFactory())
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW == "1")
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            DefaultNewArchitectureEntryPoint.load()
        }
        
        logHistoricalProcessExitReasons()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.i("MainApplication", "onLowMemory called")
        StatusPackage.releaseOSMemory()
        emitEvent(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.i("MainApplication", "onTrimMemory called with level: $level")
        if (level !in arrayOf(
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE
        )) {
            return
        }
        StatusPackage.releaseOSMemory()
        emitEvent(level)
    }

    private fun emitEvent(level: Int) {
        val data = Arguments.createMap().apply { putInt("level", level) }
        // we can handle this event in src/status_im/common/signals/events.cljs with function process if we want to
        val jsonEvent = JSONObject().apply {
            put("type", "system.low-memory")
            put("data", data)
        }
        val params = Arguments.createMap().apply { putString("jsonEvent", jsonEvent.toString()) }
        getCurrentReactContext()
            ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            ?.emit("gethEvent", params)
    }

    private fun getCurrentReactContext(): ReactContext? =
        reactNativeHost.reactInstanceManager.currentReactContext

    private fun logHistoricalProcessExitReasons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val exitReasons = activityManager.getHistoricalProcessExitReasons(packageName, 0, 5)
                
                if (exitReasons.isEmpty()) {
                    Log.i("MainApplication", "No historical process exit reasons found")
                    return
                }
                
                Log.e("MainApplication", "Historical process exit reasons (last 5):")
                exitReasons.forEachIndexed { index, reason ->
                    Log.e("MainApplication", "Exit reason #${index + 1}:")
                    Log.e("MainApplication", "  Process: ${reason.processName}")
                    Log.e("MainApplication", "  Reason: ${reason.reason}")
                    Log.e("MainApplication", "  Timestamp: ${reason.timestamp}")
                    Log.e("MainApplication", "  Description: ${reason.description}")
                    if (reason.importance > 0) {
                        Log.e("MainApplication", "  Importance: ${reason.importance}")
                    }
                    if (reason.pss > 0) {
                        Log.e("MainApplication", "  PSS: ${reason.pss} KB")
                    }
                    if (reason.rss > 0) {
                        Log.e("MainApplication", "  RSS: ${reason.rss} KB")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainApplication", "Error getting historical process exit reasons", e)
            }
        } else {
            Log.i("MainApplication", "Historical process exit reasons not available on this Android version")
        }
    }
}
