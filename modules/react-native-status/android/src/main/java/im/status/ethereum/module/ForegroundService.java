package im.status.ethereum.module;

import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Build;

public class ForegroundService extends Service {
    private static final String CHANNEL_ID = "status-service";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // NOTE: recent versions of Android require the service to display
        // a sticky notification to inform the user that the service is running
        Context context = getApplicationContext();
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                                                                                  "Status Service",
                                                                                  NotificationManager.IMPORTANCE_HIGH));
        }
        String content = "Keep Status running to receive notifications";
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify_status)
            .setContentTitle("Background notification service opened")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build();
        // the id of the foreground notification MUST NOT be 0
        startForeground(1, notification);
        return START_STICKY;
    }
}
