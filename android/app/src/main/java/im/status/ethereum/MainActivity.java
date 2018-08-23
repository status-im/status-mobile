package im.status.ethereum;

import android.content.Context;
import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.Settings;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.modules.core.PermissionListener;
import org.devio.rn.splashscreen.SplashScreen;
import com.testfairy.TestFairy;
import com.instabug.library.Instabug;

import java.util.Properties;
import im.status.ethereum.module.StatusThreadPoolExecutor;

public class MainActivity extends ReactActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    @Nullable private PermissionListener mPermissionListener;

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

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent.getDataString() != null && intent.getData().getScheme().startsWith("app-settings")) {
          startActivity(createNotificationSettingsIntent());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Make sure we get an Alert for every uncaught exceptions
        registerUncaughtExceptionHandler(MainActivity.this);

        // Report memory details for this application
        final ActivityManager activityManager = getActivityManager();
        Log.v("RNBootstrap", "Available system memory "+getAvailableMemory(activityManager).availMem + ", maximum usable application memory " + activityManager.getLargeMemoryClass()+"M");


        SplashScreen.show(this, true);
        super.onCreate(savedInstanceState);

        Instabug.setIntroMessageEnabled(false);
        if(BuildConfig.TESTFAIRY_ENABLED == "1") {
            TestFairy.begin(this, "969f6c921cb435cea1d41d1ea3f5b247d6026d55");
        }

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
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "StatusIm";
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
        if (RootUtil.isDeviceRooted()) {
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
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted. Start camera preview Activity.
            com.github.alinz.reactnativewebviewbridge.WebViewBridgeManager.grantAccess(requestCode);
        }
    }
}
