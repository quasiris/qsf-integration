package com.quasiris.qsf.query;

import com.quasiris.qsf.commons.text.date.HumanDateParser;
import com.quasiris.qsf.commons.text.date.SupportedDateFormatsParser;
import com.quasiris.qsf.dto.error.SearchQueryException;
import com.quasiris.qsf.dto.query.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SearchQueryMapper {

    private Integer defaultPage = 1;

    private FilterType defaultFilterType = FilterType.TERM;

    private FilterDataType defaultFilterDataType = FilterDataType.STRING;
    private FilterOperator defaultFilterOperator = FilterOperator.OR;

    public static SearchQuery map(SearchQueryDTO searchQueryDTO) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQ(searchQueryDTO.getQ());
        searchQuery.setTracking(searchQueryDTO.getTracking());
        searchQuery.setRequestOrigin(searchQueryDTO.getRequestOrigin());
        searchQuery.setRows(searchQueryDTO.getRows());
        if(searchQueryDTO.getTrackingTags() != null) {
            searchQuery.setTrackingTags(new ArrayList<>(searchQueryDTO.getTrackingTags()));
        }

        Sort sort = map(searchQueryDTO.getSort());
        searchQuery.setSort(sort);

        searchQuery.setPage(searchQueryDTO.getPage());
        searchQuery.setNextPageToken(searchQueryDTO.getNextPageToken());

        if(searchQuery.getParameters() == null) {
            searchQuery.setParameters(new HashMap<>());
        }
        if(searchQueryDTO.getParameters() != null) {
            searchQuery.getParameters().putAll(searchQueryDTO.getParameters());
        }



        List<BaseSearchFilter> searchFilters = map(searchQueryDTO.getSearchFilters());
        searchQuery.setSearchFilterList(searchFilters);

        searchQuery.setResult(searchQueryDTO.getResult());

        if(searchQueryDTO.getRequestId() == null) {
            searchQuery.setRequestId(UUID.randomUUID().toString());
        } else {
            searchQuery.setRequestId(searchQueryDTO.getRequestId());
        }
        searchQuery.setSessionId(searchQueryDTO.getSessionId());
        searchQuery.setUserId(searchQueryDTO.getUserId());

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

    public static List<BaseSearchFilter> map(List<BaseSearchFilterDTO> baseSearchFilters) {
        List<BaseSearchFilter> mappedFilters = new ArrayList<>();
        if(baseSearchFilters == null) {
            return mappedFilters;
        }
        for(BaseSearchFilterDTO baseSearchFilterDTO : baseSearchFilters) {
            if(baseSearchFilterDTO instanceof SearchFilterDTO) {
                SearchFilterDTO searchFilterDTO = (SearchFilterDTO) baseSearchFilterDTO;
                mappedFilters.add(map(searchFilterDTO));
            } else if(baseSearchFilterDTO instanceof BoolSearchFilterDTO) {
                BoolSearchFilterDTO boolSearchFilterDTO = (BoolSearchFilterDTO) baseSearchFilterDTO;
                BoolSearchFilter boolSearchFilter = new BoolSearchFilter();
                boolSearchFilter.setOperator(FilterOperator.valueOf(boolSearchFilterDTO.getOperator().toString()));
                List<BaseSearchFilter> filters = map(boolSearchFilterDTO.getFilters());
                boolSearchFilter.setFilters(filters);
                mappedFilters.add(boolSearchFilter);
            }
        }
        return mappedFilters;
    }

    public static SearchFilter map(SearchFilterDTO searchFilterDTO) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setId(searchFilterDTO.getId());
        searchFilter.setName(searchFilterDTO.getName());

        if(searchFilterDTO.getFilterType() != null && searchFilterDTO.getFilterType().isRelativeRange()) {
            searchFilter.setFilterType(FilterType.RANGE);
        }

        if(searchFilterDTO.getFilterDataType() != null) {
            searchFilter.setFilterDataType(FilterDataType.valueOf(searchFilterDTO.getFilterDataType().toString()));
        }


        if(searchFilterDTO.getFilterOperator() != null) {
            searchFilter.setFilterOperator(FilterOperator.valueOf(searchFilterDTO.getFilterOperator().toString()));
        }

        mapTermFilter(searchFilterDTO, searchFilter);
        mapRangeFilter(searchFilterDTO, searchFilter);
        mapDateRangeFilter(searchFilterDTO, searchFilter);
        mapRelativeDateRangeFilter(searchFilterDTO, searchFilter);

        return searchFilter;
    }

    public static void mapTermFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(searchFilterDTO.getValues() == null) {
            return;
        }
        if(searchFilterDTO.getFilterType()  != null && !searchFilterDTO.getFilterType().isTerm()) {
            return;
        }
        if(searchFilterDTO.getFilterOperator() != null) {
            searchFilter.setFilterOperator(FilterOperator.valueOf(searchFilterDTO.getFilterOperator().getCode().toUpperCase()));
        }
        searchFilter.setFilterType(FilterType.TERM);
        searchFilter.setValues(mapValues(searchFilterDTO.getValues()));
    }

    public static void mapRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(isNumberDataType(searchFilterDTO)) {
            try {
                RangeFilterValue<Number> rangeFilterValue = new RangeFilterValue<>();
                rangeFilterValue.setMinValue(parseObjectAsDouble(searchFilterDTO.getMinValue()));
                rangeFilterValue.setMaxValue(parseObjectAsDouble(searchFilterDTO.getMaxValue()));
                searchFilter.setRangeValue(rangeFilterValue);
                searchFilter.setFilterType(FilterType.RANGE);
                searchFilter.setFilterDataType(FilterDataType.NUMBER);
            } catch (SearchQueryException e) {
                throw new SearchQueryException(e.getMessage() + " filter: " + searchFilter.getId());
            }
        }
    }

    public static void mapDateRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(isRangeFilter(searchFilterDTO) && isDateDataType(searchFilterDTO)) {
            RangeFilterValue<String> rangeFilterValue = new RangeFilterValue<>();

            if (searchFilterDTO.getMinValue() != null) {
                rangeFilterValue.setMinValue(SupportedDateFormatsParser.requireFromString(searchFilterDTO.getMinValue().toString()));
            }

            if (searchFilterDTO.getMaxValue() != null) {
                rangeFilterValue.setMaxValue(SupportedDateFormatsParser.requireFromString(searchFilterDTO.getMaxValue().toString()));
            }

            searchFilter.setRangeValue(rangeFilterValue);
            searchFilter.setFilterType(FilterType.RANGE);
            searchFilter.setFilterDataType(FilterDataType.DATE);
        }
    }

    public static boolean isRangeFilter(SearchFilterDTO searchFilterDTO) {
        if(searchFilterDTO.getFilterType() == null) {
            return false;
        }
        return searchFilterDTO.getFilterType().isRange();
    }

    public static boolean isDateDataType(SearchFilterDTO searchFilterDTO) {
        if(searchFilterDTO.getFilterDataType() == null) {
            if(isString(searchFilterDTO.getMinValue())) {
                return true;
            }
            if(isString(searchFilterDTO.getMaxValue())) {
                return true;
            }
        } else {
            return searchFilterDTO.getFilterDataType().isDate();
        }
        return false;
    }
    public static boolean isNumberDataType(SearchFilterDTO searchFilterDTO) {
        if(searchFilterDTO.getFilterDataType() == null) {
            if(isNumber(searchFilterDTO.getMinValue())) {
                return true;
            }
            if(isNumber(searchFilterDTO.getMaxValue())) {
                return true;
            }
        } else {
            return searchFilterDTO.getFilterDataType().isNumber();
        }
        return false;
    }

    public static boolean isNumber(Object value) {
        if(value == null) {
            return false;
        }
        return !isString(value);
    }

    public static boolean isString(Object value) {
        if(value == null) {
            return false;
        }
        return value instanceof String;
    }

    public static void mapRelativeDateRangeFilter(SearchFilterDTO searchFilterDTO, SearchFilter searchFilter) {
        if(searchFilterDTO.getFilterType() != null &&
                searchFilterDTO.getFilterType().isRelativeRange() &&
                searchFilterDTO.getFilterDataType().isDate()) {
            RangeFilterValue<String> rangeFilterValue = new RangeFilterValue<>();
            HumanDateParser parser = new HumanDateParser(searchFilterDTO.getValues().get(0).toString());
            rangeFilterValue.setMinValue(SupportedDateFormatsParser.requireFromInstant(parser.getStart()));
            rangeFilterValue.setMaxValue(SupportedDateFormatsParser.requireFromInstant(parser.getEnd()));
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

    public void applyDefaults(SearchQuery searchQuery) {
        if(searchQuery.getPage() == null) {
            searchQuery.setPage(defaultPage);
        }
        searchQuery.getAllSearchFilters().forEach(this::applyDefaultsForFilter);


    }
    public SearchFilter applyDefaultsForFilter(SearchFilter  searchFilter) {
        if(searchFilter.getName() == null) {
            searchFilter.setName(searchFilter.getId());
        }
        if(searchFilter.getFilterType() == null) {
            searchFilter.setFilterType(defaultFilterType);
        }
        if(searchFilter.getFilterDataType() == null) {
            searchFilter.setFilterDataType(defaultFilterDataType);
        }

        if(searchFilter.getFilterOperator() == null) {
            searchFilter.setFilterOperator(defaultFilterOperator);
        }
        return searchFilter;
    }

    public Integer getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(Integer defaultPage) {
        this.defaultPage = defaultPage;
    }

    public FilterType getDefaultFilterType() {
        return defaultFilterType;
    }

    public void setDefaultFilterType(FilterType defaultFilterType) {
        this.defaultFilterType = defaultFilterType;
    }

    public FilterDataType getDefaultFilterDataType() {
        return defaultFilterDataType;
    }

    public void setDefaultFilterDataType(FilterDataType defaultFilterDataType) {
        this.defaultFilterDataType = defaultFilterDataType;
    }

    public FilterOperator getDefaultFilterOperator() {
        return defaultFilterOperator;
    }

    public void setDefaultFilterOperator(FilterOperator defaultFilterOperator) {
        this.defaultFilterOperator = defaultFilterOperator;
    }

    public static Double parseObjectAsDouble(Object obj) throws IllegalArgumentException {
        if(obj == null) {
            return null;
        }
        try {
            if (obj instanceof String) {
                return Double.parseDouble((String) obj);
            } else if (obj instanceof Double) {
                return (Double) obj;
            } else if (obj instanceof Integer) {
                return ((Integer) obj).doubleValue();
            } else if (obj instanceof Long) {
                return ((Long) obj).doubleValue();
            } else {
                throw new SearchQueryException("Can not parse " + obj + " to a Double value");
            }
        } catch (SearchQueryException e) {
            throw e;
        } catch (Exception e) {
            throw new SearchQueryException("Can not parse " + obj + " to a Double value becaouse: " + e.getMessage());
        }
    }

}
