package im.status.ethereum.module

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
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

class LogManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)

    override fun getName() = "LogManager"

    private fun getRequestLogFile(): File {
        val pubDirectory = utils.getPublicStorageDirectory()
        return File(pubDirectory, requestsLogFileName)
    }

    private fun getGethLogFile(): File {
        val pubDirectory = utils.getPublicStorageDirectory()
        return File(pubDirectory, gethLogFileName)
    }

    fun prepareLogsFile(context: Context): File? {
        val logFile = getGethLogFile()

        try {
            logFile.setReadable(true)
            val parent = logFile.parentFile
            if (!parent?.canWrite()!!) {
                return null
            }
            if (!parent.exists()) {
                parent.mkdirs()
            }
            logFile.createNewFile()
            logFile.setWritable(true)
            Log.d(TAG, "Can write ${logFile.canWrite()}")
            val gethLogUri = Uri.fromFile(logFile)

            val gethLogFilePath = logFile.absolutePath
            Log.d(TAG, gethLogFilePath)

            return logFile
        } catch (e: Exception) {
            Log.d(TAG, "Can't create geth.log file! ${e.message}")
        }

        return null
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
    fun sendLogs(dbJson: String, jsLogs: String, callback: Callback) {
        Log.d(TAG, "sendLogs")
        if (!utils.checkAvailability()) {
            return
        }

        val context = reactApplicationContext
        val logsTempDir = File(context.cacheDir, "logs") // This path needs to be in sync with android/app/src/main/res/xml/file_provider_paths.xml
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
        val gethLogFile = getGethLogFile()
        val requestLogFile = getRequestLogFile()

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
            val filesToZip = mutableListOf(dbFile, gethLogFile, statusLogFile)
            if (requestLogFile.exists()) {
                filesToZip.add(requestLogFile)
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

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun logFileDirectory(): String? {
        return utils.getPublicStorageDirectory()?.absolutePath
    }

    companion object {
        private const val TAG = "LogManager"
        private const val gethLogFileName = "geth.log"
        private const val statusLogFileName = "Status.log"
        private const val requestsLogFileName = "api.log"
        private const val logsZipFileName = "Status-debug-logs.zip"
    }
}
