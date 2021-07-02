package com.quasiris.qsf.paging;

import com.quasiris.qsf.dto.response.Page;
import com.quasiris.qsf.dto.response.Paging;

import java.math.BigDecimal;

public class PagingBuilder {


    public static Paging buildPaging(Long total, Integer currentPage, Integer rows) {
        if(currentPage == null) {
            currentPage = 1;
        }

        if(rows == null) {
            rows = 10;
        }



        int pageCount = BigDecimal.valueOf(total).
                divide(BigDecimal.valueOf(rows), BigDecimal.ROUND_UP).
                intValue();

        Paging paging = new Paging();
        paging.setCurrentPage(currentPage);
        paging.setPageCount(pageCount);
        paging.setFirstPage(createPage(1, currentPage));
        paging.setLastPage(createPage(pageCount, currentPage));


        paging.setNextPage(createPage(Math.min(pageCount, (currentPage+1)), currentPage));
        paging.setPreviousPage(createPage(Math.max(1, (currentPage-1)), currentPage));

        paging.setRows(rows);

        return paging;
    }

    public static Page createPage(int number, Integer currentPage) {
        Page page = new Page();
        page.setCurrentPage(number == currentPage);
        page.setNumber(number);
        return page;
    }
}
