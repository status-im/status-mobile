package im.status.ethereum;

import androidx.multidex.MultiDexApplication;
import android.util.Log;
import android.content.Context;
import android.webkit.WebView;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.PackageList;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.facebook.react.ReactApplication;
import cl.json.RNSharePackage;
import com.facebook.react.ReactNativeHost;
import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.react.NavigationReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.modules.network.OkHttpClientProvider;

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

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClientProvider.setOkHttpClientFactory(new StatusOkHttpClientFactory());

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW == "1");
        initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
    }
    /**
     * Loads Flipper in React Native templates. Call this in the onCreate method with something like
     * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
     *
     * @param context
     * @param reactInstanceManager
     */
    private static void initializeFlipper(
          Context context, ReactInstanceManager reactInstanceManager) {
        if (BuildConfig.DEBUG) {
            try {
                /*
                  We use reflection here to pick up the class that initializes Flipper,
                  since Flipper library is not available in release mode
                */
                Class<?> aClass = Class.forName("im.status.ethereum.ReactNativeFlipper");
                aClass
                    .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
                    .invoke(null, context, reactInstanceManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
