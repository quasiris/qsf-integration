package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.pipeline.filter.tracking.TrackingFilter;
import com.quasiris.qsf.response.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticTrackingFilter extends TrackingFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticTrackingFilter.class);

    private static Map<String, String> rotationPatterns = new HashMap<>();
    static {
        rotationPatterns.put("daily","yyyy-MM-dd");
        rotationPatterns.put("hourly","yyyy-MM-dd-HH");
        rotationPatterns.put("monthly","yyyy-MM");
        rotationPatterns.put("yearly","yyyy");
    }

    private String rotation = "daily";

    private String baseUrl;

    private ElasticClientIF elasticClient;

    @Override
    public void init() {
        super.init();
        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getElasticClient();
        }
    }


    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {

        Document tracking = getTracking(pipelineContainer);
        DateFormat dateFormat = new SimpleDateFormat(rotationPatterns.get(rotation));
        String datePattern = dateFormat.format(new Date());
        String indexUrl = baseUrl + "_" + datePattern + "/_doc";
        elasticClient.index(indexUrl, tracking.getDocument());

        return pipelineContainer;
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


    /**
     * Getter for property 'elasticClient'.
     *
     * @return Value for property 'elasticClient'.
     */
    public ElasticClientIF getElasticClient() {
        return elasticClient;
    }

    /**
     * Setter for property 'elasticClient'.
     *
     * @param elasticClient Value to set for property 'elasticClient'.
     */
    public void setElasticClient(ElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }

    /**
     * Getter for property 'rotation'.
     *
     * @return Value for property 'rotation'.
     */
    public String getRotation() {
        return rotation;
    }

    /**
     * Setter for property 'rotation'.
     *
     * @param rotation Value to set for property 'rotation'.
     */
    public void setRotation(String rotation) {
        this.rotation = rotation;
    }
}
