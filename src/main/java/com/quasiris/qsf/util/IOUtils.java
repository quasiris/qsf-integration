package com.quasiris.qsf.util;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tblsoft
 */
public class IOUtils {

    public static String getAbsoluteFile(String directory, String fileName) {
        if(fileName.toLowerCase().startsWith("http")) {
            return fileName;
        } else if(fileName.toLowerCase().startsWith("c:\\")) {
    		return fileName;
    	} else if(fileName.startsWith("/")) {
            return fileName;
    	} else if(fileName.startsWith("./")) {
            return fileName;
        } else {
            return directory + "/" + fileName;
        }
    }

    public static File getAbsoluteFileAsFile(String directory, String fileName) {
        return new File(getAbsoluteFile(directory, fileName));
    }

    public static String getDirectoryForFile(String file) {
        File f = new File(file);
        File currentPath = new File(f.getParent());

        return currentPath.getName();
    }

    public static List<String> getFiles(String path) {
        List<String> fileList = new ArrayList<String>();
        
        File root = new File(path);
        
		if(path.contains("*")) {
			IOFileFilter fileFilter = new WildcardFileFilter(root.getName());
			IOFileFilter dirFilter = new WildcardFileFilter("*");
			Collection<File> files = FileUtils.listFiles(root.getParentFile(), fileFilter, dirFilter);
			for(File file:files) {
				fileList.add(file.getAbsolutePath());
			}
			return fileList;
		}
        
        if(root.isFile()) {
            fileList.add(path);
            return fileList;
        }

        if(root.isDirectory()) {
            for (File file : Files.fileTraverser().breadthFirst(root)) {
                if (file.isFile()) {
                    fileList.add(file.getAbsolutePath());
                }
            }
            return fileList;
        }

        throw new RuntimeException("The file or path does not exists: " + path);
    }

    public static InputStream getInputStream(File inputFile) throws IOException {
        return getInputStream(inputFile.getAbsolutePath());
    }

    public static String inputStreamToString(InputStream inputStream) throws IOException {
        return inputStreamToString(inputStream, StandardCharsets.UTF_8.name());
    }
    public static String inputStreamToString(InputStream inputStream, String charset) throws IOException {

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, charset))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }

        return textBuilder.toString();

    }


    public static InputStream getInputStream(String inputFileName) throws IOException {
        InputStream inputStream =  new FileInputStream(inputFileName);
        if (inputFileName.endsWith(".gz")) {
            return new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    public static String getString(String location) throws IOException {
        return FileUtils.readFileToString(new File(location));
    }


    public static OutputStream getOutputStream(File outputFile) throws IOException {
        return getOutputStream(outputFile.getAbsolutePath());

    }
    public static OutputStream getOutputStream(String outputFileName) throws IOException {
        if("stdout".equals(outputFileName)) {
            return System.out;
        } else if("stderr".equals(outputFileName)) {
            return System.err;
        } else if (outputFileName.endsWith(".gz")) {
            OutputStream out = new FileOutputStream(outputFileName);
            return new GZIPOutputStream(out);
        } else {
            return new FileOutputStream(outputFileName);
        }
    }

    public static void appendToOutputStream(OutputStream out, String value ) throws IOException {
        out.write(value.getBytes("UTF-8"));

    }
}
