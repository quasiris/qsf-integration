package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.pipeline.filter.solr.SolrClientFactory;
import com.quasiris.qsf.pipeline.filter.solr.SolrFilterBuilder;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 17.01.18.
 */
@Disabled
public class QsfqlSolrFilterTest extends AbstractPipelineTest {


    @Test
    public void testMultipleFilter() throws Exception {
            SearchResult searchResult = executePipeline("http://localhost?q=*:*&foo=bar&f.genre=fantasy&f.category=book&f.category=hardcover");
            assertEquals(Long.valueOf(1), searchResult.getTotal());
            assertEquals(1,searchResult.getDocuments().size());

            Document document = searchResult.getDocuments().get(0);
            assertEquals(3, document.getFieldCount());
    }

    @Test
    public void testMultipleAndFilter() throws Exception {
        SearchResult searchResult = executePipeline("http://localhost?q=*:*&foo=bar&f.genre=fantasy&f.category.and=book&f.category.and=hardcover");
        assertEquals(Long.valueOf(1), searchResult.getTotal());
        assertEquals(1,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        assertEquals(3, document.getFieldCount());
    }

    @Test
    public void testMultipleOrFilter() throws Exception {
        SearchResult searchResult = executePipeline("http://localhost?q=*:*&foo=bar&f.genre=fantasy&f.category.or=book&f.category.or=hardcover");
        assertEquals(Long.valueOf(11), searchResult.getTotal());
        assertEquals(10,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        assertEquals(3, document.getFieldCount());
    }


    @Test
    public void smokeTest() throws Exception {

            SearchResult searchResult = executePipeline("http://localhost?q=*:*&foo=bar&f.genre=fantasy&page=2");
            assertEquals(Long.valueOf(11), searchResult.getTotal());
            assertEquals(1,searchResult.getDocuments().size());

            Document document = searchResult.getDocuments().get(0);
            assertEquals(3, document.getFieldCount());


            Facet facet = searchResult.getFacetById("genre");
            assertEquals("Genre", facet.getName());
            assertEquals("genre", facet.getId());
            assertEquals(Long.valueOf(3), facet.getCount());
            assertEquals(Long.valueOf(11), facet.getResultCount());

            FacetValue facetValue = facet.getValues().get(0);
            assertEquals("fantasy", facetValue.getValue());
            assertEquals(Long.valueOf(11), facetValue.getCount());
            assertEquals("genre=fantasy", facetValue.getFilter());

        }

        private SearchResult executePipeline(String url) throws PipelineContainerException, PipelineContainerDebugException {
                String baseUrl = "http://localhost:8983/solr/gettingstarted";
                SolrClientFactory.setSolrClient(Mockfactory.createSolrClient(baseUrl), baseUrl);

                Pipeline pipeline = PipelineBuilder.create().
                        pipeline("products").
                        timeout(1000000000L).
                        filter(new QSQLRequestFilter()).
                        filter(SolrFilterBuilder.create().
                                baseUrl(baseUrl).
                                param("facet", "true").
                                param("facet.field","author").
                                param("facet.field","genre_s").
                                mapFilter("genre", "genre_s").
                                mapFilter("category", "cat").
                                mapField("id","id").
                                mapField("genre_s","genre").
                                mapField("cat","category").
                                mapFacet("genre_s", "genre").
                                mapFacetName("genre", "Genre").
                                resultSetId("products").
                                build()).
                        build();

                assertNotNull(pipeline.print(""));

                HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest(url);

                PipelineContainer pipelineContainer = PipelineExecuter.create().
                        pipeline(pipeline).
                        httpRequest(httpServletRequest).
                        execute();

                if(!pipelineContainer.isSuccess()) {
                        fail();
                }

                SearchResult searchResult = pipelineContainer.getSearchResult("products");
                return searchResult;
        }


}
