package im.status.ethereum.module;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.concurrent.CountDownLatch;

import java.lang.ref.WeakReference;


/**
 * StatusService has nothing to do with Android services anymore.
 * The name "StatusService" is kept to keep backward compatibility with status-go.
 * Hopefully, it will be replaced when GoMobile
 */
public class StatusService {
    static final StatusService INSTANCE = new StatusService();

    private static final String TAG = "StatusService";

    /**
     * signalEvent is called by Statusgo JNI module to pass events from the node.
     * @param jsonEvent
     */
    public static void signalEvent(String jsonEvent) {
        Log.d(TAG, "[signalEvent] event: " + jsonEvent);
        StatusNodeEventHandler listener = StatusService.INSTANCE.getSignalEventListener();

        if (listener == null) {
            Log.w(TAG, "[signalEvent] no listener is set (module is missing?) ignoring event: " + jsonEvent);
            return;
        }

        Log.d(TAG, "[signalEvent] passing event to the listener: " + jsonEvent);
        listener.handleEvent(jsonEvent);
    }

    private StatusNodeEventHandler signalEventListener;

    void setSignalEventListener(StatusNodeEventHandler listener) {
        Log.d(TAG, "[setSignalEventListener], setting listener to: " + this.safeClassName(listener));
        this.signalEventListener = listener;
    }

    private String safeClassName(Object object) {
        if (object == null) {
            return "null";
        }

        if (object.getClass() == null) {
            return "<unknown object>";
        }

        return object.getClass().getCanonicalName();
    }

    private StatusNodeEventHandler getSignalEventListener() {
        return this.signalEventListener;
    }

}
