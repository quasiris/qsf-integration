package com.quasiris.qsf.query.parser;

import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParserTest {

    @Test
    public void testSort() {
        SearchQuery query = createQuery("sort=name");
        Assert.assertEquals("name", query.getSort().getSort());
    }

    @Test
    public void testParameterQ() {
        SearchQuery query = createQuery("q=foo");
        Assert.assertEquals("foo", query.getQ());
    }

    @Test
    public void testParameterRequestId() {
        SearchQuery query = createQuery("requestId=0815");
        Assert.assertEquals("0815", query.getRequestId());
    }

    @Test
    public void testParameterPage() {
        SearchQuery query = createQuery("page=5");
        Assert.assertEquals(Integer.valueOf(5), query.getPage());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParameterPageInvalidValue() {
        createQuery("page=foo");
    }

    @Test
    public void testParameterRows() {
        SearchQuery query = createQuery("rows=50");
        Assert.assertEquals(Integer.valueOf(50), query.getRows());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParameterRowsInvalidValue() {
        createQuery("rows=foo");
    }

    @Test
    public void testEmty() {
        SearchQuery query = createQuery();
        Assert.assertNull(query.getQ());
        Assert.assertNotNull(query.getRequestId());
        Assert.assertNull(query.getPage());
        Assert.assertNull(query.getRows());
    }


    @Test(expected=IllegalArgumentException.class)
    public void testValidationForMultipleParameters() {
        createQuery("q=foo","q=bar");
    }

    @Test
    public void testFilter() {
        SearchQuery query = createQuery("f.foo=bar");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals(searchFilter.getName(), "foo");
        Assert.assertEquals(searchFilter.getValues().get(0), "bar");
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.AND);
    }


    @Test
    public void testOrFilter() {
        SearchQuery query = createQuery("f.foo.or=alice", "f.foo.or=bob");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals(searchFilter.getName(), "foo");
        Assert.assertEquals(searchFilter.getValues().get(0), "alice");
        Assert.assertEquals(searchFilter.getValues().get(1), "bob");
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.OR);
    }

    @Test
    public void testAndFilter() {
        SearchQuery query = createQuery("f.foo.and=alice", "f.foo.and=bob");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals(searchFilter.getName(), "foo");
        Assert.assertEquals(searchFilter.getValues().get(0), "alice");
        Assert.assertEquals(searchFilter.getValues().get(1), "bob");
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.AND);
    }

    @Test
    public void testFilterWithMultipleValues() {
        SearchQuery query = createQuery("f.foo=bar", "f.foo=alice");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals("bar", searchFilter.getValues().get(0));
        Assert.assertEquals("alice", searchFilter.getValues().get(1));
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.AND);
    }

    @Test
    public void testRangeFilterForDoubleValues() {
        SearchQuery query = createQuery("f.foo.range=0.1,5.2");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(0.1), searchFilter.getRangeValue(Double.class).getMinValue());
        Assert.assertEquals(Double.valueOf(5.2), searchFilter.getRangeValue(Double.class).getMaxValue());
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
    }

    @Test
    public void testRangeFilterForLongValues() {
        SearchQuery query = createQuery("f.foo.range=3,5");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        Assert.assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
    }

    @Test
    public void testSliderFilterForLongValues() {
        SearchQuery query = createQuery("f.foo.slider=3,5");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(3.0), searchFilter.getRangeValue(Double.class).getMinValue());
        Assert.assertEquals(Double.valueOf(5.0), searchFilter.getRangeValue(Double.class).getMaxValue());
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.SLIDER);
    }

    @Test
    public void testRangeFilterForMinMaxValues() {
        SearchQuery query = createQuery("f.foo.range=min,max");
        SearchFilter searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(Double.MIN_VALUE), searchFilter.getRangeValue(Double.class).getMinValue());
        Assert.assertEquals(Double.valueOf(Double.MAX_VALUE), searchFilter.getRangeValue(Double.class).getMaxValue());
        Assert.assertEquals(searchFilter.getFilterType(), FilterType.RANGE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRangeFilterWithWrongDelimiter() {
        createQuery("f.foo.range=0.1-5.2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRangeFilterWithWrongValue() {
        createQuery("f.foo.range=0.1,bar");
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
