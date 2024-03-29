package com.quasiris.qsf.migration;

import com.quasiris.qsf.query.*;
import com.quasiris.qsf.test.service.TestSuiteExecuter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QsfSearchQueryParserTest {
    private final QsfSearchQueryParser parser = new QsfSearchQueryParser();

    @Test
    void parseSearchQueryGetRequestSuccess() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String q = "*";
        String requestId = "1";
        HashMap<String, String[]> parameters = new HashMap<String, String[]>() {{
            put("requestId", new String[]{requestId});
            put("debug", new String[]{"false"});
            put("q", new String[]{q});
            put("page", new String[]{"-1"});
            put("rows", new String[]{"-1"});
            put("f.myHumanDate.humandate", new String[]{"1992"});
            put("f.myTree.tree", new String[]{"node1 > node2"});
            put("f.sku", new String[]{"2101675", "2092164"});
        }};
        when(mockRequest.getParameterMap()).thenReturn(parameters);
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(parameters.keySet()));
        when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(new ArrayList<>()));
        when(mockRequest.getRequestURI()).thenReturn("/deploy-folder/servlet-mapping/request-path");
        when(mockRequest.getMethod()).thenReturn("GET");

        SearchQuery actual = parser.parseSearchQuery(mockRequest);
        assertEquals(q, actual.getQ());
        assertEquals(requestId, actual.getRequestId());
        assertEquals(1, actual.getPage());
        assertEquals(0, actual.getRows());
        assertFalse(actual.isDebug());

        SearchFilter myHumanDate = actual.getSearchFilterById("myHumanDate");


        assertEquals("myHumanDate", myHumanDate.getId());
        // the date values are parsed to min and max
        assertNull(myHumanDate.getValues());
        assertNotNull(myHumanDate.getMinValue());
        assertNotNull(myHumanDate.getMaxValue());

        SearchFilter myTree0 = actual.getSearchFilterById("myTree0");
        assertEquals("myTree0",myTree0.getId());
        assertEquals("node1",myTree0.getValues().get(0));

        SearchFilter myTree1 = actual.getSearchFilterById("myTree1");
        assertEquals("myTree1",myTree1.getId());
        assertEquals("node2",myTree1.getValues().get(0));


        SearchFilter sku = actual.getSearchFilterById("sku");
        assertEquals("2101675", sku.getValues().get(0));
        assertEquals("2092164", sku.getValues().get(1));
        assertEquals(FilterDataType.STRING, sku.getFilterDataType());
        assertEquals(FilterType.TERM, sku.getFilterType());
        assertEquals(FilterOperator.OR, sku.getFilterOperator());
    }

    @Test
    void parseSearchQueryPostRequestWithDefaultsSuccess() throws IOException {
        String fileName = "/com/quasiris/qsf/migration/qsf-search-query-parser-test-post-success.json";
        try (InputStream is = TestSuiteExecuter.class.getResourceAsStream(fileName)) {

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletInputStream servletInputStream = mockServletInputStream(is);
            when(mockRequest.getInputStream()).thenReturn(servletInputStream);
            when(mockRequest.getMethod()).thenReturn("POST");

            SearchQuery actual = parser.parseSearchQuery(mockRequest);

            assertEquals("*", actual.getQ());
            assertEquals(1, actual.getPage());
            assertEquals(1, actual.getRows());
            assertNull(actual.getTrackingTags());
            assertFalse(actual.isDebug());
            
            assertEquals(11 ,actual.getAllSearchFilters().size());

            SearchFilter notExists = actual.getSearchFilterById("not-exists");
            assertNull(notExists);

            SearchFilter myIdFilter = actual.getSearchFilterById("myid");
            assertEquals("myValue", myIdFilter.getValues().get(0));
            assertEquals(FilterDataType.STRING, myIdFilter.getFilterDataType());
            assertEquals(FilterType.TERM, myIdFilter.getFilterType());
            assertEquals(FilterOperator.OR, myIdFilter.getFilterOperator());

            SearchFilter myIdOperatorNot = actual.getSearchFilterById("myid-operator-not");
            assertEquals("myValue", myIdOperatorNot.getValues().get(0));
            assertEquals(FilterDataType.STRING, myIdOperatorNot.getFilterDataType());
            assertEquals(FilterType.TERM, myIdOperatorNot.getFilterType());
            assertEquals(FilterOperator.NOT, myIdOperatorNot.getFilterOperator());

            SearchFilter myIdOperatorOr = actual.getSearchFilterById("myid-operator-or");
            assertEquals("myValue", myIdOperatorOr.getValues().get(0));
            assertEquals(FilterDataType.STRING, myIdOperatorOr.getFilterDataType());
            assertEquals(FilterType.TERM, myIdOperatorOr.getFilterType());
            assertEquals(FilterOperator.OR, myIdOperatorOr.getFilterOperator());

            SearchFilter myIdOperatorAnd = actual.getSearchFilterById("myid-operator-and");
            assertEquals("myValue", myIdOperatorAnd.getValues().get(0));
            assertEquals(FilterDataType.STRING, myIdOperatorAnd.getFilterDataType());
            assertEquals(FilterType.TERM, myIdOperatorAnd.getFilterType());
            assertEquals(FilterOperator.AND, myIdOperatorAnd.getFilterOperator());

            SearchFilter myRangeFilter = actual.getSearchFilterById("my-range-filter");
            assertEquals(FilterDataType.NUMBER, myRangeFilter.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeFilter.getFilterType());
            assertEquals(0.0, myRangeFilter.getMinValue());
            assertEquals(10.0, myRangeFilter.getMaxValue());

            SearchFilter myRangeFilterWithoutDatatype = actual.getSearchFilterById("my-range-filter-without-filterDataType");
            assertEquals(FilterDataType.NUMBER, myRangeFilterWithoutDatatype.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeFilterWithoutDatatype.getFilterType());
            assertEquals(0.0, myRangeFilterWithoutDatatype.getMinValue());
            assertEquals(10.0, myRangeFilterWithoutDatatype.getMaxValue());

            SearchFilter myRangeFilterWithoutDatatypeFilterType = actual.getSearchFilterById("my-range-filter-without-filterDataType-filterType");
            assertEquals(FilterDataType.NUMBER, myRangeFilterWithoutDatatypeFilterType.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeFilterWithoutDatatypeFilterType.getFilterType());
            assertEquals(0.0, myRangeFilterWithoutDatatypeFilterType.getMinValue());
            assertEquals(10.0, myRangeFilterWithoutDatatypeFilterType.getMaxValue());



            SearchFilter myRangeDateFilter= actual.getSearchFilterById("my-range-date-filter");
            assertEquals(FilterDataType.DATE, myRangeDateFilter.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeDateFilter.getFilterType());
            assertTrue(myRangeDateFilter.getMinValue() instanceof String);
            assertTrue(myRangeDateFilter.getMaxValue() instanceof String);
            assertEquals("2000-08-06", myRangeDateFilter.getMinValue());
            assertEquals("2020-08-06", myRangeDateFilter.getMaxValue());



            SearchFilter myRangeDateFilterWithoutFilterDataType = actual.getSearchFilterById("my-range-date-filter-without-filterDataType");
            assertEquals(FilterDataType.DATE, myRangeDateFilterWithoutFilterDataType.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeDateFilterWithoutFilterDataType.getFilterType());
            assertTrue(myRangeDateFilterWithoutFilterDataType.getMinValue() instanceof String);
            assertTrue(myRangeDateFilterWithoutFilterDataType.getMaxValue() instanceof String);
            assertEquals("2000-08-06", myRangeDateFilterWithoutFilterDataType.getMinValue());
            assertEquals("2020-08-06", myRangeDateFilterWithoutFilterDataType.getMaxValue());


            SearchFilter myRangeDateFilterWithoutFilterDataTypeFilterType = actual.getSearchFilterById("my-range-date-filter-without-filterDataType-filterType");
            assertEquals(FilterDataType.DATE, myRangeDateFilterWithoutFilterDataTypeFilterType.getFilterDataType());
            assertEquals(FilterType.RANGE, myRangeDateFilterWithoutFilterDataTypeFilterType.getFilterType());
            assertTrue(myRangeDateFilterWithoutFilterDataTypeFilterType.getMinValue() instanceof String);
            assertTrue(myRangeDateFilterWithoutFilterDataTypeFilterType.getMaxValue() instanceof String);
            assertEquals("2000-08-06", myRangeDateFilterWithoutFilterDataTypeFilterType.getMinValue());
            assertEquals("2020-08-06", myRangeDateFilterWithoutFilterDataTypeFilterType.getMaxValue());

        }
    }

    @Test
    void parseSearchQueryPostRequestTrackingTags() throws IOException {
        String fileName = "/com/quasiris/qsf/migration/qsf-search-query-parser-test-post-tracking-tags.json";
        try (InputStream is = TestSuiteExecuter.class.getResourceAsStream(fileName)) {

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletInputStream servletInputStream = mockServletInputStream(is);
            when(mockRequest.getInputStream()).thenReturn(servletInputStream);
            when(mockRequest.getMethod()).thenReturn("POST");

            SearchQuery actual = parser.parseSearchQuery(mockRequest);

            assertEquals(2, actual.getTrackingTags().size());
            assertEquals("testing", actual.getTrackingTags().get(0));
            assertEquals("monitoring", actual.getTrackingTags().get(1));
        }
    }


    public static ServletInputStream mockServletInputStream(InputStream is) throws IOException {
        ServletInputStream mockServletInputStream = mock(ServletInputStream.class);
        Mockito.when(mockServletInputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer((Answer<Integer>) invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            byte[] output = (byte[]) args[0];
            int offset = (int) args[1];
            int length = (int) args[2];
            return is.read(output, offset, length);
        });
        return mockServletInputStream;
    }

}