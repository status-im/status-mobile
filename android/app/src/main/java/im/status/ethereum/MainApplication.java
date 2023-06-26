package im.status.ethereum;

import androidx.multidex.MultiDexApplication;
import android.content.Context;
import android.webkit.WebView;
import im.status.ethereum.newarchitecture.MainApplicationReactNativeHost;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.PackageList;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.facebook.react.ReactApplication;
import cl.json.RNSharePackage;
import com.facebook.react.ReactNativeHost;
import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.react.NavigationReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.config.ReactFeatureFlags;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.reactnativecommunity.blurview.BlurViewPackage;

import java.util.List;

import im.status.ethereum.keycard.RNStatusKeycardPackage;
import im.status.ethereum.module.StatusPackage;
import im.status.ethereum.pushnotifications.PushNotificationPackage;
import im.status.ethereum.StatusOkHttpClientFactory;

import com.facebook.react.bridge.JSIModulePackage;
import com.swmansion.reanimated.ReanimatedJSIModulePackage;

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
            packages.add(new ReactNativeDialogsPackage());
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
	protected JSIModulePackage getJSIModulePackage() {
	    return new ReanimatedJSIModulePackage();
	}
    };

    private final ReactNativeHost mNewArchitectureNativeHost = new MainApplicationReactNativeHost(this);

    @Override
    public ReactNativeHost getReactNativeHost() {
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
          return mNewArchitectureNativeHost;
        } else {
          return mReactNativeHost;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If you opted-in for the New Architecture, we enable the TurboModule system
        ReactFeatureFlags.useTurboModules = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;

        OkHttpClientProvider.setOkHttpClientFactory(new StatusOkHttpClientFactory());

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW == "1");
    }

}
