package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        IOReactorConfig httpConfig = IOReactorConfig.custom()
                .setSelectInterval(ASYNC_TIMEOUT)
                .setSoTimeout(ASYNC_TIMEOUT)
                .setConnectTimeout(ASYNC_TIMEOUT)
                .build();
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
            .setDefaultIOReactorConfig(httpConfig)
            .build();
        client.start();
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(postString, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type",contentType);


        client.execute(httpPost, new FutureCallback<HttpResponse>() {
            public void failed(final Exception e) {
                LOG.error("The async request failed because " + e.getMessage(), e);
                try {
                    client.close();
                } catch (IOException ex) {
                    LOG.error("Could not close async http client", ex);
                }
            }

            public void completed(final HttpResponse httpResponse) {
                LOG.debug("The async request finished successful with code: "
                        + httpResponse.getStatusLine().getStatusCode());
                try {
                    client.close();
                } catch (IOException ex) {
                    LOG.error("Could not close async http client", ex);
                }
            }

            public void cancelled() {
                LOG.error("The async request was canceled.");
                try {
                    client.close();
                } catch (IOException ex) {
                    LOG.error("Could not close async http client", ex);
                }
            }
        });
    }
}
