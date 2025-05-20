package im.status.ethereum.module

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import statusgo.Statusgo

class StatusPackage(private val rootedDevice: Boolean) : ReactPackage {

    companion object {
        fun getImageTLSCert(): String = 
            StatusBackendClient.executeStatusGoRequestWithResult(
                endpoint = "ImageServerTLSCert",
                requestBody = "",
                statusgoFunction = { Statusgo.imageServerTLSCert() }
            )
            
        fun releaseOSMemory() {
            // use pool to execute the request to avoid android.os.NetworkOnMainThreadException
            StatusThreadPoolExecutor.getInstance().execute {
                StatusBackendClient.executeStatusGoRequest(
                    endpoint = "ReleaseOSMemory",
                    requestBody = "",
                    statusgoFunction = { Statusgo.releaseOSMemory() }
                )
            }
        }
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules = mutableListOf<NativeModule>()

        modules.apply {
            add(StatusModule(reactContext, rootedDevice))
            add(AccountManager(reactContext))
            add(EncryptionUtils(reactContext))
            add(DatabaseManager(reactContext))
            add(UIHelper(reactContext))
            add(LogManager(reactContext))
            add(Utils(reactContext))
            add(NetworkManager(reactContext))
            add(MailManager(reactContext))
            add(RNSelectableTextInputModule(reactContext))
            add(StatusBackendClient(reactContext))
        }

        return modules
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(
            RNSelectableTextInputViewManager()
        )
    }
}
