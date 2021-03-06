package com.quasiris.qsf.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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


    @Test
    public void testShortId() {

        ModelRepositoryManager modelRepositoryManager = ModelRepositoryManager.Builder.create().
               shortId("com.quasiris.qsf|test-model|1.2.3").
                build();

        assertEquals("com.quasiris.qsf", modelRepositoryManager.getGroupId());
        assertEquals("test-model", modelRepositoryManager.getArtifactId());
        assertEquals("1.2.3", modelRepositoryManager.getVersion());
    }

    @Test
    public void testShortIdWithFilename() {

        ModelRepositoryManager modelRepositoryManager = ModelRepositoryManager.Builder.create().
                shortId("com.quasiris.qsf|test-model|1.2.3|test-model.bin").
                build();

        assertEquals("com.quasiris.qsf", modelRepositoryManager.getGroupId());
        assertEquals("test-model", modelRepositoryManager.getArtifactId());
        assertEquals("1.2.3", modelRepositoryManager.getVersion());
    }
}