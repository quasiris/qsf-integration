package com.quasiris.qsf.pipeline.filter.elastic;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.quasiris.qsf.util.ElasticUtil;
import com.quasiris.qsf.commons.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ProfileLoader {

    public static String loadProfile(String filename, Map<String, Object> vars) throws IOException {
        String profile = null;
        if (filename.startsWith("classpath://")) {
            profile = loadProfileFromClasspath(filename);
        } else {
            profile = loadProfileFromFile(filename);
        }
        return replaceParameters(profile, vars);
    }

    public static String replaceParameters(String profile, Map<String, Object> vars) {

        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            if(entry.getValue() == null) {
                entry.setValue("null");
            }
        }
        StringSubstitutor stringSubstitutor = new StringSubstitutor(vars);
        profile = stringSubstitutor.replace(profile);
        return profile;

    }

    public static String loadProfileFromClasspath(String filename) throws IOException {

        String resource = filename.replaceFirst("classpath://", "");
        InputStream in = ProfileLoader.class.getClassLoader()
                .getResourceAsStream(resource);

        // TODO try to remove IOUtils
        String profile = IOUtils.toString(in, Charset.forName("UTF-8"));
        IOUtils.closeQuietly(in);
        return profile;
    }

    public static String loadProfileFromFile(String filename) throws IOException {
        File file = new File(filename);
        String profile = Files.toString(file, Charsets.UTF_8);
        return profile;
    }

    public static Map<String, Object> encodeParameters(Map<String, Object> rawValues) {
        Map<String, Object> escapedValues = rawValues.entrySet().stream().collect(HashMap::new,
                (m,e)->m.put(e.getKey() + ".escaped", escapeValue(e.getValue())), HashMap::putAll);

        Map<String, String> encodedValues = rawValues.entrySet().stream().collect(HashMap::new,
                (m,e)->m.put(e.getKey() + ".encoded", JsonUtil.encode(e.getValue().toString())), HashMap::putAll);

        Map<String, Object> replaceMap = new HashMap<>(escapedValues);
        replaceMap.putAll(encodedValues);
        replaceMap.putAll(rawValues);
        return replaceMap;
    }

    public static Map<String, String> encodeValues(Map<String, String> replaceMap) {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            String escapedValue = ElasticUtil.escape(entry.getValue());
            String encodedValue = JsonUtil.encode(escapedValue);
            ret.put(entry.getKey(), encodedValue);
        }
        return ret;
    }

    public static String escapeValue(Object value) {
        if(value == null) {
            return null;
        }
        String escapedValue = ElasticUtil.escape(value.toString());
        String encodedValue = JsonUtil.encode(escapedValue);
        return encodedValue;
    }
}
