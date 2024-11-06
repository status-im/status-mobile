package im.status.ethereum

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.GlideModule
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import java.io.InputStream
import java.util.concurrent.Executors

class CustomGlideModule : GlideModule {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Create a single thread executor for background operations
        val executor = Executors.newSingleThreadExecutor()
        val mainHandler = Handler(Looper.getMainLooper())
        
        // Execute network operation in background
        executor.execute {
            val client = StatusOkHttpClientFactory().createNewNetworkModuleClient()
            
            // Post results back to main thread
            mainHandler.post {
                client?.let { 
                    val factory = OkHttpUrlLoader.Factory(it)
                    registry.replace(
                        GlideUrl::class.java,
                        InputStream::class.java,
                        factory
                    )
                }
            }
        }
        
        // Clean up executor
        executor.shutdown()
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // No additional options needed
    }
} 
