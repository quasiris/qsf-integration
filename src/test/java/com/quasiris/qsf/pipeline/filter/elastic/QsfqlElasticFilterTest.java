package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.builder.FacetBuilder;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 11.02.18.
 */
public class QsfqlElasticFilterTest extends AbstractPipelineTest {

    @Test
    public void testSubFacets() throws Exception {

        MockElasticSearchClient mockElasticClient = new MockElasticSearchClient();
        //mockElasticClient.setRecord(true);


        Pipeline pipeline = PipelineBuilder.create().
                pipeline("osm").
                timeout(100000000L).
                filter(ElasticFilterBuilder.create().
                        client(mockElasticClient).
                        baseUrl("http://localhost:9200/osm").
                        profile("classpath://com/quasiris/qsf/elastic/profiles/location-qsfql.json" ).
                        resultSetId("osm").
                        build()).
                build();

        assertNotNull(pipeline.print(""));

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("darmstadt");

        com.quasiris.qsf.query.Facet tagkeysFacet =
                com.quasiris.qsf.query.Facet.Builder.create().
                        id("tagkeys").
                        fieldName("tagkeys.keyword").
                        name("tagkeys").
                        build();


        com.quasiris.qsf.query.Facet subFacet = new com.quasiris.qsf.query.Facet();
        subFacet.setId("name");
        subFacet.setFieldName("name.keyword");
        subFacet.setName("name");

        tagkeysFacet.setChildren(subFacet);
        searchQuery.addFacet(tagkeysFacet);
        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                searchQuery(searchQuery).
                execute();

        if(!pipelineContainer.isSuccess()) {
            fail();
        }

        SearchResult searchResult = pipelineContainer.getSearchResult("osm");




        Facet tagkeys = searchResult.getFacetById("tagkeys");
        assertEquals("tagkeys", tagkeys.getName());
        assertEquals("tagkeys", tagkeys.getId());
        assertEquals(Long.valueOf(10), tagkeys.getCount());
        assertEquals(Long.valueOf(21), tagkeys.getResultCount());

        FacetValue facetValue = tagkeys.getValues().get(0);
        assertEquals("name", facetValue.getValue());
        assertEquals(Long.valueOf(5), facetValue.getCount());
        assertEquals("tagkeys=name", facetValue.getFilter());

        //assertEquals("tagkeys", facetValue.getChildren().getId());
        //assertEquals("tagkeys", facetValue.getChildren().getName());

        FacetValue subFacetValue = facetValue.getChildren().getValues().get(0);
        assertEquals("Darmstadt", subFacetValue.getValue() );
        assertEquals(Long.valueOf(1), subFacetValue.getCount() );

    }

}
