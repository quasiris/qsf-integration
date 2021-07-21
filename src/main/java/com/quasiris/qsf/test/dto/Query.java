package com.quasiris.qsf.test.dto;

import com.quasiris.qsf.dto.query.SearchQueryDTO;

import java.util.List;

public class Query {

    private String url;
    private SearchQueryDTO searchQuery;

    private List<String> alternativeQueries;

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
     * Getter for property 'alternativeQueries'.
     *
     * @return Value for property 'alternativeQueries'.
     */
    public List<String> getAlternativeQueries() {
        return alternativeQueries;
    }

    /**
     * Setter for property 'alternativeQueries'.
     *
     * @param alternativeQueries Value to set for property 'alternativeQueries'.
     */
    public void setAlternativeQueries(List<String> alternativeQueries) {
        this.alternativeQueries = alternativeQueries;
    }
}
