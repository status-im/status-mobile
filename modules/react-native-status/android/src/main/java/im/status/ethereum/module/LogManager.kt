package im.status.ethereum.module

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import org.json.JSONException
import org.json.JSONObject
import statusgo.Statusgo
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LogManager(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        private const val TAG = "LogManager"
        private const val CREATE_BACKUP_FILE_REQUEST = 8001
        private const val statusLogFileName = "Status.log"
        private const val logsZipFileName = "Status-debug-logs.zip"
    }

    private val utils = Utils(reactContext)
    private var pendingBackupFilePath: String? = null
    private var pendingBackupCallback: Callback? = null

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName() = "LogManager"

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_BACKUP_FILE_REQUEST) {
            handleBackupFileSelection(resultCode, data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        // Not needed for our use case
    }

    private fun showErrorMessage(message: String) {
        val activity = currentActivity

        AlertDialog.Builder(activity)
            .setTitle("Error")
            .setMessage(message)
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun dumpAdbLogsTo(statusLogStream: FileOutputStream) {
        val filter = "logcat -d -b main ReactNativeJS:D StatusModule:D StatusService:D StatusNativeLogs:D *:S"
        val p = Runtime.getRuntime().exec(filter)
        val input = BufferedReader(InputStreamReader(p.inputStream))
        val output = BufferedWriter(OutputStreamWriter(statusLogStream))
        var line: String?
        while (input.readLine().also { line = it } != null) {
            output.write(line)
            output.newLine()
        }
        output.close()
        input.close()
    }

    private fun zip(files: Array<File>, zipFile: File, errorList: Stack<String>): Boolean {
        val BUFFER = 0x8000

        try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(zipFile)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(BUFFER)

            for (file in files) {
                if (file == null || !file.exists()) {
                    continue
                }

                Log.v("Compress", "Adding: ${file.absolutePath}")
                try {
                    val fi = FileInputStream(file)
                    origin = BufferedInputStream(fi, BUFFER)

                    val entry = ZipEntry(file.name)
                    out.putNextEntry(entry)
                    var count: Int

                    while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                        out.write(data, 0, count)
                    }
                    origin.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.message!!)
                    errorList.push(e.message!!)
                }
            }

            out.close()

            return true
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            e.printStackTrace()
            return false
        }
    }

    @ReactMethod
    fun sendLogs(dbJson: String, jsLogs: String, usePublicLogDir: Boolean, callback: Callback) {
        Log.d(TAG, "sendLogs")
        if (!utils.checkAvailability()) {
            return
        }

        val context = reactApplicationContext
        val logsTempDir = File(context.cacheDir, Utils.LOGS_DIRECTORY_NAME) // This path needs to be in sync with android/app/src/main/res/xml/file_provider_paths.xml
        logsTempDir.mkdir()

        val dbFile = File(logsTempDir, "db.json")
        try {
            val outputStreamWriter = OutputStreamWriter(FileOutputStream(dbFile))
            outputStreamWriter.write(dbJson)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "File write failed: ${e}")
            showErrorMessage(e.localizedMessage!!)
        }

        val zipFile = File(logsTempDir, logsZipFileName)
        val statusLogFile = File(logsTempDir, statusLogFileName)

        try {
            if (zipFile.exists() || zipFile.createNewFile()) {
                val usableSpace = zipFile.usableSpace
                if (usableSpace < 20 * 1024 * 1024) {
                    val message = "Insufficient space available on device (${android.text.format.Formatter.formatShortFileSize(context, usableSpace)}) to write logs.\nPlease free up some space."
                    Log.e(TAG, message)
                    showErrorMessage(message)
                    return
                }
            }

            dumpAdbLogsTo(FileOutputStream(statusLogFile))

            val errorList = Stack<String>()
            val filesToZip = mutableListOf<File>(dbFile, statusLogFile)
            
            // Get all files from the log directory
            val logDirectory = utils.getLogDirectory(usePublicLogDir)
            if (logDirectory != null && logDirectory.exists()) {
                val logFiles = logDirectory.listFiles()
                if (logFiles != null) {
                    for (file in logFiles) {
                        if (file.isFile) {
                            Log.d(TAG, "Adding log file: ${file.name}")
                            filesToZip.add(file)
                        }
                    }
                }
            }
            
            val zipped = zip(filesToZip.toTypedArray(), zipFile, errorList)
            if (zipped && zipFile.exists()) {
                zipFile.setReadable(true, false)
                val extUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", zipFile)
                callback.invoke(extUri.toString())
            } else {
                Log.d(TAG, "File ${zipFile.absolutePath} does not exist")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            showErrorMessage(e.localizedMessage!!)
            e.printStackTrace()
            return
        } finally {
            dbFile.delete()
            statusLogFile.delete()
            zipFile.deleteOnExit()
        }
    }

    // workaround for android since react-native-share is not working for zip files, for iOS we use react-native-share
    @ReactMethod
    fun shareLogs(fileUri: String, callback: Callback) {
        Log.d(TAG, "shareLogs: $fileUri")
        
        try {
            val uri = Uri.parse(fileUri)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/zip"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            val chooser = Intent.createChooser(intent, "Share Logs")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            reactContext.startActivity(chooser)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file: ${e.message}")
            callback.invoke(e.message)
        }
    }

    @ReactMethod
    fun shareBackupFile(filePath: String, callback: Callback) {
        Log.d(TAG, "shareBackupFile: $filePath")

        try {
            val context = reactApplicationContext
            val sourceFile = File(filePath)

            if (!sourceFile.exists()) {
                val errorMsg = "Backup file does not exist: $filePath"
                Log.e(TAG, errorMsg)
                callback.invoke(errorMsg)
                return
            }

            // Share directly from files/backups directory using FileProvider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", sourceFile)
            Log.d(TAG, "FileProvider URI: $uri")

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Share Backup File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            reactContext.startActivity(chooser)

        } catch (e: Exception) {
            Log.e(TAG, "Error sharing backup file: ${e.message}")
            e.printStackTrace()
            callback.invoke(e.message)
        }
    }

    @ReactMethod
    fun saveBackupFileLocally(filePath: String, callback: Callback) {
        Log.d(TAG, "saveBackupFileLocally: $filePath")

        try {
            val context = reactApplicationContext
            val sourceFile = File(filePath)

            if (!sourceFile.exists()) {
                val errorMsg = "Backup file does not exist: $filePath"
                Log.e(TAG, errorMsg)
                callback.invoke(errorMsg)
                return
            }

            val fileName = sourceFile.name
            Log.d(TAG, "Saving backup file: $fileName")

            // Use MediaStore API for Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (uri == null) {
                    callback.invoke("Failed to create file in Downloads")
                    return
                }

                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Log.d(TAG, "Backup saved successfully to Downloads: $fileName")
                callback.invoke(null)
            } else {
                // Fallback for Android 9 and below
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)

                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(TAG, "Backup saved successfully to Downloads: $fileName")
                callback.invoke(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error saving backup file: ${e.message}")
            e.printStackTrace()
            callback.invoke(e.message)
        }
    }

    @ReactMethod
    fun saveBackupFileWithPicker(filePath: String, callback: Callback) {
        Log.d(TAG, "saveBackupFileWithPicker: $filePath")

        try {
            val sourceFile = File(filePath)

            if (!sourceFile.exists()) {
                val errorMsg = "Backup file does not exist: $filePath"
                Log.e(TAG, errorMsg)
                callback.invoke(errorMsg)
                return
            }

            val fileName = sourceFile.name
            Log.d(TAG, "Launching file picker for: $fileName")

            // Store the file path and callback for later use
            pendingBackupFilePath = filePath
            pendingBackupCallback = callback

            // Create an intent to let user choose where to save the file
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            val activity = currentActivity
            if (activity == null) {
                callback.invoke("Activity not available")
                pendingBackupFilePath = null
                pendingBackupCallback = null
                return
            }

            activity.startActivityForResult(intent, CREATE_BACKUP_FILE_REQUEST)

        } catch (e: Exception) {
            Log.e(TAG, "Error launching file picker: ${e.message}")
            e.printStackTrace()
            callback.invoke(e.message)
            pendingBackupFilePath = null
            pendingBackupCallback = null
        }
    }

    private fun handleBackupFileSelection(resultCode: Int, data: Intent?) {
        val callback = pendingBackupCallback
        val filePath = pendingBackupFilePath

        // Clear pending state
        pendingBackupCallback = null
        pendingBackupFilePath = null

        if (callback == null || filePath == null) {
            Log.e(TAG, "No pending backup operation")
            return
        }

        if (resultCode != Activity.RESULT_OK || data?.data == null) {
            Log.d(TAG, "User cancelled file picker")
            callback.invoke("User cancelled")
            return
        }

        try {
            val destinationUri = data.data!!
            val sourceFile = File(filePath)

            Log.d(TAG, "Copying backup to selected location: $destinationUri")

            reactContext.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Backup saved successfully to selected location")
            callback.invoke(null)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving backup to selected location: ${e.message}")
            e.printStackTrace()
            callback.invoke(e.message)
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun logFileDirectory(usePublicLogDir: Boolean): String? {
        Log.d(TAG, "logFileDirectory: usePublicLogDir=$usePublicLogDir")
        return utils.getLogDirectory(usePublicLogDir)?.absolutePath
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun setProfileLogLevel(setProfileLogLevelRequest: String): String {
        Log.d(TAG, "setProfileLogLevel: $setProfileLogLevelRequest")

        return StatusBackendClient.executeStatusGoRequestWithResult(
            endpoint = "SetProfileLogLevel",
            requestBody = setProfileLogLevelRequest,
            statusgoFunction = { Statusgo.setProfileLogLevel(setProfileLogLevelRequest) }
        )
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun setProfileLogEnabled(setProfileLogEnabledRequest: String): String {
        Log.d(TAG, "setProfileLogEnabled: $setProfileLogEnabledRequest")

        return StatusBackendClient.executeStatusGoRequestWithResult(
            endpoint = "SetLogEnabled",
            requestBody = setProfileLogEnabledRequest,
            statusgoFunction = { Statusgo.setProfileLogEnabled(setProfileLogEnabledRequest) }
        )
    }

    @ReactMethod
    fun setPreLoginLogLevel(setPreLoginLogLevelRequest: String) {
        Log.d(TAG, "setPreLoginLogLevel: $setPreLoginLogLevelRequest")

        StatusBackendClient.executeStatusGoRequest(
            endpoint = "SetPreLoginLogLevel",
            requestBody = setPreLoginLogLevelRequest,
            statusgoFunction = { Statusgo.setPreLoginLogLevel(setPreLoginLogLevelRequest) }
        )
    }

    @ReactMethod
    fun setPreLoginLogEnabled(setPreLoginLogEnabledRequest: String) {
        Log.d(TAG, "setPreLoginLogEnabled: $setPreLoginLogEnabledRequest")

        StatusBackendClient.executeStatusGoRequest(
            endpoint = "SetPreLoginLogEnabled",
            requestBody = setPreLoginLogEnabledRequest,
            statusgoFunction = { Statusgo.setPreLoginLogEnabled(setPreLoginLogEnabledRequest) }
        )
    }
}
