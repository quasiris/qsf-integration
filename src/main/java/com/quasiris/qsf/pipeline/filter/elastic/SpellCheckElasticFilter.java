package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.MultiElasticClientIF;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Token;
import com.quasiris.qsf.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbl on 11.4.20.
 */
public class SpellCheckElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(SpellCheckElasticFilter.class);

    private int minTokenLenght = 4;

    private String baseUrl;

    private MultiElasticClientIF elasticClient;

    public SpellCheckElasticFilter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void init() {
        super.init();
        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getMulitElasticClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        process(pipelineContainer);
        return pipelineContainer;
    }


    private void process(PipelineContainer pipelineContainer) throws IOException, PipelineContainerException {

        List<String> elasticQueries = new ArrayList<>();
        for(Token token: pipelineContainer.getSearchQuery().getQueryToken()) {
            if(token.getValue().length() < minTokenLenght) {
                pipelineContainer.getTracking().addValue("tags", "spellcheck_ignore_tag_min_token_length");
                continue;
            }

            String elasticRequest  = "{\"query\": {\"fuzzy\": {\"text.keyword\": {\"value\": \"" + JsonUtil.encode(token.getValue().toLowerCase()) + "\",\"fuzziness\": \"AUTO\"}}}}";
            elasticQueries.add(elasticRequest);
        }

        if(elasticQueries.isEmpty()) {
            return;
        }

        MultiElasticResult multiElasticResult = elasticClient.request(baseUrl + "/_msearch", elasticQueries);

        boolean hasTypo = false;
        StringBuilder correctedBuilder = new StringBuilder();
        int ignoredTokenCount = 0;
        long spellcheckVariants = 0;
        long correctedWords = 0;

        for (int i = 0; i < pipelineContainer.getSearchQuery().getQueryToken().size() ; i++) {
            Token token = pipelineContainer.getSearchQuery().getQueryToken().get(i);

            String bestMatch = token.getValue();
            double maxScore = 0.0;
            boolean typo = true;

            if(token.getValue().length() < minTokenLenght) {
                ignoredTokenCount++;
                correctedBuilder.append(bestMatch).append(" ");
                continue;
            }

            int elasticPointer = i - ignoredTokenCount;
            ElasticResult elasticResult = multiElasticResult.getResponses().get(elasticPointer);
            if(elasticResult.getHits() == null || elasticResult.getHits().getHits().isEmpty() ) {
                pipelineContainer.getTracking().addValue("tags", "spellcheck_not_found");
                continue;
            }

            for(Hit hit: multiElasticResult.getResponses().get(elasticPointer).getHits().getHits()) {
                String text = getAsText(hit.get_source(), "text");
                if(fuzzyEquals(token.getValue(), text)) {
                    bestMatch = token.getValue();
                    typo = false;
                    break;
                }
                Double score = hit.get_score();
                String weightString = getAsText(hit.get_source(), "weight");
                Double weight = Double.valueOf(weightString);
                if(weight > 10) {
                    score = score + 100;
                }
                if(score > maxScore) {
                    bestMatch = text;
                    maxScore = score;
                }
            }

            if(typo) {
                hasTypo = true;
                spellcheckVariants = spellcheckVariants + elasticResult.getHits().getTotal();
                correctedWords++;
            }
            correctedBuilder.append(bestMatch).append(" ");
        }

        if(hasTypo) {
            String corrected = correctedBuilder.toString().trim();
            SearchQuery searchQuery = pipelineContainer.getSearchQuery();
            if(!fuzzyEquals(corrected, searchQuery.getQ())) {
                searchQuery.setOriginalQuery(searchQuery.getQ());
                searchQuery.setQ(corrected);
                searchQuery.setQueryChanged(true);
            }
            pipelineContainer.getTracking().setValue("spellcheckVariants", spellcheckVariants);
            pipelineContainer.getTracking().setValue("spellcheckCorrectedWords", correctedWords);
        }
    }



    private boolean fuzzyEquals(String left, String right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null || right == null) {
            return false;
        }
        left = normalize(left);
        right = normalize(right);
        return left.equals(right);
    }

    private String normalize(String value) {
        if(value == null) {
            return null;
        }
        value = value.toLowerCase();
        value = value.trim();
        return value;
    }

    private String getAsText(ObjectNode objectNode, String name) {
        JsonNode node = objectNode.get(name);
        if(node == null) {
            return null;
        }
        if(node.isArray()) {
            return node.get(0).asText();
        }
        return node.asText();

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
