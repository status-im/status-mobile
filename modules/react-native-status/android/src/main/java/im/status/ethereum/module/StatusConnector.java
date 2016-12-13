package im.status.ethereum.module;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class StatusConnector extends ServiceConnector {

    private static final String TAG = "StatusConnector";

    public static final String CALLBACK_IDENTIFIER = "callbackIdentifier";

    public StatusConnector(Context context, Class serviceClass) {

        super(context, serviceClass);
    }

    public void startNode(String callbackIdentifier) {

        if (checkBound()) {
            sendMessage(callbackIdentifier, StatusMessages.MSG_START_NODE, null);
        }
    }

    public void stopNode(String callbackIdentifier) {

        if (checkBound()) {
            sendMessage(callbackIdentifier, StatusMessages.MSG_STOP_NODE, null);
        }
    }

    public void startRPC() {
        if (checkBound()) {
            sendMessage(null, StatusMessages.MSG_START_RPC, null);
        }
    }

    public void stopRPC() {
        if (checkBound()) {
            sendMessage(null, StatusMessages.MSG_STOP_RPC, null);
        }
    }

    public void login(String callbackIdentifier, String address, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("address", address);
            data.putString("password", password);
            sendMessage(callbackIdentifier, StatusMessages.MSG_LOGIN, data);
        }
    }

    public void createAccount(String callbackIdentifier, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("password", password);
            sendMessage(callbackIdentifier, StatusMessages.MSG_CREATE_ACCOUNT, data);
        }
    }

    public void recoverAccount(String callbackIdentifier, String passphrase, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("passphrase", passphrase);
            data.putString("password", password);
            sendMessage(callbackIdentifier, StatusMessages.MSG_RECOVER_ACCOUNT, data);
        }
    }

    public void completeTransaction(String callbackIdentifier, String hash, String password){

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("hash", hash);
            data.putString("password", password);
            sendMessage(callbackIdentifier, StatusMessages.MSG_COMPLETE_TRANSACTION, data);
        }
    }

    public void discardTransaction(String id){

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("id", id);
            sendMessage(null, StatusMessages.MSG_DISCARD_TRANSACTION, data);
        }
    }

    public void initJail(String callbackIdentifier, String js){

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("js", js);
            sendMessage(callbackIdentifier, StatusMessages.MSG_JAIL_INIT, data);
        }
    }

    public void parseJail(String callbackIdentifier, String chatId, String js){

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("chatId", chatId);
            data.putString("js", js);
            sendMessage(callbackIdentifier, StatusMessages.MSG_JAIL_PARSE, data);
        }
    }

    public void callJail(String callbackIdentifier, String chatId, String path, String params){

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("chatId", chatId);
            data.putString("path", path);
            data.putString("params", params);
            sendMessage(callbackIdentifier, StatusMessages.MSG_JAIL_CALL, data);
        }
    }

    private boolean checkBound() {

        if (!isBound) {
            Log.d(TAG, "StatusConnector not bound!");
            return false;
        }
        return true;
    }

    private Message createMessage(String callbackIdentifier, int idMessage, Bundle data) {

        Log.d(TAG, "Client messenger: " + clientMessenger.toString());
        Message msg = Message.obtain(null, idMessage, 0, 0);
        msg.replyTo = clientMessenger;
        if (data == null) {
            data = new Bundle();
        }
        data.putString(CALLBACK_IDENTIFIER, callbackIdentifier);
        msg.setData(data);
        return msg;
    }

    private void sendMessage(String callbackIdentifier, int idMessage, Bundle data) {

        Message msg = createMessage(callbackIdentifier, idMessage, data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception sending message(" + msg.toString() + ") to service: ", e);
        }
    }
}
