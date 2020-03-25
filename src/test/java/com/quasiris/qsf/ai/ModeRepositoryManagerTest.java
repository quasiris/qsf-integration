package com.quasiris.qsf.ai;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModeRepositoryManagerTest {


    @Test
    public void testPathNamesAndUrl() {

        ModelRepositoryManager modelRepositoryManager = ModelRepositoryManager.Builder.create().
                groupId("com.quasiris.qsf").
                artifactId("test-model").
                version("1.2.3").
                modelBasePath("/path/to/models").
                modelBaseUrl("https://models.quasiris.de/models").
                uploadBaseUrl("https://upload.quasiris.de/models").
                build();

        assertEquals("com/quasiris/qsf/test-model/1.2.3/", modelRepositoryManager.getUrlPath());
        assertEquals("com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelRepositoryManager.getUrlZipFile());
        assertEquals("https://models.quasiris.de/models/", modelRepositoryManager.getModelBaseUrl());
        assertEquals("/path/to/models/", modelRepositoryManager.getModelBasePath());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/", modelRepositoryManager.getAbsoluteModelPath());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3/my-model.bin", modelRepositoryManager.getAbsoluteModelFile("my-model.bin"));
        assertEquals("https://models.quasiris.de/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelRepositoryManager.getModelUrl());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelRepositoryManager.getZipFile());
        assertEquals("https://upload.quasiris.de/models/", modelRepositoryManager.getUploadBaseUrl());
        assertEquals("https://upload.quasiris.de/models/com.quasiris.qsf/test-model/1.2.3", modelRepositoryManager.getUploadUrl());
    }

}