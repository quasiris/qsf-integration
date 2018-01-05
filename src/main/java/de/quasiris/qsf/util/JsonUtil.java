package de.quasiris.qsf.util;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
