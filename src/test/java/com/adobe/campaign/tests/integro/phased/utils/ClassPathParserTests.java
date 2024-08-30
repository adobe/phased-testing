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

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass;
import com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty;
import com.adobe.campaign.tests.integro.phased.data.permutational.SimpleProducerConsumerNestedContainer;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClassPathParserTests {

    @BeforeMethod
    @AfterMethod
    private void reset() {
        ConfigValueHandlerPhased.resetAllValues();
    }

    @Test
    public void testStorageMethodWithoutArgs() throws NoSuchMethodException, SecurityException {

        final Method l_myTestNoArgs = PhasedTestManagerTests.class.getMethod("testStorageMethod");

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] {});
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestNoArgs);

        assertThat("We should have the correct full name", ClassPathParser.fetchFullName(l_itr),
                equalTo("com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testStorageMethod"));

    }

    @Test
    public void testStorageMethodWithArgs() throws NoSuchMethodException, SecurityException {

        final Method l_myTestNoArgs = PhasedSeries_H_SingleClass.class.getMethod("step2", String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "A" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestNoArgs);

        assertThat("We should have the correct full name", ClassPathParser.fetchFullName(l_itr),
                equalTo("com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass.step2(A)"));

    }

    @Test
    public void testStorageMethodWithMultiArgs() throws SecurityException {

        final Object[] l_parameterValues = new Object[] { "Q", "Z" };

        assertThat("We should have the correct full name",
                ClassPathParser.fetchParameterValues(l_parameterValues), equalTo("(Q,Z)"));

    }

    @Test
    public void testStorageMethodWithMultiArgsNotJustStrings() throws SecurityException {

        final Object[] l_parameterValues = new Object[] { "Q", Integer.valueOf("3") };
        assertThat("We should have the correct full name",
                ClassPathParser.fetchParameterValues(l_parameterValues), equalTo("(Q,3)"));

    }

    /**
     * This test tests that we correctly fetch the class file by name of a class
     * <p>
     * Author : vinaysha, baubakg
     *
     * @throws SecurityException
     */
    @Test
    public void testFetchClassFile() throws SecurityException {

        //Fetch File for Class
        assertThat("We should have correctly found the file that is not null",
                ClassPathParser.fetchClassFile(PhasedSeries_H_SingleClass.class.getTypeName()),
                notNullValue());

        assertThat("We should have correctly found the file that exists",
                ClassPathParser.fetchClassFile(PhasedSeries_H_SingleClass.class.getTypeName()).exists());

        assertThat("We should get null if file name is empty",
                ClassPathParser.fetchClassFile(""), nullValue());

        assertThat("We should get null if file name is null",
                ClassPathParser.fetchClassFile((String) null), nullValue());

        //Fetch File for Class
        assertThat("We should have correctly found the file that is not null",
                ClassPathParser.fetchClassFile(PhasedSeries_H_SingleClass.class),
                notNullValue());

    }

    @Test
    public void testFetchNestedClassFile() throws SecurityException {

        //Fetch File for Class
        Class<SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested> l_testClass = SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested.class;
        assertThat("We should have correctly found the file that is not null",
                ClassPathParser.fetchClassFile(l_testClass),
                notNullValue());

        assertThat("We should have correctly found the file that exists",
                ClassPathParser.fetchClassFile(l_testClass).exists());

        assertThat("We should get null if file name is empty",
                ClassPathParser.fetchClassFile(""), nullValue());

        assertThat("We should get null if file name is null",
                ClassPathParser.fetchClassFile((String) null), nullValue());

    }

    @Test
    public void testFetchNestedClassFilePassingString() throws SecurityException {

        //Fetch File for Class
        Class<SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested> l_testClass = SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested.class;
        assertThat("We should have correctly found the file that is not null",
                ClassPathParser.fetchClassFile(l_testClass.getTypeName()),
                notNullValue());

        assertThat("We should have correctly found the file that exists",
                ClassPathParser.fetchClassFile(l_testClass.getTypeName()).exists());

        assertThat("We should get null if file name is empty",
                ClassPathParser.fetchClassFile(""), nullValue());

        assertThat("We should get null if file name is null",
                ClassPathParser.fetchClassFile((String) null), nullValue());

    }

    @Test
    public void testFetchClassFileConfigured() throws SecurityException {
        ConfigValueHandlerPhased.PHASED_TEST_SOURCE_LOCATION.activate("/src/test/java/com");

        //Fetch File for Class
        assertThat("We should have correctly found the file that is not null",
                ClassPathParser.fetchClassFile("adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass"),
                notNullValue());

        assertThat("We should have correctly found the file that exists",
                ClassPathParser.fetchClassFile("adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass")
                        .exists());
    }

    @Test
    public void testElementsCorrespondSimple() throws NoSuchMethodException {
        //Case 1: The method corresponds
        String l_selectedMethodName = "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty.step1";

        Class l_class = TestSINGLEWithEvent_eventAsExecProperty.class;
        assertThat("The class should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_class, l_selectedMethodName));

        final Method l_method = TestSINGLEWithEvent_eventAsExecProperty.class.getMethod("step1", String.class);
        assertThat("The method should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_method, l_selectedMethodName));

        //Case 2 With #
        String l_selectedMethodName2 = "TestSINGLEWithEvent_eventAsExecProperty#step1";

        assertThat("The class should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_class, l_selectedMethodName2));

        assertThat("The method should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_method, l_selectedMethodName2));

        //Case 3 Full with #
        String l_selectedMethodName3 = "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty#step1";

        assertThat("The class should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_class, l_selectedMethodName3));

        assertThat("The method should correspond to the selected method",
                ClassPathParser.elementsCorrespond(l_method, l_selectedMethodName3));
    }

    @Test
    public void testExtractClassFromElementSelection() throws NoSuchMethodException, ClassNotFoundException {
        //Case 1: The method corresponds
        String l_selectedMethodName = "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty.step1";

        assertThat("The class should correspond to the selected method",
                ClassPathParser.extractElements("TestSINGLEWithEvent_eventAsExecProperty#step1"),
                Matchers.arrayContaining(
                        "TestSINGLEWithEvent_eventAsExecProperty",
                        "step1"));

        assertThat("The class should correspond to the selected method",
                ClassPathParser.extractElements(l_selectedMethodName),
                Matchers.arrayContaining(
                        "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty",
                        "step1"));

        assertThat("The class should correspond to the selected method", ClassPathParser.extractElements(
                        "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsExecProperty"),
                Matchers.arrayContaining("com.adobe.campaign.tests.integro.phased.data.events",
                        "TestSINGLEWithEvent_eventAsExecProperty"));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> ClassPathParser.extractElements("TestSINGLEWithEvent_eventAsExecProperty")
        );

        // final Method l_method = TestSINGLEWithEvent_eventAsExecProperty.class.getMethod("step1", String.class);
        // assertThat("The class should correspond to the selected method", ClassPathParser.elementsCorrespond(l_method, l_selectedMethodName));

    }

}
