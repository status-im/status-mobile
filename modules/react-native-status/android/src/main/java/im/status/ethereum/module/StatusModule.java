package im.status.ethereum.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import android.app.Service;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, SignalHandler {

    private static final String TAG = "StatusModule";
    private static final String logsZipFileName = "Status-debug-logs.zip";
    private static final String gethLogFileName = "geth.log";
    private static final String statusLogFileName = "Status.log";
    private static StatusModule module;
    private ReactApplicationContext reactContext;
    private boolean rootedDevice;
    private NewMessageSignalHandler newMessageSignalHandler;
    private boolean background;

    StatusModule(ReactApplicationContext reactContext, boolean rootedDevice) {
        super(reactContext);
        this.reactContext = reactContext;
        this.rootedDevice = rootedDevice;
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

    @ReactMethod
    public void enableNotifications() {
        this.newMessageSignalHandler = new NewMessageSignalHandler(reactContext);
    }

    @ReactMethod
    public void disableNotifications() {
        if (newMessageSignalHandler != null) {
            newMessageSignalHandler.stop();
            newMessageSignalHandler = null;
        }
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
        try {
            final JSONObject jsonEvent = new JSONObject(jsonEventString);
            String eventType = jsonEvent.getString("type");
            Log.d(TAG, "Signal event: " + jsonEventString);
            // NOTE: the newMessageSignalHandler is only instanciated if the user
            // enabled notifications in the app
            if (this.background && newMessageSignalHandler != null) {
                if (eventType.equals("messages.new")) {
                    newMessageSignalHandler.handleNewMessageSignal(jsonEvent);
                }
            }
            WritableMap params = Arguments.createMap();
            params.putString("jsonEvent", jsonEventString);
            this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    private File getLogsFile() {
        final Context context = this.getReactApplicationContext();
        // Environment.getExternalStoragePublicDirectory doesn't work as expected on Android Q
        // https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
        final File pubDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        final File logFile = new File(pubDirectory, gethLogFileName);

        return logFile;
    }

    private File prepareLogsFile(final Context context) {
        final File logFile = getLogsFile();

        try {
            logFile.setReadable(true);
            File parent = logFile.getParentFile();
            if (!parent.canWrite()) {
                return null;
            }
            if (!parent.exists()) {
                parent.mkdirs();
            }
            logFile.createNewFile();
            logFile.setWritable(true);
            Log.d(TAG, "Can write " + logFile.canWrite());
            Uri gethLogUri = Uri.fromFile(logFile);

            String gethLogFilePath = logFile.getAbsolutePath();
            Log.d(TAG, gethLogFilePath);

            return logFile;
        } catch (Exception e) {
            Log.d(TAG, "Can't create geth.log file! " + e.getMessage());
        }

        return null;
    }

    private String updateConfig(final String jsonConfigString, final String absRootDirPath, final String keystoreDirPath) throws JSONException {
        final JSONObject jsonConfig = new JSONObject(jsonConfigString);
        // retrieve parameters from app config, that will be applied onto the Go-side config later on
        final String dataDirPath = jsonConfig.getString("DataDir");
        final Boolean logEnabled = jsonConfig.getBoolean("LogEnabled");
        final Context context = this.getReactApplicationContext();
        final File gethLogFile = logEnabled ? prepareLogsFile(context) : null;
        String gethLogDirPath = null;
        if (gethLogFile != null) {
            gethLogDirPath = gethLogFile.getParent();
        }

        Log.d(TAG, "log dir: " + gethLogDirPath + " log name: " + gethLogFileName);

        jsonConfig.put("DataDir", dataDirPath);
        jsonConfig.put("KeyStoreDir", keystoreDirPath);
        jsonConfig.put("LogDir", gethLogDirPath);
        jsonConfig.put("LogFile", gethLogFileName);

        return jsonConfig.toString();
    }

    private static void prettyPrintConfig(final String config) {
        Log.d(TAG, "startNode() with config (see below)");
        String configOutput = config;
        final int maxOutputLen = 4000;
        Log.d(TAG, "********************** NODE CONFIG ****************************");
        while (!configOutput.isEmpty()) {
            Log.d(TAG, "Node config:" + configOutput.substring(0, Math.min(maxOutputLen, configOutput.length())));
            if (configOutput.length() > maxOutputLen) {
                configOutput = configOutput.substring(maxOutputLen);
            } else {
                break;
            }
        }
        Log.d(TAG, "******************* ENDOF NODE CONFIG *************************");
    }

    private String getTestnetDataDir(final String absRootDirPath) {
        return pathCombine(absRootDirPath, "ethereum/testnet");
    }

    private String pathCombine(final String path1, final String path2) {
        // Replace this logic with Paths.get(path1, path2) once API level 26+ becomes the minimum supported API level
        final File file = new File(path1, path2);
        return file.getAbsolutePath();
    }

    private String prepareDirAndUpdateConfig(final String jsonConfigString, final String keyUID) {

        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = this.getNoBackupDirectory();
        final String dataFolder = this.getTestnetDataDir(absRootDirPath);
        Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);
            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        final String ropstenFlagPath = pathCombine(absRootDirPath, "ropsten_flag");
        final File ropstenFlag = new File(ropstenFlagPath);
        if (!ropstenFlag.exists()) {
            try {
                final String chaindDataFolderPath = pathCombine(dataFolder, "StatusIM/lightchaindata");
                final File lightChainFolder = new File(chaindDataFolderPath);
                if (lightChainFolder.isDirectory()) {
                    String[] children = lightChainFolder.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(lightChainFolder, children[i]).delete();
                    }
                }
                lightChainFolder.delete();
                ropstenFlag.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String testnetDataDir = dataFolder;
        String oldKeystoreDir = pathCombine(testnetDataDir, "keystore");
        String newKeystoreDir = pathCombine(absRootDirPath, "keystore");
        final File oldKeystore = new File(oldKeystoreDir);
        if (oldKeystore.exists()) {
            try {
                final File newKeystore = new File(newKeystoreDir);
                copyDirectory(oldKeystore, newKeystore);

                if (oldKeystore.isDirectory()) {
                    String[] children = oldKeystore.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(oldKeystoreDir, children[i]).delete();
                    }
                }
                oldKeystore.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            final String multiaccountKeystoreDir = pathCombine("/keystore", keyUID);
            final String updatedJsonConfigString = this.updateConfig(jsonConfigString, absRootDirPath, multiaccountKeystoreDir);

            prettyPrintConfig(updatedJsonConfigString);

            return updatedJsonConfigString;
        } catch (JSONException e) {
            Log.e(TAG, "updateConfig failed: " + e.getMessage());
            System.exit(1);

            return "";
        }
    }

    @ReactMethod
    public void prepareDirAndUpdateConfig(final String keyUID, final String config, final Callback callback) {
        Log.d(TAG, "prepareDirAndUpdateConfig");
        String finalConfig = prepareDirAndUpdateConfig(config, keyUID);
        callback.invoke(finalConfig);
    }

    @ReactMethod
    public void saveAccountAndLogin(final String multiaccountData, final String password, final String settings, final String config, final String accountsData) {
        try {
            Log.d(TAG, "saveAccountAndLogin");
            String finalConfig = prepareDirAndUpdateConfig(config, this.getKeyUID(multiaccountData));
            String result = Statusgo.saveAccountAndLogin(multiaccountData, password, settings, finalConfig, accountsData);
            if (result.startsWith("{\"error\":\"\"")) {
                Log.d(TAG, "saveAccountAndLogin result: " + result);
                Log.d(TAG, "Geth node started");
            } else {
                Log.e(TAG, "saveAccountAndLogin failed: " + result);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    @ReactMethod
    public void saveAccountAndLoginWithKeycard(final String multiaccountData, final String password, final String settings, final String config, final String accountsData, final String chatKey) {
        try {
            Log.d(TAG, "saveAccountAndLoginWithKeycard");
            String finalConfig = prepareDirAndUpdateConfig(config, this.getKeyUID(multiaccountData));
            String result = Statusgo.saveAccountAndLoginWithKeycard(multiaccountData, password, settings, finalConfig, accountsData, chatKey);
            if (result.startsWith("{\"error\":\"\"")) {
                Log.d(TAG, "saveAccountAndLoginWithKeycard result: " + result);
                Log.d(TAG, "Geth node started");
            } else {
                Log.e(TAG, "saveAccountAndLoginWithKeycard failed: " + result);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    private String getKeyUID(final String json) throws JSONException {
        final JSONObject jsonObj = new JSONObject(json);
        return jsonObj.getString("key-uid");
    }

    @ReactMethod
    public void login(final String accountData, final String password) {
        Log.d(TAG, "login");
        this.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.login(accountData, password);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "Login result: " + result);
        } else {
            Log.e(TAG, "Login failed: " + result);
        }
    }

    @ReactMethod
    public void logout() {
        Log.d(TAG, "logout");
        disableNotifications();
        String result = Statusgo.logout();
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "Logout result: " + result);
        } else {
            Log.e(TAG, "Logout failed: " + result);
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

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    @ReactMethod
    private void initKeystore(final String keyUID, final Callback callback) {
        Log.d(TAG, "initKeystore");

        Activity currentActivity = getCurrentActivity();

        if (!checkAvailability()) {
            Log.e(TAG, "[initKeystore] Activity doesn't exist, cannot init keystore");
            System.exit(0);
            return;
        }

        final String commonKeydir = pathCombine(this.getNoBackupDirectory(), "/keystore");
        final String keydir = pathCombine(commonKeydir, keyUID);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Statusgo.initKeystore(keydir);
                callback.invoke(true);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
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

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.openAccounts(rootDir);
                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void verify(final String address, final String password, final Callback callback) {
        Log.d(TAG, "verify");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = this.getNoBackupDirectory();
        final String newKeystoreDir = pathCombine(absRootDirPath, "keystore");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.verifyAccountPassword(newKeystoreDir, address, password);

                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
    public void loginWithKeycard(final String accountData, final String password, final String chatKey) {
        Log.d(TAG, "loginWithKeycard");
        this.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.loginWithKeycard(accountData, password, chatKey);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithKeycard result: " + result);
        } else {
            Log.e(TAG, "LoginWithKeycard failed: " + result);
        }
    }

    private Boolean zip(File[] _files, File zipFile, Stack<String> errorList) {
        final int BUFFER = 0x8000;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                final File file = _files[i];
                if (file == null || !file.exists()) {
                    continue;
                }

                Log.v("Compress", "Adding: " + file.getAbsolutePath());
                try {
                    FileInputStream fi = new FileInputStream(file);
                    origin = new BufferedInputStream(fi, BUFFER);

                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    int count;

                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    errorList.push(e.getMessage());
                }
            }

            out.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void dumpAdbLogsTo(final FileOutputStream statusLogStream) throws IOException {
        final String filter = "logcat -d -b main ReactNativeJS:D StatusModule:D StatusService:D StatusNativeLogs:D *:S";
        final java.lang.Process p = Runtime.getRuntime().exec(filter);
        final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
        final java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(statusLogStream));
        String line;
        while ((line = in.readLine()) != null) {
            out.write(line);
            out.newLine();
        }
        out.close();
        in.close();
    }

    private void showErrorMessage(final String message) {
        final Activity activity = getCurrentActivity();

        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @ReactMethod
    public void sendLogs(final String dbJson, final String jsLogs, final Callback callback) {
        Log.d(TAG, "sendLogs");
        if (!checkAvailability()) {
            return;
        }

        final Context context = this.getReactApplicationContext();
        final File logsTempDir = new File(context.getCacheDir(), "logs"); // This path needs to be in sync with android/app/src/main/res/xml/file_provider_paths.xml
        logsTempDir.mkdir();

        final File dbFile = new File(logsTempDir, "db.json");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(dbFile));
            outputStreamWriter.write(dbJson);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            showErrorMessage(e.getLocalizedMessage());
        }

        final File zipFile = new File(logsTempDir, logsZipFileName);
        final File statusLogFile = new File(logsTempDir, statusLogFileName);
        final File gethLogFile = getLogsFile();

        try {
            if (zipFile.exists() || zipFile.createNewFile()) {
                final long usableSpace = zipFile.getUsableSpace();
                if (usableSpace < 20 * 1024 * 1024) {
                    final String message = String.format("Insufficient space available on device (%s) to write logs.\nPlease free up some space.", android.text.format.Formatter.formatShortFileSize(context, usableSpace));
                    Log.e(TAG, message);
                    showErrorMessage(message);
                    return;
                }
            }

            dumpAdbLogsTo(new FileOutputStream(statusLogFile));

            final Stack<String> errorList = new Stack<String>();
            final Boolean zipped = zip(new File[]{dbFile, gethLogFile, statusLogFile}, zipFile, errorList);
            if (zipped && zipFile.exists()) {
                zipFile.setReadable(true, false);
                callback.invoke(zipFile.getAbsolutePath());
            } else {
                Log.d(TAG, "File " + zipFile.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            showErrorMessage(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        } finally {
            dbFile.delete();
            statusLogFile.delete();
            zipFile.deleteOnExit();
        }
    }

    @ReactMethod
    public void addPeer(final String enode, final Callback callback) {
        Log.d(TAG, "addPeer");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.addPeer(enode);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountStoreAccount(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountStoreAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountStoreAccount(json);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountLoadAccount(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountLoadAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountLoadAccount(json);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountReset(final Callback callback) {
        Log.d(TAG, "multiAccountReset");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountReset();

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountDeriveAddresses(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountDeriveAddresses");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountDeriveAddresses(json);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountGenerateAndDeriveAddresses(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountGenerateAndDeriveAddresses");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountGenerateAndDeriveAddresses(json);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountStoreDerived(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountStoreDerived");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountStoreDerivedAccounts(json);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountImportMnemonic(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountImportMnemonic");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountImportMnemonic(json);
                callback.invoke(res);
            }
        };
        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountImportPrivateKey(final String json, final Callback callback) {
        Log.d(TAG, "multiAccountImportPrivateKey");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.multiAccountImportPrivateKey(json);
                callback.invoke(res);
            }
        };
        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void hashTransaction(final String txArgsJSON, final Callback callback) {
        Log.d(TAG, "hashTransaction");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.hashTransaction(txArgsJSON);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void hashMessage(final String message, final Callback callback) {
        Log.d(TAG, "hashMessage");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.hashMessage(message);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void hashTypedData(final String data, final Callback callback) {
        Log.d(TAG, "hashTypedData");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.hashTypedData(data);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }


    @ReactMethod
    public void sendTransactionWithSignature(final String txArgsJSON, final String signature, final Callback callback) {
        Log.d(TAG, "sendTransactionWithSignature");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.sendTransactionWithSignature(txArgsJSON, signature);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void sendTransaction(final String txArgsJSON, final String password, final Callback callback) {
        Log.d(TAG, "sendTransaction");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.sendTransaction(txArgsJSON, password);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void signMessage(final String rpcParams, final Callback callback) {
        Log.d(TAG, "signMessage");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.signMessage(rpcParams);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void signTypedData(final String data, final String account, final String password, final Callback callback) {
        Log.d(TAG, "signTypedData");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.signTypedData(data, account, password);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void setAdjustResize() {
        Log.d(TAG, "setAdjustResize");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });
    }

    @ReactMethod
    public void setAdjustPan() {
        Log.d(TAG, "setAdjustPan");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });
    }

    @ReactMethod
    public void setSoftInputMode(final int mode) {
        Log.d(TAG, "setSoftInputMode");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(mode);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @ReactMethod
    public void clearCookies() {
        Log.d(TAG, "clearCookies");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(activity);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    @ReactMethod
    public void clearStorageAPIs() {
        Log.d(TAG, "clearStorageAPIs");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        WebStorage storage = WebStorage.getInstance();
        if (storage != null) {
            storage.deleteAllData();
        }
    }

    @ReactMethod
    public void callRPC(final String payload, final Callback callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.callRPC(payload);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void callPrivateRPC(final String payload, final Callback callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.callPrivateRPC(payload);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
    public void extractGroupMembershipSignatures(final String signaturePairs, final Callback callback) {
        Log.d(TAG, "extractGroupMembershipSignatures");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.extractGroupMembershipSignatures(signaturePairs);

                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void signGroupMembership(final String content, final Callback callback) {
        Log.d(TAG, "signGroupMembership");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.signGroupMembership(content);

                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }


    @ReactMethod
    public void chaosModeUpdate(final boolean on, final Callback callback) {
        Log.d(TAG, "chaosModeUpdate");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.chaosModeUpdate(on);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void deleteMultiaccount(final String keyUID, final Callback callback) {
        Log.d(TAG, "deleteMultiaccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        final String keyStoreDir = this.getKeyStorePath(keyUID);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.deleteMultiaccount(keyUID, keyStoreDir);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String generateAlias(final String seed) {
        return Statusgo.generateAlias(seed);
    }

    @ReactMethod
    public void generateAliasAsync(final String seed, final Callback callback) {
        Log.d(TAG, "generateAliasAsync");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.generateAlias(seed);

                Log.d(TAG, res);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String identicon(final String seed) {
        return Statusgo.identicon(seed);
    }

    @ReactMethod
    public void identiconAsync(final String seed, final Callback callback) {
        Log.d(TAG, "identiconAsync");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.identicon(seed);

                Log.d(TAG, res);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void generateAliasAndIdenticonAsync(final String seed, final Callback callback) {
        Log.d(TAG, "generateAliasAndIdenticonAsync");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String resIdenticon = Statusgo.identicon(seed);
                String resAlias = Statusgo.generateAlias(seed);

                Log.d(TAG, resIdenticon);
                Log.d(TAG, resAlias);
                callback.invoke(resAlias, resIdenticon);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }


    @ReactMethod
    public void getNodesFromContract(final String rpcEndpoint, final String contractAddress, final Callback callback) {
        Log.d(TAG, "getNodesFromContract");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.getNodesFromContract(rpcEndpoint, contractAddress);

                Log.d(TAG, res);
                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
    public void validateMnemonic(final String seed, final Callback callback) {
        Log.d(TAG, "validateMnemonic");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String resValidateMnemonic = Statusgo.validateMnemonic(seed);

                Log.d(TAG, resValidateMnemonic);
                callback.invoke(resValidateMnemonic);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
}

