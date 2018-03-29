package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by mki on 03.12.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleSearchResponse extends ArrayList<Map<String, Object>> {


    public static SimpleSearchResponse create(SearchResult searchResult) {
        SimpleSearchResponse searchResponse = new SimpleSearchResponse();
        if(searchResult == null) {
            return searchResponse;
        }
        if(searchResult.getDocuments() == null) {
            return searchResponse;
        }

        for(Document document : searchResult.getDocuments()) {
            searchResponse.add(document.getDocument());
        }
        return searchResponse;
    }
}
