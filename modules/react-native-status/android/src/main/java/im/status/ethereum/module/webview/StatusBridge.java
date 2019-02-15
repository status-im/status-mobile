package im.status.ethereum.module.webview;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 Wraps JS Objects made available in the WebView.
*/
public class StatusBridge {

    private final WebView webView;

    public StatusBridge(final WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void send(final String message) {
        final WritableMap event = Arguments.createMap();
        event.putString("message", message);
        final ReactContext reactContext = (ReactContext) this.webView.getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                this.webView.getId(),
                "topChange",
                event);
    }

}
