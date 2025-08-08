package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.config.QsfSearchConfigUtil;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.mapper.CategorySelectFacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.NavigationValueFacetFilterMapper;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Elastic2SearchResultMappingTransformerTest {

    @Test
    void testHistogramFacet() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFacetTypeMapping("price", "histogram");
        ElasticResult elasticResult = readElasticResultFromFile("histogram-facet.json");

        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "histogram-facet.json");
    }
    @Test
    void testCustomData() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("facet-with-custom-data.json");

        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "facet-with-custom-data.json");
    }
    @Test
    void testNextPageToken() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("next-page-token.json");

        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "next-page-token.json");
    }

    @Test
    void testSubFacet() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getFilter().setFilterPrefix("f.");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("sub-facet.json");
        String facetId = "supplierName";
        String facetName = "Hersteller";
        String subFacetName = "Kategorie";

        transformer.addFacetNameMapping(facetId, facetName);
        transformer.addFacetTypeMapping(facetId, "term");

        transformer.addFacetNameMapping(facetId + ".1", subFacetName);
        transformer.addFacetTypeMapping(facetId + ".1", "term");
        transformer.getFacetMapping().get(facetId + ".1").setId("category");

        SearchResult searchResult = transformer.transform(elasticResult);
        searchResult.setName("sub-facet");
        assertSearchResult(searchResult, "sub-facet.json");
    }
    @Test
    void testScriptFields() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("script-fields.json");

        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "script-fields.json");
    }

    @Test
    void testRangeFacet() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        String id = "stock";
        transformer.addFacetNameMapping(id, "stock");
        transformer.addFacetTypeMapping(id, "range");

        ElasticResult elasticResult = readElasticResultFromFile("range-facet.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertEquals(1,searchResult.getFacets().size());
        Facet stock = searchResult.getFacetById("stock");
        assertEquals(3, stock.getValues().size());
        assertEquals("range", stock.getType());

        assertEquals("Not In Stock", stock.getValues().get(0).getValue());
        assertEquals(2666, stock.getValues().get(0).getCount());
        assertEquals("stock=Not%20In%20Stock", stock.getValues().get(0).getFilter());


    }


    @Test
    void testNavigationIdFilter() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        String id = "categories";
        transformer.addFacetNameMapping(id, "categories");
        transformer.addFacetTypeMapping(id, "navigation");



        ElasticResult elasticResult = readElasticResultFromFile("navigation.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "navigation-id-filter.json");

    }

    @Test
    void testNavigationValueFilter() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        String id = "categories";
        transformer.addFacetNameMapping(id, "categories");
        transformer.addFacetTypeMapping(id, "navigation");
        transformer.addFacetFilterMapper(id, new NavigationValueFacetFilterMapper());

        ElasticResult elasticResult = readElasticResultFromFile("navigation.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertSearchResult(searchResult, "navigation-value-filter.json");
    }


    @Test
    void testCategorySelect() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        int level = 4;
        String fieldName = "myCategory";
        FacetKeyMapper facetKeyMapper = new CategorySelectFacetKeyMapper();
        for (int i = 0; i <= level; i++) {
            String id = fieldName + "Tree" + i;
            transformer.addFacetNameMapping(id, "myCategory");
            transformer.addFacetTypeMapping(id, "categorySelect");
            transformer.addFacetKeyMapper(id, facetKeyMapper);
        }



        ElasticResult elasticResult = readElasticResultFromFile("category-select.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertEquals(3,searchResult.getFacets().size());
        assertEquals("myCategoryTree0", searchResult.getFacets().get(0).getId());
        assertEquals("myCategoryTree1", searchResult.getFacets().get(1).getId());
        assertEquals("myCategoryTree2", searchResult.getFacets().get(2).getId());

        assertEquals("Kunden", searchResult.getFacets().get(0).getValues().get(0).getValue());
        assertEquals("myCategoryTree0=123456%7C-%7C2%7C-%7CKunden", searchResult.getFacets().get(0).getValues().get(0).getFilter());
        assertEquals(63, searchResult.getFacets().get(0).getValues().get(0).getCount());

        assertEquals("SMB", searchResult.getFacets().get(1).getValues().get(2).getValue());
        assertEquals("myCategoryTree1=123456%7C-%7C2%7C-%7CKunden%7C___%7C732606%7C-%7C12%7C-%7CSMB", searchResult.getFacets().get(1).getValues().get(2).getFilter());
        assertEquals(4, searchResult.getFacets().get(1).getValues().get(2).getCount());

        assertEquals("Kontakte", searchResult.getFacets().get(2).getValues().get(2).getValue());
        assertEquals("myCategoryTree2=123456%7C-%7C2%7C-%7CKunden%7C___%7C789012%7C-%7C14%7C-%7CBuchhaltung%7C___%7C937264%7C-%7C1%7C-%7CKontakte", searchResult.getFacets().get(2).getValues().get(2).getFilter());
        assertEquals(2, searchResult.getFacets().get(2).getValues().get(2).getCount());
    }

    @Test
    void testFieldGrouping() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();

        QsfSearchConfigUtil.addVariantMapping(qsfSearchConfigDTO, "variantObject", "variantObject");
        QsfSearchConfigUtil.addVariantMapping(qsfSearchConfigDTO, "id", "variantIds");
        QsfSearchConfigUtil.addVariantOptions(qsfSearchConfigDTO, "groupField");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("field-grouping.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        Document document = searchResult.getDocuments().get(0);
        assertEquals("237462:236444", document.getDocument().get("id"));

        List<Object> variantObjects = (List<Object>) document.getDocument().get("variantObject");
        assertEquals(4, variantObjects.size());
        Map<String, Object> variantObject = (Map<String, Object>) variantObjects.get(0);
        assertEquals("grün", variantObject.get("farbe"));
        assertEquals("Quasiris Phone XXL", document.getFieldValue("title"));
    }

    @Test
    void testFieldGroupingWithVariantSort() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        QsfSearchConfigUtil.addVariantOptions(qsfSearchConfigDTO, "replaceFirstVariant");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("field-grouping.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        Document document = searchResult.getDocuments().get(0);
        assertEquals("237462:237458", document.getDocument().get("id"));
    }

    @Test
    void testFieldMappingWithWildcardInFromAndTo() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("attr_farbe*", "farbe*");
        ElasticResult elasticResult = readElasticResultFromFile("attribute-field-mapping.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        Document document = searchResult.getDocuments().get(0);
        assertEquals(1, document.getDocument().size());
        assertEquals("schwarz", document.getFieldValue("farbe_txt"));
    }

    @Test
    void testFieldMappingWithWildcardInFrom() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("attr_farbe*", "farbe");
        ElasticResult elasticResult = readElasticResultFromFile("attribute-field-mapping.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        Document document = searchResult.getDocuments().get(0);
        assertEquals(1, document.getDocument().size());
        assertEquals("schwarz", document.getFieldValue("farbe"));
    }
    @Test
    void testScoreMapping() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setSearchQuery(new SearchQuery());
        pipelineContainer.getSearchQuery().setCtrl(new HashSet<>());
        transformer.init(pipelineContainer);
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("_score", "myScore");
        transformer.addFieldMapping("_matched_queries", "myQueries");
        ElasticResult elasticResult = readElasticResultFromFile("variant-count.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertEquals(69.09049, searchResult.getDocuments().get(0).getDocument().get("myScore"));
        assertEquals(List.of("edismax"), searchResult.getDocuments().get(0).getDocument().get("myQueries"));
    }


    @Test
    void testExplanationMapping() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setSearchQuery(new SearchQuery());
        pipelineContainer.getSearchQuery().setCtrl(new HashSet<>());
        pipelineContainer.getSearchQuery().getCtrl().add("trace");
        transformer.init(pipelineContainer);
        transformer.addFieldMapping("id", "id");
        ElasticResult elasticResult = readElasticResultFromFile("variant-count.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertNotNull(searchResult.getDocuments().get(0).getDocument().get("_explanation"));
        assertEquals(69.09049, searchResult.getDocuments().get(0).getDocument().get("_score"));
        assertEquals(List.of("edismax"), searchResult.getDocuments().get(0).getDocument().get("_matched_queries"));
    }

    @Test
    void testVariantCountHotfix() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getVariant().setVariantId("variantId");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setSearchQuery(new SearchQuery());
        transformer.init(pipelineContainer);
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("variant-count.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(1, searchResult.getTotal());
    }

    @Test
    void transformStats() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFacetTypeMapping("price", "slider");
        ElasticResult elasticResult = readElasticResultFromFile("stats.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        Facet price = searchResult.getFacetById("price");
        assertEquals("slider", price.getType());
        assertEquals(526, price.getCount());
        assertEquals(15.0, price.getMinRange());
        assertEquals(170.0, price.getMaxRange());
    }

    @Test
    void transformNestedAggregations() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("nested-aggregations.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        Facet supplierName = searchResult.getFacetById("supplierName");
        assertEquals("Technics", supplierName.getValues().get(0).getValue());
        assertEquals(183, supplierName.getValues().get(0).getCount());
        assertEquals("Panasonic", supplierName.getValues().get(1).getValue());
        assertEquals(2, supplierName.getValues().get(1).getCount());
        assertEquals("Sony", supplierName.getValues().get(2).getValue());
        assertEquals(1, supplierName.getValues().get(2).getCount());

    }

    @Test
    void transformInnerhitsAppendScores() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("snippets", "snippets");

        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getDisplay().getScoreMapping().put("snippets", "snippets");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("innerhits-append-scores.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> snippets = (List<Map<String, Object>>) searchResult.getDocuments().get(0).getFieldValueAsObject("snippets");
        Map<String, Object> firstSnippet = snippets.get(0);
        assertEquals(4, snippets.size());
        assertEquals(4, firstSnippet.size());
        assertEquals("Features", firstSnippet.get("title"));
        assertEquals(0.0, firstSnippet.get("_score"));
        assertEquals(0, firstSnippet.get("_offset"));

    }

    @Test
    void transformMultipleInnerhits() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getDisplay().getScoreMapping().put("lineItems_positions", "lineItems");
        qsfSearchConfigDTO.getDisplay().getScoreMapping().put("lineItems_order", "lineItems");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("multiple-innerhits.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) searchResult.getDocuments().get(0).getFieldValueAsObject("lineItems");
        Map<String, Object> firstLineItem = lineItems.get(0);
        assertEquals(10, lineItems.size());
        assertEquals(6, firstLineItem.size());
        assertEquals("12345678", firstLineItem.get("sku"));
        assertEquals(0.0, firstLineItem.get("_score"));
        assertEquals(0, firstLineItem.get("_offset"));
        assertEquals(true, firstLineItem.get("_found"));



        assertEquals(true, lineItems.get(0).get("_found"));
        assertEquals(true, lineItems.get(1).get("_found"));
        assertEquals(true, lineItems.get(2).get("_found"));
        assertEquals(false, lineItems.get(3).get("_found"));
        assertEquals(false, lineItems.get(4).get("_found"));
        assertEquals(false, lineItems.get(5).get("_found"));
        assertEquals(true, lineItems.get(6).get("_found"));
        assertEquals(false, lineItems.get(7).get("_found"));
        assertEquals(false, lineItems.get(8).get("_found"));
        assertEquals(false, lineItems.get(9).get("_found"));

    }

    @Test
    void transformVariantsTotalCount() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("aggregation-variant-count.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        Facet material = searchResult.getFacetById("attr_material_txt");
        assertEquals("Leder", material.getValues().get(0).getValue());
        assertEquals(1, material.getValues().get(0).getCount());
        Facet farbe = searchResult.getFacetById("attr_farbe_txt");
        assertEquals("Blau", farbe.getValues().get(0).getValue());
        assertEquals(1, farbe.getValues().get(0).getCount());
        assertEquals("Rot", farbe.getValues().get(1).getValue());
        assertEquals(1, farbe.getValues().get(1).getCount());

    }

    @Test
    void transformInnerhitsCollapse() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");

        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getVariant().setVariantId("variantId");
        qsfSearchConfigDTO.getVariant().setVariantResultField("product_type_grouped");
        QsfSearchConfigUtil.addVariantOptions(qsfSearchConfigDTO, "groupDocument");

        transformer.setSearchConfig(qsfSearchConfigDTO);

        ElasticResult elasticResult = readElasticResultFromFile("innerhits-collapse.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        List<Document> productTypeGrouped = searchResult.getDocuments().get(0).getChildDocument("product_type_grouped");
        assertEquals(5, productTypeGrouped.size());
        assertEquals("iphone Handy Hülle blau title-id-doc-02", productTypeGrouped.get(1).getFieldValue("title"));

    }

    @Test
    void transformFilteredAggregation() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(3, searchResult.getFacets().size());
        assertEquals(10, searchResult.getFacetById("farbe").getValues().size());
        assertEquals("farbe", searchResult.getFacetById("farbe").getId());
        assertEquals("farbe", searchResult.getFacetById("farbe").getName());
        assertEquals("Schwarz", searchResult.getFacetById("farbe").getValues().get(0).getValue());

    }

    @Test
    void updateTotalDocumentsWhenUseVariantIdAndHasAggregationDocCount() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getVariant().setVariantId("variantId");
        transformer.setSearchConfig(qsfSearchConfigDTO);
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg-doc_count.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(8, searchResult.getTotal());
    }

    @Test
    void updateTotalDocumentsWhenUseVariantIdAndNotHasAggregationDocCount() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getVariant().setVariantId("variantId");
        transformer.setSearchConfig(qsfSearchConfigDTO);
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(8, searchResult.getTotal());
    }

    @Test
    void updateTotalDocumentsWhenNotUseVariantIdAndHasAggregationDocCount() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg-doc_count.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(25, searchResult.getTotal());
    }

    @Test
    void updateTotalDocumentsWhenUseVariantIdAndHasAggregationDocCountHigherThanTotal() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        QsfSearchConfigDTO qsfSearchConfigDTO = QsfSearchConfigUtil.initSearchConfig();
        qsfSearchConfigDTO.getVariant().setVariantId("variantId");
        transformer.setSearchConfig(qsfSearchConfigDTO);

        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg-doc_count-higher.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(8, searchResult.getTotal());
    }

    public ElasticResult readElasticResultFromFile(String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String file = "src/test/resources/com/quasiris/qsf/pipeline/filter/elastic/bean/" + fileName;
        ElasticResult elasticResult = objectMapper.readValue(new File(file), ElasticResult.class);
        return elasticResult;
    }

    private void assertSearchResult(SearchResult searchResult, String file) throws IOException {
        JsonAssert.assertJsonFile("classpath://com/quasiris/qsf/pipeline/filter/elastic/searchresult/" + file, searchResult);
    }
}