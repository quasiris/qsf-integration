package com.quasiris.qsf.response;

import com.quasiris.qsf.TestHelper;
import com.quasiris.qsf.dto.response.SearchResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchResponseFactoryTest {

    @Test
    void create() {
    }

    @Test
    void createEmptyTestNotNullString() {
        long time = 12L;
        String query = "*";
        SearchResponse searchResponse = SearchResponseFactory.createEmpty(query, time);
        TestHelper.checkNearlySameTime(searchResponse.getCurrentTime());
        Assertions.assertEquals(time, searchResponse.getTime());
        Assertions.assertEquals(query, searchResponse.getRequest().getQuery());
        Assertions.assertEquals(200, searchResponse.getStatusCode());
    }

    @Test
    void createEmptyTestNullString() {
        long time = 12L;
        SearchResponse searchResponse = SearchResponseFactory.createEmpty(null, time);
        TestHelper.checkNearlySameTime(searchResponse.getCurrentTime());
        Assertions.assertEquals(time, searchResponse.getTime());
        Assertions.assertNull(searchResponse.getRequest());
        Assertions.assertEquals(200, searchResponse.getStatusCode());
    }
}