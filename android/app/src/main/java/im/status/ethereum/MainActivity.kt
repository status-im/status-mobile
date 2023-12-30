package im.status.ethereum

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.modules.core.PermissionListener
import com.reactnativenavigation.NavigationActivity
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView
import im.status.ethereum.MainApplication
import im.status.ethereum.module.StatusThreadPoolExecutor
import java.util.Properties

class MainActivity : NavigationActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    @Nullable
    private var mPermissionListener: PermissionListener? = null
    private var keepSplash = true
    private val SPLASH_DELAY = 3200

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    protected fun getMainComponentName(): String {
        return "StatusIm"
    }

    private fun registerUncaughtExceptionHandler(context: Context) {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        // High priority, so don't use StatusThreadPoolExecutor
        Thread.setDefaultUncaughtExceptionHandler { thread, t ->
            Thread {
                Looper.prepare()
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(t.toString())
                    .setNegativeButton("Exit") { dialog, id ->
                        dialog.dismiss()
                        defaultUncaughtExceptionHandler.uncaughtException(thread, t)
                    }.show()
                Looper.loop()
            }.start()
        }
    }

    private fun getActivityManager(): ActivityManager {
        return getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private fun getAvailableMemory(activityManager: ActivityManager): ActivityManager.MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    protected fun configureStatus() {
        // Required because of crazy APN settings redirecting localhost (found in GB)
        val properties = System.getProperties()
        properties.setProperty("http.nonProxyHosts", "localhost|127.0.0.1")
        properties.setProperty("https.nonProxyHosts", "localhost|127.0.0.1")
    }

    private fun createNotificationSettingsIntent(): Intent {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
        }
        return intent
    }

    private fun tryToEmit(eventName: String, event: WritableMap) {
        try {
                (getApplication() as MainApplication).getReactNativeHost()
                .getReactInstanceManager()
                .getCurrentReactContext()
                ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("url", event)
            } catch (e: Exception) { // we expect NPE on first start, which is OK because we have a fallback
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.dataString != null && intent.data!!.scheme!!.startsWith("app-settings")) {
            startActivity(createNotificationSettingsIntent())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen();
        setTheme(R.style.DarkTheme)
        // Make sure we get an Alert for every uncaught exceptions
        registerUncaughtExceptionHandler(this)

        // Report memory details for this application
        val activityManager = getActivityManager()
        Log.v("RNBootstrap", "Available system memory " + getAvailableMemory(activityManager).availMem + ", maximum usable application memory " + activityManager.largeMemoryClass + "M")

        setSecureFlag()

        // NOTE: Try to not restore the state https://github.com/software-mansion/react-native-screens/issues/17
        super.onCreate(null)

        if (!shouldShowRootedNotification()) {
            configureStatus()
        } else {
            val dialog = AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.root_warning))
                .setPositiveButton(resources.getString(R.string.root_okay)) { dialog, which ->
                    rejectRootedNotification()
                    dialog.dismiss()
                    configureStatus()
                }
                .setNegativeButton(resources.getString(R.string.root_cancel)) { dialog, which ->
                    dialog.dismiss()
                    finishAffinity()
                }
                .setOnCancelListener {
                    it.dismiss()
                    finishAffinity()
                }
                .create()

            dialog.show()
        }

        val r = Runnable {
            System.loadLibrary("status-logs")

            // when app is started but the Activity has been destroyed, the deep linking url event is
            // not emitted when coming back to foreground. This is a workaround. If the problem is
            // resolved in react-native this code should be removed
            if (intent.data != null) {
                val event = Arguments.createMap()
                event.putString("url", intent.dataString)
                // on first start emit will (silently) fail, but the regular deep linking handler will work
                tryToEmit("url", event)
            }
        }

        splashScreen.setKeepOnScreenCondition { keepSplash }

        val handler = Handler()
        handler.postDelayed({ keepSplash = false }, SPLASH_DELAY.toLong())

        StatusThreadPoolExecutor.getInstance().execute(r)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val intent = Intent("onConfigurationChanged")
        intent.putExtra("newConfig", newConfig)
        sendBroadcast(intent)
    }

    private val REJECTED_ROOTED_NOTIFICATION = "rejectedRootedNotification"
    private val FREQUENCY_OF_REMINDER_IN_PERCENT = 5

    private fun shouldShowRootedNotification(): Boolean {
        if (RootUtil.isDeviceRooted() && BuildConfig.ENABLE_ROOT_ALERT == "1") {
            return if (userRejectedRootedNotification()) {
                (Math.random() * 100) < FREQUENCY_OF_REMINDER_IN_PERCENT
            } else true
        } else {
            return false
        }
    }

    private fun userRejectedRootedNotification(): Boolean {
        val preferences = getPreferences(0)
        return preferences.getBoolean(REJECTED_ROOTED_NOTIFICATION, false)
    }

    private fun rejectRootedNotification() {
        val preferences = getPreferences(0)
        val editor = preferences.edit()
        editor.putBoolean(REJECTED_ROOTED_NOTIFICATION, true)
        editor.commit()
    }

    private fun setSecureFlag() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val setSecure = sharedPrefs.getBoolean("BLANK_PREVIEW", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && setSecure) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

   @TargetApi(Build.VERSION_CODES.M)
   override fun requestPermissions(permissions: Array<String>, requestCode: Int, listener: PermissionListener) {
       mPermissionListener = listener
       super.requestPermissions(permissions, requestCode)
   }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (mPermissionListener != null && mPermissionListener!!.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            mPermissionListener = null
        }
    }
}
