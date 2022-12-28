package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.mapper.CategorySelectFacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Elastic2SearchResultMappingTransformerTest {



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
        assertEquals("stock=Not+In+Stock", stock.getValues().get(0).getFilter());


    }

    @Test
    void testNavigation() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        String id = "categories";
        transformer.addFacetNameMapping(id, "categories");
        transformer.addFacetTypeMapping(id, "navigation");



        ElasticResult elasticResult = readElasticResultFromFile("navigation.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        assertEquals(1,searchResult.getFacets().size());
        Facet categories = searchResult.getFacetById("categories");
        assertEquals(3,categories.getValues().size());



        assertEquals("Industrietechnik", categories.getValues().get(0).getValue());
        assertEquals(18384, categories.getValues().get(0).getCount());
        // TODO be careful, the category import rely on this id - if the filter is changed, this must be considered
        assertEquals("2", categories.getValues().get(0).getFilter());


        FacetValue child = categories.getValues().get(0).getChildren().getValues().get(1);
        assertEquals("Automatisierung und Antriebstechnik", child.getValue());
        assertEquals(1511, child.getCount());
        assertEquals("211", child.getFilter());
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
        transformer.addInnerhitsGroupMapping("variantObject", "variantObject");
        ElasticResult elasticResult = readElasticResultFromFile("field-grouping.json");
        SearchResult searchResult = transformer.transform(elasticResult);
        Document document = searchResult.getDocuments().get(0);
        List<Object> variantObjects = (List<Object>) document.getDocument().get("variantObject");
        assertEquals(4, variantObjects.size());
        Map<String, Object> variantObject = (Map<String, Object>) variantObjects.get(0);
        assertEquals("grün", variantObject.get("farbe"));
        assertEquals("Quasiris Phone XXL", document.getFieldValue("title"));
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
    void testVariantCountHotfix() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        transformer.setVariantId("variantId");
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
        ElasticResult elasticResult = readElasticResultFromFile("innerhits-append-scores.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> snippets = (List<Map<String, Object>>) searchResult.getDocuments().get(0).getFieldValueAsObject("snippets");
        Map<String, Object> firstSnippet = snippets.get(0);
        assertEquals(4, snippets.size());
        assertEquals(3, firstSnippet.size());
        assertEquals("Features", firstSnippet.get("title"));
        assertEquals(0.0, firstSnippet.get("_score"));
        assertEquals(0, firstSnippet.get("_offset"));

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
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        transformer.setVariantId("variantId");
        ElasticResult elasticResult = readElasticResultFromFile("filtered-agg-doc_count.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        assertEquals(8, searchResult.getTotal());
    }

    @Test
    void updateTotalDocumentsWhenUseVariantIdAndNotHasAggregationDocCount() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        transformer.setVariantId("variantId");
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
        transformer.addFieldMapping("id", "id");
        transformer.addFieldMapping("title", "title");
        transformer.addFieldMapping("description", "description");
        transformer.addFieldMapping("product_type_grouped", "product_type_grouped");
        transformer.setVariantId("variantId");
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
}