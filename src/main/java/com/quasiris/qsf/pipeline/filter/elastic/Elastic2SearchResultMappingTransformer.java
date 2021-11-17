package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.filter.elastic.bean.*;
import com.quasiris.qsf.dto.response.*;
import com.quasiris.qsf.pipeline.filter.mapper.DefaultFacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import com.quasiris.qsf.commons.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by mki on 04.11.17.
 */
public class Elastic2SearchResultMappingTransformer implements SearchResultTransformerIF {

    private Map<String, List<String>> fieldMapping = new LinkedHashMap<>();


    private Map<String, FacetMapping> facetMapping = new LinkedHashMap<>();

    private String filterPrefix = "";
    private String variantId;

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
        updateTotalDocuments(elasticResult, searchResult);

        return searchResult;

    }

    protected void updateTotalDocuments(ElasticResult elasticResult, SearchResult searchResult) {
        if(elasticResult.getAggregations() != null && elasticResult.getAggregations().getDoc_count() != null && StringUtils.isNotEmpty(getVariantId())) {
            // TODO totalVariants = total;
            searchResult.setTotal(elasticResult.getAggregations().getDoc_count());
        }
    }

    protected void mapFacets(ElasticResult elasticResult, SearchResult searchResult) {
        if(elasticResult.getAggregations() == null || elasticResult.getAggregations().getAggregations() == null) {
            return;
        }

        // for backward compatibility
        // if no mapping is defined for a aggregation a default facet mapping is configured
        for(Map.Entry<String, Aggregation> aggregation : elasticResult.getAggregations().getAggregations().entrySet()) {
            if(!facetMapping.containsKey(aggregation.getKey())) {
                FacetMapping mapping = new FacetMapping();
                mapping.setId(aggregation.getKey());
                mapping.setName(aggregation.getKey());
                facetMapping.put(aggregation.getKey(), mapping);
            }
        }

        for(Map.Entry<String, FacetMapping> mapping : facetMapping.entrySet()) {
            FacetMapping facetMapping = mapping.getValue();
            Aggregation aggregation = elasticResult.getAggregations().getAggregations().get(mapping.getKey());
            if(aggregation == null) {
                continue;
            }
            Facet facet = mapAggregation(facetMapping.getId(), aggregation, "", "");
            searchResult.addFacet(facet);
        }
    }

    protected Facet mapAggregation(String facetId, Aggregation aggregation, String filterType, String filterValuePrefix) {
        FacetMapping mapping = facetMapping.get(facetId);

        if ("slider".equals(mapping.getType())) {
            return mapAggregationToSlider(facetId, aggregation, mapping);
        } else if ("navigation".equals(mapping.getType())) {
            mapAggregationToNavigation(facetId, aggregation, mapping);
        }


        return mapAggregationToFacet(facetId, aggregation, filterType, filterValuePrefix);
    }


    protected Facet mapAggregationToFacet(String facetId, Aggregation aggregation, String filterType, String filterValuePrefix) {
        Facet facet = new Facet();
        facet.setValues(new ArrayList<>());
        FacetMapping mapping = facetMapping.get(facetId);
        facet.setType(mapping.getType());
        String name = mapping.getName();

        if(name == null) {
            name = facetId;
        }
        facet.setId(facetId);
        facet.setName(name);
        facet.setCount((long) aggregation.getBuckets().size());

        Long facetReseultCount = 0L;
        FacetKeyMapper facetKeyMapper = mapping.getFacetKeyMapper();
        if(facetKeyMapper == null) {
            facetKeyMapper = new DefaultFacetKeyMapper();
        }
        for(Bucket bucket : aggregation.getBuckets()) {
            String key = facetKeyMapper.map(bucket.getKey());

            FacetValue facetValue = new FacetValue(key, bucket.getDoc_count());
            facetReseultCount = facetReseultCount + facetValue.getCount();

            String filterValueEncoded = UrlUtil.encode(bucket.getKey());

            facetValue.setFilter(filterPrefix + facet.getId() + filterType + "=" + filterValuePrefix + filterValueEncoded);

            if(bucket.getSubFacet() != null) {
                filterType=".tree";
                String treeFilterSeperator = UrlUtil.encode(" > ");
                Facet subFacet = mapAggregationToFacet(
                        facetId,
                        bucket.getSubFacet(),
                        filterType,
                        filterValueEncoded + treeFilterSeperator);
                facetValue.setChildren(subFacet);
            }
            facet.getValues().add(facetValue);
        }
        facet.setFilterName(filterPrefix + facetId + filterType);
        facet.setResultCount(facetReseultCount);

        return facet;

    }

    protected Facet mapAggregationToNavigation(String id, Aggregation aggregation, FacetMapping mapping) {
        Facet facet = new Facet();

        for(Bucket bucket : aggregation.getBuckets()) {
            String[] categories = bucket.getKey().split(Pattern.quote("|_|"));
            for (String category : categories) {
                String[] splitted = category.split(Pattern.quote("|-|"));
                String navId = splitted[0];
                String name = splitted[1];
                System.out.println(splitted);
            }
        }
        return facet;

    }

    protected Facet mapAggregationToSlider(String id, Aggregation aggregation, FacetMapping mapping) {
        Facet facet = new Facet();

        facet.setId(id);
        facet.setFilterName(filterPrefix + id);
        facet.setType(mapping.getType());

        String name = mapping.getName();

        if(name == null) {
            name = id;
        }
        facet.setName(name);
        facet.setCount(Long.valueOf(aggregation.getCount()));
        facet.setMinRange(aggregation.getMin());
        facet.setMaxRange(aggregation.getMax());
        return facet;

    }

    @Override
    public Document transformHit(Hit hit) {
        String id = hit.get_id();
        ObjectNode objectNode = hit.get_source();
        LinkedHashMap<String, InnerHitResult> innerHits = hit.getInner_hits();

        Document document = new Document(id);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map fields = mapper.readValue(objectNode.toString(), Map.class);

            if(innerHits != null) {
                transformInnerHits(document, fields, innerHits);
            }

            if(fieldMapping.size() == 0) {
                document.getDocument().putAll(fields);
            } else {
                for(Map.Entry<String, List<String>> mapping : fieldMapping.entrySet()) {
                    String key = mapping.getKey();
                    if(key.endsWith("*")) {
                        mapPrefixFields(mapping, fields, document);
                    } else {
                        mapField(mapping, fields, document);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        transformHighlight(hit, document);
        return document;
    }

    public void mapField(Map.Entry<String, List<String>> mapping, Map fields, Document document) {
        String key = mapping.getKey();
        Object mappedValue = fields.get(key);
        if(mappedValue != null) {
            for(String mappedKey: mapping.getValue()) {
                mapValue(document, mappedKey, mappedValue);
            }
        }
    }

    public void mapPrefixFields(Map.Entry<String, List<String>> mapping, Map fields, Document document) {
        String key = mapping.getKey();
        String prefix = key.substring(0, key.length() - 1);
        for(Object elasticKeyObject : fields.keySet()) {
            String elasticKey = (String) elasticKeyObject;
            if(elasticKey.startsWith(prefix)) {
                for(String mappedKeyPrefix: mapping.getValue()) {
                    String mappedKey = elasticKey.replaceFirst(prefix, mappedKeyPrefix);
                    mapValue(document, mappedKey, fields.get(elasticKey));
                }
            }

        }

    }

    public void transformInnerHits(Document document,Map fields, LinkedHashMap<String, InnerHitResult> innerHits) {
        for (Map.Entry<String, InnerHitResult> entry : innerHits.entrySet()) {
            String fieldName = entry.getKey();
            List<Map<String, Object>> values = (List) fields.get(fieldName);
            List<String> mappedFieldNames = fieldMapping.get(fieldName);
            if(values == null && mappedFieldNames != null) {
                for(Hit hit : entry.getValue().getHits().getHits()) {
                    Document innerDocument = transformHit(hit);
                    for(String mappedFieldName : mappedFieldNames) {
                        document.addChildDocument(mappedFieldName, innerDocument);
                    }
                }
            } else if(values != null) {
                int valueOffset = 0;
                for (Map<String, Object> value : values) {
                    value.put("_score", 0.0);
                    value.put("_offset", valueOffset++);
                }
                for (Hit innerHit : entry.getValue().getHits().getHits()) {
                    Integer offset = innerHit.get_nested().getOffset();
                    values.get(offset).put("_score", innerHit.get_score());
                }
            }
        }
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

    public void addFacetTypeMapping(String id, String type) {
        FacetMapping facetMapping = getOrCreateFacetMapping(id);
        facetMapping.setType(type);
    }

    public void addFacetNameMapping(String id, String name) {
        FacetMapping facetMapping = getOrCreateFacetMapping(id);
        facetMapping.setName(name);
    }

    FacetMapping getOrCreateFacetMapping(String id) {
        FacetMapping facetMapping = this.facetMapping.get(id);
        if(facetMapping == null) {
            facetMapping = new FacetMapping();
            facetMapping.setId(id);
            facetMapping.setName(id);
            this.facetMapping.put(id, facetMapping);
        }
        return facetMapping;
    }

    public void addFacetKeyMapper(String id, FacetKeyMapper facetKeyMapper) {
        FacetMapping facetMapping = getOrCreateFacetMapping(id);
        facetMapping.setFacetKeyMapper(facetKeyMapper);
    }

    public Map<String, List<String>> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(Map<String, List<String>> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public Map<String, FacetMapping> getFacetMapping() {
        return facetMapping;
    }

    public void setFacetMapping(Map<String, FacetMapping> facetMapping) {
        this.facetMapping = facetMapping;
    }

    public String getFilterPrefix() {
        return filterPrefix;
    }

    public void setFilterPrefix(String filterPrefix) {
        this.filterPrefix = filterPrefix;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }
}
