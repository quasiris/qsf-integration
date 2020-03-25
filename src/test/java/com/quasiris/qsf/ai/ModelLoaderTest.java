package com.quasiris.qsf.ai;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModelLoaderTest {


    @Test
    public void testPathNamesAndUrl() {

        ModelLoader modelLoader = ModelLoader.ModelLoaderBuilder.create().
                groupId("com.quasiris.qsf").
                artifactId("test-model").
                version("1.2.3").
                modelBasePath("/path/to/models").
                modelBaseUrl("https://models.quasiris.de/models").
                uploadBaseUrl("https://upload.quasiris.de/models").
                build();

        assertEquals("com/quasiris/qsf/test-model/1.2.3/", modelLoader.getUrlPath());
        assertEquals("com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelLoader.getUrlZipFile());
        assertEquals("https://models.quasiris.de/models/", modelLoader.getModelBaseUrl());
        assertEquals("/path/to/models/", modelLoader.getModelBasePath());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/", modelLoader.getAbsoluteModelPath());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3/my-model.bin", modelLoader.getAbsoluteModelFile("my-model.bin"));
        assertEquals("https://models.quasiris.de/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelLoader.getModelUrl());
        assertEquals("/path/to/models/com/quasiris/qsf/test-model/1.2.3/test-model-1.2.3.zip", modelLoader.getZipFile());
        assertEquals("https://upload.quasiris.de/models/", modelLoader.getUploadBaseUrl());
        assertEquals("https://upload.quasiris.de/models/com.quasiris.qsf/test-model/1.2.3", modelLoader.getUploadUrl());
    }

}