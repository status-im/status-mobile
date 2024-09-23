package im.status.ethereum.module

import android.content.Context
import android.os.Environment
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray;
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.function.Supplier
import statusgo.Statusgo

class Utils(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val TAG = "Utils"
    }

    override fun getName(): String {
        return "Utils"
    }

    fun getNoBackupDirectory(): String {
        return reactContext.noBackupFilesDir.absolutePath
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun backupDisabledDataDir(): String {
        return getNoBackupDirectory()
    }

    fun getPublicStorageDirectory(): File? {
        // Environment.getExternalStoragePublicDirectory doesn't work as expected on Android Q
        // https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
        return reactContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    fun getKeyUID(json: String): String {
        val jsonObj = JSONObject(json)
        return jsonObj.getString("key-uid")
    }

    fun pathCombine(path1: String, path2: String): String {
        val file = File(path1, path2)
        return file.absolutePath
    }

    fun getKeyStorePath(keyUID: String): String {
        val commonKeydir = pathCombine(getNoBackupDirectory(), "/keystore")
        return pathCombine(commonKeydir, keyUID)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun keystoreDir(): String {
        val absRootDirPath = getNoBackupDirectory()
        return pathCombine(absRootDirPath, "keystore")
    }

    fun migrateKeyStoreDir(accountData: String, password: String) {
        try {
            val commonKeydir = pathCombine(getNoBackupDirectory(), "/keystore")
            val keydir = getKeyStorePath(getKeyUID(accountData))
            Log.d(TAG, "before migrateKeyStoreDir $keydir")

            val keydirFile = File(keydir)
            if (!keydirFile.exists() || keydirFile.list().isEmpty()) {
                Log.d(TAG, "migrateKeyStoreDir")
                Statusgo.migrateKeyStoreDir(accountData, password, commonKeydir, keydir)
                Statusgo.initKeystore(keydir)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON conversion failed: ${e.message}")
        }
    }

    fun checkAvailability(): Boolean {
        // We wait at least 10s for getCurrentActivity to return a value,
        // otherwise we give up
        for (attempts in 0 until 100) {
            if (currentActivity != null) {
                return true
            }
            try {
                Thread.sleep(100)
            } catch (ex: InterruptedException) {
                if (currentActivity != null) {
                    return true
                }
                Log.d(TAG, "Activity doesn't exist")
                return false
            }
        }

        Log.d(TAG, "Activity doesn't exist")
        return false
    }

    fun executeRunnableStatusGoMethod(method: Supplier<String>, callback: Callback) {
        if (!checkAvailability()) {
            callback.invoke(false)
            return
        }

        val runnableTask = Runnable {
            val res = method.get()
            callback.invoke(res)
        }

        StatusThreadPoolExecutor.getInstance().execute(runnableTask)
    }

    @ReactMethod
    fun validateMnemonic(seed: String, callback: Callback) {
        executeRunnableStatusGoMethod({ Statusgo.validateMnemonic(seed) }, callback)
    }

    fun is24Hour(): Boolean {
        return android.text.format.DateFormat.is24HourFormat(reactContext.applicationContext)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun checkAddressChecksum(address: String): String {
        return Statusgo.checkAddressChecksum(address)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun isAddress(address: String): String {
        return Statusgo.isAddress(address)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun toChecksumAddress(address: String): String {
        return Statusgo.toChecksumAddress(address)
    }

    fun readableArrayToStringArray(r: ReadableArray): Array<String> {
        val length = r.size()
        val strArray = Array(length) { "" }

        for (keyIndex in 0 until length) {
            strArray[keyIndex] = r.getString(keyIndex) ?: ""
        }

        return strArray
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun validateConnectionString(connectionString: String): String {
        return Statusgo.validateConnectionString(connectionString)
    }
}
