package im.status.ethereum.module;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import android.util.Log;
import statusgo.Statusgo;
import org.json.JSONException;
import java.util.function.Function;
import java.util.function.Supplier;
import android.app.Activity;

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

    //TODO: remove duplicate checkAvailability
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
    private void initKeystore(final String keyUID, final Callback callback) throws JSONException {
        Log.d(TAG, "initKeystore");

        final String commonKeydir = this.utils.pathCombine(this.utils.getNoBackupDirectory(), "/keystore");
        final String keydir = this.utils.pathCombine(commonKeydir, keyUID);

        executeRunnableStatusGoMethod(() -> Statusgo.initKeystore(keydir), callback);
    }

    @ReactMethod
    public void reEncryptDbAndKeystore(final String keyUID, final String password, final String newPassword, final Callback callback) throws JSONException {
        executeRunnableStatusGoMethod(() -> Statusgo.changeDatabasePassword(keyUID, password, newPassword), callback);
    }

    @ReactMethod
    public void convertToKeycardAccount(final String keyUID, final String accountData, final String options, final String keycardUID, final String password,
                                        final String newPassword, final Callback callback) throws JSONException {
        final String keyStoreDir = this.utils.getKeyStorePath(keyUID);
        executeRunnableStatusGoMethod(() -> {
            Statusgo.initKeystore(keyStoreDir);
            return Statusgo.convertToKeycardAccount(accountData, options, keycardUID, password, newPassword);
        }, callback);
    }

}