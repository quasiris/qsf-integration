package com.quasiris.qsf.pipeline.filter;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineValidation;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.util.PrintUtil;

import java.beans.Transient;

/**
 * Created by mki on 04.11.17.
 */
public abstract class AbstractFilter implements Filter {

    private String id;

    private long startTime;

    private boolean active = true;

    @Override
    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void init() {

    }

    @Override
    public void end() {

    }

    @Transient
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    @Override
    public PipelineContainer onError(PipelineContainer pipelineContainer, Exception e) {
        pipelineContainer.error("error in filter: " + getId() + " message: " + e.getMessage());
        pipelineContainer.error(e);
        return pipelineContainer;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer = new StringBuilder();
        printer.append(indent).append("filter: ").append(getId()).append("\n");
        PrintUtil.printKeyValue(printer,indent, "acitve", String.valueOf(isActive()));
        return printer;
    }

    @Override
    public PipelineValidation validate(PipelineValidation pipelineValidation) {
        if(Strings.isNullOrEmpty(this.getId())) {
            pipelineValidation.error("The id for the filter is missing.");
        }
        return pipelineValidation;
    }
}
