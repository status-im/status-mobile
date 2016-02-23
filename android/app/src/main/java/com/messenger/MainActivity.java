package com.messenger;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import android.os.Bundle;
import com.github.ethereum.go_ethereum.cmd.Geth;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;


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

        // Launch!
        new Thread(new Runnable() {
            public void run() {
                Geth.run("--bootnodes \"enode://dead745c1dbcde518b48e52aca1e8d5ba666005a2c8804e39826c6080fb11c1e8abe41d1e41896e871f204f790a90fa9781744cccecf492212192a7c56e7673b@rpc0.syng.io:40404\" --shh --ipcdisable --nodiscover --rpc --rpcapi \"db,eth,net,web3\" --fast --datadir=" + getFilesDir().getAbsolutePath());
            }
        }).start();
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "Messenger";
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
        new ReactNativeContacts()
      );
    }
}
