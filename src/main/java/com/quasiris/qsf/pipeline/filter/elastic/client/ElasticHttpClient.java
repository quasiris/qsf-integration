package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ElasticHttpClient {


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
}
