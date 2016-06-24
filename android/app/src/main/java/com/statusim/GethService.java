package com.statusim;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.Environment;

import java.lang.ref.WeakReference;

import com.github.ethereum.go_ethereum.cmd.Geth;

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

    protected class StartTask extends AsyncTask<Void, Void, Void> {

        public StartTask() {
        }

        protected Void doInBackground(Void... args) {
            startGeth();
            return null;
        }

        protected void onPostExecute(Void results) {
            onGethStarted();
        }
    }

    protected void onGethStarted() {
        Log.d(TAG, "Geth Service started");
        isGethStarted = true;
    }

    protected void startGeth() {
        Log.d(TAG, "Starting background Geth Service");

        File extStore = Environment.getExternalStorageDirectory();

        final String dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() :
                getApplicationInfo().dataDir;

        final Runnable addPeer = new Runnable() {
            public void run() {
                Log.w("Geth", "adding peer");
                Geth.run("--exec admin.addPeer(\"enode://e2f28126720452aa82f7d3083e49e6b3945502cb94d9750a15e27ee310eed6991618199f878e5fbc7dfa0e20f0af9554b41f491dc8f1dbae8f0f2d37a3a613aa@139.162.13.89:55555\") attach http://localhost:8545");
            }
        };

        new Thread(new Runnable() {
            public void run() {
                Geth.run("--shh --ipcdisable --nodiscover --rpc --rpcapi db,eth,net,web3,shh,admin --fast --datadir=" + dataFolder);
            }
        }).start();

        handler.postDelayed(addPeer, 5000);
    }

    public void signalEvent(String jsonEvent) {
        
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("gethraw");
        System.loadLibrary("geth");

        if (!isGethInitialized) {
            isGethInitialized = true;
            new StartTask().execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: stop geth
        isGethStarted = false;
        isGethInitialized = false;
        Log.d(TAG, "Geth Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    protected boolean handleMessage(Message message) {
        return false;
    }

    public static boolean isRunning() {
        return isGethInitialized;
    }
}
