package im.status.ethereum.module

import java.util.concurrent.*

/**
 * Uses an unbounded queue but allows timeout of core threads
 * (modified case 2 in
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html )
 */
class StatusThreadPoolExecutor private constructor() {
    private val NUMBER_OF_CORES: Int = Runtime.getRuntime().availableProcessors()
    private val THREADS_TO_CORES_RATIO: Int = 100
    private val KEEP_ALIVE_TIME: Int = 1
    private val KEEP_ALIVE_TIME_UNIT: TimeUnit = TimeUnit.SECONDS

    private val mQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val mThreadPool: ThreadPoolExecutor

    init {
        mThreadPool = ThreadPoolExecutor(
                THREADS_TO_CORES_RATIO * NUMBER_OF_CORES,
                THREADS_TO_CORES_RATIO * NUMBER_OF_CORES,
                KEEP_ALIVE_TIME.toLong(),
                KEEP_ALIVE_TIME_UNIT,
                mQueue
        )

        // Allow pool to drain
        mThreadPool.allowCoreThreadTimeOut(true)
    }

    /** Singleton holder */
    private object Holder {
        val instance = StatusThreadPoolExecutor()
    }

    companion object {
        @JvmStatic
        fun getInstance(): StatusThreadPoolExecutor {
            return Holder.instance
        }
    }

    fun execute(r: Runnable) {
        mThreadPool.execute(r)
    }
}
