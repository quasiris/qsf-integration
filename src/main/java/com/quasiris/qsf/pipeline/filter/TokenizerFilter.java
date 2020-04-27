package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbl on 22.04.20.
 */
public class TokenizerFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        String query = pipelineContainer.getSearchQuery().getQ();
        String[] tokens = query.split(" ");
        List<Token> queryTokens = new ArrayList<>();
        for(String tokenString: tokens) {
            Token token = new Token(tokenString);
            queryTokens.add(token);
        }
        pipelineContainer.getSearchQuery().setQueryToken(queryTokens);
        return pipelineContainer;
    }
}
