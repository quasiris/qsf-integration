package com.quasiris.qsf.mapping;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.test.AbstractQsfTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParameterMapperTest extends AbstractQsfTest {

    @Test
    void testMapping() throws Exception {

        PipelineContainer pipelineContainer = getPipelineContainer("src/test/resources/com/quasiris/qsf/query/search-with-filter.json");

        List<ParameterMappingDTO> mapping = new ArrayList<>();
        mapping.add(new ParameterMappingDTO("searchQuery.q", "tbl"));
        mapping.add(new ParameterMappingDTO("searchQuery.rows", "size"));
        mapping.add(new ParameterMappingDTO("searchQuery.parameters.docId", "docId"));
        mapping.add(new ParameterMappingDTO("searchQuery.parameters.my.docId", "myDocIdWithDot"));
        mapping.add(new ParameterMappingDTO("searchQuery.filters.sku.value", "filterValue"));
        mapping.add(new ParameterMappingDTO("searchQuery.filters.my.sku.value", "mySku"));
        ParameterMapper parameterMapper = new ParameterMapper(mapping, pipelineContainer);

        assertEquals("my query", parameterMapper.get("tbl"));
        assertEquals(16, parameterMapper.get("size"));
        assertEquals("0123456789", parameterMapper.get("filterValue"));
        assertEquals("9876543210", parameterMapper.get("mySku"));
        assertEquals("my-doc-id", parameterMapper.get("docId"));
        assertEquals("my-doc-id-with-dot", parameterMapper.get("myDocIdWithDot"));



    }
}