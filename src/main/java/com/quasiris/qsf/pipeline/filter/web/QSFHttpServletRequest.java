package com.quasiris.qsf.pipeline.filter.web;

import com.google.common.base.Splitter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.*;

@Deprecated
public class QSFHttpServletRequest implements HttpServletRequest {

    private String url;

    private URI uri;

    private String method;

    private Enumeration<String> parameterNames;

    private Map<String, List<String>> parametersMap;

    private Map<String, String[]> decodedParametersMap;

    public QSFHttpServletRequest(String url) throws URISyntaxException {
        this.url = url;
        init();
    }

    private void init() throws URISyntaxException {

        uri = new URI(url);
        String query = "";
        if(uri.getQuery() != null) {
            query = uri.getQuery();
        }
        final Iterable<String> parameterList = Splitter.on("&")
                .omitEmptyStrings()
                .trimResults().split(query);

        parametersMap = new HashMap<>();

        decodedParametersMap = new HashMap<>();

        for (String param : parameterList) {
            List<String> splitted = Splitter.on("=").splitToList(param);
            if (splitted.size() != 2) {
                throw new URISyntaxException(url, "The url " + url + " is not valid for param: " + param);
            }
            String key = splitted.get(0);
            List<String> values = parametersMap.get(key);
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(splitted.get(1));
            parametersMap.put(key, values);
        }

        for (Map.Entry<String, List<String>> entry : parametersMap.entrySet()) {
            List<String> decodedValues = new ArrayList<>();
            for (String value : entry.getValue()) {
                decodedValues.add(decodeUrlParam(value));
            }
            decodedParametersMap.put(entry.getKey(), decodedValues.toArray(new String[0]));

        }
        parameterNames = Collections.enumeration(parametersMap.keySet());

        this.method = "GET";
    }


    public String decodeUrlParam(String param) throws URISyntaxException {
        try {
            return URLDecoder.decode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new URISyntaxException(url, "The param " + param + " could not be decoded.");
        }
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Cookie[] getCookies() {
        Cookie[] cookies = {};
        return cookies;
    }

    @Override
    public long getDateHeader(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public int getIntHeader(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getQueryString() {
        return uri.getQuery();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isUserInRole(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getRequestURI() {
        return uri.getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public HttpSession getSession(boolean b) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Object getAttribute(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public int getContentLength() {
       throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public long getContentLengthLong() {
       throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getParameter(String key) {
        List<String> values = parametersMap.get(key);
        if(values == null || values.size() == 0) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return parameterNames;
    }

    @Override
    public String[] getParameterValues(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return decodedParametersMap;
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public int getServerPort() {
       throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public void setAttribute(String s, Object o) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public void removeAttribute(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public int getRemotePort() {
       throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public int getLocalPort() {
       throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getRequestId() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public String getProtocolRequestId() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }

    @Override
    public ServletConnection getServletConnection() {
        throw new UnsupportedOperationException("The method is not implemented yet.");
    }
}
