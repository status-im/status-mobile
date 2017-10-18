package im.status.ethereum.module;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;

import java.util.ArrayList;

public class ServiceConnector {

    private static final String TAG = "ServiceConnector";
    /** Context of the activity from which this connector was launched */
    private Context context;

    /** The class of the service we want to connect to */
    private Class serviceClass;

    /** Flag indicating if the service is bound. */
    boolean isBound;

    /** Sends messages to the service. */
    Messenger serviceMessenger = null;

    /** Receives messages from the service. */
    Messenger clientMessenger = null;

    private ArrayList<ConnectorHandler> handlers = new ArrayList<>();

    /** Handles incoming messages from service. */
    private class IncomingHandler extends Handler {

        IncomingHandler(HandlerThread thread) {

            super(thread.getLooper());
        }

        @Override
        public void handleMessage(Message message) {

            boolean isClaimed = false;
            //if (message.obj != null) {
            //  String identifier = ((Bundle) message.obj).getString("identifier");
            //if (identifier != null) {

            for (ConnectorHandler handler : handlers) {
                //        if (identifier.equals(handler.getID())) {
                isClaimed = handler.handleMessage(message);
                //      }
            }
            //  }
            //}
            if (!isClaimed) {
                super.handleMessage(message);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.

            serviceMessenger = new Messenger(service);
            isBound = true;
            for (ConnectorHandler handler: handlers) {
                handler.onConnectorConnected();
            }
        }

        public void onServiceDisconnected(ComponentName className) {

            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            serviceMessenger = null;
            isBound = false;
            for (ConnectorHandler handler: handlers) {
                handler.onConnectorDisconnected();
            }
        }
    };

    ServiceConnector(Context context, Class serviceClass) {
        this.context = context;
        this.serviceClass = serviceClass;
        // Handler thread to avoid running on the main UI thread
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        // Incoming message handler. Calls to its binder are sequential!
        IncomingHandler handler = new IncomingHandler(handlerThread);
        clientMessenger = new Messenger(handler);
    }

    /** Bind to the service */
    public boolean bindService() {

        if (serviceConnection != null) {
            Intent intent = new Intent(context, serviceClass);
            context.getApplicationContext().startService(intent);
            return context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            return false;
        }
    }

    /** Unbind from the service */
    public void unbindService() {

        if (isBound && serviceConnection != null) {
            context.getApplicationContext().unbindService(serviceConnection);
            isBound = false;
/*
            Intent intent = new Intent(context, serviceClass);
            context.getApplicationContext().stopService(intent);
*/
        }
    }

    public void registerHandler(ConnectorHandler handler) {

        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public void removeHandler(ConnectorHandler handler) {

        handlers.remove(handler);
    }

    public void sendMessage() {

        Message msg = Message.obtain(null, 0, 0, 0);
        msg.replyTo = clientMessenger;
        try {
            Log.d(TAG, "Sending message to service: ");
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception sending message(" + msg.toString() + ") to service: ", e);
        }
    }
}
