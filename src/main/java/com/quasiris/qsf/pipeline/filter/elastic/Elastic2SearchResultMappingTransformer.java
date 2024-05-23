package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.category.dto.CategoryDTO;
import com.quasiris.qsf.dto.response.*;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.*;
import com.quasiris.qsf.pipeline.filter.mapper.DefaultFacetFilterMapper;
import com.quasiris.qsf.pipeline.filter.mapper.DefaultFacetKeyMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetFilterMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.tree.Node;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by mki on 04.11.17.
 */
public class Elastic2SearchResultMappingTransformer implements SearchResultTransformerIF {

    private Map<String, List<String>> fieldMapping = new LinkedHashMap<>();

    // mapping for innerhits to a field, if there are multiple inner hits, that belong to one field
    private Map<String, String> innerhitsMapping = new HashMap<>();

    private Map<String, FacetMapping> facetMapping = new LinkedHashMap<>();

    private String filterPrefix = "";
    private String variantId;

    private Map<String, List<String>> groupInnerhitsMapping;

    private SearchQuery searchQuery;

    public Elastic2SearchResultMappingTransformer() {


    }

    @Override
    public void init(PipelineContainer pipelineContainer) {
        this.searchQuery = pipelineContainer.getSearchQuery();
    }

    @Override
    public SearchResult transform(ElasticResult elasticResult) {

        SearchResult searchResult = new SearchResult();
        searchResult.initDocuments();
        searchResult.setTotal(elasticResult.getHits().getTotal());
        searchResult.setStatusMessage("OK");
        searchResult.setStatusCode(200);
        searchResult.setTime(elasticResult.getTook());

        if(elasticResult.get_scroll_id() != null) {

            Paging paging = new Paging();
            paging.setNextPage(new Page());
            paging.getNextPage().setToken(elasticResult.get_scroll_id());
            searchResult.setPaging(paging);
        }

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
            Facet facet = mapAggregation(facetMapping.getId(), aggregation);
            searchResult.addFacet(facet);
        }
    }

    protected Facet mapAggregation(String facetId, Aggregation aggregation) {
        FacetMapping mapping = facetMapping.get(facetId);

        if ("slider".equals(mapping.getType())) {
            return mapAggregationToSlider(facetId, aggregation, mapping);
        } else if ("navigation".equals(mapping.getType())) {
            return mapAggregationToNavigation(facetId, aggregation, mapping);
        } else if ("categorySelect".equals(mapping.getType())) {
            return mapAggregationToFacet(facetId, aggregation, null, 0);
        }


        return mapAggregationToFacet(facetId, aggregation, null, 0);
    }


    protected Facet mapAggregationToFacet(String facetId, Aggregation aggregation, FacetFilterMapper facetFilterMapper, int level) {
        Facet facet = new Facet();
        facet.setValues(new ArrayList<>());
        FacetMapping mapping = facetMapping.get(facetId);
        String name = null;
        if(mapping != null) {
            facet.setId(mapping.getId());
            facet.setType(mapping.getType());
            name = mapping.getName();
        }

        if(name == null) {
            name = facetId;
        }
        if(facet.getId() == null) {
            facet.setId(facetId);
        }
        facet.setName(name);
        facet.setCount((long) aggregation.getBuckets().size());

        Long facetReseultCount = 0L;
        FacetKeyMapper facetKeyMapper = null;
        if(mapping != null) {
            facetKeyMapper = mapping.getFacetKeyMapper();
        }
        if(facetKeyMapper == null) {
            facetKeyMapper = new DefaultFacetKeyMapper();
        }

        if(facetFilterMapper == null) {
            facetFilterMapper = mapping.getFacetFilterMapper();
        }

        if(facetFilterMapper == null) {
            facetFilterMapper = new DefaultFacetFilterMapper();
        }
        facetFilterMapper.setFacet(facet);
        facetFilterMapper.setFilterPrefix(filterPrefix);

        for(Bucket bucket : aggregation.getBuckets()) {
            String key = facetKeyMapper.map(bucket.getKey());
            facetFilterMapper.setKey(bucket.getKey());
            Long count = bucket.getDoc_count();
            if(bucket.getVariant_count() != null && bucket.getVariant_count().getValue() != null) {
                count = bucket.getVariant_count().getValue();
            }

            FacetValue facetValue = new FacetValue(key, count);
            if(bucket.getCustomData() != null) {
                facetValue.setCustomData(bucket.getCustomData());
            }
            facetReseultCount = facetReseultCount + facetValue.getCount();

            facetFilterMapper.map(facetValue);

            if(bucket.getSubFacet() != null) {
                int l = level + 1;
                FacetValue parentFacetValue = facetFilterMapper.getParentFacetValue();
                facetFilterMapper.setParentFacetValue(facetValue);
                Facet subFacet = mapAggregationToFacet(
                        facetId + "." + l,
                        bucket.getSubFacet(), facetFilterMapper, l);
                facetValue.setChildren(subFacet);

                // reset the mapper
                facetFilterMapper.setParentFacetValue(parentFacetValue);
                facetFilterMapper.setFacet(facet);

            }
            facet.getValues().add(facetValue);


        }
        if(mapping != null) {
            facet.setFilterName(filterPrefix + mapping.getId());
        } else {
            facet.setFilterName(filterPrefix + facetId);
        }

        facet.setResultCount(facetReseultCount);

        return facet;

    }

    protected Facet mapAggregationToNavigation(String id, Aggregation aggregation, FacetMapping mapping) {
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

        Facet facet = traverse(root);

        facet.setType(mapping.getType());
        String name = mapping.getName();

        if(name == null) {
            name = id;
        }
        facet.setId(id);
        facet.setName(name);
        facet.setCount((long) aggregation.getBuckets().size());

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

            if(fieldMapping.size() == 0) {
                document.getDocument().putAll(fields);
            } else {
                for(Map.Entry<String, List<String>> mapping : fieldMapping.entrySet()) {
                    String key = mapping.getKey();
                    if(key.endsWith("*")) {
                        mapPrefixFields(mapping, fields, document);
                    } else if(key.contains(".")) {
                        mapNestedField(mapping, fields, document);
                    } else {
                        mapField(mapping, fields, document, hit);
                    }
                }
            }

            if(innerHits != null) {
                transformInnerHits(document, fields, innerHits);
            }

            if(hit.getFields() != null) {
                for(Map.Entry<String, List<Object>> field :  hit.getFields().entrySet()) {
                    if(field.getValue() != null && field.getValue().size() > 0 && field.getValue().get(0) != null) {
                        document.setValue(field.getKey(), field.getValue());
                    }
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        transformHighlight(hit, document);
        transformExplanation(hit, document);
        return document;
    }

    /**
     * Nested fields are taken as-is. The visibility is controlled via elastic source and the search behaviour through search-config.
     * Note: Mapping to other name is not supported!
     * Note: Only one level is supported!
     * @param mapping of nested field
     * @param fields with values
     * @param document target
     */
    public void mapNestedField(Map.Entry<String, List<String>> mapping, Map fields, Document document) {
        String[] keyParts = mapping.getKey().split("\\.");
        String nestedField = keyParts.length > 1 ? keyParts[0] : null;
        Object mappedValue = fields.get(nestedField);
        mapValue(document, nestedField, mappedValue);
    }

    public void mapField(Map.Entry<String, List<String>> mapping, Map fields, Document document, Hit hit) {
        String key = mapping.getKey();
        Object mappedValue = fields.get(key);

        if (mappedValue == null && "_score".equals(key)) {
            mappedValue = hit.get_score();
        }
        if(mappedValue != null) {
            for(String mappedKey: mapping.getValue()) {
                mapValue(document, mappedKey, mappedValue);
            }
        }
    }

    public void mapPrefixFields(Map.Entry<String, List<String>> mapping, Map fields, Document document) {
        String from = mapping.getKey();
        String prefix = from.substring(0, from.length() - 1);
        for(Object elasticKeyObject : fields.keySet()) {
            String elasticKey = (String) elasticKeyObject;
            if(elasticKey.startsWith(prefix)) {
                for(String mappedKeyPrefix: mapping.getValue()) {
                    if(mappedKeyPrefix.endsWith("*")) {
                        mappedKeyPrefix = mappedKeyPrefix.substring(0, mappedKeyPrefix.length() - 1);
                        String mappedKey = elasticKey.replaceFirst(prefix, mappedKeyPrefix);
                        mapValue(document, mappedKey, fields.get(elasticKey));
                    } else {
                        mapValue(document, mappedKeyPrefix, fields.get(elasticKey));
                    }

                }
            }

        }

    }


    public void transformInnerHits(Document document,Map fields, LinkedHashMap<String, InnerHitResult> innerHits) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Map.Entry<String, InnerHitResult> entry : innerHits.entrySet()) {
            String innerHitsName = entry.getKey();
            String fieldName = innerHitsName;
            if(innerhitsMapping != null && innerhitsMapping.get(innerHitsName) != null) {
                fieldName = innerhitsMapping.get(innerHitsName);
            }
            List<Map<String, Object>> values = (List) fields.get(fieldName);

            List<String> mappedFieldNames = fieldMapping.get(fieldName);
            if(groupInnerhitsMapping != null) {
                for(Map.Entry<String, List<String>> groupInnerhitsMappingEntry : groupInnerhitsMapping.entrySet()) {
                    String innerhitsFieldName = groupInnerhitsMappingEntry.getKey();
                    List<Object> grouped = new ArrayList<>();
                    for (Hit hit : entry.getValue().getHits().getHits()) {
                        Map<String, Object> innerHitsFields = objectMapper.convertValue(hit.get_source(), new TypeReference<Map<String, Object>>() {});
                        Object groupedObject = innerHitsFields.get(innerhitsFieldName);
                        if (groupedObject != null) {
                            grouped.add(groupedObject);
                        }
                    }
                    for(String resultFieldName: groupInnerhitsMappingEntry.getValue()) {
                        document.getDocument().put(resultFieldName, grouped);
                    }
                }
            }
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
                    if(value.get("_found") == null) {
                        value.put("_score", 0.0);
                        value.put("_offset", valueOffset++);
                        value.put("_found", false);
                    }
                }
                for (Hit innerHit : entry.getValue().getHits().getHits()) {
                    Integer offset = innerHit.get_nested().getOffset();
                    Double score = innerHit.get_score();
                    values.get(offset).put("_score", score);
                    values.get(offset).put("_found", true);

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
    public void transformExplanation(Hit hit, Document document) {
        if(hit.get_explanation() == null) {
            return;
        }
        if(!searchQuery.isCtrl("trace")) {
            return;
        }
        document.getDocument().put("_explanation", hit.get_explanation());
        document.getDocument().put("_score", hit.get_score());
    }

    @Override
    public StringBuilder print(String indent) {
        return new StringBuilder("TODO");
    }

    public void filterPrefix(String filterPrefix) {
        this.filterPrefix=filterPrefix;
    }

    public void addInnerhitsGroupMapping(String from, String to) {
        if(groupInnerhitsMapping == null) {
            groupInnerhitsMapping = new HashMap<>();
        }
        List<String> mapping = groupInnerhitsMapping.get(from);
        if(mapping == null) {
            mapping = new ArrayList<>();
        }
        mapping.add(to);

        groupInnerhitsMapping.put(from, mapping);
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

    public void addFacetFilterMapper(String id, FacetFilterMapper facetFilterMapper) {
        FacetMapping facetMapping = getOrCreateFacetMapping(id);
        facetMapping.setFacetFilterMapper(facetFilterMapper);
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

    public void addInnerhitsMapping(String from, String to) {
        if(innerhitsMapping == null) {
            innerhitsMapping = new HashMap<>();
        }
        innerhitsMapping.put(from, to);
    }
    public Map<String, String> getInnerhitsMapping() {
        return innerhitsMapping;
    }

    public void setInnerhitsMapping(Map<String, String> innerhitsMapping) {
        this.innerhitsMapping = innerhitsMapping;
    }
}
