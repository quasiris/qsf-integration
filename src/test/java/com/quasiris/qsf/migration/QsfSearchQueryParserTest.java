package com.quasiris.qsf.migration;

import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.test.service.TestSuiteExecuter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Matchers.anyInt;
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
            put("f.humandate", new String[]{"1992"});
            put("f.tree", new String[]{"node1 > node2"});
        }};
        when(mockRequest.getParameterMap()).thenReturn(parameters);
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(parameters.keySet()));
        when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(new ArrayList<>()));
        when(mockRequest.getRequestURI()).thenReturn("/deploy-folder/servlet-mapping/request-path");
        when(mockRequest.getMethod()).thenReturn("GET");

        SearchQuery actual = parser.parseSearchQuery(mockRequest);
        System.out.println("actual = " + actual);
        Assertions.assertEquals(q, actual.getQ());
        Assertions.assertEquals(requestId, actual.getRequestId());
        Assertions.assertEquals(requestId, actual.getRequestId());
        Assertions.assertEquals(1, actual.getPage());
        Assertions.assertEquals(0, actual.getRows());
        Assertions.assertFalse(actual.isDebug());
        Assertions.assertEquals("humandate", ((SearchFilter)actual.getSearchFilterList().get(0)).getId());
        Assertions.assertEquals("tree", ((SearchFilter)actual.getSearchFilterList().get(1)).getId());

    }

    @Test
    void parseSearchQueryPostRequestSuccess() throws IOException {
        String fileName = "/com/quasiris/qsf/migration/qsf-search-query-parser-test-post-success.json";
        try (InputStream is = TestSuiteExecuter.class.getResourceAsStream(fileName)) {

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletInputStream servletInputStream = mockServletInputStream(is);
            when(mockRequest.getInputStream()).thenReturn(servletInputStream);
            when(mockRequest.getMethod()).thenReturn("POST");

            SearchQuery actual = parser.parseSearchQuery(mockRequest);

            Assertions.assertEquals("*", actual.getQ());
            Assertions.assertEquals(1, actual.getPage());
            Assertions.assertEquals(1, actual.getRows());
            Assertions.assertFalse(actual.isDebug());
        }
    }


    public static ServletInputStream mockServletInputStream(InputStream is) throws IOException {
        ServletInputStream mockServletInputStream = mock(ServletInputStream.class);
        when(mockServletInputStream.read(Matchers.any(), anyInt(), anyInt())).thenAnswer((Answer<Integer>) invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            byte[] output = (byte[]) args[0];
            int offset = (int) args[1];
            int length = (int) args[2];
            return is.read(output, offset, length);
        });
        return mockServletInputStream;
    }

}