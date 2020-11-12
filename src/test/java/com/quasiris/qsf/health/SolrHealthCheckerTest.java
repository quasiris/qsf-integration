package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import com.quasiris.qsf.pipeline.filter.solr.SolrClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SolrHealthCheckerTest {

    private String baseUrl = "http://localhost:8983/solr/gettingstarted";

    @BeforeEach
    public void before() {
        SolrClientFactory.setSolrClient(new MockSolrClient(baseUrl), baseUrl);
    }


    @Test
    public void isHealthyTrue() throws Exception {

        Assertions.assertTrue(SolrHealthChecker.create().
                baseUrl(baseUrl).
                build().
                isHealthy());
    }

    @Test
    public void isHealthyFalse() throws Exception {

        Assertions.assertFalse(SolrHealthChecker.create().
                baseUrl(baseUrl).
                predicate(sr -> sr.getTotal() > 21).
                build().
                isHealthy());
    }
}