package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

public class ElasticHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(ElasticHttpClient.class);
    private static Integer ASYNC_TIMEOUT = 100;


    public static String post(String url, String postString) throws IOException {
        return post(url, postString, "application/json");
    }


    public static String delete(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        return execute(httpDelete);
    }

    public static String post(String url, String postString, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(postString, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type",contentType);

        return execute(httpPost);
    }

    public static String execute(HttpUriRequest request) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        StringBuilder responseBuilder = new StringBuilder();

        responseBuilder.append(EntityUtils.toString(response.getEntity()));
        httpclient.close();

        if (response.getStatusLine().getStatusCode() < 300) {
            return responseBuilder.toString();
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), responseBuilder.toString());
        }
    }


    public static void postAsync(String url, String postString) {
        postAsync(url, postString, "application/json");
    }

    public static void postAsync(String url, String postString, String contentType) {
        org.apache.hc.client5.http.config.RequestConfig config = org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT)).build();
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setSelectInterval(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setSoTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .build();
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
            .setIOReactorConfig(reactorConfig)
            .setDefaultRequestConfig(config)
            .build();
        client.start();

        final SimpleHttpRequest request = SimpleRequestBuilder.post(url)
                .setBody(postString, ContentType.parse(contentType))
                .setCharset(StandardCharsets.UTF_8)
                .build();

        Future<SimpleHttpResponse> future = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse simpleHttpResponse) {
                LOG.debug("The async request finished successful with code: "+simpleHttpResponse.getCode());
            }

            @Override
            public void failed(Exception e) {
                LOG.error("The async request failed because " + e.getMessage(), e);
            }

            @Override
            public void cancelled() {
                LOG.error("The async request was canceled.");
            }
        });

        try {
            future.get();
        } catch (Exception e) {
            LOG.error("Error while get future: "+e.getMessage(), e);
        }

        client.close(CloseMode.GRACEFUL);
    }
}
