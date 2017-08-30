package im.status.ethereum;

import android.support.multidex.MultiDexApplication;
import com.BV.LinearGradient.LinearGradientPackage;
import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.bitgo.randombytes.RandomBytesPackage;
import com.cboy.rn.splashscreen.SplashScreenReactPackage;
import com.centaurwarchief.smslistener.SmsListenerPackage;
import com.facebook.react.ReactApplication;
import com.horcrux.svg.SvgPackage;
import com.mapbox.reactnativemapboxgl.ReactNativeMapboxGLPackage;
import com.evollu.react.fcm.FIRMessagingPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import com.github.yamill.orientation.OrientationPackage;
import com.i18n.reactnativei18n.ReactNativeI18n;
import com.instabug.reactlibrary.RNInstabugReactnativePackage;
import com.lwansbrough.RCTCamera.RCTCameraPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.rnfs.RNFSPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import es.tiarg.nfcreactnative.NfcReactNativePackage;
import fr.bamlab.rnimageresizer.ImageResizerPackage;
import im.status.ethereum.module.StatusPackage;
import io.realm.react.RealmReactPackage;
import me.alwx.HttpServer.HttpServerReactPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            List<ReactPackage> packages = new ArrayList<ReactPackage>(Arrays.asList(
                    new MainReactPackage(),
                    new SvgPackage(),
                    new ReactNativeMapboxGLPackage(),
                    new FIRMessagingPackage(),
                    new HttpServerReactPackage(),
                    new NfcReactNativePackage(),
                    new SplashScreenReactPackage(),
                    new StatusPackage(BuildConfig.DEBUG),
                    new RealmReactPackage(),
                    new VectorIconsPackage(),
                    new ReactNativeContacts(),
                    new ReactNativeI18n(),
                    new RandomBytesPackage(),
                    new LinearGradientPackage(),
                    new RCTCameraPackage(),
                    new SmsListenerPackage(),
                    new OrientationPackage(),
                    new RNFSPackage(),
                    new ReactNativeDialogsPackage(),
                    new ImageResizerPackage(),
                    new PickerPackage(),
                    new WebViewBridgePackage(BuildConfig.DEBUG),
                    new ReactNativeConfigPackage()
                                                                                    ));

            if (!BuildConfig.DEBUG) {
                packages.add(new RNInstabugReactnativePackage("b239f82a9cb00464e4c72cc703e6821e", MainApplication.this, "shake"));
            }

            return packages;
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

}
