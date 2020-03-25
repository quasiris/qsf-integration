package com.quasiris.qsf.util;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.*;

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

    public static void unzip(String zipFile) throws IOException {
        int BUFFER = 2048;
        File file = new File(zipFile);

        ZipFile zip = new ZipFile(file);
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(file.getParentFile(), currentEntry);
            //destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                unzip(destFile.getAbsolutePath());
            }
        }
    }

    public static void zip(String dir, String rootDirName, String zipFile) throws IOException {
        File directory = new File(dir);
        if(rootDirName == null) {
            rootDirName = directory.getName();
        }
        List<String> fileList = getAllFiles(dir).stream().
                map(p -> p.toFile().getAbsolutePath()).
                collect(Collectors.toList());


        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : fileList) {

                // Creates a zip entry.
                String name = rootDirName + "/" + filePath.substring(
                        directory.getAbsolutePath().length() + 1,
                        filePath.length());

                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);

                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();

            }
        }
    }

    public static void zip(String dir, String zipFile) throws IOException {
        zip(dir, null, zipFile);
    }


    public static List<Path> getAllFiles(String directory) throws IOException {
        try (Stream<Path> paths = java.nio.file.Files.walk(Paths.get(directory))) {
            return paths.filter(java.nio.file.Files::isRegularFile).collect(Collectors.toList());
        }
    }

    public static String ensureEndingSlash(String value ) {
        if(value == null) {
            return null;
        }
        if(value.endsWith("/")) {
            return value;
        }
        return value + "/";
    }


    public static boolean createDirectoryIfNotExists(File directory) {
        if (!directory.exists()){
            return directory.mkdirs();
        }
        return false;
    }

    public static boolean createDirectoryIfNotExists(String directory) {
        return createDirectoryIfNotExists(new File(directory));
    }
}
