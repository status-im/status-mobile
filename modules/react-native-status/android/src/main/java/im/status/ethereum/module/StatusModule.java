package im.status.ethereum.module;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.view.WindowManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.github.status_im.status_go.Statusgo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.json.JSONException;
import com.instabug.library.Instabug;

import javax.annotation.Nullable;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    private static final String TAG = "StatusModule";

    private final static int TESTNET_NETWORK_ID = 3;

    private HashMap<String, Callback> callbacks = new HashMap<>();

    private static StatusModule module;
    private ServiceConnector status = null;
    private ExecutorService executor = null;
    private boolean debug;
    private boolean devCluster;
    private String logLevel;
    private ReactApplicationContext reactContext;

    StatusModule(ReactApplicationContext reactContext, boolean debug, boolean devCluster, String logLevel) {
        super(reactContext);
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        this.debug = debug;
        this.devCluster = devCluster;
        this.logLevel = logLevel;
        this.reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Status";
    }

    @Override
    public void onHostResume() {  // Actvity `onResume`
        module = this;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.d(TAG, "On host Activity doesn't exist");
            return;
        }

        if (status == null) {
            status = new ServiceConnector(currentActivity, StatusService.class);
            status.registerHandler(this);
        }

        status.bindService();

        signalEvent("{\"type\":\"module.initialized\"}");
    }

    @Override
    public void onHostPause() {
        if (status != null) {
            status.unbindService();
        }
    }

    @Override
    public void onHostDestroy() {

    }

    private boolean checkAvailability() {

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.d(TAG, "Activity doesn't exist");
            return false;
        }

        return true;
    }


    void signalEvent(String jsonEvent) {
        Log.d(TAG, "Signal event: " + jsonEvent);
        WritableMap params = Arguments.createMap();
        params.putString("jsonEvent", jsonEvent);
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
    }

    private static String prepareLogsFile() {
        String gethLogFileName = "geth.log";
        File pubDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logFile = new File(pubDirectory, gethLogFileName);

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
            try {
                Log.d(TAG, "Attach to geth.log to instabug " + gethLogUri.getPath());
                Instabug.setFileAttachment(gethLogUri, gethLogFileName);
            } catch (NullPointerException e) {
                Log.d(TAG, "Instabug is not initialized!");
            }

            String gethLogFilePath = logFile.getAbsolutePath();
            Log.d(TAG, gethLogFilePath);

            return gethLogFilePath;
        } catch (Exception e) {
            Log.d(TAG, "Can't create geth.log file! " + e.getMessage());
        }

        return null;
    }

    private String generateConfig(final String dataDir, final int networkId, final String keystoreDir, final String fleet, final Object upstreamConfig) throws JSONException {

            JSONObject jsonConfig = new JSONObject(
                    Statusgo.GenerateConfig(dataDir, fleet, networkId));

            jsonConfig.put("NetworkId", networkId);
            jsonConfig.put("KeyStoreDir", keystoreDir);

            if (upstreamConfig != null) {
                Log.d(TAG, "UpstreamConfig is not null");
                jsonConfig.put("UpstreamConfig", upstreamConfig);
            }

            final String gethLogFilePath = TextUtils.isEmpty(this.logLevel) ? null : prepareLogsFile();
            final boolean logsEnabled = (gethLogFilePath != null);

            jsonConfig.put("LogEnabled", logsEnabled);
            jsonConfig.put("LogFile", gethLogFilePath);
            jsonConfig.put("LogLevel", TextUtils.isEmpty(this.logLevel) ? "ERROR" : this.logLevel.toUpperCase());


            // Setting up whisper config
            JSONObject whisperConfig = jsonConfig.optJSONObject("WhisperConfig");
            if (whisperConfig == null) {
                whisperConfig = new JSONObject();
            }
            whisperConfig.put("LightClient", true);
            jsonConfig.put("WhisperConfig", whisperConfig);


            // Setting up cluster config
            JSONObject clusterConfig = jsonConfig.optJSONObject("ClusterConfig");
            if (clusterConfig != null) {
                Log.d(TAG, "ClusterConfig is not null");
                clusterConfig.put("Fleet", fleet);
                jsonConfig.put("ClusterConfig", clusterConfig);
            } else {
                Log.w(TAG, "ClusterConfig: Cannot find ClusterConfig: doesn't exist or not a JSON object");
                Log.w(TAG, "ClusterConfig: Fleet will be set to defaults");
            }

            return jsonConfig.toString();
    }


    private String generateConfigFromDefaultConfig(final String root, final String keystoreDir, final String fleet, final String defaultConfig) {
        try {
            JSONObject customConfig = new JSONObject(defaultConfig);

            // parameters from config
            final String dataDir = root + customConfig.get("DataDir");
            final int networkId = customConfig.getInt("NetworkId");
            final Object upstreamConfig = customConfig.opt("UpstreamConfig");

            return generateConfig(dataDir, networkId, keystoreDir, fleet, upstreamConfig);

        } catch (JSONException e) {
            Log.d(TAG, "Something went wrong " + e.getMessage());
            Log.d(TAG, "Default configuration will be used: ropsten, beta fleet");
            return Statusgo.GenerateConfig(this.getTestnetDataDir(root), "eth.beta", TESTNET_NETWORK_ID);
        }
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

    private String getTestnetDataDir(final String root) {
        return root + "/ethereum/testnet";
    }

    private void doStartNode(final String defaultConfig, final String fleet) {

        Activity currentActivity = getCurrentActivity();

        final String root = currentActivity.getApplicationInfo().dataDir;
        final String dataFolder = this.getTestnetDataDir(root);
        Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);
            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        final String ropstenFlagPath = root + "/ropsten_flag";
        final File ropstenFlag = new File(ropstenFlagPath);
        if (!ropstenFlag.exists()) {
            try {
                final String chaindDataFolderPath = dataFolder + "/StatusIM/lightchaindata";
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
        String oldKeystoreDir = testnetDataDir + "/keystore";
        String newKeystoreDir = root + "/keystore";
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

        final String config = this.generateConfigFromDefaultConfig(root, newKeystoreDir, fleet, defaultConfig);

        prettyPrintConfig(config);

        String res = Statusgo.StartNode(config);
        if (res.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "StartNode result: " + res);
        }
        else {
            Log.e(TAG, "StartNode failed: " + res);
        }
        Log.d(TAG, "Geth node started");
	status.sendMessage();
    }

    private String getOldExternalDir() {
        File extStore = Environment.getExternalStorageDirectory();
        return extStore.exists() ? extStore.getAbsolutePath() + "/ethereum/testnet" : getNewInternalDir();
    }

    private String getNewInternalDir() {
        Activity currentActivity = getCurrentActivity();
        return currentActivity.getApplicationInfo().dataDir + "/ethereum/testnet";
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
    public void shouldMoveToInternalStorage(Callback callback) {
        String oldDir = getOldExternalDir();
        String newDir = getNewInternalDir();

        File oldDirFile = new File(oldDir);
        File newDirFile = new File(newDir);

        callback.invoke(oldDirFile.exists() && !newDirFile.exists());
    }

    @ReactMethod
    public void moveToInternalStorage(Callback callback) {
        String oldDir = getOldExternalDir();
        String newDir = getNewInternalDir();

        try {
            File oldDirFile = new File(oldDir);
            copyDirectory(oldDirFile, new File(newDir));
            deleteDirectory(oldDirFile);
        } catch (IOException e) {
            Log.d(TAG, "Moving error: " + e);
        }

        callback.invoke();
    }

    @ReactMethod
    public void startNode(final String config, final String fleet) {
        Log.d(TAG, "startNode");
        if (!checkAvailability()) {
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                doStartNode(config, fleet);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void stopNode() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "stopNode");
                String res = Statusgo.StopNode();
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void login(final String address, final String password, final Callback callback) {
        Log.d(TAG, "login");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.Login(address, password);

                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void createAccount(final String password, final Callback callback) {
        Log.d(TAG, "createAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.CreateAccount(password);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void notifyUsers(final String message, final String payloadJSON, final String tokensJSON, final Callback callback) {
        Log.d(TAG, "notifyUsers");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
                @Override
                public void run() {
                    String res = Statusgo.NotifyUsers(message, payloadJSON, tokensJSON);

                    callback.invoke(res);
                }
            };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
                    String res = Statusgo.AddPeer(enode);

                    callback.invoke(res);
                }
            };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }


    @ReactMethod
    public void recoverAccount(final String passphrase, final String password, final Callback callback) {
        Log.d(TAG, "recoverAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.RecoverAccount(password, passphrase);

                callback.invoke(res);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    private String createIdentifier() {
        return UUID.randomUUID().toString();
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
                String res = Statusgo.SendTransaction(txArgsJSON, password);
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
                String res = Statusgo.SignMessage(rpcParams);
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

    @Override
    public boolean handleMessage(Message message) {

        Log.d(TAG, "Received message: " + message.toString());
        Bundle bundle = message.getData();

        String event = bundle.getString("event");
        signalEvent(event);

        return true;
    }

    @Override
    public void onConnectorConnected() {

    }

    @Override
    public void onConnectorDisconnected() {

    }

    @ReactMethod
    public void callRPC(final String payload, final Callback callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String res = Statusgo.CallRPC(payload);
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
                String res = Statusgo.CallPrivateRPC(payload);
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
        Statusgo.ConnectionChange(type, isExpensive ? 1 : 0);
    }

    @ReactMethod
    public void appStateChange(final String type) {
        Log.d(TAG, "AppStateChange: " + type);
        Statusgo.AppStateChange(type);
    }
    
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    @ReactMethod
    public void getDeviceUUID(final Callback callback) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = this.getReactApplicationContext().getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        callback.invoke(uniqueID);
    }

  private Boolean is24Hour() {
    return android.text.format.DateFormat.is24HourFormat(this.reactContext.getApplicationContext());
  }

  @Override
  public @Nullable
  Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<String, Object>();

    constants.put("is24Hour", this.is24Hour());
    return constants;
  }
}
