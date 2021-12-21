package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Elastic2SearchResultMappingTransformerTest {

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