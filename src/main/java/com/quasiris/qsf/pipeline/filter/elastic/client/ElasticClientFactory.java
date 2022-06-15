package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.quasiris.qsf.commons.elasticsearch.client.ElasticAnalyzeClient;
import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;

public class ElasticClientFactory {


    private static ElasticClientIF elasticClient;
    private static ElasticSearchClient elasticSearchClient;
    private static ElasticAnalyzeClient elasticAnalyzeClient;
    private static MultiElasticClientIF multiElasticClient;

    @Deprecated
    public static ElasticClientIF getElasticClient() {
        if(elasticClient == null) {
            return new StandardElasticClient();
        }
        return elasticClient;
    }

    public static ElasticSearchClient getElasticSearchClient() {
        if(elasticSearchClient == null) {
            return new ElasticSearchClient();
        }
        return elasticSearchClient;
    }

    public static void setElasticSearchClient(ElasticSearchClient elasticClient) {
        ElasticClientFactory.elasticSearchClient = elasticClient;
    }

    public static ElasticAnalyzeClient getElasticAnalyzeClient() {
        if(elasticAnalyzeClient == null) {
            return new ElasticAnalyzeClient();
        }
        return elasticAnalyzeClient;
    }

    public static void setElasticAnalyzeClient(ElasticAnalyzeClient elasticClient) {
        ElasticClientFactory.elasticAnalyzeClient = elasticClient;
    }

    @Deprecated
    public static void setElasticClient(ElasticClientIF elasticClient) {
        ElasticClientFactory.elasticClient = elasticClient;
    }


    @Deprecated
    public static MultiElasticClientIF getMulitElasticClient() {
        if(multiElasticClient == null) {
            return new StandardMultiElasticClient();
        }
        return multiElasticClient;
    }

    @Deprecated
    public static void setMulitElasticClient(MultiElasticClientIF elasticClient) {
        ElasticClientFactory.multiElasticClient = elasticClient;
    }
}
