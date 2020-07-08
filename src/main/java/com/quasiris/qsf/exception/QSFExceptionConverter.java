package com.quasiris.qsf.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QSFExceptionConverter {

    public static String error2Html(PipelineContainerException ex) {
        StringBuilder builder = new StringBuilder();
        if(ex.getPipelineContainer() != null && ex.getPipelineContainer().isDebugEnabled()) {
            builder.append(debugToHtml(ex.getPipelineContainer().getDebugStack()));
        }

        builder.append("<pre>");
        builder.append(ex.getMessage());
        builder.append("</pre>");

        return builder.toString();

    }

    public static Object debugToType(String debugId, String debugType, List<Debug> debugList) {
        if(debugId == null && "json".equals(debugType)) {
            Map<String, String> json = new HashMap<>();
            for(Debug debug : debugList) {
                String debugText = debugToType(debugType, debug).toString();
                if(!Strings.isNullOrEmpty(debug.getId())) {
                    json.put(debug.getId(), debugText);
                }
            }
            return json;
        }

        if(debugId == null) {
            return debugToHtml(debugList).toString();
        }



        for(Debug debug : debugList) {
            if(debug.getId() != null && debug.getId().equals(debugId)) {
                return debugToType(debugType, debug);
            }
        }
        return debugToHtml(debugList).toString();
    }

    public static Object debugToType(String debugId, String debugType, PipelineContainerDebugException ex) {
       return debugToType(debugId, debugType, ex.getDebugStack());
    }

    public static String debugToHtml(PipelineContainerDebugException ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(debugToHtml(ex.getDebugStack()));
        return builder.toString();
    }

    public static StringBuilder debugToHtml(List<Debug> debugList) {
        StringBuilder builder = new StringBuilder();
        for(Debug debug : debugList) {
           builder.append(debugToHtml(debug));
        }
        return builder;
    }


    public static Object debugToType(String type, Debug debug) {
        if("text".equals(type)) {
            return debugToText(debug);
        }
        if("json".equals(type) && debug.getType().isJson()) {
            return debug.getDebugObject();
        }
        return debugToHtml(debug);
    }

    public static StringBuilder debugToText(Debug debug) {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();

        Object formatted = null;
        DebugType debugType = debug.getType();
        if (debugType.isJson() || debugType.isObject()) {
            try {
                formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(debug.getDebugObject());
            } catch (JsonProcessingException e) {
                formatted = ExceptionUtils.getStackTrace(e);
            }
        } else {
            formatted = debug.getDebugObject();
        }
        builder.append(formatted);

        return builder;
    }

    public static StringBuilder debugToHtml(Debug debug) {
        StringBuilder builder = new StringBuilder();

        builder.append("\n<pre>\n");
        builder.append(debug.getId() + ":\n");

        builder.append(debugToText(debug));
        builder.append("</pre>");
        return builder;
    }
}
