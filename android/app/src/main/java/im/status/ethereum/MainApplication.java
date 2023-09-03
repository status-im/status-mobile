package im.status.ethereum;

import androidx.multidex.MultiDexApplication;
import android.webkit.WebView;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.PackageList;

import com.facebook.react.ReactApplication;
import cl.json.RNSharePackage;
import com.facebook.react.ReactNativeHost;
import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.react.NavigationReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.reactnativecommunity.blurview.BlurViewPackage;

import java.util.List;

import im.status.ethereum.keycard.RNStatusKeycardPackage;
import im.status.ethereum.module.StatusPackage;
import im.status.ethereum.pushnotifications.PushNotificationPackage;
import im.status.ethereum.StatusOkHttpClientFactory;

import com.facebook.react.bridge.JSIModulePackage;

public class MainApplication extends NavigationApplication {

    private final ReactNativeHost mReactNativeHost = new NavigationReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {

            StatusPackage statusPackage = new StatusPackage(RootUtil.isDeviceRooted());

            List<ReactPackage> packages = new PackageList(this).getPackages();
            packages.add(statusPackage);
            packages.add(new RNStatusKeycardPackage());
            packages.add(new PushNotificationPackage());
            packages.add(new BlurViewPackage());
            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }


        @Override
        protected boolean isNewArchEnabled() {
            return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
        }

        @Override
        protected Boolean isHermesEnabled() {
            return BuildConfig.IS_HERMES_ENABLED;
        }

    };


    @Override
    public ReactNativeHost getReactNativeHost() {
       return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClientProvider.setOkHttpClientFactory(new StatusOkHttpClientFactory());

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW == "1");

        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            DefaultNewArchitectureEntryPoint.load();
        }

    }

}
