package com.quasiris.qsf.ai;

import com.quasiris.qsf.util.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

/**
 * Download and save models from a url to a local path.
 * If the model exists the local model is used.
 */
public class ModelRepositoryManager {
    private static final Logger logger = LoggerFactory.getLogger(ModelRepositoryManager.class);

    private String groupId;
    private String artifactId;
    private String version;
    private String modelBaseUrl;
    private String uploadBaseUrl;
    private String modelBasePath;


    protected String getModelName() {
        return artifactId + "-" + version;
    }

    protected String getUrlZipFile() {
        return getUrlPath() + getModelName() + ".zip";
    }


    protected String getUrlPath() {
        StringBuilder groupIdPath = new StringBuilder(groupId.replaceAll(Pattern.quote("."), "/"));
        groupIdPath.append("/");
        groupIdPath.append(artifactId);
        groupIdPath.append("/");
        groupIdPath.append(version);
        groupIdPath.append("/");
        return groupIdPath.toString();
    }


    /**
     * Zip the source directory and save it to the specified path.
     *
     * @param sourceDir the source directory to zip.
     * @throws IOException if the zip file can not be created or saved.
     */
    public void save(String sourceDir) throws IOException {
        String absoluteModelPath =  getAbsoluteModelPath();
        IOUtils.createDirectoryIfNotExists(absoluteModelPath);
        String zipFileName = getZipFile();
        IOUtils.zip(sourceDir, getModelName(), zipFileName);
    }

    public void saveAndUpload(String sourceDir) throws IOException {
        save(sourceDir);
        upload(getZipFile());
    }

    /**
     * Save the input stream as a zip file in the correct repository structure.
     * The zip file must be created in a correct way. See TODO
     *
     * @param zipFileInputStream input stream of the zip file
     * @throws IOException if the file can not be saved
     */
    public void save(InputStream zipFileInputStream) throws IOException {
        String absoluteModelPath =  getAbsoluteModelPath();
        IOUtils.createDirectoryIfNotExists(absoluteModelPath);
        String zipFile = getZipFile();
        FileUtils.copyInputStreamToFile(zipFileInputStream, new File(zipFile));
    }


    /**
     * Load a specific file from the zipped model directory.
     * If the model does not exists, it is installed.
     *
     * @param fileName the file name to be loaded.
     * @return the inputstream of the file.
     * @throws FileNotFoundException if the file was not found.
     */
    public InputStream load(String fileName) throws FileNotFoundException {
        install();
        String absoluteFile = getAbsoluteModelFile(fileName);
        return new FileInputStream(new File(absoluteFile));
    }

    protected boolean isInstalled() {
        Path path = Paths.get(getAbsoluteModelPath());
        return Files.exists(path)
                && Files.isReadable(path)
                && Files.isWritable(path);
    }

    public void install() {
        if(!isInstalled()) {
            logger.warn("Model {} is not installed locally!", getModelName());
            download();
            unzip();
        } else {
            logger.info("Found installed model {}", getModelName());
        }
    }

    protected String getModelUrl() {
        return modelBaseUrl + getUrlZipFile();
    }

    protected String getAbsoluteModelPath() {
        return modelBasePath + getUrlPath();
    }

    protected String getAbsoluteModelFile(String fileName) {
        return modelBasePath + getUrlPath()  + getModelName() + "/" + fileName;
    }

    protected String getUploadUrl() {

        StringBuilder uploadUrl = new StringBuilder(uploadBaseUrl);
        uploadUrl.append(groupId);
        uploadUrl.append("/");
        uploadUrl.append(artifactId);
        uploadUrl.append("/");
        uploadUrl.append(version);

        return uploadUrl.toString();
    }

