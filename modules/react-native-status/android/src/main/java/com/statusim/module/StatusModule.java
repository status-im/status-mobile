package com.statusim.module;

import android.app.Activity;
import android.view.WindowManager;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

class StatusModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    private static final String TAG = "StatusModule";

    private StatusConnector status = null;

    private HashMap<String, Callback> callbacks = new HashMap<>();

    StatusModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Status";
    }

    @Override
    public void onHostResume() {  // Actvity `onResume`
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.d(TAG, "On host Activity doesn't exist");
            return;
        }
        if (status == null) {
            status = new StatusConnector(currentActivity, StatusService.class);
            status.registerHandler(this);
        }

        status.bindService();

        WritableMap params = Arguments.createMap();
        Log.d(TAG, "Send module.initialized event");
        params.putString("jsonEvent", "{\"type\":\"module.initialized\"}");
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("gethEvent", params);
    }

    @Override
    public void onHostPause() {  // Actvity `onPause`
        if (status != null) {
            status.unbindService();
        }
    }

    @Override
    public void onHostDestroy() {  // Actvity `onDestroy`
        if (status != null) {
            status.stopNode(null);
        }
    }

    @Override
    public void onConnectorConnected() {
    }

    @Override
    public void onConnectorDisconnected() {
    }

    @Override
    public boolean handleMessage(Message message) {

        Log.d(TAG, "Received message: " + message.toString());
        boolean isClaimed = true;
        Bundle bundle = message.getData();
        String callbackIdentifier = bundle.getString(StatusConnector.CALLBACK_IDENTIFIER);
        String data = bundle.getString("data");
        Callback callback = callbacks.remove(callbackIdentifier);
        switch (message.what) {
            case StatusMessages.MSG_START_NODE:
            case StatusMessages.MSG_STOP_NODE:
            case StatusMessages.MSG_LOGIN:
            case StatusMessages.MSG_CREATE_ACCOUNT:
            case StatusMessages.MSG_RECOVER_ACCOUNT:
            case StatusMessages.MSG_COMPLETE_TRANSACTION:
            case StatusMessages.MSG_JAIL_INIT:
            case StatusMessages.MSG_JAIL_PARSE:
            case StatusMessages.MSG_JAIL_CALL:
                if (callback == null) {
                    Log.d(TAG, "Could not find callback: " + callbackIdentifier);
                } else {
                    callback.invoke(data);
                }
                break;
            case StatusMessages.MSG_GETH_EVENT:
                String event = bundle.getString("event");
                WritableMap params = Arguments.createMap();
                params.putString("jsonEvent", event);
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
                break;
            default:
                isClaimed = false;
        }

        return isClaimed;
    }

    private boolean checkAvailability() {

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.d(TAG, "Activity doesn't exist");
            return false;
        }

        if (status == null) {
            Log.d(TAG, "Status connector is null");
            return false;
        }

        return true;
    }

    // Geth

    @ReactMethod
    public void startNode(Callback callback) {
        Log.d(TAG, "startNode");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.startNode(callbackIdentifier);
    }

    @ReactMethod
    public void login(String address, String password, Callback callback) {
        Log.d(TAG, "login");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.login(callbackIdentifier, address, password);
    }

    @ReactMethod
    public void createAccount(String password, Callback callback) {
        Log.d(TAG, "createAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.createAccount(callbackIdentifier, password);
    }

    @ReactMethod
    public void recoverAccount(String passphrase, String password, Callback callback) {
        Log.d(TAG, "recoverAccount");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.recoverAccount(callbackIdentifier, passphrase, password);
    }

    private String createIdentifier() {
        return UUID.randomUUID().toString();
    }

    @ReactMethod
    public void completeTransaction(String hash, String password, Callback callback) {
        Log.d(TAG, "completeTransaction");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        Log.d(TAG, "Complete transaction: " + hash);
        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.completeTransaction(callbackIdentifier, hash, password);
    }


    @ReactMethod
    public void discardTransaction(String id) {
        Log.d(TAG, "discardTransaction");
        if (!checkAvailability()) {
            return;
        }

        Log.d(TAG, "Discard transaction: " + id);
        status.discardTransaction(id);
    }

    // Jail

    @ReactMethod
    public void initJail(String js, Callback callback) {
        Log.d(TAG, "initJail");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.initJail(callbackIdentifier, js);
    }

    @ReactMethod
    public void parseJail(String chatId, String js, Callback callback) {
        Log.d(TAG, "parseJail");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.parseJail(callbackIdentifier, chatId, js);
    }

    @ReactMethod
    public void callJail(String chatId, String path, String params, Callback callback) {
        Log.d(TAG, "callJail");
        if (!checkAvailability()) {
            callback.invoke(false);
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        status.callJail(callbackIdentifier, chatId, path, params);
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
}
