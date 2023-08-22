package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;
import com.quasiris.qsf.commons.exception.ResourceNotFoundException;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardMultiElasticClient;

import java.time.Duration;
import java.util.List;


public class MockElasticSearchClient extends ElasticSearchClient {

    private String mockDir = "src/test/mock/elastic";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;

    @Override
    public ElasticResult search(String indexUrl, String jsonQuery) throws ResourceNotFoundException {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(jsonQuery, ElasticResult.class);
        }

        ElasticResult elasticResult = super.search(indexUrl, jsonQuery);
        if(record) {
            mockRequestFileHandler.recordQueryElasticResult(jsonQuery, elasticResult);
        }
        return elasticResult;
    }

    @Override
    public ElasticResult search(String indexUrl, String jsonQuery, Duration requestTimeout) throws ResourceNotFoundException {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(jsonQuery, ElasticResult.class);
        }

        ElasticResult elasticResult = super.search(indexUrl, jsonQuery);
        if(record) {
            mockRequestFileHandler.recordQueryElasticResult(jsonQuery, elasticResult);
        }
        return elasticResult;
    }

    @Override
    public MultiElasticResult multiSearch(String elasticUrl, List<String> requests) throws ResourceNotFoundException {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);

        String request = StandardMultiElasticClient.createRequest(requests);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(request, MultiElasticResult.class);
        }

        MultiElasticResult multiElasticResult = super.multiSearch(elasticUrl, requests);
        if(record) {
            mockRequestFileHandler.recordQueryElasticResult(request, multiElasticResult);
        }
        return multiElasticResult;
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
