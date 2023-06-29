package com.quasiris.qsf.pipeline.filter.qsql.parser;

import com.google.common.base.Strings;
import com.quasiris.qsf.commons.text.date.HumanDateParser;
import com.quasiris.qsf.commons.util.DateUtil;
import com.quasiris.qsf.commons.util.QsfInstant;
import com.quasiris.qsf.dto.query.ResultDTO;
import com.quasiris.qsf.dto.query.SpellcheckDTO;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.text.Splitter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParser {

    private static final Pattern filterPattern = Pattern.compile("f\\.([^\\.]+)(.*)");
    private static final Pattern facetFilterPattern = Pattern.compile("ff\\.([^\\.]+)(.*)");

    private Map<String, String[]> parameters;

    private HttpServletRequest httpServletRequest;


    private SearchQuery query = null;

    public QsfqlParser(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        this.parameters = httpServletRequest.getParameterMap();
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
        parseTracking(query);
        return query;
    }

    void parseParameter(SearchQuery query) {
        if(httpServletRequest == null) {
            return;
        }

        Map<String, Object> params = getRequestParameter(httpServletRequest);
        query.setParameters(params);
    }

    void parseCtrl(SearchQuery query) {
        Set<String> ctrl = parseCtrlFromString(getParameter("ctrl"));
        if(ctrl == null || ctrl.size() == 0) {
            return;
        }
        query.setCtrl(ctrl);
        if(Control.isSpellcheckDisabled(query)) {
            ResultDTO resultDTO = query.getResult();
            if(resultDTO == null) {
                resultDTO = new ResultDTO();
                query.setResult(resultDTO);
            }
            SpellcheckDTO spellcheckDTO = resultDTO.getSpellcheck();
            if(spellcheckDTO == null) {
                spellcheckDTO = new SpellcheckDTO();
                resultDTO.setSpellcheck(spellcheckDTO);
            }
            spellcheckDTO.setEnabled(Boolean.FALSE);
        }
    }

    public static Set<String> parseCtrlFromString(String ctrl) {
        if(ctrl == null) {
            return null;
        }
        return Splitter.splitToSet(ctrl);
    }

    void parseMeta(SearchQuery query) {
        query.setRequestId(getParameter("requestId", UUID.randomUUID().toString()));
        query.setDebug(getParameterAsBoolean("debug", query.isDebug()));
        query.setExplain(getParameterAsBoolean("explain", query.isExplain()));
        query.setTracking(getParameterAsBoolean("tracking", Boolean.TRUE));
        query.setRequestOrigin(getParameter("requestOrigin"));
    }

    void parseTracking(SearchQuery query) {
        List<String> tags = new ArrayList<>();
        String[] trackingGET = parameters.get("tracking");
        if(trackingGET != null) {
            for(String tracking : trackingGET) {
                tags.addAll(Arrays.asList(tracking.split(Pattern.quote(","))));
            }

        }
        query.setTrackingTags(tags);
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

        RangeFilterValue<String> rangeFilterValue = new RangeFilterValue<>();
        rangeFilterValue.setMinValue(start.toString());
        rangeFilterValue.setMaxValue(end.toString());
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

        RangeFilterValue<String> rangeFilterValue = createRangeFilterValue(filterValues);

        searchFilter.setName(filterName);
        searchFilter.setId(filterName);
        searchFilter.setRangeValue(rangeFilterValue);
        return searchFilter;
    }

    RangeFilterValue<String> createRangeFilterValue(String[] filterValues) {
        RangeFilterValue<String> rangeFilterValue = new RangeFilterValue<>();
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
                    rangeFilterValue.setMinValue(QsfInstant.now().toString());
                } else if("*".equals(min)) {
                    rangeFilterValue.setMinValue(DateUtil.min().toInstant().toString());
                } else {
                    rangeFilterValue.setMinValue(min);
                }

                if("NOW".equals(max)) {
                    // TODO consider Time zones
                    rangeFilterValue.setMaxValue(QsfInstant.now().toString());
                } else if("*".equals(max)) {
                    rangeFilterValue.setMaxValue(DateUtil.max().toInstant().toString());
                } else {
                    rangeFilterValue.setMaxValue(max);
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
                if(Strings.isNullOrEmpty(filterType)) {
                    SearchFilter searchFilter = createSearchFilter(filterName, filterValues);
                    searchFilter.setFilterType(FilterType.TERM);
                    query.getSearchFilterList().add(searchFilter);
                } else if (".and".equals(filterType)) {
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

    public  static Map<String, Object> getRequestParameter(HttpServletRequest request) {
        Map<String, Object> replaceMap = new HashMap<>();
        if (request == null) {
            return replaceMap;
        }
        synchronized (request) {
            Enumeration<String> parameterName = request.getParameterNames();
            while (parameterName.hasMoreElements()) {
                String name = parameterName.nextElement();
                replaceMap.put("query." + name, request.getParameter(name));
            }

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                replaceMap.put("header." + name, value);
            }


            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    replaceMap.put("cookie." + name, value);
                }
            }

            String path = request.getRequestURI();
            int pathCounter = 0;
            for (String pathPart : path.split(Pattern.quote("/"))) {
                if (!Strings.isNullOrEmpty(pathPart)) {
                    replaceMap.put("path" + pathCounter++ + ".", pathPart);
                }
            }
            return replaceMap;
        }
    }



}
