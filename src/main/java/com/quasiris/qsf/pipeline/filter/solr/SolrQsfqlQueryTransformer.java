package com.quasiris.qsf.pipeline.filter.solr;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchFilter;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.HashMap;
import java.util.List;
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
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList();
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {
            transformFilter(searchFilter);
        }

    }

    public void transformFilter(SearchFilter searchFilter) {
        String solrField = getFilterMapping().get(searchFilter.getName());
        if(Strings.isNullOrEmpty(solrField)) {
            return;
        }

        String firstValue = searchFilter.getValues().stream().findFirst().orElse(null);
        if(Strings.isNullOrEmpty(firstValue)) {
            return;
        }
        getSolrQuery().addFilterQuery(solrField + ":" + firstValue);
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
