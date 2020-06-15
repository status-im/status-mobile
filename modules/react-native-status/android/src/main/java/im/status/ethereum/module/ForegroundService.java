package im.status.ethereum.module;

import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

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
    public int onStartCommand(Intent i, int flags, int startId) {
        // NOTE: recent versions of Android require the service to display
        // a sticky notification to inform the user that the service is running
        Context context = getApplicationContext();
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                                                                  context.getResources().getString(R.string.status_service),
                                                                  NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
        Class intentClass;
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            intentClass =  Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }

        Intent intent = new Intent(context, intentClass);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Intent stopIntent = new Intent(NewMessageSignalHandler.ACTION_TAP_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String content = context.getResources().getString(R.string.keep_status_running);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify_status)
            .setContentTitle(context.getResources().getString(R.string.background_service_opened))
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setNumber(0)
            .addAction(R.drawable.ic_stat_notify_status,
                       context.getResources().getString(R.string.stop),
                       stopPendingIntent)
            .build();
        // the id of the foreground notification MUST NOT be 0
        startForeground(1, notification);
        return START_STICKY;
    }
}
