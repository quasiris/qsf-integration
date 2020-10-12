package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.elastic.suggest.SuggestQueryTransoformer;
import com.quasiris.qsf.query.SearchFilterBuilder;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        JSONAssert.assertEquals("{ \"title\": {\"terms\": {\"field\": \"title\",\"include\": \"test.*\"}}}", jsonBuilder.path("aggs").writeCurrentAsString(), true);
    }

    @Test
    public void testSecondWord() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        transformer.setProfile(Profiles.queryString());
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("test fo");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);

        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        JSONAssert.assertEquals("[{\"query_string\": {\"query\": \"test\"}}]", jsonBuilder.paths("query/bool/must").writeCurrentAsString(), true);
        JSONAssert.assertEquals("{ \"title\": {\"terms\": {\"field\": \"title\",\"include\": \"fo.*\"}}}", jsonBuilder.root().path("aggs").writeCurrentAsString(), true);

    }

    @Test
    public void testFirstWordWithFilter() throws Exception {
        SuggestQueryTransoformer transformer = new SuggestQueryTransoformer(Arrays.asList("title"));
        SearchQuery searchQuery = new SearchQuery();

        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("tenant").value("quasiris").build());
        searchQuery.getSearchFilterList().add(SearchFilterBuilder.create().withId("code").value("suggest").build());
        searchQuery.setQ("test");
        ObjectNode elasticQuery = transform(transformer,  searchQuery);

        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        JSONAssert.assertEquals("[{\"term\": {\"tenant\": \"quasiris\"}},{\"term\": {\"code\": \"suggest\"}}]", jsonBuilder.paths("query/bool/filter/bool/must").writeCurrentAsString(), true);
        JSONAssert.assertEquals("{ \"title\": {\"terms\": {\"field\": \"title\",\"include\": \"test.*\"}}}", jsonBuilder.root().path("aggs").writeCurrentAsString(), true);
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

        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        JSONAssert.assertEquals("[{\"term\": {\"tenant\": \"quasiris\"}},{\"term\": {\"code\": \"suggest\"}}]", jsonBuilder.paths("query/bool/filter/bool/must").writeCurrentAsString(), true);
        JSONAssert.assertEquals("[{\"query_string\": {\"query\": \"test\"}}]", jsonBuilder.root().paths("query/bool/must").writeCurrentAsString(), true);
        JSONAssert.assertEquals("{ \"title\": {\"terms\": {\"field\": \"title\",\"include\": \"fo.*\"}}}", jsonBuilder.root().path("aggs").writeCurrentAsString(), true);

    }


    private ObjectNode transform(SuggestQueryTransoformer transformer, SearchQuery searchQuery) throws PipelineContainerException {
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }



}