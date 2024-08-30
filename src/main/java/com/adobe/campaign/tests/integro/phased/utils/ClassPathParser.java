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

    /**
     * Lets us know if the given class corresponds to the selected method/class
     * @param in_class The current class to assess
     * @param in_selectedElementName the selected element
     * @return if the element corresponds to the class
     */
    public static boolean elementsCorrespond(Class in_class, String in_selectedElementName) {

        return in_class.getTypeName().endsWith(extractElements(in_selectedElementName)[0]);
    }

    /**
     * Lets us know if the given method corresponds to the selected method/class
     * @param in_method The current class to assess
     * @param in_selectedElementName the selected element
     * @return if the element corresponds to the class
     */
    public static boolean elementsCorrespond(Method in_method, String in_selectedElementName) {
        String[] l_elements = extractElements(in_selectedElementName);

        return elementsCorrespond(in_method.getDeclaringClass(), in_selectedElementName) && in_method.getName().equals(l_elements[1]);
    }

    /**
     * Given a string representing a class or a
     * @param l_selectedMethodName
     * @return
     */
    public static String[] extractElements(String l_selectedMethodName) {
        if (l_selectedMethodName.contains("#")) {
            return l_selectedMethodName.split("#");
        }

        int lioDot = l_selectedMethodName.lastIndexOf('.');

        if (lioDot == -1) {
            throw new IllegalArgumentException("The selected method name is not valid, or is of a bad format. Please include a full reference to a step name");
        }
        return new String []{l_selectedMethodName.substring(0,lioDot),l_selectedMethodName.substring(lioDot+1)};
    }
}
