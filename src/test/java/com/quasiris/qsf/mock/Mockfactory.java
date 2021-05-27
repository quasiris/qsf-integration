package com.quasiris.qsf.mock;

import com.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import com.quasiris.qsf.pipeline.filter.web.QSFHttpServletRequest;
import com.quasiris.qsf.util.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by mki on 25.11.17.
 */
public class Mockfactory {


    public static CloseableHttpClient createCloseableHttpClient(String fileName, int statusCode) throws IOException {
        String responseString = IOUtils.getString(fileName);

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = new StringEntity(responseString);

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);

        Mockito.when(response.getEntity()).thenReturn(httpEntity);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);

        Mockito.when(httpClient.execute(Mockito.any())).thenReturn(response);

        return httpClient;
    }

    @Deprecated
    public static HttpServletRequest createHttpServletRequest(String url){
        try {
            return new QSFHttpServletRequest(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static MockSolrClient createSolrClient(String url) {
        MockSolrClient mockSolrClient = new MockSolrClient(url);
        //mockSolrClient.setRecord(true);
        return mockSolrClient;
    }
}
