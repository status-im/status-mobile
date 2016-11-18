package im.status.ethereum.module;


import android.os.Message;

public interface ConnectorHandler {
    boolean handleMessage(Message message);
    void onConnectorConnected();
    void onConnectorDisconnected();
}
