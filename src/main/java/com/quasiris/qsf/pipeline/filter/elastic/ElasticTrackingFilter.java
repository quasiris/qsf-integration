package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.tracking.TrackingFilter;
import com.quasiris.qsf.response.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticTrackingFilter extends TrackingFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticTrackingFilter.class);



    private String rotation = "daily";

    private String baseUrl;

    private ElasticTrackingClient elasticTrackingClient;

    @Override
    public void init() {
        super.init();

        if(this.elasticTrackingClient == null) {
            this.elasticTrackingClient = new ElasticTrackingClient(baseUrl);
            this.elasticTrackingClient.setRotation(rotation);
        }
    }


    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        if(!isTrackingEnabled(pipelineContainer)) {
            return pipelineContainer;
        }

        Document tracking = getTracking(pipelineContainer);
        elasticTrackingClient.trackDocument(tracking);

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
