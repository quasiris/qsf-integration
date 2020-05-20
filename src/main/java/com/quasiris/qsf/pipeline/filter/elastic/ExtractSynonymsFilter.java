package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tbl on 21.04.20.
 */
public class ExtractSynonymsFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ExtractSynonymsFilter.class);



    @Override
    public void init() {
        super.init();
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        SearchResult analyze = pipelineContainer.getSearchResult("analyze");
        Map<String, Document> synonyms = new HashMap<>();
        for(Document document : analyze.getDocuments()) {
            String type = document.getFieldValue("type");
            if(!"SYNONYM".equals(type)) {
                continue;
            }
            Integer startOffset = document.getFieldValueAsInteger("start_offset");
            Integer endOffset = document.getFieldValueAsInteger("end_offset");
            String synonym = pipelineContainer.getSearchQuery().getQ().substring(startOffset, endOffset);
            String hashKey = synonym + startOffset + endOffset;
            synonyms.put(hashKey, document);
        }

        SearchResult synonymsResult = new SearchResult();
        synonymsResult.setDocuments(new ArrayList<>());
        for(Map.Entry<String, Document> synonymEntry: synonyms.entrySet()) {
            Document synonymsDocument = new Document();
            Integer startOffset = synonymEntry.getValue().getFieldValueAsInteger("start_offset");
            Integer endOffset = synonymEntry.getValue().getFieldValueAsInteger("end_offset");
            String synonym = pipelineContainer.getSearchQuery().getQ().substring(startOffset, endOffset);

            synonymsDocument.setValue("start_offset", startOffset);
            synonymsDocument.setValue("end_offset", endOffset);
            synonymsDocument.setValue("synonym", synonym);
            synonymsResult.getDocuments().add(synonymsDocument);
        }

        pipelineContainer.putSearchResult("synonyms", synonymsResult);

        return pipelineContainer;
    }

}
