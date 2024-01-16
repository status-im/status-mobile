package im.status.ethereum.pushnotifications

import im.status.ethereum.pushnotifications.PushNotification
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class PushNotificationPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf<NativeModule>(PushNotification(reactContext))
    }

    fun createJSModules(): List<java.lang.Class<out JavaScriptModule?>> {
        return emptyList<java.lang.Class<out JavaScriptModule?>>()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
