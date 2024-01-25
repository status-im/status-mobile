package im.status.ethereum.module;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import java.io.File;
import java.util.Stack;
import android.util.Log;
import android.net.Uri;
import java.io.OutputStreamWriter;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.core.content.FileProvider;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import org.json.JSONObject;
import statusgo.Statusgo;
import android.content.Context;
import org.json.JSONException;
public class LogManager extends ReactContextBaseJavaModule {
    private static final String TAG = "LogManager";
    private static final String gethLogFileName = "geth.log";
    private static final String statusLogFileName = "Status.log";
    private static final String logsZipFileName = "Status-debug-logs.zip";
    private ReactApplicationContext reactContext;
    private Utils utils;

    public LogManager(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.utils = new Utils(reactContext);
    }

    @Override
    public String getName() {
        return "LogManager";
    }

    private File getLogsFile() {
        final File pubDirectory = this.utils.getPublicStorageDirectory();
        final File logFile = new File(pubDirectory, gethLogFileName);

        return logFile;
    }

    public File prepareLogsFile(final Context context) {
        final File logFile = this.utils.getLogsFile();

        try {
            logFile.setReadable(true);
            File parent = logFile.getParentFile();
            if (!parent.canWrite()) {
                return null;
            }
            if (!parent.exists()) {
                parent.mkdirs();
            }
            logFile.createNewFile();
            logFile.setWritable(true);
            Log.d(TAG, "Can write " + logFile.canWrite());
            Uri gethLogUri = Uri.fromFile(logFile);

            String gethLogFilePath = logFile.getAbsolutePath();
            Log.d(TAG, gethLogFilePath);

            return logFile;
        } catch (Exception e) {
            Log.d(TAG, "Can't create geth.log file! " + e.getMessage());
        }

        return null;
    }

    private void showErrorMessage(final String message) {
        final Activity activity = getCurrentActivity();

        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void dumpAdbLogsTo(final FileOutputStream statusLogStream) throws IOException {
        final String filter = "logcat -d -b main ReactNativeJS:D StatusModule:D StatusService:D StatusNativeLogs:D *:S";
        final java.lang.Process p = Runtime.getRuntime().exec(filter);
        final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
        final java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(statusLogStream));
        String line;
        while ((line = in.readLine()) != null) {
            out.write(line);
            out.newLine();
        }
        out.close();
        in.close();
    }

    private Boolean zip(File[] _files, File zipFile, Stack<String> errorList) {
        final int BUFFER = 0x8000;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                final File file = _files[i];
                if (file == null || !file.exists()) {
                    continue;
                }

                Log.v("Compress", "Adding: " + file.getAbsolutePath());
                try {
                    FileInputStream fi = new FileInputStream(file);
                    origin = new BufferedInputStream(fi, BUFFER);

                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    int count;

                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    errorList.push(e.getMessage());
                }
            }

            out.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @ReactMethod
    public void sendLogs(final String dbJson, final String jsLogs, final Callback callback) {
        Log.d(TAG, "sendLogs");
        if (!this.utils.checkAvailability()) {
            return;
        }

        final Context context = this.getReactApplicationContext();
        final File logsTempDir = new File(context.getCacheDir(), "logs"); // This path needs to be in sync with android/app/src/main/res/xml/file_provider_paths.xml
        logsTempDir.mkdir();

        final File dbFile = new File(logsTempDir, "db.json");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(dbFile));
            outputStreamWriter.write(dbJson);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            showErrorMessage(e.getLocalizedMessage());
        }

        final File zipFile = new File(logsTempDir, logsZipFileName);
        final File statusLogFile = new File(logsTempDir, statusLogFileName);
        final File gethLogFile = getLogsFile();

        try {
            if (zipFile.exists() || zipFile.createNewFile()) {
                final long usableSpace = zipFile.getUsableSpace();
                if (usableSpace < 20 * 1024 * 1024) {
                    final String message = String.format("Insufficient space available on device (%s) to write logs.\nPlease free up some space.", android.text.format.Formatter.formatShortFileSize(context, usableSpace));
                    Log.e(TAG, message);
                    showErrorMessage(message);
                    return;
                }
            }

            dumpAdbLogsTo(new FileOutputStream(statusLogFile));

            final Stack<String> errorList = new Stack<String>();
            final Boolean zipped = zip(new File[]{dbFile, gethLogFile, statusLogFile}, zipFile, errorList);
            if (zipped && zipFile.exists()) {
                zipFile.setReadable(true, false);
                Uri extUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", zipFile);
                callback.invoke(extUri.toString());
            } else {
                Log.d(TAG, "File " + zipFile.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            showErrorMessage(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        } finally {
            dbFile.delete();
            statusLogFile.delete();
            zipFile.deleteOnExit();
        }
    }

    @ReactMethod
    public void initLogging(final boolean enabled, final boolean mobileSystem, final String logLevel, final Callback callback) throws JSONException {
        final JSONObject jsonConfig = new JSONObject();
        jsonConfig.put("Enabled", enabled);
        jsonConfig.put("MobileSystem", mobileSystem);
        jsonConfig.put("Level", logLevel);
        jsonConfig.put("File", getLogsFile().getAbsolutePath());
        final String config = jsonConfig.toString();
        this.utils.executeRunnableStatusGoMethod(() -> Statusgo.initLogging(config), callback);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String logFileDirectory() {
        return this.utils.getPublicStorageDirectory().getAbsolutePath();
    }

}
