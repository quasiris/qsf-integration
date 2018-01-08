package com.quasiris.qsf.pipeline.filter.solr;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import java.io.*;

/**
 * Created by mki on 25.11.17.
 */
public class MockSolrClient extends HttpSolrClient {
    public MockSolrClient(String baseURL) {
        super(baseURL);
    }

    private String mockDir = "src/test/mock/solr";

    private String mockFile;

    private boolean record = false;
    private boolean mock = true;

    @Override
    public QueryResponse query(SolrParams params) throws SolrServerException, IOException {
        if(params instanceof SolrQuery) {
            return query((SolrQuery) params);
        }
        return super.query(params);
    }


    public QueryResponse query(SolrQuery solrQuery) throws SolrServerException, IOException {
        if(mock) {
            return getMockedQueryResponse(solrQuery);
        }

        QueryResponse queryResponse = super.query(solrQuery);
        if(record) {
            recordQueryResponse(solrQuery, queryResponse);
        }
        return queryResponse;
    }


    String getFilename(SolrQuery solrQuery) {
        String fileName = mockFile;
        if(fileName == null) {
            fileName = DigestUtils.md5Hex(SolrFilter.query2url(solrQuery));
        }

        return mockDir + "/" + fileName + ".mock";
    }

    QueryResponse getMockedQueryResponse(SolrQuery solrQuery) {
       String fileName = getFilename(solrQuery);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            QueryResponse queryResponse = (QueryResponse) ois.readObject();
            return queryResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fis);
        }

    }

    void recordQueryResponse(SolrQuery solrQuery, QueryResponse queryResponse) {
        String fileName = getFilename(solrQuery);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(queryResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fos);
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
