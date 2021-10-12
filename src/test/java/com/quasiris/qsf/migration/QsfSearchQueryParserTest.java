package com.quasiris.qsf.migration;

import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QsfSearchQueryParserTest {
    private final QsfSearchQueryParser parser = new QsfSearchQueryParser();

    @Test
    void parseSearchQuery() {
    }

    @Test
    void handlePOSTRequest() {
    }

    @Test
    void handleGETRequest() {
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

        SearchQuery actual = parser.handleGETRequest(mockRequest);

        Assertions.assertEquals(q, actual.getQ());
        Assertions.assertEquals(requestId, actual.getRequestId());
        Assertions.assertEquals(requestId, actual.getRequestId());
        Assertions.assertEquals(1, actual.getPage());
        Assertions.assertEquals(0, actual.getRows());
        Assertions.assertFalse(actual.isDebug());
        Assertions.assertEquals("humandate", actual.getSearchFilterList().get(0).getId());
        Assertions.assertEquals("tree", actual.getSearchFilterList().get(1).getId());
    }
}