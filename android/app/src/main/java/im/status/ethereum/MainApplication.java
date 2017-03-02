package im.status.ethereum;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.pusherman.networkinfo.RNNetworkInfoPackage;
import me.alwx.HttpServer.HttpServerReactPackage;
import es.tiarg.nfcreactnative.NfcReactNativePackage;
import com.instabug.reactlibrary.RNInstabugReactnativePackage;
import com.cboy.rn.splashscreen.SplashScreenReactPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import im.status.ethereum.module.StatusPackage;
import io.realm.react.RealmReactPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.rt2zz.reactnativecontacts.ReactNativeContacts;
import com.i18n.reactnativei18n.ReactNativeI18n;
import com.bitgo.randombytes.RandomBytesPackage;
import com.BV.LinearGradient.LinearGradientPackage;
import com.lwansbrough.RCTCamera.*;
import com.centaurwarchief.smslistener.SmsListenerPackage;
import com.github.yamill.orientation.OrientationPackage;
import com.rnfs.RNFSPackage;
import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import fr.bamlab.rnimageresizer.ImageResizerPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import cl.json.RNSharePackage;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    protected boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.asList(
              new MainReactPackage(),
              new RNNetworkInfoPackage(),
              new HttpServerReactPackage(),
              new NfcReactNativePackage(),
              new RNInstabugReactnativePackage("b239f82a9cb00464e4c72cc703e6821e",MainApplication.this,"shake"),
              new SplashScreenReactPackage(),
              new StatusPackage(),
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
              new WebViewBridgePackage(),
              new RNSharePackage()
      );
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
      return mReactNativeHost;
  }
}
