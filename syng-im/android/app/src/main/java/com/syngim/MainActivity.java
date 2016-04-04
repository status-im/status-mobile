package com.syngim;

import com.facebook.react.ReactActivity;
import io.realm.react.RealmReactPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import android.os.Bundle;
import android.os.Environment;
import com.github.ethereum.go_ethereum.cmd.Geth;
import com.bitgo.randombytes.RandomBytesPackage;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.File;

import com.i18n.reactnativei18n.ReactNativeI18n;
import io.realm.react.RealmReactPackage;

public class MainActivity extends ReactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required for android-16 (???)
        System.loadLibrary("gethraw");
        System.loadLibrary("geth");

        // Required because of crazy APN settings redirecting localhost
        Properties properties = System.getProperties();
        properties.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        properties.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");

        File extStore = Environment.getExternalStorageDirectory();

        final String dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() :
                getApplicationInfo().dataDir;

        // Launch!
        new Thread(new Runnable() {
            public void run() {
                Geth.run("--bootnodes enode://e2f28126720452aa82f7d3083e49e6b3945502cb94d9750a15e27ee310eed6991618199f878e5fbc7dfa0e20f0af9554b41f491dc8f1dbae8f0f2d37a3a613aa@139.162.13.89:30303 --shh --ipcdisable --nodiscover --rpc --rpcapi db,eth,net,web3,shh,admin --fast --datadir=" + dataFolder);
            }
        }).start();
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "SyngIm";
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
                new RandomBytesPackage()
        );
    }
}
