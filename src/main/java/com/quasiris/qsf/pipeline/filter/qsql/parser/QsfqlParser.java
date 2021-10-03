package com.quasiris.qsf.pipeline.filter.qsql.parser;

import com.google.common.base.Strings;
import com.quasiris.qsf.commons.text.date.HumanDateParser;
import com.quasiris.qsf.commons.util.DateUtil;
import com.quasiris.qsf.commons.util.QsfInstant;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.text.Splitter;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParser {

    private static final Pattern filterPattern = Pattern.compile("f\\.([^\\.]+)(.*)");
    private static final Pattern facetFilterPattern = Pattern.compile("ff\\.([^\\.]+)(.*)");

    private Map<String, String[]> parameters;

    private SearchQuery query = null;

    public QsfqlParser(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }


    SearchQuery parse() {
        SearchQuery query = new SearchQuery();
        parseMeta(query);
        parseQuery(query);
        parsePaging(query);
        parseSort(query);
        parseFilter(query);
        parseFacetFilter(query);
        parseCtrl(query);
        parseParameter(query);
        return query;
    }

    void parseParameter(SearchQuery query) {
        Map<String, Object> copy = new HashMap<>();
        for(Map.Entry<String, String[]> entry : this.parameters.entrySet()) {
            if(entry.getValue().length == 1) {
                copy.put(entry.getKey(),entry.getValue()[0]);
            } else {
                copy.put(entry.getKey(), entry.getValue());
            }
        }

        query.setParameters(copy);
    }

    void parseCtrl(SearchQuery query) {
        String ctrl = getParameter("ctrl");
        if(Strings.isNullOrEmpty(ctrl)) {
            return;
        }
        query.setCtrl(Splitter.splitToSet(getParameter("ctrl")));
    }

    void parseMeta(SearchQuery query) {
        query.setRequestId(getParameter("requestId", UUID.randomUUID().toString()));
        query.setDebug(getParameterAsBoolean("debug", query.isDebug()));
        query.setTracking(getParameterAsBoolean("tracking", Boolean.TRUE));
    }

    void parseQuery(SearchQuery query) {
        query.setQ(getParameter("q"));
    }

    void parsePaging(SearchQuery query) {
        Integer page = getParameterAsInt("page", query.getPage());
        if(page != null && page < 1) {
            page = 1;
        }
        Integer rows = getParameterAsInt("rows", query.getRows());
        if(rows != null && rows < 0) {
            rows = 0;
        }
        query.setPage(page);
        query.setRows(rows);
    }

    void parseSort(SearchQuery query) {
        String sortValue = getParameter("sort");
        if(Strings.isNullOrEmpty(sortValue)) {
            return;
        }
        Sort sort = new Sort();
        sort.setSort(sortValue);
        query.setSort(sort);

    }


    SearchFilter createSearchFilter(String filterName, String[] filterValues)  {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setId(filterName);
        searchFilter.setName(filterName);
        searchFilter.setValues(Arrays.asList(filterValues));
        return searchFilter;
    }


    List<SearchFilter> createTreeFilter(String filterName, String[] filterValues) {

        List<SearchFilter> searchFilterList = new ArrayList<>();

        if(filterValues.length != 1) {
            // do some userfule logging
            return searchFilterList;
        }

        int counter = 0;
        for(String value : filterValues[0].split(Pattern.quote(" > "))) {
            SearchFilter searchFilter = new SearchFilter();
            searchFilter.setFilterType(FilterType.TERM);
            String treeFilterName = filterName + counter;
            searchFilter.setName(treeFilterName);
            searchFilter.setId(treeFilterName);
            searchFilter.setValues(Arrays.asList(value));
            counter++;
            searchFilterList.add(searchFilter);
        }


        return searchFilterList;
    }

    SearchFilter createRangeFilter(String filterName, String[] filterValues) {
        //SearchFilter<RangeFilterValue<Number>> searchFilter = new SearchFilter<>();
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setFilterType(FilterType.RANGE);
        searchFilter.setFilterOperator(FilterOperator.AND);

        RangeFilterValue<Number> rangeFilterValue = new RangeFilterValue<>();

        for(String value : filterValues) {
            String[] valueSplitted = value.split(Pattern.quote(","));
            if(valueSplitted.length != 2) {
                throw new IllegalArgumentException("The value of a range searchFilter must be in the format parameter=v1-v2");
            }
            String min = valueSplitted[0];
            String max = valueSplitted[1];
            try {
                if(min.startsWith(UpperLowerBound.LOWER_EXCLUDED.getCode())) {
                    rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_EXCLUDED);
                    min = min.substring(1);
                } else if (min.startsWith(UpperLowerBound.LOWER_INCLUDED.getCode())) {
                    rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_INCLUDED);
                    min = min.substring(1);
                }

                if(max.endsWith(UpperLowerBound.UPPER_EXCLUDED.getCode())) {
                    rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_EXCLUDED);
                    max = max.substring(0, max.length()-1);
                } else if (max.endsWith(UpperLowerBound.UPPER_INCLUDED.getCode())) {
                    rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_INCLUDED);
                    max = max.substring(0, max.length()-1);
                }

                if (min.equals("min")) {
                    rangeFilterValue.setMinValue(Double.MIN_VALUE);
                } else {
                    rangeFilterValue.setMinValue(Double.valueOf(min));
                }

                if (max.equals("max")) {
                    rangeFilterValue.setMaxValue(Double.MAX_VALUE);
                } else {
                    rangeFilterValue.setMaxValue(Double.valueOf(max));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The min value " + min + " or max value " + max + " is no number value.");
            }
        }

        searchFilter.setName(filterName);
        searchFilter.setId(filterName);
        searchFilter.setRangeValue(rangeFilterValue);
        return searchFilter;
    }


    SearchFilter createHumanDateFilter(String filterName, Instant start, Instant end) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setFilterType(FilterType.RANGE);
        searchFilter.setFilterDataType(FilterDataType.DATE);
        searchFilter.setFilterOperator(FilterOperator.AND);

        RangeFilterValue<Date> rangeFilterValue = new RangeFilterValue<>();
        rangeFilterValue.setMinValue(Date.from(start));
        rangeFilterValue.setMaxValue(Date.from(end));
        rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_INCLUDED);
        rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_EXCLUDED);

        searchFilter.setName(filterName);
        searchFilter.setId(filterName);
        searchFilter.setRangeValue(rangeFilterValue);
        return searchFilter;
    }


    SearchFilter createDateRangeFilter(String filterName, String[] filterValues) {
        //SearchFilter<RangeFilterValue<Number>> searchFilter = new SearchFilter<>();
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setFilterType(FilterType.RANGE);
        searchFilter.setFilterDataType(FilterDataType.DATE);
        searchFilter.setFilterOperator(FilterOperator.AND);

        RangeFilterValue<Date> rangeFilterValue = createRangeFilterValue(filterValues);

        searchFilter.setName(filterName);
        searchFilter.setId(filterName);
        searchFilter.setRangeValue(rangeFilterValue);
        return searchFilter;
    }

    RangeFilterValue<Date> createRangeFilterValue(String[] filterValues) {
        RangeFilterValue<Date> rangeFilterValue = new RangeFilterValue<>();
        for(String value : filterValues) {
            String[] valueSplitted = value.split(Pattern.quote(","));
            if(valueSplitted.length != 2) {
                throw new IllegalArgumentException("The value of a range searchFilter must be in the format parameter=v1-v2");
            }
            String min = valueSplitted[0];
            String max = valueSplitted[1];
            try {
                if(min.startsWith(UpperLowerBound.LOWER_EXCLUDED.getCode())) {
                    rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_EXCLUDED);
                    min = min.substring(1);
                } else if (min.startsWith(UpperLowerBound.LOWER_INCLUDED.getCode())) {
                    rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_INCLUDED);
                    min = min.substring(1);
                }

                if(max.endsWith(UpperLowerBound.UPPER_EXCLUDED.getCode())) {
                    rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_EXCLUDED);
                    max = max.substring(0, max.length()-1);
                } else if (max.endsWith(UpperLowerBound.UPPER_INCLUDED.getCode())) {
                    rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_INCLUDED);
                    max = max.substring(0, max.length()-1);
                }

                if("NOW".equals(min)) {
                    // TODO consider Time zones
                    rangeFilterValue.setMinValue(Date.from(QsfInstant.now()));
                } else if("*".equals(min)) {
                    rangeFilterValue.setMinValue(DateUtil.min());
                } else {
                    rangeFilterValue.setMinValue(DateUtil.getDate(min));
                }

                if("NOW".equals(max)) {
                    // TODO consider Time zones
                    rangeFilterValue.setMaxValue(Date.from(QsfInstant.now()));
                } else if("*".equals(max)) {
                    rangeFilterValue.setMaxValue(DateUtil.max());
                } else {
                    rangeFilterValue.setMaxValue(DateUtil.getDate(max));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("The min value " + min + " or max value " + max + " is no date value. " + e.getMessage(), e);
            }
        }
        return rangeFilterValue;

    }

    void parseFacetFilter(SearchQuery query) {
        for(String name: getParameterNames()) {
            Matcher m = facetFilterPattern.matcher(name);
            if(m.matches()) {
                String filterName = m.group(1);
                String[] filterValues = parameters.get(name);
                String filterType = m.group(2);

                Facet facet = new Facet();
                facet.setId(filterName);
                facet.setName(filterName);
                SearchFilter searchFilter = SearchFilterBuilder.create().withId(filterName).values(Arrays.asList(filterValues)).build();
                facet.getFacetFilters().add(searchFilter);
                if(query.getFacetList() == null) {
                    query.setFacetList(new ArrayList<>());
                }
                query.getFacetList().add(facet);

            }
        }

    }

    void parseFilter(SearchQuery query) {
        for(String name: getParameterNames()) {
            Matcher m = filterPattern.matcher(name);
            if(m.matches()) {
                String filterName = m.group(1);
                String[] filterValues = parameters.get(name);
                String filterType = m.group(2);
                if(Strings.isNullOrEmpty(filterType) || ".and".equals(filterType)) {
                    SearchFilter searchFilter = createSearchFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.TERM);
                    searchFilter.setFilterOperator(FilterOperator.AND);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".or".equals(filterType)) {
                    SearchFilter searchFilter = createSearchFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.TERM);
                    searchFilter.setFilterOperator(FilterOperator.OR);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".not".equals(filterType)) {
                        SearchFilter searchFilter = createSearchFilter(filterName, filterValues);
                        searchFilter.setFilterType(FilterType.TERM);
                        searchFilter.setFilterOperator(FilterOperator.NOT);
                        query.getSearchFilterList().add(searchFilter);
                } else if (".slider".equals(filterType)) {
                    SearchFilter searchFilter = createRangeFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.SLIDER);
                    searchFilter.setFilterDataType(FilterDataType.NUMBER);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".range".equals(filterType)) {
                    SearchFilter searchFilter = createRangeFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.RANGE);
                    searchFilter.setFilterDataType(FilterDataType.NUMBER);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".daterange".equals(filterType)) {
                    SearchFilter searchFilter = createDateRangeFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.RANGE);
                    searchFilter.setFilterDataType(FilterDataType.DATE);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".humandate".equals(filterType) && filterValues.length == 1) {
                    HumanDateParser humanDateParser = new HumanDateParser(filterValues[0]);
                    SearchFilter searchFilter = createHumanDateFilter(filterName, humanDateParser.getStart(), humanDateParser.getEnd());
                    query.getSearchFilterList().add(searchFilter);
                } else if (".tree".equals(filterType)) {
                    List<SearchFilter> searchFilters = createTreeFilter(filterName, filterValues);
                    query.getSearchFilterList().addAll(searchFilters);
                }

            }
        }

    }


    public SearchQuery getQuery() {
        if(this.query == null) {
            this.query = parse();
        }
        return this.query;
    }

    String getParameter(String name) {
        String[] values = this.parameters.get(name);

        if(values == null || values.length == 0) {
            return null;
        }

        if(values.length != 1) {
            throw new IllegalArgumentException("The parameter " + name + " must have exactly one value.");
        }
        return values[0];
    }

    String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    Long getParameterAsLong(String name) {
        String value = getParameter(name);
        if(value == null) {
            return null;
        }
        try {
            Long longValue = Long.parseLong(value);
            return longValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    Integer getParameterAsInt(String name) {
        String value = getParameter(name);
        if(value == null) {
            return null;
        }
        try {
            Integer intValue = Integer.parseInt(value);
            return intValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    Long getParameterAsLong(String name, Long defaultValue) {
        Long value = getParameterAsLong(name);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    Integer getParameterAsInt(String name, Integer defaultValue) {
        Integer value = getParameterAsInt(name);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    Boolean getParameterAsBoolean(String name) {
        String value = getParameter(name);
        if("true".equals(value)) {
            return Boolean.TRUE;
        }
        if("false".equals(value)) {
            return Boolean.FALSE;
        }
        return null;
    }

    Boolean getParameterAsBoolean(String name, Boolean defaultValue) {
        Boolean value = getParameterAsBoolean(name);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    Set<String> getParameterNames() {
        return this.parameters.keySet();
    }


}
