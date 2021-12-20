package com.quasiris.qsf.query;

import com.quasiris.qsf.dto.query.BaseSearchFilterDTO;
import com.quasiris.qsf.dto.query.BoolSearchFilterDTO;
import com.quasiris.qsf.dto.query.SearchFilterDTO;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.dto.query.SortDTO;

import java.util.ArrayList;
import java.util.List;

public class SearchQueryDTOMapper {

    public static SearchQueryDTO map(SearchQuery searchQuery) {
        SearchQueryDTO searchQueryDTO = new SearchQueryDTO();
        searchQueryDTO.setQ(searchQuery.getQ());
        searchQueryDTO.setTracking(searchQuery.getTracking());
        searchQueryDTO.setRequestOrigin(searchQuery.getRequestOrigin());
        searchQueryDTO.setPage(searchQuery.getPage());
        searchQueryDTO.setRows(searchQuery.getRows());

        SortDTO sort = map(searchQuery.getSort());
        searchQueryDTO.setSort(sort);


        List<BaseSearchFilterDTO> searchFiltersDTO = map(searchQuery.getSearchFilterList());
        searchQueryDTO.setSearchFilters(searchFiltersDTO);

        return searchQueryDTO;
    }


    protected static SortDTO map(Sort sort) {
        if(sort == null) {
            return null;
        }
        SortDTO sortDTO = new SortDTO();
        sortDTO.setSort(sort.getSort());

        sortDTO.setField(sort.getField());
        sortDTO.setDirection(sort.getDirection());

        return sortDTO;
    }

    protected static List<BaseSearchFilterDTO> map(List<BaseSearchFilter> baseSearchFilters) {
        List<BaseSearchFilterDTO> mappedFilters = new ArrayList<>();
        if(baseSearchFilters == null) {
            return mappedFilters;
        }
        for(BaseSearchFilter baseSearchFilter : baseSearchFilters) {
            if(baseSearchFilter instanceof SearchFilter) {
                SearchFilter searchFilter = (SearchFilter) baseSearchFilter;
                mappedFilters.add(map(searchFilter));
            } else if(baseSearchFilter instanceof BoolSearchFilter) {
                BoolSearchFilter boolSearchFilter = (BoolSearchFilter) baseSearchFilter;
                BoolSearchFilterDTO boolSearchFilterDTO = new BoolSearchFilterDTO();
                boolSearchFilterDTO.setOperator(com.quasiris.qsf.dto.query.FilterOperator.valueOf(boolSearchFilter.getOperator().toString()));
                List<BaseSearchFilterDTO> filters = map(boolSearchFilter.getFilters());
                boolSearchFilterDTO.setFilters(filters);
                mappedFilters.add(boolSearchFilterDTO);
            }
        }
        return mappedFilters;
    }

    protected static SearchFilterDTO map(SearchFilter searchFilter) {
        SearchFilterDTO searchFilterDTO = new SearchFilterDTO();
        searchFilterDTO.setId(searchFilter.getId());
        searchFilterDTO.setName(searchFilter.getId());
        if(searchFilter.getFilterType() == null) {
            searchFilter.setFilterType(FilterType.TERM);
        }
        searchFilterDTO.setFilterType(com.quasiris.qsf.dto.query.FilterType.valueOf(searchFilter.getFilterType().toString()));

        if(searchFilter.getFilterDataType() == null) {
            searchFilter.setFilterDataType(FilterDataType.STRING);
        }
        searchFilterDTO.setFilterDataType(com.quasiris.qsf.dto.query.FilterDataType.valueOf(searchFilter.getFilterDataType().toString()));


        if(searchFilter.getFilterOperator() == null) {
            searchFilter.setFilterOperator(FilterOperator.AND);
        }
        searchFilterDTO.setFilterOperator(com.quasiris.qsf.dto.query.FilterOperator.valueOf(searchFilter.getFilterOperator().toString()));


        mapTermFilter(searchFilter, searchFilterDTO);
        mapRangeFilter(searchFilter, searchFilterDTO);
        mapDateRangeFilter(searchFilter, searchFilterDTO);

        return searchFilterDTO;
    }

    protected static void mapTermFilter(SearchFilter searchFilter, SearchFilterDTO searchFilterDTO) {
        if(!(searchFilter.getFilterType() == FilterType.TERM)) {
            return;
        }
        searchFilterDTO.setFilterType(com.quasiris.qsf.dto.query.FilterType.TERM);
        if(searchFilterDTO.getFilterOperator() == null) {
            searchFilterDTO.setFilterOperator(com.quasiris.qsf.dto.query.FilterOperator.OR);
        }
        searchFilterDTO.setValues(mapValues(searchFilter.getValues()));
    }

    protected static void mapRangeFilter(SearchFilter searchFilter, SearchFilterDTO searchFilterDTO) {
        if(!searchFilter.getFilterDataType().isNumber()) {
            return;
        }
        searchFilterDTO.setMinValue(searchFilter.getMinValue());
        searchFilterDTO.setMaxValue(searchFilter.getMaxValue());
        searchFilterDTO.setFilterType(com.quasiris.qsf.dto.query.FilterType.RANGE);
        searchFilterDTO.setFilterDataType(com.quasiris.qsf.dto.query.FilterDataType.NUMBER);
    }

    protected static void mapDateRangeFilter(SearchFilter searchFilter, SearchFilterDTO searchFilterDTO) {
        if(searchFilter.getFilterType() == FilterType.RANGE && searchFilter.getFilterDataType().isDate()) {
            searchFilterDTO.setMinValue(searchFilter.getMinValue());
            searchFilterDTO.setMaxValue(searchFilter.getMaxValue());
            searchFilterDTO.setFilterType(com.quasiris.qsf.dto.query.FilterType.RANGE);
            searchFilterDTO.setFilterDataType(com.quasiris.qsf.dto.query.FilterDataType.DATE);
        }
    }

    protected static List<Object> mapValues(List<String> values) {
        List<Object> mappedValues = new ArrayList<>();
        mappedValues.addAll(values);
        return mappedValues;
    }
}
