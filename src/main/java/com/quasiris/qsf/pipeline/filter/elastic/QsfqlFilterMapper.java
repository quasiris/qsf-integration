package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.util.SerializationUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QsfqlFilterMapper {

    private QsfSearchConfigDTO searchConfig;

    public QsfqlFilterMapper(QsfSearchConfigDTO searchConfig) {
        this.searchConfig = searchConfig;
    }

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html
    // TODO implement range queries for date
    public ArrayNode computeFilter(List<SearchFilter> searchFilterList) throws JsonBuilderException {
        JsonBuilder filters = JsonBuilder.create().array();
        for (SearchFilter searchFilter : searchFilterList) {
            ArrayNode filter = computeFilter(searchFilter);
            if(filter != null) {
                filters.stash();
                filters.addJson(filter);
                filters.unstash();
            }
        }
        return (ArrayNode) filters.get();
    }

    public ArrayNode computeFilter(SearchFilter searchFilter) throws JsonBuilderException {
        String elasticField = mapFilterField(searchFilter.getName());
        return computeFilter(searchFilter, elasticField);
    }

    /**
     * Create elastic json for filter
     * @param searchFilter qsc
     * @param elasticField for mapping
     * @return json or null if filter type unknown
     * @throws JsonBuilderException on any json error
     */
    public @Nullable ArrayNode computeFilter(@Nonnull SearchFilter searchFilter, @Nullable String elasticField) throws JsonBuilderException {
        ArrayNode filter = null;
        switch (searchFilter.getFilterType()) {
            case TERM:
            case MATCH:
            case MATCH_PHRASE:
                filter = transformTermsFilter(searchFilter, elasticField);
                break;
            case RANGE:
                filter = transformRangeFilter(searchFilter, elasticField);
                break;
            case DEFINED_RANGE:
                filter = transformDefinedRangeFilter(searchFilter, elasticField);
                break;
            case SLIDER:
                filter = transformRangeFilter(searchFilter, elasticField);
                break;
            default:
                throw new IllegalArgumentException("The filter type " + searchFilter.getFilterType().getCode() + " is not implemented.");
        }

        return filter;
    }

    protected static ArrayNode transformTermsFilter(SearchFilter searchFilter, String elasticField) throws JsonBuilderException {
        if(searchFilter.getValues() == null | searchFilter.getValues().isEmpty()) {
            return null;
        }
        if(elasticField == null) {
            elasticField = searchFilter.getId();
        }
        if(elasticField == null) {
            throw new IllegalArgumentException("There is no field name defined.");
        }
        JsonBuilder jsonBuilder = JsonBuilder.create().array();

        for(String filterValue : searchFilter.getValues()) {
            jsonBuilder.stash();
            jsonBuilder.object(searchFilter.getFilterType().getCode());
            jsonBuilder.object(elasticField, filterValue);
            jsonBuilder.unstash();
        }
        return (ArrayNode) jsonBuilder.unstash().get();

    }

    public String mapFilterField(String fieldName) {
        if(fieldName == null) {
            return null;
        }
        String elasticField = searchConfig.getFilter().getFilterMapping().get(fieldName);
        if(!Strings.isNullOrEmpty(elasticField)) {
            return elasticField;
        }

        for(Map.Entry<String, String> rule : searchConfig.getFilter().getFilterRules().entrySet()) {
            String pattern = rule.getKey();
            String replacement = rule.getValue();
            elasticField = fieldName.replaceAll(pattern, replacement);
            if(!Strings.isNullOrEmpty(elasticField)) {
                return elasticField;
            }
        }
        return fieldName;

    }

    protected ArrayNode transformDefinedRangeFilter(SearchFilter searchFilter, String elasticField) throws JsonBuilderException {
        JsonBuilder jsonBuilder = JsonBuilder.create().array();

        for(String filterValue : searchFilter.getValues()) {

            Range range = searchConfig.getFilter().getDefinedRangeFilterMapping().get(searchFilter.getId() + "." + filterValue);
            ArrayNode an = transformDefinedRangeFilterForDefinedRange(searchFilter, elasticField, range);
            if(an != null) {
                jsonBuilder.stash();
                jsonBuilder.addJson(an);
                jsonBuilder.unstash();
            }

        }

        return (ArrayNode) jsonBuilder.get();

    }

    protected ArrayNode transformDefinedRangeFilterForDefinedRange(SearchFilter searchFilter, String elasticField, Range definedRange) throws JsonBuilderException {
        if(definedRange == null) {
            return null;
        }
        SearchFilter rangeFilter = SerializationUtils.deepCopy(searchFilter);
        RangeFilterValue rangeFilterValue = new RangeFilterValue(definedRange.getMin(), definedRange.getMax());
        rangeFilterValue.setLowerBound(UpperLowerBound.LOWER_INCLUDED);
        rangeFilterValue.setUpperBound(UpperLowerBound.UPPER_EXCLUDED);
        rangeFilter.setFilterType(FilterType.RANGE);
        rangeFilter.setRangeValue(rangeFilterValue);
        return transformRangeFilter(rangeFilter, elasticField);

    }

    protected static ArrayNode transformRangeFilter(SearchFilter searchFilter, String elasticField) throws JsonBuilderException {
        if(elasticField == null) {
            elasticField = searchFilter.getId();
        }

        JsonBuilder rangeBuilder = JsonBuilder.create().
                array().
                object("range").
                object(elasticField);

        if(searchFilter.getFilterDataType().isNumber()) {
            RangeFilterValue<Double> rangeFilterValue = searchFilter.getRangeValue(Double.class);
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    object(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());
        } else if (searchFilter.getFilterDataType().isString()) {
            RangeFilterValue<String> rangeFilterValue = searchFilter.getRangeValue(String.class);
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    object(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());

        } else if(searchFilter.getFilterDataType().isDate()) {
            RangeFilterValue<String> rangeFilterValue = searchFilter.getRangeValue(String.class);
            String minValue = rangeFilterValue.getMinValue();
            String maxValue = rangeFilterValue.getMaxValue();
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), minValue).
                    object(rangeFilterValue.getUpperBound().getOperator(), maxValue);
        } else {
            throw new IllegalArgumentException("For the data type " + searchFilter.getFilterDataType().getCode() +
                    " no implementation is available.");
        }

        return (ArrayNode) rangeBuilder.root().get();

    }

    public @Nullable ArrayNode getFilterAsJson(@Nonnull List<SearchFilter> searchFilters) throws JsonBuilderException {
        if(searchFilters.size() == 0) {
            return null;
        }
        JsonBuilder jsonBuilder = JsonBuilder.create().array();
        for(SearchFilter searchFilter : searchFilters) {
            ArrayNode filters = computeFilter(Arrays.asList(searchFilter));
            if (filters.size() == 0) {
                // don't add empty filters
                continue;
            } else if (filters.size() == 1 && searchFilter.getFilterOperator() != FilterOperator.NOT) {
                // don't wrap with bool
                jsonBuilder.addJson(filters);
            } else if (filters.size() >= 1) {
                // wrap with bool here
                JsonBuilder operatorBuilder = JsonBuilder.create().
                        object("bool");
                if (searchFilter.getFilterOperator() == FilterOperator.AND || searchFilter.getFilterOperator() == null) {
                    operatorBuilder.array("must");
                } else if (searchFilter.getFilterOperator() == FilterOperator.OR) {
                    operatorBuilder.array("should");
                } else if (searchFilter.getFilterOperator() == FilterOperator.NOT) {
                    operatorBuilder.array("must_not");
                }
                operatorBuilder.addJson(filters);
                jsonBuilder.addJson(operatorBuilder.root().get());
            }
        }

        return (ArrayNode) jsonBuilder.get();
    }

    public <T extends BaseSearchFilter> ObjectNode buildFiltersJson(@Nonnull List<T> searchFilters) throws JsonBuilderException {
        return buildFiltersJson(searchFilters, null);
    }

    public <T extends BaseSearchFilter> ObjectNode buildFiltersJson(@Nonnull List<T> searchFilters, FilterOperator operator) throws JsonBuilderException {
        JsonBuilder builder = JsonBuilder.create();
        builder.object("bool");
        if (operator == FilterOperator.AND || operator == null) {
            builder.array("must");
        } else if (operator == FilterOperator.OR) {
            builder.array("should");
        } else if (operator == FilterOperator.NOT) {
            builder.array("must_not");
        }

        int filtersCreated = 0;
        boolean hasBoolFilter = false;
        if(searchFilters != null && searchFilters.size() > 0) {
            List<SearchFilter> regularSearchFilters = new ArrayList<>();
            for (BaseSearchFilter searchFilter : searchFilters) {
                if (searchFilter instanceof BoolSearchFilter) {
                    FilterOperator filterOperator = ((BoolSearchFilter) searchFilter).getOperator();
                    BoolSearchFilter filter = (BoolSearchFilter) searchFilter;
                    if(filter.getFilters() != null && filter.getFilters().size() > 0) {
                        hasBoolFilter = true;
                        ObjectNode boolNode = buildFiltersJson(filter.getFilters(), filterOperator);
                        builder.addJson(boolNode);
                        filtersCreated++;
                    }
                } else if (searchFilter instanceof SearchFilter) {
                    SearchFilter filter = (SearchFilter) searchFilter;
                    regularSearchFilters.add(filter);
                    filtersCreated++;
                }
            }
            if(regularSearchFilters.size() > 0) {
                // add classic filters
                ArrayNode filters = getFilterAsJson(regularSearchFilters);
                if(filters != null) {
                    if(builder.getCurrent() instanceof ArrayNode) {
                        builder.addJson(filters);
                    } else {
                        builder.json(filters);
                    }
                }
            }
        }

        if(filtersCreated == 0) {
            // add empty bool, to prevent nullptr
            builder = JsonBuilder.create().object("bool");
        }

        return (ObjectNode) builder.get();
    }

}
