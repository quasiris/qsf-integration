package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.filter.solr.SolrClientFactory;
import com.quasiris.qsf.pipeline.filter.solr.SolrFilterBuilder;
import com.quasiris.qsf.pipeline.filter.solr.SolrParameterQueryTransformer;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 07.11.17.
 */
@Disabled
public class SolrPipelineTest extends AbstractPipelineTest {


    private boolean debug = false;

    @Test
    public void debug() throws Exception {
        Assertions.assertThrows(PipelineContainerDebugException.class, () -> {
            this.debug = true;
            testSolrPipeline();
        });
    }


    @Test
    public void testSolrPipeline() throws Exception {
        String baseUrl = "http://localhost:8983/solr/gettingstarted";

        SolrClientFactory.setSolrClient(Mockfactory.createSolrClient(baseUrl), baseUrl);
        Pipeline pipeline = PipelineBuilder.create().
                pipeline("products").
                timeout(1000L).
                filter(SolrFilterBuilder.create().
                        baseUrl(baseUrl).
                        queryTransformer(SolrParameterQueryTransformer.class).
                        param("q","${q}").
                        param("fq","cat:*").
                        param("sort","id asc").
                        param("rows","9").
                        param("facet","true").
                        param("facet.mincount","1").
                        param("facet.field","price").
                        param("facet.field","inStock").
                        param("facet.field","author").
                        param("facet.field","genre_s").
                        mapField("id","id").
                        mapField("id","productId").
                        mapField("author","author").
                        mapField("price","price").
                        mapField("inStock","stock").
                        mapField("genre_s","genre").
                        resultField("url","http://quasiris.de/shop/products/${id}").
                        mapFacet("genre_s", "genre").
                        mapFacetName("genre", "Genre").
                        mapFacet("author", "author").
                        mapFacetName("author", "Autor").
                        resultSetId("products").
                        build()).
                build();

        assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=*:*&foo=bar");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                debug(debug).
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();


        if(!pipelineContainer.isSuccess()) {
            fail();
        }

        SearchResult searchResult = pipelineContainer.getSearchResult("products");
        assertEquals(Long.valueOf(34), searchResult.getTotal());
        assertEquals(9,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        assertEquals(7, document.getFieldCount());
        assertEquals("6.99", document.getFieldValue("price"));
        assertEquals("Roger Zelazny", document.getFieldValue("author"));
        assertEquals("fantasy", document.getFieldValue("genre"));
        assertEquals("0380014300", document.getFieldValue("id"));
        assertEquals("0380014300", document.getFieldValue("productId"));
        assertEquals("http://quasiris.de/shop/products/0380014300", document.getFieldValue("url"));
        assertEquals("true", document.getFieldValue("stock"));

        Facet facet = searchResult.getFacetById("author");
        assertEquals("Autor", facet.getName());
        assertEquals("author", facet.getId());
        assertEquals(Long.valueOf(22), facet.getCount());
        assertEquals(Long.valueOf(32), facet.getResultCount());

        FacetValue facetValue = facet.getValues().get(0);
        assertEquals("george", facetValue.getValue());
        assertEquals(Long.valueOf(3), facetValue.getCount());
        assertEquals("author=george", facetValue.getFilter());

    }


}
