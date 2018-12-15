package com.quasiris.qsf.pipeline.filter.solr;

import org.apache.solr.client.solrj.SolrClient;

public class SolrClientFactory {


    private static SolrClient solrClient;

    public static SolrClient getSolrClient() {
        return solrClient;
    }

    public static void setSolrClient(SolrClient solrClient) {
        SolrClientFactory.solrClient = solrClient;
    }
}
