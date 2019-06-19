package com.quasiris.qsf.pipeline.filter.solr;

import java.util.List;

/**
 * Created by mki on 11.11.17.
 */
public class SolrFilterBuilder {

    private SolrFilter solrFilter;

    private Solr2SearchResultMappingTransformer mappingTransformer;

    private SolrParameterQueryTransformer solrParameterQueryTransformer;

    private SolrQsfqlQueryTransformer solrQsfqlQueryTransformer;

    private Class<?> queryTransformer = SolrQsfqlQueryTransformer.class;

    public static SolrFilterBuilder create() {
        SolrFilterBuilder builder = new SolrFilterBuilder();
        builder.solrFilter = new SolrFilter();
        return builder;
    }


    public SolrFilterBuilder baseUrl(String baseUrl) {
        solrFilter.setSolrBaseUrl(baseUrl);
        return this;
    }

    public SolrFilterBuilder id(String id) {
        solrFilter.setId(id);
        return this;
    }


    public SolrFilterBuilder resultSetId(String resultSetId) {
        solrFilter.setResultSetId(resultSetId);
        return this;
    }
    public SolrFilter build() {

        if(queryTransformer.equals(SolrQsfqlQueryTransformer.class)) {
            solrFilter.setQueryTransformer(solrQsfqlQueryTransformer);
        } else if(queryTransformer.equals(SolrParameterQueryTransformer.class)) {
            solrFilter.setQueryTransformer(solrParameterQueryTransformer);
        }

        if(mappingTransformer != null) {
            solrFilter.setSearchResultTransformer(mappingTransformer);
        }
        return solrFilter;
    }

    public SolrFilterBuilder params(String name, List<String> values) {
        for(String value: values) {
            param(name, value);
        }
        return this;
    }

    public SolrFilterBuilder param(String name, String value) {
        getSolrParameterQueryTransformer().addParam(name,value);
        getSolrQsfqlQueryTransformer().addParam(name,value);
        return this;
    }

    private Solr2SearchResultMappingTransformer getMappingTransformer() {
        if(mappingTransformer == null) {
            mappingTransformer = new Solr2SearchResultMappingTransformer();
        }
        return mappingTransformer;
    }

    private SolrParameterQueryTransformer getSolrParameterQueryTransformer() {
        if(solrParameterQueryTransformer == null) {
            solrParameterQueryTransformer = new SolrParameterQueryTransformer();
        }
        return solrParameterQueryTransformer;
    }

    public SolrQsfqlQueryTransformer getSolrQsfqlQueryTransformer() {
        if(solrQsfqlQueryTransformer == null) {
            solrQsfqlQueryTransformer = new SolrQsfqlQueryTransformer();
        }
        return solrQsfqlQueryTransformer;
    }

    public void setSolrQsfqlQueryTransformer(SolrQsfqlQueryTransformer solrQsfqlQueryTransformer) {
        this.solrQsfqlQueryTransformer = solrQsfqlQueryTransformer;
    }

    public SolrFilterBuilder mapField(String from, String to) {
        getMappingTransformer().addFieldMapping(from, to);
        getSolrParameterQueryTransformer().addFieldListValue(from);
        return this;
    }

    public SolrFilterBuilder mapFacet(String from, String to) {
        getMappingTransformer().addFacetMapping(from, to);
        return this;
    }
    public SolrFilterBuilder mapFacetName(String from, String to) {
        getMappingTransformer().addFacetNameMapping(from, to);
        return this;
    }


    public SolrFilterBuilder mapFilter(String from, String to) {
        getSolrQsfqlQueryTransformer().addFilterMapping(from, to);
        return this;
    }

    public SolrFilterBuilder queryTransformer(QueryTransformerIF queryTransformer) {
        if( queryTransformer instanceof SolrQsfqlQueryTransformer) {
            this.solrQsfqlQueryTransformer = (SolrQsfqlQueryTransformer) queryTransformer;
        } else if(queryTransformer instanceof SolrParameterQueryTransformer) {
            this.solrParameterQueryTransformer = (SolrParameterQueryTransformer) queryTransformer;
        } else {
            throw new IllegalArgumentException("The query transformer " + queryTransformer.getClass().getName() + " is not supported.");
        }

        return this;
    }

    public SolrFilterBuilder queryTransformer(Class<?> queryTransformer) {
        this.queryTransformer = queryTransformer;

        return this;
    }

    public SolrFilterBuilder disable() {
        solrFilter.setActive(false);
        return this;
    }

    public SolrFilterBuilder resultField(String resultFieldName, String value) {
        getMappingTransformer().addResultField(resultFieldName, value);
        return this;
    }

    public SolrFilterBuilder filterPrefix(String filterPrefix) {
        getMappingTransformer().filterPrefix(filterPrefix);
        return this;
    }

    public SolrFilterBuilder mapSort(String from, String to) {
        getSolrQsfqlQueryTransformer().addSortMapping(from, to);
        return this;
    }

    public SolrFilterBuilder defaultSort(String defaultSort) {
        getSolrQsfqlQueryTransformer().setDefaultSort(defaultSort);
        return this;
    }


}
