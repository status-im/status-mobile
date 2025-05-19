package im.status.ethereum.module

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Callback
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import java.io.File
import androidx.core.content.FileProvider
import android.text.Html

class MailManager(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val utils = Utils(reactContext)

    override fun getName() = "MailManager"

        @ReactMethod
        fun mail(options: ReadableMap, callback: Callback) {
            Log.d(TAG, "attempting to send email")
            val i = Intent(Intent.ACTION_SEND_MULTIPLE)
            val selectorIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            i.selector = selectorIntent

            if (options.hasKey("subject") && !options.isNull("subject")) {
                i.putExtra(Intent.EXTRA_SUBJECT, options.getString("subject"))
            }

            if (options.hasKey("body") && !options.isNull("body")) {
                val body = options.getString("body")
                if (options.hasKey("isHTML") && options.getBoolean("isHTML")) {
                    i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body))
                } else {
                    i.putExtra(Intent.EXTRA_TEXT, body)
                }
            }

            if (options.hasKey("recipients") && !options.isNull("recipients")) {
                val recipients = options.getArray("recipients")
                i.putExtra(Intent.EXTRA_EMAIL, this.utils.readableArrayToStringArray(recipients!!))
            }

            if (options.hasKey("ccRecipients") && !options.isNull("ccRecipients")) {
                val ccRecipients = options.getArray("ccRecipients")
                i.putExtra(Intent.EXTRA_CC, this.utils.readableArrayToStringArray(ccRecipients!!))
            }

            if (options.hasKey("bccRecipients") && !options.isNull("bccRecipients")) {
                val bccRecipients = options.getArray("bccRecipients")
                i.putExtra(Intent.EXTRA_BCC, this.utils.readableArrayToStringArray(bccRecipients!!))
            }

            if (options.hasKey("attachments") && !options.isNull("attachments")) {
                val r = options.getArray("attachments")
                val length = r?.size() ?: 0

                val provider = reactContext.applicationContext.packageName + ".rnmail.provider"
                val resolvedIntentActivities = reactContext.packageManager.queryIntentActivities(i,
                        PackageManager.MATCH_DEFAULT_ONLY)

                val uris = ArrayList<Uri>()
                for (keyIndex in 0 until length) {
                    val clip = r?.getMap(keyIndex)
                    val uri: Uri
                    if (clip?.hasKey("path") == true && !clip.isNull("path")) {
                        val path = clip.getString("path")
                        val file = File(path)
                        uri = FileProvider.getUriForFile(reactContext, provider, file)
                    } else if (clip?.hasKey("uri") == true && !clip.isNull("uri")) {
                        val uriPath = clip.getString("uri")
                        uri = Uri.parse(uriPath)
                    } else {
                        callback.invoke("not_found")
                        return
                    }
                    uris.add(uri)

                    for (resolvedIntentInfo in resolvedIntentActivities) {
                        val packageName = resolvedIntentInfo.activityInfo.packageName
                        reactContext.grantUriPermission(packageName, uri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }

            val manager = reactContext.packageManager
            val list = manager.queryIntentActivities(i, 0)

            if (list == null || list.isEmpty()) {
                Log.d(TAG, "not_available")
                callback.invoke("not_available")
                return
            }

            if (list.size == 1) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    reactContext.startActivity(i)
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message!!)
                    callback.invoke("error")
                }
            } else {
                var chooserTitle = "Send Mail"

                if (options.hasKey("customChooserTitle") && !options.isNull("customChooserTitle")) {
                    chooserTitle = options.getString("customChooserTitle") ?: ""
                }

                val chooser = Intent.createChooser(i, chooserTitle)
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                try {
                    reactContext.startActivity(chooser)
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message!!)
                    callback.invoke("error")
                }
            }
        }

    companion object {
        private const val TAG = "MailManager"
    }

}
