package com.quasiris.qsf.ai;

import com.quasiris.qsf.util.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Download models from a url to a local path.
 * If the model exists the local model is used.
 */
public class ModelLoader {
    private static final Logger logger = LoggerFactory.getLogger(ModelLoader.class);

    private String modelBaseUrl;
    private String modelBasePath;
    private String modelName;

    public ModelLoader(String modelBaseUrl, String modelBasePath, String modelName) {
        this.modelBasePath = IOUtils.ensureEndingSlash(modelBasePath);
        this.modelBaseUrl =  IOUtils.ensureEndingSlash(modelBaseUrl);
        this.modelName = modelName;
    }

    public boolean isInstalled() {
        Path path = Paths.get(modelBasePath, modelName);
        return Files.exists(path)
                && Files.isReadable(path)
                && Files.isWritable(path);
    }

    public void install() {
        if(!isInstalled()) {
            logger.warn("Model "+modelName+" is not installed locally!");
            download();
            unzip();
        } else {
            logger.info("Found installed model "+modelName);
        }
    }

    protected void download() {
        logger.info("Downloading model {} from url: {} to path: {} ", modelName, modelBaseUrl, modelBasePath);
        String modelUrl = modelBaseUrl + modelName + ".zip";
        try {

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(modelUrl);

            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() < 300) {
                try (InputStream inputStream = response.getEntity().getContent()) {
                    Files.copy(inputStream, Paths.get(getZipFile()), StandardCopyOption.REPLACE_EXISTING);
                }
                httpclient.close();
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getStatusLine().getStatusCode();
                httpclient.close();
                throw new HttpResponseException(statusCode, responseBody);
            }
            logger.info("Unzipping finished!");
        } catch (Exception e) {
            throw new RuntimeException("Something gone wrong while downloading model file from " + modelUrl, e);
        }
    }

    private String getZipFile() {
        return modelBasePath + modelName + ".zip";
    }

    public void unzip() {
        String zipFile = getZipFile();
        try {
            logger.info("Unzipping downloaded file {}", zipFile);
            IOUtils.unzip(zipFile);
            logger.info("Finished Unzipping file {}" , zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not unzip file: " + zipFile, e);
        }
    }
}
