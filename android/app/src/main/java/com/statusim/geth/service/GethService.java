package com.statusim.geth.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import com.github.ethereum.go_ethereum.Statusgo;

import java.io.File;

public class GethService extends Service {

    private static final String TAG = "GethService";

    private static boolean isGethStarted = false;
    private static boolean isGethInitialized = false;
    private final Handler handler = new Handler();

    static class IncomingHandler extends Handler {

        private final WeakReference<GethService> service;

        IncomingHandler(GethService service) {

            this.service = new WeakReference<GethService>(service);
        }

        @Override
        public void handleMessage(Message message) {

            GethService service = this.service.get();
            if (service != null) {
                if (!service.handleMessage(message)) {
                    super.handleMessage(message);
                }
            }
        }
    }

    final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));


    public static void signalEvent(String jsonEvent) {
        System.out.println("\n\n\nIT WOOOOOORKS1111!!!!!!\n\n\n");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("statusgoraw");
        System.loadLibrary("statusgo");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: stop geth
        stopNode(null);
        isGethStarted = false;
        isGethInitialized = false;
        Log.d(TAG, "Geth Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    protected boolean handleMessage(Message message) {
        switch (message.what) {

            case GethMessages.MSG_START_NODE:
                startNode(message);
                break;

            case GethMessages.MSG_STOP_NODE:
                stopNode(message);
                break;

            case GethMessages.MSG_CREATE_ACCOUNT:
                createAccount(message);
                break;

            case GethMessages.MSG_ADD_ACCOUNT:
                addAccount(message);
                break;

            case GethMessages.MSG_UNLOCK_ACCOUNT:
                unlockAccount(message);
                break;

            default:
                return false;
        }

        return true;
    }

    protected void startNode(Message message) {
        if (!isGethInitialized) {
            isGethInitialized = true;
            new StartTask(message).execute();
        }
    }

    protected class StartTask extends AsyncTask<Void, Void, Void> {

        protected Message message;

        public StartTask(Message message) {
            this.message = message;
        }

        protected Void doInBackground(Void... args) {
            startGeth();
            return null;
        }

        protected void onPostExecute(Void results) {
            onGethStarted(message);
        }
    }

    protected void onGethStarted(Message message) {
        Log.d(TAG, "Geth Service started");
        isGethStarted = true;

        sendReply(message, GethMessages.MSG_NODE_STARTED, null);
    }

    protected void startGeth() {
        Log.d(TAG, "Starting background Geth Service");

        File extStore = Environment.getExternalStorageDirectory();

        final String dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() :
                getApplicationInfo().dataDir;

        new Thread(new Runnable() {
            public void run() {

                Statusgo.doStartNode(dataFolder);
            }
        }).start();
    }

    protected void stopNode(Message message) {
        // TODO: stop node

        sendReply(message, GethMessages.MSG_NODE_STOPPED, null);
    }

    protected void createAccount(Message message) {
        Bundle data = message.getData();
        String password = data.getString("password");
        // TODO: remove second argument
        String address = Statusgo.doCreateAccount(password, "");
        Log.d(TAG, "Created account: " + address);

        Bundle replyData = new Bundle();
        replyData.putString("address", address);
        sendReply(message, GethMessages.MSG_ACCOUNT_CREATED, replyData);
    }

    protected void addAccount(Message message) {
        Bundle data = message.getData();
        String privateKey = data.getString("privateKey");
        String password = data.getString("password");
        // TODO: add account
        //String address = Statusgo.doAddAccount(privateKey, password);
        String address = "added account address";
        Log.d(TAG, "Added account: " + address);

        Bundle replyData = new Bundle();
        replyData.putString("address", address);
        sendReply(message, GethMessages.MSG_ACCOUNT_ADDED, replyData);
    }

    protected void unlockAccount(Message message) {
        Bundle data = message.getData();
        String address = data.getString("address");
        String password = data.getString("password");
        // TODO: remove third argument
        String result = Statusgo.doUnlockAccount(address, password, 0);
        Log.d(TAG, "Unlocked account: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        sendReply(message, GethMessages.MSG_ACCOUNT_UNLOCKED, replyData);
    }

    public static boolean isRunning() {
        return isGethInitialized;
    }

    protected void sendReply(Message message, int replyIdMessage, Bundle replyData) {

        if (message == null) {
            return;
        }
        Message replyMessage = Message.obtain(null, replyIdMessage, 0, 0, message.obj);
        if (replyData == null) {
            replyData = new Bundle();
        }
        Bundle data = message.getData();
        String callbackIdentifier = data.getString("callbackIdentifier");
        replyData.putString("callbackIdentifier", callbackIdentifier);
        replyMessage.setData(replyData);

        try {
            message.replyTo.send(replyMessage);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception sending message id: " + replyIdMessage, e);
        }
    }
}
