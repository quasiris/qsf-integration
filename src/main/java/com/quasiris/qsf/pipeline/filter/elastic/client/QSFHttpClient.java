package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class QSFHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(QSFHttpClient.class);

    private int timeout = 4000;

    private String contentType = "application/json";

    private String charset = "UTF-8";

    ObjectMapper objectMapper = new ObjectMapper();


    private CloseableHttpResponse response;

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout).build();

        CloseableHttpClient client =
                HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        return client;

    }


    public <T> T post(String url, Object postData, Class<T> responseValueType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        String postString = object2Json(postData);
        StringEntity entity = new StringEntity(postString, charset);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", contentType);

        CloseableHttpClient closeableHttpClient = getHttpClient();
        response = getHttpClient().execute(httpPost);
        T responseObject;
        if(responseValueType.getName().equals(String.class.getName())) {
            responseObject =  (T) EntityUtils.toString(response.getEntity());
        } else {
            responseObject = readResponse(response.getEntity().getContent(), responseValueType);
        }
        closeableHttpClient.close();
        return responseObject;
    }


    private <T> T readResponse(InputStream inputStream, Class<T> valueType) throws IOException {
        return objectMapper.readValue(inputStream, valueType);
    }

    private String object2Json(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
