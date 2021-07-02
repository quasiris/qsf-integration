package com.quasiris.qsf.pipeline.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.text.TextUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by mki on 9.12.17.
 */
public class SearchIntentLocationFilter extends AbstractFilter {


    private static Set<String> locationBlacklist = new HashSet<>();
    static{
        locationBlacklist.add("thomas");
        locationBlacklist.add("stephan");
        locationBlacklist.add("jürgen");
        locationBlacklist.add("peter");
        locationBlacklist.add("oswald");
        locationBlacklist.add("dr");

    }

    private Set<String> normalizeLocationNames(String highlightedLocationName) {
        if(highlightedLocationName == null) {
            return Collections.emptySet();
        }

        return getHighlightedValues(highlightedLocationName).
                stream().
                map(SearchIntentLocationFilter::normalizeToken).
                collect(Collectors.toSet());
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        SearchResult locationLookup = pipelineContainer.getSearchResult("locationLookup");
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        if(locationLookup == null) {
            return pipelineContainer;
        }


        int relevantDocuments = 2;
        int currentDocument = 0;
        Set<String> normalizedLocationNames = new HashSet<>();
        for(Document document : locationLookup.getDocuments()) {
            if(currentDocument >= relevantDocuments) {
                break;
            }
            currentDocument++;
            String highlightedLocationName = document.getFieldValue("highlight.name");
            normalizedLocationNames.addAll(normalizeLocationNames(highlightedLocationName));
        }




        List<String> locationTokens = new ArrayList<>();
        List<String> otherTokens = new ArrayList<>();
        for(String token :Splitter.on(" ").split(searchQuery.getQ())) {
            String normalizedToken = normalizeToken(token);
            if(TextUtils.isGermanPostalCode(token)) {
                otherTokens.add(token);
            } else if(locationBlacklist.contains(token)) {
                otherTokens.add(token);
            } else if(normalizedLocationNames.contains(normalizedToken)) {
                locationTokens.add(token);
            } else {
                otherTokens.add(token);
            }
        }

        SearchResult locationSearchIntent = new SearchResult();
        locationSearchIntent.setStatusCode(200);
        locationSearchIntent.setStatusMessage("OK");
        locationSearchIntent.setTotal(1L);


        Document document = new Document();
        String location = Joiner.on(" ").join(locationTokens);
        String other = Joiner.on(" ").join(otherTokens);

        document.getDocument().put("location", location);
        document.getDocument().put("other", other);
        locationSearchIntent.addDocument(document);

        pipelineContainer.putSearchResult("search-intent",locationSearchIntent);
        return pipelineContainer;
    }




    private static String normalizeToken(String token) {
        token = token.toLowerCase();
        token = TextUtils.replaceGermanUmlaut(token);
        return token;
    }



    private static List<String> getHighlightedValues(String str) {
        if(str==null) {
            return Collections.emptyList();
        }
        Pattern TAG_REGEX = Pattern.compile(".*?<em>(.*?)</em>.*?");
        final List<String> tagValues = new ArrayList<>();
        final Matcher matcher = TAG_REGEX.matcher(str);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

}
