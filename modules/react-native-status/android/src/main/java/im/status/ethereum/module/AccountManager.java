package im.status.ethereum.module;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import statusgo.Statusgo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;
import android.content.Context;
import android.app.Activity;

public class AccountManager extends ReactContextBaseJavaModule {
    private static final String TAG = "AccountManager";
    private static final String gethLogFileName = "geth.log";
    private ReactApplicationContext reactContext;

    private Utils utils;
    private LogManager logManager;

    public AccountManager(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.utils = new Utils(reactContext);
        this.logManager = new LogManager(reactContext);
    }

    @Override
    public String getName() {
        return "AccountManager";
    }

    private String getTestnetDataDir(final String absRootDirPath) {
        return this.utils.pathCombine(absRootDirPath, "ethereum/testnet");
    }

    @ReactMethod
    public void createAccountAndLogin(final String createAccountRequest) {
        Log.d(TAG, "createAccountAndLogin");
        String result = Statusgo.createAccountAndLogin(createAccountRequest);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "createAccountAndLogin success: " + result);
            Log.d(TAG, "Geth node started");
        } else {
            Log.e(TAG, "createAccountAndLogin failed: " + result);
        }
    }

    @ReactMethod
    public void restoreAccountAndLogin(final String restoreAccountRequest) {
        Log.d(TAG, "restoreAccountAndLogin");
        String result = Statusgo.restoreAccountAndLogin(restoreAccountRequest);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "restoreAccountAndLogin success: " + result);
            Log.d(TAG, "Geth node started");
        } else {
            Log.e(TAG, "restoreAccountAndLogin failed: " + result);
        }
    }

    private String updateConfig(final String jsonConfigString, final String absRootDirPath, final String keystoreDirPath) throws JSONException {
        final JSONObject jsonConfig = new JSONObject(jsonConfigString);
        // retrieve parameters from app config, that will be applied onto the Go-side config later on
        final String dataDirPath = jsonConfig.getString("DataDir");
        final Boolean logEnabled = jsonConfig.getBoolean("LogEnabled");
        final Context context = this.getReactApplicationContext();
        final File gethLogFile = logEnabled ? this.logManager.prepareLogsFile(context) : null;
        String gethLogDirPath = null;
        if (gethLogFile != null) {
            gethLogDirPath = gethLogFile.getParent();
        }

        Log.d(TAG, "log dir: " + gethLogDirPath + " log name: " + gethLogFileName);

        jsonConfig.put("DataDir", dataDirPath);
        jsonConfig.put("KeyStoreDir", keystoreDirPath);
        jsonConfig.put("LogDir", gethLogDirPath);
        jsonConfig.put("LogFile", gethLogFileName);

        return jsonConfig.toString();
    }

    private static void prettyPrintConfig(final String config) {
        Log.d(TAG, "startNode() with config (see below)");
        String configOutput = config;
        final int maxOutputLen = 4000;
        Log.d(TAG, "********************** NODE CONFIG ****************************");
        while (!configOutput.isEmpty()) {
            Log.d(TAG, "Node config:" + configOutput.substring(0, Math.min(maxOutputLen, configOutput.length())));
            if (configOutput.length() > maxOutputLen) {
                configOutput = configOutput.substring(maxOutputLen);
            } else {
                break;
            }
        }
        Log.d(TAG, "******************* ENDOF NODE CONFIG *************************");
    }

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    private String prepareDirAndUpdateConfig(final String jsonConfigString, final String keyUID) {

        final String absRootDirPath = this.utils.getNoBackupDirectory();
        final String dataFolder = this.getTestnetDataDir(absRootDirPath);
        Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);
            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        final String ropstenFlagPath = this.utils.pathCombine(absRootDirPath, "ropsten_flag");
        final File ropstenFlag = new File(ropstenFlagPath);
        if (!ropstenFlag.exists()) {
            try {
                final String chaindDataFolderPath = this.utils.pathCombine(dataFolder, "StatusIM/lightchaindata");
                final File lightChainFolder = new File(chaindDataFolderPath);
                if (lightChainFolder.isDirectory()) {
                    String[] children = lightChainFolder.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(lightChainFolder, children[i]).delete();
                    }
                }
                lightChainFolder.delete();
                ropstenFlag.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String testnetDataDir = dataFolder;
        String oldKeystoreDir = this.utils.pathCombine(testnetDataDir, "keystore");
        String newKeystoreDir = this.utils.pathCombine(absRootDirPath, "keystore");
        final File oldKeystore = new File(oldKeystoreDir);
        if (oldKeystore.exists()) {
            try {
                final File newKeystore = new File(newKeystoreDir);
                copyDirectory(oldKeystore, newKeystore);

                if (oldKeystore.isDirectory()) {
                    String[] children = oldKeystore.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(oldKeystoreDir, children[i]).delete();
                    }
                }
                oldKeystore.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            final String multiaccountKeystoreDir = this.utils.pathCombine("/keystore", keyUID);
            final String updatedJsonConfigString = this.updateConfig(jsonConfigString, absRootDirPath, multiaccountKeystoreDir);

            prettyPrintConfig(updatedJsonConfigString);

            return updatedJsonConfigString;
        } catch (JSONException e) {
            Log.e(TAG, "updateConfig failed: " + e.getMessage());
            System.exit(1);

            return "";
        }
    }

    @ReactMethod
    public void prepareDirAndUpdateConfig(final String keyUID, final String config, final Callback callback) {
        Log.d(TAG, "prepareDirAndUpdateConfig");
        String finalConfig = prepareDirAndUpdateConfig(config, keyUID);
        callback.invoke(finalConfig);
    }

    @ReactMethod
    public void saveAccountAndLoginWithKeycard(final String multiaccountData, final String password, final String settings, final String config, final String accountsData, final String chatKey) {
        Log.d(TAG, "saveAccountAndLoginWithKeycard");
        String finalConfig = prepareDirAndUpdateConfig(config, this.utils.getKeyUID(multiaccountData));
        String result = Statusgo.saveAccountAndLoginWithKeycard(multiaccountData, password, settings, finalConfig, accountsData, chatKey);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "saveAccountAndLoginWithKeycard result: " + result);
            Log.d(TAG, "Geth node started");
        } else {
            Log.e(TAG, "saveAccountAndLoginWithKeycard failed: " + result);
        }
    }

    @ReactMethod
    public void loginWithKeycard(final String accountData, final String password, final String chatKey, final String nodeConfigJSON) {
        Log.d(TAG, "loginWithKeycard");
        this.utils.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.loginWithKeycard(accountData, password, chatKey, nodeConfigJSON);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithKeycard result: " + result);
        } else {
            Log.e(TAG, "LoginWithKeycard failed: " + result);
        }
    }

    @ReactMethod
    public void loginWithConfig(final String accountData, final String password, final String configJSON) {
        Log.d(TAG, "loginWithConfig");
        this.utils.migrateKeyStoreDir(accountData, password);
        String result = Statusgo.loginWithConfig(accountData, password, configJSON);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "LoginWithConfig result: " + result);
        } else {
            Log.e(TAG, "LoginWithConfig failed: " + result);
        }
    }

    @ReactMethod
    public void loginAccount(final String request) {
        Log.d(TAG, "loginAccount");
        String result = Statusgo.loginAccount(request);
        if (result.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "loginAccount result: " + result);
        } else {
            Log.e(TAG, "loginAccount failed: " + result);
        }
    }

    @ReactMethod
    public void verify(final String address, final String password, final Callback callback) throws JSONException {
        Activity currentActivity = getCurrentActivity();

        final String absRootDirPath = this.utils.getNoBackupDirectory();
        final String newKeystoreDir = this.utils.pathCombine(absRootDirPath, "keystore");

        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.verifyAccountPassword(newKeystoreDir, address, password), callback);
    }

    @ReactMethod
    public void verifyDatabasePassword(final String keyUID, final String password, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.verifyDatabasePassword(keyUID, password), callback);
    }

    @ReactMethod
    private void openAccounts(final Callback callback) throws JSONException {
        Log.d(TAG, "openAccounts");
        final String rootDir = this.utils.getNoBackupDirectory();
        Log.d(TAG, "[Opening accounts" + rootDir);
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.openAccounts(rootDir), callback);
    }

    //TODO : refactor logout usage to accept callback so that we can do this.utils.executeRunnableStatusGoMethod
    @ReactMethod
    public void logout() {
        Log.d(TAG, "logout");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String result = Statusgo.logout();
                if (result.startsWith("{\"error\":\"\"")) {
                    Log.d(TAG, "Logout result: " + result);
                } else {
                    Log.e(TAG, "Logout failed: " + result);
                }
            }
        };

        StatusThreadPoolExecutor.getInstance().execute(r);
    }

    @ReactMethod
    public void multiAccountStoreAccount(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountStoreAccount(json), callback);
    }

    @ReactMethod
    public void multiAccountLoadAccount(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountLoadAccount(json), callback);
    }

    @ReactMethod
    public void multiAccountDeriveAddresses(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountDeriveAddresses(json), callback);
    }

    @ReactMethod
    public void multiAccountGenerateAndDeriveAddresses(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountGenerateAndDeriveAddresses(json), callback);
    }

    @ReactMethod
    public void multiAccountStoreDerived(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountStoreDerivedAccounts(json), callback);
    }

    @ReactMethod
    public void multiAccountImportMnemonic(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountImportMnemonic(json), callback);
    }

    @ReactMethod
    public void multiAccountImportPrivateKey(final String json, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.multiAccountImportPrivateKey(json), callback);
    }

    @ReactMethod
    public void deleteMultiaccount(final String keyUID, final Callback callback) throws JSONException {
        final String keyStoreDir = this.utils.getKeyStorePath(keyUID);
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.deleteMultiaccount(keyUID, keyStoreDir), callback);
    }

    @ReactMethod
    public void getRandomMnemonic(final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.getRandomMnemonic(), callback);
    }

    @ReactMethod
    public void createAccountFromMnemonicAndDeriveAccountsForPaths(final String mnemonic, final Callback callback) throws JSONException {
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.createAccountFromMnemonicAndDeriveAccountsForPaths(mnemonic), callback);
    }
}