    public void upload(String fileName) throws IOException {
        String url = getUploadUrl();

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);
            File file = new File(fileName);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, fileName);
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = httpclient.execute(post);

            if(response.getStatusLine().getStatusCode() >= 300) {
                String responseBody = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("Could not upload file " + fileName +
                        " to url: " + url +
                        " http code: " + response.getStatusLine().getStatusCode() +
                        " message: " +  responseBody);
            }
            httpclient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void download() {

        String modelUrl = getModelUrl();
        String path = getAbsoluteModelPath();

        IOUtils.createDirectoryIfNotExists(path);

        logger.info("Downloading model {} from url: {} to path: {} ", getModelName(), modelUrl, getZipFile());
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

    protected String getZipFile() {
        return modelBasePath + getUrlZipFile();
    }



    protected void unzip() {
        String zipFile = getZipFile();
        try {
            logger.info("Unzipping downloaded file {}", zipFile);
            IOUtils.unzip(zipFile);
            logger.info("Finished Unzipping file {}" , zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not unzip file: " + zipFile, e);
        }
    }

    public static final class Builder {
        private String groupId;
        private String artifactId;
        private String version;
        private String modelBaseUrl;
        private String modelBasePath;
        private String uploadBaseUrl;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder shortId(String shortId) {
            String[] splitted = shortId.split(Pattern.quote("|"));
            if(splitted.length < 3) {
                throw new IllegalArgumentException("The short id: " + shortId + " is invalid.");
            }
            this.groupId = splitted[0];
            this.artifactId = splitted[1];
            this.version = splitted[2];
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder modelBaseUrl(String modelBaseUrl) {
            this.modelBaseUrl = modelBaseUrl;
            return this;
        }

        public Builder modelBasePath(String modelBasePath) {
            this.modelBasePath = modelBasePath;
            return this;
        }

        public Builder uploadBaseUrl(String uploadBaseUrl) {
            this.uploadBaseUrl = uploadBaseUrl;
            return this;
        }

        public ModelRepositoryManager build() {
            ModelRepositoryManager modelRepositoryManager =  new ModelRepositoryManager();
            modelRepositoryManager.setGroupId(groupId);
            modelRepositoryManager.setArtifactId(artifactId);
            modelRepositoryManager.setVersion(version);
            modelRepositoryManager.setModelBasePath(modelBasePath);
            modelRepositoryManager.setModelBaseUrl(modelBaseUrl);
            modelRepositoryManager.setUploadBaseUrl(uploadBaseUrl);
            return modelRepositoryManager;
        }
    }

    /**
     * Getter for property 'groupId'.
     *
     * @return Value for property 'groupId'.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Setter for property 'groupId'.
     *
     * @param groupId Value to set for property 'groupId'.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Getter for property 'artifactId'.
     *
     * @return Value for property 'artifactId'.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Setter for property 'artifactId'.
     *
     * @param artifactId Value to set for property 'artifactId'.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Getter for property 'version'.
     *
     * @return Value for property 'version'.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter for property 'version'.
     *
     * @param version Value to set for property 'version'.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter for property 'modelBaseUrl'.
     *
     * @return Value for property 'modelBaseUrl'.
     */
    public String getModelBaseUrl() {
        return modelBaseUrl;
    }

    /**
     * Setter for property 'modelBaseUrl'.
     *
     * @param modelBaseUrl Value to set for property 'modelBaseUrl'.
     */
    public void setModelBaseUrl(String modelBaseUrl) {
        this.modelBaseUrl = IOUtils.ensureEndingSlash(modelBaseUrl);
    }

    /**
     * Getter for property 'modelBasePath'.
     *
     * @return Value for property 'modelBasePath'.
     */
    public String getModelBasePath() {
        return modelBasePath;
    }

    /**
     * Setter for property 'modelBasePath'.
     *
     * @param modelBasePath Value to set for property 'modelBasePath'.
     */
    public void setModelBasePath(String modelBasePath) {
        this.modelBasePath = IOUtils.ensureEndingSlash(modelBasePath);
    }

    /**
     * Getter for property 'uploadBaseUrl'.
     *
     * @return Value for property 'uploadBaseUrl'.
     */
    public String getUploadBaseUrl() {
        return uploadBaseUrl;
    }

    /**
     * Setter for property 'uploadBaseUrl'.
     *
     * @param uploadBaseUrl Value to set for property 'uploadBaseUrl'.
     */
    public void setUploadBaseUrl(String uploadBaseUrl) {
        this.uploadBaseUrl = IOUtils.ensureEndingSlash(uploadBaseUrl);
    }
}
