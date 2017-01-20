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

    private boolean isNodeInitialized = false;
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
        isNodeInitialized = false;
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
        applicationMessenger = message.replyTo;
        doStartNode();
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

            case StatusMessages.MSG_START_RPC:
                startRPC();
                break;

            case StatusMessages.MSG_STOP_RPC:
                stopRPC();
                break;

            default:
                return false;
        }

        return true;
    }

    public void doStartNode() {
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

            Statusgo.AddPeer("enode://e19d89e6faf2772e2f250e9625478ee7f313fcc0bb5e9310d5d407371496d9d7d73ccecd9f226cc2a8be34484525f72ba9db9d26f0222f4efc3c6d9d995ee224@198.199.105.122:30303");
            Statusgo.AddPeer("enode://1ad53266faaa9258ae71eef4d162022ba0d39498e1a3488e6c65fd86e0fb528e2aa68ad0e199da69fd39f4a3a38e9e8e95ac53ba5cc7676dfeaacf5fd6c0ad27@139.59.212.114:30303");
            isNodeInitialized = true;
        }
    }

    private void startNode(Message message) {
        doStartNode();
        createAndSendReply(message, StatusMessages.MSG_START_NODE, null);
    }

    private void stopNode(Message message) {
        // TODO: stop node

        createAndSendReply(message, StatusMessages.MSG_STOP_NODE, null);
    }

    private void startRPC() {
        Statusgo.StartNodeRPCServer();
    }

    private void stopRPC() {
        Statusgo.StopNodeRPCServer();
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

    public boolean isNodeInitialized() {
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
