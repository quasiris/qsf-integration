package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(ElasticHttpClient.class);


    public static String post(String url, String postString) throws IOException {
        return post(url, postString, "application/json");
    }


    public static String post(String url, String postString, String contentType) throws IOException {
        //System.out.println(postString);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(postString, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type",contentType);

        CloseableHttpResponse response = httpclient.execute(httpPost);
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
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
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
