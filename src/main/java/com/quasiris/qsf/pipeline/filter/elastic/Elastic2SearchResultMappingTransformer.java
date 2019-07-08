package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregation;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Bucket;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.response.*;
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


    private Map<String, String> facetMapping = new HashMap<>();
    private Map<String, String> facetNameMapping = new HashMap<>();

    private Map<String, String> sliderMapping = new HashMap<>();
    private Map<String, String> sliderNameMapping = new HashMap<>();


    private String filterPrefix = "";

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


        // for backward compatibility
        // if no mapping is defined for a aggregation a default facet mapping is configured
        for(Map.Entry<String, Aggregation> aggregation : elasticResult.getAggregations().entrySet()) {
            if(!facetMapping.containsKey(aggregation.getKey()) &&
                    !sliderMapping.containsKey(aggregation.getKey())) {
                facetMapping.put(aggregation.getKey(), aggregation.getKey());
            }

        }

        for(Map.Entry<String, String> mapping : facetMapping.entrySet()) {
            Aggregation aggregation = elasticResult.getAggregations().get(mapping.getKey());
            if(aggregation == null) {
                continue;
            }
            Facet facet = mapAggregation2Facet(mapping.getValue(), aggregation);
            searchResult.addFacet(facet);
        }

        for(Map.Entry<String, String> mapping : sliderMapping.entrySet()) {
            Aggregation aggregation = elasticResult.getAggregations().get(mapping.getKey());
            if(aggregation == null) {
                continue;
            }
            Slider slider = mapAggregation2Slider(mapping.getValue(), aggregation);
            searchResult.addSlider(slider);
        }
    }

    protected Slider mapAggregation2Slider(String sliderId, Aggregation aggregation) {
        Slider slider = new Slider();

        slider.setId(sliderId);
        slider.setFilterName(filterPrefix + sliderId);

        String name = sliderNameMapping.get(sliderId);

        if(name == null) {
            name = sliderId;
        }
        slider.setName(name);
        slider.setCount(Long.valueOf(aggregation.getCount()));
        slider.setMinRange(aggregation.getMin());
        slider.setMaxRange(aggregation.getMax());
        return slider;

    }

    protected Facet mapAggregation2Facet(String facetId, Aggregation aggregation) {
        Facet facet = new Facet();

        facet.setId(facetId);
        facet.setFilterName(filterPrefix + facetId);

        String name = facetNameMapping.get(facetId);

        if(name == null) {
            name = facetId;
        }
        facet.setName(name);
        facet.setCount(Long.valueOf(aggregation.getBuckets().size()));

        Long facetReseultCount = 0L;
        for(Bucket bucket : aggregation.getBuckets()) {
            FacetValue facetValue = new FacetValue(bucket.getKey(), bucket.getDoc_count());
            facetReseultCount = facetReseultCount + facetValue.getCount();
            facetValue.setFilter(filterPrefix + facet.getId() + "=" + EncodingUtil.encode(bucket.getKey()));

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

    public void addSliderMapping(String from, String to) {
        this.sliderMapping.put(from, to);
    }

}
