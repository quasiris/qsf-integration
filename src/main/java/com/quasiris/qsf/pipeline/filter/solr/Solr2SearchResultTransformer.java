package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.dto.response.Document;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;

/**
 * Created by mki on 12.11.17.
 */
public class Solr2SearchResultTransformer extends Solr2SearchResultMappingTransformer implements SearchResultTransformerIF {


    public Solr2SearchResultTransformer() {


    }


    protected void mapFacets(QueryResponse queryResponse, SearchResult searchResult) {
        if(queryResponse.getFacetFields() == null) {
            return;
        }
        searchResult.setFacetCount(queryResponse.getFacetFields().size());
        for(FacetField facetField : queryResponse.getFacetFields()) {
            Facet facet = new Facet();
            facet.setValues(new ArrayList<>());
            facet.setId(facetField.getName());
            facet.setName(facetField.getName());
            facet.setFilterName(facetField.getName());

            facet.setCount(Long.valueOf(facetField.getValues().size()));
            Long facetReseultCount = 0L;
            for(FacetField.Count count: facetField.getValues()) {
                FacetValue facetValue = new FacetValue(count.getName(), count.getCount());
                facet.getValues().add(facetValue);
                facetReseultCount = facetReseultCount + facetValue.getCount();
            }
            facet.setResultCount(facetReseultCount);
            searchResult.addFacet(facet);
        }
    }

    public void transformField(Document document, String name, Object value) {
        document.getDocument().put(name, value);
    }
}
