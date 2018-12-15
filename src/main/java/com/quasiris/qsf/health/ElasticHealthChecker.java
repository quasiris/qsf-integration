package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticFilterBuilder;
import com.quasiris.qsf.response.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ElasticHealthChecker {

    private String baseUrl;
    private String profile = "classpath://com/quasiris/qsf/elastic/profiles/health-profile.json";

    private String type = "health";

    private long minTotal = 1;

    List<Predicate<SearchResult>> predicates = new ArrayList<>();


    public static Builder create() {
        return new Builder();
    }

    public static class Builder {

        private ElasticHealthChecker solrHealthChecker = new ElasticHealthChecker();

        public ElasticHealthChecker build() {
            predicate(sr -> sr.getTotal() > solrHealthChecker.getMinTotal());
            return solrHealthChecker;

        }
        public Builder baseUrl(String baseUrl) {
            solrHealthChecker.setBaseUrl(baseUrl);
            return this;
        }


        public Builder predicate(Predicate<SearchResult> predicate) {
            solrHealthChecker.getPredicates().add(predicate);
            return this;
        }

        public Builder profile(String profile) {
            solrHealthChecker.setProfile(profile);
            return this;
        }

        public Builder type(String type) {
            solrHealthChecker.setType(type);
            return this;
        }

        public Builder minTotal(long minTotal) {
            solrHealthChecker.setMinTotal(minTotal);
            return this;
        }

    }



    public boolean isHealthy() throws PipelineContainerException, PipelineContainerDebugException {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline(type).
                timeout(4000L).
                filter(ElasticFilterBuilder.create().
                        baseUrl(baseUrl).
                        profile(profile).
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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
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
