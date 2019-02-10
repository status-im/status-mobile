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
import android.support.v4.content.FileProvider;
import android.util.Log;
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
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.github.status_im.status_go.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, StatusNodeEventHandler {

    private static final String TAG = "StatusModule";
    private static final String logsZipFileName = "Status-debug-logs.zip";
    private static final String gethLogFileName = "geth.log";
    private static final String statusLogFileName = "Status.log";

    private static StatusModule module;
    private ReactApplicationContext reactContext;
    private boolean rootedDevice;

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
        StatusService.INSTANCE.setSignalEventListener(this);
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }

    private boolean checkAvailability() {
        if (getCurrentActivity() != null) {
            return true;
        }

        Log.d(TAG, "Activity doesn't exist");
        return false;

    }

    @Override
    public void handleEvent(String jsonEvent) {
        Log.d(TAG, "[handleEvent] event: " + jsonEvent);
        WritableMap params = Arguments.createMap();
        params.putString("jsonEvent", jsonEvent);
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
    }

    private File getLogsFile() {
        final File pubDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final File logFile = new File(pubDirectory, gethLogFileName);

        return logFile;
    }

    private String prepareLogsFile(final Context context) {
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

            return gethLogFilePath;
        } catch (Exception e) {
            Log.d(TAG, "Can't create geth.log file! " + e.getMessage());
        }

        return null;
    }

    private String updateConfig(final String jsonConfigString, final String absRootDirPath, final String absKeystoreDirPath) throws JSONException {
        final JSONObject jsonConfig = new JSONObject(jsonConfigString);
        // retrieve parameters from app config, that will be applied onto the Go-side config later on
        final String absDataDirPath = pathCombine(absRootDirPath, jsonConfig.getString("DataDir"));
        final Boolean logEnabled = jsonConfig.getBoolean("LogEnabled");
        final Context context = this.getReactApplicationContext();
        final String gethLogFilePath = logEnabled ? prepareLogsFile(context) : null;

        jsonConfig.put("DataDir", absDataDirPath);
        jsonConfig.put("KeyStoreDir", absKeystoreDirPath);
        jsonConfig.put("LogFile", gethLogFilePath);

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

    private void doStartNode(final String jsonConfigString) {

        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = currentActivity.getApplicationInfo().dataDir;
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
            final String updatedJsonConfigString = this.updateConfig(jsonConfigString, absRootDirPath, newKeystoreDir);

            prettyPrintConfig(updatedJsonConfigString);

            String res = Statusgo.StartNode(updatedJsonConfigString);
            if (res.startsWith("{\"error\":\"\"")) {
                Log.d(TAG, "StartNode result: " + res);
                Log.d(TAG, "Geth node started");
            }
            else {
                Log.e(TAG, "StartNode failed: " + res);
            }
        } catch (JSONException e) {
            Log.e(TAG, "updateConfig failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private String getOldExternalDir() {
        File extStore = Environment.getExternalStorageDirectory();
        return extStore.exists() ? pathCombine(extStore.getAbsolutePath(), "ethereum/testnet") : getNewInternalDir();
    }

    private String getNewInternalDir() {
        Activity currentActivity = getCurrentActivity();
        return pathCombine(currentActivity.getApplicationInfo().dataDir, "ethereum/testnet");
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
    public void startNode(final String config) {
        Log.d(TAG, "startNode");
        if (!checkAvailability()) {
            Log.e(TAG, "[startNode] Activity doesn't exist, cannot start node");
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                doStartNode(config);
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
    public void verify(final String address, final String password, final Callback callback) {
        Log.d(TAG, "verify");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = currentActivity.getApplicationInfo().dataDir;
        final String newKeystoreDir = pathCombine(absRootDirPath, "keystore");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.VerifyAccountPassword(newKeystoreDir, address, password);

                callback.invoke(result);
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void loginWithKeycard(final String whisperPrivateKey, final String encryptionPublicKey, final Callback callback) {
        Log.d(TAG, "loginWithKeycard");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                  String result = Statusgo.LoginWithKeycard(whisperPrivateKey, encryptionPublicKey);

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
    public void sendDataNotification(final String dataPayloadJSON, final String tokensJSON, final Callback callback) {
        Log.d(TAG, "sendDataNotification");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Runnable r = new Runnable() {
                @Override
                public void run() {
                    String res = Statusgo.SendDataNotification(dataPayloadJSON, tokensJSON);

                    callback.invoke(res);
                }
            };

        StatusThreadPoolExecutor.getInstance().execute(r);
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
    public void sendLogs(final String dbJson) {
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
        }
        catch (IOException e) {
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
            final Boolean zipped = zip(new File[] {dbFile, gethLogFile, statusLogFile}, zipFile, errorList);
            if (zipped && zipFile.exists()) {
                Log.d(TAG, "Sending " + zipFile.getAbsolutePath() + " file through share intent");

                final String providerName = context.getPackageName() + ".provider";
                final Activity activity = getCurrentActivity();
                zipFile.setReadable(true, false);
                final Uri dbJsonURI = FileProvider.getUriForFile(activity, providerName, zipFile);

                Intent intentShareFile = new Intent(Intent.ACTION_SEND);

                intentShareFile.setType("application/json");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, dbJsonURI);

                SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormatGmt.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Status.im logs");
                intentShareFile.putExtra(Intent.EXTRA_TEXT,
                    String.format("Logs from %s GMT\n\nThese logs have been generated automatically by the user's request for debugging purposes.\n\n%s",
                                  dateFormatGmt.format(new java.util.Date()),
                                  errorList));

                activity.startActivity(Intent.createChooser(intentShareFile, "Share Debug Logs"));
            } else {
                Log.d(TAG, "File " + zipFile.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            showErrorMessage(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
        finally {
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
        String result = Statusgo.ExtractGroupMembershipSignatures(signaturePairs);

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
        String result = Statusgo.SignGroupMembership(content);

        callback.invoke(result);
      }
    };

    StatusThreadPoolExecutor.getInstance().execute(r);
  }

  @ReactMethod
  public void enableInstallation(final String installationId, final Callback callback) {
    Log.d(TAG, "enableInstallation");
    if (!checkAvailability()) {
      callback.invoke(false);
      return;
    }

    Runnable r = new Runnable() {
      @Override
      public void run() {
        String result = Statusgo.EnableInstallation(installationId);

        callback.invoke(result);
      }
    };

    StatusThreadPoolExecutor.getInstance().execute(r);
  }

  @ReactMethod
  public void disableInstallation(final String installationId, final Callback callback) {
    Log.d(TAG, "disableInstallation");
    if (!checkAvailability()) {
      callback.invoke(false);
      return;
    }

    Runnable r = new Runnable() {
      @Override
      public void run() {
        String result = Statusgo.DisableInstallation(installationId);

        callback.invoke(result);
      }
    };

    StatusThreadPoolExecutor.getInstance().execute(r);
  }

  @ReactMethod
  public void updateMailservers(final String enodes, final Callback callback) {
    Log.d(TAG, "updateMailservers");
    if (!checkAvailability()) {
      callback.invoke(false);
      return;
    }

    Runnable r = new Runnable() {
      @Override
      public void run() {
        String res = Statusgo.UpdateMailservers(enodes);

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
    return constants;
  }

  @ReactMethod
  public void isDeviceRooted(final Callback callback) {
    callback.invoke(rootedDevice);
  }
}
