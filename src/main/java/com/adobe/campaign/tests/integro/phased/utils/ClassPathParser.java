/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import org.testng.ITestResult;

public final class ClassPathParser {

    private ClassPathParser() {
        //Utility class. Defeat instantiation
    }

    /**
     * This method constructs a full name from the given method.
     * 
     * Author : gandomi
     *
     * @param in_method
     *        A defined method object
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
        return fetchFullName(in_testNGResult.getMethod().getConstructorOrMethod().getMethod())
            + fetchParameterValues(in_testNGResult);
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
        if (in_parameterValues == null || in_parameterValues.length == 0) {
            return "";
        }
        return "(" +
            Arrays.stream(in_parameterValues)
                .map(Object::toString)
                .collect(Collectors.joining(","))
            + ")";
    }

    /**
     * This method returns the file path of the given class
     *
     * @param className The full class path represented as a String
     * @return The file representing the java class
     *
     */
    public static File fetchClassFile(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        if (className.contains("$")) {
            className = className.substring(0,className.lastIndexOf('$'));
        }

        final String l_rootPath = (new File("")).getAbsolutePath() + ConfigValueHandlerPhased.PHASED_TEST_SOURCE_LOCATION.fetchValue();
        final String l_filePath = l_rootPath + "/" + className.replace('.', '/') + ".java";
        return new File(l_filePath);
    }

    /**
     * Returns the file representing the test class
     * @param in_class A class object
     * @return The file representing the java class
     */
    public static File fetchClassFile(Class in_class) {
        return (in_class.getDeclaringClass() == null) ? fetchClassFile(in_class.getTypeName()) : fetchClassFile(
                in_class.getDeclaringClass().getTypeName());
    }
}
