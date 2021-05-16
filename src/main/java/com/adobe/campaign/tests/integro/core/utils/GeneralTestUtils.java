/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.adobe.campaign.tests.integro.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods dedicated to manage resources used by tests
 * 
 * 
 * Author : gandomi
 *
 */
public class GeneralTestUtils {
    public static final String STD_CACHE_DIR = "phased_output";
    public static final String STD_LOG_PREFIX = "[integro-testngwrapper] ";
    protected static Logger log = LogManager.getLogger();

    /**
     * Fetches a file given the path
     * 
     * Author : gandomi
     *
     * @param in_fileName
     *        A path to a file
     * @return NULL if non existent
     */
    public static File fetchFile(String in_fileName) {
        File lr_file = null;
        URL l_resourceUrl = fetchResourceURL(in_fileName);

        if (l_resourceUrl != null) {
            try {

                lr_file = new File(URLDecoder.decode(l_resourceUrl.getFile(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("Decoding problem");
            }
        } else {
            log.error("File " + in_fileName + " does not exist.");
            return null;
        }
        return lr_file;
    }

    /**
     * Creates a cache directory under te standard output directory
     * {@value GeneralTestUtils.STD_CACHE_DIR }
     *
     * Author : gandomi
     *
     * @param in_directoryName
     *        A simple name under which we will store cache data
     * @return An existing cache directory object
     *
     */
    public static File createCacheDirectory(String in_directoryName) {
        if (in_directoryName == null || in_directoryName.isEmpty()) {
            throw new IllegalArgumentException(
                    "The given argument 'in_directoryName' cannot be null nor empty.");
        }

        File lr_cacheDir = new File(STD_CACHE_DIR, in_directoryName);
        if (!lr_cacheDir.exists()) {
            lr_cacheDir.mkdirs();
        }
        return lr_cacheDir;
    }

    /**
     * Returns a cache directory with the given name. It will create it, if not
     * present.
     *
     * Author : gandomi
     *
     * @param in_directoryName
     *        A cache directory name
     * @return An existing cache directory object
     *
     */
    public static File fetchCacheDirectory(String in_directoryName) {
        return createCacheDirectory(in_directoryName);
    }

    /**
     * Generates an empty file in the given cache directory. If the file exists
     * it is overwritten.
     *
     * Author : gandomi
     *
     * @param in_cacheDir
     *        A cache directory
     * @param in_fileName
     * @return a file with the given name and stored in the given cache
     *         directory
     * @throws IllegalArgumentException
     *         when the given file name or directory are null
     *
     */
    public static File createEmptyCacheFile(File in_cacheDir, String in_fileName) {
        if (in_cacheDir == null) {
            throw new IllegalArgumentException("The given argument 'in_cacheDir' cannot be null nor empty.");
        }

        if (in_fileName == null || in_fileName.isEmpty()) {
            throw new IllegalArgumentException("The given argument 'in_fileName' cannot be null nor empty.");
        }

        File l_jsonFile = new File(in_cacheDir, in_fileName);

        if (l_jsonFile.exists()) {
            log.debug("Deleting cache File");
            l_jsonFile.delete();
        }
        return new File(in_cacheDir, in_fileName);
    }

    /**
     * With this method you can fill a given file with a given content
     *
     * Author : gandomi
     * 
     * @param in_destinationFile
     *        A file that we want to fill
     * @param in_content
     *        The content that we want to fill the file with
     * 
     * @throws IllegalArgumentException
     *         if the file does not exist or is null
     *
     */
    public static void fillFile(File in_destinationFile, final String in_content) {

        if (in_destinationFile == null) {
            throw new IllegalArgumentException("The given argument 'in_destinationFile' cannot be null.");
        }

        try (FileWriter fw = new FileWriter(in_destinationFile)) {

            fw.write(in_content);
        } catch (IOException e) {
            log.error("Error when creating json file.");
        }
    }

    /**
     * @param in_fileName
     *        A file path (both absolute and relative)
     * @return A UTL path to that file
     *
     *         Author : gandomi
     */
    public static URL fetchResourceURL(String in_fileName) {
        URL l_resourceUrl = null;

        //We allow both passing an absolute path, and a reference path
        if (in_fileName.startsWith("/")) {

            File l_file = new File(in_fileName);
            if (l_file.exists()) {
                try {
                    l_resourceUrl = l_file.toURI().toURL();
                } catch (MalformedURLException e) {
                    log.error(e.getMessage());
                }
            }
        } else {
            l_resourceUrl = Thread.currentThread().getContextClassLoader().getResource(in_fileName);
        }
        return l_resourceUrl;
    }

    /**
     * This method fetches the lines of a given file.
     * 
     * Author : gandomi
     *
     * @param in_resourceFile
     *        A file object
     * @return A list where each entry is a line in the file
     *
     */
    public static List<String> fetchFileContentLines(File in_resourceFile) {
        List<String> lr_listOfFlaggedTests = new ArrayList<>();

        //Parse file as one entry per line
        try (Scanner s = new Scanner(new FileInputStream(in_resourceFile))) {

            while (s.hasNextLine()) {
                lr_listOfFlaggedTests.add(s.nextLine());
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }

        return lr_listOfFlaggedTests;
    }

    /**
     * This method fetches the contents of a file as a string
     *
     * Author : gandomi
     *
     * @param in_resourceFile
     *        A file object
     * @return A string representing the contents f that file
     *
     */
    public static String fetchFileContent(File in_resourceFile) {
        if (!in_resourceFile.exists()) {
            throw new IllegalArgumentException(
                    "The given file " + in_resourceFile.getPath() + " does not exist.");
        }

        StringBuilder lr_fileContent = new StringBuilder();

        //Parse file as one entry per line
        try (Scanner s = new Scanner(new FileInputStream(in_resourceFile))) {

            while (s.hasNextLine()) {
                lr_fileContent.append(s.nextLine());
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }

        return lr_fileContent.toString();
    }

}
