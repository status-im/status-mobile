package im.status.ethereum.module;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import org.json.JSONException;
import statusgo.Statusgo;
import org.json.JSONObject;

public class NetworkManager extends ReactContextBaseJavaModule {
    private ReactApplicationContext reactContext;
    private Utils utils;

    public NetworkManager(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.utils = new Utils(reactContext);
    }

    @Override
    public String getName() {
        return "NetworkManager";
    }

    @ReactMethod
    public void startSearchForLocalPairingPeers(final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.startSearchForLocalPairingPeers(), callback);
    }

    @ReactMethod
    public void getConnectionStringForBootstrappingAnotherDevice(final String configJSON, final Callback callback) throws JSONException {
        final JSONObject jsonConfig = new JSONObject(configJSON);
        final JSONObject senderConfig = jsonConfig.getJSONObject("senderConfig");
        final String keyUID = senderConfig.getString("keyUID");
        final String keyStorePath = this.utils.getKeyStorePath(keyUID);
        senderConfig.put("keystorePath", keyStorePath);

        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.getConnectionStringForBootstrappingAnotherDevice(jsonConfig.toString()), callback);
    }

    @ReactMethod
    public void inputConnectionStringForBootstrapping(final String connectionString, final String configJSON, final Callback callback) throws JSONException {
        final JSONObject jsonConfig = new JSONObject(configJSON);
        final JSONObject receiverConfig = jsonConfig.getJSONObject("receiverConfig");
        final String keyStorePath = this.utils.pathCombine(this.utils.getNoBackupDirectory(), "/keystore");
        receiverConfig.put("keystorePath", keyStorePath);
        receiverConfig.getJSONObject("nodeConfig").put("rootDataDir", this.utils.getNoBackupDirectory());

        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.inputConnectionStringForBootstrapping(connectionString, jsonConfig.toString()), callback);
    }

    @ReactMethod
    public void sendTransactionWithSignature(final String txArgsJSON, final String signature, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.sendTransactionWithSignature(txArgsJSON, signature), callback);
    }

    @ReactMethod
    public void sendTransaction(final String txArgsJSON, final String password, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.sendTransaction(txArgsJSON, password), callback);
    }

    @ReactMethod
    public void callRPC(final String payload, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.callRPC(payload), callback);
    }

    @ReactMethod
    public void callPrivateRPC(final String payload, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.callPrivateRPC(payload), callback);
    }

    @ReactMethod
    public void recover(final String rpcParams, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.recover(rpcParams), callback);
    }

}
