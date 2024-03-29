package com.quasiris.qsf.pipeline.filter.solr;

import com.google.common.base.Optional;
import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.util.PrintUtil;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.noggit.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 11.11.17.
 */
public class SolrFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(SolrFilter.class);

    private String solrBaseUrl;

    private String resultSetId;

    private SearchResultTransformerIF searchResultTransformer = new Solr2SearchResultTransformer();
    private QueryTransformerIF queryTransformer;

    private String userName;
    private String password;

    @Override
    public void init() {
        super.init();

    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {

        SolrQuery solrQuery = queryTransformer.transform(pipelineContainer);


        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug(getId() + ".baseUrl", DebugType.STRING, query2url(this.solrBaseUrl, solrQuery));
        }

        SolrClient solrClient = SolrClientFactory.getSolrClient(solrBaseUrl);
        if(solrClient == null) {
            solrClient = new HttpSolrClient.Builder(solrBaseUrl).build();
            SolrClientFactory.setSolrClient(solrClient, solrBaseUrl);
        }

        QueryRequest req = new QueryRequest(solrQuery);
        if(userName != null && password != null) {
            req.setBasicAuthCredentials(userName, password);
        }
        QueryResponse solrResponse = req.process(solrClient);

        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug(getId() + ".result", DebugType.STRING, queryResponse2Json(solrResponse));
        }

        SearchResult searchResult = searchResultTransformer.transform(solrResponse);
        searchResult.setName(resultSetId);
        searchResult.setTime(getCurrentTime());
        searchResult.setStatusCode(200);
        searchResult.setStatusMessage("OK");

        pipelineContainer.putSearchResult(resultSetId, searchResult);
        return pipelineContainer;
    }

    public void setSolrBaseUrl(String solrBaseUrl) {
        this.solrBaseUrl = solrBaseUrl;
    }

    public void setResultSetId(String resultSetId) {
        this.resultSetId = resultSetId;
    }

    public void setSearchResultTransformer(SearchResultTransformerIF searchResultTransformer) {
        this.searchResultTransformer = searchResultTransformer;
    }

    public void setQueryTransformer(QueryTransformerIF queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    public static String query2url(SolrQuery solrQuery) {
        return query2url("", solrQuery);
    }

    public static String query2url(String solrBase, SolrQuery solrQuery) {
        if(solrBase == null) {
            solrBase = "";
        }
        Map<String,String[]> parameters = solrQuery.getMap();
        String requestHandler = Optional.fromNullable(solrQuery.getRequestHandler()).or("select");
        StringBuilder url = new StringBuilder(solrBase);
        url.append("/").append(requestHandler).append("?");
        boolean first = true;
        for(Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            for(String value: entry.getValue()) {
                if(first) {
                    first = false;
                } else {
                    url.append("&");
                }
                url.append(key).append("=").append(value);
            }

        }
        return url.toString();
    }

    public static Object queryResponse2Json(QueryResponse queryResponse) {
        Map<String, Object> json = new HashMap<>();
        json.put("results", toJson(queryResponse.getResults()));

        // TODO facet values

        json.put("facetQuery", queryResponse.getFacetQuery());
        json.put("facetRanges", queryResponse.getFacetRanges());
        json.put("intervalFacets", queryResponse.getIntervalFacets());
        json.put("debugMap", queryResponse.getDebugMap());
        json.put("explainMap", queryResponse.getExplainMap());
        json.put("fieldStatsInfo", queryResponse.getFieldStatsInfo());
        json.put("explainResults", toJson(queryResponse.getExpandedResults()));
        return json;
    }

    private static Object toJson(Object object) {
        return JsonUtil.toJson(JSONUtil.toJSON(object));
    }

    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer =  super.print(indent);
        PrintUtil.printKeyValue(printer, indent, "solrBaseUrl", solrBaseUrl);
        PrintUtil.printKeyValue(printer, indent, "resultSetId", resultSetId);
        PrintUtil.printKeyValue(printer, indent, "searchResultTransformer", searchResultTransformer.getClass().getSimpleName());
        printer.append(searchResultTransformer.print(indent + "\t"));
        PrintUtil.printKeyValue(printer, indent, "queryTransformer", queryTransformer.getClass().getSimpleName());
        printer.append(queryTransformer.print(indent + "\t"));
        return printer;
    }

    /**
     * Setter for property 'userName'.
     *
     * @param userName Value to set for property 'userName'.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Setter for property 'password'.
     *
     * @param password Value to set for property 'password'.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}