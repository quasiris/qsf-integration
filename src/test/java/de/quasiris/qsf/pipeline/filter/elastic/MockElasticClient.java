package de.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import de.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import de.quasiris.qsf.pipeline.filter.elastic.client.StandardElasticClient;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by mki on 25.11.17.
 */
public class MockElasticClient extends StandardElasticClient {

    private String mockDir = "src/test/mock/elastic";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;

    @Override
    public ElasticResult request(String elasticUrl, String request) throws IOException {
        if(mock) {
            return getMockedElasticResult(request);
        }

        ElasticResult elasticResult = super.request(elasticUrl, request);
        if(record) {
            recordQueryElasticResult(request, elasticResult);
        }
        return elasticResult;
    }

    String getFilename(String request) {
        request = request.replaceAll("\\r\\n|\\r|\\n", "");
        String fileName = mockFile;
        if(fileName == null) {
            fileName = DigestUtils.md5Hex(request);
        }

        return mockDir + "/" + fileName + ".json";
    }

    ElasticResult getMockedElasticResult(String request) {
        String fileName = getFilename(request);
        try {
            String responseString = Files.toString(new File(fileName), Charset.forName("UTF-8"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ElasticResult.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    void recordQueryElasticResult(String request, ElasticResult elasticResult) {
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

    public String getMockDir() {
        return mockDir;
    }

    public void setMockDir(String mockDir) {
        this.mockDir = mockDir;
    }

    public String getMockFile() {
        return mockFile;
    }

    public void setMockFile(String mockFile) {
        this.mockFile = mockFile;
    }

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        if(record) {
            mock = false;
        }
        this.record = record;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }
}
