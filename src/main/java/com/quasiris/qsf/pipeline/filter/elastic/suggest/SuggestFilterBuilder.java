package com.quasiris.qsf.pipeline.filter.elastic.suggest;

import com.quasiris.qsf.pipeline.filter.elastic.Elastic2SearchResultMappingTransformer;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticFilter;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;

import java.util.List;

/**
 * Created by tbl on 5.09.19.
 */
public class SuggestFilterBuilder {

    private ElasticFilter elasticFilter;

    private List<String> suggestFields;

    private SuggestQueryTransoformer suggestQueryTransoformer;

    public static SuggestFilterBuilder create() {
        SuggestFilterBuilder builder = new SuggestFilterBuilder();
        builder.elasticFilter = new ElasticFilter();
        builder.suggestQueryTransoformer = new SuggestQueryTransoformer();
        return builder;
    }


    public SuggestFilterBuilder baseUrl(String baseUrl) {
        elasticFilter.setBaseUrl(baseUrl);
        return this;
    }

    public SuggestFilterBuilder suggestFields(List<String> suggestFields) {
        this.suggestFields = suggestFields;
        this.suggestQueryTransoformer.setSuggestFields(suggestFields);
        return this;
    }

    public SuggestFilterBuilder id(String id) {
        elasticFilter.setId(id);
        return this;
    }


    public SuggestFilterBuilder resultSetId(String resultSetId) {
        elasticFilter.setResultSetId(resultSetId);
        return this;
    }

    public ElasticFilter build() {

        elasticFilter.setQueryTransformer(suggestQueryTransoformer);
        elasticFilter.setSearchResultTransformer(new Elastic2SearchResultMappingTransformer());
        return elasticFilter;
    }

    public SuggestFilterBuilder disable() {
        elasticFilter.setActive(false);
        return this;
    }

    public SuggestFilterBuilder profile(String profile) {
        suggestQueryTransoformer.setProfile(profile);
        return this;
    }

    public SuggestFilterBuilder matchAllProfile(String matchAllProfile) {
        suggestQueryTransoformer.setMatchAllProfile(matchAllProfile);
        return this;
    }

    public SuggestFilterBuilder profileParameter(String key, String value) {
        suggestQueryTransoformer.setProfileParameter(key, value);
        return this;
    }


    public SuggestFilterBuilder client(ElasticClientIF elasticClient) {
        elasticFilter.setElasticClient(elasticClient);
        return this;
    }

}
