package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer defaultPage = 1;


    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) {
        super.transform(pipelineContainer);

        transformQuery();
        transformSort();
        transformFilters();
        transformPaging();


        return getElasticQuery();
    }

    public void transformQuery() {
        // nothing to do - the query is defined in the profile, which is loaded in the ElasticParameterQueryTransformer
    }

    public void transformSort() {
        try {
            String sort = null;
            if (getSearchQuery().getSort() == null) {
                sort = sortMapping.get(defaultSort);
            } else {
                sort = sortMapping.get(getSearchQuery().getSort().getSort());
                if (sort == null) {
                    sort = sortMapping.get(defaultSort);
                }
            }
            if (sort == null) {
                return;
            }
            ArrayNode sortJson = (ArrayNode) getObjectMapper().readTree(sort);
            getElasticQuery().set("sort", sortJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html
    // TODO implement range queries
    public void transformFilters() {
        ArrayNode filters = getObjectMapper().createArrayNode();
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {
            ObjectNode filter = transformFilter(searchFilter);
            if(filter != null) {
                filters.add(filter);
            }
        }
        ObjectNode query = (ObjectNode) getElasticQuery().get("query");
        query.set("filter", filters);

    }

    public ObjectNode transformFilter(SearchFilter searchFilter) {

        String elasticField = getFilterMapping().get(searchFilter.getName());
        if (Strings.isNullOrEmpty(elasticField)) {
            return null;
        }

        String firstValue = searchFilter.getValues().stream().findFirst().orElse(null);
        if (Strings.isNullOrEmpty(firstValue)) {
            return null;
        }

        ObjectNode filter = (ObjectNode) getObjectMapper().createObjectNode().set("term",
                getObjectMapper().createObjectNode().put(elasticField, firstValue));

        return filter;

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

}
