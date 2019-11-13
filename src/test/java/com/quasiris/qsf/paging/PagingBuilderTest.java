package com.quasiris.qsf.paging;

import com.quasiris.qsf.response.Paging;
import org.junit.Assert;
import org.junit.Test;

public class PagingBuilderTest {

    @Test
    public void buildPagingExactRowsPerPage() {
        Paging paging = PagingBuilder.buildPaging(770L,12,10);
        Assert.assertEquals(Integer.valueOf(77), paging.getPageCount());
    }

    @Test
    public void buildPagingNoResults() {
        Paging paging = PagingBuilder.buildPaging(0L,12,10);
        Assert.assertEquals(Integer.valueOf(0), paging.getPageCount());
    }

    @Test
    public void buildPagingOnePageResults() {
        Paging paging = PagingBuilder.buildPaging(5L,12,10);
        Assert.assertEquals(Integer.valueOf(1), paging.getPageCount());
    }
}