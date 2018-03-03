package com.quasiris.qsf.pipeline;

import java.util.concurrent.*;

/**
 * Created by mki on 26.12.17.
 */
public class PipelineFutureTask<T> extends FutureTask<T> {

    private Pipeline pipeline;

    private final long startTime;

    public PipelineFutureTask(Callable<T> callable, Pipeline pipeline) {
        super(callable);
        this.pipeline = pipeline;
        this.startTime = System.currentTimeMillis();
    }

    public PipelineFutureTask(Runnable runnable, T result, Pipeline pipeline) {
        super(runnable, result);
        this.pipeline = pipeline;
        this.startTime = System.currentTimeMillis();
    }

    public T getWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        long timeout = startTime - System.currentTimeMillis() + pipeline.getTimeout();
        return super.get(timeout, TimeUnit.MILLISECONDS);
    }

    public Pipeline getPipeline() {
        return pipeline;
    }
}
