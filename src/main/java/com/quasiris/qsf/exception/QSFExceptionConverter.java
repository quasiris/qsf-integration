package com.quasiris.qsf.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;

import java.util.List;

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

    public static String debugToHtml(String debugId, PipelineContainerDebugException ex) {
        if(debugId == null) {
            return debugToHtml(ex);
        }
        for(Debug debug : ex.getDebugStack()) {
            if(debug.getId() != null && debug.getId().equals(debugId)) {
                return debugToHtml(debug).toString();
            }
        }
        return debugToHtml(ex);
    }

    public static String debugToHtml(PipelineContainerDebugException ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(debugToHtml(ex.getDebugStack()));
        return builder.toString();
    }

    public static StringBuilder debugToHtml(List<Debug> debugList) {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();
        for(Debug debug : debugList) {
           builder.append(debugToHtml(debug));
        }
        return builder;
    }

    public static StringBuilder debugToHtml(Debug debug) {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();

        Object formatted = null;
        DebugType debugType = debug.getType();
        if (debugType.isJson() || debugType.isObject()) {
            try {
                formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(debug.getDebugObject());
            } catch (JsonProcessingException e) {
                //
            }
        } else {
            formatted = debug.getDebugObject();
        }
        builder.append("<pre>");
        builder.append(formatted);
        builder.append("</pre>");

        return builder;
    }
}
