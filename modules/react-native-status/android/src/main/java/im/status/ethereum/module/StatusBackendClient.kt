package im.status.ethereum.module

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Callback
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class StatusBackendClient(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "StatusBackendClient"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val TIMEOUT_SECONDS = 30L
        @Volatile private var instance: StatusBackendClient? = null
        private lateinit var utils: Utils
        
        fun getInstance(): StatusBackendClient? = instance

        @JvmStatic
        fun executeStatusGoRequest(
            endpoint: String,
            requestBody: String,
            statusgoFunction: () -> String
        ) {
            val statusBackendClient = getInstance()
            if (statusBackendClient?.serverEnabled == true) {
                val result = statusBackendClient.request(endpoint, requestBody)
                result.onSuccess { response ->
                    utils.handleStatusGoResponse(response, endpoint)
                }.onFailure { error ->
                    Log.e(TAG, "request to $endpoint failed", error)
                }
            } else {
                val result = statusgoFunction()
                utils.handleStatusGoResponse(result, endpoint)
            }
        }

        @JvmStatic
        fun executeStatusGoRequestWithCallback(
            endpoint: String,
            requestBody: String,
            statusgoFunction: () -> String,
            callback: Callback?
        ) {
            val statusBackendClient = getInstance()
            if (statusBackendClient?.serverEnabled == true) {
                val runnable = Runnable {
                    val result = statusBackendClient.request(endpoint, requestBody)
                    result.onSuccess { response ->
                        callback?.invoke(response)
                    }.onFailure { error ->
                        Log.e(TAG, "request to $endpoint failed", error)
                        callback?.invoke(false)
                    }
                }
                StatusThreadPoolExecutor.getInstance().execute(runnable)
            } else {
                utils.executeRunnableStatusGoMethod(statusgoFunction, callback)
            }
        }

        @JvmStatic
        fun executeStatusGoRequestWithResult(
            endpoint: String,
            requestBody: String,
            statusgoFunction: () -> String
        ): String {
            val statusBackendClient = getInstance()
            return if (statusBackendClient?.serverEnabled == true) {
                val result = statusBackendClient.request(endpoint, requestBody)
                result.getOrElse { error ->
                    Log.e(TAG, "request to $endpoint failed", error)
                    ""
                }
            } else {
                statusgoFunction()
            }
        }
    }

    init {
        instance = this
        utils = Utils(reactContext)
    }

    override fun getName(): String = "StatusBackendClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val wsClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    
    @Volatile var serverEnabled = false
    @Volatile private var statusGoEndpoint: String? = null
    @Volatile private var signalEndpoint: String? = null
    @Volatile var rootDataDir: String? = null

    @ReactMethod
    fun configStatusBackendServer(
        serverEnabled: Boolean,
        statusGoEndpoint: String,
        signalEndpoint: String,
        rootDataDir: String
    ) {
        configure(serverEnabled, statusGoEndpoint, signalEndpoint, rootDataDir)
    }

    private fun configure(
        serverEnabled: Boolean,
        statusGoEndpoint: String,
        signalEndpoint: String,
        rootDataDir: String
    ) {
        Log.d(TAG, "configure: serverEnabled=$serverEnabled, statusGoEndpoint=$statusGoEndpoint, " +
                   "signalEndpoint=$signalEndpoint, rootDataDir=$rootDataDir")
        
        this.serverEnabled = serverEnabled
        if (serverEnabled) {
            this.statusGoEndpoint = statusGoEndpoint
            this.signalEndpoint = signalEndpoint
            this.rootDataDir = rootDataDir
            connectWebSocket()
        } else {
            disconnectWebSocket()
            this.statusGoEndpoint = null
            this.signalEndpoint = null
            this.rootDataDir = null
        }
    }

    private fun connectWebSocket() {
        if (!serverEnabled || signalEndpoint == null) {
            return
        }

        val request = Request.Builder()
            .url("$signalEndpoint")
            .build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                StatusModule.getInstance()?.handleSignal(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}")
            }
        })
    }

    private fun disconnectWebSocket() {
        webSocket?.cancel()
        webSocket = null
    }

    fun request(endpoint: String, body: String): Result<String> {
        if (!serverEnabled || statusGoEndpoint == null) {
            return Result.failure(IllegalStateException("Status backend server is not enabled"))
        }

        val fullUrl = "$statusGoEndpoint$endpoint"
        
        return try {
            val request = Request.Builder()
                .url(fullUrl)
                .post(body.toRequestBody(JSON))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Request to $endpoint succeeded: $responseBody")
                    Result.success(responseBody)
                } else {
                    val errorMsg = "Request failed with code ${response.code}: $responseBody"
                    Log.e(TAG, "Request to $endpoint failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException -> Log.e(TAG, "Request to $endpoint timed out", e)
                is SocketException -> Log.e(TAG, "Socket error for $endpoint", e)
                else -> Log.e(TAG, "Request to $endpoint failed with exception", e)
            }
            Result.failure(e)
        }
    }
} 
