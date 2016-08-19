package com.statusim.geth.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import com.github.status_im.status_go.Statusgo;

import java.io.File;

public class GethService extends Service {

    private static final String TAG = "GethService";

    private static boolean isGethInitialized = false;
    private final Handler handler = new Handler();

    private static String dataFolder;

    private static class IncomingHandler extends Handler {

        private final WeakReference<GethService> service;

        IncomingHandler(GethService service) {

            this.service = new WeakReference<>(service);
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

    private final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));


    public static void signalEvent(String jsonEvent) {
        Log.d(TAG, "Signal event: " + jsonEvent);
        Bundle replyData = new Bundle();
        replyData.putString("event", jsonEvent);
        //createAndSendReply(message, GethMessages.MSG_SIGNAL_EVENT, replyData);
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
        isGethInitialized = false;
        Log.d(TAG, "Geth Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private boolean handleMessage(Message message) {
        switch (message.what) {

            case GethMessages.MSG_START_NODE:
                Log.d(TAG, "Received start node message." + message.toString());
                startNode(message);
                break;

            case GethMessages.MSG_STOP_NODE:
                stopNode(message);
                break;

            case GethMessages.MSG_CREATE_ACCOUNT:
                createAccount(message);
                break;

            case GethMessages.MSG_LOGIN:
                login(message);
                break;

            case GethMessages.MSG_COMPLETE_TRANSACTION:
                completeTransaction(message);
                break;

            default:
                return false;
        }

        return true;
    }

    private void startNode(Message message) {
        if (!isGethInitialized) {
            isGethInitialized = true;
            Log.d(TAG, "Client messenger1: " + message.replyTo.toString());
            Bundle data = message.getData();
            String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
            Log.d(TAG, "Callback identifier: " + callbackIdentifier);
            new StartTask(message.replyTo, callbackIdentifier).execute();
        }
    }

    private class StartTask extends AsyncTask<Void, Void, Void> {

        String callbackIdentifier;
        Messenger messenger;

        StartTask(Messenger messenger, String callbackIdentifier) {
            this.messenger = messenger;
            this.callbackIdentifier = callbackIdentifier;
        }

        protected Void doInBackground(Void... args) {
            startGeth();
            return null;
        }

        protected void onPostExecute(Void results) {
            onGethStarted(messenger, callbackIdentifier);
        }
    }

    private void onGethStarted(Messenger messenger, String callbackIdentifier) {
        Log.d(TAG, "Geth Service started");
        Message replyMessage = Message.obtain(null, GethMessages.MSG_NODE_STARTED, 0, 0, null);
        Bundle replyData = new Bundle();
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(GethConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
        replyMessage.setData(replyData);
        sendReply(messenger, replyMessage);
    }

    private void startGeth() {


        File extStore = Environment.getExternalStorageDirectory();

        dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() + "/ethereum" :
                getApplicationInfo().dataDir + "/ethereum";
        Log.d(TAG, "Starting background Geth Service in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);
            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        final Runnable addPeer = new Runnable() {
            public void run() {
                Log.w("Geth", "adding peer");
                Statusgo.addPeer("enode://409772c7dea96fa59a912186ad5bcdb5e51b80556b3fe447d940f99d9eaadb51d4f0ffedb68efad232b52475dd7bd59b51cee99968b3cc79e2d5684b33c4090c@139.162.166.59:30303");
            }
        };

        new Thread(new Runnable() {
            public void run() {
                Statusgo.StartNode(dataFolder);
            }
        }).start();

        handler.postDelayed(addPeer, 5000);
    }

    private void stopNode(Message message) {
        // TODO: stop node

        createAndSendReply(message, GethMessages.MSG_NODE_STOPPED, null);
    }

    private void createAccount(Message message) {
        Bundle data = message.getData();
        String password = data.getString("password");
        // TODO: remove second argument
        Log.d(TAG, "Creating account: " + password + " - " + dataFolder);
        String jsonData = Statusgo.CreateAccount(password);
        Log.d(TAG, "Created account: " + jsonData);

        Bundle replyData = new Bundle();
        replyData.putString("data", jsonData);
        createAndSendReply(message, GethMessages.MSG_ACCOUNT_CREATED, replyData);
    }

    private void login(Message message) {
        Bundle data = message.getData();
        String address = data.getString("address");
        String password = data.getString("password");
        // TODO: remove third argument
        String result = Statusgo.UnlockAccount(address, password, 0);
        Log.d(TAG, "Unlocked account: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        createAndSendReply(message, GethMessages.MSG_LOGGED_IN, replyData);
    }

    private void completeTransaction(Message message){
        Bundle data = message.getData();
        String hash = data.getString("hash");

        Log.d(TAG, "Before CompleteTransaction: " + hash);
        String result = Statusgo.CompleteTransaction(hash);
        Log.d(TAG, "After CompleteTransaction: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        createAndSendReply(message, GethMessages.MSG_TRANSACTION_COMPLETED, replyData);
    }

    public static boolean isRunning() {
        return isGethInitialized;
    }

    private static void createAndSendReply(Message message, int replyIdMessage, Bundle replyData) {

        if (message == null) {
            return;
        }
        Message replyMessage = Message.obtain(null, replyIdMessage, 0, 0, message.obj);
        if (replyData == null) {
            replyData = new Bundle();
        }
        Bundle data = message.getData();
        String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(GethConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
        replyMessage.setData(replyData);

        sendReply(message.replyTo, replyMessage);
    }

    private static void sendReply(Messenger messenger, Message message) {
        try {
            messenger.send(message);

        } catch (Exception e) {

            Log.e(TAG, "Exception sending message id: " + message.what, e);
        }
    }
}
