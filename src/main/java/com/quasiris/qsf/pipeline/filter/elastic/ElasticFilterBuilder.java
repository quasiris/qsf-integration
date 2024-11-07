package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;
import com.quasiris.qsf.config.DisplayDTO;
import com.quasiris.qsf.config.DisplayMappingDTO;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.config.QsfSearchConfigUtil;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.pipeline.filter.mapper.FacetFilterMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;
import com.quasiris.qsf.query.Facet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mki on 10.02.18.
 */
public class ElasticFilterBuilder {

    private ElasticFilter elasticFilter;

    private Elastic2SearchResultMappingTransformer elastic2SearchResultMappingTransformer;

    private SearchResultTransformerIF searchResultTransformer;

    private ElasticParameterQueryTransformer elasticParameterQueryTransformer;

    private ElasticQsfqlQueryTransformer elasticQsfqlQueryTransformer;

    private Class<?> queryTransformer = ElasticQsfqlQueryTransformer.class;

    private QsfSearchConfigDTO searchConfig = new QsfSearchConfigDTO();

    //private Set<String> sourceFieldExcludes = new HashSet<>();

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
            elasticQsfqlQueryTransformer.setSearchConfig(searchConfig);
            elasticFilter.setQueryTransformer(elasticQsfqlQueryTransformer);
        } else if(queryTransformer.equals(ElasticParameterQueryTransformer.class)) {
            elasticParameterQueryTransformer.setSearchConfig(searchConfig);
            elasticFilter.setQueryTransformer(elasticParameterQueryTransformer);
        } else {
            try {
                elasticFilter.setQueryTransformer((QueryTransformerIF) queryTransformer.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(searchResultTransformer != null) {
            elasticFilter.setSearchResultTransformer(searchResultTransformer);
        } else {
            elasticFilter.setSearchResultTransformer(getMappingTransformer());
        }
        return elasticFilter;
    }

    private Elastic2SearchResultMappingTransformer getMappingTransformer() {
        if(elastic2SearchResultMappingTransformer == null) {
            elastic2SearchResultMappingTransformer = new Elastic2SearchResultMappingTransformer();
        }
        elastic2SearchResultMappingTransformer.setVariantId(getElasticQsfqlQueryTransformer().getVariantId());
        return elastic2SearchResultMappingTransformer;
    }

    private ElasticParameterQueryTransformer getElasticParameterQueryTransformer() {
        if(elasticParameterQueryTransformer == null) {
            elasticParameterQueryTransformer = new ElasticParameterQueryTransformer();
            elasticParameterQueryTransformer.setVariantId(getElasticQsfqlQueryTransformer().getVariantId());
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
        getMappingTransformer().addFieldMapping(from, to);
        addSourceField(from);
        //getElasticParameterQueryTransformer().addFieldListValue(from);
        return this;
    }

    public ElasticFilterBuilder addSourceField(String fieldName) {
        QsfSearchConfigUtil.initDisplayMapping(searchConfig);
        DisplayMappingDTO mapping = new DisplayMappingDTO();
        mapping.setFrom(fieldName);
        mapping.setTo(fieldName);
        this.searchConfig.getDisplay().getMapping().add(mapping);
        return this;
    }

    public ElasticFilterBuilder searchResultTransformer(SearchResultTransformerIF searchResultTransformer) {
        this.searchResultTransformer = searchResultTransformer;
        return this;
    }

    public ElasticFilterBuilder addAggregation(Facet facet) {
        getElasticParameterQueryTransformer().addAggregation(facet);
        getElasticQsfqlQueryTransformer().addAggregation(facet);


        return this;
    }

    public ElasticFilterBuilder addAggregation(String name, String id, String field) {

        getElasticParameterQueryTransformer().addAggregation(name, id, field);
        getElasticQsfqlQueryTransformer().addAggregation(name, id, field);
        mapAggregationType(name, "terms");
        return this;
    }

    public ElasticFilterBuilder addAggregation(String name, String id, String field, int size) {
        getElasticParameterQueryTransformer().addAggregation(name, id, field, size);
        getElasticQsfqlQueryTransformer().addAggregation(name, id, field, size);
        return this;
    }

    public ElasticFilterBuilder mapAggregation(String from, String to) {
        //getMappingTransformer().addFacetMapping(from, to);
        return this;
    }
    public ElasticFilterBuilder mapAggregationName(String from, String to) {
        getMappingTransformer().addFacetNameMapping(from, to);
        return this;
    }
    public ElasticFilterBuilder mapAggregationType(String id, String type) {
        getMappingTransformer().addFacetTypeMapping(id, type);
        return this;
    }

    public ElasticFilterBuilder facetKeyMapper(String id, FacetKeyMapper facetKeyMapper) {
        getMappingTransformer().addFacetKeyMapper(id, facetKeyMapper);
        return this;
    }

    public ElasticFilterBuilder facetFilterMapper(String id, FacetFilterMapper facetFilterMapper) {
        getMappingTransformer().addFacetFilterMapper(id, facetFilterMapper);
        return this;
    }


    public ElasticFilterBuilder mapFilter(String from, String to) {
        getElasticQsfqlQueryTransformer().addFilterMapping(from, to);
        return this;
    }

    public ElasticFilterBuilder addFilterRule(String pattern, String replacement) {
        getElasticQsfqlQueryTransformer().addFilterRule(pattern, replacement);
        return this;
    }
    public ElasticFilterBuilder addSortRule(String pattern, String replacement) {
        getElasticQsfqlQueryTransformer().addSortRule(pattern, replacement);
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

    public ElasticFilterBuilder profileMatchAll() {
       return profile(Profiles.matchAll());
    }


    public ElasticFilterBuilder profile(String profile) {
        getElasticQsfqlQueryTransformer().setProfile(profile);
        getElasticParameterQueryTransformer().setProfile(profile);
        return this;
    }

    public ElasticFilterBuilder profileParameter(String key, Object value) {
        getElasticQsfqlQueryTransformer().addProfileParameter(key, value);
        getElasticParameterQueryTransformer().addProfileParameter(key, value);
        return this;
    }

    public ElasticFilterBuilder resultField(String resultFieldName, String value) {
        //getMappingTransformer().addResultField(resultFieldName, value);
        return this;
    }

    @Deprecated
    public ElasticFilterBuilder client(ElasticClientIF elasticClient) {
        elasticFilter.setElasticClient(elasticClient);
        return this;
    }

    public ElasticFilterBuilder client(ElasticSearchClient elasticClient) {
        elasticFilter.setElasticSearchClient(elasticClient);
        return this;
    }

    public ElasticFilterBuilder filterPrefix(String filterPrefix) {
        getMappingTransformer().filterPrefix(filterPrefix);
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

    public ElasticFilterBuilder defaultRows(int defaultRows) {
        getElasticQsfqlQueryTransformer().setDefaultRows(defaultRows);
        return this;
    }

    public ElasticFilterBuilder rows(int rows) {
        getElasticQsfqlQueryTransformer().setRows(rows);
        return this;
    }

    public ElasticFilterBuilder defaultPage(int defaultPage) {
        getElasticQsfqlQueryTransformer().setDefaultPage(defaultPage);
        return this;
    }

    public ElasticFilterBuilder filterVariable(String filterVariable) {
        getElasticQsfqlQueryTransformer().setFilterVariable(filterVariable);
        return this;
    }

    public ElasticFilterBuilder filterVariable() {
        return filterVariable("filter");
    }

    public ElasticFilterBuilder filterPath(String filterPath) {
        getElasticQsfqlQueryTransformer().setFilterPath(filterPath);
        return this;
    }


}
