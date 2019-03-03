package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregation;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Bucket;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.FacetValue;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.EncodingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 04.11.17.
 */
public class Elastic2SearchResultMappingTransformer implements SearchResultTransformerIF {

    private Map<String, List<String>> fieldMapping = new HashMap<>();
    private Map<String, String> resultFields = new HashMap<>();
    private Map<String, String> facetMapping = new HashMap<>();
    private Map<String, String> facetNameMapping = new HashMap<>();
    private String filterPrefix = "";

    private boolean mapEmptyValues = true;

    public Elastic2SearchResultMappingTransformer() {


    }


    @Override
    public SearchResult transform(ElasticResult elasticResult) {
        SearchResult searchResult = new SearchResult();
        searchResult.initDocuments();
        searchResult.setTotal(elasticResult.getHits().getTotal());
        searchResult.setStatusMessage("OK");
        searchResult.setStatusCode(200);
        searchResult.setTime(elasticResult.getTook());

        for(Hit hit :elasticResult.getHits().getHits()) {
            Document document = transformHit(hit);
            searchResult.addDocument(document);
        }

        mapFacets(elasticResult, searchResult);


        return searchResult;

    }

    protected void mapFacets(ElasticResult elasticResult, SearchResult searchResult) {
        if(elasticResult.getAggregations() == null) {
            return;
        }
        searchResult.setFacetCount(elasticResult.getAggregations().size());
        for(Map.Entry<String, Aggregation> aggregationEntry : elasticResult.getAggregations().entrySet()) {
            Facet facet = mapAggregation2Facet(aggregationEntry.getKey(), aggregationEntry.getValue());
            searchResult.addFacet(facet);

        }
    }

    protected Facet mapAggregation2Facet(String key, Aggregation aggregation) {
        Facet facet = new Facet();
        String id = facetMapping.get(key);
        if(id == null) {
            id = key;
        }
        facet.setId(id);
        facet.setFilterName(filterPrefix + id);

        String name = facetNameMapping.get(id);

        if(name == null) {
            name = key;
        }
        facet.setName(name);

        facet.setCount(Long.valueOf(aggregation.getBuckets().size()));

        Long facetReseultCount = 0L;
        for(Bucket bucket : aggregation.getBuckets()) {
            FacetValue facetValue = new FacetValue(bucket.getKey(), bucket.getDoc_count());
            facetReseultCount = facetReseultCount + facetValue.getCount();
            facetValue.setFilter(filterPrefix + id + "=" + EncodingUtil.encode(bucket.getKey()));

            if(bucket.getSubFacet() != null) {
                Facet subFacet = mapAggregation2Facet("subFacet", bucket.getSubFacet());
                facetValue.setSubFacet(subFacet);
            }

            facet.getValues().add(facetValue);
        }
        facet.setResultCount(facetReseultCount);
        return facet;
    }

    @Override
    public Document transformHit(Hit hit) {
        String id = hit.get_id();
        ObjectNode objectNode = hit.get_source();

        Document document = new Document(id);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map fields = mapper.readValue(objectNode.toString(), Map.class);
            if(fieldMapping.size() == 0) {
                document.setDocument(fields);
            } else {
                for(Map.Entry<String, List<String>> mapping : fieldMapping.entrySet()) {
                    String key = mapping.getKey();
                    Object mappedValue = fields.get(key);
                    if(mappedValue != null) {
                        for(String mappedKey: mapping.getValue()) {
                            mapValue(document, mappedKey, mappedValue);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        transformHighlight(hit, document);
        return document;
    }

    public void mapValue(Document document, String key, Object value) {
        document.getDocument().put(key, value);
    }


    public void transformHighlight(Hit hit, Document document) {
        if(hit.getHighlight() == null) {
            return;
        }

        for(Map.Entry<String, List<String>> entry : hit.getHighlight().entrySet()) {
            document.getDocument().put("highlight." + entry.getKey(), entry.getValue());
        }
    }

    @Override
    public StringBuilder print(String indent) {
        return new StringBuilder("TODO");
    }

    public void filterPrefix(String filterPrefix) {
        this.filterPrefix=filterPrefix;
    }

    public void addFieldMapping(String from, String to) {
        List<String> mapping = fieldMapping.get(from);
        if(mapping == null) {
            mapping = new ArrayList<>();
        }
        mapping.add(to);

        fieldMapping.put(from, mapping);
    }
}
