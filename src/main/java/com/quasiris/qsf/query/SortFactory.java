package com.quasiris.qsf.query;

public class SortFactory {
    public static Sort create(String sortString) {
        Sort sort = new Sort(sortString);
        String[] sortSplitted = sortString.split(" ");
        if(sortSplitted.length == 1) {
            sort.setField(sortSplitted[0]);
            sort.setDirection("desc");
        } else if(sortSplitted.length == 2) {
            sort.setField(sortSplitted[0]);
            sort.setDirection(sortSplitted[1]);
        }
        return sort;
    }
}
