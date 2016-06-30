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

    private static String dataFolder;

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
                Log.d(TAG, "Received start node message." + message.toString());
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

            case GethMessages.MSG_LOGIN:
                login(message);
                break;

            default:
                return false;
        }

        return true;
    }

    protected void startNode(Message message) {
        if (!isGethInitialized) {
            isGethInitialized = true;
            Log.d(TAG, "Client messenger1: " + message.replyTo.toString());
            Bundle data = message.getData();
            String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
            Log.d(TAG, "Callback identifier: " + callbackIdentifier);
            new StartTask(message.replyTo, callbackIdentifier).execute();
        }
    }

    protected class StartTask extends AsyncTask<Void, Void, Void> {

        protected String callbackIdentifier;
        protected Messenger messenger;

        public StartTask(Messenger messenger, String callbackIdentifier) {
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

    protected void onGethStarted(Messenger messenger, String callbackIdentifier) {
        Log.d(TAG, "Geth Service started");
        isGethStarted = true;
        Message replyMessage = Message.obtain(null, GethMessages.MSG_NODE_STARTED, 0, 0, null);
        Bundle replyData = new Bundle();
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(GethConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
        replyMessage.setData(replyData);
        sendReply(messenger, replyMessage);
    }

    protected void startGeth() {


        File extStore = Environment.getExternalStorageDirectory();

        dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() + "/ethereum" :
                getApplicationInfo().dataDir + "/ethereum";
        Log.d(TAG, "Starting background Geth Service in folder: " + dataFolder);
        try {
            final File newFile = new File(dataFolder);
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        new Thread(new Runnable() {
            public void run() {

                Statusgo.StartNode(dataFolder);
            }
        }).start();
    }

    protected void stopNode(Message message) {
        // TODO: stop node

        createAndSendReply(message, GethMessages.MSG_NODE_STOPPED, null);
    }

    protected void createAccount(Message message) {
        Bundle data = message.getData();
        String password = data.getString("password");
        // TODO: remove second argument
        Log.d(TAG, "Creating account: " + password + " - " + dataFolder);
        String jsonData = Statusgo.CreateAccount(password, dataFolder + "/keystore");
        Log.d(TAG, "Created account: " + jsonData);

        Bundle replyData = new Bundle();
        replyData.putString("data", jsonData);
        createAndSendReply(message, GethMessages.MSG_ACCOUNT_CREATED, replyData);
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
        createAndSendReply(message, GethMessages.MSG_ACCOUNT_ADDED, replyData);
    }

    protected void login(Message message) {
        Bundle data = message.getData();
        String address = data.getString("address");
        String password = data.getString("password");
        // TODO: remove third argument
        String result = Statusgo.Login(address, password);
        Log.d(TAG, "Unlocked account: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        createAndSendReply(message, GethMessages.MSG_LOGGED_IN, replyData);
    }

    public static boolean isRunning() {
        return isGethInitialized;
    }

    protected void createAndSendReply(Message message, int replyIdMessage, Bundle replyData) {

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

    protected void sendReply(Messenger messenger, Message message) {
        try {
            messenger.send(message);

        } catch (Exception e) {

            Log.e(TAG, "Exception sending message id: " + message.what, e);
        }
    }
}
