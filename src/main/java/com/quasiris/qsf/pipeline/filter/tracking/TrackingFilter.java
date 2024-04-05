package com.quasiris.qsf.pipeline.filter.tracking;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.BoolSearchFilter;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by mki on 02.12.17.
 */
public class TrackingFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(TrackingFilter.class);

    private String resultSetId;

    private String idFieldName;

    private String trackingId;

    private Map<String, Object> customParameter = new HashMap<>();

    private Set<String> singleValueParameters = new HashSet<>();

    @Override
    public void init() {
        super.init();
        singleValueParameters.add("id");
        singleValueParameters.add("timestamp");
        singleValueParameters.add("requestId");
        singleValueParameters.add("requestOrigin");
        singleValueParameters.add("sessionId");
        singleValueParameters.add("userId");
        singleValueParameters.add("q");
        singleValueParameters.add("queryChanged");
        singleValueParameters.add("queryTokenCount");
        singleValueParameters.add("page");
        singleValueParameters.add("rows");
        singleValueParameters.add("sort");
        singleValueParameters.add("total");
        singleValueParameters.add("duration");
        singleValueParameters.add("userAgent");
        singleValueParameters.add("ipAddress");
        singleValueParameters.add("referrer");
        singleValueParameters.add("url");
        singleValueParameters.add("httpMethod");
    }


    public boolean isTrackingEnabled(PipelineContainer pipelineContainer) {
        if(Boolean.FALSE.equals(pipelineContainer.getSearchQuery().getTracking())) {
            return false;
        }
        return true;
    }


    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        if(!isTrackingEnabled(pipelineContainer)) {
            return pipelineContainer;
        }
        Document tracking = getTracking(pipelineContainer);
        if(trackingId != null) {
            pipelineContainer.putContext(trackingId, tracking);
        }

        return pipelineContainer;
    }

    private void addTrackingValues(Document tracking, List<BaseSearchFilter> searchFilters) {
        if(searchFilters != null) {
            for (BaseSearchFilter baseSearchFilter : searchFilters) {
                if (baseSearchFilter instanceof SearchFilter) {
                    SearchFilter searchFilter = (SearchFilter) baseSearchFilter;
                    tracking.addValue("filterId", searchFilter.getId());

                    if (searchFilter.getValues() != null) {
                        for (String value : searchFilter.getValues()) {
                            tracking.addValue("filterValue", searchFilter.getId() + "=" + value);
                        }
                    }

                    if (searchFilter.getMinValue() != null || searchFilter.getMaxValue() != null) {
                        tracking.addValue("filterValue", searchFilter.getId() + "=" + searchFilter.getMinValue() + "," + searchFilter.getMaxValue());
                    }
                } else if (baseSearchFilter instanceof BoolSearchFilter) {
                    BoolSearchFilter searchFilter = (BoolSearchFilter) baseSearchFilter;
                    addTrackingValues(tracking, searchFilter.getFilters());
                }
            }
        }
    }

    protected Document getTracking(PipelineContainer pipelineContainer) {

        Document tracking = new Document();



        tracking.setValue("id", UUID.randomUUID().toString());
        tracking.setValue("timestamp", new Date());
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        if(searchQuery == null) {
            return tracking;
        }
        tracking.setValue("requestId", searchQuery.getRequestId());
        tracking.setValue("requestOrigin", searchQuery.getRequestOrigin());

        // TODO implement a logic for session and user id
        tracking.setValue("sessionId", searchQuery.getRequestId());
        tracking.setValue("userId", searchQuery.getRequestId());

        tracking = trackRequestParameter(pipelineContainer, tracking);

        tracking.setValue("resultSetId", resultSetId);

        if(searchQuery.getOriginalQuery() != null) {
            tracking.setValue("q", searchQuery.getOriginalQuery());
            tracking.addValue("changedQuery", searchQuery.getQ());
            tracking.setValue("queryChanged", true);
            tracking.setValue("queryChangedReasons", searchQuery.getQueryChangedReasons());
        } else {
            tracking.setValue("q", searchQuery.getQ());
        }

        if(searchQuery.getQ() != null) {
            tracking.setValue("queryTokenCount", searchQuery.getQ().split(" ").length);
        }
        tracking.setValue("page", searchQuery.getPage());
        tracking.setValue("rows", searchQuery.getRows());

        addTrackingValues(tracking, searchQuery.getSearchFilterList());

        if(searchQuery.getSort() != null) {
            tracking.setValue("sort", searchQuery.getSort().getSort());
        }

        if(searchQuery.getFacetList() != null) {
            for (Facet facet : searchQuery.getFacetList()) {
                tracking.addValue("queryFacet", facet.getId());
            }
        }

        if(searchQuery.getTrackingTags() != null) {
            tracking.addValue("tags", searchQuery.getTrackingTags());
        }

        tracking = trackSearchResult(pipelineContainer, tracking);




        tracking.getDocument().putAll(customParameter);

        Document t = pipelineContainer.getTracking();
        for(Map.Entry<String, Object> entry : t.getDocument().entrySet()) {
            String value = tracking.getFieldValue(entry.getKey());
            if(value == null) {
                tracking.setValue(entry.getKey(), entry.getValue());
            } else if (singleValueParameters.contains(entry.getKey())) {
                tracking.setValue(entry.getKey(), entry.getValue());
            } else {
                tracking.addValue(entry.getKey(), entry.getValue());
            }

        }


        return tracking;
    }


    protected Document trackSearchResult(PipelineContainer pipelineContainer, Document tracking) {
        SearchResult searchResult = pipelineContainer.getSearchResult(resultSetId);
        if(searchResult == null) {
            return tracking;
        }

        tracking.setValue("total", searchResult.getTotal());
        tracking.setValue("duration", pipelineContainer.currentTime());

        List<String> docIds = new ArrayList<>();


        for (Document document : searchResult.getDocuments()) {
            String id = null;
            if(idFieldName != null) {
                id = document.getFieldValue(idFieldName);
            }
            if(id == null) {
                id = document.getId();
            }
            docIds.add(id);
        }
        tracking.setValue("docIds", docIds);

        return tracking;
    }

    protected Document trackRequestParameter(PipelineContainer pipelineContainer, Document tracking) {
        HttpServletRequest request = pipelineContainer.getRequest();
        if(request == null) {
            return tracking;
        }

        tracking.setValue("userAgent", request.getHeader("User-Agent"));
        tracking.setValue("ipAddress", request.getRemoteAddr());
        tracking.setValue("referrer", request.getHeader("Referer"));

        String queryString = request.getQueryString();
        if (!Strings.isNullOrEmpty(queryString)) {
            tracking.setValue("url", request.getRequestURI() + "?" + queryString);
        } else {
            tracking.setValue("url", request.getRequestURI());
        }
        tracking.setValue("httpMethod", request.getMethod());

        return tracking;

    }

    public void addCustomerParameter(String key, Object value) {
        this.customParameter.put(key, value);
    }

    public Map<String, Object> getCustomParameter() {
        return customParameter;
    }

    public void setCustomParameter(Map<String, Object> customParameter) {
        this.customParameter = customParameter;
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
