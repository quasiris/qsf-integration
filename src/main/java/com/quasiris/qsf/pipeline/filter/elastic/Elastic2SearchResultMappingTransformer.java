package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.category.dto.CategoryDTO;
import com.quasiris.qsf.config.DisplayMappingDTO;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.config.QsfSearchConfigUtil;
import com.quasiris.qsf.dto.response.*;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.*;
import com.quasiris.qsf.pipeline.filter.mapper.*;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.tree.Node;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by mki on 04.11.17.
 */
public class Elastic2SearchResultMappingTransformer implements SearchResultTransformerIF {

    private QsfSearchConfigDTO searchConfigDTO = QsfSearchConfigUtil.initSearchConfig();

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
        String varianId = searchConfigDTO.getVariant().getVariantId();
        if(elasticResult.getAggregations() != null && StringUtils.isNotEmpty(varianId)) {
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

        Map<String, FacetMapping> facetMappingMap = getSearchConfigDTO().getFacet().getFacetMapping();
        Map<String, Aggregation> aggregationMap = new HashMap<>();
        traverseAggsWithBuckets(aggregationMap, elasticResult.getAggregations());

        // for backward compatibility
        // if no mapping is defined for a aggregation a default facet mapping is configured
        for(Map.Entry<String, Aggregation> aggregation : aggregationMap.entrySet()) {
            if(!facetMappingMap.containsKey(aggregation.getKey())) {
                FacetMapping mapping = new FacetMapping();
                mapping.setId(aggregation.getKey());
                mapping.setName(aggregation.getKey());
                facetMappingMap.put(aggregation.getKey(), mapping);
            }
        }

        for(Map.Entry<String, FacetMapping> mapping : facetMappingMap.entrySet()) {
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
        Map<String, FacetMapping> facetMapping = getSearchConfigDTO().getFacet().getFacetMapping();
        FacetMapping mapping = facetMapping.get(facetId);

        if ("slider".equals(mapping.getType())) {
            return mapAggregationToSlider(facetId, aggregation, mapping);
        } else if ("navigation".equals(mapping.getType())) {
            return mapAggregationToNavigation(facetId, aggregation, mapping);
        } else if ("categorySelect".equals(mapping.getType())) {
            return mapAggregationToFacet(facetId, aggregation, null, 0);
        } else if ("histogram".equals(mapping.getType())) {
            Facet facet =  mapAggregationToFacet(facetId, aggregation, null, 0);

            Number minRange = null;
            Number maxRange = null;
            for(FacetValue facetValue : facet.getValues()) {
                if(minRange == null) {
                    minRange = (Number) facetValue.getValue();
                }
                if(maxRange == null) {
                    maxRange = (Number) facetValue.getValue();
                }

                if(((Number) facetValue.getValue()).doubleValue() < minRange.doubleValue()) {
                    minRange = (Number) facetValue.getValue();
                }
                if(((Number) facetValue.getValue()).doubleValue() > maxRange.doubleValue()) {
                    maxRange = (Number) facetValue.getValue();
                }
            }
            if(minRange != null) {
                facet.setMinRange(minRange.doubleValue());
            }

            if(maxRange != null) {
                facet.setMaxRange(maxRange.doubleValue());
            }
            return facet;
        }


        return mapAggregationToFacet(facetId, aggregation, null, 0);
    }


    protected Facet mapAggregationToFacet(String facetId, Aggregation aggregation, FacetFilterMapper facetFilterMapper, int level) {
        Map<String, FacetMapping> facetMapping = getSearchConfigDTO().getFacet().getFacetMapping();
        String filterPrefix = getSearchConfigDTO().getFilter().getFilterPrefix();
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

        FacetKeyMapper facetKeyMapper = getFacetKeyMapper(mapping);
        if(facetFilterMapper == null) {
            facetFilterMapper = getFacetFilterMapper(mapping, new DefaultFacetFilterMapper());
        }
        facetFilterMapper.setFacetId(facet.getId());
        for(Bucket bucket : aggregation.getBuckets()) {
            Object key = facetKeyMapper.map(bucket.getKey());
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
                facetFilterMapper.setFacetId(facet.getId());

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

    private FacetKeyMapper getFacetKeyMapper(FacetMapping mapping) {
        FacetKeyMapper facetKeyMapper = null;
        if(mapping != null) {
            facetKeyMapper = mapping.getFacetKeyMapper();
        }
        if(facetKeyMapper == null) {
            facetKeyMapper = new DefaultFacetKeyMapper();
        }
        return facetKeyMapper;
    }

    private FacetFilterMapper getFacetFilterMapper(FacetMapping mapping, FacetFilterMapper defaultFacetFilterMapper) {
        String filterPrefix = getSearchConfigDTO().getFilter().getFilterPrefix();
        FacetFilterMapper facetFilterMapper = mapping.getFacetFilterMapper();
        if(facetFilterMapper == null) {
            facetFilterMapper = defaultFacetFilterMapper;
        }
        facetFilterMapper.setFilterPrefix(filterPrefix);
        return facetFilterMapper;
    }

    protected Facet mapAggregationToNavigation(String id, Aggregation aggregation, FacetMapping mapping) {
        Node<CategoryDTO> root = new Node<>();
        for(Bucket bucket : aggregation.getBuckets()) {
            String[] categories = bucket.getKey().toString().split(Pattern.quote("|___|"));
            Node<CategoryDTO> current = root;
            for (String categoryString : categories) {
                String[] splitted = categoryString.split(Pattern.quote("|-|"));
                CategoryDTO category = new CategoryDTO();
                category.setId(splitted[0]);
                category.setPosition(Integer.valueOf(splitted[1]));
                category.setName(splitted[2]);

                Long count = bucket.getDoc_count();
                if(bucket.getVariant_count() != null && bucket.getVariant_count().getValue() != null) {
                    count = bucket.getVariant_count().getValue();
                }
                category.setCount(count);
                category.setFilter(bucket.getKey().toString());
                current = current.addChildIfNotExists(category);
            }
        }

        FacetKeyMapper facetKeyMapper = getFacetKeyMapper(mapping);
        FacetFilterMapper facetFilterMapper = getFacetFilterMapper(mapping, new NavigationIdFacetFilterMapper());
        facetFilterMapper.setFacetId(id);
        Facet facet = traverse(root, id, facetKeyMapper, facetFilterMapper);

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


    public Facet traverse(Node<CategoryDTO> node, String filterName, FacetKeyMapper facetKeyMapper, FacetFilterMapper facetFilterMapper ){
        Collections.sort(node.getChildren(), new Comparator<Node<CategoryDTO>>() {
            @Override
            public int compare(Node<CategoryDTO> left, Node<CategoryDTO> right) {
                return left.getData().getPosition().compareTo(right.getData().getPosition());
            }
        });

        String filterPrefix = getSearchConfigDTO().getFilter().getFilterPrefix();
        Facet facet = new Facet();
        facet.setFilterName(filterPrefix + filterName);
        facet.setValues(new ArrayList<>());
        for (Node<CategoryDTO> child : node.getChildren()) {

            FacetValue facetValue = new FacetValue();
            facetValue.setCount(child.getData().getCount());
            Object value = facetKeyMapper.map(child.getData().getName());
            facetValue.setFilter(child.getData().getFilter());
            Map<String, Object> customData = new HashMap<>();
            customData.put("id", child.getData().getId());
            facetValue.setCustomData(customData);
            facetValue.setValue(value);
            facetFilterMapper.map(facetValue);
            Facet subFacet = traverse(child, filterName, facetKeyMapper, facetFilterMapper);
            facetValue.setChildren(subFacet);
            facet.getValues().add(facetValue);
        }

        return facet;

    }

    protected Facet mapAggregationToSlider(String id, Aggregation aggregation, FacetMapping mapping) {
        String filterPrefix = getSearchConfigDTO().getFilter().getFilterPrefix();
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
            if (!QsfSearchConfigUtil.hasDisplayMapping(searchConfigDTO)) {
                document.getDocument().putAll(fields);
            } else {
                for(DisplayMappingDTO mapping : searchConfigDTO.getDisplay().getMapping()) {
                    if(mapping.getFrom().endsWith("*")) {
                        mapPrefixFields(mapping, fields, document);
                    } else if(mapping.getFrom().contains(".")) {
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
    public void mapNestedField(DisplayMappingDTO mapping, Map fields, Document document) {
        String[] keyParts = mapping.getFrom().split("\\.");
        String nestedField = keyParts.length > 1 ? keyParts[0] : null;
        Object mappedValue = fields.get(nestedField);
        mapValue(document, nestedField, mappedValue);
    }

    public void mapField(DisplayMappingDTO mapping, Map fields, Document document, Hit hit) {
        String key = mapping.getFrom();
        Object mappedValue = fields.get(key);

        if (mappedValue == null && "_score".equals(key)) {
            mappedValue = hit.get_score();
        }

        if (mappedValue == null && "_matched_queries".equals(key)) {
            mappedValue = hit.getMatched_queries();
        }

        if(mappedValue != null) {

            mapValue(document, mapping.getTo(), mappedValue);

        }
    }

    public void mapPrefixFields(DisplayMappingDTO mapping, Map fields, Document document) {
        String from = mapping.getFrom();
        String prefix = from.substring(0, from.length() - 1);
        for(Object elasticKeyObject : fields.keySet()) {
            String elasticKey = (String) elasticKeyObject;
            if(elasticKey.startsWith(prefix)) {
                String mappedKeyPrefix = mapping.getTo();
                if(mappedKeyPrefix != null) {
                    if (mappedKeyPrefix.endsWith("*")) {
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
            String fieldName;

            Map<String, String> innerhitsMapping = searchConfigDTO.getDisplay().getInnerhitsMapping();
            if(innerhitsMapping != null && innerhitsMapping.get(innerHitsName) != null) {
                fieldName = innerhitsMapping.get(innerHitsName);
            } else {
                fieldName = innerHitsName;
            }
            List<Map<String, Object>> values = (List) fields.get(fieldName);

            List<String> mappedFieldNames = null;

            if(QsfSearchConfigUtil.hasDisplayMapping(searchConfigDTO)) {
                mappedFieldNames = searchConfigDTO.getDisplay().getMapping().stream().
                        filter(m -> m.getFrom().equals(fieldName)).
                        map(DisplayMappingDTO::getFrom).
                        collect(Collectors.toList());
            }

            Map<String, List<String>>  groupInnerhitsMapping = getSearchConfigDTO().getDisplay().getGroupInnerhitsMapping();
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
                    if(hit.get_source() != null) {
                        Document innerDocument = transformHit(hit);
                        for (String mappedFieldName : mappedFieldNames) {
                            document.addChildDocument(mappedFieldName, innerDocument);
                        }
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
        if(key != null) {
            document.getDocument().put(key, value);
        }
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
        document.getDocument().put("_matched_queries", hit.getMatched_queries());
    }

    @Override
    public StringBuilder print(String indent) {
        return new StringBuilder("TODO");
    }

    public void addFieldMapping(String from, String to) {
        QsfSearchConfigUtil.initDisplayMapping(searchConfigDTO);
        DisplayMappingDTO mapping = new DisplayMappingDTO();
        mapping.setFrom(from);
        mapping.setTo(to);
        this.searchConfigDTO.getDisplay().getMapping().add(mapping);
    }

    public void addFacetTypeMapping(String id, String type) {
        FacetMapping facetMapping = getOrCreateFacetMapping(this.searchConfigDTO, id);
        facetMapping.setType(type);
    }

    public void addFacetNameMapping(String id, String name) {
        FacetMapping facetMapping = getOrCreateFacetMapping(this.searchConfigDTO, id);
        facetMapping.setName(name);
    }

    public static FacetMapping getOrCreateFacetMapping(QsfSearchConfigDTO config, String id) {
        Map<String, FacetMapping> facetMappingMap = config.getFacet().getFacetMapping();
        FacetMapping facetMapping = facetMappingMap.get(id);
        if(facetMapping == null) {
            facetMapping = new FacetMapping();
            facetMapping.setId(id);
            facetMapping.setName(id);
            facetMappingMap.put(id, facetMapping);
        }
        return facetMapping;
    }

    public void addFacetKeyMapper(String id, FacetKeyMapper facetKeyMapper) {
        FacetMapping facetMapping = getOrCreateFacetMapping(this.searchConfigDTO, id);
        facetMapping.setFacetKeyMapper(facetKeyMapper);
    }



    public void addFacetFilterMapper(String id, FacetFilterMapper facetFilterMapper) {
        FacetMapping facetMapping = getOrCreateFacetMapping(this.searchConfigDTO, id);
        facetMapping.setFacetFilterMapper(facetFilterMapper);
    }

    public Map<String, FacetMapping> getFacetMapping() {
        Map<String, FacetMapping> facetMappingMap = getSearchConfigDTO().getFacet().getFacetMapping();
        return facetMappingMap;
    }

    public QsfSearchConfigDTO getSearchConfigDTO() {
        return searchConfigDTO;
    }

    public void setSearchConfig(QsfSearchConfigDTO searchConfigDTO) {
        this.searchConfigDTO = searchConfigDTO;
    }
}
