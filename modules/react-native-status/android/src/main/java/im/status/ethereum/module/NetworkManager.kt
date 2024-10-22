package im.status.ethereum.module

import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import org.json.JSONException
import org.json.JSONObject
import statusgo.Statusgo

class NetworkManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)

    override fun getName() = "NetworkManager"

    @ReactMethod
    fun startSearchForLocalPairingPeers(callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.startSearchForLocalPairingPeers() }, callback)
    }

    @ReactMethod
    fun getConnectionStringForBootstrappingAnotherDevice(configJSON: String, callback: Callback) {
        val jsonConfig = JSONObject(configJSON)
        val senderConfig = jsonConfig.getJSONObject("senderConfig")
        val keyUID = senderConfig.getString("keyUID")
        val keyStorePath = utils.getKeyStorePath(keyUID)
        senderConfig.put("keystorePath", keyStorePath)

        utils.executeRunnableStatusGoMethod(
            { Statusgo.getConnectionStringForBootstrappingAnotherDevice(jsonConfig.toString()) },
            callback
        )
    }

    @ReactMethod
    fun inputConnectionStringForBootstrapping(connectionString: String, configJSON: String, callback: Callback) {
        val receiverClientConfig = JSONObject(configJSON)
        val params = JSONObject().apply {
            put("connectionString", connectionString)
            put("receiverClientConfig", receiverClientConfig)
        }
        val jsonString = params.toString()
        utils.executeRunnableStatusGoMethod(
            { Statusgo.inputConnectionStringForBootstrappingV2(jsonString) },
            callback
        )
    }

    @ReactMethod
    fun sendTransactionWithSignature(txArgsJSON: String, signature: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod(
            { Statusgo.sendTransactionWithSignature(txArgsJSON, signature) },
            callback
        )
    }

    @ReactMethod
    fun sendTransaction(txArgsJSON: String, password: String, callback: Callback) {
        val jsonParams = JSONObject().apply {
            put("txArgs", txArgsJSON)
            put("password", password)
        }
        val jsonString = jsonParams.toString()
        utils.executeRunnableStatusGoMethod({ Statusgo.sendTransactionV2(jsonString) }, callback)
    }

    @ReactMethod
    fun callRPC(payload: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.callRPC(payload) }, callback)
    }

    @ReactMethod
    fun callPrivateRPC(payload: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.callPrivateRPC(payload) }, callback)
    }

    @ReactMethod
    fun recover(rpcParams: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.recover(rpcParams) }, callback)
    }

    @ReactMethod
    fun getConnectionStringForExportingKeypairsKeystores(configJSON: String, callback: Callback) {
        val jsonConfig = JSONObject(configJSON)
        val senderConfig = jsonConfig.getJSONObject("senderConfig")
        val keyUID = senderConfig.getString("loggedInKeyUid")
        val keyStorePath = utils.getKeyStorePath(keyUID)
        senderConfig.put("keystorePath", keyStorePath)

        utils.executeRunnableStatusGoMethod(
                { Statusgo.getConnectionStringForExportingKeypairsKeystores(jsonConfig.toString()) },
                callback)
    }

    @ReactMethod
    fun inputConnectionStringForImportingKeypairsKeystores(connectionString: String, configJSON: String, callback: Callback) {
        val keystoreFilesReceiverClientConfig = JSONObject(configJSON)
        val receiverConfig = keystoreFilesReceiverClientConfig.getJSONObject("receiverConfig")
        val keyStorePath = utils.pathCombine(utils.getNoBackupDirectory(), "/keystore")
        receiverConfig.put("keystorePath", keyStorePath)

        val params = JSONObject().apply {
            put("connectionString", connectionString)
            put("keystoreFilesReceiverClientConfig", keystoreFilesReceiverClientConfig)
        }

        utils.executeRunnableStatusGoMethod(
                { Statusgo.inputConnectionStringForImportingKeypairsKeystoresV2(params.toString()) },
                callback
        )
    }
}
