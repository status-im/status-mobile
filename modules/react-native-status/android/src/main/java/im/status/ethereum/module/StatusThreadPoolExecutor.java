package im.status.ethereum.module;

import java.util.concurrent.*;

/** Uses an unbounded queue, but allows timeout of core threads
 * (modified case 2 in
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html ) */
public class StatusThreadPoolExecutor {
    private static final int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static final int THREADS_TO_CORES_RATIO = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final BlockingQueue<Runnable> mQueue;
    private final ThreadPoolExecutor mThreadPool;

    private StatusThreadPoolExecutor() {
        mQueue = new LinkedBlockingQueue<>();

        mThreadPool = new ThreadPoolExecutor(
            THREADS_TO_CORES_RATIO * NUMBER_OF_CORES,
            THREADS_TO_CORES_RATIO * NUMBER_OF_CORES,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mQueue);

        // Allow pool to drain
        mThreadPool.allowCoreThreadTimeOut(true);
    }

    /** Pugh singleton */
    private static class Holder {
        private static StatusThreadPoolExecutor instance = new StatusThreadPoolExecutor();
    }

    public static StatusThreadPoolExecutor getInstance() {
        return Holder.instance;
    }

    public void execute(final Runnable r) {
        mThreadPool.execute(r);
    }
}
