package com.statusim.geth.module;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.statusim.geth.service.ConnectorHandler;
import com.statusim.geth.service.GethConnector;
import com.statusim.geth.service.GethMessages;
import com.statusim.geth.service.GethService;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

class GethModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    private static final String TAG = "GethModule";

    private GethConnector geth = null;

    private HashMap<String, Callback> callbacks = new HashMap<>();

    GethModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Geth";
    }

    @Override
    public void onHostResume() {  // Actvity `onResume`

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            return;
        }
        if (geth == null) {
            geth = new GethConnector(currentActivity, GethService.class);
            geth.registerHandler(this);
        }
        geth.bindService();
    }

    @Override
    public void onHostPause() {  // Actvity `onPause`

        if (geth != null) {
            geth.unbindService();
        }
    }

    @Override
    public void onHostDestroy() {  // Actvity `onDestroy`

        if (geth != null) {
            geth.stopNode(null);
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
        Bundle data = message.getData();
        String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
        Log.d(TAG, "callback identifier: " + callbackIdentifier);
        Callback callback = callbacks.remove(callbackIdentifier);
        if (callback == null) {
            Log.d(TAG, "Could not find callback: " + callbackIdentifier);
        }
        switch (message.what) {
            case GethMessages.MSG_NODE_STARTED:
                if (callback != null) {
                    callback.invoke(true);
                }
                break;
            case GethMessages.MSG_NODE_STOPPED:
                break;
            case GethMessages.MSG_ACCOUNT_CREATED:
                if (callback != null) {
                    callback.invoke(data.getString("data"));
                }
                break;
            case GethMessages.MSG_ACCOUNT_RECOVERED:
                if (callback != null) {
                    callback.invoke(data.getString("data"));
                }
                break;
            case GethMessages.MSG_LOGGED_IN:
                if (callback != null) {
                    callback.invoke(data.getString("result"));
                }
                break;
            case GethMessages.MSG_TRANSACTION_COMPLETED:
                String result = data.getString("result");
                Log.d(TAG, "Send result: " + result + (callback == null));
                if (callback != null) {
                    callback.invoke(result);
                }
                break;
            case GethMessages.MSG_GETH_EVENT:
                String event = data.getString("event");
                WritableMap params = Arguments.createMap();
                params.putString("jsonEvent", event);
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("gethEvent", params);
            default:
                isClaimed = false;
        }

        return isClaimed;
    }

    @ReactMethod
    public void startNode(Callback callback, Callback onAlreadyRunning) {

        if (GethService.isRunning()) {
            onAlreadyRunning.invoke();
            return;
        }

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.startNode(callbackIdentifier);
    }

    @ReactMethod
    public void login(String address, String password, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.login(callbackIdentifier, address, password);
    }

    @ReactMethod
    public void createAccount(String password, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.createAccount(callbackIdentifier, password);
    }

    @ReactMethod
    public void recoverAccount(String passphrase, String password, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.recoverAccount(callbackIdentifier, passphrase, password);
    }

    private String createIdentifier() {
        return UUID.randomUUID().toString();
    }

    @ReactMethod
    public void completeTransaction(String hash, String password, Callback callback) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        Log.d(TAG, "Complete transaction: " + hash);
        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.completeTransaction(callbackIdentifier, hash, password);
    }
}
