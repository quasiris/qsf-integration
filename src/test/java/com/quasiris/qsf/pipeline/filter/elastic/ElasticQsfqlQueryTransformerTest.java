package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParserTest;
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
        Assert.assertEquals("red", elasticQuery.get("query").get("filter").get(0).get("term").get("colorElasticField").asText());
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

    private ObjectNode transform(ElasticQsfqlQueryTransformer transformer, String... parameters) {
        SearchQuery searchQuery = QsfqlParserTest.createQuery(parameters);
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }

}