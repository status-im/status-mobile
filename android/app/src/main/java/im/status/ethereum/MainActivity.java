package im.status.ethereum;

import android.content.Context;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.support.multidex.MultiDexApplication;
import android.os.Looper;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.facebook.react.ReactActivity;
import com.cboy.rn.splashscreen.SplashScreen;
import com.testfairy.TestFairy;

import java.util.Properties;

public class MainActivity extends ReactActivity {

    private static void registerUncaughtExceptionHandler(final Context context) {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable t) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure we get an Alert for every uncaught exceptions
        registerUncaughtExceptionHandler(MainActivity.this);

        // Report memory details for this application
        final ActivityManager activityManager = getActivityManager();
        Log.v("RNBootstrap", "Available system memory "+getAvailableMemory(activityManager).availMem + ", maximum usable application memory " + activityManager.getLargeMemoryClass()+"M");


        SplashScreen.show(this);
        super.onCreate(savedInstanceState);

        if(BuildConfig.TESTFAIRY_ENABLED == "1") {
            TestFairy.begin(this, "969f6c921cb435cea1d41d1ea3f5b247d6026d55");
        }

        if (!RootUtil.isDeviceRooted()) {
            configureStatus();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(getResources().getString(R.string.root_warning))
                    .setPositiveButton(getResources().getString(R.string.root_okay), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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

        Thread thread = new Thread() {
            @Override
            public void run() {
                System.loadLibrary("status-logs");
            }
        };

        thread.start();
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
}
