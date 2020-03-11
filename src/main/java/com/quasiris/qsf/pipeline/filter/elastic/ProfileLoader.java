package com.quasiris.qsf.pipeline.filter.elastic;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class ProfileLoader {

    public static String loadProfile(String filename, Map<String, String> vars) throws IOException {
        String profile = null;
        if (filename.startsWith("classpath://")) {
            profile = loadProfileFromClasspath(filename);
        } else {
            profile = loadProfileFromFile(filename);
        }
        return profile;
    }

    public static String loadProfileFromClasspath(String filename) throws IOException {

        String resource = filename.replaceFirst("classpath://", "");
        InputStream in = ProfileLoader.class.getClassLoader()
                .getResourceAsStream(resource);

        // TODO try to remove IOUtils
        String profile = IOUtils.toString(in, Charset.forName("UTF-8"));
        IOUtils.closeQuietly(in);
        return profile;
    }

    public static String loadProfileFromFile(String filename) throws IOException {
        File file = new File(filename);
        String profile = Files.toString(file, Charsets.UTF_8);
        return profile;
    }
}
