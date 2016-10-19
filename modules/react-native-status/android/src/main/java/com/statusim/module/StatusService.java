package com.statusim.module;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.concurrent.Callable;

import java.lang.ref.WeakReference;

import com.github.status_im.status_go.cmd.Statusgo;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatusService extends Service {

    private static final String TAG = "StatusService";

    private static boolean isNodeInitialized = false;
    private final Handler handler = new Handler();

    private ExecutorService executor = null;

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
        if (executor != null) {
            executor.shutdownNow();
        }
        //TODO: stop geth
        stopNode(null);
        //isNodeInitialized = false;
        Log.d(TAG, "Status Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
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

            Statusgo.AddPeer("enode://efe4e6899e05237180c0970aedb81cb5aecf5b200779c7c9e1f955783e8299b364c0b981c03f4c36ad5328ef972b417afde260bbf2c5a8db37ba7f5738033952@198.199.105.122:30303");
            Statusgo.AddPeer("enode://5a5839435f48d1e3f2f907e4582f0a134e0b7857afe507073978ca32cf09ea54989dac433605047d0bc4cd19a8c80affac6876069014283aa7c7bb4954d0e623@95.85.40.211:30303");
            Statusgo.AddPeer("enode://2f05d430b4cb1c0e2a0772d48da3a034f1b596ea7163ab80d3404802d10b7d55bde323897c2be0d36026181e1a68510ea1f42a646ef9494c27e61f61e4088b7d@188.166.229.119:30303");
            Statusgo.AddPeer("enode://ad61a21f83f12b0ca494611650f5e4b6427784e7c62514dcb729a3d65106de6f12836813acf39bdc35c12ecfd0e230723678109fd4e7091ce389697bd7da39b4@139.59.212.114:30303");
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
        String callbackIdentifier = data.getString(StatusConnector.CALLBACK_IDENTIFIER);

        Log.d(TAG, "Before StatusGo.Call");
        Callable<String> callable = new JailRequest(message.replyTo, chatId, path, params, callbackIdentifier);
        executor.submit(callable);
    }

    public class JailRequest implements Callable<String> {

        String chatId;
        String path;
        String params;
        String callbackIdentifier;
        Messenger messenger;

        JailRequest(Messenger messenger, String chatId, String path, String params, String callbackIdentifier) {

            this.messenger = messenger;
            this.chatId = chatId;
            this.path = path;
            this.params = params;
            this.callbackIdentifier = callbackIdentifier;
        }

        public String call() throws Exception {
            Log.d(TAG, "StatusGo.Call");
            String result = Statusgo.Call(chatId, path, params);

            Bundle replyData = new Bundle();
            replyData.putString("data", result);
            Message replyMessage = Message.obtain(null, StatusMessages.MSG_JAIL_CALL, 0, 0, null);
            Log.d(TAG, "Callback identifier: " + callbackIdentifier);
            replyData.putString(StatusConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
            replyMessage.setData(replyData);
            sendReply(messenger, replyMessage);

            return result;
        }

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
