package de.quasiris.qsf.mock;

import com.google.common.base.Splitter;
import de.quasiris.qsf.pipeline.filter.solr.MockSolrClient;
import org.mockito.Mockito;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mki on 25.11.17.
 */
public class Mockfactory {

    public static HttpServletRequest createHttpServletRequest(String url){
        try {
            URI uri = new URI(url);


            final Map<String, String> parametersMap = Splitter.on("&")
                    .omitEmptyStrings()
                    .trimResults()
                    .withKeyValueSeparator("=")
                    .split(uri.getQuery());

            final Map<String, String[]> decodedParametersMap = parametersMap.
                    entrySet().
                    stream().
                    collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> decodeUrlParam(entry.getValue())));


            //map(e -> decodeUrlParam(e.getValue()))


            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
            Enumeration<String> parameterNames = Collections.enumeration(parametersMap.keySet());
            Mockito.when(httpServletRequest.getParameterNames()).thenReturn(parameterNames);
            for (String key : parametersMap.keySet()) {
                Mockito.when(httpServletRequest.getParameter(key)).thenReturn(decodedParametersMap.get(key)[0]);
            }

            Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(parameterNames);
            Cookie[] cookies = {};
            Mockito.when(httpServletRequest.getCookies()).thenReturn(cookies);

            Mockito.when(httpServletRequest.getRequestURI()).thenReturn(uri.getPath());

            Mockito.when(httpServletRequest.getParameterMap()).thenReturn(decodedParametersMap);

            return httpServletRequest;

        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public static String[] decodeUrlParam(String param) {
        try {
            String[] paramArray = new String[] {URLDecoder.decode(param, "UTF-8")};
            return paramArray;
        } catch (UnsupportedEncodingException e) {
            String[] paramArray = new String[] {param};
            return paramArray;
        }
    }

    public static MockSolrClient createSolrClient(String url) {
        MockSolrClient mockSolrClient = new MockSolrClient(url);
        //mockSolrClient.setRecord(true);
        return mockSolrClient;
    }
}
