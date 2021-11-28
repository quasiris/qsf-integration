package com.quasiris.qsf.query;

import com.quasiris.qsf.commons.text.date.HumanDateParser;
import com.quasiris.qsf.commons.text.date.SimpleDateParser;
import com.quasiris.qsf.dto.query.SearchFilterDTO;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.dto.query.SortDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SearchQueryMapper {

    public static SearchQuery map(SearchQueryDTO searchQueryDTO) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ(searchQueryDTO.getQ());
        searchQuery.setTracking(searchQueryDTO.getTracking());
        searchQuery.setRequestOrigin(searchQueryDTO.getRequestOrigin());
        searchQuery.setRows(searchQueryDTO.getRows());

        Sort sort = map(searchQueryDTO.getSort());
        searchQuery.setSort(sort);


        Integer page = 1;
        if(searchQueryDTO.getPage() != null && searchQueryDTO.getPage() > 0) {
            page = searchQueryDTO.getPage();
        }
        searchQuery.setPage(page);

        if(searchQuery.getParameters() == null) {
            searchQuery.setParameters(new HashMap<>());
        }
        if(searchQueryDTO.getParameters() != null) {
            searchQuery.getParameters().putAll(searchQueryDTO.getParameters());
        }



        List<SearchFilter> searchFilters = map(searchQueryDTO.getSearchFilters());
        searchQuery.setSearchFilterList(searchFilters);

        searchQuery.setResult(searchQueryDTO.getResult());

        return searchQuery;
    }

    public static Sort map(SortDTO sortDTO) {
        if(sortDTO == null) {
            return null;
        }
        Sort sort = new Sort();
        sort.setSort(sortDTO.getSort());

        sort.setField(sortDTO.getField());
        sort.setDirection(sortDTO.getDirection());

        return sort;
    }

    public static List<SearchFilter> map(List<SearchFilterDTO> searchFilters) {
        List<SearchFilter> mappedFilters = new ArrayList<>();
        if(searchFilters == null) {
            return mappedFilters;
        }
        for(SearchFilterDTO searchFilter : searchFilters) {
            mappedFilters.add(map(searchFilter));
        }
        return mappedFilters;
    }

    public static SearchFilter map(SearchFilterDTO searchFilterDTO) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setId(searchFilterDTO.getId());
        searchFilter.setName(searchFilterDTO.getName());
        if(searchFilterDTO.getFilterType() == null) {
            searchFilterDTO.setFilterType(com.quasiris.qsf.dto.query.FilterType.TERM);
        }
        if(searchFilterDTO.getFilterType().isRelativeRange()) {
            searchFilter.setFilterType(FilterType.RANGE);
        } else {
            searchFilter.setFilterType(FilterType.valueOf(searchFilterDTO.getFilterType().toString()));
        }

        if(searchFilterDTO.getFilterDataType() == null) {
            searchFilterDTO.setFilterDataType(com.quasiris.qsf.dto.query.FilterDataType.STRING);
        }
        searchFilter.setFilterDataType(FilterDataType.valueOf(searchFilterDTO.getFilterDataType().toString()));


        if(searchFilterDTO.getFilterOperator() == null) {
            searchFilterDTO.setFilterOperator(com.quasiris.qsf.dto.query.FilterOperator.AND);
        }
        searchFilter.setFilterOperator(FilterOperator.valueOf(searchFilterDTO.getFilterOperator().toString()));


        mapTermFilter(searchFilterDTO, searchFilter);
        mapRangeFilter(searchFilterDTO, searchFilter);
        mapDateRangeFilter(searchFilterDTO, searchFilter);
        mapRelativeDateRangeFilter(searchFilterDTO, searchFilter);

        return searchFilter;
    }

    public static void mapTermFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(!searchFilterDTO.getFilterType().isTerm()) {
            return;
        }
        searchFilter.setFilterType(FilterType.MATCH);
        if(searchFilter.getFilterOperator() == null) {
            searchFilter.setFilterOperator(FilterOperator.OR);
        }
        searchFilter.setValues(mapValues(searchFilterDTO.getValues()));
    }

    public static void mapRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(!searchFilterDTO.getFilterDataType().isNumber()) {
            return;
        }
        RangeFilterValue<Number> rangeFilterValue = new RangeFilterValue<>();
        rangeFilterValue.setMinValue(((Number) searchFilterDTO.getMinValue()).doubleValue());
        rangeFilterValue.setMaxValue(((Number) searchFilterDTO.getMaxValue()).doubleValue());
        searchFilter.setRangeValue(rangeFilterValue);
        searchFilter.setFilterType(FilterType.RANGE);
        searchFilter.setFilterDataType(FilterDataType.NUMBER);
    }

    public static void mapDateRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(searchFilterDTO.getFilterType().isRange() && searchFilterDTO.getFilterDataType().isDate()) {
            RangeFilterValue<Date> rangeFilterValue = new RangeFilterValue<>();
            SimpleDateParser parserMin = new SimpleDateParser(searchFilterDTO.getMinValue().toString());
            rangeFilterValue.setMinValue(parserMin.getDate());

            SimpleDateParser parserMax = new SimpleDateParser(searchFilterDTO.getMaxValue().toString());
            rangeFilterValue.setMaxValue(parserMax.getDate());
            searchFilter.setRangeValue(rangeFilterValue);
            searchFilter.setFilterType(FilterType.RANGE);
            searchFilter.setFilterDataType(FilterDataType.DATE);
        }
    }

    public static void mapRelativeDateRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(searchFilterDTO.getFilterType().isRelativeRange() && searchFilterDTO.getFilterDataType().isDate()) {
            RangeFilterValue<Date> rangeFilterValue = new RangeFilterValue<>();
            HumanDateParser parser = new HumanDateParser(searchFilterDTO.getValues().get(0).toString());
            rangeFilterValue.setMinValue(Date.from(parser.getStart()));
            rangeFilterValue.setMaxValue(Date.from(parser.getEnd()));
            searchFilter.setRangeValue(rangeFilterValue);
            searchFilter.setFilterType(FilterType.RANGE);
            searchFilter.setFilterDataType(FilterDataType.DATE);
        }

    }


    public static List<String> mapValues(List<Object> values) {
        List<String> mappedValues = new ArrayList<>();
        for(Object value : values) {
            mappedValues.add(String.valueOf(value));
        }

        return mappedValues;


    }
}
