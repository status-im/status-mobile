package im.status.ethereum;

import android.content.Context;
import android.annotation.TargetApi;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.Settings;
import android.os.Bundle;
import android.os.Handler;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import com.reactnativenavigation.NavigationActivity;
import com.facebook.react.modules.core.PermissionListener;
import androidx.core.splashscreen.SplashScreen;

import java.util.Properties;
import im.status.ethereum.module.StatusThreadPoolExecutor;
import im.status.ethereum.MainApplication;

public class MainActivity extends NavigationActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{


    @Nullable private PermissionListener mPermissionListener;
    private boolean keepSplash = true;
    private final int SPLASH_DELAY = 3200;

     /**
       * Returns the name of the main component registered from JavaScript. This is used to schedule
       * rendering of the component.
       */
      protected String getMainComponentName() {
        return "StatusIm";
      }

    /**
       * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
       * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
       * (aka React 18) with two boolean flags.
       */
    /** MainActivity.java:64: error: incompatible types: MainActivity cannot be converted to ReactActivity this, ^
        protected ReactActivityDelegate createReactActivityDelegate() {
            return new DefaultReactActivityDelegate(
            this,
            getMainComponentName(),
            // If you opted-in for the New Architecture, we enable the Fabric Renderer.
             DefaultNewArchitectureEntryPoint.getFabricEnabled()
        );
      }
     */
    private static void registerUncaughtExceptionHandler(final Context context) {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable t) {
                // High priority, so don't use StatusThreadPoolExecutor
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();

                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage(t.toString())
                                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.dismiss();
                                        defaultUncaughtExceptionHandler.uncaughtException(thread, t);
                                    }
                                }).show();

                        Looper.loop();
                    }
                }.start();
            }
        });
    }

    private ActivityManager getActivityManager() {
        return (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
    }

    private ActivityManager.MemoryInfo getAvailableMemory(final ActivityManager activityManager) {
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    protected void configureStatus() {
        // Required because of crazy APN settings redirecting localhost (found in GB)
        Properties properties = System.getProperties();
        properties.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        properties.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");
    }

    private Intent createNotificationSettingsIntent() {
        final Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        return intent;
    }

    private void tryToEmit(String eventName, WritableMap event) {
        try {
            ((MainApplication) getApplication()).getReactNativeHost()
                                                .getReactInstanceManager()
                                                .getCurrentReactContext()
                                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                                .emit("url", event);
        } catch(Exception e) {/* we expect NPE on first start, which is OK because we have a fallback */}
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent.getDataString() != null && intent.getData().getScheme().startsWith("app-settings")) {
          startActivity(createNotificationSettingsIntent());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setTheme(R.style.DarkTheme);
        // Make sure we get an Alert for every uncaught exceptions
        registerUncaughtExceptionHandler(MainActivity.this);

        // Report memory details for this application
        final ActivityManager activityManager = getActivityManager();
        Log.v("RNBootstrap", "Available system memory "+getAvailableMemory(activityManager).availMem + ", maximum usable application memory " + activityManager.getLargeMemoryClass()+"M");

        setSecureFlag();

        // NOTE: Try to not restore the state https://github.com/software-mansion/react-native-screens/issues/17
        super.onCreate(null);

        if (!shouldShowRootedNotification()) {
            configureStatus();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(getResources().getString(R.string.root_warning))
                    .setPositiveButton(getResources().getString(R.string.root_okay), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rejectRootedNotification();
                            dialog.dismiss();
                            configureStatus();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.root_cancel), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MainActivity.this.finishAffinity();
                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            MainActivity.this.finishAffinity();
                        }
                    })
                    .create();

            dialog.show();
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.loadLibrary("status-logs");

                // when app is started but the Activity has been destroyed, the deep linking url event is
                // not emitted when coming back to foreground. This is a workaround. If the problem is
                // resolved in react-native this code should be removed
                if (getIntent().getData() != null) {
                    WritableMap event = Arguments.createMap();
                    event.putString("url", getIntent().getDataString());
                    // on first start emit will (silently) fail, but the regular deep linking handler will work
                    tryToEmit("url", event);
                }
            }
        };

        splashScreen.setKeepOnScreenCondition(() -> keepSplash);

        Handler handler = new Handler();
        handler.postDelayed(() -> keepSplash = false, SPLASH_DELAY);

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent = new Intent("onConfigurationChanged");
        intent.putExtra("newConfig", newConfig);
        this.sendBroadcast(intent);
    }

    private static final String REJECTED_ROOTED_NOTIFICATION = "rejectedRootedNotification";
    private static final Integer FREQUENCY_OF_REMINDER_IN_PERCENT = 5;

    private boolean shouldShowRootedNotification() {
        if (RootUtil.isDeviceRooted() && BuildConfig.ENABLE_ROOT_ALERT == "1") {
            if (userRejectedRootedNotification()) {
                return ((Math.random() * 100) < FREQUENCY_OF_REMINDER_IN_PERCENT);
            } else return true;
        } else {
            return false;
        }
    }

    private boolean userRejectedRootedNotification() {
        SharedPreferences preferences = getPreferences(0);
        return preferences.getBoolean(REJECTED_ROOTED_NOTIFICATION, false);
    }

    private void rejectRootedNotification() {
        SharedPreferences preferences = getPreferences(0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(REJECTED_ROOTED_NOTIFICATION, true);
        editor.commit();
    }

    private void setSecureFlag() {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean setSecure = sharedPrefs.getBoolean("BLANK_PREVIEW", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && setSecure) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        mPermissionListener = listener;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mPermissionListener != null && mPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            mPermissionListener = null;
        }
    }
}
