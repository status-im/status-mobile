package com.statusim;

import com.facebook.react.ReactActivity;
import io.realm.react.RealmReactPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.Context;

import com.bitgo.randombytes.RandomBytesPackage;
import com.BV.LinearGradient.LinearGradientPackage;
import com.centaurwarchief.smslistener.SmsListener;


import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.File;
import com.statusim.Jail.JailPackage;

import com.lwansbrough.RCTCamera.*;
import com.i18n.reactnativei18n.ReactNativeI18n;
import io.realm.react.RealmReactPackage;


public class MainActivity extends ReactActivity {

    private static final String TAG = "MainActivity";

    /**
     * Incoming message handler. Calls to its binder are sequential!
     */
    protected final IncomingHandler handler = new IncomingHandler();

    /** Flag indicating if the service is bound. */
    protected boolean isBound;

    /** Sends messages to the service. */
    protected Messenger serviceMessenger = null;

    /** Receives messages from the service. */
    protected Messenger clientMessenger = new Messenger(handler);

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            boolean isClaimed = false;
            Log.d(TAG, "!!!!!!!!!!!!!! Received Service Message !!!!!!!!!!!!!!");
            super.handleMessage(message);
        }
    }

    protected ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            serviceMessenger = new Messenger(service);
            isBound = true;
            onConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            serviceMessenger = null;
            isBound = false;
            Log.d(TAG, "!!!!!!!!!!!!!! Geth Service Disconnected !!!!!!!!!!!!!!");
        }
    };

    protected void onConnected() {
        Log.d(TAG, "!!!!!!!!!!!!!! Geth Service Connected !!!!!!!!!!!!!!");
    }

    protected void startStatus() {
        // Required because of crazy APN settings redirecting localhost (found in GB)
        Properties properties = System.getProperties();
        properties.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        properties.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!RootUtil.isDeviceRooted()) {
            startStatus();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setMessage(getResources().getString(R.string.root_warning))
                    .setPositiveButton(getResources().getString(R.string.root_okay), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startStatus();
                        }
                    }).setNegativeButton(getResources().getString(R.string.root_cancel), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MainActivity.this.finishAffinity();
                        }
                    }).setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            MainActivity.this.finishAffinity();
                        }
                    }).create();
            dialog.show();
        }
        Intent intent = new Intent(this, GethService.class);
        if (!GethService.isRunning()) {
            startService(intent);
        }
        if (serviceConnection != null && GethService.isRunning()) {
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(serviceConnection);
        }
        catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the geth service", t);
        }
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "StatusIm";
    }

    /**
     * Returns whether dev mode should be enabled.
     * This enables e.g. the dev menu.
     */
    @Override
    protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    /**
     * A list of packages used by the app. If the app uses additional views
     * or modules besides the default ones, add more packages here.
     */
    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
                new MainReactPackage(),
                new JailPackage(),
                new RealmReactPackage(),
                new VectorIconsPackage(),
                new ReactNativeContacts(),
                new ReactNativeI18n(),
                new RandomBytesPackage(),
                new LinearGradientPackage(),
                new RCTCameraPackage(),
                new SmsListener(this)
        );
    }
}
