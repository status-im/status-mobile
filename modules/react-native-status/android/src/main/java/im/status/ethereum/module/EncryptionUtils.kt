package im.status.ethereum.module

import android.app.Activity
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import com.facebook.react.bridge.*
import org.json.JSONException
import org.json.JSONObject
import statusgo.Statusgo
import java.lang.Exception
import java.lang.RuntimeException

class EncryptionUtils(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val utils = Utils(reactContext)
    
    companion object {
        private const val TAG = "EncryptionUtils"
    }

    override fun getName(): String = "EncryptionUtils"

    @ReactMethod
    @Throws(Exception::class)
    private fun initKeystore(keyUID: String, callback: Callback) {
        Log.d(TAG, "initKeystore")
        val commonKeydir = utils.pathCombine(utils.getNoBackupDirectory(), "/keystore")
        val keydir = utils.pathCombine(commonKeydir, keyUID)

        StatusBackendClient.executeStatusGoRequestWithCallback(
            "InitKeystore",
            keydir,
            { Statusgo.initKeystore(keydir) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun reEncryptDbAndKeystore(keyUID: String, password: String, newPassword: String, callback: Callback) {
        val params = JSONObject().apply {
            put("keyUID", keyUID)
            put("oldPassword", password)
            put("newPassword", newPassword)
        }
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "ChangeDatabasePasswordV2",
            params.toString(),
            { Statusgo.changeDatabasePasswordV2(params.toString()) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun convertToKeycardAccount(
        keyUID: String, 
        accountData: String, 
        options: String, 
        keycardUID: String, 
        password: String,
        newPassword: String, 
        callback: Callback
    ) {
        val keyStoreDir = utils.getKeyStorePath(keyUID)
        val params = JSONObject().apply {
            put("keyUID", keyUID)
            put("account", JSONObject(accountData))
            put("settings", JSONObject(options))
            put("keycardUID", keycardUID)
            put("oldPassword", password)
            put("newPassword", newPassword)
        }
        val jsonParams = params.toString()
        
        StatusBackendClient.executeStatusGoRequest(
            "InitKeystore",
            keyStoreDir,
            { Statusgo.initKeystore(keyStoreDir) }
        )
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "ConvertToKeycardAccountV2",
            jsonParams,
            { Statusgo.convertToKeycardAccountV2(jsonParams) },
            callback
        )
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun encodeTransfer(to: String, value: String): String? {
        return try {
            val params = JSONObject().apply {
                put("to", to)
                put("value", value)
            }
            StatusBackendClient.executeStatusGoRequestWithResult(
                "EncodeTransferV2",
                params.toString(),
                { Statusgo.encodeTransferV2(params.toString()) }
            )
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating JSON for encodeTransfer: ${e.message}")
            null
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun encodeFunctionCall(method: String, paramsJSON: String): String? {
        return try {
            val params = JSONObject().apply {
                put("method", method)
                put("paramsJSON", JSONObject(paramsJSON))
            }
            StatusBackendClient.executeStatusGoRequestWithResult(
                "EncodeFunctionCallV2",
                params.toString(),
                { Statusgo.encodeFunctionCallV2(params.toString()) }
            )
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating JSON for encodeFunctionCall: ${e.message}")
            null
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun decodeParameters(decodeParamJSON: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "DecodeParameters",
            decodeParamJSON,
            { Statusgo.decodeParameters(decodeParamJSON) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun hexToNumber(hex: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "HexToNumber",
            hex,
            { Statusgo.hexToNumber(hex) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun numberToHex(numString: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "NumberToHex",
            numString,
            { Statusgo.numberToHex(numString) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun sha3(str: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "Sha3",
            str,
            { Statusgo.sha3(str) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun utf8ToHex(str: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "Utf8ToHex",
            str,
            { Statusgo.utf8ToHex(str) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun hexToUtf8(str: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "HexToUtf8",
            str,
            { Statusgo.hexToUtf8(str) }
        )

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun serializeLegacyKey(publicKey: String): String = 
        StatusBackendClient.executeStatusGoRequestWithResult(
            "SerializeLegacyKey",
            publicKey,
            { Statusgo.serializeLegacyKey(publicKey) }
        )

    @ReactMethod
    fun setBlankPreviewFlag(blankPreview: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(reactContext)
            .edit()
            .putBoolean("BLANK_PREVIEW", blankPreview)
            .commit()
        setSecureFlag()
    }

    private fun setSecureFlag() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(reactContext)
        val setSecure = sharedPrefs.getBoolean("BLANK_PREVIEW", true)
        
        reactContext.currentActivity?.run {
            runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && setSecure) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
    }

    @ReactMethod
    @Throws(Exception::class)
    fun hashTransaction(txArgsJSON: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "HashTransaction",
            txArgsJSON,
            { Statusgo.hashTransaction(txArgsJSON) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun hashMessage(message: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "HashMessage",
            message,
            { Statusgo.hashMessage(message) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun multiformatDeserializePublicKey(multiCodecKey: String, base58btc: String, callback: Callback) {
        val params = JSONObject().apply {
            put("key", multiCodecKey)
            put("outBase", base58btc)
        }
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "MultiformatDeserializePublicKeyV2",
            params.toString(),
            { Statusgo.multiformatDeserializePublicKeyV2(params.toString()) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun deserializeAndCompressKey(desktopKey: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "DeserializeAndCompressKey",
            desktopKey,
            { Statusgo.deserializeAndCompressKey(desktopKey) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun hashTypedData(data: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.hashTypedData(data) }, callback)
    }

    @ReactMethod
    @Throws(Exception::class)
    fun hashTypedDataV4(data: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.hashTypedDataV4(data) }, callback)
    }

    @ReactMethod
    @Throws(Exception::class)
    fun signMessage(rpcParams: String, callback: Callback) {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "SignMessage",
            rpcParams,
            { Statusgo.signMessage(rpcParams) },
            callback
        )
    }

    @ReactMethod
    @Throws(Exception::class)
    fun signTypedData(data: String, account: String, password: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.signTypedData(data, account, password) }, callback)
    }

    @ReactMethod
    @Throws(Exception::class)
    fun signTypedDataV4(data: String, account: String, password: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.signTypedDataV4(data, account, password) }, callback)
    }

    @ReactMethod
    @Throws(Exception::class)
    fun signGroupMembership(content: String, callback: Callback) {
        utils.executeRunnableStatusGoMethod({ Statusgo.signGroupMembership(content) }, callback)
    }
}
