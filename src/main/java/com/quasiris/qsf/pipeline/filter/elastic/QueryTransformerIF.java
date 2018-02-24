package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;

/**
 * Created by mki on 04.92.18.
 */
public interface QueryTransformerIF {


    ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException;

    StringBuilder print(String indent);
}
