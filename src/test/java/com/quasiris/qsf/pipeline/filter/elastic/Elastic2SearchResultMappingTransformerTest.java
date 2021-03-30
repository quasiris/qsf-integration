package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Elastic2SearchResultMappingTransformerTest {

    @Test
    void transformInnerhitsAppendScores() throws Exception {
        Elastic2SearchResultMappingTransformer transformer = new Elastic2SearchResultMappingTransformer();
        ElasticResult elasticResult = readElasticResultFromFile("innerhits-append-scores.json");
        SearchResult searchResult = transformer.transform(elasticResult);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> snippets = (List<Map<String, Object>>) searchResult.getDocuments().get(0).getFieldValueAsObject("snippets");
        Map<String, Object> firstSnippet = snippets.get(0);
        assertEquals(3, firstSnippet.size());
        assertEquals("Features", firstSnippet.get("title"));
        assertEquals(0.0, firstSnippet.get("_score"));
        assertEquals(0, firstSnippet.get("_offset"));

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

    public ElasticResult readElasticResultFromFile(String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String file = "src/test/resources/com/quasiris/qsf/pipeline/filter/elastic/bean/" + fileName;
        ElasticResult elasticResult = objectMapper.readValue(new File(file), ElasticResult.class);
        return elasticResult;
    }
}