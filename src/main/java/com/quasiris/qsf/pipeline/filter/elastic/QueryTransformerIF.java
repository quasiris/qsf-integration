package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 04.92.18.
 */
public interface QueryTransformerIF {


    ObjectNode transform(PipelineContainer pipelineContainer);

    StringBuilder print(String indent);
}
