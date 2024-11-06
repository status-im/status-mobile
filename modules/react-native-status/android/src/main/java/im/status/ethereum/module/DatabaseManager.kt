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
import org.json.JSONObject
import org.json.JSONException

class DatabaseManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)

    override fun getName() = "DatabaseManager"

    private fun getExportDBFile(): File {
        StatusBackendClient.getInstance()?.let {
            if (it.serverEnabled) {
                return File(it.rootDataDir, exportDBFileName)
            }
        }
        val pubDirectory = reactContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(pubDirectory, exportDBFileName)
    }

    @ReactMethod
    fun exportUnencryptedDatabase(accountData: String, password: String, callback: Callback) {
        Log.d(TAG, "exportUnencryptedDatabase")

        val newFile = getExportDBFile()

        utils.migrateKeyStoreDir(accountData, password)
        
        try {
            val accountJson = JSONObject(accountData)
            
            val params = JSONObject().apply {
                put("account", accountJson)
                put("password", password)
                put("databasePath", newFile.absolutePath)
            }
            
            val jsonParams = params.toString()
            StatusBackendClient.executeStatusGoRequestWithCallback(
                endpoint = "ExportUnencryptedDatabaseV2",
                requestBody = jsonParams,
                statusgoFunction = { Statusgo.exportUnencryptedDatabaseV2(jsonParams) },
                callback = null
            )
            callback.invoke(newFile.absolutePath)

        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing account data: ${e.message}")
        }
    }

    @ReactMethod
    fun importUnencryptedDatabase(accountData: String, password: String) {
        Log.d(TAG, "importUnencryptedDatabase")

        val newFile = getExportDBFile()

        utils.migrateKeyStoreDir(accountData, password)

        try {
            val accountJson = JSONObject(accountData)
            
            val params = JSONObject().apply {
                put("account", accountJson)
                put("password", password)
                put("databasePath", newFile.absolutePath)
            }
            
            val jsonParams = params.toString()
            
            StatusBackendClient.executeStatusGoRequest(
                endpoint = "ImportUnencryptedDatabaseV2",
                requestBody = jsonParams,
                statusgoFunction = { Statusgo.importUnencryptedDatabaseV2(jsonParams) }
            )
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing account data: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "DatabaseManager"
        private const val exportDBFileName = "export.db"
    }
}
