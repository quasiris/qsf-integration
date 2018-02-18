package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;

/**
 * Created by mki on 10.02.18.
 */
public class ElasticFilterBuilder {

    private ElasticFilter elasticFilter;

    private Elastic2SearchResultMappingTransformer mappingTransformer;

    private ElasticParameterQueryTransformer elasticParameterQueryTransformer;

    private ElasticQsfqlQueryTransformer elasticQsfqlQueryTransformer;

    private Class<?> queryTransformer = ElasticQsfqlQueryTransformer.class;

    public static ElasticFilterBuilder create() {
        ElasticFilterBuilder builder = new ElasticFilterBuilder();
        builder.elasticFilter = new ElasticFilter();
        return builder;
    }


    public ElasticFilterBuilder baseUrl(String baseUrl) {
        elasticFilter.setBaseUrl(baseUrl);
        return this;
    }

    public ElasticFilterBuilder id(String id) {
        elasticFilter.setId(id);
        return this;
    }


    public ElasticFilterBuilder resultSetId(String resultSetId) {
        elasticFilter.setResultSetId(resultSetId);
        return this;
    }


    public ElasticFilterBuilder elasticVersion(int elasticVersion) {
        getElasticQsfqlQueryTransformer().setElasticVersion(elasticVersion);
        return this;
    }

    public ElasticFilter build() {

        if(queryTransformer.equals(ElasticQsfqlQueryTransformer.class)) {
            elasticFilter.setQueryTransformer(elasticQsfqlQueryTransformer);
        } else if(queryTransformer.equals(ElasticParameterQueryTransformer.class)) {
            elasticFilter.setQueryTransformer(elasticParameterQueryTransformer);
        }

        if(mappingTransformer != null) {
            elasticFilter.setSearchResultTransformer(mappingTransformer);
        } else {
            elasticFilter.setSearchResultTransformer(new Elastic2SearchResultMappingTransformer());
        }
        return elasticFilter;
    }

    private Elastic2SearchResultMappingTransformer getMappingTransformer() {
        if(mappingTransformer == null) {
            mappingTransformer = new Elastic2SearchResultMappingTransformer();
        }
        return mappingTransformer;
    }

    private ElasticParameterQueryTransformer getElasticParameterQueryTransformer() {
        if(elasticParameterQueryTransformer == null) {
            elasticParameterQueryTransformer = new ElasticParameterQueryTransformer();
        }
        return elasticParameterQueryTransformer;
    }

    public ElasticQsfqlQueryTransformer getElasticQsfqlQueryTransformer() {
        if(elasticQsfqlQueryTransformer == null) {
            elasticQsfqlQueryTransformer = new ElasticQsfqlQueryTransformer();
        }
        return elasticQsfqlQueryTransformer;
    }

    public void setElasticQsfqlQueryTransformer(ElasticQsfqlQueryTransformer elasticQsfqlQueryTransformer) {
        this.elasticQsfqlQueryTransformer = elasticQsfqlQueryTransformer;
    }

    public ElasticFilterBuilder mapField(String from, String to) {
        //getMappingTransformer().addFieldMapping(from, to);
        //getElasticParameterQueryTransformer().addFieldListValue(from);
        return this;
    }

    public ElasticFilterBuilder addAggregation(String name, String field) {
        getElasticParameterQueryTransformer().addAggregation(name, field);
        getElasticQsfqlQueryTransformer().addAggregation(name, field);
        return this;
    }

    public ElasticFilterBuilder mapAggregation(String from, String to) {
        //getMappingTransformer().addFacetMapping(from, to);
        return this;
    }
    public ElasticFilterBuilder mapAggregationName(String from, String to) {
        //getMappingTransformer().addFacetNameMapping(from, to);
        return this;
    }


    public ElasticFilterBuilder mapFilter(String from, String to) {
        getElasticQsfqlQueryTransformer().addFilterMapping(from, to);
        return this;
    }

    public ElasticFilterBuilder queryTransformer(QueryTransformerIF queryTransformer) {
        if( queryTransformer instanceof ElasticQsfqlQueryTransformer) {
            this.elasticQsfqlQueryTransformer = (ElasticQsfqlQueryTransformer) queryTransformer;
        } else if(queryTransformer instanceof ElasticParameterQueryTransformer) {
            this.elasticParameterQueryTransformer = (ElasticParameterQueryTransformer) queryTransformer;
        } else {
            throw new IllegalArgumentException("The query transformer " + queryTransformer.getClass().getName() + " is not supported.");
        }

        return this;
    }

    public ElasticFilterBuilder queryTransformer(Class<?> queryTransformer) {
        this.queryTransformer = queryTransformer;

        return this;
    }

    public ElasticFilterBuilder disable() {
        elasticFilter.setActive(false);
        return this;
    }

    public ElasticFilterBuilder profile(String profile) {
        getElasticQsfqlQueryTransformer().setProfile(profile);
        getElasticParameterQueryTransformer().setProfile(profile);
        return this;
    }

    public ElasticFilterBuilder resultField(String resultFieldName, String value) {
        //getMappingTransformer().addResultField(resultFieldName, value);
        return this;
    }

    public ElasticFilterBuilder client(ElasticClientIF elasticClient) {
        elasticFilter.setElasticClient(elasticClient);
        return this;
    }

    public ElasticFilterBuilder filterPrefix(String filterPrefix) {
        //getMappingTransformer().filterPrefix(filterPrefix);
        return this;
    }

    public ElasticFilterBuilder mapSort(String from, String to) {
        getElasticQsfqlQueryTransformer().addSortMapping(from, to);
        return this;
    }

    public ElasticFilterBuilder defaultSort(String defaultSort) {
        getElasticQsfqlQueryTransformer().setDefaultSort(defaultSort);
        return this;
    }


}
