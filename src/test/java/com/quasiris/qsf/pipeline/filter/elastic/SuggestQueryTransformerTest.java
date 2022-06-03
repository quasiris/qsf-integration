package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.elastic.suggest.SuggestQueryTransoformer;
import com.quasiris.qsf.query.SearchFilterBuilder;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by tbl on 12.10.20.
 */
public class SuggestQueryTransformerTest {

    @Test
    public void testFirstWord() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("test");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "first-word.json");
    }

    @Test
    public void testSecondWord() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        transformer.setProfile(Profiles.queryString());
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("test fo");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);

        assertQuery(elasticQuery, "second-word.json");
    }

    @Test
    public void testFirstWordWithFilter() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        SearchQuery searchQuery = new SearchQuery();

        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("tenant").value("quasiris").build());
        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("code").value("suggest").build());
        searchQuery.setQ("test");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);

        assertQuery(elasticQuery, "first-word-with-filter.json");
    }


    @Test
    public void testSecondWordWithFilter() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        transformer.setProfile(Profiles.queryString());
        SearchQuery searchQuery = new SearchQuery();

        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("tenant").value("quasiris").build());
        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("code").value("suggest").build());

        searchQuery.setQ("test fo");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);

        assertQuery(elasticQuery, "second-word-with-filter.json");
    }


    private ObjectNode transform(SuggestQueryTransoformer transformer, SearchQuery searchQuery) throws PipelineContainerException {
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }


    private void assertQuery(ObjectNode elasticQuery, String file) throws IOException {
        JsonAssert.assertJsonFile("classpath://com/quasiris/qsf/pipeline/filter/elastic/suggest/" + file, elasticQuery);
    }



}