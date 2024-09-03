package com.quasiris.qsf.mock;

import com.quasiris.qsf.commons.util.IOUtils;
import com.quasiris.qsf.pipeline.filter.web.QSFHttpServletRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;
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
//        Mockito.when(response.getStatusLine()).thenReturn(statusLine);

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
}
