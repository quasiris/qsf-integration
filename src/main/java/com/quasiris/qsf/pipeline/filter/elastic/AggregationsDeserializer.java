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
        Iterator<String> it = jsonNode.fieldNames();
        while(it.hasNext()) {
            String key = it.next();
            if(key.endsWith("_filter_wrapper")) {
                String wrappedKey = key.replaceAll("_filter_wrapper", "");
                ObjectMapper mapper = new ObjectMapper();
                Aggregation aggregation = mapper.convertValue(jsonNode.get(key).get(wrappedKey), Aggregation.class);
                aggregations.put(wrappedKey, aggregation);


            } else {
                ObjectMapper mapper = new ObjectMapper();
                Aggregation aggregation = mapper.convertValue(jsonNode.get(key), Aggregation.class);
                aggregations.put(key, aggregation);

            }
        }
        return aggregations;
    }
}
