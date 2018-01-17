package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import com.quasiris.qsf.pipeline.filter.solr.SolrFilterBuilder;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.FacetValue;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mki on 17.01.18.
 */
public class QsfqlSolrFilterTest extends AbstractPipelineTest {


    @Test
    public void testQsfqlSolrFilter() throws Exception {

            MockSolrClient mockSolrClient = Mockfactory.createSolrClient("http://localhost:8983/solr/gettingstarted");
            //mockSolrClient.setRecord(true);

            Pipeline pipeline = PipelineBuilder.create().
                    pipeline("products").
                    timeout(1000L).
                    filter(new QSQLRequestFilter()).
                    filter(SolrFilterBuilder.create().
                            solrClient(mockSolrClient).
                            param("facet", "true").
                            param("facet.field","author").
                            param("facet.field","genre_s").
                            mapFilter("genre", "genre_s").
                            mapField("id","id").
                            mapField("genre_s","genre").
                            mapFacet("genre_s", "genre").
                            mapFacetName("genre", "Genre").
                            resultSetId("products").
                            build()).
                    build();

            Assert.assertNotNull(pipeline.print(""));

            HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=*:*&foo=bar&f.genre=fantasy&page=2");

            PipelineContainer pipelineContainer = PipelineExecuter.create().
                    pipeline(pipeline).
                    httpRequest(httpServletRequest).
                    execute();

            if(!pipelineContainer.isSuccess()) {
                Assert.fail();
            }

            SearchResult searchResult = pipelineContainer.getSearchResult("products");
            Assert.assertEquals(Long.valueOf(11), searchResult.getTotal());
            Assert.assertEquals(1,searchResult.getDocuments().size());

            Document document = searchResult.getDocuments().get(0);
            Assert.assertEquals(2, document.getFieldCount());


            Facet facet = searchResult.getFacetById("genre");
            Assert.assertEquals("Genre", facet.getName());
            Assert.assertEquals("genre", facet.getId());
            Assert.assertEquals(Long.valueOf(3), facet.getCount());
            Assert.assertEquals(Long.valueOf(11), facet.getResultCount());

            FacetValue facetValue = facet.getValues().get(0);
            Assert.assertEquals("fantasy", facetValue.getValue());
            Assert.assertEquals(Long.valueOf(11), facetValue.getCount());
            Assert.assertEquals("genre=fantasy", facetValue.getFilter());

        }


}
