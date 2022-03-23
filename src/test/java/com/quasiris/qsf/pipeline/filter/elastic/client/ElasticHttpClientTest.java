package com.quasiris.qsf.pipeline.filter.elastic.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElasticHttpClientTest {

    @Test
    void postAsync() {
        ElasticHttpClient.postAsync("http://elastic-lb.quasiris.de:19200", "");
    }
}