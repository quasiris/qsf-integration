package com.quasiris.qsf.health;

import com.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import com.quasiris.qsf.pipeline.filter.solr.SolrClientFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SolrHealthCheckerTest {

    private String baseUrl = "http://localhost:8983/solr/gettingstarted";

    @Before
    public void before() {
        SolrClientFactory.setSolrClient(new MockSolrClient(baseUrl));
    }


    @Test
    public void isHealthyTrue() throws Exception {

        Assert.assertTrue(SolrHealthChecker.create().
                baseUrl(baseUrl).
                build().
                isHealthy());
    }

    @Test
    public void isHealthyFalse() throws Exception {

        Assert.assertFalse(SolrHealthChecker.create().
                baseUrl(baseUrl).
                predicate(sr -> sr.getTotal() > 21).
                build().
                isHealthy());
    }
}