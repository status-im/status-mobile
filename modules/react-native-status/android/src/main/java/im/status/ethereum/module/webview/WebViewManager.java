package im.status.ethereum.module.webview;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.net.HttpURLConnection;

import android.app.Activity;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.reactnativecommunity.webview.RNCWebViewManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

import im.status.ethereum.function.Function;


import static okhttp3.internal.Util.UTF_8;
// TODO add service worker support

/**
 *
 */
@ReactModule(name = WebViewManager.REACT_CLASS)
public class WebViewManager extends RNCWebViewManager {

    public static class StatusWebView extends RNCWebViewManager.RNCWebView implements LifecycleEventListener {

        String injectedJS;
        String injectedOnStartLoadingJS;
        private boolean messagingEnabled = false;

        public StatusWebView(final ThemedReactContext reactContext) {
            super(reactContext);
        }

        @Override
        public void onHostDestroy() {
            //cleanupCallbacksAndDestroy()
        }

        public void setInjectedJavaScript(final String js) {
            injectedJS = js;
        }

        public void setInjectedOnStartLoadingJavaScript(final String js) {
            injectedOnStartLoadingJS = js;
        }

    }

    /* This name must match what we're referring to in JS */
    protected static final String REACT_CLASS = "StatusWebView.java";

    public final static String HEADER_CONTENT_TYPE = "content-type";

    private static final String MIME_TEXT_HTML = "text/html";
    private static final String MIME_UNKNOWN = "application/octet-stream";

    private OkHttpClient httpClient;

    public static boolean urlStringLooksInvalid(final String url) {
        return url == null ||
                url.trim().equals("") ||
                !(url.startsWith("http") && !url.startsWith("www")) ||
                url.contains("|");
    }

    public static boolean responseRequiresJSInjection(final Response response) {
        // we don't want to inject JS into redirects
        if (response.isRedirect()) {
            return false;
        }

        // ...okhttp appends charset to content type sometimes, like "text/html; charset=UTF8"
        final String contentTypeAndCharset = response.header(HEADER_CONTENT_TYPE, MIME_UNKNOWN);
        // ...and we only want to inject it in to HTML, really
        return contentTypeAndCharset.startsWith(MIME_TEXT_HTML);
    }

    public WebViewManager() {
        final Builder b = new Builder();
        httpClient = b
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected StatusWebView createRNCWebViewInstance(final ThemedReactContext reactContext) {
        return new StatusWebView(reactContext);
    }

    protected WebView createViewInstance(final ThemedReactContext reactContext) {
        final WebView webView = super.createViewInstance(reactContext);
        webView.addJavascriptInterface(new StatusBridge(webView), "WebViewBridge");

        final WebSettings settings = webView.getSettings();
        settings.setSafeBrowsingEnabled(true);
        return webView;
    }

    @Override
    protected void addEventEmitters(final ThemedReactContext reactContext, final WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {
                final String url = request.getUrl().toString();
                boolean onlyMainFrame = true;
                if (onlyMainFrame && !request.isForMainFrame()) {
                    return null;
                }

               // if (WebViewBridgeManager.urlStringLooksInvalid(url)) {
               //     return null;
               // }

                try {
                    final StatusWebView webView = new StatusWebView(reactContext);
                    final Request req = new Request.Builder()
                            .url(url)
                            .header("User-Agent", webView.getSettings().getUserAgentString())
                            .build();

                    final Response response = httpClient.newCall(req).execute();

                    //    if (!WebViewBridgeManager.responseRequiresJSInjection(response)) {
                    //        return null;
                    //    }


                    InputStream is = response.body().byteStream();
                    final MediaType contentType = response.body().contentType();
                    final Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        is = new InputStreamWithInjectedJS(is, webView.injectedOnStartLoadingJS, charset);
                    }

                    return new WebResourceResponse("text/html", charset.name(), is);
                } catch (IOException e) {
                    return null;
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                Log.d("ReactNative","request" + request.getUrl());
                // Requests matching one of allowed schemes and having an existing Activity are processed externally
                if (request.getUrl().getScheme().contains("aaaaaaaaaaaaa")) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(request.getUrl());
                    final Activity activity = reactContext.getCurrentActivity();
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        Log.d("ReactNative","Delegating request <" + request.getUrl() + ">");
                        activity.startActivity(intent);
                    }
                }
                return true;
            }
        });
    }

    @ReactProp(name = "initialInjectedJavaScript")
    public void setInitialInjectedJavaScript(final WebView view, final String code) {
        //((CustomWebView) view).setFinalUrl(url);
    }

}
