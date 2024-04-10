package im.status.ethereum.module

import android.content.Context
import android.os.Environment
import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import statusgo.Statusgo
import java.io.File

class DatabaseManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)

    override fun getName() = "DatabaseManager"

    private fun getExportDBFile(): File {
        val pubDirectory = reactContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(pubDirectory, exportDBFileName)
    }

    @ReactMethod
    fun exportUnencryptedDatabase(accountData: String, password: String, callback: Callback) {
        Log.d(TAG, "login")

        val newFile = getExportDBFile()

        utils.migrateKeyStoreDir(accountData, password)
        val result = Statusgo.exportUnencryptedDatabase(accountData, password, newFile.absolutePath)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "Login result: $result")
        } else {
            Log.e(TAG, "Login failed: $result")
        }
    }

    @ReactMethod
    fun importUnencryptedDatabase(accountData: String, password: String) {
        Log.d(TAG, "importUnencryptedDatabase")

        val newFile = getExportDBFile()

        utils.migrateKeyStoreDir(accountData, password)
        val result = Statusgo.importUnencryptedDatabase(accountData, password, newFile.absolutePath)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "import result: $result")
        } else {
            Log.e(TAG, "import failed: $result")
        }
    }

    companion object {
        private const val TAG = "DatabaseManager"
        private const val exportDBFileName = "export.db"
    }
}
