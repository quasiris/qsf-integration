package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class MockRequestFileHandler {


    private String mockDir;
    private String mockFile;

    public MockRequestFileHandler(String mockDir, String mockFile) {
        this.mockDir = mockDir;
        this.mockFile = mockFile;
    }

    private String getFilename(String request) {
        request = request.replaceAll("\\r\\n|\\r|\\n", "");
        String fileName = mockFile;
        if(fileName == null) {
            fileName = DigestUtils.md5Hex(request);
        }

        return mockDir + "/" + fileName + ".json";
    }

    <T> T getMockedElasticResult(String request, Class<T> valueType) {
        String fileName = getFilename(request);
        try {
            String responseString = Files.toString(new File(fileName), Charset.forName("UTF-8"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void recordQueryElasticResult(String request, Object elasticResult) {
        String fileName = getFilename(request);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String resultAsString = objectMapper.writeValueAsString(elasticResult);
            Files.write(resultAsString,new File(fileName), Charset.forName("UTF-8"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
