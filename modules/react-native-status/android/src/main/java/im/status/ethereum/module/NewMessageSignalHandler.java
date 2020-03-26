package im.status.ethereum.module;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import androidx.core.app.Person;
import androidx.core.app.Person.Builder;

import android.util.Base64;

import androidx.core.graphics.drawable.IconCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.net.Uri;
import android.media.AudioAttributes;

import android.util.Log;

public class NewMessageSignalHandler {
    //NOTE: currently we only show notifications for 1-1 chats, in the future we
    //will most likely extend to other kind of notifications. The first step will
    //be to define actions for these notifications, add it to the filter in
    //`registerBroadcastReceiver` method, and add some action specific code
    //in notificationActionReceiver.
    public static final String ACTION_DELETE_NOTIFICATION = "im.status.ethereum.module.DELETE_NOTIFICATION";
    public static final String ACTION_TAP_NOTIFICATION = "im.status.ethereum.module.TAP_NOTIFICATION";
    private static final String GROUP_STATUS_MESSAGES = "im.status.notifications.message";
    private static final String CHANNEL_NAME = "Status";
    private static final String CHANNEL_DESCRIPTION = "Get notifications on new messages and mentions";
    private static final String CHANNEL_ID = "status-chat-notifications";
    private static final String TAG = "StatusModule";
    private NotificationManager notificationManager;
    private HashMap<String, Person> persons;
    private HashMap<String, StatusChat> chats;
    private Context context;
    private Intent serviceIntent;
    private Boolean shouldRefreshNotifications;

