package im.status.ethereum.module;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import android.app.Activity;
import android.util.Log;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.view.WindowManager;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.EditText;
import android.content.Context;

public class UIHelper extends ReactContextBaseJavaModule {
    private static final String TAG = "UIHelper";
    private ReactApplicationContext reactContext;

    public UIHelper(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "UIHelper";
    }

    @ReactMethod
    public void setSoftInputMode(final int mode) {
        Log.d(TAG, "setSoftInputMode");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(mode);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @ReactMethod
    public void clearCookies() {
        Log.d(TAG, "clearCookies");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(activity);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    @ReactMethod
    public void toggleWebviewDebug(final boolean val) {
        Log.d(TAG, "toggleWebviewDebug");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView.setWebContentsDebuggingEnabled(val);
            }
        });
    }

    @ReactMethod
    public void clearStorageAPIs() {
        Log.d(TAG, "clearStorageAPIs");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        WebStorage storage = WebStorage.getInstance();
        if (storage != null) {
            storage.deleteAllData();
        }
    }

    @ReactMethod
    public void resetKeyboardInputCursor(final int reactTagToReset, final int selection) {
        UIManagerModule uiManager = getReactApplicationContext().getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            @Override
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                InputMethodManager imm = (InputMethodManager) getReactApplicationContext().getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    View viewToReset = nativeViewHierarchyManager.resolveView(reactTagToReset);
                    imm.restartInput(viewToReset);
                    try {
                        EditText textView = (EditText) viewToReset;
                        textView.setSelection(selection);
                    } catch (Exception e) {}
                }
            }
        });
    }

}
