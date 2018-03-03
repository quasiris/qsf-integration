package com.quasiris.qsf.test;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.util.PrintUtil;

/**
 * Created by mki on 25.11.17.
 */
public class AbstractPipelineTest {


    public void print(Object object) {
        PrintUtil.print(object);
    }

    public void print(PipelineContainer pipelineContainer) {
        print(pipelineContainer.getSearchQuery());
        print(pipelineContainer.getSearchResults());
    }
}
