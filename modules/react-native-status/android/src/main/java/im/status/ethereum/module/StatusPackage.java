package im.status.ethereum.module;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.github.status_im.status_go.Statusgo;
import com.reactnativecommunity.webview.RNCWebViewPackage;

import im.status.ethereum.function.Function;
import im.status.ethereum.module.webview.WebViewManager;

public class StatusPackage extends RNCWebViewPackage {

    private boolean rootedDevice;

    public StatusPackage(final boolean rootedDevice) {
        this.rootedDevice = rootedDevice;
    }

    public StatusPackage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // Added in Kitkat
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final PackageInfo info = WebView.getCurrentWebViewPackage();
            if (info != null) {
                Log.d("ReactNative", "WebView version <" + info.versionName + ">");
            }
        }
    }

    @Override
    public List<NativeModule> createNativeModules(final ReactApplicationContext reactContext) {
        final List<NativeModule> modules = new ArrayList<>();
        System.loadLibrary("statusgoraw");
        System.loadLibrary("statusgo");
        modules.add(new StatusModule(reactContext, this.rootedDevice));

        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(final ReactApplicationContext reactContext) {
        final WebViewManager manager = new WebViewManager();
        manager.setPackage(this);
        return Arrays.<ViewManager>asList(manager);
    }

    public Function<String, String> getCallRPC() {
        return new Function<String, String>() {
            @Override
            public String apply(String payload) {
                return Statusgo.CallRPC(payload);
            }
        };
    }
}
