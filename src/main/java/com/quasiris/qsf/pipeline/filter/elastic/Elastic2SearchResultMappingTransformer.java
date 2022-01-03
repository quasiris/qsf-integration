package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.category.dto.CategoryDTO;
import com.quasiris.qsf.commons.util.UrlUtil;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregation;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Bucket;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.bean.InnerHitResult;
import com.quasiris.qsf.pipeline.filter.mapper.DefaultFacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import com.quasiris.qsf.tree.Node;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    Long getTotalCountAggregation(Aggregation aggregation) {
        Long totalCount = null;
        if(aggregation.getKey().equals(QsfIntegrationConstants.TOTAL_COUNT_AGGREGATION_NAME)) {
            return aggregation.getValue();
        }

        if(aggregation.getAggregations() != null) {
            for(Aggregation inner : aggregation.getAggregations()) {
                totalCount = getTotalCountAggregation(inner);
                if(totalCount != null) {
                    return totalCount;
                }
            }
        }
        return totalCount;
    }

    protected void updateTotalDocuments(ElasticResult elasticResult, SearchResult searchResult) {
        if(elasticResult.getAggregations() != null && StringUtils.isNotEmpty(getVariantId())) {
            Long totalCount = getTotalCountAggregation(elasticResult.getAggregations());
            if(totalCount != null) {
                // TODO totalVariants = total;
                searchResult.setTotal(totalCount);
            }
        }
    }

    void traverseAggsWithBuckets(Map<String, Aggregation> aggregationMap, Aggregation aggregation) {

        if(aggregation.getBuckets() != null && aggregation.getAggregations() != null && aggregation.getSum() != null) {
            // LOG error
            return;
        }

        if(aggregation.getBuckets() != null || aggregation.getSum() != null) {
            aggregationMap.put(aggregation.getKey(), aggregation);
        }

        if(aggregation.getAggregations() != null) {
            for(Aggregation inner : aggregation.getAggregations()) {
                traverseAggsWithBuckets(aggregationMap, inner);
            }
        }
    }

    protected void mapFacets(ElasticResult elasticResult, SearchResult searchResult) {
        if(elasticResult.getAggregations() == null) {
            return;
        }

        Map<String, Aggregation> aggregationMap = new HashMap<>();
        traverseAggsWithBuckets(aggregationMap, elasticResult.getAggregations());

        // for backward compatibility
        // if no mapping is defined for a aggregation a default facet mapping is configured
        for(Map.Entry<String, Aggregation> aggregation : aggregationMap.entrySet()) {
            if(!facetMapping.containsKey(aggregation.getKey())) {
                FacetMapping mapping = new FacetMapping();
                mapping.setId(aggregation.getKey());
                mapping.setName(aggregation.getKey());
                facetMapping.put(aggregation.getKey(), mapping);
            }
        }

        for(Map.Entry<String, FacetMapping> mapping : facetMapping.entrySet()) {
            FacetMapping facetMapping = mapping.getValue();
            Aggregation aggregation = aggregationMap.get(mapping.getKey());
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

            Long count = bucket.getDoc_count();
            if(bucket.getVariant_count() != null && bucket.getVariant_count().getValue() != null) {
                count = bucket.getVariant_count().getValue();
            }

            FacetValue facetValue = new FacetValue(key, count);
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

        Node<CategoryDTO> root = new Node<>();
        for(Bucket bucket : aggregation.getBuckets()) {
            String[] categories = bucket.getKey().split(Pattern.quote("|___|"));
            Node<CategoryDTO> current = root;
            for (String categoryString : categories) {
                String[] splitted = categoryString.split(Pattern.quote("|-|"));
                CategoryDTO category = new CategoryDTO();
                category.setId(splitted[0]);
                category.setPosition(Integer.valueOf(splitted[1]));
                category.setName(splitted[2]);
                category.setCount(bucket.getDoc_count());
                current = current.addChildIfNotExists(category);
            }
        }

        facet = traverse(root);
        return facet;

    }


    public Facet traverse(Node<CategoryDTO> node){
        Collections.sort(node.getChildren(), new Comparator<Node<CategoryDTO>>() {
            @Override
            public int compare(Node<CategoryDTO> left, Node<CategoryDTO> right) {
                return left.getData().getPosition().compareTo(right.getData().getPosition());
            }
        });
        Facet facet = new Facet();
        facet.setValues(new ArrayList<>());
        for (Node<CategoryDTO> child : node.getChildren()) {
            FacetValue facetValue = new FacetValue();
            facetValue.setCount(child.getData().getCount());
            facetValue.setValue(child.getData().getName());
            facetValue.setFilter(child.getData().getId());
            Facet subFacet = traverse(child);
            facetValue.setChildren(subFacet);
            facet.getValues().add(facetValue);
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
                    Double score = innerHit.get_score();
                    values.get(offset).put("_score", score);
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
