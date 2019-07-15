package com.quasiris.qsf.pipeline.filter.tracking;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 02.12.17.
 */
public class TrackingFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(TrackingFilter.class);

    private String resultSetId;

    private String idFieldName;

    private String trackingId;

    private Map<String, Object> customParameter = new HashMap<>();

    @Override
    public void init() {
        super.init();
    }


    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        Document tracking = getTracking(pipelineContainer);
        if(trackingId != null) {
            pipelineContainer.putContext(trackingId, tracking);
        }

        return pipelineContainer;
    }

    protected Document getTracking(PipelineContainer pipelineContainer) {

        SearchResult searchResult = pipelineContainer.getSearchResult(resultSetId);
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        HttpServletRequest request = pipelineContainer.getRequest();

        Document tracking = new Document();
        tracking.setValue("timestamp", new Date());
        tracking.setValue("requestId", searchQuery.getRequestId());

        // TODO implement a logic for session and user id
        tracking.setValue("sessionId", searchQuery.getRequestId());
        tracking.setValue("userId", searchQuery.getRequestId());

        tracking.setValue("userAgent", request.getHeader("User-Agent"));
        tracking.setValue("ipAddress", request.getRemoteAddr());
        tracking.setValue("referrer", request.getHeader("Referer"));

        String queryString = request.getQueryString();
        if(!Strings.isNullOrEmpty(queryString)) {
            tracking.setValue("url", request.getRequestURI() + "?" + queryString);
        } else {
            tracking.setValue("url", request.getRequestURI());
        }
        tracking.setValue("httpMethod", request.getMethod());
        tracking.setValue("resultSetId", resultSetId);

        tracking.setValue("q", searchQuery.getQ());
        tracking.setValue("page", searchQuery.getPage());
        tracking.setValue("rows", searchQuery.getRows());

        for(SearchFilter searchFilter : searchQuery.getSearchFilterList()) {
            tracking.addValue("filterId" ,searchFilter.getId());

            if(searchFilter.getValues() != null) {
                for(String value : searchFilter.getValues()) {
                    tracking.addValue("filterValue" , searchFilter.getId() + "=" + value);
                }
            }
        }

        if(searchQuery.getSort() != null) {
            tracking.setValue("sort", searchQuery.getSort().getSort());
        }

        if(searchQuery.getFacetList() != null) {
            for (Facet facet : searchQuery.getFacetList()) {
                tracking.addValue("queryFacet", facet.getId());
            }
        }

        if(idFieldName != null) {
            for (Document document : searchResult.getDocuments()) {
                String id = document.getFieldValue(idFieldName);
                tracking.addValue("docIds", id);
            }
        }

        tracking.setValue("total", searchResult.getTotal());


        tracking.setValue("duration", pipelineContainer.currentTime());

        tracking.getDocument().putAll(customParameter);

        return tracking;
    }

    public void addCustomerParameter(String key, Object value) {
        this.customParameter.put(key, value);
    }

    /**
     * Getter for property 'resultSetId'.
     *
     * @return Value for property 'resultSetId'.
     */
    public String getResultSetId() {
        return resultSetId;
    }

    /**
     * Setter for property 'resultSetId'.
     *
     * @param resultSetId Value to set for property 'resultSetId'.
     */
    public void setResultSetId(String resultSetId) {
        this.resultSetId = resultSetId;
    }


    /**
     * Getter for property 'idFieldName'.
     *
     * @return Value for property 'idFieldName'.
     */
    public String getIdFieldName() {
        return idFieldName;
    }

    /**
     * Setter for property 'idFieldName'.
     *
     * @param idFieldName Value to set for property 'idFieldName'.
     */
    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    /**
     * Getter for property 'trackingId'.
     *
     * @return Value for property 'trackingId'.
     */
    public String getTrackingId() {
        return trackingId;
    }

    /**
     * Setter for property 'trackingId'.
     *
     * @param trackingId Value to set for property 'trackingId'.
     */
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
}
