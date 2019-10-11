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

    public static String debugToHtml(PipelineContainerDebugException ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(debugToHtml(ex.getDebugStack()));
        return builder.toString();
    }

    public static StringBuilder debugToHtml(List<Object> debugObjects) {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();
        for(Object debugObject : debugObjects) {

            Object formatted = debugObject;

            try {
                formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(debugObject);
            } catch (JsonProcessingException e) {
                //
            }
            builder.append("<pre>");
            builder.append(formatted);
            builder.append("</pre>");
        }
        return builder;
    }
}
