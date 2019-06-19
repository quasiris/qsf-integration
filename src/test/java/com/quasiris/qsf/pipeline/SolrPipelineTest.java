package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.filter.solr.SolrClientFactory;
import com.quasiris.qsf.pipeline.filter.solr.SolrFilterBuilder;
import com.quasiris.qsf.pipeline.filter.solr.SolrParameterQueryTransformer;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.FacetValue;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mki on 07.11.17.
 */
public class SolrPipelineTest extends AbstractPipelineTest {


    private boolean debug = false;

    @Test
    //@Ignore
    public void debug() throws Exception {
        this.debug = true;
        try {
            testSolrPipeline();
        } catch (PipelineContainerDebugException e) {
            System.out.println(e.getDebugStack());
        } catch (PipelineContainerException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
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

        Assert.assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=*:*&foo=bar");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                debug(debug).
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();


        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        SearchResult searchResult = pipelineContainer.getSearchResult("products");
        Assert.assertEquals(Long.valueOf(34), searchResult.getTotal());
        Assert.assertEquals(9,searchResult.getDocuments().size());

        Document document = searchResult.getDocuments().get(0);
        Assert.assertEquals(7, document.getFieldCount());
        Assert.assertEquals("6.99", document.getFieldValue("price"));
        Assert.assertEquals("Roger Zelazny", document.getFieldValue("author"));
        Assert.assertEquals("fantasy", document.getFieldValue("genre"));
        Assert.assertEquals("0380014300", document.getFieldValue("id"));
        Assert.assertEquals("0380014300", document.getFieldValue("productId"));
        Assert.assertEquals("http://quasiris.de/shop/products/0380014300", document.getFieldValue("url"));
        Assert.assertEquals("true", document.getFieldValue("stock"));

        Facet facet = searchResult.getFacetById("author");
        Assert.assertEquals("Autor", facet.getName());
        Assert.assertEquals("author", facet.getId());
        Assert.assertEquals(Long.valueOf(22), facet.getCount());
        Assert.assertEquals(Long.valueOf(32), facet.getResultCount());

        FacetValue facetValue = facet.getValues().get(0);
        Assert.assertEquals("george", facetValue.getValue());
        Assert.assertEquals(Long.valueOf(3), facetValue.getCount());
        Assert.assertEquals("author=george", facetValue.getFilter());

    }


}
