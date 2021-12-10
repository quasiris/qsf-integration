package com.quasiris.qsf.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.util.IOUtils;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;

import java.io.IOException;

public class TestUtils {
    public static ObjectNode mockQuery(String profileClasspath) throws JsonBuilderException, IOException {
        JsonBuilder jsonBuilder = new JsonBuilder();
        String matchAllQuery = IOUtils.getStringFromClassPath(profileClasspath);
        jsonBuilder.string(matchAllQuery);
        JsonNode elasticQuery = jsonBuilder.get();
        return (ObjectNode) elasticQuery;
    }
}
