package im.status.ethereum;

import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.facebook.react.ReactActivity;
import com.cboy.rn.splashscreen.SplashScreen;
import com.testfairy.TestFairy;

import java.util.Properties;

public class MainActivity extends ReactActivity {

    private void registerUncaughtExceptionHandler() {
        // Make sure we get an Alert for every uncaught exceptions
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable t) {
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();

                        new AlertDialog.Builder(MainActivity.this)
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

    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
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
        registerUncaughtExceptionHandler();
        Log.v("RNBootstrap", "Available memory "+getAvailableMemory().availMem);

        SplashScreen.show(this);
        super.onCreate(savedInstanceState);
        TestFairy.begin(this, "969f6c921cb435cea1d41d1ea3f5b247d6026d55");

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
