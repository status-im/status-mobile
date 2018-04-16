package im.status.ethereum.module;

import java.util.concurrent.*;

public class StatusThreadPoolExecutor {
    private static final int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final BlockingQueue<Runnable> mQueue;
    private final ThreadPoolExecutor mThreadPool;

    private StatusThreadPoolExecutor() {
        mQueue = new LinkedBlockingQueue<>();

        mThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,
            NUMBER_OF_CORES,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mQueue);
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
