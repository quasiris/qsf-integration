package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Sort;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTest;
import com.quasiris.qsf.test.converter.NullValueConverter;
import com.quasiris.qsf.commons.util.DateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("Transform filters Multiselect")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
    })
    public void transformFilterMultiselect(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "f.brand=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("term").get("brandElasticField").asText());
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
    @DisplayName("Transform filter OR Multiselect")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
    })
    public void transformFilterOrMultiSelect(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setFilterVariable(filterVariable);
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red");
        assertEquals("red", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
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

    @DisplayName("Transform multiple or filters Multiselect")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
    })
    public void transformFilterOrMultipleMultiselect(
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
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.or=foo", "f.brand.or=bar", "f.color=red", "f.size.or=xl", "f.size.or=xxl");
        assertEquals("red", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("xl", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("bool").get("should").get(0).get("term").get("sizeElasticField").asText());
        assertEquals("xxl", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("bool").get("should").get(1).get("term").get("sizeElasticField").asText());
        assertEquals("foo", elasticQuery.get("post_filter").get("bool").get("must").get(2).get("bool").get("should").get(0).get("term").get("brandElasticField").asText());
        assertEquals("bar", elasticQuery.get("post_filter").get("bool").get("must").get(2).get("bool").get("should").get(1).get("term").get("brandElasticField").asText());
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


    @DisplayName("Transform not filters Multiselect")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
    })
    public void transformFilterNotMultiselect(
            @ConvertWith(NullValueConverter.class) String profile,
            @ConvertWith(NullValueConverter.class) String filterPath,
            @ConvertWith(NullValueConverter.class) String filterVariable) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/" + profile);
        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("color", "colorElasticField");
        transformer.setFilterPath(filterPath);
        transformer.setMultiSelectFilter(true);
        transformer.setFilterVariable(filterVariable);
        ObjectNode elasticQuery = transform(transformer,  "f.brand.not=foo", "f.color=red");
        assertEquals("red", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("colorElasticField").asText());
        assertEquals("foo", elasticQuery.get("post_filter").get("bool").get("must_not").get(0).get("term").get("brandElasticField").asText());
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

        assertTrue(DateUtil.isDateEqual("2021-01-03T00:00:00.000+0100", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("timestamp").get("gte").asText()));
        assertTrue(DateUtil.isDateEqual("2021-02-05T21:59:38.000+0100", elasticQuery.get("query").get("bool").get("filter").get("bool").get("must").get(0).get("range").get("timestamp").get("lte").asText()));
        Assertions.assertFalse(JsonBuilder.create().newJson(elasticQuery).exists("query/bool/$filters"));
    }

    @DisplayName("Transform facet")
    @Test
    public void transformFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet brand = new Facet();
        brand.setId("brand");
        brand.setName("brand");
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stock");
        stock.setName("stock");
        transformer.addAggregation(stock);


        ObjectNode elasticQuery = transform(transformer,  "q=*");

        assertEquals("brand", elasticQuery.get("aggs").get("brand").get("terms").get("field").asText());
        assertEquals("stock", elasticQuery.get("aggs").get("stock").get("terms").get("field").asText());
    }

    @DisplayName("Transform facet with multi select filters with or operator")
    @Test
    public void transformFacetFilterMultiselectOperatorOr() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet brand = new Facet();
        brand.setId("brandElasticField");
        brand.setName("brand");
        brand.setOperator(FilterOperator.OR);
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stockElasticField");
        stock.setName("stock");
        stock.setOperator(FilterOperator.OR);
        transformer.addAggregation(stock);

        Facet type = new Facet();
        type.setId("typeElasticField");
        type.setName("type");
        type.setOperator(FilterOperator.OR);
        transformer.addAggregation(type);

        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("stock", "stockElasticField");
        transformer.addFilterMapping("type", "typeElasticField");

        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.brand=waldschuh", "f.stock=true");

        assertEquals("true", elasticQuery.get("aggs").get("brand_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("stockElasticField").asText());
        assertEquals("brandElasticField", elasticQuery.get("aggs").get("brand_filter_wrapper").get("aggs").get("brand").get("terms").get("field").asText());

        assertEquals("waldschuh", elasticQuery.get("aggs").get("stock_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("stockElasticField", elasticQuery.get("aggs").get("stock_filter_wrapper").get("aggs").get("stock").get("terms").get("field").asText());



        assertEquals("waldschuh", elasticQuery.get("aggs").get("type_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("aggs").get("type_filter_wrapper").get("filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
        assertEquals("typeElasticField", elasticQuery.get("aggs").get("type_filter_wrapper").get("aggs").get("type").get("terms").get("field").asText());

        assertEquals("waldschuh", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
    }

   @DisplayName("Transform facet with multi select filters with AND operator")
    @Test
    public void transformFacetFilterMultiselectOperatorAnd() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet brand = new Facet();
        brand.setId("brandElasticField");
        brand.setName("brand");
        brand.setOperator(FilterOperator.AND);
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stockElasticField");
        stock.setName("stock");
        stock.setOperator(FilterOperator.AND);
        transformer.addAggregation(stock);

        Facet type = new Facet();
        type.setId("typeElasticField");
        type.setName("type");
        type.setOperator(FilterOperator.AND);
        transformer.addAggregation(type);

        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("stock", "stockElasticField");
        transformer.addFilterMapping("type", "typeElasticField");

        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.brand=waldschuh", "f.stock=true");

        assertEquals("waldschuh", elasticQuery.get("aggs").get("brand_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("aggs").get("brand_filter_wrapper").get("filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
        assertEquals("brandElasticField", elasticQuery.get("aggs").get("brand_filter_wrapper").get("aggs").get("brand").get("terms").get("field").asText());

        assertEquals("waldschuh", elasticQuery.get("aggs").get("stock_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("aggs").get("stock_filter_wrapper").get("filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
        assertEquals("stockElasticField", elasticQuery.get("aggs").get("stock_filter_wrapper").get("aggs").get("stock").get("terms").get("field").asText());



        assertEquals("waldschuh", elasticQuery.get("aggs").get("type_filter_wrapper").get("filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("aggs").get("type_filter_wrapper").get("filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
        assertEquals("typeElasticField", elasticQuery.get("aggs").get("type_filter_wrapper").get("aggs").get("type").get("terms").get("field").asText());

        assertEquals("waldschuh", elasticQuery.get("post_filter").get("bool").get("must").get(0).get("term").get("brandElasticField").asText());
        assertEquals("true", elasticQuery.get("post_filter").get("bool").get("must").get(1).get("term").get("stockElasticField").asText());
    }

    @DisplayName("Transform facet with multi select filters")
    @Test
    public void transformWaldlaufer() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet farbe = new Facet();
        farbe.setId("attr_farbe.keyword");
        farbe.setName("farbe");
        transformer.addAggregation(farbe);

        Facet futter = new Facet();
        futter.setId("attr_futter.keyword");
        futter.setName("futter");
        transformer.addAggregation(futter);

        Facet form = new Facet();
        form.setId("attr_form.keyword");
        form.setName("form");
        transformer.addAggregation(form);

        transformer.addFilterMapping("farbe", "attr_farbe.keyword");
        transformer.addFilterMapping("futter", "attr_futter.keyword");
        transformer.addFilterMapping("form", "attr_form.keyword");

        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.farbe=Schwarz", "f.futter=Leder");
    }

    @DisplayName("Transform facet with multi select filters and no filter is queried")
    @Test
    public void transformFacetFilterMultiselectNoFilterInQuery() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet brand = new Facet();
        brand.setId("brand");
        brand.setName("brand");
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stock");
        stock.setName("stock");
        transformer.addAggregation(stock);

        Facet type = new Facet();
        type.setId("type");
        type.setName("type");
        transformer.addAggregation(type);

        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("stock", "stockElasticField");
        transformer.addFilterMapping("type", "typeElasticField");

        ObjectNode elasticQuery = transform(transformer,  "q=*");

        assertEquals("brand", elasticQuery.get("aggs").get("brand").get("terms").get("field").asText());
        assertEquals("stock", elasticQuery.get("aggs").get("stock").get("terms").get("field").asText());
        assertEquals("type", elasticQuery.get("aggs").get("type").get("terms").get("field").asText());


    }

    @DisplayName("Transform date range filter")
    @Test
    public void transformDateHistogramFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setId("timestamp");
        facet.setName("searchQueries");
        facet.setType("date_histogram");
        transformer.addAggregation(facet);
        ObjectNode elasticQuery = transform(transformer,  "q=*");

        assertEquals("timestamp", elasticQuery.get("aggs").get("searchQueries").get("date_histogram").get("field").asText());
        assertEquals("hour", elasticQuery.get("aggs").get("searchQueries").get("date_histogram").get("calendar_interval").asText());
        assertEquals("Europe/Berlin", elasticQuery.get("aggs").get("searchQueries").get("date_histogram").get("time_zone").asText());
        assertEquals("0", elasticQuery.get("aggs").get("searchQueries").get("date_histogram").get("min_doc_count").asText());
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