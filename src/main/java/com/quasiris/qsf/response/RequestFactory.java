package com.quasiris.qsf.response;

import com.quasiris.qsf.dto.response.Request;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

@Deprecated
public class RequestFactory {
    public static Request create(HttpServletRequest httpServletRequest) {
        Request request = new Request();
        request.setPath(httpServletRequest.getRequestURI());
        request.setParameters(httpServletRequest.getParameterMap());
        request.setMethod(httpServletRequest.getMethod());

        request.setQuery(httpServletRequest.getQueryString());
        if(StringUtils.isEmpty(request.getQuery())) {
            request.setUrl(httpServletRequest.getRequestURL().toString());
        } else {
            request.setUrl(httpServletRequest.getRequestURL().append("?").append(request.getQuery()).toString());
        }
        return request;
    }
}
