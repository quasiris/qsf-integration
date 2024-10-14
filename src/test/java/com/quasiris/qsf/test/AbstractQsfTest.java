package com.quasiris.qsf.test;

import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryMapper;

import java.io.File;
import java.io.IOException;

public abstract class AbstractQsfTest {


    public PipelineContainer getPipelineContainer(String searchQueryFile) throws IOException {
        SearchQuery searchQuery = getSearchQuery(searchQueryFile);
        PipelineContainer pipelineContainer = getPipelineContainer(searchQuery);
        return pipelineContainer;
    }

    public PipelineContainer getPipelineContainer(SearchQuery searchQuery) {
        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setSearchQuery(searchQuery);
        return pipelineContainer;
    }

    public SearchQuery getSearchQuery(String file) throws IOException {
        SearchQueryDTO searchQueryDTO = readSearchQueryFromFile(file);
        SearchQuery searchQuery = SearchQueryMapper.map(searchQueryDTO);
        return searchQuery;
    }

    public SearchQueryDTO readSearchQueryFromFile(String fileName) throws IOException {
        SearchQueryDTO searchQueryDTO = JsonUtil.defaultMapper()
                .readValue(new File(fileName), SearchQueryDTO.class);
        return searchQueryDTO;
    }
}
