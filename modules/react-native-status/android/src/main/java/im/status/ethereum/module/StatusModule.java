package im.status.ethereum.module;

import android.app.Activity;
import android.net.Uri;
import android.os.*;
import android.view.WindowManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.github.status_im.status_go.cmd.Statusgo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.json.JSONException;
import com.instabug.library.Instabug;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    private static final String TAG = "StatusModule";

    private HashMap<String, Callback> callbacks = new HashMap<>();

    private static StatusModule module;
    private ServiceConnector status = null;
    private ExecutorService executor = null;
    private boolean debug;

    StatusModule(ReactApplicationContext reactContext, boolean debug) {
        super(reactContext);
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        this.debug = debug;
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

    // Geth
    private void signalEvent(String jsonEvent) {
        Log.d(TAG, "Signal event: " + jsonEvent);
        WritableMap params = Arguments.createMap();
        params.putString("jsonEvent", jsonEvent);
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
    }

    private void doStartNode() {

        Activity currentActivity = getCurrentActivity();

        String root = currentActivity.getApplicationInfo().dataDir;
        String dataFolder = root + "/ethereum/testnet";
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

        String config;
        int devCluster = this.debug ? 1 : 0;
        String defaultConfig = Statusgo.GenerateConfig(dataFolder, 3, devCluster);
        try {
            JSONObject jsonConfig = new JSONObject(defaultConfig);
            String gethLogFileName = "geth.log";
            jsonConfig.put("LogEnabled", true);
            jsonConfig.put("LogFile", gethLogFileName);
            jsonConfig.put("LogLevel", "DEBUG");
            JSONObject upstreamConfig = new JSONObject();
            upstreamConfig.put("Enabled", true);
            upstreamConfig.put("URL", "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4");
            jsonConfig.put("UpstreamConfig", upstreamConfig);
            String gethLogPath = dataFolder + "/" + gethLogFileName;
            File logFile = new File(gethLogPath);
            if (logFile.exists()) {
                logFile.delete();
            }
            try {
                logFile.setReadable(true);
                File parent = logFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                logFile.createNewFile();
                logFile.setReadable(true);
            } catch (Exception e) {
                Log.d(TAG, "Can't create geth.log file!");
            }
            Uri gethLogUri = Uri.fromFile(logFile);
            try {
                Log.d(TAG, "Attach to geth.log to instabug " + gethLogUri.getPath());
                Instabug.setFileAttachment(gethLogUri, gethLogFileName);
            } catch (NullPointerException e) {
                Log.d(TAG, "Instabug is not initialized!");
            }

            config = jsonConfig.toString();
        } catch (JSONException e) {
            Log.d(TAG, "Something went wrong " + e.getMessage());
            Log.d(TAG, "Default configuration will be used");

            config = defaultConfig;
        }

        Statusgo.StartNode(config);
        Log.d(TAG, "Geth node started");
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
    public void startNode(Callback callback) {
        Log.d(TAG, "startNode");
        status.sendMessage();
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                doStartNode();
            }
        };

        thread.start();

        callback.invoke(false);
    }

    @ReactMethod
    public void startNodeRPCServer() {
        Log.d(TAG, "startNodeRPCServer");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Statusgo.StartNodeRPCServer();
            }
        };

        thread.start();
    }

    @ReactMethod
    public void stopNodeRPCServer() {
        Log.d(TAG, "stopNodeRPCServer");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Statusgo.StopNodeRPCServer();
            }
        };

        thread.start();
    }

    @ReactMethod
    public void login(final String address, final String password, final Callback callback) {
        Log.d(TAG, "login");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                String result = Statusgo.Login(address, password);

                callback.invoke(result);
            }
        };

        thread.start();
    }

    @ReactMethod
    public void createAccount(final String password, final Callback callback) {
        Log.d(TAG, "createAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                String res = Statusgo.CreateAccount(password);

                callback.invoke(res);
            }
        };

        thread.start();
    }

    @ReactMethod
    public void recoverAccount(final String passphrase, final String password, final Callback callback) {
        Log.d(TAG, "recoverAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                String res = Statusgo.RecoverAccount(password, passphrase);

                callback.invoke(res);
            }
        };

        thread.start();
    }

    private String createIdentifier() {
        return UUID.randomUUID().toString();
    }

    @ReactMethod
    public void completeTransactions(final String hashes, final String password, final Callback callback) {
        Log.d(TAG, "completeTransactions");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                String res = Statusgo.CompleteTransactions(hashes, password);
                callback.invoke(res);
            }
        };

        thread.start();
    }


    @ReactMethod
    public void discardTransaction(final String id) {
        Log.d(TAG, "discardTransaction");
        if (!checkAvailability()) {
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Statusgo.DiscardTransaction(id);
            }
        };

        thread.start();
    }

    // Jail

    @ReactMethod
    public void initJail(final String js, final Callback callback) {
        Log.d(TAG, "initJail");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Statusgo.InitJail(js);

                callback.invoke(false);
            }
        };

        thread.start();
    }

    @ReactMethod
    public void parseJail(final String chatId, final String js, final Callback callback) {
        Log.d(TAG, "parseJail");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                String res = Statusgo.Parse(chatId, js);
                Log.d(TAG, "endParseJail");
                callback.invoke(res);
            }
        };

        thread.start();
    }

    @ReactMethod
    public void callJail(final String chatId, final String path, final String params, final Callback callback) {
        Log.d(TAG, "callJail");
        Log.d(TAG, path);
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "startCallJail");
                String res = Statusgo.Call(chatId, path, params);
                Log.d(TAG, "endCallJail");
                callback.invoke(res);
            }
        });
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
    public void sendWeb3Request(final String host, final String payload, final Callback callback) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                String res = Statusgo.CallRPC(payload);
                callback.invoke(res);
            }
        };

        thread.start();
    }
}
