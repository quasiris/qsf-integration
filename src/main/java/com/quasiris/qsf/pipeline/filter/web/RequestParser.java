package com.quasiris.qsf.pipeline.filter.web;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by mki on 02.12.17.
 */
public class RequestParser {

    public  static Map<String, String> getRequestParameter(PipelineContainer pipelineContainer) {
        return getRequestParameter(pipelineContainer.getRequest());


    }
     public  static Map<String, String> getRequestParameter(HttpServletRequest request) {
        Map<String, String> replaceMap = new HashMap<>();
        if(request == null) {
            return replaceMap;
        }
        synchronized (request) {
            Enumeration<String> parameterName = request.getParameterNames();
            while (parameterName.hasMoreElements()) {
                String name = parameterName.nextElement();
                replaceMap.put(name, request.getParameter(name));
                replaceMap.put("query." + name, request.getParameter(name));
            }

            Enumeration<String> headerNames =  request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                replaceMap.put("header." + name, value);
            }


            if(request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    replaceMap.put("cookie." + name, value);
                }
            }

            String path = request.getRequestURI();
            int pathCounter = 0;
            for(String pathPart: path.split(Pattern.quote("/"))) {
                if(!Strings.isNullOrEmpty(pathPart)) {
                    replaceMap.put("path" + pathCounter++ + ".", pathPart);
                }
            }
            return replaceMap;
        }

    }


}
