package com.statusim.module;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import com.github.status_im.status_go.cmd.Statusgo;

import java.io.File;

public class StatusService extends Service {

    private static final String TAG = "StatusService";

    private static boolean isNodeInitialized = false;
    private final Handler handler = new Handler();

    private static String dataFolder;

    private static Messenger applicationMessenger = null;

    private static class IncomingHandler extends Handler {

        private final WeakReference<StatusService> service;

        IncomingHandler(StatusService service) {

            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message message) {

            StatusService service = this.service.get();
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

        Message replyMessage = Message.obtain(null, StatusMessages.MSG_GETH_EVENT, 0, 0, null);
        replyMessage.setData(replyData);
        sendReply(applicationMessenger, replyMessage);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        
        super.onDestroy();
        //TODO: stop geth
        stopNode(null);
        //isNodeInitialized = false;
        Log.d(TAG, "Status Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    private boolean handleMessage(Message message) {
        Log.d(TAG, "Received service message." + message.toString());
        switch (message.what) {

            case StatusMessages.MSG_START_NODE:
                startNode(message);
                break;

            case StatusMessages.MSG_STOP_NODE:
                stopNode(message);
                break;

            case StatusMessages.MSG_CREATE_ACCOUNT:
                createAccount(message);
                break;

            case StatusMessages.MSG_RECOVER_ACCOUNT:
                recoverAccount(message);
                break;

            case StatusMessages.MSG_LOGIN:
                login(message);
                break;

            case StatusMessages.MSG_COMPLETE_TRANSACTION:
                completeTransaction(message);
                break;

            case StatusMessages.MSG_JAIL_INIT:
                initJail(message);
                break;

            case StatusMessages.MSG_JAIL_PARSE:
                parseJail(message);
                break;

            case StatusMessages.MSG_JAIL_CALL:
                callJail(message);
                break;

            default:
                return false;
        }

        return true;
    }

    private void startNode(Message message) {

        applicationMessenger = message.replyTo;
        if (!isNodeInitialized) {

            File extStore = Environment.getExternalStorageDirectory();
            dataFolder = extStore.exists() ?
                    extStore.getAbsolutePath() + "/ethereum" :
                    getApplicationInfo().dataDir + "/ethereum";
            Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

            try {
                final File newFile = new File(dataFolder);
                // todo handle error?
                newFile.mkdir();
            } catch (Exception e) {
                Log.e(TAG, "error making folder: " + dataFolder, e);
            }

            Statusgo.StartNode(dataFolder);
            Log.d(TAG, "Geth node started");
            Log.w(TAG, "adding peer");
            Statusgo.AddPeer("enode://4e2bb6b09aa34375ae2df23fa063edfe7aaec952dba972449158ae0980a4abd375aca3c06a519d4f562ff298565afd288a0ed165944974b2557e6ff2c31424de@138.68.73.175:30303");
            isNodeInitialized = true;
        }
        createAndSendReply(message, StatusMessages.MSG_START_NODE, null);
    }

    private void stopNode(Message message) {
        // TODO: stop node

        createAndSendReply(message, StatusMessages.MSG_STOP_NODE, null);
    }

    private void createAccount(Message message) {
        
        Bundle data = message.getData();
        String password = data.getString("password");
        Log.d(TAG, "Creating account: " + password);
        String jsonData = Statusgo.CreateAccount(password);
        Log.d(TAG, "Created account: " + jsonData);

        Bundle replyData = new Bundle();
        replyData.putString("data", jsonData);
        createAndSendReply(message, StatusMessages.MSG_CREATE_ACCOUNT, replyData);
    }

    private void recoverAccount(Message message) {
        
        Bundle data = message.getData();
        String passphrase = data.getString("passphrase");
        String password = data.getString("password");
        Log.d(TAG, "Recovering account: " + passphrase + " - " + password);
        String jsonData = Statusgo.RecoverAccount(password, passphrase);
        Log.d(TAG, "Recovered account: " + jsonData);

        Bundle replyData = new Bundle();
        replyData.putString("data", jsonData);
        createAndSendReply(message, StatusMessages.MSG_RECOVER_ACCOUNT, replyData);
    }

    private void login(Message message) {
        
        applicationMessenger = message.replyTo;
        Bundle data = message.getData();
        String address = data.getString("address");
        String password = data.getString("password");
        String result = Statusgo.Login(address, password);
        Log.d(TAG, "Loggedin account: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("data", result);
        createAndSendReply(message, StatusMessages.MSG_LOGIN, replyData);

        // Test signalEvent
        //signalEvent("{ \"type\": \"test\", \"event\": \"test event\" }");
    }

    private void completeTransaction(Message message){
        
        Bundle data = message.getData();
        String hash = data.getString("hash");
        String password = data.getString("password");

        Log.d(TAG, "Before CompleteTransaction: " + hash);
        String result = Statusgo.CompleteTransaction(hash, password);
        Log.d(TAG, "After CompleteTransaction: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("data", result);
        createAndSendReply(message, StatusMessages.MSG_COMPLETE_TRANSACTION, replyData);
    }

    private void initJail(Message message){

        Bundle data = message.getData();
        String js = data.getString("js");

        Statusgo.InitJail(js);

        Bundle replyData = new Bundle();
        createAndSendReply(message, StatusMessages.MSG_JAIL_INIT, replyData);
    }

    private void parseJail(Message message){

        Bundle data = message.getData();
        String chatId = data.getString("chatId");
        String js = data.getString("js");

        String result = Statusgo.Parse(chatId, js);

        Bundle replyData = new Bundle();
        replyData.putString("data", result);
        createAndSendReply(message, StatusMessages.MSG_JAIL_PARSE, replyData);
    }

    private void callJail(Message message){

        Bundle data = message.getData();
        String chatId = data.getString("chatId");
        String path = data.getString("path");
        String params = data.getString("params");

        String result = Statusgo.Call(chatId, path, params);

        Bundle replyData = new Bundle();
        replyData.putString("data", result);
        createAndSendReply(message, StatusMessages.MSG_JAIL_CALL, replyData);
    }

    public static boolean isNodeInitialized() {
        return isNodeInitialized;
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
        String callbackIdentifier = data.getString(StatusConnector.CALLBACK_IDENTIFIER);
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(StatusConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
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
