package im.status.ethereum.module;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import android.util.Log;
import statusgo.Statusgo;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.function.Function;
import android.app.Activity;
import android.view.WindowManager;
import android.os.Build;
import android.view.Window;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class EncryptionUtils extends ReactContextBaseJavaModule {
    private static final String TAG = "EncryptionUtils";

    private ReactApplicationContext reactContext;
    private Utils utils;

    public EncryptionUtils(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.utils = new Utils(reactContext);
    }

    @Override
    public String getName() {
        return "EncryptionUtils";
    }

    @ReactMethod
    private void initKeystore(final String keyUID, final Callback callback) throws JSONException {
        Log.d(TAG, "initKeystore");

        final String commonKeydir = this.utils.pathCombine(this.utils.getNoBackupDirectory(), "/keystore");
        final String keydir = this.utils.pathCombine(commonKeydir, keyUID);

        StatusBackendClient.executeStatusGoRequestWithCallback(
            "InitKeystore",
            keydir,
            () -> Statusgo.initKeystore(keydir),
            callback
        );
    }

    @ReactMethod
    public void reEncryptDbAndKeystore(final String keyUID, final String password, final String newPassword, final Callback callback) throws JSONException {
        JSONObject params = new JSONObject();
        params.put("keyUID", keyUID);
        params.put("oldPassword", password);
        params.put("newPassword", newPassword);
        String jsonParams = params.toString();
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "ChangeDatabasePasswordV2",
            jsonParams,
            () -> Statusgo.changeDatabasePasswordV2(jsonParams),
            callback
        );
    }

    @ReactMethod
    public void convertToKeycardAccount(final String keyUID, final String accountData, final String options, final String keycardUID, final String password,
                                        final String newPassword, final Callback callback) throws JSONException {
        final String keyStoreDir = this.utils.getKeyStorePath(keyUID);
        JSONObject params = new JSONObject();
        params.put("keyUID", keyUID);
        params.put("account", new JSONObject(accountData));
        params.put("settings", new JSONObject(options));
        params.put("keycardUID", keycardUID);
        params.put("oldPassword", password);
        params.put("newPassword", newPassword);
        final String jsonParams = params.toString();
        StatusBackendClient.executeStatusGoRequest(
            "InitKeystore",
            keyStoreDir,
            () -> Statusgo.initKeystore(keyStoreDir)
        );
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "ConvertToKeycardAccountV2",
            jsonParams,
            () -> Statusgo.convertToKeycardAccountV2(jsonParams),
            callback
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String encodeTransfer(final String to, final String value) {
        try {
            JSONObject params = new JSONObject();
            params.put("to", to);
            params.put("value", value);
            String jsonParams = params.toString();
            return StatusBackendClient.executeStatusGoRequestWithResult(
                "EncodeTransferV2",
                jsonParams,
                () -> Statusgo.encodeTransferV2(jsonParams)
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for encodeTransfer: " + e.getMessage());
            return null;
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String encodeFunctionCall(final String method, final String paramsJSON) {
        try {
            JSONObject params = new JSONObject();
            params.put("method", method);
            params.put("paramsJSON", new JSONObject(paramsJSON));
            String jsonString = params.toString();
            return StatusBackendClient.executeStatusGoRequestWithResult(
                "EncodeFunctionCallV2",
                jsonString,
                () -> Statusgo.encodeFunctionCallV2(jsonString)
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for encodeFunctionCall: " + e.getMessage());
            return null;
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String decodeParameters(final String decodeParamJSON) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "DecodeParameters",
            decodeParamJSON,
            () -> Statusgo.decodeParameters(decodeParamJSON)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String hexToNumber(final String hex) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "HexToNumber",
            hex,
            () -> Statusgo.hexToNumber(hex)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String numberToHex(final String numString) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "NumberToHex",
            numString,
            () -> Statusgo.numberToHex(numString)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String sha3(final String str) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "Sha3",
            str,
            () -> Statusgo.sha3(str)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String utf8ToHex(final String str) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "Utf8ToHex",
            str,
            () -> Statusgo.utf8ToHex(str)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String hexToUtf8(final String str) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "HexToUtf8",
            str,
            () -> Statusgo.hexToUtf8(str)
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String serializeLegacyKey(final String publicKey) {
        return StatusBackendClient.executeStatusGoRequestWithResult(
            "SerializeLegacyKey",
            publicKey,
            () -> Statusgo.serializeLegacyKey(publicKey)
        );
    }

    @ReactMethod
    public void setBlankPreviewFlag(final Boolean blankPreview) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.reactContext);
        sharedPrefs.edit().putBoolean("BLANK_PREVIEW", blankPreview).commit();
        setSecureFlag();
    }

    private void setSecureFlag() {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.reactContext);
        final boolean setSecure = sharedPrefs.getBoolean("BLANK_PREVIEW", true);
        final Activity activity = this.reactContext.getCurrentActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Window window = activity.getWindow();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && setSecure) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                }
            });
        }
    }

    @ReactMethod
    public void hashTransaction(final String txArgsJSON, final Callback callback) throws JSONException {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "HashTransaction",
            txArgsJSON,
            () -> Statusgo.hashTransaction(txArgsJSON),
            callback
        );
    }

    @ReactMethod
    public void hashMessage(final String message, final Callback callback) throws JSONException {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "HashMessage",
            message,
            () -> Statusgo.hashMessage(message),
            callback
        );
    }

    @ReactMethod
    public void multiformatDeserializePublicKey(final String multiCodecKey, final String base58btc, final Callback callback) throws JSONException {
        JSONObject params = new JSONObject();
        params.put("key", multiCodecKey);
        params.put("outBase", base58btc);
        String jsonParams = params.toString();
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "MultiformatDeserializePublicKeyV2",
            jsonParams,
            () -> Statusgo.multiformatDeserializePublicKeyV2(jsonParams),
            callback
        );
    }

    @ReactMethod
    public void deserializeAndCompressKey(final String desktopKey, final Callback callback) throws JSONException {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "DeserializeAndCompressKey",
            desktopKey,
            () -> Statusgo.deserializeAndCompressKey(desktopKey),
            callback
        );
    }

    @ReactMethod
    public void hashTypedData(final String data, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.hashTypedData(data), callback);
    }

    @ReactMethod
    public void hashTypedDataV4(final String data, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.hashTypedDataV4(data), callback);
    }

    @ReactMethod
    public void signMessage(final String rpcParams, final Callback callback) throws JSONException {
        StatusBackendClient.executeStatusGoRequestWithCallback(
            "SignMessage",
            rpcParams,
            () -> Statusgo.signMessage(rpcParams),
            callback
        );
    }

    @ReactMethod
    public void signTypedData(final String data, final String account, final String password, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.signTypedData(data, account, password), callback);
    }

    @ReactMethod
    public void signTypedDataV4(final String data, final String account, final String password, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.signTypedDataV4(data, account, password), callback);

    }

    @ReactMethod
    public void signGroupMembership(final String content, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.signGroupMembership(content), callback);
    }


}
