package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.mapping.ParameterMapper;
import com.quasiris.qsf.mapping.SimpleParameterMappingDTO;
import com.quasiris.qsf.pipeline.PipelineContainer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformerParameterMapperTest extends ElasticQsfqlQueryTransformerTest{

    @Override
    protected ParameterMapper getParameterMapper(PipelineContainer pipelineContainer) {
        try {
            SimpleParameterMappingDTO mapping =
                    readSimpleParameterMappingDTOFromFile("src/test/resources/com/quasiris/qsf/pipeline/filter/elastic/mapping/simple-mapping.json");
            return new ParameterMapper(mapping, pipelineContainer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleParameterMappingDTO readSimpleParameterMappingDTOFromFile(String fileName) throws IOException {
        SimpleParameterMappingDTO simpleParameterMappingDTO = JsonUtil.defaultMapper()
                .readValue(new File(fileName), SimpleParameterMappingDTO.class);
        return simpleParameterMappingDTO;
    }
}