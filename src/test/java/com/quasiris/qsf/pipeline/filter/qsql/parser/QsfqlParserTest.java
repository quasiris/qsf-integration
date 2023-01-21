package com.quasiris.qsf.pipeline.filter.qsql.parser;

import com.quasiris.qsf.commons.util.DateUtil;
import com.quasiris.qsf.commons.util.QsfInstant;
import com.quasiris.qsf.query.Control;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.UpperLowerBound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTestUtil.createQuery;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParserTest {

    @Test
    public void testTracking() throws Exception {
        SearchQuery query = createQuery("tracking=testing,monitoring");
        assertEquals(2, query.getTrackingTags().size());
        assertEquals("testing", query.getTrackingTags().get(0));
        assertEquals("monitoring", query.getTrackingTags().get(1));
    }
    @Test
    public void testControl() throws Exception {
        SearchQuery query = createQuery("ctrl=loadMoreFacets,modified");
        assertTrue(Control.isLoadMoreFacets(query));
        assertTrue(Control.isModified(query));
    }
    @Test
    public void testCtrl() throws Exception {
        SearchQuery query = createQuery("ctrl=loadMoreFacets,modified");
        assertTrue(query.isCtrl("loadMoreFacets"));
        assertTrue(query.isCtrl("modified"));
        assertFalse(query.isCtrl("foo"));
    }

    @Test
    public void testSort() throws Exception {
        SearchQuery query = createQuery("sort=name");
        assertEquals("name", query.getSort().getSort());
    }

    @Test
    public void testParameterQ() throws Exception {
        SearchQuery query = createQuery("q=foo");
        assertEquals("foo", query.getQ());
    }

    @Test
    public void testParameterRequestId() throws Exception {
        SearchQuery query = createQuery("requestId=0815");
        assertEquals("0815", query.getRequestId());
    }

    @Test
    public void testParameterPage() throws Exception {
        SearchQuery query = createQuery("page=5");
        assertEquals(Integer.valueOf(5), query.getPage());
    }

    @Test
    public void testParameterPageInvalidValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("page=foo");
        });
    }

    @Test
    public void testParameterRows() throws Exception {
        SearchQuery query = createQuery("rows=50");
        assertEquals(Integer.valueOf(50), query.getRows());
    }

    @Test
    public void testParameterRowsInvalidValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("rows=foo");
        });
    }

    @Test
    public void testEmty() throws Exception {
        SearchQuery query = createQuery();
        assertNull(query.getQ());
        assertNotNull(query.getRequestId());
        assertEquals(1, query.getPage());
        assertNull(query.getRows());
    }


    @Test
    public void testValidationForMultipleParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("q=foo","q=bar");
        });

    }

    @Test
    public void testFilter() throws Exception {
        SearchQuery query = createQuery("f.foo=bar");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "bar");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.OR);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }


    @Test
    public void testOrFilter() throws Exception {
        SearchQuery query = createQuery("f.foo.or=alice", "f.foo.or=bob");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "alice");
        assertEquals(searchFilter.getValues().get(1), "bob");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.OR);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testAndFilter() throws Exception {
        SearchQuery query = createQuery("f.foo.and=alice", "f.foo.and=bob");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "alice");
        assertEquals(searchFilter.getValues().get(1), "bob");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.AND);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testFilterWithMultipleValues() throws Exception {
        SearchQuery query = createQuery("f.foo=bar", "f.foo=alice");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals("bar", searchFilter.getValues().get(0));
        assertEquals("alice", searchFilter.getValues().get(1));
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.OR);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testRangeFilterForDoubleValues() throws Exception {
        SearchQuery query = createQuery("f.foo.range=0.1,5.2");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(0.1), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.2), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterForDateValues() throws Exception {
        SearchQuery query = createQuery("f.timestamp.daterange=2021-01-02T23:00:00Z,2021-02-05T20:59:38Z");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("timestamp", searchFilter.getName());
        assertNotNull(searchFilter.getRangeValue(Date.class).getMinValue());
        assertNotNull(searchFilter.getRangeValue(Date.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.DATE);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterForDateValuesNOWAndInfinity() throws Exception {
        Instant reference = Instant.parse("2021-08-26T10:58:09.000Z");
        QsfInstant.setNow(reference);
        SearchQuery query = createQuery("f.timestamp.daterange=NOW,*");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("timestamp", searchFilter.getName());
        assertEquals(Date.from(reference), searchFilter.getRangeValue(Date.class).getMinValue());
        assertTrue(searchFilter.getRangeValue(Date.class).getMaxValue().equals(DateUtil.max()));
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.DATE);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterForLongValues() throws Exception {
        SearchQuery query = createQuery("f.foo.range=3,5");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterLowerUpperBoundExcluded() throws Exception {
        SearchQuery query = createQuery("f.foo.range={3,5}");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_EXCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_EXCLUDED);
    }

    @Test
    public void testRangeFilterLowerUpperBoundIncluded() throws Exception {
        SearchQuery query = createQuery("f.foo.range=[3,5]");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testSliderFilterForLongValues() throws Exception {
        SearchQuery query = createQuery("f.foo.slider=3,5");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.SLIDER);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
    }

    @Test
    public void testRangeFilterForMinMaxValues() throws Exception {
        SearchQuery query = createQuery("f.foo.range=min,max");
        SearchFilter searchFilter = (SearchFilter) query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(Double.MIN_VALUE), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(Double.MAX_VALUE), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
    }

    @Test
    public void testRangeFilterWithWrongDelimiter() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("f.foo.range=0.1-5.2");
        });

    }

    @Test
    public void testRangeFilterWithWrongValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("f.foo.range=0.1,bar");
        });

    }


    @Test
    public void tesFacetFilter() throws Exception {
        SearchQuery query = createQuery("ff.accountId=1234");
        Facet facet = query.getFacetList().get(0);
        assertEquals(facet.getName(), "accountId");
        assertEquals(facet.getId(), "accountId");
        //assertEquals(facet.getFacetFilters().get(0).getName(), "accountId");
        assertEquals(((SearchFilter)facet.getFacetFilters().get(0)).getId(), "accountId");
        assertEquals(((SearchFilter)facet.getFacetFilters().get(0)).getValues().get(0), "1234");
    }

}
