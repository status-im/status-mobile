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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        StatusPackage.switchToLowMemoryMode()
        emitEvent(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level !in arrayOf(
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE
        )) {
            return
        }
        StatusPackage.switchToLowMemoryMode()
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
}
