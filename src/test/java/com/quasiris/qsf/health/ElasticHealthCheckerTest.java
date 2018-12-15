package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.filter.elastic.MockElasticClient;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ElasticHealthCheckerTest {


    @Before
    public void before() {
        ElasticClientFactory.setElasticClient(new MockElasticClient());
    }

    @Test
    public void isHealthyTrue() throws Exception {
        Assert.assertTrue(ElasticHealthChecker.create().
                baseUrl("http://localhost:9214/osm").
                build().
                isHealthy());
    }

    @Test
    public void isHealthyFalse() throws Exception {
        Assert.assertFalse(ElasticHealthChecker.create().
                baseUrl("http://localhost:9214/osm").
                predicate(sr -> sr.getTotal() > 21).
                build().
                isHealthy());
    }
}