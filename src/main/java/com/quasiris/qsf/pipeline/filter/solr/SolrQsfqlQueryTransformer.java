package com.quasiris.qsf.pipeline.filter.solr;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 18.11.17.
 */
public class SolrQsfqlQueryTransformer extends SolrParameterQueryTransformer implements QueryTransformerIF {


    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer defaultPage = 1;


    @Override
    public SolrQuery transform(PipelineContainer pipelineContainer) {
        super.transform(pipelineContainer);

        transformQuery();
        transformSort();
        transformFilters();
        transformPaging();


        return getSolrQuery();
    }

    public void transformQuery() {
        String query = getSearchQuery().getQ();
        getSolrQuery().setQuery(query);
    }

    public void transformSort() {
        String sort = null;
        if(getSearchQuery().getSort() == null) {
            sort = sortMapping.get(defaultSort);
        } else {
            sort = sortMapping.get(getSearchQuery().getSort().getSort());
            if(sort == null) {
                sort = sortMapping.get(defaultSort);
            }
        }
        if(sort == null) {
            return;
        }
        getSolrQuery().setParam("sort", sort);

    }

    public void transformFilters() {
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {


            switch (searchFilter.getFilterType()) {
                case OR:
                    transformFilter(searchFilter);
                    break;
                case AND:
                    transformFilter(searchFilter);
                    break;
                case RANGE:
                    transformRangeFilter(searchFilter);
                    break;
                case SLIDER:
                    transformRangeFilter(searchFilter);
                    break;
                default:
                    throw new IllegalArgumentException("The filter type " + searchFilter.getFilterType().getCode() + " is not implemented.");
            }

        }

    }


    public void transformRangeFilter(SearchFilter searchFilter) {
        String solrField = getFilterMapping().get(searchFilter.getName());
        if(Strings.isNullOrEmpty(solrField)) {
            return;
        }

        RangeFilterValue<Number> rangeFilterValue = searchFilter.getRangeValue(Number.class);
        if(rangeFilterValue == null) {
            return;
        }
        StringBuilder solrFilter  = new StringBuilder();
        if(searchFilter.isExclude()) {
            solrFilter.append("{!tag=").append(searchFilter.getId()).append("}");
        }

        String min = rangeFilterValue.getMinValue().toString();
        if(rangeFilterValue.getMinValue().equals(Double.MIN_VALUE)) {
            min = "*";
        }

        String max = rangeFilterValue.getMaxValue().toString();
        if(rangeFilterValue.getMaxValue().equals(Double.MAX_VALUE)) {
            max = "*";
        }

        solrFilter.append(solrField).append(":").
                append("[").
                append(min).
                append(" TO ").
                append(max).
                append("]");

        getSolrQuery().addFilterQuery(solrFilter.toString());
    }


    public void transformFilter(SearchFilter searchFilter) {
        String solrField = getFilterMapping().get(searchFilter.getName());
        if(Strings.isNullOrEmpty(solrField)) {
            return;
        }

        if(searchFilter.getValues() == null || searchFilter.getValues().isEmpty()) {
            return;
        }
        StringBuilder solrFilter  = new StringBuilder();
        if(searchFilter.isExclude()) {
            solrFilter.append("{!tag=").append(searchFilter.getId()).append("}");
        }


        String operator = " AND ";
        switch (searchFilter.getFilterType()) {
            case OR:
                operator = " OR ";
                break;
            case AND:
                operator = " AND ";
                break;
            case RANGE:
                throw new IllegalArgumentException("A range filter can not be applied to solr terms filter.");
            case SLIDER:
                throw new IllegalArgumentException("A slider filter can not be applied to solr terms filter.");
        }

        solrFilter.append(solrField).append(":").append("(");
        solrFilter.append(Joiner.on(operator).skipNulls().join(searchFilter.getValues()));
        solrFilter.append(")");
        getSolrQuery().addFilterQuery(solrFilter.toString());
    }

    public void transformPaging() {
        Integer page = getSearchQuery().getPage();
        if(page == null) {
            page = defaultPage;
        }
        Integer rows = getSearchQuery().getRows();
        if(rows == null) {
            rows = defaultRows;
        }


        int start = (page - 1) * rows;

        getSolrQuery().setStart(start);
        getSolrQuery().setRows(rows);


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
