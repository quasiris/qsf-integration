package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.solr.SolrFilterBuilder;
import com.quasiris.qsf.response.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SolrHealthChecker {


    private String baseUrl;

    private String type = "health";

    private long minTotal = 1;

    List<Predicate<SearchResult>> predicates = new ArrayList<>();


    public static SolrHealthChecker.Builder create() {
        return new SolrHealthChecker.Builder();
    }

    public static class Builder {

        private SolrHealthChecker solrHealthChecker = new SolrHealthChecker();

        public SolrHealthChecker build() {
            predicate(sr -> sr.getTotal() > solrHealthChecker.getMinTotal());
            return solrHealthChecker;

        }
        public SolrHealthChecker.Builder baseUrl(String baseUrl) {
            solrHealthChecker.setBaseUrl(baseUrl);
            return this;
        }


        public SolrHealthChecker.Builder predicate(Predicate<SearchResult> predicate) {
            solrHealthChecker.getPredicates().add(predicate);
            return this;
        }

        public SolrHealthChecker.Builder type(String type) {
            solrHealthChecker.setType(type);
            return this;
        }

        public SolrHealthChecker.Builder minTotal(long minTotal) {
            solrHealthChecker.setMinTotal(minTotal);
            return this;
        }

    }



    public boolean isHealthy() throws PipelineContainerException, PipelineContainerDebugException {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline(type).
                timeout(4000L).
                filter(SolrFilterBuilder.create().
                        baseUrl(baseUrl).
                        param("q", "*:*").
                        param("rows", "1").
                        resultSetId(type).
                        build()).
                build();
        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        SearchResult searchResult = pipelineContainer.getSearchResults().get(type);
        for(Predicate<SearchResult> p : getPredicates()) {
            if(!p.test(searchResult)) {
                return false;
            }
        }
        return true;

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getMinTotal() {
        return minTotal;
    }

    public void setMinTotal(long minTotal) {
        this.minTotal = minTotal;
    }

    public List<Predicate<SearchResult>> getPredicates() {
        return predicates;
    }
}
