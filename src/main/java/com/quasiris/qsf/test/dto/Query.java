package com.quasiris.qsf.test.dto;

import com.quasiris.qsf.dto.query.SearchQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * A Query represents a query against a QSF API
 */
public class Query {

    /**
     * The query URL that should be tested. Can be parameterized with {@link #variations}<br>
     *  e.g. `"dev/kam/search/search?q=fliesen"` for a fixed query <br>
     *  or `"${baseUrl}/${serviceName}kam/search/search?q=fliesen"` for a parameterized query
     */
    private String url;

    /**
     * Can be used to further specify the search query using a {@link com.quasiris.qsf.dto.query.SearchQueryDTO} object <br>
     * This makes it possible to specify query parameters without having to use a pure URL-based query
     * TODO implement the actual usage in TestExecuter, since this field is currently not used
     */
    private SearchQueryDTO searchQuery;

    /**
     * Variations can be used to test the same testcase against a variety of queries with different URLs via variables <br>
     * The defined variables can be referenced with their key in the URL by using `${variable_key}` <br>
     * Note that using `${variable_key.encoded}` additionally URL-encodes the variable value <br><br>
     * A Variable is built as follows:
     * <ul><li>A variable identifier key</li>
     * <li>AnObject as value, usually a String</li></ul>
     */
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
