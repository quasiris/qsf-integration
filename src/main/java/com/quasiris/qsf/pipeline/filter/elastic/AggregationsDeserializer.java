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
import com.quasiris.qsf.pipeline.filter.elastic.bean.Bucket;
import com.quasiris.qsf.util.QsfIntegrationConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Aggregations deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Aggregations aggregations = new Aggregations();
        JsonNode jsonNode = p.getCodec().readTree(p);
        Iterator<Map.Entry<String, JsonNode>> aggs = jsonNode.fields();

        List<Aggregation> aggregationList = new ArrayList<>();
        while(aggs.hasNext()) {
            Map.Entry<String, JsonNode> nextAgg = aggs.next();
            if(!"doc_count".equals(nextAgg.getKey()) && !"meta".equals(nextAgg.getKey())) {
                Aggregation aggregation = deserializeAggregation(nextAgg.getKey(), nextAgg.getValue());
                aggregationList.add(aggregation);
            }
        }
        if(aggregationList.size() > 0) {
            aggregations.setAggregations(aggregationList);
        }
        return aggregations;
    }

    private Aggregation deserializeAggregation(String key, JsonNode jsonNode) {
        if(jsonNode.get("buckets") != null || jsonNode.get("value") != null) {
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
            if(!"doc_count".equals(nextAgg.getKey()) && !"meta".equals(nextAgg.getKey())) {
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
