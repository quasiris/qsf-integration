package com.quasiris.qsf.pipeline;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.quasiris.qsf.exception.Debug;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by mki on 04.11.17.
 */
public class PipelineContainer {

    public PipelineContainer() {
    }

    @Deprecated
    public PipelineContainer(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    private String requestId;

    /**
     * use the context
     */
    @Deprecated
    private Map<String, String> parameter = new HashMap<>();

    /**
     * Use conditionals
     */
    @Deprecated
    private List<String> notActiveFilters = new ArrayList<>();

    /**
     * use status object
     */
    @Deprecated
    private boolean success = true;

    /**
     * use status object
     */
    @Deprecated
    private StringBuffer message = new StringBuffer();

    private boolean debug = false;

    /**
     * use status object
     */
    @Deprecated
    private boolean timeout = false;


    /**
     * the filter must decide this
     */
    @Deprecated
    private boolean failOnError = true;

    private List<Debug> debugStack = new ArrayList<>();

    private Document tracking = new Document();

    private SearchQuery searchQuery = new SearchQuery();
    private Map<String, SearchResult> searchResults = new HashMap<>();

    private Map<String, ? super Object> context = new HashMap<>();

    /**
     * map the request to the search Query object and
     * use the SearchQuery
     */
    @Deprecated
    private HttpServletRequest request;

    /**
     * we need this somewhere?
     */
    @Deprecated
    private HttpServletResponse response;

    private Long startTime = System.currentTimeMillis();

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public SearchResult getSearchResult(String name) {
        return searchResults.get(name);
    }


    public void putSearchResult(String name, SearchResult searchResult) {
        if(isDebugEnabled()) {
            Debug debug = new Debug();
            debug.setId("searchResult." + searchResult.getName());
            debug.setDebugObject(searchResult);
            debug.setType(DebugType.OBJECT);
            debugStack.add(debug);
        }
        searchResults.put(name, searchResult);
    }

    public <T> T getContext(String name, Class<T> clazz) {
        return (T) context.get(name);
    }

    public void putContext(String name, Object value) {
        context.put(name, value);
    }

    public Map<String, SearchResult> getSearchResults() {
        return searchResults;
    }

    public void start() {
        if(this.startTime == null) {
            this.startTime = System.currentTimeMillis();
        }
    }

    public long currentTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    /**
     * map the request to the search Query object and
     * TODO use the SearchQuery
     * @return request
     */
    @Deprecated
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * TODO we need this somewhere?
     * @return response
     */
    @Deprecated
    public HttpServletResponse getResponse() {
        return response;
    }

    public void debug(Object debugObject) {
        debug(UUID.randomUUID().toString(),DebugType.OBJECT, debugObject );
    }

    public void debug(String id, DebugType type, Object debugObject) {
        Debug debug = new Debug();
        debug.setId(id);
        debug.setType(type);
        debug.setDebugObject(debugObject);
        debug(debug);
    }

    public void debug(Debug debug) {
        if(this.debug) {
            this.debugStack.add(debug);
        }
    }

    public void enableDebug() {
        this.debug = true;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebugEnabled() {
        return this.debug;
    }

    public List<Debug> getDebugStack() {
        return debugStack;
    }

    /**
     * TODO use status object
     * @param message param
     */
    @Deprecated
    public void error(String message) {
        this.success = false;
        if(Strings.isNullOrEmpty(message)) {
            return;
        }
        if(this.message.length() != 0) {
            this.message.append("\n");
        }
        this.message.append(message);
    }

    /**
     * TODO use status object
     * @param e param
     */
    @Deprecated
    public void error(Throwable e) {
        if (e instanceof TimeoutException) {
            timeout = true;
        }
        error(e.getMessage());
        error(Throwables.getStackTraceAsString(e));
    }

    /**
     * TODO use status object
     * @return message
     */
    @Deprecated
    public String getMessage() {
        return message.toString();
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * TODO use status object
     * @return true if timeout
     */
    @Deprecated
    public boolean isTimeout() {
        return timeout;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * TODO the filter must decide this
     * @return true if fail on any error
     */
    @Deprecated
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * TODO the filter must decide this
     * @param failOnError param
     */
    @Deprecated
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * TODO Use conditionals
     * @param filterId param
     */
    @Deprecated
    public void deactivateFilter(String filterId) {
        notActiveFilters.add(filterId);
    }

    /**
     * TODO Use conditionals
     * @param filterId param
     */
    @Deprecated
    public void activateFilter(String filterId) {
        notActiveFilters.remove(filterId);
    }

    /**
     * TODO Use conditionals
     * @param filterId param
     * @return true if active
     */
    @Deprecated
    public boolean isFilterActive(String filterId) {
        return !notActiveFilters.contains(filterId);
    }

    /**
     * TODO Use the context
     * @param key param
     * @param value param
     */
    @Deprecated
    public void setParameter(String key, String value) {
        this.parameter.put(key,value);
    }

    /**
     * TODO Use the context
     * @param key param
     * @return param
     */
    @Deprecated
    public String getParameter(String key) {
        return this.parameter.get(key);
    }

    /**
     * TODO Use the context
     * @return params
     */
    @Deprecated
    public Map<String, String> getParameters() {
        return this.parameter;
    }

    @Deprecated
    public Document getFirstDocumentOrNull(String resultSetId) {
        SearchResult searchResult = searchResults.get(resultSetId);
        if(searchResult == null) {
            return null;
        }
        if(searchResult.getDocuments() == null) {
            return null;
        }

        Document document =  searchResult.getDocuments().stream().findFirst().orElse(null);
        if(document == null || document.getDocument() == null) {
            return null;
        }
        return document;
    }

    /**
     * Getter for property 'requestId'.
     *
     * @return Value for property 'requestId'.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for property 'requestId'.
     *
     * @param requestId Value to set for property 'requestId'.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for property 'tracking'.
     *
     * @return Value for property 'tracking'.
     */
    public Document getTracking() {
        return tracking;
    }

    /**
     * Setter for property 'tracking'.
     *
     * @param tracking Value to set for property 'tracking'.
     */
    public void setTracking(Document tracking) {
        this.tracking = tracking;
    }

    @Override
    public String toString() {
        return "PipelineContainer{" +
                "\nsearchQuery=" + searchQuery +
                "\n, searchResponses=" + searchResults +
                "\n, context=" + context +
                '}';
    }
}
