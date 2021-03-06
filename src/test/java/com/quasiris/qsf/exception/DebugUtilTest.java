package com.quasiris.qsf.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DebugUtilTest {

    @Test
    public void testError2Html() {
        PipelineContainerException pipelineContainerException = new PipelineContainerException("test error");
        String html =  QSFExceptionConverter.error2Html(pipelineContainerException);
        assertTrue(html.contains("test error"));
    }


    @Test
    public void testError2HtmlWithDebugEnabled() {

        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setDebug(true);
        Debug debug = new Debug();
        debug.setDebugObject("test debug");
        pipelineContainer.debug(debug);
        PipelineContainerException ex = new PipelineContainerException(pipelineContainer, "test error");

        String html =  QSFExceptionConverter.error2Html(ex);
        assertTrue(html.contains("test error"));
        assertTrue(html.contains("test debug"));
    }

    @Test
    public void testDebug2HtmlWithDebugEnabled() {

        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setDebug(true);
        Debug debug = new Debug();
        debug.setDebugObject("test debug");
        pipelineContainer.debug(debug);
        PipelineContainerDebugException ex = new PipelineContainerDebugException(pipelineContainer);

        String html =  QSFExceptionConverter.debugToHtml(ex);
        assertTrue(html.contains("test debug"));
    }

}