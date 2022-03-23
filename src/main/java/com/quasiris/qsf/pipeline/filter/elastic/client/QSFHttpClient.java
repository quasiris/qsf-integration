package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
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
                .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout)).build();

        CloseableHttpClient client =
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(config)
                        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                                .setDefaultSocketConfig(SocketConfig.custom()
                                        .setSoTimeout(Timeout.ofMilliseconds(timeout))
                                        .build())
                                .build())
                        .build();
        return client;

    }


    public <T> T post(String url, Object postData, Class<T> responseValueType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        String postString = object2Json(postData);
        StringEntity entity = new StringEntity(postString, ContentType.parse(charset));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", contentType);

        CloseableHttpClient closeableHttpClient = getHttpClient();
        response = getHttpClient().execute(httpPost);
        T responseObject;
        if(responseValueType.getName().equals(String.class.getName())) {
            try {
                responseObject =  (T) EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new IOException(e);
            }
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
