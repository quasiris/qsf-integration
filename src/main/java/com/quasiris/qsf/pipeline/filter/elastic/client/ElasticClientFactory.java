package com.quasiris.qsf.pipeline.filter.elastic.client;

public class ElasticClientFactory {


    private static ElasticClientIF elasticClient;

    public static ElasticClientIF getElasticClient() {
        return elasticClient;
    }

    public static void setElasticClient(ElasticClientIF elasticClient) {
        ElasticClientFactory.elasticClient = elasticClient;
    }
}
