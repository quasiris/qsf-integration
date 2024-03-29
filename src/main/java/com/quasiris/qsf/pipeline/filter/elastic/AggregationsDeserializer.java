package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Aggregation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AggregationsDeserializer extends StdDeserializer<Aggregation> {

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

    private ObjectMapper mapper = new ObjectMapper();

    private static final Set<String> JSON_KEY_AGG_EXCLUDES = new HashSet<>(Arrays.asList("meta"));

    @Override
    public Aggregation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        return deserializeAggregation("aggregations", jsonNode);
    }

    private Aggregation deserializeAggregation(String key, JsonNode jsonNode) {
        if(jsonNode.get("buckets") != null || jsonNode.get("value") != null || jsonNode.get("sum") != null) {
            Aggregation aggregation = mapper.convertValue(jsonNode, Aggregation.class);
            aggregation.setKey(key);
            return aggregation;
        }

        Aggregation aggregation = new Aggregation();
        aggregation.setKey(key);
        Iterator<Map.Entry<String, JsonNode>> aggs = jsonNode.fields();
        List<Aggregation> aggregationList = new ArrayList<>();
        while(aggs.hasNext()) {
            Map.Entry<String, JsonNode> nextAgg = aggs.next();
            if(nextAgg.getValue() != null && nextAgg.getValue().isObject() && !JSON_KEY_AGG_EXCLUDES.contains(nextAgg.getKey())) {
                Aggregation aggregationInner = deserializeAggregation(nextAgg.getKey(), nextAgg.getValue());
                aggregationList.add(aggregationInner);
            }
        }
        if(aggregationList.size() > 0) {
            aggregation.setAggregations(aggregationList);
        }
        return aggregation;


    }
}
