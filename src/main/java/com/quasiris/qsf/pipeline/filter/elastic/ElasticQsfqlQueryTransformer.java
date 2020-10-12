package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.Sort;

import java.util.*;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Map<String, String> filterRules = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer rows;
    private Integer defaultPage = 1;

    private Integer elasticVersion = 6;


    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException {
        super.transform(pipelineContainer);

        try {
            transformQuery();
            transformSort();
            transformFilters();
            transformPaging();
        } catch (JsonBuilderException e) {
            throw new PipelineContainerException(e.getMessage(), e);
        }


        return getElasticQuery();
    }


    @Override
    public void transformAggregations() throws JsonBuilderException {
        if(getSearchQuery().getFacetList() != null) {
            for(Facet facet : getSearchQuery().getFacetList()) {
                addAggregation(facet);
            }
        }
        super.transformAggregations();

    }

    public void transformQuery() {
        // nothing to do - the query is defined in the profile, which is loaded in the ElasticParameterQueryTransformer
    }

    public void transformSort() {
        try {
            Sort sort = getSearchQuery().getSort();
            if(sort == null && defaultSort == null) {
                return;
            }
            if (sort == null) {
                sort = new Sort(defaultSort);
            }

            ArrayNode sortJson = transformSortWithField(sort);
            if(sortJson == null) {
                sortJson = transformSortWithMapping(sort);
            }
            if(sortJson == null) {
                return;
            }
            getElasticQuery().set("sort", sortJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ArrayNode transformSortWithField(Sort sort) throws JsonBuilderException {
        if(sort == null || sort.getField() == null) {
            return null;
        }
        if(sort.getDirection() == null) {
            sort.setDirection("asc");
        }

        String sortJson = "[{\"" + sort.getField() + "\": \"" + sort.getDirection() + "\"}]";
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string(sortJson);
        return (ArrayNode) jsonBuilder.get();

    }

    protected ArrayNode transformSortWithMapping(Sort sort) throws JsonBuilderException {
        if(sort == null || sort.getSort() == null) {
            return null;
        }
        String sortJson = sortMapping.get(sort.getSort());
        if(sortJson == null) {
            return null;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string(sortJson);
        return (ArrayNode) jsonBuilder.get();
    }

    public void transformFilters() throws JsonBuilderException {
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion,
                getObjectMapper(),
                getElasticQuery(),
                getSearchQuery(),
                getFilterRules(),
                getFilterMapping()
        );
        filterTransformer.transformFilters();
    }



    public ObjectNode getBoolQuery() {
        ObjectNode query = (ObjectNode) getElasticQuery().get("query");
        ObjectNode functionScore = (ObjectNode) query.get("function_score");
        if(functionScore != null) {
            query = (ObjectNode) functionScore.get("query");
        }



        ObjectNode bool = (ObjectNode) query.get("bool");
        if(bool == null) {
            bool = objectMapper.createObjectNode();
            query.set("bool", bool);
        }
        return bool;
    }

    public void transformPaging() {
        Integer page = getSearchQuery().getPage();
        if (page == null) {
            page = defaultPage;
        }
        Integer rows = getSearchQuery().getRows();
        if (rows == null) {
            rows = defaultRows;
        }
        if(this.rows != null) {
            rows = this.rows;
        }


        int start = (page - 1) * rows;

        getElasticQuery().put("from", start);
        getElasticQuery().put("size", rows);
    }



    @Override
    public StringBuilder print(String indent) {
        return new StringBuilder("TODO");
    }

    public void addSortMapping(String from, String to) {
        sortMapping.put(from, to);
    }

    public void addFilterMapping(String from, String to) {
        filterMapping.put(from, to);
    }


    public void addFilterRule(String pattern, String replacement) {
        filterRules.put(pattern, replacement);
    }

    public Map<String, String> getSortMapping() {
        return sortMapping;
    }

    public void setSortMapping(Map<String, String> sortMapping) {
        this.sortMapping = sortMapping;
    }

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }

    public void setFilterMapping(Map<String, String> filterMapping) {
        this.filterMapping = filterMapping;
    }

    public Integer getDefaultRows() {
        return defaultRows;
    }

    public void setDefaultRows(Integer defaultRows) {
        this.defaultRows = defaultRows;
    }

    public Integer getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(Integer defaultPage) {
        this.defaultPage = defaultPage;
    }

    public Integer getElasticVersion() {
        return elasticVersion;
    }

    public void setElasticVersion(Integer elasticVersion) {
        this.elasticVersion = elasticVersion;
    }

    /**
     * Getter for property 'filterRules'.
     *
     * @return Value for property 'filterRules'.
     */
    public Map<String, String> getFilterRules() {
        return filterRules;
    }

    /**
     * Setter for property 'filterRules'.
     *
     * @param filterRules Value to set for property 'filterRules'.
     */
    public void setFilterRules(Map<String, String> filterRules) {
        this.filterRules = filterRules;
    }

    /**
     * Getter for property 'rows'.
     *
     * @return Value for property 'rows'.
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * Setter for property 'rows'.
     *
     * @param rows Value to set for property 'rows'.
     */
    public void setRows(Integer rows) {
        this.rows = rows;
    }
}
