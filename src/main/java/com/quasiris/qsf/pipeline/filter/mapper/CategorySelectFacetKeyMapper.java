package com.quasiris.qsf.pipeline.filter.mapper;

import java.util.regex.Pattern;

public class CategorySelectFacetKeyMapper implements FacetKeyMapper {


    @Override
    public String map(String value) {
        String[] categories = value.split(Pattern.quote("|___|"));

        String lastCategory = categories[categories.length -1];

        String[] categorySplitted =  lastCategory.split(Pattern.quote("|-|"));

        String name = categorySplitted[categorySplitted.length -1];

        return name;
    }
}
