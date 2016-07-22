package com.statusim;

import com.facebook.react.ReactActivity;
import com.statusim.geth.module.GethPackage;
import io.realm.react.RealmReactPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

import com.bitgo.randombytes.RandomBytesPackage;
import com.BV.LinearGradient.LinearGradientPackage;
import com.centaurwarchief.smslistener.SmsListener;
import com.github.yamill.orientation.OrientationPackage;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.File;
import com.statusim.Jail.JailPackage;

import com.lwansbrough.RCTCamera.*;
import com.i18n.reactnativei18n.ReactNativeI18n;
import android.content.res.Configuration;

import com.rnfs.RNFSPackage;

public class MainActivity extends ReactActivity {

    private static final String TAG = "MainActivity";

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                new JailPackage(this),
                new RealmReactPackage(),
                new VectorIconsPackage(),
                new ReactNativeContacts(),
                new ReactNativeI18n(),
                new RandomBytesPackage(),
                new LinearGradientPackage(),
                new RCTCameraPackage(),
                new SmsListener(this),
                new OrientationPackage(this),
                new RNFSPackage(),
                new GethPackage()
        );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent = new Intent("onConfigurationChanged");
        intent.putExtra("newConfig", newConfig);
        this.sendBroadcast(intent);
    }
}
