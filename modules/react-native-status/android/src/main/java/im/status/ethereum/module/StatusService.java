package im.status.ethereum.module;

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

            case StatusMessages.MSG_DISCARD_TRANSACTION:
                discardTransaction(message);
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

            Statusgo.AddPeer("enode://fc3065bb80bfced98a01441718e2b70a0353f023b9da3d57beb8f96a827402d23702b3a461e1c1b6c7a208cb09cc0aea9b7c42bf953bb8f732529c198b158db4@95.85.40.211:30303");
            Statusgo.AddPeer("enode://5ffa3a39f95614d881e07d24e265865218c45fe73b3a5f5d05868190e385cbf60d03ac8beaa4c31b7ee84a0ec947f22c969e2dd1783041a4d7381f7774c74526@188.166.229.119:30303");
            Statusgo.AddPeer("enode://3b020a1fd6ab980a5670975e8a7361af1732fa3fa1819b751a94b6a4265e8c52b02c608c0de1347784b834b298280b018bcf6547f47bbba63612cba0e4707ec1@139.59.212.114:30303");
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

    private void discardTransaction(Message message){

        Bundle data = message.getData();
        String id = data.getString("id");

        Log.d(TAG, "Before DiscardTransaction: " + id);
        String result = Statusgo.DiscardTransaction(id);
        Log.d(TAG, "After DiscardTransaction: " + result);
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
