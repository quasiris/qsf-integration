package com.quasiris.qsf.pipeline.filter.elastic.client;

public class ElasticClientFactory {


    private static ElasticClientIF elasticClient;
    private static MultiElasticClientIF multiElasticClient;

    public static ElasticClientIF getElasticClient() {
        if(elasticClient == null) {
            return new StandardElasticClient();
        }
        return elasticClient;
    }

    public static void setElasticClient(ElasticClientIF elasticClient) {
        ElasticClientFactory.elasticClient = elasticClient;
    }


    public static MultiElasticClientIF getMulitElasticClient() {
        if(multiElasticClient == null) {
            return new StandardMultiElasticClient();
        }
        return multiElasticClient;
    }

    public static void setMulitElasticClient(MultiElasticClientIF elasticClient) {
        ElasticClientFactory.multiElasticClient = elasticClient;
    }
}
