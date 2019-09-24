package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by mki on 19.11.17.
 */
public class ElasticResultTest {

    @Test
    public void deserializeUntilVersion6() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String file = "src/test/resources/com/quasiris/qsf/pipeline/filter/elastic/bean/elastic.json";
        ElasticResult elasticResult = objectMapper.readValue(new File(file), ElasticResult.class);

        Assert.assertEquals("Moosbach",elasticResult.getHits().getHits().get(0).get_source().get("name").asText());
        Assert.assertEquals(139727L,elasticResult.getHits().getTotal().longValue());


        Assert.assertEquals("village", elasticResult.getAggregations().get("places").getBuckets().get(0).getKey());
        Assert.assertEquals(Long.valueOf(7427L), elasticResult.getAggregations().get("places").getBuckets().get(0).getDoc_count());
    }

    @Test
    public void deserializeVersion7AndNewer() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String file = "src/test/resources/com/quasiris/qsf/pipeline/filter/elastic/bean/elastic7.json";
        ElasticResult elasticResult = objectMapper.readValue(new File(file), ElasticResult.class);

        Assert.assertEquals("Moosbach",elasticResult.getHits().getHits().get(0).get_source().get("name").asText());
        Assert.assertEquals(139727L,elasticResult.getHits().getTotal().longValue());


        Assert.assertEquals("village", elasticResult.getAggregations().get("places").getBuckets().get(0).getKey());
        Assert.assertEquals(Long.valueOf(7427L), elasticResult.getAggregations().get("places").getBuckets().get(0).getDoc_count());
    }
}
