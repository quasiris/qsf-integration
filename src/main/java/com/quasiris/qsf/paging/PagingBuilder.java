package com.quasiris.qsf.paging;

import com.quasiris.qsf.response.Page;
import com.quasiris.qsf.response.Paging;

public class PagingBuilder {


    public static Paging buildPaging(Long total, Integer currentPage, Integer rows) {
        if(currentPage == null) {
            currentPage = 1;
        }

        if(rows == null) {
            rows = 10;
        }

        Long pageCount = ((total / rows) +1);

        Paging paging = new Paging();
        paging.setCurrentPage(currentPage);
        paging.setPageCount(pageCount.intValue());
        paging.setFirstPage(createPage(1, currentPage));
        paging.setLastPage(createPage(pageCount.intValue(), currentPage));


        paging.setNextPage(createPage(Math.min(pageCount.intValue(), (currentPage+1)), currentPage));
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
