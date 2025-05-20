package im.status.ethereum.module

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.EditText
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIBlock
import com.facebook.react.uimanager.UIManagerModule

class UIHelper(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "UIHelper"
    }

    override fun getName(): String = "UIHelper"

    @ReactMethod
    fun setSoftInputMode(mode: Int) {
        Log.d(TAG, "setSoftInputMode")
        getCurrentActivity()?.run {
            runOnUiThread {
                window.setSoftInputMode(mode)
            }
        }
    }

    @Suppress("DEPRECATION")
    @ReactMethod
    fun clearCookies() {
        Log.d(TAG, "clearCookies")
        getCurrentActivity()?.let { activity ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                CookieManager.getInstance().apply {
                    removeAllCookies(null)
                    flush()
                }
            } else {
                CookieSyncManager.createInstance(activity).also { cookieSyncManager ->
                    cookieSyncManager.startSync()
                    CookieManager.getInstance().apply {
                        removeAllCookie()
                        removeSessionCookie()
                    }
                    cookieSyncManager.stopSync()
                    cookieSyncManager.sync()
                }
            }
        }
    }

    @ReactMethod
    fun toggleWebviewDebug(enabled: Boolean) {
        Log.d(TAG, "toggleWebviewDebug")
        getCurrentActivity()?.run {
            runOnUiThread {
                WebView.setWebContentsDebuggingEnabled(enabled)
            }
        }
    }

    @ReactMethod
    fun clearStorageAPIs() {
        Log.d(TAG, "clearStorageAPIs")
        getCurrentActivity()?.let {
            WebStorage.getInstance()?.deleteAllData()
        }
    }

    @ReactMethod
    fun resetKeyboardInputCursor(reactTagToReset: Int, selection: Int) {
        reactApplicationContext.getNativeModule(UIManagerModule::class.java)?.addUIBlock(object : UIBlock {
            override fun execute(nativeViewHierarchyManager: NativeViewHierarchyManager) {
                (reactApplicationContext.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
                    val viewToReset = nativeViewHierarchyManager.resolveView(reactTagToReset)
                    imm.restartInput(viewToReset)
                    try {
                        (viewToReset as? EditText)?.setSelection(selection)
                    } catch (e: Exception) {
                        // Ignore exceptions during selection setting
                    }
                }
            }
        })
    }
}
