package com.quasiris.qsf.pipeline.filter.web;

import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;

import javax.servlet.http.Cookie;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by mki on 02.12.17.
 */
public class RequestParser {


    public  static Map<String, String> getRequestParameter(PipelineContainer pipelineContainer) {
        Map<String, String> replaceMap = new HashMap<>();
        if(pipelineContainer.getRequest() == null) {
            return replaceMap;
        }
        synchronized (pipelineContainer.getRequest()) {
            Enumeration<String> parameterName = pipelineContainer.getRequest().getParameterNames();
            while (parameterName.hasMoreElements()) {
                String name = parameterName.nextElement();
                replaceMap.put(name, pipelineContainer.getRequest().getParameter(name));
                replaceMap.put("query." + name, pipelineContainer.getRequest().getParameter(name));
            }

            Enumeration<String> headerNames =  pipelineContainer.getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = pipelineContainer.getRequest().getHeader(name);
                replaceMap.put("header." + name, value);
            }


            if(pipelineContainer.getRequest().getCookies() != null) {
                for (Cookie cookie : pipelineContainer.getRequest().getCookies()) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    replaceMap.put("cookie." + name, value);
                }
            }

            String path = pipelineContainer.getRequest().getRequestURI();
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
