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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;


/**
 * Methods dedicated to manage resources used by tests
 * 
 * 
 * Author : gandomi
 *
 */
public class GeneralTestUtils {
    public static final String STD_CACHE_DIR = "ac_test_output";
    public static final String STD_LOG_PREFIX = "[integro-testngwrapper] ";
    protected static Logger log = LogManager.getLogger();

    /**
     * Fetches a file given the path
     * 
     * Author : gandomi
     *
     * @param in_fileName
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
     * Creates a cache directory
     *
     * Author : gandomi
     *
     * @param in_directoryName
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
     * @return
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
     * With this method you can fill a fiven file
     *
     * Author : gandomi
     * 
     * @param in_destinationFile
     * @param in_content
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
     * @return
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
     * @return
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
     * This method returns the file path of the given method
     *
     * Author : gandomi
     *
     * @param in_testMethod
     * @return
     *
     */
    public static File fetchFile(Method in_testMethod) {

        return fetchFile(in_testMethod.getDeclaringClass());
    }

    /**
     * This method returns the file path of the given method
     *
     * Author : gandomi
     *
     * @param in_testMethod
     * @return
     *
     */
    public static File fetchFile(Class in_class) {
        final String l_rootPath = (new File("")).getAbsolutePath() + "/src/test/java";

        final String l_filePath = l_rootPath + "/" + in_class.getName().replace('.', '/') + ".java";

        return new File(l_filePath);
    }

    /**
     * This method constructs a full name from the given method.
     * 
     * Author : gandomi
     *
     * @param in_method
     * @return The full qualified name of the method
     *
     */
    public static String fetchFullName(Method in_method) {
        return in_method.getDeclaringClass().getTypeName() + "." + in_method.getName();
    }

    /**
     * This method constructs a full name from the given TestNGResult.
     * 
     * Author : gandomi
     *
     * @param in_testNGResult
     *        The TestNGResult Object
     * @return The full qualified name of the method based on the TestNGResult
     *
     */
    public static String fetchFullName(ITestResult in_testNGResult) {
        StringBuilder sb = new StringBuilder(
                fetchFullName(in_testNGResult.getMethod().getConstructorOrMethod().getMethod()));

        sb.append(fetchParameterValues(in_testNGResult));
        
        return sb.toString();
    }

    /**
     * This method retrieves the Data Providers of a test results.
     *
     * Author : gandomi
     *
     * @param in_testNGResult
     *        The testNG result object
     * @return A String containing the data providers. Empty string if there are
     *         no data providers
     *
     */
    public static String fetchParameterValues(ITestResult in_testNGResult) {
        return fetchParameterValues(in_testNGResult.getParameters());
    }

    /**
     * This method retrieves the Data Providers of a test results.
     *
     * Author : gandomi
     *
     * @param in_parameterValues
     *        An array of Object (Usually toString compatible)
     * @return A String containing the data providers. Empty string if there are
     *         no data providers
     *
     */
    public static String fetchParameterValues(Object[] in_parameterValues) {
        StringBuilder lr_sbArg = new StringBuilder();
        if (in_parameterValues.length > 0) {
            lr_sbArg.append('(');
            List<String> l_parameterList = Arrays.asList(in_parameterValues).stream().map(t -> t.toString())
                    .collect(Collectors.toList());

            lr_sbArg.append(String.join(",", l_parameterList));
            lr_sbArg.append(')');
        }
        return lr_sbArg.toString();
    }

    /**
     * This method returns the file path of the given class
     *
     * Author : vinaysha
     *
     * @param className
     * @return
     *
     */
    public static File fetchClassFile(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        final String l_rootPath = (new File("")).getAbsolutePath() + "/src/test/java";
        final String l_filePath = l_rootPath + "/" + className.replace('.', '/') + ".java";
        return new File(l_filePath);
    }

    /**
     * This method fetches the contents of a file as a string
     *
     * Author : gandomi
     *
     * @param in_resourceFile
     * @return A string representing the
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

    /**
     * This method fetches the list of tests that are to be filtered out
     * 
     * @param in_fileName
     *        The file path containing the list of methods
     * @return A list of method identifiers that will be omitted from execution.
     *         An empty list if the file is empty or does not exist
     * 
     *         Author : gandomi
     *
     */
    public static List<String> fetchFlaggedMethods(String in_fileName) {

        File l_resourceFile = fetchFile(in_fileName);

        if (l_resourceFile == null) {
            log.error("[Integro TestNG Wrapper] The file with the path " + in_fileName
                    + " does not seem to exist");
            return new ArrayList<>();
        }

        return fetchFlaggedMethods(l_resourceFile);
    }

    /**
     * This method fetches the list of tests that are to be filtered out
     * 
     * @param in_resourceFile
     *        The file containing the list of methods
     * @return A list of method identifiers that will be omitted from execution.
     *         An empty list if the file is empty or does not exist
     * 
     *         Author : gandomi
     *
     */

    public static List<String> fetchFlaggedMethods(File in_resourceFile) {
        return fetchFileContentLines(in_resourceFile).stream().filter(ln -> !ln.startsWith("#"))
                .collect(Collectors.toList());
    }
}
