package com.statusim;

import com.facebook.react.ReactActivity;
import io.realm.react.RealmReactPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import com.github.ethereum.go_ethereum.cmd.Geth;
import com.bitgo.randombytes.RandomBytesPackage;
import com.BV.LinearGradient.LinearGradientPackage;
import com.centaurwarchief.smslistener.SmsListener;

import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.File;

import com.lwansbrough.RCTCamera.*;
import com.i18n.reactnativei18n.ReactNativeI18n;
import io.realm.react.RealmReactPackage;


public class MainActivity extends ReactActivity {

    final Handler handler = new Handler();

    protected void startStatus() {
        // Required because of crazy APN settings redirecting localhost (found in GB)
        Properties properties = System.getProperties();
        properties.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        properties.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");

        File extStore = Environment.getExternalStorageDirectory();

        final String dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() :
                getApplicationInfo().dataDir;

        // Launch!
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required for android-16 (???)
        // Crash if put in startStatus() ?
        System.loadLibrary("gethraw");
        System.loadLibrary("geth");

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
