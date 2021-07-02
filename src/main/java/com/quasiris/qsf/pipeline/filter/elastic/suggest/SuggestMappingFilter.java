package com.quasiris.qsf.pipeline.filter.elastic.suggest;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.FacetValueCountComparator;
import com.quasiris.qsf.dto.response.SearchResult;

import java.util.*;

public class SuggestMappingFilter extends AbstractFilter {


    private String type = "suggest";

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        SearchResult suggestResult = new SearchResult();

        SearchResult searchResult = pipelineContainer.getSearchResult(type);

        Map<String, FacetValue> mergedFacet = new HashMap<>();

        SuggestContext suggestContext = pipelineContainer.getContext("suggestContext", SuggestContext.class);
        List<String> suggestFields = suggestContext.getSuggestFields();
        Set<String> startTokens = suggestContext.getStartTokens();
        StringJoiner startTokenJoiner = suggestContext.getStartTokenJoiner();

        String startToken = startTokenJoiner.toString();

        for(String suggestField: suggestFields) {
            mergeFacet(mergedFacet, searchResult,suggestField, startTokens);
        }

        List<FacetValue> sortedFacetValues = new ArrayList(mergedFacet.values());
        Collections.sort(sortedFacetValues, new FacetValueCountComparator());

        suggestResult.setDocuments(new ArrayList<>());
        for (int i = sortedFacetValues.size() - 1; i >=0  ; i--) {

            FacetValue facetValue = sortedFacetValues.get(i);
            if(startTokens.contains(facetValue.getValue())) {
                continue;
            }

            Document document = new Document();
            String keyword = null;
            if(startTokenJoiner.length() == 0) {
                keyword = facetValue.getValue();
            } else {
                keyword = startToken + " " + facetValue.getValue();
            }
            document.addValue("suggest", keyword);
            suggestResult.addDocument(document);
        }

        pipelineContainer.putSearchResult(type + "Processed", suggestResult);
        return pipelineContainer;
    }

    private void mergeFacet(Map<String, FacetValue> mergedFacet, SearchResult searchResult, String fieldName, Set<String> startTokens ) {
        for(FacetValue facetValue : searchResult.getFacetById(fieldName).getValues()) {
            if (startTokens.contains(facetValue.getValue())) {
                continue;
            }
            FacetValue f = mergedFacet.get(facetValue.getValue());
            if(f ==  null) {
                mergedFacet.put(facetValue.getValue(), facetValue);
            } else {
                f.setCount(f.getCount() + facetValue.getCount());
            }

        }
    }
}
