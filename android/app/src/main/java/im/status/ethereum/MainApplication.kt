package im.status.ethereum

import android.webkit.WebView
import androidx.multidex.MultiDexApplication
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint
import com.facebook.react.modules.network.OkHttpClientProvider
import com.reactnativenavigation.NavigationApplication
import com.reactnativenavigation.react.NavigationReactNativeHost
import cl.json.RNSharePackage
import com.reactnativecommunity.blurview.BlurViewPackage
import im.status.ethereum.keycard.RNStatusKeycardPackage
import im.status.ethereum.module.StatusPackage
import im.status.ethereum.pushnotifications.PushNotificationPackage
import im.status.ethereum.StatusOkHttpClientFactory

class MainApplication : NavigationApplication() {

    private val mReactNativeHost = object : NavigationReactNativeHost(this) {
        override fun getUseDeveloperSupport(): Boolean {
            return BuildConfig.DEBUG
        }

        override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
          // Packages that cannot be autolinked yet can be added manually here
          add(StatusPackage(RootUtil.isDeviceRooted()))
          add(RNStatusKeycardPackage())
          add(PushNotificationPackage())
          add(BlurViewPackage())
        }

        override fun getJSMainModuleName(): String = "index"

        override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED

        override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
    }

    override val reactNativeHost: ReactNativeHost
        get() = mReactNativeHost

    override fun onCreate() {
        super.onCreate()

        OkHttpClientProvider.setOkHttpClientFactory(StatusOkHttpClientFactory())

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW == "1")

        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            DefaultNewArchitectureEntryPoint.load()
        }
    }
}
