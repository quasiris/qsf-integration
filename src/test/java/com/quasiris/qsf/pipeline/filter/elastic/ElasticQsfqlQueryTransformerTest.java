package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Sort;
import com.quasiris.qsf.query.parser.QsfqlParserTest;
import com.quasiris.qsf.test.converter.NullValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformerTest {

    @DisplayName("Transform sort mapping")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformSortMapping(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        String sort = "[{\"price\": {\"order\": \"asc\",\"mode\": \"avg\"}}]";
        transformer.addSortMapping("name_asc", sort);
        ObjectNode elasticQuery = transform(transformer,  "sort=name_asc");
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
        assertEquals("asc", elasticQuery.get("sort").get(0).get("price").get("order").asText());
    }

    @DisplayName("Transform sort field")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformSortField(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSort(new Sort("price", "asc"));

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertEquals("asc", elasticQuery.get("sort").get(0).get("price").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));

    }

    @DisplayName("Transform filters")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformFilter(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform filter OR")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformFilterOr(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform multiple or filters")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformFilterOrMultiple(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.addFilterMapping("size", "sizeElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red", "f.size.or=xl", "f.size.or=xxl");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("xl", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("sizeElasticField").asText());
        assertEquals("xxl", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("sizeElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(2).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(2).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }


    @DisplayName("Transform not filters")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformFilterNot(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.not=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must_not").get(0).get("term").get("brandElasticField").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }


    @DisplayName("Transform filter rule")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformFilterRule(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterRule("(.+)", "attr_$1.keyword");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("term").get("attr_color.keyword").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }


    @DisplayName("Transform range filter")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformRangeFilter(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("price", "priceElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.price.range=3,5");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gte").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lte").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform range filter wit upper and lower bound excluded")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformRangeFilterUpperLowerExcluded(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("price", "priceElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.price.range={3,5}");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gt").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lt").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform range filter wit upper and lower bound included")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformRangeFilterUpperLowerIncluded(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("price", "priceElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.price.range=[3,5]");

        assertEquals("3.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("gte").asText());
        assertEquals("5.0", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("priceElasticField").get("lte").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }


    @DisplayName("Transform date range filter")
    @Test
    public void transformDateRangeFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.addFilterMapping("timestamp", "timestamp");
        ObjectNode elasticQuery = transform(transformer,  "f.timestamp.daterange=2021-01-02T23:00:00Z,2021-02-05T20:59:38Z");

        assertEquals("2021-01-03T00:00:00.000+0100", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("timestamp").get("gte").asText());
        assertEquals("2021-02-05T21:59:38.000+0100", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("timestamp").get("lte").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
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

    @DisplayName("Transform paging")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformPaging(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);

        ObjectNode elasticQuery = transform(transformer,  "page=3");
        assertEquals(10, elasticQuery.get("size").asInt());
        assertEquals(20, elasticQuery.get("from").asInt());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform paging with default values")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            "location-with-filters-variable.json, null, filters"
    })
    public void transformPagingDefault(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);

        ObjectNode elasticQuery = transform(transformer,  "q=foo");
        assertEquals(10, elasticQuery.get("size").asInt());
        assertEquals(0, elasticQuery.get("from").asInt());
        assertEquals("foo", elasticQuery.get("query").get("bool").get("must").get(0).get("dis_max").get("queries").get(0).get("query_string").get("query").asText());
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
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