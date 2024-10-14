package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.mapping.ParameterMapper;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTestUtil;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.test.converter.NullValueConverter;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformerTest {


    @DisplayName("Transform sub facet with variant id and filter")
    @Test
    public void testSubFacetWithVariantIdAndFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = createSubFacetTransformer();
        transformer.setVariantId("myVariantId");
        ObjectNode elasticQuery = transform(transformer,
                "q=*","f.sku=1234"
        );
        assertQuery(elasticQuery, "sub-facet-with-variant-id-and-filter.json");

    }
    @DisplayName("Transform sub facet with variant id")
    @Test
    public void testSubFacetWithVariantId() throws Exception {
        ElasticQsfqlQueryTransformer transformer = createSubFacetTransformer();
        transformer.setVariantId("myVariantId");
        ObjectNode elasticQuery = transform(transformer,
                "q=*"
        );
        assertQuery(elasticQuery, "sub-facet-with-variant-id.json");

    }
    @DisplayName("Transform sub facet")
    @Test
    public void testSubFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = createSubFacetTransformer();
        ObjectNode elasticQuery = transform(transformer,
                "q=*"
        );
        assertQuery(elasticQuery, "sub-facet.json");

    }

    private ElasticQsfqlQueryTransformer createSubFacetTransformer() {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.addFilterRule("(.+)", "$1.keyword");
        Facet facet = new Facet();
        facet.setName("supplierName");
        facet.setId("supplierName");
        facet.setFieldName("supplierName.keyword");
        facet.setOperator(FilterOperator.OR);


        Facet subFacet = new Facet();
        subFacet.setName("category");
        subFacet.setId("category");
        subFacet.setFieldName("category.keyword");
        subFacet.setOperator(FilterOperator.OR);

        facet.setChildren(subFacet);

        transformer.setAggregations(Arrays.asList(facet));
        return transformer;
    }


    @DisplayName("Transform defined range facet")
    @Test
    public void testDefinedRangeFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.addFilterRule("(.+)", "$1.keyword");

        RangeFacet facet = new RangeFacet();
        facet.setName("created_at");
        facet.setId("created_at_attachments");
        facet.setFieldName("created_at");
        facet.setType("range");
        //facet.setOperator(FilterOperator.OR);


        Range lastWeek = new Range("last week", "now-1w/w", "now/w");
        Range lastMonth = new Range("last month", "now-1M/M", "now/M");

        facet.setRanges(Arrays.asList(lastWeek, lastMonth));

        transformer.setAggregations(Arrays.asList(facet));
        ObjectNode elasticQuery = transform(transformer,
                "q=*"
        );
        assertQuery(elasticQuery, "defined-range-facet.json");

    }

    @DisplayName("Transform range facet")
    @Test
    public void testRangeFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.addFilterRule("(.+)", "$1.keyword");

        RangeFacet facet = new RangeFacet();
        facet.setName("stock");
        facet.setId("stock");
        facet.setOperator(FilterOperator.OR);

        Range rangeNotInStock = new Range("Not In Stock", null, 1L);
        Range rangeCriticalStock = new Range("Critical Stock", 1L, 5L);
        Range rangeInStock = new Range("In Stock", 5L, null);

        facet.setRanges(Arrays.asList(rangeNotInStock, rangeCriticalStock, rangeInStock));

        transformer.setAggregations(Arrays.asList(facet));
        ObjectNode elasticQuery = transform(transformer,
                "q=*"
        );
        assertQuery(elasticQuery, "range-facet.json");

    }
    @DisplayName("Transform categorySelect")
    @Test
    public void transformFacetCategorySElect() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.addFilterRule("(.+)", "$1.keyword");

        Facet facet = new Facet();
        facet.setType("categorySelect");
        facet.setName("gfmCategoryTree");
        facet.setId("gfmCategoryTree");
        facet.setOperator(FilterOperator.OR);


        //SearchFilter gfmCategory0 = SearchFilterBuilder.create().withId("gfmCategory0").value("237030|-|2|-|Änderung").build();

        transformer.setAggregations(Arrays.asList(facet));
        ObjectNode elasticQuery = transform(transformer,
                "q=*",
                //"f.gfmCategoryTree0=237030|-|2|-|Änderung"
                //"f.gfmCategoryTree1=237030|-|2|-|Änderung|___|237090|-|14|-|Rechnung"
                "f.gfmCategoryTree2=237030|-|2|-|Änderung|___|237090|-|14|-|Rechnung|___|237124|-|1|-|RechnungOnline"
        );
        //assertQuery(elasticQuery, "slider.json");

    }

    @DisplayName("Transform slider")
    @Test
    public void transformSlider() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setType("slider");
        facet.setName("price");
        facet.setId("price");

        transformer.setAggregations(Arrays.asList(facet));
        ObjectNode elasticQuery = transform(transformer,  "q=*");
        assertQuery(elasticQuery, "slider.json");

    }

    @DisplayName("Transform slider multiselect with no filter")
    @Test
    public void transformSliderMultiSelectNoFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setType("slider");
        facet.setName("price");
        facet.setId("price");

        transformer.setAggregations(Arrays.asList(facet));
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "q=*");
        assertQuery(elasticQuery, "slider-multiselect-no-filter.json");

    }

    @DisplayName("Transform slider multiselect with filter")
    @Test
    public void transformSliderMultiSelectWithFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setType("slider");
        facet.setName("price");
        facet.setId("price");

        transformer.setAggregations(Arrays.asList(facet));
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.color=red");
        assertQuery(elasticQuery, "slider-multiselect-with-filter.json");

    }

    @DisplayName("Transform slider multiselect with range filter")
    @Test
    public void transformSliderMultiSelectWithRangeFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setType("slider");
        facet.setName("price");
        facet.setId("price");
        facet.setOperator(FilterOperator.OR);

        transformer.setAggregations(Arrays.asList(facet));
        transformer.setMultiSelectFilter(true);
        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.color=red", "f.price.range=49,170");
        assertQuery(elasticQuery, "slider-multiselect-with-range-filter.json");
    }

    @DisplayName("Transform sort mapping")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            //"location-with-filters-variable.json, null, filters"
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
        assertQuery(elasticQuery, "sort-mapping-query.json");
    }

    @Test
    public void transformSortMappingWithParameter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        String sort = "[\n" +
                "    {\n" +
                "      \"_script\" : {\n" +
                "        \"script\" : {\n" +
                "          \"source\" : \"return doc['customerIdsSort'].stream().filter(x -> x.startsWith('{customerId=' + params.accountId)).findFirst().orElse('a');\",\n" +
                "          \"params\" : {\n" +
                "            \"accountId\" : \"$query.f.customerId\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"type\" : \"string\",\n" +
                "        \"order\" : \"desc\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n";
        transformer.addSortMapping("lastPurchased", sort);
        ObjectNode elasticQuery = transform(transformer,  "sort=lastPurchased", "f.customerId=4711");
        assertQuery(elasticQuery, "sort-mapping-with-variable.json");
    }


    @DisplayName("Transform sort field")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            //"location-with-filters-variable.json, null, filters"
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

        assertQuery(elasticQuery, "sort-query.json");

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
        assertQuery(elasticQuery, "filter-query.json");
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
        assertQuery(elasticQuery, "filter-multiselect-query.json");
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
        assertQuery(elasticQuery, "filter-or-query.json");
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
        assertQuery(elasticQuery, "filter-or-multiselect-query.json");

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
        assertQuery(elasticQuery, "filter-or-multiple-query.json");
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
        assertQuery(elasticQuery, "filter-or-multiple-multiselect-query.json");
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
        assertQuery(elasticQuery, "filter-not-query.json");
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
        assertQuery(elasticQuery, "filter-not-multiselected-query.json");
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
        assertQuery(elasticQuery, "filter-rule-query.json");
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
        assertQuery(elasticQuery, "range-filter-query.json");
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
        assertQuery(elasticQuery, "range-filter-upper-lower-excluded-query.json");
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
        assertQuery(elasticQuery, "range-filter-upper-lower-included-query.json");
    }


    @DisplayName("Transform date range filter")
    @Test
    public void transformDateRangeFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.addFilterMapping("timestamp", "timestamp");
        ObjectNode elasticQuery = transform(transformer,  "f.timestamp.daterange=2021-01-02T23:00:00Z,2021-02-05T20:59:38Z");
        assertQuery(elasticQuery, "date-range-filter-query.json");
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
        assertQuery(elasticQuery, "facet-query.json");

    }
    @DisplayName("Transform facet with filter")
    @Test
    public void transformFacetWithFilter() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet accountId = new Facet();
        accountId.setId("accountId");
        accountId.setName("accountId");
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("accountId").value("1234").build();

        accountId.getFacetFilters().add(searchFilter);

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");
        searchQuery.setFacetList(new ArrayList<>());
        searchQuery.getFacetList().add(accountId);


        Facet stock = new Facet();
        stock.setId("stock");
        stock.setName("stock");
        searchQuery.getFacetList().add(stock);


        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "facet-with-filter.json");
    }

    @DisplayName("Transform facet with filter and variantId")
    @Test
    public void transformFacetWithFilterAndVariantId() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");

        Facet accountId = new Facet();
        accountId.setId("accountId");
        accountId.setName("accountId");
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("accountId").value("1234").build();

        accountId.getFacetFilters().add(searchFilter);

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");
        searchQuery.setFacetList(new ArrayList<>());
        searchQuery.getFacetList().add(accountId);


        Facet stock = new Facet();
        stock.setId("stock");
        stock.setName("stock");
        searchQuery.getFacetList().add(stock);


        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "facet-with-filter-and-variants.json");
    }
    @DisplayName("Transform facet with filter and variantId")
    @Test
    public void transformVariantId() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "variant-id.json");
    }

    @DisplayName("Transform with variantId and source fields")
    @Test
    public void transformVariantIdAndSource() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");
        Set<String> innerHitsSourceFields = new HashSet<>();
        innerHitsSourceFields.add("productDetails");
        transformer.setInnerHitsSourceFields(innerHitsSourceFields);

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "variant-id-source.json");
    }

    @DisplayName("Transform with variantId and source fields and size")
    @Test
    public void transformVariantIdAndSourceAndSize() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");
        Set<String> innerHitsSourceFields = new HashSet<>();
        innerHitsSourceFields.add("productDetails");
        transformer.setInnerHitsSourceFields(innerHitsSourceFields);
        transformer.setVariantSize(12);

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "variant-id-source-size.json");
    }

    @DisplayName("Transform with variantId and source fields and sort")
    @Test
    public void transformVariantIdAndSourceAndSort() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");
        Set<String> innerHitsSourceFields = new HashSet<>();
        innerHitsSourceFields.add("productDetails");
        transformer.setInnerHitsSourceFields(innerHitsSourceFields);
        transformer.setVariantSort("[ { \"productPosition\": \"desc\" } ]");

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");

        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "variant-id-source-sort.json");
    }

    @DisplayName("Transform facet with filter and variantId and source fields")
    @Test
    public void transformFacetWithFilterAndVariantIdAndSourceFields() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);
        transformer.setVariantId("variantId");
        transformer.setInnerHitsSourceFields(new HashSet<>(Arrays.asList("variantObject")));

        Facet accountId = new Facet();
        accountId.setId("accountId");
        accountId.setFieldName("accountId");
        accountId.setName("accountId");
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("accountId").value("1234").build();

        accountId.getFacetFilters().add(searchFilter);

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ("*");
        searchQuery.setFacetList(new ArrayList<>());
        searchQuery.getFacetList().add(accountId);


        Facet stock = new Facet();
        stock.setId("stock");
        stock.setName("stock");
        searchQuery.getFacetList().add(stock);


        ObjectNode elasticQuery = transform(transformer,  searchQuery);
        assertQuery(elasticQuery, "facet-with-filter-and-variants-and-source-fields.json");
    }

    @DisplayName("Transform facet with multi select filters with or operator")
    @Test
    public void transformFacetFilterMultiselectOperatorOr() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet brand = new Facet();
        brand.setId("brand");
        brand.setFieldName("brandElasticField");
        brand.setName("brand");
        brand.setOperator(FilterOperator.OR);
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stock");
        stock.setFieldName("stockElasticField");
        stock.setName("stock");
        stock.setOperator(FilterOperator.OR);
        transformer.addAggregation(stock);

        Facet type = new Facet();
        type.setId("type");
        type.setFieldName("typeElasticField");
        type.setName("type");
        type.setOperator(FilterOperator.OR);
        transformer.addAggregation(type);

        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("stock", "stockElasticField");
        transformer.addFilterMapping("type", "typeElasticField");

        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.brand=waldschuh", "f.stock=true");
        assertQuery(elasticQuery, "facet-filter-multiselect-operator-or.json");

    }

   @DisplayName("Transform facet with multi select filters with AND operator")
    @Test
    public void transformFacetFilterMultiselectOperatorAnd() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());
        transformer.setMultiSelectFilter(true);

        Facet brand = new Facet();
        brand.setId("brand");
        brand.setFieldName("brandElasticField");
        brand.setName("brand");
        brand.setOperator(FilterOperator.AND);
        transformer.addAggregation(brand);

        Facet stock = new Facet();
        stock.setId("stock");
        stock.setFieldName("stockElasticField");
        stock.setName("stock");
        stock.setOperator(FilterOperator.AND);
        transformer.addAggregation(stock);

        Facet type = new Facet();
        type.setId("type");
        type.setFieldName("typeElasticField");
        type.setName("type");
        type.setOperator(FilterOperator.AND);
        transformer.addAggregation(type);

        transformer.addFilterMapping("brand", "brandElasticField");
        transformer.addFilterMapping("stock", "stockElasticField");
        transformer.addFilterMapping("type", "typeElasticField");

        ObjectNode elasticQuery = transform(transformer,  "q=*", "f.brand=waldschuh", "f.stock=true");
        assertQuery(elasticQuery, "facet-filter-multiselect-operator-and.json");

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
        assertQuery(elasticQuery, "waldlaeufer.json");
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

        assertQuery(elasticQuery, "facet-filter-multiselect-no-filter-in-query.json");

    }

    @DisplayName("Transform histogram facet")
    @Test
    public void transformHistogramFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setId("price");
        facet.setName("price");
        facet.setType("histogram");
        transformer.addAggregation(facet);
        ObjectNode elasticQuery = transform(transformer,  "q=*");
        assertQuery(elasticQuery, "histogram-facet.json");
    }
    @DisplayName("Transform date histogram facet")
    @Test
    public void transformDateHistogramFacet() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.matchAll());

        Facet facet = new Facet();
        facet.setId("searchQueries");
        facet.setFieldName("timestamp");
        facet.setName("searchQueries");
        facet.setType("date_histogram");
        transformer.addAggregation(facet);
        ObjectNode elasticQuery = transform(transformer,  "q=*");
        assertQuery(elasticQuery, "date-histogram-facet.json");
    }

    @DisplayName("Transform paging")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            // "location-with-filters-variable.json, null, filters"
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
        assertQuery(elasticQuery, "paging-query.json");
    }

    @DisplayName("Transform paging with default values")
    @ParameterizedTest(name = "{index} => profile=''{0}'' filterPath=''{1}'' filterVariable=''{2}''")
    @CsvSource({
            "location.json, null, null",
            "location.json, query/bool/filter, null",
            //"location-with-filters-variable.json, null, filters"
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
        assertQuery(elasticQuery, "paging-default-query.json");
    }

    @Test
    public void transformPagingWithRows() throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile("classpath://com/quasiris/qsf/elastic/profiles/location.json");

        ObjectNode elasticQuery = transform(transformer,  "q=foo", "rows=5", "page=5");
        assertQuery(elasticQuery, "paging-with-rows-query.json");

    }


    protected ObjectNode transform(ElasticQsfqlQueryTransformer transformer, SearchQuery searchQuery) throws PipelineContainerException {
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);

        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        return elasticQuery;
    }

    private ObjectNode transform(ElasticQsfqlQueryTransformer transformer, String... parameters) throws Exception {
        SearchQuery searchQuery = QsfqlParserTestUtil.createQuery(parameters);
        return transform(transformer, searchQuery);
    }

    private void assertQuery(ObjectNode elasticQuery, String file) throws IOException {
        JsonAssert.assertJsonFile("classpath://com/quasiris/qsf/pipeline/filter/elastic/query/" + file, elasticQuery);
    }

}