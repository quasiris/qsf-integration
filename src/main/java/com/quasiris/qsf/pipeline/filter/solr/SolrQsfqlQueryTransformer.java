package com.quasiris.qsf.pipeline.filter.solr;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 18.11.17.
 */
public class SolrQsfqlQueryTransformer implements QueryTransformerIF {


    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer defaultPage = 1;


    private SearchQuery searchQuery;
    private SolrQuery solrQuery;

    @Override
    public SolrQuery transform(PipelineContainer pipelineContainer) {
        searchQuery = pipelineContainer.getSearchQuery();
        solrQuery = new SolrQuery();
        transformQuery();
        transformSort();
        transformFilters();
        transformPaging();


        return solrQuery;
    }

    public void transformQuery() {
        String query = searchQuery.getQ();
        solrQuery.setQuery(query);
    }

    public void transformSort() {
        String sort = null;
        if(searchQuery.getSort() == null) {
            sort = sortMapping.get(defaultSort);
        } else {
            sort = sortMapping.get(searchQuery.getSort().getSort());
            if(sort == null) {
                sort = sortMapping.get(defaultSort);
            }
        }
        if(sort == null) {
            return;
        }
        solrQuery.setParam("sort", sort);

    }

    public void transformFilters() {
        List<SearchFilter> searchFilterList = searchQuery.getSearchFilterList();
        for (SearchFilter searchFilter : searchQuery.getSearchFilterList()) {
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
        solrQuery.addFilterQuery(solrField + ":" + firstValue);
    }

    public void transformPaging() {
        Integer page = searchQuery.getPage();
        if(page == null) {
            page = defaultPage;
        }
        Integer rows = searchQuery.getRows();
        if(rows == null) {
            rows = defaultRows;
        }


        int start = (page - 1) * rows;

        solrQuery.setStart(start);
        solrQuery.setRows(rows);


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

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
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
