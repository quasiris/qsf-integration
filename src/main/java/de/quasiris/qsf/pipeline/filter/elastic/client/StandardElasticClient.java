package de.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by mki on 16.12.17.
 */
public class StandardElasticClient implements  ElasticClientIF {
    private static Logger LOG = LoggerFactory.getLogger(StandardElasticClient.class);

    public ElasticResult request(String elasticUrl, String request) throws IOException {
        LOG.debug("elastic request: " + request);
        String response = post(elasticUrl, request);
        ObjectMapper objectMapper = new ObjectMapper();
        ElasticResult elasticResult = objectMapper.readValue(response, ElasticResult.class);
        return elasticResult;
    }


    public String post(String url, String postString) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(postString, "UTF-8");
        httpPost.setEntity(entity);
        // httpPost.setHeader("Content-Type",contentType);

        CloseableHttpResponse response = httpclient.execute(httpPost);
        StringBuilder responseBuilder = new StringBuilder();

        responseBuilder.append(EntityUtils.toString(response.getEntity()));
        httpclient.close();
        return responseBuilder.toString();
    }
}
