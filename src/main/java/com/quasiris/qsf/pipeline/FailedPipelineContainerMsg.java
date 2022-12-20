package com.quasiris.qsf.pipeline;

public class FailedPipelineContainerMsg {
    private String prefix;
    private Throwable throwable;
    private String additionalMsg;

    public FailedPipelineContainerMsg() {
    }

    public FailedPipelineContainerMsg(String prefix, Throwable throwable, String additionalMsg) {
        this.prefix = prefix;
        this.throwable = throwable;
        this.additionalMsg = additionalMsg;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getAdditionalMsg() {
        return additionalMsg;
    }

    public void setAdditionalMsg(String additionalMsg) {
        this.additionalMsg = additionalMsg;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
