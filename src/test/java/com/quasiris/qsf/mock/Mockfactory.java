package com.quasiris.qsf.mock;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by mki on 25.11.17.
 */
public class Mockfactory {


    public static CloseableHttpClient createCloseableHttpClient(String fileName, int statusCode) throws IOException {
        String responseString = Files.toString(new File(fileName), Charset.forName("UTF-8"));

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

    public static HttpServletRequest createHttpServletRequest(String url){
        try {
            URI uri = new URI(url);


            final Iterable<String> parameterList = Splitter.on("&")
                    .omitEmptyStrings()
                    .trimResults().split(uri.getQuery());

            final Map<String, List<String>> parametersMap = new HashMap<>();

            final Map<String, String[]> decodedParametersMap = new HashMap<>();

            for(String param : parameterList) {
                List<String> splitted = Splitter.on("=").splitToList(param);
                if(splitted.size() != 2) {
                    throw new IllegalArgumentException("The url " + url + " is not valid for param: " + param);
                }
                String key = splitted.get(0);
                List<String> values = parametersMap.get(key);
                if(values == null) {
                    values = new ArrayList<>();
                }
                values.add(splitted.get(1));
                parametersMap.put(key, values);
            }

            for(Map.Entry<String, List<String>> entry : parametersMap.entrySet()) {
                List<String> decodedValues = new ArrayList<>();
                for(String value: entry.getValue()) {
                    decodedValues.add(decodeUrlParam(value));
                }
                decodedParametersMap.put(entry.getKey(), decodedValues.toArray(new String[0]));

            }






            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
            Enumeration<String> parameterNames = Collections.enumeration(parametersMap.keySet());
            Mockito.when(httpServletRequest.getParameterNames()).thenReturn(parameterNames);
            for (String key : parametersMap.keySet()) {
                Mockito.when(httpServletRequest.getParameter(key)).thenReturn(decodedParametersMap.get(key)[0]);
            }

            Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(parameterNames);
            Cookie[] cookies = {};
            Mockito.when(httpServletRequest.getCookies()).thenReturn(cookies);

            Mockito.when(httpServletRequest.getRequestURI()).thenReturn(uri.getPath());

            Mockito.when(httpServletRequest.getParameterMap()).thenReturn(decodedParametersMap);

            return httpServletRequest;

        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeUrlParam(String param) {
        try {
            return URLDecoder.decode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return param;
        }
    }

    public static MockSolrClient createSolrClient(String url) {
        MockSolrClient mockSolrClient = new MockSolrClient(url);
        //mockSolrClient.setRecord(true);
        return mockSolrClient;
    }
}
