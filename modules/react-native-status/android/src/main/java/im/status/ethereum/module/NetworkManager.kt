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
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "StartSearchForLocalPairingPeers",
            requestBody = "",
            statusgoFunction = { Statusgo.startSearchForLocalPairingPeers() },
            callback = callback
        )
    }

    @ReactMethod
    fun getConnectionStringForBootstrappingAnotherDevice(configJSON: String, callback: Callback) {
        val jsonConfig = JSONObject(configJSON)
        val senderConfig = jsonConfig.getJSONObject("senderConfig")
        val keyUID = senderConfig.getString("keyUID")
        val keyStorePath = utils.getKeyStorePath(keyUID)
        senderConfig.put("keystorePath", keyStorePath)
        val jsonString = jsonConfig.toString()

        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "GetConnectionStringForBootstrappingAnotherDevice",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.getConnectionStringForBootstrappingAnotherDevice(jsonString) },
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
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "InputConnectionStringForBootstrappingV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.inputConnectionStringForBootstrappingV2(jsonString) },
            callback
        )
    }

    @ReactMethod
    fun sendTransactionWithSignature(txArgsJSON: String, signature: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "SendTransactionWithSignature",
            requestBody = txArgsJSON,
            statusgoFunction = { Statusgo.sendTransactionWithSignature(txArgsJSON, signature) },
            callback
        )
    }

    @ReactMethod
    fun sendTransaction(txArgsJSON: String, password: String, callback: Callback) {
        val jsonParams = JSONObject().apply {
            put("txArgs", JSONObject(txArgsJSON))
            put("password", password)
        }
        val jsonString = jsonParams.toString()
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "SendTransactionV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.sendTransactionV2(jsonString) },
            callback
        )
    }

    @ReactMethod
    fun callRPC(payload: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "CallRPC",
            requestBody = payload,
            statusgoFunction = { Statusgo.callRPC(payload) },
            callback = callback
        )
    }

    @ReactMethod
    fun callPrivateRPC(payload: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "CallPrivateRPC",
            requestBody = payload,
            statusgoFunction = { Statusgo.callPrivateRPC(payload) },
            callback = callback
        )
    }

    @ReactMethod
    fun recover(rpcParams: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "Recover",
            requestBody = rpcParams,
            statusgoFunction = { Statusgo.recover(rpcParams) },
            callback
        )
    }

    @ReactMethod
    fun getConnectionStringForExportingKeypairsKeystores(configJSON: String, callback: Callback) {
        val jsonConfig = JSONObject(configJSON)
        val senderConfig = jsonConfig.getJSONObject("senderConfig")
        val keyUID = senderConfig.getString("loggedInKeyUid")
        val keyStorePath = utils.getKeyStorePath(keyUID)
        senderConfig.put("keystorePath", keyStorePath)
        val jsonString = jsonConfig.toString()

        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "GetConnectionStringForExportingKeypairsKeystores",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.getConnectionStringForExportingKeypairsKeystores(jsonString) },
            callback = callback
        )
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
        val jsonString = params.toString()
        StatusBackendClient.executeStatusGoRequestWithCallback(
            endpoint = "InputConnectionStringForImportingKeypairsKeystoresV2",
            requestBody = jsonString,
            statusgoFunction = { Statusgo.inputConnectionStringForImportingKeypairsKeystoresV2(jsonString) },
            callback = callback
        )
    }
}
