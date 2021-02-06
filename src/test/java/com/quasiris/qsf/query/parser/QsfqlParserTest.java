package com.quasiris.qsf.query.parser;

import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.UpperLowerBound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParserTest {

    @Test
    public void testSort() {
        SearchQuery query = createQuery("sort=name");
        assertEquals("name", query.getSort().getSort());
    }

    @Test
    public void testParameterQ() {
        SearchQuery query = createQuery("q=foo");
        assertEquals("foo", query.getQ());
    }

    @Test
    public void testParameterRequestId() {
        SearchQuery query = createQuery("requestId=0815");
        assertEquals("0815", query.getRequestId());
    }

    @Test
    public void testParameterPage() {
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
    public void testParameterRows() {
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
    public void testEmty() {
        SearchQuery query = createQuery();
        assertNull(query.getQ());
        assertNotNull(query.getRequestId());
        assertNull(query.getPage());
        assertNull(query.getRows());
    }


    @Test
    public void testValidationForMultipleParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createQuery("q=foo","q=bar");
        });

    }

    @Test
    public void testFilter() {
        SearchQuery query = createQuery("f.foo=bar");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "bar");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.AND);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }


    @Test
    public void testOrFilter() {
        SearchQuery query = createQuery("f.foo.or=alice", "f.foo.or=bob");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "alice");
        assertEquals(searchFilter.getValues().get(1), "bob");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.OR);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testAndFilter() {
        SearchQuery query = createQuery("f.foo.and=alice", "f.foo.and=bob");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals(searchFilter.getName(), "foo");
        assertEquals(searchFilter.getValues().get(0), "alice");
        assertEquals(searchFilter.getValues().get(1), "bob");
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.AND);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testFilterWithMultipleValues() {
        SearchQuery query = createQuery("f.foo=bar", "f.foo=alice");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals("bar", searchFilter.getValues().get(0));
        assertEquals("alice", searchFilter.getValues().get(1));
        assertEquals(searchFilter.getFilterType(), FilterType.TERM);
        assertEquals(searchFilter.getFilterOperator(), FilterOperator.AND);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.STRING);
    }

    @Test
    public void testRangeFilterForDoubleValues() {
        SearchQuery query = createQuery("f.foo.range=0.1,5.2");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(0.1), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.2), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterForLongValues() {
        SearchQuery query = createQuery("f.foo.range=3,5");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testRangeFilterLowerUpperBoundExcluded() {
        SearchQuery query = createQuery("f.foo.range={3,5}");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_EXCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_EXCLUDED);
    }

    @Test
    public void testRangeFilterLowerUpperBoundIncluded() {
        SearchQuery query = createQuery("f.foo.range=[3,5]");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
        assertEquals(searchFilter.getRangeValue(Double.class).getLowerBound(), UpperLowerBound.LOWER_INCLUDED);
        assertEquals(searchFilter.getRangeValue(Double.class).getUpperBound(), UpperLowerBound.UPPER_INCLUDED);
    }

    @Test
    public void testSliderFilterForLongValues() {
        SearchQuery query = createQuery("f.foo.slider=3,5");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        assertEquals("foo", searchFilter.getName());
        assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        assertEquals(searchFilter.getFilterType(), FilterType.SLIDER);
        assertEquals(searchFilter.getFilterDataType(), FilterDataType.NUMBER);
    }

    @Test
    public void testRangeFilterForMinMaxValues() {
        SearchQuery query = createQuery("f.foo.range=min,max");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
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

    public static SearchQuery createQuery(String... parameters) {
        Map<String,String[]> parameter = new HashMap<String,String[]>();
        for(String param: parameters) {
            String[] paramSplitted = param.split("=");
            addParameter(parameter,paramSplitted[0],paramSplitted[1]);
        }
        QsfqlParser qsfqlParser = new QsfqlParser(parameter);
        SearchQuery query = qsfqlParser.getQuery();
        return query;
    }

    public static void addParameter(Map<String,String[]> parameter, String name, String value) {
        String[] values = parameter.get(name);
        if(values == null) {
            values = new String[0];
        }

        List<String> valueList = new ArrayList<>(Arrays.asList(values));
        valueList.add(value);
        values = valueList.toArray(new String[valueList.size()]);
        parameter.put(name,values);
    }
}
