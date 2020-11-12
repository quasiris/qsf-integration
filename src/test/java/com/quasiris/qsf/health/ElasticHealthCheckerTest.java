package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.filter.elastic.MockElasticClient;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ElasticHealthCheckerTest {


    @BeforeEach
    public void before() {
        ElasticClientFactory.setElasticClient(new MockElasticClient());
    }

    @Test
    public void isHealthyTrue() throws Exception {
        Assertions.assertTrue(ElasticHealthChecker.create().
                baseUrl("http://localhost:9214/osm").
                build().
                isHealthy());
    }

    @Test
    public void isHealthyFalse() throws Exception {
        Assertions.assertFalse(ElasticHealthChecker.create().
                baseUrl("http://localhost:9214/osm").
                predicate(sr -> sr.getTotal() > 21).
                build().
                isHealthy());
    }
}