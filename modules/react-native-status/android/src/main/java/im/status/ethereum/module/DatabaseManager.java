package im.status.ethereum.module;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import statusgo.Statusgo;
import android.util.Log;
import java.io.File;
import android.os.Environment;
import android.content.Context;
public class DatabaseManager extends ReactContextBaseJavaModule {
    private static final String TAG = "DatabaseManager";
    private ReactApplicationContext reactContext;
    private static final String exportDBFileName = "export.db";
    private Utils utils;
    public DatabaseManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.utils = new Utils(reactContext);
    }
    @Override
    public String getName() {
        return "DatabaseManager";
    }

    private File getExportDBFile() {
        final Context context = this.getReactApplicationContext();
        // Environment.getExternalStoragePublicDirectory doesn't work as expected on Android Q
        // https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
        final File pubDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        final File filename = new File(pubDirectory, exportDBFileName);

        return filename;
    }

    @ReactMethod
    public void exportUnencryptedDatabase(final String accountData, final String password, final Callback callback) {
        Log.d(TAG, "login");

        final File newFile = getExportDBFile();

        this.utils.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.exportUnencryptedDatabase(accountData, password, newFile.getAbsolutePath());
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "Login result: " + result);
        } else {
            Log.e(TAG, "Login failed: " + result);
        }
    }

    @ReactMethod
    public void importUnencryptedDatabase(final String accountData, final String password) {
        Log.d(TAG, "importUnencryptedDatabase");

        final File newFile = getExportDBFile();

        this.utils.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.importUnencryptedDatabase(accountData, password, newFile.getAbsolutePath());
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "import result: " + result);
        } else {
            Log.e(TAG, "import failed: " + result);
        }
    }

}
