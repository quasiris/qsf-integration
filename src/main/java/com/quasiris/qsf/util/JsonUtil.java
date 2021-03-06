package com.quasiris.qsf.util;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mki on 23.12.17.
 */
public class JsonUtil {

    public static String toPrettyString(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return pretty;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object toJson(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> encode(List<String> values) {
        JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();

        return values.stream().
                map(v -> new String(jsonStringEncoder.quoteAsString(v))).
                collect(Collectors.toList());

    }

    public static String encode(String value) {
        String result = null;
        if(value != null) {
            JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
            result = new String(jsonStringEncoder.quoteAsString(value));
        }
        return result;
    }
}