    //NOTE: we use a dynamically created BroadcastReceiver here so that we can capture
    //intents from notifications and act on them. For instance when tapping/dismissing
    //a chat notification we want to clear the chat so that next messages don't show
    //the messages that we have seen again
    private final BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String chatId = intent.getExtras().getString("im.status.ethereum.chatId");
                if (intent.getAction() == ACTION_TAP_NOTIFICATION) {
                    context.startActivity(getOpenAppIntent(chatId));
                }
                removeChat(chatId);
                // clean up the group notifications when there is no
                // more unread chats
                if (chats.size() == 0) {
                    notificationManager.cancelAll();
                }
                Log.e(TAG, "intent received: " + intent.getAction());
            }
        };

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DELETE_NOTIFICATION);
        filter.addAction(ACTION_TAP_NOTIFICATION);
        context.registerReceiver(notificationActionReceiver, filter);
        Log.e(TAG, "Broadcast Receiver registered");
    }

    //NOTE: this method takes a chatId and returns an intent that will open the app in that chat
    //Once we support other kind of notifications we will need to adapt it. The simplest method
    //is probably to pass the universal link as param instead of the chatId.
    public Intent getOpenAppIntent(String chatId) {
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
        intent.setData(Uri.parse("status-im://chat/private/" + chatId));
        return intent;
    }

    public NewMessageSignalHandler(Context context) {
        // NOTE: when instanciated the NewMessageSignalHandler class starts a foreground service
        // to keep the app running in the background in order to receive notifications
        // call the stop() method in order to stop the service
        this.context = context;
        this.persons = new HashMap<String, Person>();
        this.chats = new HashMap<String, StatusChat>();
        this.notificationManager = context.getSystemService(NotificationManager.class);
        this.createNotificationChannel();
        this.shouldRefreshNotifications = false;
        Log.e(TAG, "Starting Foreground Service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.startService(serviceIntent);
        this.registerBroadcastReceiver();
    }

    public void stop() {
        Log.e(TAG, "Stopping Foreground Service");
        //NOTE: we cancel all the current notifications, because the intents can't be used anymore
        //since the broadcast receiver will be killed as well and won't be able to handle any intent
        notificationManager.cancelAll();
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
        context.unregisterReceiver(notificationActionReceiver);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.notification_sound);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
            channel.setSound(soundUri, audioAttributes);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void removeChat(String chatId) {
        this.chats.remove(chatId);
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId, String chatId) {
        Intent intent = new Intent(ACTION_DELETE_NOTIFICATION);
        intent.putExtra("im.status.ethereum.chatId", chatId);
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createOnTapIntent(Context context, int notificationId, String chatId) {
        Intent intent = new Intent(ACTION_TAP_NOTIFICATION);
        intent.putExtra("im.status.ethereum.chatId", chatId);
        return PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
   } 

    public void notify(int notificationId, StatusChat chat) {
        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Me");
        ArrayList<StatusMessage> messages = chat.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            StatusMessage message = messages.get(i);
            messagingStyle.addMessage(message.getText(),
                                      message.getTimestamp(),
                                      message.getAuthor());
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify_status)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(messagingStyle)
            .setGroup(GROUP_STATUS_MESSAGES)
            .setGroupSummary(true)
            .setContentIntent(createOnTapIntent(context, notificationId, chat.getId()))
            .setDeleteIntent(createOnDismissedIntent(context, notificationId, chat.getId()))
            .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setVibrate(new long[0]);
        }
        notificationManager.notify(notificationId, builder.build());
    }

    public void refreshNotifications() {
        NotificationCompat.InboxStyle summaryStyle = new NotificationCompat.InboxStyle();
        int notificationId = 2; // we start at 2 because the service is using 1 and can't use 0
        Iterator<StatusChat> chatIterator = chats.values().iterator();
        while(chatIterator.hasNext()) {
            StatusChat chat = (StatusChat)chatIterator.next();
            notify(notificationId, chat);
            notificationId++;
        }
    }

    void handleNewMessageSignal(JSONObject newMessageSignal) {
        try {
            JSONArray chatsNewMessagesData = newMessageSignal.getJSONObject("event").getJSONArray("chats");
            for (int i = 0; i < chatsNewMessagesData.length(); i++) {
                try {
                    upsertChat(chatsNewMessagesData.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON conversion failed: " + e.getMessage());
                }
            }
            JSONArray messagesNewMessagesData = newMessageSignal.getJSONObject("event").getJSONArray("messages");
            for (int i = 0; i < messagesNewMessagesData.length(); i++) {
                try {
                    upsertMessage(messagesNewMessagesData.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON conversion failed: " + e.getMessage());
                }
            }

            if(shouldRefreshNotifications) {
                refreshNotifications();
                shouldRefreshNotifications = false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    private Person getPerson(String publicKey, String icon, String name) {
        // TODO: invalidate cache if icon and name are not the same as
        // the Person returned (in case the user set a different icon or username for instance)
        // not critical it's just for notifications at the moment
        // using a HashMap to cache Person because it's immutable
        Person person = persons.get(publicKey);
        if (person == null) {
            String base64Image = icon.split(",")[1];
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            person = new Person.Builder().setIcon(IconCompat.createWithBitmap(decodedByte)).setName(name).build();
            persons.put(publicKey, person);
        }
        return person;
    }

    private void upsertChat(JSONObject chatData) {
        try {
            // NOTE: this is an exemple of chatData
            // {"chatId":"contact-discovery-3622","filterId":"c0239d63f830e8b25f4bf7183c8d207f355a925b89514a17068cae4898e7f193",
            //  "symKeyId":"","oneToOne":true,"identity":"046599511623d7385b926ce709ac57d518dac10d451a81f75cd32c7fb4b1c...",
            // "topic":"0xc446561b","discovery":false,"negotiated":false,"listen":true}
            int oneToOne = chatData.getInt("chatType");
            // NOTE: for now we only notify one to one chats
            // TODO: also notifiy on mentions, keywords and group chats
            // TODO: one would have to pass the ens name and keywords to notify on when instanciating the class as well
            // as have a method to add new ones after the handler is instanciated
            if (oneToOne == 1) {
                //JSONArray messagesData = chatNewMessagesData.getJSONArray("messages");

                // there is no proper id for oneToOne chat in chatData so we peek into first message sig
                // TODO: won't work for sync becaus it could be our own message
                String id = chatData.getString("id");
                StatusChat chat = chats.get(id);


                // if the chat was not already there, we create one
                if (chat == null) {
                    chat = new StatusChat(id, true);
                }

                chats.put(id, chat);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }



    private void upsertMessage(JSONObject messageData) {
        try {
            String chatId = messageData.getString("localChatId");
            StatusChat chat = chats.get(chatId);
            if (chat == null) {
                return;
            }

            StatusMessage message = createMessage(messageData);
            if (message != null) {
                chat.appendMessage(message);
                chats.put(chatId, chat);
                shouldRefreshNotifications = true;
            }

        }
        catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    private StatusMessage createMessage(JSONObject messageData) {
        try {
            Person author = getPerson(messageData.getString("from"), messageData.getString("identicon"), messageData.getString("alias"));
            return new StatusMessage(author,  messageData.getLong("whisperTimestamp"), messageData.getString("text"));
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
        return null;
    }
}

class StatusChat {
    private ArrayList<StatusMessage> messages;
    private String id;
    private String name;
    private Boolean oneToOne;

    StatusChat(String id, Boolean oneToOne) {
        this.id = id;
        this.oneToOne = oneToOne;
        this.messages = new  ArrayList<StatusMessage>();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {

        //TODO this should be improved as it would rename the chat
        // after our own user if we were posting from another device
        // in 1-1 chats it should be the name of the user whose
        // key is different than ours
        StatusMessage message = getLastMessage();
        if (message == null) {
            return "no-name";
        }
        return message.getAuthor().getName().toString();
    }

    private StatusMessage getLastMessage() {
        if (messages.size() > 0) {
            return messages.get(messages.size()-1);
        }
        return null;
    }

    public long getTimestamp() {
        return getLastMessage().getTimestamp();
    }

    public ArrayList<StatusMessage> getMessages() {
        return messages;
    }

    public void appendMessage(StatusMessage message) {
        this.messages.add(message);
    }

    public String getSummary() {
        return "<b>" + getLastMessage().getAuthor().getName() + "</b>: " + getLastMessage().getText();
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

    private Person author;
    private long timestamp;
    private String text;

    StatusMessage(Person author, long timestamp, String text) {
        this.author = author;
        this.timestamp = timestamp;
        this.text = text;
    }
}
