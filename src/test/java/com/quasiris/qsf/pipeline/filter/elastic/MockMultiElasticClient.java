package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardMultiElasticClient;

import java.io.IOException;
import java.util.List;

/**
 * Created by mki on 25.11.17.
 */
public class MockMultiElasticClient extends StandardMultiElasticClient {

    private String mockDir = "src/test/mock/elastic";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;


    @Override
    public MultiElasticResult request(String elasticUrl, List<String> requests) throws IOException, PipelineContainerException {
        MockRequestFileHandler mockRequestFileHandler = new MockRequestFileHandler(mockDir, mockFile);

        String request = StandardMultiElasticClient.createRequest(requests);
        if(mock) {
            return mockRequestFileHandler.getMockedElasticResult(request, MultiElasticResult.class);
        }

        MultiElasticResult multiElasticResult = super.request(elasticUrl, requests);
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
