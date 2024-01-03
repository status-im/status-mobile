package im.status.ethereum.module;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import android.util.Log;
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

    private File getPublicStorageDirectory() {
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
}