package im.status.ethereum.module;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;

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

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.net.Uri;
import android.media.AudioAttributes;

import android.util.Log;

class NewMessageSignalHandler {
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
    }

    public void stop() {
        Log.e(TAG, "Stopping Foreground Service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
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
            .setGroup(GROUP_STATUS_MESSAGES);
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setVibrate(new long[0]);
        }
        notificationManager.notify(notificationId, builder.build());
    }

    public void refreshNotifications() {
        NotificationCompat.InboxStyle summaryStyle = new NotificationCompat.InboxStyle();
        String summary = "";
        int notificationId = 2; // we start at 2 because the service is using 1 and can't use 0
        int messageCounter = 0;
        Iterator<StatusChat> chatIterator = chats.values().iterator();
        while(chatIterator.hasNext()) {
            StatusChat chat = (StatusChat)chatIterator.next();
            notify(notificationId, chat);
            notificationId++;
            messageCounter += chat.getMessages().size();
            summaryStyle.addLine(chat.getSummary());
            summary += chat.getSummary() + "\n";
        }
        // NOTE: this is necessary for old versions of Android, newer versions are
        // building this group themselves and I was not able to make any change to
        // what this group displays
        NotificationCompat.Builder groupBuilder =
            new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentText(summary)
            .setSmallIcon(R.drawable.ic_stat_notify_status)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentTitle("You got " + messageCounter + " messages in " + chats.size() + " chats")
            .setStyle(summaryStyle
                      .setBigContentTitle("You got " + messageCounter + " messages in " + chats.size() + " chats"))
            .setGroup(GROUP_STATUS_MESSAGES)
            .setGroupSummary(true);
        notificationManager.notify(notificationId, groupBuilder.build());
    }

    void handleNewMessageSignal(JSONObject newMessageSignal) {
        try {
            JSONArray chatsNewMessagesData = newMessageSignal.getJSONObject("event").getJSONArray("messages");
            for (int i = 0; i < chatsNewMessagesData.length(); i++) {
                try {
                    upsertChat(chatsNewMessagesData.getJSONObject(i));
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

    private void upsertChat(JSONObject chatNewMessagesData) {
        try {
            JSONObject chatData = chatNewMessagesData.getJSONObject("chat");
            // NOTE: this is an exemple of chatData
            // {"chatId":"contact-discovery-3622","filterId":"c0239d63f830e8b25f4bf7183c8d207f355a925b89514a17068cae4898e7f193",
            //  "symKeyId":"","oneToOne":true,"identity":"046599511623d7385b926ce709ac57d518dac10d451a81f75cd32c7fb4b1c...",
            // "topic":"0xc446561b","discovery":false,"negotiated":false,"listen":true}
            boolean oneToOne = chatData.getBoolean("oneToOne");
            // NOTE: for now we only notify one to one chats
            // TODO: also notifiy on mentions, keywords and group chats
            // TODO: one would have to pass the ens name and keywords to notify on when instanciating the class as well
            // as have a method to add new ones after the handler is instanciated
            if (oneToOne) {
                JSONArray messagesData = chatNewMessagesData.getJSONArray("messages");

                // there is no proper id for oneToOne chat in chatData so we peek into first message sig
                // TODO: won't work for sync becaus it could be our own message
                String id = messagesData.getJSONObject(0).getJSONObject("message").getString("sig");
                StatusChat chat = chats.get(id);


                // if the chat was not already there, we create one
                if (chat == null) {
                    chat = new StatusChat(id, oneToOne);
                }

                ArrayList<StatusMessage> messages = chat.getMessages();
                // parse the new messages
                for (int j = 0; j < messagesData.length(); j++) {
                    StatusMessage message = createMessage(messagesData.getJSONObject(j));
                    if (message != null) {
                        messages.add(message);
                    }
                }

                if (!messages.isEmpty()) {
                    chat.setMessages(messages);
                    chats.put(id, chat);
                    shouldRefreshNotifications = true;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON conversion failed: " + e.getMessage());
        }
    }

    private StatusMessage createMessage(JSONObject messageData) {
        try {
            JSONObject metadata = messageData.getJSONObject("metadata");
            JSONObject authorMetadata = metadata.getJSONObject("author");
            JSONArray payload = new JSONArray(messageData.getString("payload"));
            // NOTE: this is an exemple of payload we are currently working with
            // it is in the transit format, which is basically JSON
            // refer to `transport.message.transit.cljs` on react side for details
            // ["~#c4",["7","text/plain","~:public-group-user-message",157201130275201,1572011302752,["^ ","~:chat-id","test","~:text","7"]]]
            if (payload.getString(0).equals("~#c4")) {
                Person author = getPerson(authorMetadata.getString("publicKey"), authorMetadata.getString("identicon"), authorMetadata.getString("alias"));
                JSONArray payloadContent = payload.getJSONArray(1);
                String text = payloadContent.getString(0);
                Double timestamp = payloadContent.getDouble(4);
                return new StatusMessage(author, timestamp.longValue(), text);
            }
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
    }

    public String getId() {
        return id;
    }

    public String getName() {
        //TODO this should be improved as it would rename the chat
        // after our own user if we were posting from another device
        // in 1-1 chats it should be the name of the user whose
        // key is different than ours
        return getLastMessage().getAuthor().getName().toString();
    }

    private StatusMessage getLastMessage() {
        return messages.get(messages.size()-1);
    }

    public long getTimestamp() {
        return getLastMessage().getTimestamp();
    }

    public ArrayList<StatusMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<StatusMessage> messages) {
        this.messages = messages;
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
