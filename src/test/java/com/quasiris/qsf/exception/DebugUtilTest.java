package com.quasiris.qsf.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import org.junit.Assert;
import org.junit.Test;

public class DebugUtilTest {

    @Test
    public void testError2Html() {
        PipelineContainerException pipelineContainerException = new PipelineContainerException("test error");
        String html =  QSFExceptionConverter.error2Html(pipelineContainerException);
        Assert.assertTrue(html.contains("test error"));
    }


    @Test
    public void testError2HtmlWithDebugEnabled() {

        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setDebug(true);
        pipelineContainer.debug("test debug");
        PipelineContainerException ex = new PipelineContainerException(pipelineContainer, "test error");

        String html =  QSFExceptionConverter.error2Html(ex);
        Assert.assertTrue(html.contains("test error"));
        Assert.assertTrue(html.contains("test debug"));
    }

    @Test
    public void testDebug2HtmlWithDebugEnabled() {

        PipelineContainer pipelineContainer = new PipelineContainer();
        pipelineContainer.setDebug(true);
        pipelineContainer.debug("test debug");
        PipelineContainerDebugException ex = new PipelineContainerDebugException(pipelineContainer);

        String html =  QSFExceptionConverter.debugToHtml(ex);
        Assert.assertTrue(html.contains("test debug"));
    }

}