package com.quasiris.qsf.test.dto;

import com.quasiris.qsf.dto.query.SearchQueryDTO;

import java.util.List;
import java.util.Map;

public class Query {

    private String url;
    private SearchQueryDTO searchQuery;

    private List<Map<String, Object>> variations;

    /**
     * Getter for property 'url'.
     *
     * @return Value for property 'url'.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter for property 'url'.
     *
     * @param url Value to set for property 'url'.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter for property 'searchQuery'.
     *
     * @return Value for property 'searchQuery'.
     */
    public SearchQueryDTO getSearchQuery() {
        return searchQuery;
    }

    /**
     * Setter for property 'searchQuery'.
     *
     * @param searchQuery Value to set for property 'searchQuery'.
     */
    public void setSearchQuery(SearchQueryDTO searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Getter for property 'variations'.
     *
     * @return Value for property 'variations'.
     */
    public List<Map<String, Object>> getVariations() {
        return variations;
    }

    /**
     * Setter for property 'variations'.
     *
     * @param variations Value to set for property 'variations'.
     */
    public void setVariations(List<Map<String, Object>> variations) {
        this.variations = variations;
    }
}
