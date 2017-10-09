package im.status.ethereum.module;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.concurrent.CountDownLatch;

import java.lang.ref.WeakReference;

public class StatusService extends Service {

    private static final String TAG = "StatusService";

    public StatusService() {
        super();
    }

    private static class IncomingHandler extends Handler {

        private final WeakReference<StatusService> service;

        IncomingHandler(StatusService service) {

            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message message) {

            StatusService service = this.service.get();
            if (service != null) {
                if (!service.handleMessage(message)) {
                    super.handleMessage(message);
                }
            }
        }
    }

    private static CountDownLatch applicationMessengerIsSet = new CountDownLatch(1);

    private final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));

    private static Messenger applicationMessenger = null;

    private boolean handleMessage(Message message) {
        Log.d(TAG, "Received service message." + message.toString());
        applicationMessenger = message.replyTo;
	applicationMessengerIsSet.countDown();

        return true;
    }

    public static void signalEvent(String jsonEvent) {

        Log.d(TAG, "Signal event: " + jsonEvent);
        Bundle replyData = new Bundle();
        replyData.putString("event", jsonEvent);

        Message replyMessage = Message.obtain(null, 0, 0, 0, null);
        replyMessage.setData(replyData);
	try {
	    applicationMessengerIsSet.await();
	    sendReply(applicationMessenger, replyMessage);
	} catch(InterruptedException e) {
	    Log.d(TAG, "Interrupted during event signalling.");
	}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Status Service created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Status Service stopped!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private static void sendReply(Messenger messenger, Message message) {
        try {
            boolean ex = false;
            if (messenger != null) {
                ex = true;
            }
            Log.d(TAG, "before sendReply " + ex);
            messenger.send(message);
        } catch (Exception e) {
            Log.e(TAG, "Exception sending message id: " + message.what, e);
        }
    }
}
