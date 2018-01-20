package com.quasiris.qsf.pipeline.filter.solr;

import com.google.common.base.Strings;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.EncodingUtil;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.FacetValue;
import com.quasiris.qsf.util.PrintUtil;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.*;

/**
 * Created by mki on 11.11.17.
 */
public class Solr2SearchResultMappingTransformer implements SearchResultTransformerIF {

    private Map<String, List<String>> fieldMapping = new HashMap<>();
    private Map<String, String> resultFields = new HashMap<>();
    private Map<String, String> facetMapping = new HashMap<>();
    private Map<String, String> facetNameMapping = new HashMap<>();
    private String filterPrefix = "";

    public Solr2SearchResultMappingTransformer() {


    }

    public SearchResult transform(QueryResponse queryResponse) {
        SearchResult result = new SearchResult();
        result.setTotal(queryResponse.getResults().getNumFound());
        result.setMaxScore(queryResponse.getResults().getMaxScore());
        Iterator<SolrDocument> solrDocumentIterator = queryResponse.getResults().iterator();
        while(solrDocumentIterator.hasNext()) {
            SolrDocument solrDocument = solrDocumentIterator.next();
            Document document = transformDocument(solrDocument);
            result.addDocument(document);
        }

        mapFacets(queryResponse, result);


        return result;
    }

    protected void mapFacets(QueryResponse queryResponse, SearchResult searchResult) {
        if(queryResponse.getFacetFields() == null) {
            return;
        }
        searchResult.setFacetCount(queryResponse.getFacetFields().size());
        for(FacetField facetField : queryResponse.getFacetFields()) {
            Facet facet = new Facet();

            String id = facetMapping.get(facetField.getName());
            facet.setId(id);

            String name = facetNameMapping.get(id);
            facet.setName(name);

            facet.setCount(Long.valueOf(facetField.getValues().size()));
            Long facetReseultCount = 0L;
            for(FacetField.Count count: facetField.getValues()) {
                FacetValue facetValue = new FacetValue(count.getName(), count.getCount());
                facetReseultCount = facetReseultCount + facetValue.getCount();
                facetValue.setFilter(filterPrefix + id + "=" + EncodingUtil.encode(count.getName()));
                facet.getValues().add(facetValue);
            }
            facet.setResultCount(facetReseultCount);
            searchResult.addFacet(facet);
        }
    }

    public Document transformDocument(SolrDocument solrDocument) {
        Document document = new Document();

        Map<String, String> replaceMap = new HashMap<>();
        for(String fieldName: solrDocument.getFieldNames()) {
            Object value = solrDocument.get(fieldName);
            transformField(document, fieldName,value);
            replaceMap.put(fieldName, String.valueOf(value));
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(replaceMap);
        for(Map.Entry<String, String> textEntry: resultFields.entrySet()) {
            String fieldName = textEntry.getKey();
            String fieldValue = textEntry.getValue();
            String replacedValue = strSubstitutor.replace(fieldValue);
            document.getDocument().put(fieldName, replacedValue);
        }

        return document;

    }

    public void transformField(Document document, String name, Object value) {
        List<String> mappedNames = fieldMapping.get(name);
        if(mappedNames == null) {
            return;
        }
        for(String mappedName : mappedNames) {
            if (!Strings.isNullOrEmpty(mappedName)) {
                document.getDocument().put(mappedName, value);
            }
        }
    }

    public void setFieldMapping(Map<String, List<String>> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public void setFacetMapping(Map<String, String> facetMapping) {
        this.facetMapping = facetMapping;
    }

    public void setFacetNameMapping(Map<String, String> facetNameMapping) {
        this.facetNameMapping = facetNameMapping;
    }

    public void addFieldMapping(String from, String to) {
        List<String> mapping = fieldMapping.get(from);
        if(mapping == null) {
            mapping = new ArrayList<>();
        }
        mapping.add(to);

        fieldMapping.put(from, mapping);
    }
    public void addFacetMapping(String from, String to) {
        facetMapping.put(from, to);
    }

    public void addFacetNameMapping(String from, String to) {
        facetNameMapping.put(from, to);
    }

    public void addResultField(String resultFieldName, String value) {
        resultFields.put(resultFieldName, value);
    }

    public void filterPrefix(String filterPrefix) {
        this.filterPrefix=filterPrefix;
    }

    public StringBuilder print(String indent) {
        StringBuilder printer = new StringBuilder();
        PrintUtil.printMap(printer,indent, "fieldMapping", fieldMapping);
        PrintUtil.printMap(printer,indent, "resultFields", resultFields);
        PrintUtil.printMap(printer,indent, "facetMapping", facetMapping);
        PrintUtil.printMap(printer,indent, "facetNameMapping", facetNameMapping);
        PrintUtil.printKeyValue(printer, indent,"filterPrefix", filterPrefix);
        return printer;
    }
}
