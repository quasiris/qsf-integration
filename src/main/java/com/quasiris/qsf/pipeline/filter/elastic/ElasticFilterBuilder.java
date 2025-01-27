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
import java.util.Map;
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

    private QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

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
            searchResultTransformer.setSearchConfig(searchConfig);
            elasticFilter.setSearchResultTransformer(searchResultTransformer);
        } else {
            elasticFilter.setSearchResultTransformer(getMappingTransformer());
        }
        return elasticFilter;
    }

    private Elastic2SearchResultMappingTransformer getMappingTransformer() {
        if(elastic2SearchResultMappingTransformer == null) {
            elastic2SearchResultMappingTransformer = new Elastic2SearchResultMappingTransformer();
            elastic2SearchResultMappingTransformer.setSearchConfig(searchConfig);
        }
        return elastic2SearchResultMappingTransformer;
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
        QsfSearchConfigUtil.initDisplayMapping(this.searchConfig);
        DisplayMappingDTO mapping = new DisplayMappingDTO();
        mapping.setFrom(from);
        mapping.setTo(to);
        this.searchConfig.getDisplay().getMapping().add(mapping);
        return this;
    }

    public ElasticFilterBuilder searchResultTransformer(SearchResultTransformerIF searchResultTransformer) {
        this.searchResultTransformer = searchResultTransformer;
        return this;
    }

    public ElasticFilterBuilder addAggregation(Facet facet) {
        searchConfig.getFacet().getFacets().add(facet);
        return this;
    }

    public ElasticFilterBuilder addFilterRule(String pattern, String replacement) {
        this.searchConfig.getFilter().getFilterRules().put(pattern, replacement);
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
        searchConfig.getFilter().setFilterPrefix(filterPrefix);
        return this;
    }

    public ElasticFilterBuilder mapFilter(String from, String to) {
        this.searchConfig.getFilter().getFilterMapping().put(from, to);
        return this;
    }

    public ElasticFilterBuilder facetKeyMapper(String id, FacetKeyMapper facetKeyMapper) {
        FacetMapping facetMapping = Elastic2SearchResultMappingTransformer.getOrCreateFacetMapping(searchConfig, id);
        facetMapping.setFacetKeyMapper(facetKeyMapper);
        return this;
    }

    public ElasticFilterBuilder facetFilterMapper(String id, FacetFilterMapper facetFilterMapper) {
        FacetMapping facetMapping = Elastic2SearchResultMappingTransformer.getOrCreateFacetMapping(searchConfig, id);
        facetMapping.setFacetFilterMapper(facetFilterMapper);

        getMappingTransformer().addFacetFilterMapper(id, facetFilterMapper);
        return this;
    }

    public ElasticFilterBuilder mapSort(String from, String to) {
        searchConfig.getSort().getSortMapping().put(from, to);
        return this;
    }

    public ElasticFilterBuilder defaultSort(String defaultSort) {
        searchConfig.getSort().setDefaultSort(defaultSort);
        return this;
    }

    public ElasticFilterBuilder defaultRows(int defaultRows) {
        searchConfig.getPaging().setDefaultRows(defaultRows);
        return this;
    }

    public ElasticFilterBuilder rows(int rows) {
        searchConfig.getPaging().setRows(rows);
        return this;
    }

    public ElasticFilterBuilder defaultPage(int defaultPage) {
        searchConfig.getPaging().setDefaultPage(defaultPage);
        return this;
    }

    public ElasticFilterBuilder filterVariable(String filterVariable) {
        searchConfig.getFilter().setFilterVariable(filterVariable);
        return this;
    }

    public ElasticFilterBuilder filterVariable() {
        return filterVariable("filter");
    }

    public ElasticFilterBuilder filterPath(String filterPath) {
        searchConfig.getFilter().setFilterPath(filterPath);
        return this;
    }


}
