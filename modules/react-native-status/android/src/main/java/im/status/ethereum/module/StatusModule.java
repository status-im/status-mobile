package im.status.ethereum.module;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import statusgo.SignalHandler;
import statusgo.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import javax.annotation.Nullable;

import android.os.Build;
import java.util.Map;


class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, SignalHandler {

    private static final String TAG = "StatusModule";
    private static StatusModule module;
    private ReactApplicationContext reactContext;
    private boolean rootedDevice;
    private boolean background;
    private Utils utils;


    StatusModule(ReactApplicationContext reactContext, boolean rootedDevice) {
        super(reactContext);
        this.reactContext = reactContext;
        this.rootedDevice = rootedDevice;
        this.utils = new Utils(reactContext);

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

    public void handleSignal(final String jsonEventString) {
        Log.d(TAG, "Signal event");
        WritableMap params = Arguments.createMap();
        params.putString("jsonEvent", jsonEventString);
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
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
    public void getNodeConfig(final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.getNodeConfig(), callback);
    }

    @ReactMethod
    public void deleteImportedKey(final String keyUID, final String address, final String password, final Callback callback) throws JSONException {
        final String keyStoreDir = this.utils.getKeyStorePath(keyUID);
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.deleteImportedKey(address, password, keyStoreDir), callback);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String fleets() {
        return Statusgo.fleets();
    }

    @ReactMethod
    public void identiconAsync(final String seed, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.identicon(seed), callback);
    }

    @Override
    public @Nullable
    Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();

        constants.put("is24Hour", this.utils.is24Hour());
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

