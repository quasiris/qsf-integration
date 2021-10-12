package com.quasiris.qsf.pipeline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class PipelineValidationTest {

    @Test
    void errorTestSingleElement() {
        PipelineValidation pipelineValidation = new PipelineValidation();
        String msg = "Not empty msg";
        pipelineValidation.error(msg);
        List<String> actualMsgs = pipelineValidation.getPipelineValidationErrors().stream()
                .map(PipelineValidationError::getMessage)
                .collect(Collectors.toList());
        Assertions.assertEquals(Collections.singletonList(msg), actualMsgs);
    }

    @Test
    void errorTestMultipleElements() {
        PipelineValidation pipelineValidation = new PipelineValidation();
        List<String> msgs = Arrays.asList(null, "", "Not empty msg");
        for (String msg : msgs) {
            pipelineValidation.error(msg);
        }
        List<String> actualMsgs = pipelineValidation.getPipelineValidationErrors().stream()
                .map(PipelineValidationError::getMessage)
                .collect(Collectors.toList());
        Assertions.assertEquals(msgs, actualMsgs);
    }
}