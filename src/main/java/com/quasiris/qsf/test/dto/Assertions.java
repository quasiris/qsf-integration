package com.quasiris.qsf.test.dto;

import com.quasiris.qsf.dto.response.SearchResponse;
import com.quasiris.qsf.dto.response.SingleSearchResponse;

import java.util.List;

public class Assertions {

    private List<SearchResponse> searchResponse;
    private List<SingleSearchResponse> singleSearchResponse;
    private List<JsonPath> jsonPath;
    private List<Suggest> suggest;
    private List<Http> http;

    /**
     * Getter for property 'searchResponse'.
     *
     * @return Value for property 'searchResponse'.
     */
    public List<SearchResponse> getSearchResponse() {
        return searchResponse;
    }

    /**
     * Setter for property 'searchResponse'.
     *
     * @param searchResponse Value to set for property 'searchResponse'.
     */
    public void setSearchResponse(List<SearchResponse> searchResponse) {
        this.searchResponse = searchResponse;
    }

    /**
     * Getter for property 'jsonPath'.
     *
     * @return Value for property 'jsonPath'.
     */
    public List<JsonPath> getJsonPath() {
        return jsonPath;
    }

    /**
     * Setter for property 'jsonPath'.
     *
     * @param jsonPath Value to set for property 'jsonPath'.
     */
    public void setJsonPath(List<JsonPath> jsonPath) {
        this.jsonPath = jsonPath;
    }

    /**
     * Getter for property 'singleSearchResponse'.
     *
     * @return Value for property 'singleSearchResponse'.
     */
    public List<SingleSearchResponse> getSingleSearchResponse() {
        return singleSearchResponse;
    }

    /**
     * Setter for property 'singleSearchResponse'.
     *
     * @param singleSearchResponse Value to set for property 'singleSearchResponse'.
     */
    public void setSingleSearchResponse(List<SingleSearchResponse> singleSearchResponse) {
        this.singleSearchResponse = singleSearchResponse;
    }

    /**
     * Getter for property 'suggest'.
     *
     * @return Value for property 'suggest'.
     */
    public List<Suggest> getSuggest() {
        return suggest;
    }

    /**
     * Setter for property 'suggest'.
     *
     * @param suggest Value to set for property 'suggest'.
     */
    public void setSuggest(List<Suggest> suggest) {
        this.suggest = suggest;
    }

    /**
     * Getter for property 'http'.
     *
     * @return Value for property 'http'.
     */
    public List<Http> getHttp() {
        return http;
    }

    /**
     * Setter for property 'http'.
     *
     * @param http Value to set for property 'http'.
     */
    public void setHttp(List<Http> http) {
        this.http = http;
    }
}
