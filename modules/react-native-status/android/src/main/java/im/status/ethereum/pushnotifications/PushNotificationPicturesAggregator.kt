package im.status.ethereum.pushnotifications

import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.core.ImagePipeline
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import android.util.Log
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.util.concurrent.atomic.AtomicInteger

class PushNotificationPicturesAggregator(private val callback: Callback?) {
    companion object {
        const val LOG_TAG = "PushNotification"
    }

    interface Callback {
        fun call(largeIconImage: Bitmap?, bigPictureImage: Bitmap?)
    }

    private val count: AtomicInteger = AtomicInteger(0)
    private var largeIconImage: Bitmap? = null
    private var bigPictureImage: Bitmap? = null
    fun setBigPicture(bitmap: Bitmap?) {
        bigPictureImage = bitmap
        finished()
    }

    fun setBigPictureUrl(context: Context, url: String?) {
        if (null == url) {
            setBigPicture(null)
            return
        }
        var uri: Uri? = null
        uri = try {
            Uri.parse(url)
        } catch (ex: java.lang.Exception) {
            Log.e(LOG_TAG, "Failed to parse bigPictureUrl", ex)
            setBigPicture(null)
            return
        }
        val aggregator = this
        downloadRequest(context, uri, object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(bitmap: Bitmap?) {
                aggregator.setBigPicture(bitmap)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                aggregator.setBigPicture(null)
            }
        })
    }

    fun setLargeIcon(bitmap: Bitmap?) {
        largeIconImage = bitmap
        finished()
    }

    fun setLargeIconUrl(context: Context, url: String?) {
        if (null == url) {
            setLargeIcon(null)
            return
        }
        var uri: Uri? = null
        uri = try {
            Uri.parse(url)
        } catch (ex: java.lang.Exception) {
            Log.e(LOG_TAG, "Failed to parse largeIconUrl", ex)
            setLargeIcon(null)
            return
        }
        val aggregator = this
        downloadRequest(context, uri, object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(bitmap: Bitmap?) {
                aggregator.setLargeIcon(bitmap)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                aggregator.setLargeIcon(null)
            }
        })
    }

    private fun downloadRequest(context: Context, uri: Uri?, subscriber: BaseBitmapDataSubscriber) {
        val imageRequest: ImageRequest = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setRequestPriority(Priority.HIGH)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build()
        if (!Fresco.hasBeenInitialized()) {
            Fresco.initialize(context)
        }
        val dataSource: DataSource<CloseableReference<CloseableImage>> = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, context)
        dataSource.subscribe(subscriber, CallerThreadExecutor.getInstance())
    }

    private fun finished() {
        synchronized(count) {
            val `val`: Int = count.incrementAndGet()
            if (`val` >= 2 && callback != null) {
                callback.call(largeIconImage, bigPictureImage)
            }
        }
    }
}
