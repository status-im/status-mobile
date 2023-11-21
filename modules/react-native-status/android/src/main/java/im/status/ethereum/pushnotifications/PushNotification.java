package im.status.ethereum.pushnotifications;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import java.security.SecureRandom;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import android.util.Log;

import im.status.ethereum.pushnotifications.PushNotificationJsDelivery;

public class PushNotification extends ReactContextBaseJavaModule implements ActivityEventListener {
    public static final String LOG_TAG = "PushNotification";

    private final SecureRandom mRandomNumberGenerator = new SecureRandom();
    private PushNotificationHelper pushNotificationHelper;
    private PushNotificationJsDelivery delivery;
    private ReactApplicationContext reactContext;
    private boolean started;

    public PushNotification(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        Application applicationContext = (Application) reactContext.getApplicationContext();

        IntentFilter intentFilter = new IntentFilter();
        pushNotificationHelper = new PushNotificationHelper(applicationContext, intentFilter);

        delivery = new PushNotificationJsDelivery(reactContext);
    }

    @Override
    public String getName() {
        return "PushNotification";
    }

    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    private Bundle getBundleFromIntent(Intent intent) {
        Bundle bundle = null;
        if (intent.hasExtra("notification")) {
            bundle = intent.getBundleExtra("notification");
        } else if (intent.hasExtra("google.message_id")) {
            bundle = new Bundle();

            bundle.putBundle("data", intent.getExtras());
        }

        if(null != bundle && !bundle.getBoolean("foreground", false) && !bundle.containsKey("userInteraction")) {
            bundle.putBoolean("userInteraction", true);
        }

        return bundle;
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle bundle = this.getBundleFromIntent(intent);
        if (bundle != null) {
            delivery.notifyNotification(bundle);
        }
    }

    @ReactMethod
    /**
     * Creates a channel if it does not already exist. Returns whether the channel was created.
     */
    public void createChannel(ReadableMap channelInfo, Callback callback) {
        boolean created = pushNotificationHelper.createChannel(channelInfo);

        if(callback != null) {
            callback.invoke(created);
        }
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        if (!this.started) {
          return;
        }

        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }

        pushNotificationHelper.sendToNotificationCentre(bundle);
    }

    @ReactMethod
    public void clearMessageNotifications(String conversationId) {
        if (this.started) {
            pushNotificationHelper.clearMessageNotifications(conversationId);
        }
    }

    @ReactMethod
    public void clearAllMessageNotifications() {
        pushNotificationHelper.clearAllMessageNotifications();
    }

    @ReactMethod
    public void enableNotifications() {
        this.started = true;
        this.pushNotificationHelper.start();
    }

    @ReactMethod
    public void disableNotifications() {
      if (this.started) {
        this.started = false;
        this.pushNotificationHelper.stop();
      }
    }
}
