package com.statusim.geth.module;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import com.facebook.react.bridge.*;
import com.statusim.geth.service.ConnectorHandler;
import com.statusim.geth.service.GethConnector;
import com.statusim.geth.service.GethMessages;
import com.statusim.geth.service.GethService;

import java.util.HashMap;
import java.util.UUID;

public class GethModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    protected GethConnector geth = null;
    protected String handlerIdentifier = createIdentifier();

    protected HashMap<String, Callback> startNodeCallbacks = new HashMap<>();
    protected HashMap<String, Callback> createAccountCallbacks = new HashMap<>();
    protected HashMap<String, Callback> addAccountCallbacks = new HashMap<>();
    protected HashMap<String, Callback> unlockAccountCallbacks = new HashMap<>();


    public GethModule(ReactApplicationContext reactContext) {
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
    public String getID() {

        return handlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {
    }

    @Override
    public void onConnectorDisconnected() {
    }

    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
        Bundle data = message.getData();
        String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
        Callback callback = null;
        switch (message.what) {
            case GethMessages.MSG_NODE_STARTED:
                callback = startNodeCallbacks.remove(callbackIdentifier);
                if (callback != null) {
                    callback.invoke(null);
                }
                break;
            case GethMessages.MSG_NODE_STOPPED:
                break;
            case GethMessages.MSG_ACCOUNT_CREATED:
                callback = createAccountCallbacks.remove(callbackIdentifier);
                if (callback != null) {
                    callback.invoke(null, "{ \"address\": \"" + data.getString("address") + "\"}");
                }
                break;
            case GethMessages.MSG_ACCOUNT_ADDED:
                callback = addAccountCallbacks.remove(callbackIdentifier);
                if (callback != null) {
                    callback.invoke(null, "{ \"address\": \"" + data.getString("address") + "\"}");
                }
                break;
            case GethMessages.MSG_ACCOUNT_UNLOCKED:
                callback = unlockAccountCallbacks.remove(callbackIdentifier);
                if (callback != null) {
                    callback.invoke(null, "{ \"result\": \"" + data.getString("result") + "\"}");
                }
                break;
            default:
                isClaimed = false;
        }

        return isClaimed;
    }

    @ReactMethod
    public void startNode(Callback callback) {

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
        startNodeCallbacks.put(callbackIdentifier, callback);

        geth.startNode(callbackIdentifier);
    }

    @ReactMethod
    public void unlockAccount(String address, String password, Callback callback) {

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
        unlockAccountCallbacks.put(callbackIdentifier, callback);

        geth.unlockAccount(callbackIdentifier, address, password);
    }

    @ReactMethod
    public void createAccount(Callback callback) {

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
        createAccountCallbacks.put(callbackIdentifier, callback);

        geth.createAccount(callbackIdentifier);
    }

    @ReactMethod
    public void addAccount(String privateKey, Callback callback) {

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
        addAccountCallbacks.put(callbackIdentifier, callback);
        geth.addAccount(callbackIdentifier, privateKey);
    }

    protected String createIdentifier() {
        return UUID.randomUUID().toString();
    }

}