package com.quasiris.qsf.pipeline;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class PipelineStatus {
    private boolean success = true;
    private boolean timeout = false;
    private final List<FailedPipelineContainerMsg> messages = new ArrayList<>();

    public void error(String prefix, Throwable throwable) {
        error(prefix, null, throwable);
    }

    public synchronized void error(String prefix, String message, Throwable throwable) {
        this.success = false;
        if (throwable instanceof TimeoutException) {
            timeout = true;
        }
        messages.add(new FailedPipelineContainerMsg(prefix, throwable, message));
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public List<FailedPipelineContainerMsg> getMessages() {
        return messages;
    }

    public String constructErrorMsg() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=======START ERROR MESSAGE=======\n");
        List<FailedPipelineContainerMsg> collectedMsgs = messages.stream()
                .sorted(Comparator.comparing(FailedPipelineContainerMsg::getPrefix))
                .collect(Collectors.toList());
        for (FailedPipelineContainerMsg msg : collectedMsgs) {
            appendLastErrorMsg(sb, msg);
        }
        sb.append("\n=======END ERROR MESSAGE=======\n");
        return sb.toString();
    }

    private void appendLastErrorMsg(StringBuilder sb, FailedPipelineContainerMsg msg) {
        sb.append("\n-----------------\n");
        sb.append(msg.getPrefix()).append(":\n");
        if (!Strings.isNullOrEmpty(msg.getAdditionalMsg())) {
            sb.append("Additional message:\n").append(msg.getAdditionalMsg()).append("\n");
        }
        sb.append("Stack trace:\n").append(Throwables.getStackTraceAsString(msg.getThrowable()));
    }

}
