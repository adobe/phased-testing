/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass;
import com.adobe.campaign.tests.integro.phased.data.permutational.SimpleProducerConsumerNestedContainer;
import org.mockito.Mockito;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

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

        final Object[] l_parameterValues = new Object[] { "Q", Integer.valueOf("3")};
        assertThat("We should have the correct full name",
                ClassPathParser.fetchParameterValues(l_parameterValues), equalTo("(Q,3)"));

    }

    /**
     * This test tests that we correctly fetch the class file by name of a class
     *
     * Author : vinaysha, baubakg
     *
     * @throws SecurityException
     *
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
                ClassPathParser.fetchClassFile("adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass").exists());



    }
}
