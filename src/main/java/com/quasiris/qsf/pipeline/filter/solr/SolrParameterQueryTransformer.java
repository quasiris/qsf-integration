package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.util.PrintUtil;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.*;

/**
 * Created by mki on 18.11.17.
 */
public class SolrParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, List<String>> parameters;
    private Set<String> fieldList = new HashSet<>();

    private PipelineContainer pipelineContainer;

    private SearchQuery searchQuery;
    private SolrQuery solrQuery;

    @Override
    public SolrQuery transform(PipelineContainer pipelineContainer) {
        this.pipelineContainer = pipelineContainer;
        this.searchQuery = pipelineContainer.getSearchQuery();
        this.solrQuery = new SolrQuery();

        transformParameter();
        transformFieldlist();

        return solrQuery;
    }

    public void transformFieldlist() {
        for(String fieldName:fieldList) {
            solrQuery.addField(fieldName);
        }
    }

    public void transformParameter() {
        if(parameters == null) {
            return;
        }
        Map<String, String> replaceMap = RequestParser.getRequestParameter(pipelineContainer);
        for(Map.Entry<String, String> param :pipelineContainer.getParameters().entrySet()) {
            replaceMap.put("param." + param.getKey(), param.getValue());
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(replaceMap);

        for(Map.Entry<String, List<String>> parameter: parameters.entrySet()) {
            for(String value: parameter.getValue()) {
                String replacedValue = stringSubstitutor.replace(value);
                solrQuery.add(parameter.getKey(),replacedValue);
            }
        }
    }

    public StringBuilder print(String indent) {
        StringBuilder printer = new StringBuilder();
        PrintUtil.printMap(printer,indent, "parameters", parameters);
        PrintUtil.printKeyValue(printer,indent, "fieldList", fieldList);
        return printer;
    }

    public void addParam(String name, String value){
        if(parameters == null) {
            parameters = new HashMap<>();
        }
        List<String> values = parameters.get(name);
        if(values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
        parameters.put(name, values);
    }

    public void addFieldListValue(String fieldName) {
        fieldList.add(fieldName);
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public Set<String> getFieldList() {
        return fieldList;
    }
}
