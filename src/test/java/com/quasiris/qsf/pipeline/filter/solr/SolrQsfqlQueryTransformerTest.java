package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTestUtil;
import com.quasiris.qsf.query.SearchQuery;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by mki on 16.01.18.
 */
public class SolrQsfqlQueryTransformerTest {


    @Test
    public void transformQuery() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo");
        assertEquals("foo", solrQuery.getQuery());
    }

    @Test
    public void transformSort() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addSortMapping("name_asc", "nameSort asc");
        SolrQuery solrQuery = transform(transformer,  "sort=name_asc");
        assertEquals("nameSort asc", solrQuery.getParams("sort")[0]);

    }

    @Test
    public void transformFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand=foo");
        assertEquals("{!tag=brand}brandSolrField:(foo)", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformMultipleFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand=foo", "f.brand=bar");
        assertEquals("{!tag=brand}brandSolrField:(foo OR bar)", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformMultipleAndFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand.and=foo", "f.brand.and=bar");
        assertEquals("{!tag=brand}brandSolrField:(foo AND bar)", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformMultipleOrFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("brand", "brandSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar");
        assertEquals("{!tag=brand}brandSolrField:(foo OR bar)", solrQuery.getFilterQueries()[0]);
    }


    @Test
    public void transformRangeFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("price", "priceSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.price.range=3,5");
        assertEquals("{!tag=price}priceSolrField:[3.0 TO 5.0]", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformRangeUpperLowerBoundExcludedFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("price", "priceSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.price.range={3,5}");
        assertEquals("{!tag=price}priceSolrField:{3.0 TO 5.0}", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformRangeUpperLowerBoundIncludedFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("price", "priceSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.price.range=[3,5]");
        assertEquals("{!tag=price}priceSolrField:[3.0 TO 5.0]", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformRangeUpperLowerBoundIncludedExcludedFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("price", "priceSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.price.range=[3,5}");
        assertEquals("{!tag=price}priceSolrField:[3.0 TO 5.0}", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformRangeMinMaxFilter() throws Exception {

        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        transformer.addFilterMapping("price", "priceSolrField");
        SolrQuery solrQuery = transform(transformer,  "f.price.range=min,max");
        assertEquals("{!tag=price}priceSolrField:[* TO *]", solrQuery.getFilterQueries()[0]);
    }

    @Test
    public void transformPaging() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "page=3");
        assertEquals(Integer.valueOf(10), solrQuery.getRows());
        assertEquals(Integer.valueOf(20), solrQuery.getStart());
    }

    @Test
    public void transformPagingDefault() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo");
        assertEquals(Integer.valueOf(10), solrQuery.getRows());
        assertEquals(Integer.valueOf(0), solrQuery.getStart());
    }

    @Test
    public void transformPagingWithRows() throws Exception {
        SolrQsfqlQueryTransformer transformer = new SolrQsfqlQueryTransformer();
        SolrQuery solrQuery = transform(transformer,  "q=foo", "rows=5", "page=5");
        assertEquals(Integer.valueOf(5), solrQuery.getRows());
        assertEquals(Integer.valueOf(20), solrQuery.getStart());
    }

    private SolrQuery transform(SolrQsfqlQueryTransformer transformer, String... parameters) throws Exception {
        SearchQuery searchQuery = QsfqlParserTestUtil.createQuery(parameters);
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        SolrQuery solrQuery = transformer.getSolrQuery();
        return solrQuery;
    }

}