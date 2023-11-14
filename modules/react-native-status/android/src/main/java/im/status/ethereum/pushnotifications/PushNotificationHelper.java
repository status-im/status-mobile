// https://github.com/zo0r/react-native-push-notification/blob/bedc8f646aab67d594f291449fbfa24e07b64fe8/android/src/main/java/com/dieam/reactnativepushnotification/modules/RNPushNotificationHelper.java Copy-Paste with removed firebase
package im.status.ethereum.pushnotifications;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.Base64;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import im.status.ethereum.module.R;
import static im.status.ethereum.pushnotifications.PushNotification.LOG_TAG;

public class PushNotificationHelper {

    private Context context;

    private static final long DEFAULT_VIBRATION = 300L;
    private static final String CHANNEL_ID = "status-im-notifications";
    public static final String ACTION_DELETE_NOTIFICATION = "im.status.ethereum.module.DELETE_NOTIFICATION";
    public static final String ACTION_TAP_STOP = "im.status.ethereum.module.TAP_STOP";
    final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_CANCEL_CURRENT;

    private NotificationManager notificationManager;


    private HashMap<String, Person> persons;
    private HashMap<String, StatusMessageGroup> messageGroups;

    private IntentFilter intentFilter;

    public PushNotificationHelper(Application context, IntentFilter intentFilter) {
        this.context = context;
        this.intentFilter = intentFilter;
        this.persons = new HashMap<String, Person>();
        this.messageGroups = new HashMap<String, StatusMessageGroup>();
        this.notificationManager = context.getSystemService(NotificationManager.class);
        this.registerBroadcastReceiver();
    }

    public Intent getOpenAppIntent(String deepLink) {
        Class intentClass;
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            intentClass =  Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Intent intent = new Intent(context, intentClass);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setAction(Intent.ACTION_VIEW);
        //NOTE: you might wonder, why the heck did he decide to set these flags in particular. Well,
        //the answer is a simple as it can get in the Android native development world. I noticed
        //that my initial setup was opening the app but wasn't triggering any events on the js side, like
        //the links do from the browser. So I compared both intents and noticed that the link from
        //the browser produces an intent with the flag 0x14000000. I found out that it was the following
        //flags in this link:
        //https://stackoverflow.com/questions/52390129/android-intent-setflags-issue
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (deepLink != null) {
            intent.setData(Uri.parse(deepLink));
        }
        return intent;
    }

