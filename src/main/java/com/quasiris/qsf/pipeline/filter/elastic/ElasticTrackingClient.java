package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardElasticClient;
import com.quasiris.qsf.dto.response.Document;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticTrackingClient {

    public enum ROTATION {
        DAILY, HOURLY, MONTHLY, YEARLY
    }

    private ROTATION rotation = ROTATION.DAILY;

    private String baseUrl;

    private ElasticClientIF elasticClient;


    public ElasticTrackingClient(String baseUrl, ElasticClientIF elasticClient) {
        this.baseUrl = baseUrl;
        this.elasticClient = elasticClient;
    }

    public ElasticTrackingClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.elasticClient = ElasticClientFactory.getElasticClient();
    }

    private static Map<ROTATION, String> rotationPatterns = new HashMap<>();
    static {
        rotationPatterns.put(ROTATION.DAILY,"yyyy-MM-dd");
        rotationPatterns.put(ROTATION.HOURLY,"yyyy-MM-dd-HH");
        rotationPatterns.put(ROTATION.MONTHLY,"yyyy-MM");
        rotationPatterns.put(ROTATION.YEARLY,"yyyy");
    }

    public void trackDocument(Document tracking) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat(rotationPatterns.get(rotation));
        String datePattern = dateFormat.format(new Date());
        String indexUrl = baseUrl + "_" + datePattern + "/_doc";
        elasticClient.index(indexUrl, tracking.getDocument());
    }


    /**
     * Getter for property 'rotation'.
     *
     * @return Value for property 'rotation'.
     */
    public ROTATION getRotation() {
        return rotation;
    }

    /**
     * Setter for property 'rotation'.
     *
     * @param rotation Value to set for property 'rotation'.
     */
    public void setRotation(ROTATION rotation) {
        this.rotation = rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = ROTATION.valueOf(rotation.toUpperCase());
    }

    /**
     * Getter for property 'baseUrl'.
     *
     * @return Value for property 'baseUrl'.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Setter for property 'baseUrl'.
     *
     * @param baseUrl Value to set for property 'baseUrl'.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
