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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.ITestResult;

public class ClassPathParser {

    /**
     * This method extracts the full class path from a given full path of a
     * method.Example: a.b.c.D.e will return 'a.b.c.D'
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     *            A full path of a method. This consists of class path + method
     *            name
     * @return null if the value cannot be extracted
     */
    public static String extractClass(String in_fullMethodPath) {

        int l_lastDotLocation = in_fullMethodPath.lastIndexOf(".");

        if (l_lastDotLocation == -1)
            return null;

        return in_fullMethodPath.substring(0, l_lastDotLocation);
    }

    /**
     * This method extracts the method name from a given full path of a method
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     *            A full path of a method. This consists of class path + method
     *            name
     * @return null if the value cannot be extracted
     */
    public static String extractMethod(String in_fullMethodPath) {
        int l_lastDotLocation = in_fullMethodPath.lastIndexOf(".");

        if (l_lastDotLocation == -1)
            return null;

        return in_fullMethodPath.substring(l_lastDotLocation + 1);
    }

    /**
     * Extracts the package from the method full path Author : gandomi
     *
     * @param in_method
     * @return NULL if there is no package
     */
    public static String extractPackage(String in_method) {

        return extractClass(extractClass(in_method));
    }

    /**
     * This method checks if a class exists
     * 
     * Author : gandomi
     *
     * @param in_className
     * @return
     */
    public static boolean isClassInProject(String in_className) {
        try {
            Class.forName(in_className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * This method extracts the Class name from a given full method name.
     * Example: a.b.c.D.e will return 'D'
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     * @return
     */
    public static String extractClassName(String in_fullMethodPath) {
        String l_fullClassPath = extractClass(in_fullMethodPath);
        if (l_fullClassPath == null) {
            return l_fullClassPath;
        }
        String[] l_pathItems = l_fullClassPath.split("[.]");

        if (l_pathItems.length == 0)
            return in_fullMethodPath;
        else
            return l_pathItems[(l_pathItems.length - 1)];
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
     * This method returns the file path of the given test class
     *
     * Author : gandomi
     *
     * @param in_testClass
     * @return A file containing the given Test Class
     *
     */
    public static File fetchFile(Class<?> in_testClass) {
        final String l_rootPath = (new File("")).getAbsolutePath() + "/src/test/java";
    
        final String l_filePath = l_rootPath + "/" + in_testClass.getName().replace('.', '/') + ".java";
    
        return new File(l_filePath);
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
    public static File fetchTestClassFile(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        final String l_rootPath = (new File("")).getAbsolutePath() + "/src/test/java";
        final String l_filePath = l_rootPath + "/" + className.replace('.', '/') + ".java";
        return new File(l_filePath);
    }

    /**
     * This method returns the file path of the given method
     *
     * Author : gandomi
     *
     * @param in_testMethod A test method
     * @return The file containing the given method.
     *
     */
    public static File fetchFile(Method in_testMethod) {
    
        return fetchFile(in_testMethod.getDeclaringClass());
    }
}
