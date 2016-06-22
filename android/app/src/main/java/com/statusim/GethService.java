package com.statusim;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.Environment;

import java.lang.ref.WeakReference;

import com.github.status_im.status_go.Statusgo;

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

        new Thread(new Runnable() {
            public void run() {
                Statusgo.doStartNode(dataFolder);
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("statusgo");

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
