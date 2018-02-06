package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.elastic.Elastic2SearchResultMappingTransformer;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticFilter;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticParameterQueryTransformer;
import com.quasiris.qsf.pipeline.filter.elastic.MockElasticClient;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mki on 07.11.17.
 */
public class SearchIntentLocationTest extends AbstractPipelineTest {

    @Test
    public void debug() throws Exception {
        try {
            testSearchIntentLocation();
        } catch (PipelineContainerException e) {
            System.out.println(e.getErrorMessage());
        }
    }


    @Test
    public void testSearchIntentLocation() throws Exception {
        MockElasticClient mockElasticClient = new MockElasticClient();
        //mockElasticClient.setRecord(true);
        QSQLRequestFilter qsqlRequestFilter = new QSQLRequestFilter();

        ElasticFilter elasticFilter = new ElasticFilter();
        elasticFilter.setResultSetId("locationLookup");
        elasticFilter.setElasticBaseUrl("http://localhost:9214/osm");
        elasticFilter.setElasticClient(mockElasticClient);


        ElasticParameterQueryTransformer queryTransformer = new ElasticParameterQueryTransformer();
        queryTransformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        queryTransformer.addAggregation("places", "place");
        queryTransformer.addAggregation("tag", "tagkey_is_in");

        elasticFilter.setQueryTransformer(queryTransformer);

        Elastic2SearchResultMappingTransformer searchResultTransformer = new Elastic2SearchResultMappingTransformer();
        elasticFilter.setSearchResultTransformer(searchResultTransformer);

        SearchIntentLocationFilter searchIntentLocationFilter = new SearchIntentLocationFilter();

        Pipeline pipeline = PipelineBuilder.create().
                pipeline("locationLookup").
                timeout(10000000L).
                    filter(qsqlRequestFilter).
                    filter(elasticFilter).
                    filter(searchIntentLocationFilter).
                build();

        Assert.assertNotNull(pipeline.print(""));


        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost/foo/bar?q=Dr.%20Thomas%20M%C3%BCller%20Darmstadt");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        SearchResult searchResult = pipelineContainer.getSearchResult("search-intent");
        searchResult.setTime(pipelineContainer.currentTime());

        Assert.assertEquals(Long.valueOf(1), searchResult.getTotal());
        Assert.assertEquals(1,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        Assert.assertEquals(2, document.getFieldCount());
        Assert.assertEquals("Darmstadt", document.getFieldValue("location"));
        Assert.assertEquals("Dr. Thomas Müller", document.getFieldValue("other"));

        SearchResult locationLookup = pipelineContainer.getSearchResult("locationLookup");
        Assert.assertEquals("places", locationLookup.getFacets().get(0).getName());


    }
}
