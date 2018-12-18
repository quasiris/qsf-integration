package com.quasiris.qsf.pipeline.filter.solr;

import org.apache.solr.client.solrj.SolrClient;

import java.util.HashMap;
import java.util.Map;

public class SolrClientFactory {


    private static Map<String, SolrClient> solrClientMap = new HashMap<>();

    public static SolrClient getSolrClient(String baseUrl) {
        return solrClientMap.get(baseUrl);
    }

    public static void setSolrClient(SolrClient solrClient, String baseUrl) {
        solrClientMap.put(baseUrl, solrClient);
    }
}
