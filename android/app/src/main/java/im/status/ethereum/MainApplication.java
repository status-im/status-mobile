package im.status.ethereum;

import androidx.multidex.MultiDexApplication;
import android.util.Log;
import com.facebook.react.PackageList;
import com.facebook.hermes.reactexecutor.HermesExecutorFactory;
import com.facebook.react.bridge.JavaScriptExecutorFactory;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import com.reactnativecommunity.webview.RNCWebViewPackage;
import com.horcrux.svg.SvgPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.oblador.keychain.KeychainPackage;
import com.ocetnik.timer.BackgroundTimerPackage;
import com.reactcommunity.rnlanguages.RNLanguagesPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.rnfs.RNFSPackage;
import com.rnfingerprint.FingerprintAuthPackage;

import net.rhogan.rnsecurerandom.RNSecureRandomPackage;

import org.devio.rn.splashscreen.SplashScreenReactPackage;
import org.reactnative.camera.RNCameraPackage;

import java.util.Arrays;
import java.util.List;

import fr.bamlab.rnimageresizer.ImageResizerPackage;
import im.status.ethereum.function.Function;
import im.status.ethereum.keycard.RNStatusKeycardPackage;
import im.status.ethereum.module.StatusPackage;
import io.invertase.firebase.RNFirebasePackage;
import io.invertase.firebase.messaging.RNFirebaseMessagingPackage;
import io.invertase.firebase.notifications.RNFirebaseNotificationsPackage;
import me.alwx.HttpServer.HttpServerReactPackage;
import com.chirag.RNMail.*;
import com.clipsub.RNShake.RNShakeEventPackage;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.swmansion.rnscreens.RNScreensPackage;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            StatusPackage statusPackage = new StatusPackage(RootUtil.isDeviceRooted());
            Function<String, String> callRPC = statusPackage.getCallRPC();
            List<ReactPackage> packages = new PackageList(this).getPackages();
            packages.add(statusPackage);
            packages.add(new RNFirebaseMessagingPackage());
            packages.add(new RNFirebaseNotificationsPackage());
            packages.add(new ReactNativeDialogsPackage());
            packages.add(new RNStatusKeycardPackage());
            packages.add(new WebViewBridgePackage(BuildConfig.DEBUG_WEBVIEW == "1", callRPC));
            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return "index.android";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }
}
