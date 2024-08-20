package com.quasiris.qsf.pipeline.filter.mapper;

import java.util.regex.Pattern;

public class CategorySelectFacetKeyMapper implements FacetKeyMapper {


    @Override
    public String map(Object value) {
        String[] categories = value.toString().split(Pattern.quote("|___|"));

        String lastCategory = categories[categories.length -1];

        String[] categorySplitted =  lastCategory.split(Pattern.quote("|-|"));

        String name = categorySplitted[categorySplitted.length -1];

        return name;
    }
}
