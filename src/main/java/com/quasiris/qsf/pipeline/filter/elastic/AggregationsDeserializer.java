package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregation;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregations;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class AggregationsDeserializer extends StdDeserializer<Aggregations> {

    public AggregationsDeserializer() {
        super(Object.class);
    }

    public AggregationsDeserializer(Class<?> vc) {
        super(vc);
    }

    public AggregationsDeserializer(JavaType valueType) {
        super(valueType);
    }

    public AggregationsDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public Aggregations deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Aggregations aggregations = new Aggregations();
        JsonNode jsonNode = p.getCodec().readTree(p);
        Iterator<Map.Entry<String, JsonNode>> aggs = jsonNode.fields();
        JsonNode qscFiltered = jsonNode.get("qsc_filtered");
        if(qscFiltered != null) {
            aggs = qscFiltered.fields();
        }

        while(aggs.hasNext()) {
            Map.Entry<String, JsonNode> nextAgg = aggs.next();
            updateResultsCount(aggregations, nextAgg);

            if(!"doc_count".equals(nextAgg.getKey()) && !"meta".equals(nextAgg.getKey())) {
                deserializeAggregation(aggregations, nextAgg.getKey(), nextAgg.getValue());
            }
        }

        return aggregations;
    }

    private void updateResultsCount(Aggregations aggregations, Map.Entry<String, JsonNode> nextAgg) {
        Long docCount = null;
        if("doc_count".equals(nextAgg.getKey())) {
            docCount = nextAgg.getValue().asLong();
        } else if(nextAgg.getValue().get("doc_count") != null) {
            docCount = nextAgg.getValue().get("doc_count").asLong();
        }

        if(docCount != null && (aggregations.getDoc_count() == null || aggregations.getDoc_count() > docCount)) {
            // lowest doc count represents the total results count
            aggregations.setDoc_count(docCount);
        }
    }

    private void deserializeAggregation(Aggregations aggregations, String key, JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode targetNode = jsonNode;
        String targetKey = key;
        if(key.endsWith("_filter_wrapper")) {
            targetKey = key.replaceAll("_filter_wrapper", "");
            targetNode = jsonNode.get(targetKey);
        }

        Aggregation aggregation = mapper.convertValue(targetNode, Aggregation.class);
        aggregations.put(targetKey, aggregation);
    }
}
