package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 11.11.16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult{

    private Object rawResponse;

    private Integer statusCode;

    private String name;

    private String statusMessage;

    private List<Document> documents;

    private Long time;

    private String debug;

    private Long total;

    private Float maxScore;

    private List<Facet> facets;

    private Integer facetCount;


    private Paging paging;

    private Map<String, Object> context;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Object getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(Object rawResponse) {
        this.rawResponse = rawResponse;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public void debug(String message) {
        if(this.debug == null) {
            this.debug = message;
            return;
        }

        this.debug = " " + message;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Float maxScore) {
        this.maxScore = maxScore;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }


    public void addDocument(Document document) {
        if(this.documents == null) {
            this.documents = new ArrayList<>();
        }
        getDocuments().add(document);
    }

    public void addFacet(Facet facet) {
        if(this.facets == null) {
            this.facets = new ArrayList<>();
        }
        getFacets().add(facet);
    }


    public Facet getFacetById(String id) {
        if(getFacets() == null) {
            return null;
        }
        for(Facet facet : getFacets()) {
            if(id.equals(facet.getId())) {
                return facet;
            }
        }
        return null;
    }

    public Integer getFacetCount() {
        return facetCount;
    }

    public void setFacetCount(Integer facetCount) {
        this.facetCount = facetCount;
    }


    public void initDocuments() {
        this.documents = new ArrayList<>();
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public <T> T getContext(String name, Class<T> clazz) {
        return (T) context.get(name);
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void putContext(String name, Object value) {
        if(context == null) {
            context = new HashMap<>();
        }
        context.put(name, value);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "rawResponse=" + rawResponse +
                ", statusCode=" + statusCode +
                ", name='" + name + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", documents=" + documents +
                ", time=" + time +
                ", debug='" + debug + '\'' +
                ", total=" + total +
                ", maxScore=" + maxScore +
                ", facets=" + facets +
                ", facetCount=" + facetCount +
                ", paging=" + paging +
                '}';
    }
}
