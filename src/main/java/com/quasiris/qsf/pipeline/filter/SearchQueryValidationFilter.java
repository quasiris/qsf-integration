package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.dto.error.SearchQueryException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.exception.PipelinePassThroughException;
import com.quasiris.qsf.query.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies base defaults and validates basic search query structure and filter syntax.
 *
 * Defaults (applied before validation):
 * - filter name defaults to filter id
 * - filter type defaults to TERM
 * - filter data type defaults to STRING
 * - filter operator defaults to OR
 *
 * Checks:
 * - page >= 0 (if set)
 * - rows >= 0 (if set)
 * - each filter has an id
 * - if filterType is set, validates syntax per type
 * - term/match/match_phrase filters have at least one value
 * - range/slider/defined_range filters have a range value
 * - numeric range filters have parseable min/max values
 *
 * Throws a 400 error via PipelinePassThroughException on validation failure.
 * Subclasses can override {@link #applyDefaults(SearchQuery)} to add custom defaults
 * and {@link #validate(SearchQuery, List)} to add custom checks.
 */
public class SearchQueryValidationFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        if (searchQuery == null) {
            throw new PipelinePassThroughException(pipelineContainer,
                    new SearchQueryException("The search query must not be null."));
        }

        applyDefaults(searchQuery);

        List<String> errors = new ArrayList<>();
        validate(searchQuery, errors);

        if (!errors.isEmpty()) {
            throw new PipelinePassThroughException(pipelineContainer,
                    new SearchQueryException(String.join(" ", errors)));
        }

        return pipelineContainer;
    }

    protected void applyDefaults(SearchQuery searchQuery) {
        List<SearchFilter> filters = searchQuery.getAllSearchFilters();
        if (filters != null) {
            filters.forEach(this::applyFilterDefaults);
        }
    }

    protected void applyFilterDefaults(SearchFilter filter) {
        if (filter.getName() == null) {
            filter.setName(filter.getId());
        }
        if (filter.getFilterType() == null) {
            filter.setFilterType(FilterType.TERM);
        }
        if (filter.getFilterDataType() == null) {
            filter.setFilterDataType(FilterDataType.STRING);
        }
        if (filter.getFilterOperator() == null) {
            filter.setFilterOperator(FilterOperator.OR);
        }
    }

    protected void validate(SearchQuery searchQuery, List<String> errors) {
        validatePaging(searchQuery, errors);
        validateSearchFilters(searchQuery, errors);
    }

    protected void validatePaging(SearchQuery searchQuery, List<String> errors) {
        if (searchQuery.getPage() != null && searchQuery.getPage() < 0) {
            errors.add("The page must be >= 0.");
        }
        if (searchQuery.getRows() != null && searchQuery.getRows() < 0) {
            errors.add("The rows must be >= 0.");
        }
    }

    protected void validateSearchFilters(SearchQuery searchQuery, List<String> errors) {
        List<SearchFilter> filters = searchQuery.getAllSearchFilters();
        if (filters == null) {
            return;
        }
        for (SearchFilter filter : filters) {
            validateSearchFilter(filter, errors);
        }
    }

    protected void validateSearchFilter(SearchFilter filter, List<String> errors) {
        String filterId = filter.getId() != null ? filter.getId() : "<unknown>";

        if (filter.getId() == null || filter.getId().isEmpty()) {
            errors.add("A search filter is missing an id.");
        }

        if (filter.getFilterType() == null) {
            return;
        }

        switch (filter.getFilterType()) {
            case TERM:
            case MATCH:
            case MATCH_PHRASE:
                if (filter.getValues() == null || filter.getValues().isEmpty()) {
                    errors.add("The filter '" + filterId + "' of type "
                            + filter.getFilterType().getCode() + " must have at least one value.");
                }
                break;
            case RANGE:
            case SLIDER:
            case DEFINED_RANGE:
                if (!filter.hasRangeValue() && (filter.getValues() == null || filter.getValues().isEmpty())) {
                    errors.add("The filter '" + filterId + "' of type "
                            + filter.getFilterType().getCode() + " must have a range value or values.");
                }
                if (filter.hasRangeValue()
                        && filter.getFilterDataType() != null
                        && filter.getFilterDataType().isNumber()) {
                    validateNumericRange(filter, filterId, errors);
                }
                break;
            default:
                break;
        }
    }

    protected void validateNumericRange(SearchFilter filter, String filterId, List<String> errors) {
        Object min = filter.getMinValue();
        Object max = filter.getMaxValue();
        if (min != null && !(min instanceof Number)) {
            try {
                Double.parseDouble(min.toString());
            } catch (NumberFormatException e) {
                errors.add("The filter '" + filterId + "' has a non-numeric min value: " + min);
            }
        }
        if (max != null && !(max instanceof Number)) {
            try {
                Double.parseDouble(max.toString());
            } catch (NumberFormatException e) {
                errors.add("The filter '" + filterId + "' has a non-numeric max value: " + max);
            }
        }
    }
}
