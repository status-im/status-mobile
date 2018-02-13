package im.status.ethereum;

import android.support.multidex.MultiDexApplication;
import com.BV.LinearGradient.LinearGradientPackage;
import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.bitgo.randombytes.RandomBytesPackage;
import org.devio.rn.splashscreen.SplashScreenReactPackage;
import com.facebook.react.ReactApplication;
import com.ocetnik.timer.BackgroundTimerPackage;
import com.horcrux.svg.SvgPackage;
import com.evollu.react.fcm.FIRMessagingPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import com.github.yamill.orientation.OrientationPackage;
import com.AlexanderZaytsev.RNI18n.RNI18nPackage;
import com.instabug.reactlibrary.RNInstabugReactnativePackage;
import org.reactnative.camera.RNCameraPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.rnfs.RNFSPackage;
import es.tiarg.nfcreactnative.NfcReactNativePackage;
import fr.bamlab.rnimageresizer.ImageResizerPackage;
import im.status.ethereum.module.StatusPackage;
import io.realm.react.RealmReactPackage;
import me.alwx.HttpServer.HttpServerReactPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import im.status.ethereum.function.Function;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            boolean devCluster = false;
            if (BuildConfig.ETHEREUM_DEV_CLUSTER == "1") {
                devCluster = true;
            }

            boolean jscEnabled = false;
            if (BuildConfig.JSC_ENABLED == "1") {
                jscEnabled = true;
            }

            StatusPackage statusPackage = new StatusPackage(BuildConfig.DEBUG, devCluster, jscEnabled, BuildConfig.LOG_LEVEL_STATUS_GO);
            Function<String, String> callRPC = statusPackage.getCallRPC();
            List<ReactPackage> packages = new ArrayList<ReactPackage>(Arrays.asList(
                    new MainReactPackage(),
                    new BackgroundTimerPackage(),
                    new SvgPackage(),
                    new FIRMessagingPackage(),
                    new HttpServerReactPackage(),
                    new NfcReactNativePackage(),
                    new SplashScreenReactPackage(),
                    statusPackage,
                    new RealmReactPackage(),
                    new VectorIconsPackage(),
                    new RNI18nPackage(),
                    new RandomBytesPackage(),
                    new LinearGradientPackage(),
                    new RNCameraPackage(),
                    new OrientationPackage(),
                    new RNFSPackage(),
                    new ReactNativeDialogsPackage(),
                    new ImageResizerPackage(),
                    new PickerPackage(),
                    new WebViewBridgePackage(BuildConfig.DEBUG, callRPC),
                    new ReactNativeConfigPackage()
                                                                                    ));

            if (!BuildConfig.DEBUG) {
                packages.add(new RNInstabugReactnativePackage("b239f82a9cb00464e4c72cc703e6821e", MainApplication.this, "shake"));
            }

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
