package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParserTest;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mki on 16.01.18.
 */
public class SolrQsfqlQueryTransformerTest {


    @Test
    public void transformQuery() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo");
        Assert.assertEquals("foo", solrQuery.getQuery());
    }

    @Test
    public void transformSort() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addSortMapping("name_asc", "nameSort asc");
        SolrQuery solrQuery = transform(transformer,  "sort=name_asc");
        Assert.assertEquals("nameSort asc", solrQuery.getParams("sort")[0]);

    }

    @Test
    public void transformFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand=foo");
        Assert.assertEquals("{!tag=brand}brandSolrField:(foo)", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformMultipleFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand=foo", "f.brand=bar");
        Assert.assertEquals("{!tag=brand}brandSolrField:(foo AND bar)", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformPaging() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "page=3");
        Assert.assertEquals(Integer.valueOf(10), solrQuery.getRows());
        Assert.assertEquals(Integer.valueOf(20), solrQuery.getStart());
    }

    @Test
    public void transformPagingDefault() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo");
        Assert.assertEquals(Integer.valueOf(10), solrQuery.getRows());
        Assert.assertEquals(Integer.valueOf(0), solrQuery.getStart());
    }

    @Test
    public void transformPagingWithRows() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo", "rows=5", "page=5");
        Assert.assertEquals(Integer.valueOf(5), solrQuery.getRows());
        Assert.assertEquals(Integer.valueOf(20), solrQuery.getStart());
    }

    private SolrQuery transform(SolrQsfqlQueryTransformer transformer, String... parameters) {
        SearchQuery searchQuery = QsfqlParserTest.createQuery(parameters);
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        SolrQuery solrQuery = transformer.getSolrQuery();
        return solrQuery;
    }

}