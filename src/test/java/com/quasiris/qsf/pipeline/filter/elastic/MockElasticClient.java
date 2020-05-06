package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.elastic.bean.Analyze;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardElasticClient;

import java.io.IOException;

/**
 * Created by mki on 25.11.17.
 */
public class MockElasticClient extends StandardElasticClient {

    private String mockDir = "src/test/mock/elastic";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;

    @Override
    public Analyze analyze(String elasticBaseUrl, String request) throws IOException {
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

    @Override
    public ElasticResult request(String elasticUrl, String request) throws IOException {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(request, ElasticResult.class);
        }

        ElasticResult elasticResult = super.request(elasticUrl, request);
        if(record) {
            mockRequestFileHandler.recordQueryElasticResult(request, elasticResult);
        }
        return elasticResult;
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
