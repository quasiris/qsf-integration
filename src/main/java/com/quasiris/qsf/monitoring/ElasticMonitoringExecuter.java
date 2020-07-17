package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticFilterBuilder;
import com.quasiris.qsf.pipeline.filter.elastic.Profiles;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.MonitoringResponse;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A executer for the elastic monitoring.
 * Create a elastic query, execute the query and check the results against the defined monitoring documents.
 *
 */
public class ElasticMonitoringExecuter {

    /**
     * Create a new ElasticMonitoringExecuter.
     * @param baseUrl The base url of the elastic index. E.g. http://localhost:9200/qsf-index
     * @param monitoringDocumentList A list of monitorings to check.
     */
    public ElasticMonitoringExecuter(String baseUrl, List<MonitoringDocument> monitoringDocumentList) {
        this.baseUrl = baseUrl;
        this.monitoringDocumentList = monitoringDocumentList;
        this.query = "*";
    }

    private String baseUrl;
    private String query;

    private String profile = Profiles.queryString();

    private List<MonitoringDocument> monitoringDocumentList;

    private Long timeout = 4000L;

    private String status = "OK";

    private int aggSize = 100;

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
     * Getter for property 'profile'.
     *
     * @return Value for property 'profile'.
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Setter for property 'profile'.
     *
     * @param profile Value to set for property 'profile'.
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * Getter for property 'monitoringDocumentList'.
     *
     * @return Value for property 'monitoringDocumentList'.
     */
    public List<MonitoringDocument> getMonitoringDocumentList() {
        return monitoringDocumentList;
    }

    /**
     * Setter for property 'monitoringDocumentList'.
     *
     * @param monitoringDocumentList Value to set for property 'monitoringDocumentList'.
     */
    public void setMonitoringDocumentList(List<MonitoringDocument> monitoringDocumentList) {
        this.monitoringDocumentList = monitoringDocumentList;
    }

    /**
     * Getter for property 'timeout'.
     *
     * @return Value for property 'timeout'.
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Setter for property 'timeout'.
     *
     * @param timeout Value to set for property 'timeout'.
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * Getter for property 'query'.
     *
     * @return Value for property 'query'.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Setter for property 'query'.
     *
     * @param query Value to set for property 'query'.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Run the monitoring.
     * Create a elastic query, executes the query and check the results.
     *
     * @return the {@link MonitoringResponse}
     * @throws PipelineContainerDebugException in case of debugging
     */
    public MonitoringResponse doMonitoring( ) throws PipelineContainerDebugException {
        String type = "health";
        ElasticFilterBuilder elasticFilterBuilder = ElasticFilterBuilder.create().
                resultSetId(type).
                baseUrl(baseUrl).
                profile(profile).
                defaultRows(1);

        for(MonitoringDocument monitoringDocument : monitoringDocumentList) {
            if(monitoringDocument.getType().equals("document")) {
                elasticFilterBuilder.
                        mapField(monitoringDocument.getFieldName(), monitoringDocument.getFieldName());
                elasticFilterBuilder.
                        mapSort(monitoringDocument.getFieldName(), "[{\"" + monitoringDocument.getFieldName() + "\": {\"order\": \"asc\"}}]").
                        defaultSort(monitoringDocument.getFieldName());
            }


            if(monitoringDocument.getType().equals("facet")) {
                elasticFilterBuilder.
                        addAggregation(monitoringDocument.getFieldName(), monitoringDocument.getFieldName(), aggSize);
            }
        }

        SearchResult searchResult = null;
        try {
            Pipeline pipeline = PipelineBuilder.create().
                    pipeline(type).
                    timeout(timeout).
                    filter(elasticFilterBuilder.
                            build()).
                    build();

            SearchQuery searchQuery = new SearchQuery();
            searchQuery.setQ(this.query);

            PipelineContainer pipelineContainer = PipelineExecuter.create().
                    pipeline(pipeline).
                    searchQuery(searchQuery).
                    execute();

            searchResult = pipelineContainer.getSearchResults().get(type);
        } catch (PipelineContainerException e) {
            searchResult = new SearchResult();
            searchResult.setDocuments(new ArrayList<>());
            searchResult.setTotal(0L);
            searchResult.setTime(e.getPipelineContainer().currentTime());
            searchResult.setStatusMessage(e.getMessage());
        }


        SearchResult monitoring = new SearchResult();
        monitoring.setTime(searchResult.getTime());
        for(MonitoringDocument monitoringDocument : monitoringDocumentList) {
            if(monitoringDocument.getType().equals("total")) {
                monitoringDocument.setValue(searchResult.getTotal());
            }

            if(monitoringDocument.getType().equals("document")) {
                Document document = searchResult.getDocuments().stream().findFirst().orElse(null);
                if(document != null && monitoringDocument.getDataType().equals("Instant")) {

                    monitoringDocument.setValue(
                            DateUtil.getInstantByElasticDate(
                                    document.getFieldValue(monitoringDocument.getFieldName())
                            )
                    );
                }
            }

            if(monitoringDocument.getType().equals("facet")) {
                Facet facet = searchResult.getFacetById(monitoringDocument.getFieldName());
                if(facet != null) {
                    Long count = facet.getFacetCountByValue(monitoringDocument.getFacetValue());
                    monitoringDocument.setValue(count);
                }
            }
            monitoringDocument.check();
            monitoringDocument.setMessage(searchResult.getStatusMessage());
            monitoringDocument.setValue("baseUrl", baseUrl);
            monitoringDocument.setValue("query", query);
            setStatus(monitoringDocument.getStatus());
            monitoring.addDocument(monitoringDocument);
        }
        return MonitoringResponse.create(monitoring, status);
    }

    /**
     * Set a global status for all monitoring documents.
     * The worst status is set. That menas if one document is in status ERROR, the whole status is also error.
     *
     * @param status Value to set for property 'status'.
     */
    private void setStatus(String status) {
        this.status = MonitoringStatus.computeStatus(this.status, status);
    }
}
