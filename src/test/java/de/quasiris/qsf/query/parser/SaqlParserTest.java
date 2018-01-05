package de.quasiris.qsf.query.parser;

import de.quasiris.qsf.query.SearchFilter;
import de.quasiris.qsf.query.SearchQuery;
import de.quasiris.qsf.query.RangeFilterValue;
import org.junit.*;

import java.util.*;

/**
 * Created by mki on 13.11.16.
 */
public class SaqlParserTest {


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
        Assert.assertEquals(5, query.getPage());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParameterPageInvalidValue() {
        createQuery("page=foo");
    }

    @Test
    public void testParameterRows() {
        SearchQuery query = createQuery("rows=50");
        Assert.assertEquals(50, query.getRows());
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
        Assert.assertEquals(1, query.getPage());
        Assert.assertEquals(20, query.getRows());
    }


    @Test(expected=IllegalArgumentException.class)
    public void testValidationForMultipleParameters() {
        createQuery("q=foo","q=bar");
    }

    @Test
    public void testFilter() {
        SearchQuery query = createQuery("f.foo=bar");
        SearchFilter<String> searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals(searchFilter.getName(), "foo");
        Assert.assertEquals(searchFilter.getValues().get(0), "bar");
    }

    @Test
    public void testFilterWithMultipleValues() {
        SearchQuery query = createQuery("f.foo=bar", "f.foo=alice");
        SearchFilter<String> searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals("bar", searchFilter.getValues().get(0));
        Assert.assertEquals("alice", searchFilter.getValues().get(1));
    }

    @Test
    public void testRangeFilterForDoubleValues() {
        SearchQuery query = createQuery("f.foo.range=0.1,5.2");
        SearchFilter<RangeFilterValue<Double>> searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(0.1), searchFilter.getValues().get(0).getMinValue());
        Assert.assertEquals(Double.valueOf(5.2), searchFilter.getValues().get(0).getMaxValue());
    }

    @Test
    public void testRangeFilterForLongValues() {
        SearchQuery query = createQuery("f.foo.range=3,5");
        SearchFilter<RangeFilterValue<Double>> searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(3.0), searchFilter.getValues().get(0).getMinValue());
        Assert.assertEquals(Double.valueOf(5.0), searchFilter.getValues().get(0).getMaxValue());
    }

    @Test
    public void testRangeFilterForMinMaxValues() {
        SearchQuery query = createQuery("f.foo.range=min,max");
        SearchFilter<RangeFilterValue<Double>> searchFilter = query.getSearchFilterList().get(0);
        Assert.assertEquals("foo", searchFilter.getName());
        Assert.assertEquals(Double.valueOf(Double.MIN_VALUE), searchFilter.getValues().get(0).getMinValue());
        Assert.assertEquals(Double.valueOf(Double.MAX_VALUE), searchFilter.getValues().get(0).getMaxValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRangeFilterWithWrongDelimiter() {
        createQuery("f.foo.range=0.1-5.2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRangeFilterWithWrongValue() {
        createQuery("f.foo.range=0.1,bar");
    }

    SearchQuery createQuery(String... parameters) {
        Map<String,String[]> parameter = new HashMap<String,String[]>();
        for(String param: parameters) {
            String[] paramSplitted = param.split("=");
            addParameter(parameter,paramSplitted[0],paramSplitted[1]);
        }
        SaqlParser saqlParser = new SaqlParser(parameter);
        SearchQuery query = saqlParser.getQuery();
        return query;
    }

    void addParameter(Map<String,String[]> parameter, String name, String value) {
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
