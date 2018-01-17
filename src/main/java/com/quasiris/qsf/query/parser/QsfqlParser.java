package com.quasiris.qsf.query.parser;

import com.google.common.base.Strings;
import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Sort;

import java.util.Arrays;
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
        return query;
    }


    void parseMeta(SearchQuery query) {
        query.setRequestId(getParameter("requestId", UUID.randomUUID().toString()));
        query.setDebug(getParameterAsBoolean("debug", query.isDebug()));
    }

    void parseQuery(SearchQuery query) {
        query.setQ(getParameter("q"));
    }

    void parsePaging(SearchQuery query) {
        query.setPage(getParameterAsInt("page", query.getPage()));
        query.setRows(getParameterAsInt("rows", query.getRows()));
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

    void parseFilter(SearchQuery query) {
        for(String name: getParameterNames()) {
            Matcher m = filterPattern.matcher(name);
            if(m.matches()) {
                String filterName = m.group(1);
                String[] filterValues = parameters.get(name);
                String filterType = m.group(2);
                if(Strings.isNullOrEmpty(filterType)) {
                    SearchFilter searchFilter = new SearchFilter();
                    searchFilter.setName(filterName);
                    searchFilter.setValues(Arrays.asList(filterValues));
                    query.getSearchFilterList().add(searchFilter);
                } else if (".range".equals(filterType)) {
                    //SearchFilter<RangeFilterValue<Number>> searchFilter = new SearchFilter<>();
                    SearchFilter searchFilter = new SearchFilter();
                    RangeFilterValue<Number> rangeFilterValue = new RangeFilterValue<>();

                    for(String value : filterValues) {
                        String[] valueSplitted = value.split(Pattern.quote(","));
                        if(valueSplitted.length != 2) {
                            throw new IllegalArgumentException("The value of a range searchFilter must be in the format parameter=v1-v2");
                        }
                        String min = valueSplitted[0];
                        String max = valueSplitted[1];
                        try {
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
                    searchFilter.setRangeValue(rangeFilterValue);
                    query.getSearchFilterList().add(searchFilter);
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
