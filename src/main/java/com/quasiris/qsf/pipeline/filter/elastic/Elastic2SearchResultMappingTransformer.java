package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public Elastic2SearchResultMappingTransformer() {


    }


    @Override
    public SearchResult transform(ElasticResult elasticResult) {
        SearchResult searchResult = new SearchResult();
        searchResult.initDocuments();
        searchResult.setTotal(elasticResult.getHits().getTotal());
        searchResult.setStatusMessage("OK");

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
            Facet facet = new Facet();
            String id = facetMapping.get(aggregationEntry.getKey());
            if(id == null) {
                id = aggregationEntry.getKey();
            }
            facet.setId(id);
            facet.setFilterName(filterPrefix + id);

            String name = facetNameMapping.get(id);

            if(name == null) {
                name = aggregationEntry.getKey();
            }
            facet.setName(name);

            facet.setCount(Long.valueOf(aggregationEntry.getValue().getBuckets().size()));

            Long facetReseultCount = 0L;
            for(Bucket bucket : aggregationEntry.getValue().getBuckets()) {
                FacetValue facetValue = new FacetValue(bucket.getKey(), bucket.getDoc_count());
                facetReseultCount = facetReseultCount + facetValue.getCount();
                facetValue.setFilter(filterPrefix + id + "=" + EncodingUtil.encode(bucket.getKey()));
                facet.getValues().add(facetValue);
            }
            facet.setResultCount(facetReseultCount);
            searchResult.addFacet(facet);

        }
    }

    @Override
    public Document transformHit(Hit hit) {
        ObjectNode objectNode = hit.get_source();

        Document document = new Document();
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map fields = mapper.readValue(objectNode.toString(), Map.class);
            document.setDocument(fields);
        } catch (IOException e) {
            e.printStackTrace();
        }

        transformHighlight(hit, document);
        return document;
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
}
