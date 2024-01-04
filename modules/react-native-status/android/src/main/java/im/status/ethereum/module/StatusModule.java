package im.status.ethereum.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;

import android.preference.PreferenceManager;

import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.NativeViewHierarchyManager;

import statusgo.SignalHandler;
import statusgo.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import android.app.Service;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, SignalHandler {

    private static final String TAG = "StatusModule";
    private static final String gethLogFileName = "geth.log";
    private static StatusModule module;
    private ReactApplicationContext reactContext;
    private boolean rootedDevice;
    private boolean background;
    private AccountManager accountManager;

    StatusModule(ReactApplicationContext reactContext, boolean rootedDevice) {
        super(reactContext);
        this.reactContext = reactContext;
        this.rootedDevice = rootedDevice;
        this.accountManager = new AccountManager(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Status";
    }

    @Override
    public void onHostResume() {  // Activity `onResume`
        module = this;
        this.background = false;
        Statusgo.setMobileSignalHandler(this);
    }

    @Override
    public void onHostPause() {
        this.background = true;
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "******************* ON HOST DESTROY *************************");
    }

    private boolean checkAvailability() {
        // We wait at least 10s for getCurrentActivity to return a value,
        // otherwise we give up
        for (int attempts = 0; attempts < 100; attempts++) {
            if (getCurrentActivity() != null) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                if (getCurrentActivity() != null) {
                    return true;
                }
                Log.d(TAG, "Activity doesn't exist");
                return false;
            }
        }

        Log.d(TAG, "Activity doesn't exist");
        return false;

    }

    public String getNoBackupDirectory() {
        return this.getReactApplicationContext().getNoBackupFilesDir().getAbsolutePath();
    }

    public void handleSignal(final String jsonEventString) {
        Log.d(TAG, "Signal event");
        WritableMap params = Arguments.createMap();
        params.putString("jsonEvent", jsonEventString);
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
    }

    private File getPublicStorageDirectory() {
        final Context context = this.getReactApplicationContext();
        // Environment.getExternalStoragePublicDirectory doesn't work as expected on Android Q
        // https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    private String pathCombine(final String path1, final String path2) {
        // Replace this logic with Paths.get(path1, path2) once API level 26+ becomes the minimum supported API level
        final File file = new File(path1, path2);
        return file.getAbsolutePath();
    }

    private String getKeyUID(final String json) throws JSONException {
        final JSONObject jsonObj = new JSONObject(json);
        return jsonObj.getString("key-uid");
    }

    @ReactMethod
    public void loginWithConfig(final String accountData, final String password, final String configJSON) {
        Log.d(TAG, "loginWithConfig");
        this.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.loginWithConfig(accountData, password, configJSON);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithConfig result: " + result);
        } else {
            Log.e(TAG, "LoginWithConfig failed: " + result);
        }
    }

    @ReactMethod
    public void loginAccount(final String request) {
        Log.d(TAG, "loginAccount");
        String result = Statusgo.loginAccount(request);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "loginAccount result: " + result);
        } else {
            Log.e(TAG, "loginAccount failed: " + result);
        }
    }

    private void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    @ReactMethod
    private void openAccounts(final Callback callback) {
        Activity currentActivity = getCurrentActivity();

        final String rootDir = this.getNoBackupDirectory();
        Log.d(TAG, "openAccounts");
        if (!checkAvailability()) {
            Log.e(TAG, "[openAccounts] Activity doesn't exist, cannot call openAccounts");
            System.exit(0);
            return;
        }

        Log.d(TAG, "[Opening accounts" + rootDir);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.openAccounts(rootDir);
                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    private void executeRunnableStatusGoMethod(Supplier<String> method, Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = () -> {
            String res = method.get();
            callback.invoke(res);
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void verify(final String address, final String password, final Callback callback) throws JSONException {
        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = this.getNoBackupDirectory();
        final String newKeystoreDir = pathCombine(absRootDirPath, "keystore");

        executeRunnableStatusGoMethod(() -> Statusgo.verifyAccountPassword(newKeystoreDir, address, password), callback);
    }

    @ReactMethod
    public void verifyDatabasePassword(final String keyUID, final String password, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.verifyDatabasePassword(keyUID, password), callback);
    }

    public String getKeyStorePath(String keyUID) {
        final String commonKeydir = pathCombine(this.getNoBackupDirectory(), "/keystore");
        final String keydir = pathCombine(commonKeydir, keyUID);

        return keydir;
    }

    public void migrateKeyStoreDir(final String accountData, final String password) {
        try {
            final String commonKeydir = pathCombine(this.getNoBackupDirectory(), "/keystore");
            final String keydir = this.getKeyStorePath(this.getKeyUID(accountData));
            Log.d(TAG, "before migrateKeyStoreDir " + keydir);

            File keydirFile = new File(keydir);
            if(!keydirFile.exists() || keydirFile.list().length == 0) {
                Log.d(TAG, "migrateKeyStoreDir");
                Statusgo.migrateKeyStoreDir(accountData, password, commonKeydir, keydir);
                Statusgo.initKeystore(keydir);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    @ReactMethod
    public void addPeer(final String enode, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.addPeer(enode), callback);
    }

    @ReactMethod
    public void multiAccountStoreAccount(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountStoreAccount(json), callback);
    }

    @ReactMethod
    public void multiAccountLoadAccount(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountLoadAccount(json), callback);
    }

    @ReactMethod
    public void multiAccountReset(final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountReset(), callback);

    }

    @ReactMethod
    public void multiAccountDeriveAddresses(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountDeriveAddresses(json), callback);
    }

    @ReactMethod
    public void multiAccountGenerateAndDeriveAddresses(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountGenerateAndDeriveAddresses(json), callback);
    }

    @ReactMethod
    public void multiAccountStoreDerived(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountStoreDerivedAccounts(json), callback);
    }

    @ReactMethod
    public void multiAccountImportMnemonic(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountImportMnemonic(json), callback);
    }

    @ReactMethod
    public void multiAccountImportPrivateKey(final String json, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.multiAccountImportPrivateKey(json), callback);
    }

    @ReactMethod
    public void hashTransaction(final String txArgsJSON, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.hashTransaction(txArgsJSON), callback);
    }

    @ReactMethod
    public void hashMessage(final String message, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.hashMessage(message), callback);
    }

    @ReactMethod
    public void startSearchForLocalPairingPeers(final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.startSearchForLocalPairingPeers(), callback);
    }

    @ReactMethod
    public void getConnectionStringForBootstrappingAnotherDevice(final String configJSON, final Callback callback) throws JSONException {
         final JSONObject jsonConfig = new JSONObject(configJSON);
         final JSONObject senderConfig = jsonConfig.getJSONObject("senderConfig");
         final String keyUID = senderConfig.getString("keyUID");
         final String keyStorePath = this.getKeyStorePath(keyUID);
        senderConfig.put("keystorePath", keyStorePath);

        executeRunnableStatusGoMethod(() -> Statusgo.getConnectionStringForBootstrappingAnotherDevice(jsonConfig.toString()), callback);
    }

    @ReactMethod
    public void inputConnectionStringForBootstrapping(final String connectionString, final String configJSON, final Callback callback) throws JSONException {
         final JSONObject jsonConfig = new JSONObject(configJSON);
         final JSONObject receiverConfig = jsonConfig.getJSONObject("receiverConfig");
         final String keyStorePath = pathCombine(this.getNoBackupDirectory(), "/keystore");
         receiverConfig.put("keystorePath", keyStorePath);
         receiverConfig.getJSONObject("nodeConfig").put("rootDataDir", this.getNoBackupDirectory());
        executeRunnableStatusGoMethod(() -> Statusgo.inputConnectionStringForBootstrapping(connectionString, jsonConfig.toString()), callback);
    }

    @ReactMethod
    public void multiformatSerializePublicKey(final String multiCodecKey, final String base58btc, final Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiformatSerializePublicKey(multiCodecKey,base58btc);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void multiformatDeserializePublicKey(final String multiCodecKey, final String base58btc, final Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiformatDeserializePublicKey(multiCodecKey,base58btc);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void compressPublicKey(final String multiCodecKey, final Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.compressPublicKey(multiCodecKey);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void decompressPublicKey(final String multiCodecKey, final Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.decompressPublicKey(multiCodecKey);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void deserializeAndCompressKey(final String desktopKey, final Callback callback) throws JSONException {
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.deserializeAndCompressKey(desktopKey);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(runnableTask);
    }

    @ReactMethod
    public void hashTypedData(final String data, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.hashTypedData(data), callback);
    }

    @ReactMethod
    public void hashTypedDataV4(final String data, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.hashTypedDataV4(data), callback);
    }

    @ReactMethod
    public void sendTransactionWithSignature(final String txArgsJSON, final String signature, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.sendTransactionWithSignature(txArgsJSON, signature), callback);
    }

    @ReactMethod
    public void sendTransaction(final String txArgsJSON, final String password, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.sendTransaction(txArgsJSON, password), callback);
    }

    @ReactMethod
    public void signMessage(final String rpcParams, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.signMessage(rpcParams), callback);
    }

    @ReactMethod
    public void recover(final String rpcParams, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.recover(rpcParams), callback);
    }

    @ReactMethod
    public void signTypedData(final String data, final String account, final String password, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.signTypedData(data, account, password), callback);
    }

    @ReactMethod
    public void signTypedDataV4(final String data, final String account, final String password, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.signTypedDataV4(data, account, password), callback);

    }

    @ReactMethod
    public void callRPC(final String payload, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.callRPC(payload), callback);
    }

    @ReactMethod
    public void callPrivateRPC(final String payload, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.callPrivateRPC(payload), callback);
    }

    @ReactMethod
    public void closeApplication() {
        System.exit(0);
    }

    @ReactMethod
    public void connectionChange(final String type, final boolean isExpensive) {
        Log.d(TAG, "ConnectionChange: " + type + ", is expensive " + isExpensive);
        Statusgo.connectionChange(type, isExpensive ? 1 : 0);
    }

    @ReactMethod
    public void appStateChange(final String type) {
        Log.d(TAG, "AppStateChange: " + type);
        Statusgo.appStateChange(type);
    }

    @ReactMethod
    public void stopLocalNotifications() {
        Log.d(TAG, "stopLocalNotifications");
        Statusgo.stopLocalNotifications();
    }

    @ReactMethod
    public void startLocalNotifications() {
        Log.d(TAG, "startLocalNotifications");
        Statusgo.startLocalNotifications();
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

    private Boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this.reactContext.getApplicationContext());
    }

    @ReactMethod
    public void extractGroupMembershipSignatures(final String signaturePairs, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.extractGroupMembershipSignatures(signaturePairs), callback);
    }

    @ReactMethod
    public void signGroupMembership(final String content, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.signGroupMembership(content), callback);
    }

    @ReactMethod
    public void getNodeConfig(final Callback callback) throws JSONException {
         executeRunnableStatusGoMethod(() -> Statusgo.getNodeConfig(), callback);
    }

    @ReactMethod
    public void deleteMultiaccount(final String keyUID, final Callback callback) throws JSONException {
        final String keyStoreDir = this.getKeyStorePath(keyUID);
        executeRunnableStatusGoMethod(() -> Statusgo.deleteMultiaccount(keyUID, keyStoreDir), callback);
    }

    @ReactMethod
    public void deleteImportedKey(final String keyUID, final String address, final String password, final Callback callback) throws JSONException {
        final String keyStoreDir = this.getKeyStorePath(keyUID);
        executeRunnableStatusGoMethod(() -> Statusgo.deleteImportedKey(address, password, keyStoreDir), callback);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String keystoreDir() {
        final String absRootDirPath = this.getNoBackupDirectory();
        return pathCombine(absRootDirPath, "keystore");
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String fleets() {
        return Statusgo.fleets();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String backupDisabledDataDir() {
        return this.getNoBackupDirectory();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String encodeTransfer(final String to, final String value) {
        return Statusgo.encodeTransfer(to, value);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String encodeFunctionCall(final String method, final String paramsJSON) {
        return Statusgo.encodeFunctionCall(method, paramsJSON);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String decodeParameters(final String decodeParamJSON) {
        return Statusgo.decodeParameters(decodeParamJSON);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String hexToNumber(final String hex) {
        return Statusgo.hexToNumber(hex);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String numberToHex(final String numString) {
        return Statusgo.numberToHex(numString);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String sha3(final String str) {
        return Statusgo.sha3(str);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String utf8ToHex(final String str) {
        return Statusgo.utf8ToHex(str);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String hexToUtf8(final String str) {
        return Statusgo.hexToUtf8(str);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String checkAddressChecksum(final String address) {
        return Statusgo.checkAddressChecksum(address);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String isAddress(final String address) {
        return Statusgo.isAddress(address);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String toChecksumAddress(final String address) {
        return Statusgo.toChecksumAddress(address);
    }

    @ReactMethod
    public void identiconAsync(final String seed, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.identicon(seed), callback);
    }

    @Override
    public @Nullable
    Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();

        constants.put("is24Hour", this.is24Hour());
        constants.put("model", Build.MODEL);
        constants.put("brand", Build.BRAND);
        constants.put("buildId", Build.ID);
        constants.put("deviceId", Build.BOARD);
        return constants;
    }

    @ReactMethod
    public void isDeviceRooted(final Callback callback) {
        callback.invoke(rootedDevice);
    }

    @ReactMethod
    public void validateMnemonic(final String seed, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.validateMnemonic(seed), callback);
    }

    @ReactMethod
    public void activateKeepAwake() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void deactivateKeepAwake() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void resetKeyboardInputCursor(final int reactTagToReset, final int selection) {
        UIManagerModule uiManager = getReactApplicationContext().getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            @Override
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                InputMethodManager imm = (InputMethodManager) getReactApplicationContext().getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    View viewToReset = nativeViewHierarchyManager.resolveView(reactTagToReset);
                    imm.restartInput(viewToReset);
                    try {
                      EditText textView = (EditText) viewToReset;
                      textView.setSelection(selection);
                    } catch (Exception e) {}
                }
            }
        });
    }



}

