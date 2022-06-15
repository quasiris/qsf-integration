package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.commons.elasticsearch.client.ElasticAnalyzeClient;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Analyze;


public class MockElasticAnalyzeClient extends ElasticAnalyzeClient {

    private String mockDir = "src/test/mock/elastic";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;

    @Override
    public Analyze analyze(String elasticBaseUrl, String request) {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(request, Analyze.class);
        }

        Analyze analyze = super.analyze(elasticBaseUrl, request);
        if(record) {
            mockRequestFileHandler.recordQueryElasticResult(request, analyze);
        }
        return analyze;
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
