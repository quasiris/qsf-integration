package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchFilterBuilder;

import java.util.regex.Pattern;

public class CategorySelectBuilder {
    private static String valueSplitChars = "|___|";

    public static SearchFilter getFilterForLevel(String id, int level, String filterValue) {
        if(level < 0) {
            return null;
        }
        String[] values = filterValue.split(Pattern.quote(valueSplitChars));
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= level; i++) {
            if(i > 0) {
                sb.append(valueSplitChars);
            }
            sb.append(values[i]);
        }
        SearchFilter searchFilter = SearchFilterBuilder.create().
                withId(id + level + ".keyword").
                value(sb.toString()).
                build();
        return searchFilter;
    }

}
