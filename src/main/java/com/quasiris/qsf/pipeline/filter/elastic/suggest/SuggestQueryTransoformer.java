package com.quasiris.qsf.pipeline.filter.elastic.suggest;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.filter.elastic.ElasticParameterQueryTransformer;
import com.quasiris.qsf.pipeline.filter.elastic.Profiles;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.util.JsonUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class SuggestQueryTransoformer extends ElasticParameterQueryTransformer {


    private String matchAllProfile = Profiles.matchAll();

    private List<String> suggestFields;

    public SuggestQueryTransoformer(List<String> suggestFields) {
        this.suggestFields = suggestFields;
    }

    public SuggestQueryTransoformer() {
    }

    @Override
    public void transformParameter() {

        SearchQuery searchQuery = getPipelineContainer().getSearchQuery();
        String query = searchQuery.getQ();
        if(query.endsWith(" ")) {
            query = query + "*";
        }
        String[] tokenizedQuery = query.split(" ");
        String lastToken = tokenizedQuery[tokenizedQuery.length - 1];
        StringJoiner startTokenJoiner = new StringJoiner(" ");
        Set<String> startTokens = new HashSet<>();

        for (int i = 0; i < tokenizedQuery.length - 1; i++) {
            startTokenJoiner.add(tokenizedQuery[i]);
            startTokens.add(tokenizedQuery[i].toLowerCase());
        }
        String startToken = startTokenJoiner.toString();
        searchQuery.setQ(startToken);
        searchQuery.setRows(0);

        SuggestContext suggestContext = new SuggestContext();
        suggestContext.setSuggestFields(suggestFields);
        suggestContext.setStartTokens(startTokens);
        suggestContext.setStartTokenJoiner(startTokenJoiner);
        getPipelineContainer().putContext("suggestContext", suggestContext);

        if(startTokenJoiner.length() == 0) {
            this.profile = matchAllProfile;
        }

        for(String suggestField: suggestFields) {
            addAggregation(createSuggestFacet(suggestField, query, lastToken));
        }


        super.transformParameter();
    }

    private Facet createSuggestFacet(String fieldName, String query, String lastToken) {
        Facet suggest = new Facet();
        suggest.setId(fieldName);
        suggest.setName(fieldName);

        if(!Strings.isNullOrEmpty(query) && !"*".equals(lastToken)) {
            suggest.setInclude(JsonUtil.encode(lastToken.toLowerCase()) + ".*");
        }
        return suggest;

    }

    /**
     * Getter for property 'suggestFields'.
     *
     * @return Value for property 'suggestFields'.
     */
    public List<String> getSuggestFields() {
        return suggestFields;
    }

    /**
     * Setter for property 'suggestFields'.
     *
     * @param suggestFields Value to set for property 'suggestFields'.
     */
    public void setSuggestFields(List<String> suggestFields) {
        this.suggestFields = suggestFields;
    }

    /**
     * Getter for property 'matchAllProfile'.
     *
     * @return Value for property 'matchAllProfile'.
     */
    public String getMatchAllProfile() {
        return matchAllProfile;
    }

    /**
     * Setter for property 'matchAllProfile'.
     *
     * @param matchAllProfile Value to set for property 'matchAllProfile'.
     */
    public void setMatchAllProfile(String matchAllProfile) {
        this.matchAllProfile = matchAllProfile;
    }
}
