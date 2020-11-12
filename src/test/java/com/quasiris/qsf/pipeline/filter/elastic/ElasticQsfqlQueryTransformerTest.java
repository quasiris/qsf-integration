package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Sort;
import com.quasiris.qsf.query.parser.QsfqlParserTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformerTest {

    @Test
    public void transformSortMapping() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        String sort = "[{\"price\": {\"order\": \"asc\",\"mode\": \"avg\"}}]";
        transformer.addSortMapping("name_asc", sort);
        ObjectNode elasticQuery = transform(transformer,  "sort=name_asc");
        assertEquals("asc", elasticQuery.get("sort").get(0).get("price").get("order").asText());
    }

    @Test
    public void transformSortField() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSort(new Sort("price", "asc"));

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertEquals("asc", elasticQuery.get("sort").get(0).get("price").asText());

    }

    @Test
    public void transformFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
    }

    @Test
    public void transformFilterOr() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
    }

    @Test
    public void transformFilterOrMultiple() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.addFilterMapping("size", "sizeElasticField");
        //transformer.setFilterVariable("filter");
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red", "f.size.or=xl", "f.size.or=xxl");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("xl", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("sizeElasticField").asText());
        assertEquals("xxl", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("sizeElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(2).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(2).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
    }


    @Test
    public void transformFilterNot() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand.not=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must_not").get(0).get("term").get("brandElasticField").asText());
    }


    @Test
    public void transformFilterRule() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterRule("(.+)", "attr_$1.keyword");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("attr_color.keyword").asText());
    }

    @Test
    public void transformRangeFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("price", "priceElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.price.range=3,5");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gte").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lt").asText());
    }

    @Test
    public void transformRangeFilterUpperLowerExcluded() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("price", "priceElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.price.range={3,5}");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gt").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lt").asText());
    }

    @Test
    public void transformRangeFilterUpperLowerIncluded() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        transformer.addFilterMapping("price", "priceElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.price.range=[3,5]");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gte").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lte").asText());
    }


    @Test
    public void transformFilterWithStaticDefinedFilters() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location-with-static-defined-filters.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("alice", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(2).get("term").get("staticElasticField").asText());
    }

    @Test
    public void transformFilterVersion1() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setElasticVersion(1);
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location-v1.json");
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("filtered").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
    }

    @Test
    public void transformPaging() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");
        ObjectNode elasticQuery = transform(transformer,  "page=3");
        assertEquals(10, elasticQuery.get("size").asInt());
        assertEquals(20, elasticQuery.get("from").asInt());
    }

    @Test
    public void transformPagingDefault() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        ObjectNode elasticQuery = transform(transformer,  "q=foo");
        assertEquals(10, elasticQuery.get("size").asInt());
        assertEquals(0, elasticQuery.get("from").asInt());
    }

    @Test
    public void transformPagingWithRows() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        ObjectNode elasticQuery = transform(transformer,  "q=foo", "rows=5", "page=5");
        assertEquals(5, elasticQuery.get("size").asInt());
        assertEquals(20, elasticQuery.get("from").asInt());

    }


    private ObjectNode transform(ElasticQsfqlQueryTransformer transformer, SearchQuery searchQuery) throws PipelineContainerException {
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }

    private ObjectNode transform(ElasticQsfqlQueryTransformer transformer, String... parameters) throws PipelineContainerException {
        SearchQuery searchQuery = QsfqlParserTest.createQuery(parameters);
        return transform(transformer, searchQuery);
    }

}