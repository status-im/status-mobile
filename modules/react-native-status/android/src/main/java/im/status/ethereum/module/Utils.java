package im.status.ethereum.module;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import android.util.Log;
import java.util.function.Supplier;
import java.io.File;
import android.content.Context;
import android.os.Environment;
import org.json.JSONObject;
import org.json.JSONException;
import statusgo.Statusgo;

public class Utils extends ReactContextBaseJavaModule  {
    private static final String gethLogFileName = "geth.log";

    private static final String TAG = "Utils";
    private ReactApplicationContext reactContext;

    public Utils(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Utils";
    }
    public String getNoBackupDirectory() {
        return this.getReactApplicationContext().getNoBackupFilesDir().getAbsolutePath();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String backupDisabledDataDir() {
        return getNoBackupDirectory();
    }

    public File getPublicStorageDirectory() {
        final Context context = this.getReactApplicationContext();
        // Environment.getExternalStoragePublicDirectory doesn't work as expected on Android Q
        // https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    public File getLogsFile() {
        final File pubDirectory = getPublicStorageDirectory();
        final File logFile = new File(pubDirectory, gethLogFileName);

        return logFile;
    }

    public String getKeyUID(final String json) throws JSONException {
        final JSONObject jsonObj = new JSONObject(json);
        return jsonObj.getString("key-uid");
    }

    public String pathCombine(final String path1, final String path2) {
        // Replace this logic with Paths.get(path1, path2) once API level 26+ becomes the minimum supported API level
        final File file = new File(path1, path2);
        return file.getAbsolutePath();
    }

    public String getKeyStorePath(String keyUID) {
        final String commonKeydir = pathCombine(getNoBackupDirectory(), "/keystore");
        final String keydir = pathCombine(commonKeydir, keyUID);

        return keydir;
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String keystoreDir() {
        final String absRootDirPath = getNoBackupDirectory();
        return pathCombine(absRootDirPath, "keystore");
    }

    public void migrateKeyStoreDir(final String accountData, final String password) {
        try {
            final String commonKeydir = pathCombine(getNoBackupDirectory(), "/keystore");
            final String keydir = getKeyStorePath(getKeyUID(accountData));
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

    public boolean checkAvailability() {
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

    public void executeRunnableStatusGoMethod(Supplier<String> method, Callback callback) throws JSONException {
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
    public void validateMnemonic(final String seed, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.validateMnemonic(seed), callback);
    }

    public Boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this.reactContext.getApplicationContext());
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

}