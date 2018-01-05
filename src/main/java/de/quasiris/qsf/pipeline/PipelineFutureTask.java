package de.quasiris.qsf.pipeline;

import java.util.concurrent.*;

/**
 * Created by mki on 26.12.17.
 */
public class PipelineFutureTask<T> extends FutureTask<T> {

    private Pipeline pipeline;

    public PipelineFutureTask(Callable<T> callable, Pipeline pipeline) {
        super(callable);
        this.pipeline = pipeline;
    }

    public PipelineFutureTask(Runnable runnable, T result, Pipeline pipeline) {
        super(runnable, result);
        this.pipeline = pipeline;
    }

    public T getWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        return super.get(pipeline.getTimeout(), TimeUnit.MILLISECONDS);
    }

    public Pipeline getPipeline() {
        return pipeline;
    }
}
