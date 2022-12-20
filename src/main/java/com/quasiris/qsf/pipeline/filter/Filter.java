package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineValidation;
import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 04.11.17.
 */
public interface Filter {

    void start();

    long getCurrentTime();

    void setId(String id);

    String getId();
    void init();

    PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception;

    PipelineContainer onError(PipelineContainer pipelineContainer, Exception e);

    void end();

    boolean isActive();

    StringBuilder print(String indent);

    PipelineValidation validate(PipelineValidation pipelineValidation);

    void setExecLocationId(String execLocationId);
}
