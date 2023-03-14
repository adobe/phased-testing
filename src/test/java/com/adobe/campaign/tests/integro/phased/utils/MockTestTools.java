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

import org.mockito.Mockito;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;

/**
 * Used for generating mocks of TestNG objects
 */
public class MockTestTools {

    /**
     * Generates a Mock Object for testing purposes
     *
     * @param in_method    a method object
     * @param in_arguments the arguments
     * @return A mock object for a TestNG ITestResult by efault it will be Success
     */
    public static ITestResult generateTestResultMock(Method in_method, Object[] in_arguments) {

        return generateTestResultMock(in_method, in_arguments, ITestResult.SUCCESS);
    }

    /**
     * Generates a Mock Object for testing purposes
     *
     * @param in_method    a method object
     * @param in_arguments the arguments
     * @param in_result    the result of the object
     * @return A mock object for a TestNG ITestResult
     */
    public static ITestResult generateTestResultMock(Method in_method, Object[] in_arguments, int in_result) {

        return generateTestResultMock(in_method, in_arguments, in_result, 1l, 2l);
    }

    /**
     * Generates a Mock Object for testing purposes
     *
     * @param in_method      a method object
     * @param in_arguments   the arguments
     * @param in_result      the result of the object
     * @param in_startMillis The start time of the test
     * @param in_endMillis   The end time of the test
     * @return A mock object for a TestNG ITestResult
     */
    public static ITestResult generateTestResultMock(Method in_method, Object[] in_arguments, int in_result,
            long in_startMillis, long in_endMillis) {
        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getName()).thenReturn(in_method.getName());
        Mockito.when(l_itr.getParameters()).thenReturn(in_arguments);
        Mockito.when(l_itr.getStatus()).thenReturn(in_result);
        Mockito.when(l_itr.getStartMillis()).thenReturn(in_startMillis);
        Mockito.when(l_itr.getEndMillis()).thenReturn(in_endMillis);

        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(in_method);
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                ClassPathParser.fetchFullName(in_method));
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(in_method.getDeclaringClass());
        return l_itr;
    }
}
