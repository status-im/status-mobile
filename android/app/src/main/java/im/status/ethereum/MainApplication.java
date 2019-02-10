package im.status.ethereum;

import android.support.multidex.MultiDexApplication;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import com.horcrux.svg.SvgPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.oblador.keychain.KeychainPackage;
import com.ocetnik.timer.BackgroundTimerPackage;
import com.reactcommunity.rnlanguages.RNLanguagesPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.rnfs.RNFSPackage;

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
import io.realm.react.RealmReactPackage;
import me.alwx.HttpServer.HttpServerReactPackage;

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
            return Arrays.asList(
                    new MainReactPackage(),
                    new RNFirebasePackage(),
                    new RNFirebaseMessagingPackage(),
                    new RNFirebaseNotificationsPackage(),
                    new RNSecureRandomPackage(),
                    new BackgroundTimerPackage(),
                    new SvgPackage(),
                    new HttpServerReactPackage(),
                    new SplashScreenReactPackage(),
                    statusPackage,
                    new RNStatusKeycardPackage(),
                    new RealmReactPackage(),
                    new RNLanguagesPackage(),
                    new RNCameraPackage(),
                    new RNFSPackage(),
                    new ReactNativeDialogsPackage(),
                    new ImageResizerPackage(),
                    new PickerPackage(),
                    new WebViewBridgePackage(BuildConfig.DEBUG_WEBVIEW == "1", callRPC),
                    new ReactNativeConfigPackage(),
                    new KeychainPackage());
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
