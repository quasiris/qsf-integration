package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticFilterBuilder;
import com.quasiris.qsf.pipeline.filter.elastic.MockElasticClient;
import com.quasiris.qsf.pipeline.filter.elastic.MockElasticSearchClient;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 07.11.17.
 */
public class SearchIntentLocationTest extends AbstractPipelineTest {

    @Test
    public void debug() throws Exception {
        try {
            testSearchIntentLocation();
        } catch (PipelineContainerException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void testSearchIntentLocation() throws Exception {
        MockElasticSearchClient mockElasticClient = new MockElasticSearchClient();
        //mockElasticClient.setRecord(true);

        Pipeline pipeline = PipelineBuilder.create().
                pipeline("locationLookup").
                timeout(4000000L).
                filter(new QSQLRequestFilter()).
                filter(ElasticFilterBuilder.create().
                        client(mockElasticClient).
                        baseUrl("http://localhost:9214/osm").
                        profile("classpath://com/quasiris/qsf/elastic/profiles/location.json").
                        addAggregation("places", "place").
                        addAggregation("tag", "tagkey_is_in").
                        //filterPrefix("f.").
                        //mapFilter("farbe", "attrFarbe").
                        //mapField("url","url").
                        //mapFacet("facetAttrFarbe", "farbe").
                        //mapFacetName("farbe", "Farbe").
                        defaultSort("nameAZ").
                        mapSort("nameAZ", "[{ \"name\" : \"desc\" }]").
                        mapSort("nameZA", "[{ \"name\" : \"asc\" }]").
                        resultSetId("locationLookup").
                        build()).
                filter(new SearchIntentLocationFilter()).
                build();

        assertNotNull(pipeline.print(""));


        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost/foo/bar?q=Dr.%20Thomas%20M%C3%BCller%20Darmstadt");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();

        if(!pipelineContainer.isSuccess()) {
            fail();
        }

        SearchResult searchResult = pipelineContainer.getSearchResult("search-intent");
        searchResult.setTime(pipelineContainer.currentTime());

        assertEquals(Long.valueOf(1), searchResult.getTotal());
        assertEquals(1,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        assertEquals(2, document.getFieldCount());
        assertEquals("Darmstadt", document.getFieldValue("location"));
        assertEquals("Dr. Thomas Müller", document.getFieldValue("other"));

        SearchResult locationLookup = pipelineContainer.getSearchResult("locationLookup");
        assertEquals("places", locationLookup.getFacets().get(0).getName());


    }
}
