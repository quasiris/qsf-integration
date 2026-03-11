package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.dto.error.SearchQueryException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.exception.PipelinePassThroughException;
import com.quasiris.qsf.query.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryValidationFilterTest {

    private SearchQueryValidationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SearchQueryValidationFilter();
        filter.setId("test-validation");
    }

    private PipelineContainer createContainer(SearchQuery searchQuery) {
        PipelineContainer container = new PipelineContainer();
        container.setSearchQuery(searchQuery);
        return container;
    }

    @Test
    void validQuery_passes() throws Exception {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        query.setPage(0);
        query.setRows(10);
        query.addFilter(SearchFilter.createTermFilter("color", "black"));

        PipelineContainer result = filter.filter(createContainer(query));
        assertNotNull(result);
    }

    @Test
    void nullSearchQuery_throwsError() {
        PipelineContainer container = new PipelineContainer();
        container.setSearchQuery(null);
        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(container));
        assertInstanceOf(SearchQueryException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("must not be null"));
    }

    @Test
    void negativePage_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        query.setPage(-1);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("page must be >= 0"));
    }

    @Test
    void negativeRows_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        query.setRows(-5);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("rows must be >= 0"));
    }

    @Test
    void filterWithoutId_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setFilterType(FilterType.TERM);
        sf.addValue("black");
        query.addFilter(sf);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("missing an id"));
    }

    @Test
    void filterWithoutType_passes() throws Exception {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setId("color");
        sf.addValue("black");
        query.addFilter(sf);

        PipelineContainer result = filter.filter(createContainer(query));
        assertNotNull(result);
    }

    @Test
    void termFilterWithoutValues_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setId("color");
        sf.setFilterType(FilterType.TERM);
        query.addFilter(sf);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("must have at least one value"));
    }

    @Test
    void rangeFilterWithoutRangeValue_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setId("price");
        sf.setFilterType(FilterType.RANGE);
        sf.setFilterDataType(FilterDataType.NUMBER);
        query.addFilter(sf);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("must have a range value"));
    }

    @Test
    void rangeFilterWithNonNumericMin_throwsError() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setId("price");
        sf.setFilterType(FilterType.RANGE);
        sf.setFilterDataType(FilterDataType.NUMBER);
        sf.setRangeValue(new RangeFilterValue<>("abc", "100"));
        query.addFilter(sf);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        assertTrue(ex.getCause().getMessage().contains("non-numeric min value"));
    }

    @Test
    void rangeFilterWithValidNumericRange_passes() throws Exception {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        SearchFilter sf = new SearchFilter();
        sf.setId("price");
        sf.setFilterType(FilterType.RANGE);
        sf.setFilterDataType(FilterDataType.NUMBER);
        sf.setRangeValue(new RangeFilterValue<>(10.0, 100.0));
        query.addFilter(sf);

        PipelineContainer result = filter.filter(createContainer(query));
        assertNotNull(result);
    }

    @Test
    void multipleErrors_allReported() {
        SearchQuery query = new SearchQuery();
        query.setQ("shoes");
        query.setPage(-1);
        query.setRows(-5);

        SearchFilter sf = new SearchFilter();
        sf.setId("color");
        sf.setFilterType(FilterType.TERM);
        query.addFilter(sf);

        PipelinePassThroughException ex = assertThrows(PipelinePassThroughException.class,
                () -> filter.filter(createContainer(query)));
        String message = ex.getCause().getMessage();
        assertTrue(message.contains("page must be >= 0"));
        assertTrue(message.contains("rows must be >= 0"));
        assertTrue(message.contains("must have at least one value"));
    }

    @Test
    void emptyQuery_passes() throws Exception {
        SearchQuery query = new SearchQuery();
        PipelineContainer result = filter.filter(createContainer(query));
        assertNotNull(result);
    }
}
