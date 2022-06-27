package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

@Deprecated
public class ElasticHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(ElasticHttpClient.class);
    private static Integer ASYNC_TIMEOUT = 1000;


    public static String post(String url, String postString) throws IOException {
        return post(url, postString, ContentType.APPLICATION_JSON.toString());
    }


    public static String delete(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        return execute(httpDelete);
    }

    public static String post(String url, String postString, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(postString, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type",contentType);

        return execute(httpPost);
    }

    public static String execute(HttpUriRequest request) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        StringBuilder responseBuilder = new StringBuilder();

        try {
            responseBuilder.append(EntityUtils.toString(response.getEntity()));
        } catch (ParseException e) {
            throw new IOException("Could not parse response body!", e);
        }
        httpclient.close();

        if (response.getCode() < 300) {
            return responseBuilder.toString();
        } else {
            throw new HttpResponseException(response.getCode(), responseBuilder.toString());
        }
    }


    @Deprecated
    public static void postAsync(String url, String postString) {
        postAsync(url, postString, ContentType.APPLICATION_JSON.toString());
    }

    @Deprecated
    public static void postAsync(String url, String postString, String contentType) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT)).build();
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setSelectInterval(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .setSoTimeout(Timeout.ofMilliseconds(ASYNC_TIMEOUT))
                .build();
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_1)
                .setIOReactorConfig(reactorConfig)
                .setDefaultRequestConfig(config)
                .build();
        try {
            client.start();
            final SimpleHttpRequest request = SimpleRequestBuilder.post(url)
                    .setBody(postString, ContentType.parse(contentType))
                    .setCharset(StandardCharsets.UTF_8)
                    .build();

            Future<SimpleHttpResponse> future = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse simpleHttpResponse) {
                    LOG.debug("The async request finished successful with code: " + simpleHttpResponse.getCode());
                    if(simpleHttpResponse.getCode() > 300) {
                        LOG.warn("Status code is not 20x for url "+url+" body: "+simpleHttpResponse.getBody().getBodyText());
                    }
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
        } finally {
            client.close(CloseMode.GRACEFUL);
        }
    }
}
