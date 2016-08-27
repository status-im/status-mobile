package com.statusim.geth.service;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class GethConnector extends ServiceConnector {

    private static final String TAG = "GethConnector";

    public static final String CALLBACK_IDENTIFIER = "callbackIdentifier";

    public GethConnector(Context context, Class serviceClass) {

        super(context, serviceClass);
    }

    public void startNode(String callbackIdentifier) {

        if (checkBound()) {
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_START_NODE, null);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(startNode) to service: ", e);
            }
        }
    }

    public void stopNode(String callbackIdentifier) {

        if (checkBound()) {
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_STOP_NODE, null);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(stopNode) to service: ", e);
            }
        }
    }

    public void login(String callbackIdentifier, String address, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("address", address);
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_LOGIN, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(unlockAccount) to service: ", e);
            }
        }
    }

    public void createAccount(String callbackIdentifier, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_CREATE_ACCOUNT, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(createAccount) to service: ", e);
            }
        }
    }

    public void recoverAccount(String callbackIdentifier, String passphrase, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("passphrase", passphrase);
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_RECOVER_ACCOUNT, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(recoverAccount) to service: ", e);
            }
        }
    }

    public void completeTransaction(String callbackIdentifier, String hash, String password){
        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("hash", hash);
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_COMPLETE_TRANSACTION, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(completeTransaction) to service: ", e);
            }
        }
    }


    private boolean checkBound() {

        if (!isBound) {
            Log.d(TAG, "GethConnector not bound!");
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
}
