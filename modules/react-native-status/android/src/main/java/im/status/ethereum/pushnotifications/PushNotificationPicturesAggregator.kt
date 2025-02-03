package im.status.ethereum.pushnotifications

import androidx.annotation.Nullable
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import android.util.Log
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.util.concurrent.atomic.AtomicInteger
import im.status.ethereum.pushnotifications.PushNotification.Companion.LOG_TAG

class PushNotificationPicturesAggregator(
    private val callback: (Bitmap?, Bitmap?) -> Unit
) {
    private val count = AtomicInteger(0)
    private var largeIconImage: Bitmap? = null
    private var bigPictureImage: Bitmap? = null

    fun setBigPicture(bitmap: Bitmap?) {
        bigPictureImage = bitmap
        finished()
    }

    fun setBigPictureUrl(context: Context, url: String?) {
        if (url == null) {
            setBigPicture(null)
            return
        }

        val uri = try {
            Uri.parse(url)
        } catch (ex: Exception) {
            Log.e(LOG_TAG, "Failed to parse bigPictureUrl", ex)
            setBigPicture(null)
            return
        }

        downloadRequest(context, uri, object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(@Nullable bitmap: Bitmap?) {
                setBigPicture(bitmap)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                setBigPicture(null)
            }
        })
    }

    fun setLargeIcon(bitmap: Bitmap?) {
        largeIconImage = bitmap
        finished()
    }

    fun setLargeIconUrl(context: Context, url: String?) {
        if (url == null) {
            setLargeIcon(null)
            return
        }

        val uri = try {
            Uri.parse(url)
        } catch (ex: Exception) {
            Log.e(LOG_TAG, "Failed to parse largeIconUrl", ex)
            setLargeIcon(null)
            return
        }

        downloadRequest(context, uri, object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(@Nullable bitmap: Bitmap?) {
                setLargeIcon(bitmap)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                setLargeIcon(null)
            }
        })
    }

    private fun downloadRequest(context: Context, uri: Uri, subscriber: BaseBitmapDataSubscriber) {
        val imageRequest = ImageRequestBuilder
            .newBuilderWithSource(uri)
            .setRequestPriority(Priority.HIGH)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .build()

        if (!Fresco.hasBeenInitialized()) {
            Fresco.initialize(context)
        }

        val dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, context)
        dataSource.subscribe(subscriber, CallerThreadExecutor.getInstance())
    }

    private fun finished() {
        synchronized(count) {
            if (count.incrementAndGet() >= 2) {
                callback(largeIconImage, bigPictureImage)
            }
        }
    }
}
