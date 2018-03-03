package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParserTest;
import com.quasiris.qsf.util.PrintUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformerTest {

    @Test
    public void transformSort() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        String sort = "[{\"price\": {\"order\": \"asc\",\"mode\": \"avg\"}}]";
        transformer.addSortMapping("name_asc", sort);
        ObjectNode elasticQuery = transform(transformer,  "sort=name_asc");
        Assert.assertEquals("asc", elasticQuery.get("sort").get(0).get("price").get("order").asText());

    }

    @Test
    public void transformFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        Assert.assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get(0).get("term").get("colorElasticField").asText());
    }


    @Test
    public void transformRangeFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("price", "priceElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.price.range=3,5");
        PrintUtil.print(elasticQuery);

        Assert.assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get(0).get("range").get("priceElasticField").get("gte").asText());
        Assert.assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get(0).get("range").get("priceElasticField").get("lte").asText());
    }

    @Test
    public void transformFilterWithStaticDefinedFilters() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location-with-static-defined-filters.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        Assert.assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get(0).get("term").get("colorElasticField").asText());
        Assert.assertEquals("alice", elasticQuery.get("query").get("bool").get("filter").get(2).get("term").get("staticElasticField").asText());
    }

    @Test
    public void transformFilterVersion1() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setElasticVersion(1);
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location-v1.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        Assert.assertEquals("red", elasticQuery.get("query").get("filtered").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
    }

    @Test
    public void transformPaging() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        ObjectNode elasticQuery = transform(transformer,  "page=3");
        Assert.assertEquals(10, elasticQuery.get("size").asInt());
        Assert.assertEquals(20, elasticQuery.get("from").asInt());
    }

    @Test
    public void transformPagingDefault() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        ObjectNode elasticQuery = transform(transformer,  "q=foo");
        Assert.assertEquals(10, elasticQuery.get("size").asInt());
        Assert.assertEquals(0, elasticQuery.get("from").asInt());
    }

    @Test
    public void transformPagingWithRows() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        ObjectNode elasticQuery = transform(transformer,  "q=foo", "rows=5", "page=5");
        Assert.assertEquals(5, elasticQuery.get("size").asInt());
        Assert.assertEquals(20, elasticQuery.get("from").asInt());

    }

    private ObjectNode transform(ElasticQsfqlQueryTransformer transformer, String... parameters) throws PipelineContainerException {
        SearchQuery searchQuery = QsfqlParserTest.createQuery(parameters);
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }

}