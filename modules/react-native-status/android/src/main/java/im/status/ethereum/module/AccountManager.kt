package im.status.ethereum.module

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import org.json.JSONException
import org.json.JSONObject
import statusgo.Statusgo
import java.io.*

class AccountManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)
    private val logManager = LogManager(reactContext)

    override fun getName() = "AccountManager"

    private fun getTestnetDataDir(absRootDirPath: String) = utils.pathCombine(absRootDirPath, "ethereum/testnet")

    @ReactMethod
    fun createAccountAndLogin(createAccountRequest: String) {
        Log.d(TAG, "createAccountAndLogin")
        val result = Statusgo.createAccountAndLogin(createAccountRequest)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "createAccountAndLogin success: $result")
            Log.d(TAG, "Geth node started")
        } else {
            Log.e(TAG, "createAccountAndLogin failed: $result")
        }
    }

    @ReactMethod
    fun restoreAccountAndLogin(restoreAccountRequest: String) {
        Log.d(TAG, "restoreAccountAndLogin")
        val result = Statusgo.restoreAccountAndLogin(restoreAccountRequest)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "restoreAccountAndLogin success: $result")
            Log.d(TAG, "Geth node started")
        } else {
            Log.e(TAG, "restoreAccountAndLogin failed: $result")
        }
    }

    private fun updateConfig(jsonConfigString: String, absRootDirPath: String, keystoreDirPath: String): String {
        val jsonConfig = JSONObject(jsonConfigString)
        // when doing local pair syncing, backend will provide default data dir
        val dataDirPath = jsonConfig.optString("DataDir","")
        val logEnabled = jsonConfig.getBoolean("LogEnabled")
        val gethLogFile = if (logEnabled) logManager.prepareLogsFile(reactContext) else null
        val gethLogDirPath = gethLogFile?.parent

        Log.d(TAG, "log dir: $gethLogDirPath log name: $gethLogFileName")

        jsonConfig.put("DataDir", dataDirPath)
        jsonConfig.put("KeyStoreDir", keystoreDirPath)
        jsonConfig.put("LogDir", gethLogDirPath)
        jsonConfig.put("LogFile", gethLogFileName)

        return jsonConfig.toString()
    }

    private fun copyDirectory(sourceLocation: File, targetLocation: File) {
        if (sourceLocation.isDirectory) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw IOException("Cannot create dir ${targetLocation.absolutePath}")
            }

            val children = sourceLocation.list()
            children?.forEach { child ->
                copyDirectory(File(sourceLocation, child), File(targetLocation, child))
            }
        } else {
            val directory = targetLocation.parentFile
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw IOException("Cannot create dir ${directory.absolutePath}")
            }

            sourceLocation.inputStream().use { input ->
                targetLocation.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun prepareDirAndUpdateConfig(jsonConfigString: String, keyUID: String): String {
        val absRootDirPath = utils.getNoBackupDirectory()
        val dataFolder = getTestnetDataDir(absRootDirPath)
        Log.d(TAG, "Starting Geth node in folder: $dataFolder")

        try {
            File(dataFolder).mkdir()
        } catch (e: Exception) {
            Log.e(TAG, "error making folder: $dataFolder", e)
        }

        val ropstenFlagPath = utils.pathCombine(absRootDirPath, "ropsten_flag")
        val ropstenFlag = File(ropstenFlagPath)
        if (!ropstenFlag.exists()) {
            try {
                val chaindDataFolderPath = utils.pathCombine(dataFolder, "StatusIM/lightchaindata")
                val lightChainFolder = File(chaindDataFolderPath)
                if (lightChainFolder.isDirectory) {
                    lightChainFolder.listFiles()?.forEach { it.delete() }
                }
                lightChainFolder.delete()
                ropstenFlag.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val testnetDataDir = dataFolder
        val oldKeystoreDir = utils.pathCombine(testnetDataDir, "keystore")
        val newKeystoreDir = utils.pathCombine(absRootDirPath, "keystore")
        val oldKeystore = File(oldKeystoreDir)
        if (oldKeystore.exists()) {
            try {
                val newKeystore = File(newKeystoreDir)
                copyDirectory(oldKeystore, newKeystore)

                if (oldKeystore.isDirectory) {
                    oldKeystore.listFiles()?.forEach { it.delete() }
                }
                oldKeystore.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return try {
            val multiaccountKeystoreDir = utils.pathCombine("/keystore", keyUID)
            val updatedJsonConfigString = updateConfig(jsonConfigString, absRootDirPath, multiaccountKeystoreDir)

            prettyPrintConfig(updatedJsonConfigString)

            updatedJsonConfigString
        } catch (e: JSONException) {
            Log.e(TAG, "updateConfig failed: ${e.message}")
            ""
        }
    }

    @ReactMethod
    fun prepareDirAndUpdateConfig(keyUID: String, config: String, callback: Callback) {
        Log.d(TAG, "prepareDirAndUpdateConfig")
        val finalConfig = prepareDirAndUpdateConfig(config, keyUID)
        callback.invoke(finalConfig)
    }

    @ReactMethod
    fun saveAccountAndLoginWithKeycard(
        multiaccountData: String,
        password: String,
        settings: String,
        config: String,
        accountsData: String,
        chatKey: String
    ) {
        try {
            Log.d(TAG, "saveAccountAndLoginWithKeycard")
            val keyUID = utils.getKeyUID(multiaccountData)
            val finalConfig = prepareDirAndUpdateConfig(config, keyUID)
            val result = Statusgo.saveAccountAndLoginWithKeycard(
                multiaccountData,
                password,
                settings,
                finalConfig,
                accountsData,
                chatKey
            )
            if (result.startsWith("{\"error\":\"\"")) {
                Log.d(TAG, "saveAccountAndLoginWithKeycard result: $result")
                Log.d(TAG, "Geth node started")
            } else {
                Log.e(TAG, "saveAccountAndLoginWithKeycard failed: $result")
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON conversion failed: ${e.message}")
        }
    }

    @ReactMethod
    fun loginWithKeycard(accountData: String, password: String, chatKey: String, nodeConfigJSON: String) {
        Log.d(TAG, "loginWithKeycard")
        utils.migrateKeyStoreDir(accountData, password)
        val result = Statusgo.loginWithKeycard(accountData, password, chatKey, nodeConfigJSON)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithKeycard result: $result")
        } else {
            Log.e(TAG, "LoginWithKeycard failed: $result")
        }
    }

    @ReactMethod
    fun loginWithConfig(accountData: String, password: String, configJSON: String) {
        Log.d(TAG, "loginWithConfig")
        utils.migrateKeyStoreDir(accountData, password)
        val result = Statusgo.loginWithConfig(accountData, password, configJSON)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithConfig result: $result")
        } else {
            Log.e(TAG, "LoginWithConfig failed: $result")
        }
    }

    @ReactMethod
    fun loginAccount(request: String) {
        Log.d(TAG, "loginAccount")
        val result = Statusgo.loginAccount(request)
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "loginAccount result: $result")
        } else {
            Log.e(TAG, "loginAccount failed: $result")
        }
    }

    @ReactMethod
    fun verify(address: String, password: String, callback: Callback) {
        val absRootDirPath = utils.getNoBackupDirectory()
        val newKeystoreDir = utils.pathCombine(absRootDirPath, "keystore")

        utils.executeRunnableStatusGoMethod(
            { Statusgo.verifyAccountPassword(newKeystoreDir, address, password) },
            callback
        )
    }

    @ReactMethod
    fun verifyDatabasePassword(keyUID: String, password: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod(
            { Statusgo.verifyDatabasePassword(keyUID, password) },
            callback
        )
    }

    @ReactMethod
    private fun openAccounts(callback: Callback) {
        Log.d(TAG, "openAccounts")
        val rootDir = utils.getNoBackupDirectory()
        Log.d(TAG, "[Opening accounts $rootDir")
        utils.executeRunnableStatusGoMethod({ Statusgo.openAccounts(rootDir) }, callback)
    }

    @ReactMethod
    fun logout() {
        Log.d(TAG, "logout")
        val runnable = Runnable {
            val result = Statusgo.logout()
            if (result.startsWith("{\"error\":\"\"")) {
                Log.d(TAG, "Logout result: $result")
            } else {
                Log.e(TAG, "Logout failed: $result")
            }
        }
        StatusThreadPoolExecutor.getInstance().execute(runnable)
    }

    @ReactMethod
    fun multiAccountStoreAccount(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountStoreAccount(json) }, callback)
    }

    @ReactMethod
    fun multiAccountLoadAccount(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountLoadAccount(json) }, callback)
    }

    @ReactMethod
    fun multiAccountDeriveAddresses(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountDeriveAddresses(json) }, callback)
    }

    @ReactMethod
    fun multiAccountGenerateAndDeriveAddresses(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountGenerateAndDeriveAddresses(json) }, callback)
    }

    @ReactMethod
    fun multiAccountStoreDerived(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountStoreDerivedAccounts(json) }, callback)
    }

    @ReactMethod
    fun multiAccountImportMnemonic(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountImportMnemonic(json) }, callback)
    }

    @ReactMethod
    fun multiAccountImportPrivateKey(json: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.multiAccountImportPrivateKey(json) }, callback)
    }

    @ReactMethod
    fun deleteMultiaccount(keyUID: String, callback: Callback) {
        val keyStoreDir = utils.getKeyStorePath(keyUID)
        utils.executeRunnableStatusGoMethod({ Statusgo.deleteMultiaccount(keyUID, keyStoreDir) }, callback)
    }

    @ReactMethod
    fun getRandomMnemonic(callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.getRandomMnemonic() }, callback)
    }

    @ReactMethod
    fun createAccountFromMnemonicAndDeriveAccountsForPaths(mnemonic: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod(
            { Statusgo.createAccountFromMnemonicAndDeriveAccountsForPaths(mnemonic) },
            callback
        )
    }

    companion object {
        private const val TAG = "AccountManager"
        private const val gethLogFileName = "geth.log"

        private fun prettyPrintConfig(config: String) {
            Log.d(TAG, "startNode() with config (see below)")
            var configOutput = config
            val maxOutputLen = 4000
            Log.d(TAG, "********************** NODE CONFIG ****************************")
            while (configOutput.isNotEmpty()) {
                Log.d(TAG, "Node config:${configOutput.take(maxOutputLen)}")
                configOutput = configOutput.drop(maxOutputLen)
            }
            Log.d(TAG, "******************* ENDOF NODE CONFIG *************************")
        }
    }
}