    //NOTE: we use a dynamically created BroadcastReceiver here so that we can capture
    //intents from notifications and act on them. For instance when tapping/dismissing
    //a chat notification we want to clear the chat so that next messages don't show
    //the messages that we have seen again
    private final BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == ACTION_DELETE_NOTIFICATION) {
                    String groupId = intent.getExtras().getString("im.status.ethereum.groupId");
                    if (groupId != null) {
                        cleanGroup(groupId);
                    }
                }
                if (intent.getAction() == ACTION_TAP_STOP) {
                    stop();
                    System.exit(0);
                }
                Log.e(LOG_TAG, "intent received: " + intent.getAction());
            }
        };

    public void registerBroadcastReceiver() {
        this.intentFilter.addAction(ACTION_DELETE_NOTIFICATION);
        this.intentFilter.addAction(ACTION_TAP_STOP);
        context.registerReceiver(notificationActionReceiver, this.intentFilter);
        Log.e(LOG_TAG, "Broadcast Receiver registered");
    }



    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void invokeApp(Bundle bundle) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();

        try {
            Class<?> activityClass = Class.forName(className);
            Intent activityIntent = new Intent(context, activityClass);

            if(bundle != null) {
                activityIntent.putExtra("notification", bundle);
            }

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(activityIntent);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Class not found", e);
            return;
        }
    }

    public void sendToNotificationCentre(final Bundle bundle) {
        PushNotificationPicturesAggregator aggregator = new PushNotificationPicturesAggregator(new PushNotificationPicturesAggregator.Callback() {
                public void call(Bitmap largeIconImage, Bitmap bigPictureImage) {
                    sendToNotificationCentreWithPicture(bundle, largeIconImage, bigPictureImage);
                }
            });

        aggregator.setLargeIconUrl(context, bundle.getString("largeIconUrl"));
        aggregator.setBigPictureUrl(context, bundle.getString("bigPictureUrl"));
    }

    public void handleConversation(final Bundle bundle) {
        if (bundle.getBoolean("deleted")){
            this.removeStatusMessage(bundle);
        } else {
            this.addStatusMessage(bundle);
        }
    }

    public void clearMessageNotifications(String conversationId) {
        notificationManager.cancel(conversationId.hashCode());
        cleanGroup(conversationId);
    }

    public void clearAllMessageNotifications() {
        notificationManager.cancelAll();
    }

    public void sendToNotificationCentreWithPicture(final Bundle bundle, Bitmap largeIconBitmap, Bitmap bigPictureBitmap) {

        try {
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                Log.e(LOG_TAG, "No activity class found for the notification");
                return;
            }

            if (bundle.getBoolean("isConversation")) {
              this.handleConversation(bundle);
              return;
            }

            if (bundle.getString("message") == null) {
                // this happens when a 'data' notification is received - we do not synthesize a local notification in this case
                Log.d(LOG_TAG, "Ignore this message if you sent data-only notification. Cannot send to notification centre because there is no 'message' field in: " + bundle);
                return;
            }

            String notificationIdString = bundle.getString("id");
            if (notificationIdString == null) {
                Log.e(LOG_TAG, "No notification ID specified for the notification");
                return;
            }

            Resources res = context.getResources();
            String packageName = context.getPackageName();

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = context.getApplicationInfo();
                title = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }

            int priority = NotificationCompat.PRIORITY_HIGH;
            final String priorityString = bundle.getString("priority");

            if (priorityString != null) {
                switch (priorityString.toLowerCase()) {
                    case "max":
                        priority = NotificationCompat.PRIORITY_MAX;
                        break;
                    case "high":
                        priority = NotificationCompat.PRIORITY_HIGH;
                        break;
                    case "low":
                        priority = NotificationCompat.PRIORITY_LOW;
                        break;
                    case "min":
                        priority = NotificationCompat.PRIORITY_MIN;
                        break;
                    case "default":
                        priority = NotificationCompat.PRIORITY_DEFAULT;
                        break;
                    default:
                        priority = NotificationCompat.PRIORITY_HIGH;
                }
            }

            int visibility = NotificationCompat.VISIBILITY_PRIVATE;
            final String visibilityString = bundle.getString("visibility");

            if (visibilityString != null) {
                switch (visibilityString.toLowerCase()) {
                    case "private":
                        visibility = NotificationCompat.VISIBILITY_PRIVATE;
                        break;
                    case "public":
                        visibility = NotificationCompat.VISIBILITY_PUBLIC;
                        break;
                    case "secret":
                        visibility = NotificationCompat.VISIBILITY_SECRET;
                        break;
                    default:
                        visibility = NotificationCompat.VISIBILITY_PRIVATE;
                }
            }

            String channel_id = bundle.getString("channelId");

            if(channel_id == null) {
                channel_id = this.getNotificationDefaultChannelId();
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channel_id)
                    .setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(visibility)
                    .setPriority(priority)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true))
                    .setOnlyAlertOnce(bundle.getBoolean("onlyAlertOnce", false));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24 and higher
                // Restore showing timestamp on Android 7+
                // Source: https://developer.android.com/reference/android/app/Notification.Builder.html#setShowWhen(boolean)
                boolean showWhen = bundle.getBoolean("showWhen", true);

                notification.setShowWhen(showWhen);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26 and higher
                // Changing Default mode of notification
                notification.setDefaults(Notification.DEFAULT_LIGHTS);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { // API 20 and higher
              String group = bundle.getString("group");

              if (group != null) {
                  notification.setGroup(group);
              }

              if (bundle.containsKey("groupSummary") || bundle.getBoolean("groupSummary")) {
                  notification.setGroupSummary(bundle.getBoolean("groupSummary"));
              }
            }

            String numberString = bundle.getString("number");

            if (numberString != null) {
                notification.setNumber(Integer.parseInt(numberString));
            }

            // Small icon
            int smallIconResId = 0;

            String smallIcon = bundle.getString("smallIcon");

            if (smallIcon != null && !smallIcon.isEmpty()) {
              smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
            } else if(smallIcon == null) {
              smallIconResId = res.getIdentifier("ic_stat_notify_status", "drawable", packageName);
            }

            if (smallIconResId == 0) {
                smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

                if (smallIconResId == 0) {
                    smallIconResId = android.R.drawable.ic_dialog_info;
                }
            }

            notification.setSmallIcon(smallIconResId);

            // Large icon
            if(largeIconBitmap == null) {
                int largeIconResId = 0;

                String largeIcon = bundle.getString("largeIcon");

                if (largeIcon != null && !largeIcon.isEmpty()) {
                  largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
                } else if(largeIcon == null) {
                  largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
                }

                // Before Lolipop there was no large icon for notifications.
                if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                    largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);
                }
            }

            Bundle author = bundle.getBundle("notificationAuthor");

            if (largeIconBitmap == null && author != null) {
                String base64Image = author.getString("icon").split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                notification.setLargeIcon(getCircleBitmap(decodedByte));
            } else if (largeIconBitmap != null){
              notification.setLargeIcon(largeIconBitmap);
            }

            String message = bundle.getString("message");

            notification.setContentText(message);

            String subText = bundle.getString("subText");

            if (subText != null) {
                notification.setSubText(subText);
            }

            String bigText = bundle.getString("bigText");

            if (bigText == null) {
                bigText = message;
            }

            NotificationCompat.Style style;

            if(bigPictureBitmap != null) {
              style = new NotificationCompat.BigPictureStyle()
                      .bigPicture(bigPictureBitmap)
                      .setBigContentTitle(title)
                      .setSummaryText(message);
            } else {
              style = new NotificationCompat.BigTextStyle().bigText(bigText);
            }

            notification.setStyle(style);

            Intent intent = new Intent(context, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("foreground", this.isApplicationInForeground());
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            Uri soundUri = null;

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                String soundName = bundle.getString("soundName");

                if (soundName == null) {
                    soundName = "default";
                }

                soundUri = getSoundUri(soundName);

                notification.setSound(soundUri);
            }

            if (soundUri == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.setSound(null);
            }

            if (bundle.containsKey("ongoing") || bundle.getBoolean("ongoing")) {
                notification.setOngoing(bundle.getBoolean("ongoing"));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                int defaultColor = -1;
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                } else if (defaultColor != -1) {
                    notification.setColor(defaultColor);
                }
            }

            int notificationID = notificationIdString.hashCode();

            notification.setContentIntent(createOnTapIntent(context, notificationID, bundle.getString("deepLink")))
              .setDeleteIntent(createOnDismissedIntent(context, notificationID, bundle.getString("deepLink")));

            NotificationManager notificationManager = notificationManager();

            long[] vibratePattern = new long[]{0};

            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
                if (vibration == 0)
                    vibration = DEFAULT_VIBRATION;

                vibratePattern = new long[]{0, vibration};

                notification.setVibrate(vibratePattern);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              // Define the shortcutId
              String shortcutId = bundle.getString("shortcutId");

              if (shortcutId != null) {
                notification.setShortcutId(shortcutId);
              }

              Long timeoutAfter = (long) bundle.getDouble("timeoutAfter");

              if (timeoutAfter != null && timeoutAfter >= 0) {
                notification.setTimeoutAfter(timeoutAfter);
              }
            }

            Long when = (long) bundle.getDouble("when");

            if (when != null && when >= 0) {
              notification.setWhen(when);
            }

            notification.setUsesChronometer(bundle.getBoolean("usesChronometer", false));
            notification.setChannelId(channel_id);

            JSONArray actionsArray = null;
            try {
                actionsArray = bundle.getString("actions") != null ? new JSONArray(bundle.getString("actions")) : null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Exception while converting actions to JSON object.", e);
            }

            if (actionsArray != null) {
                // No icon for now. The icon value of 0 shows no icon.
                int icon = 0;

                // Add button for each actions.
                for (int i = 0; i < actionsArray.length(); i++) {
                    String action;
                    try {
                        action = actionsArray.getString(i);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Exception while getting action from actionsArray.", e);
                        continue;
                    }


                    Intent actionIntent = new Intent(context, PushNotificationActions.class);
                    actionIntent.setAction(packageName + ".ACTION_" + i);

                    actionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    // Add "action" for later identifying which button gets pressed.
                    bundle.putString("action", action);
                    actionIntent.putExtra("notification", bundle);
                    actionIntent.setPackage(packageName);

                    PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, notificationID, actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      notification.addAction(new NotificationCompat.Action.Builder(icon, action, pendingActionIntent).build());
                    } else {
                      notification.addAction(icon, action, pendingActionIntent);
                    }
                }
            }


            if (!(this.isApplicationInForeground() && bundle.getBoolean("ignoreInForeground"))) {
                Notification info = notification.build();
                info.defaults |= Notification.DEFAULT_LIGHTS;

                if (bundle.containsKey("tag")) {
                    String tag = bundle.getString("tag");
                    notificationManager.notify(tag, notificationID, info);
                } else {
                    notificationManager.notify(notificationID, info);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to send push notification", e);
        }
    }

    private boolean checkOrCreateChannel(NotificationManager manager, String channel_id, String channel_name, String channel_description, Uri soundUri, int importance, long[] vibratePattern, boolean showBadge) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false;
        if (manager == null)
            return false;

        NotificationChannel channel = manager.getNotificationChannel(channel_id);

        if (
          channel == null && channel_name != null && channel_description != null ||
          channel != null &&
          (
            channel_name != null && !channel.getName().equals(channel_name) ||
            channel_description != null && !channel.getDescription().equals(channel_description)
          )
        ) {
            // If channel doesn't exist create a new one.
            // If channel name or description is updated then update the existing channel.
            channel = new NotificationChannel(channel_id, channel_name, importance);

            channel.setDescription(channel_description);
            channel.enableLights(true);
            channel.enableVibration(vibratePattern != null);
            channel.setVibrationPattern(vibratePattern);
            channel.setShowBadge(showBadge);

            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                channel.setSound(soundUri, audioAttributes);
            } else {
                channel.setSound(null, null);
            }

            manager.createNotificationChannel(channel);

            return true;
        }

        return false;
    }

    public boolean createChannel(ReadableMap channelInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false;

        String channelId = channelInfo.getString("channelId");
        String channelName = channelInfo.getString("channelName");
        String channelDescription = channelInfo.hasKey("channelDescription") ? channelInfo.getString("channelDescription") : "";
        String soundName = channelInfo.hasKey("soundName") ? channelInfo.getString("soundName") : "default";
        int importance = channelInfo.hasKey("importance") ? channelInfo.getInt("importance") : 4;
        boolean vibrate = channelInfo.hasKey("vibrate") && channelInfo.getBoolean("vibrate");
        long[] vibratePattern = vibrate ? new long[] { DEFAULT_VIBRATION } : null;
        boolean showBadge = channelInfo.hasKey("showBadge") && channelInfo.getBoolean("showBadge");

        NotificationManager manager = notificationManager();

        Uri soundUri = getSoundUri(soundName);

        return checkOrCreateChannel(manager, channelId, channelName, channelDescription, soundUri, importance, vibratePattern, showBadge);
    }

    public String getNotificationDefaultChannelId() {
        return this.CHANNEL_ID;
    }

    private Uri getSoundUri(String soundName) {
        if (soundName == null || "default".equalsIgnoreCase(soundName)) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {

            // sound name can be full filename, or just the resource name.
            // So the strings 'my_sound.mp3' AND 'my_sound' are accepted
            // The reason is to make the iOS and android javascript interfaces compatible

            int resId;
            if (context.getResources().getIdentifier(soundName, "raw", context.getPackageName()) != 0) {
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
            } else {
                soundName = soundName.substring(0, soundName.lastIndexOf('.'));
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
            }

            return Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
        }
    }

    public Class getMainActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isApplicationInForeground() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(context.getPackageName())
                    && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && processInfo.pkgList.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
    
    private Person getPerson(Bundle bundle) {
     String name = bundle.getString("name");

      return new Person.Builder().setName(name).build();
    }

    private StatusMessage createMessage(Bundle data) {
        Person author = getPerson(data.getBundle("notificationAuthor"));
        long timeStampLongValue = (long) data.getDouble("timestamp");
        return new StatusMessage(data.getString("id"), author, timeStampLongValue, data.getString("message"));
    }

    private PendingIntent createGroupOnDismissedIntent(Context context, int notificationId, String groupId, String deepLink) {
        Intent intent = new Intent(ACTION_DELETE_NOTIFICATION);
        intent.putExtra("im.status.ethereum.deepLink", deepLink);
        intent.putExtra("im.status.ethereum.groupId", groupId);
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, flag);
    }

    private PendingIntent createGroupOnTapIntent(Context context, int notificationId, String groupId, String deepLink) {
        Intent intent = getOpenAppIntent(deepLink);
        return PendingIntent.getActivity(context.getApplicationContext(), notificationId, intent, flag);
   }

    private PendingIntent createOnTapIntent(Context context, int notificationId, String deepLink) {
        Intent intent = getOpenAppIntent(deepLink);
        return PendingIntent.getActivity(context.getApplicationContext(), notificationId, intent, flag);
   }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId, String deepLink) {
        Intent intent = new Intent(ACTION_DELETE_NOTIFICATION);
        intent.putExtra("im.status.ethereum.deepLink", deepLink);
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, flag);
    }

    public void removeStatusMessage(Bundle bundle) {
      String conversationId = bundle.getString("conversationId");
      StatusMessageGroup group = this.messageGroups.get(conversationId);
      NotificationManager notificationManager = notificationManager();

      if (group == null) {
        group = new StatusMessageGroup(conversationId);
      }

      this.messageGroups.put(conversationId, group);

      String id = bundle.getString("id");
      group.removeMessage(id);

      this.showMessages(bundle);
    }

    public StatusMessageGroup getMessageGroup(String conversationId) {
      return this.messageGroups.get(conversationId);
    }

    public void addStatusMessage(Bundle bundle) {
      String conversationId = bundle.getString("conversationId");
      StatusMessageGroup group = this.messageGroups.get(conversationId);
      NotificationManager notificationManager = notificationManager();

      if (group == null) {
        group = new StatusMessageGroup(conversationId);
      }

      this.messageGroups.put(conversationId, group);

      group.addMessage(createMessage(bundle));

      this.showMessages(bundle);
    }

    public void showMessages(Bundle bundle) {
      String conversationId = bundle.getString("conversationId");
      StatusMessageGroup group = this.messageGroups.get(conversationId);
      NotificationManager notificationManager = notificationManager();

      NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Me");
      ArrayList<StatusMessage> messages = group.getMessages();

      if (messages.size() == 0) {
          notificationManager.cancel(conversationId.hashCode());
          return;
      }

      for (int i = 0; i < messages.size(); i++) {
        StatusMessage message = messages.get(i);
        messagingStyle.addMessage(message.getText(),
            message.getTimestamp(),
            message.getAuthor());
      }

      if (bundle.getString("title") != null) {
        messagingStyle.setConversationTitle(bundle.getString("title"));
      }

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_notify_status)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setStyle(messagingStyle)
        .setGroup(conversationId)
        .setOnlyAlertOnce(true)
        .setGroupSummary(true)
        .setContentIntent(createGroupOnTapIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
        .setDeleteIntent(createGroupOnDismissedIntent(context, conversationId.hashCode(), conversationId, bundle.getString("deepLink")))
        .setNumber(messages.size() + 1)
        .setAutoCancel(true);
      if (Build.VERSION.SDK_INT >= 21) {
        builder.setVibrate(new long[0]);
      }
      notificationManager.notify(conversationId.hashCode(), builder.build());
    }

    class StatusMessageGroup {
      private ArrayList<StatusMessage> messages;
      private String id;

      StatusMessageGroup(String id) {
        this.id = id;
        this.messages = new ArrayList<StatusMessage>();
      }
      public ArrayList<StatusMessage> getMessages() {
        return messages;
      }

      public void addMessage(StatusMessage message) {
        this.messages.add(message);
      }

      public void removeMessage(String id) {
        ArrayList<StatusMessage> newMessages = new ArrayList<StatusMessage>();
        for(StatusMessage message: this.messages) {
            if(!message.id.equals(id)) {
                newMessages.add(message);
            }
        }
        this.messages = newMessages;
      }

      public String getId() {
        return this.id;
      }
    }

    class StatusMessage {
      public Person getAuthor() {
        return author;
      }

      public long getTimestamp() {
        return timestamp;
      }

      public String getText() {
        return text;
      }

      private String id;
      private Person author;
      private long timestamp;
      private String text;

      StatusMessage(String id, Person author, long timestamp, String text) {
        this.id = id;
        this.author = author;
        this.timestamp = timestamp;
        this.text = text;
      }
    }

    private void removeGroup(String groupId) {
        this.messageGroups.remove(groupId);
    }

    private void cleanGroup(String groupId) {
        removeGroup(groupId);
        if (messageGroups.size() == 0) {
            notificationManager.cancelAll();
        }
    }

    public void start() {
        Log.e(LOG_TAG, "Starting Foreground Service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.startService(serviceIntent);
        this.registerBroadcastReceiver();
    }

    public void stop() {
        Log.e(LOG_TAG, "Stopping Foreground Service");
        //NOTE: we cancel all the current notifications, because the intents can't be used anymore
        //since the broadcast receiver will be killed as well and won't be able to handle any intent
        notificationManager.cancelAll();
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
        context.unregisterReceiver(notificationActionReceiver);
    }
}
