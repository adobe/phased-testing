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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertThrows;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;

public class GeneralTestUtilsTests {
    private static final String TEST_CACHE_DIR = "testCache";
    private static final String TEST_CACHE_File = "cachedFile.txt";

    @BeforeClass
    public void cleanUp() {
        File l_cacheDir = new File(PhasedTestManager.STD_CACHE_DIR, TEST_CACHE_DIR);

        File l_cacheFile = new File(l_cacheDir, TEST_CACHE_File);

        if (l_cacheFile.exists()) {
            assertThat("The file should be successfully deleted", l_cacheFile.delete());
        }

        if (l_cacheDir.exists()) {
            assertThat("The directory should be successfully deleted", l_cacheDir.delete());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void cleanAfter() {
        cleanUp();
    }

    @Test
    public void testCacheDirCreation() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);

        assertThat("The directory should exist", l_cacheDir.exists());
    }

    @Test
    public void testCacheDirFetching() {
        File l_cacheDir = GeneralTestUtils.fetchCacheDirectory(TEST_CACHE_DIR);

        assertThat("The directory should exist", l_cacheDir.exists());
    }

    @Test
    public void testCacheDirCreationTwice() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);

        assertThat("The dir should exist", l_cacheDir.exists());

        File l_cacheDir2 = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);

        assertThat("The dir should exist", l_cacheDir2.exists());
    }

    @Test
    public void testArguments_Negative() {
        assertThrows(IllegalArgumentException.class, () -> GeneralTestUtils.createCacheDirectory(null));

        assertThrows(IllegalArgumentException.class, () -> GeneralTestUtils.createCacheDirectory(""));
    }

    @Test
    public void testCacheFileCreation() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = GeneralTestUtils.createEmptyCacheFile(l_cacheDir, TEST_CACHE_File);

        assertThat("The file should exist", l_file, Matchers.not(Matchers.equalTo(null)));
    }

    @Test
    public void testCacheFileCreation_IllegalArguments() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);

        assertThrows(IllegalArgumentException.class,
                () -> GeneralTestUtils.createEmptyCacheFile(l_cacheDir, null));

        assertThrows(IllegalArgumentException.class,
                () -> GeneralTestUtils.createEmptyCacheFile(l_cacheDir, ""));

        assertThrows(IllegalArgumentException.class,
                () -> GeneralTestUtils.createEmptyCacheFile(null, "dsdfsd"));

    }

    @Test
    public void testFetchingFileLines_NegativeNonExistant() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = new File(l_cacheDir, TEST_CACHE_File);

        List<String> l_fileLines = GeneralTestUtils.fetchFileContentLines(l_file);
        assertThat("We should have the correct number of tests", l_fileLines.size(),
                Matchers.is(Matchers.equalTo(0)));

    }

    @Test
    public void testFetchingFileString_NegativeNonExistant() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = new File(l_cacheDir, TEST_CACHE_File);

        assertThrows(IllegalArgumentException.class, () -> GeneralTestUtils.fetchFileContent(l_file));

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
    public void testStorageMethodWithMultiArgs() throws NoSuchMethodException, SecurityException {

        final Object[] l_parameterValues = new Object[] { "Q", "Z" };

        assertThat("We should have the correct full name",
                ClassPathParser.fetchParameterValues(l_parameterValues), equalTo("(Q,Z)"));

    }

    @Test
    public void testStorageMethodWithMultiArgsNotJustStrings()
            throws NoSuchMethodException, SecurityException {

        final Object[] l_parameterValues = new Object[] { "Q", new Integer("3") };
        assertThat("We should have the correct full name",
                ClassPathParser.fetchParameterValues(l_parameterValues), equalTo("(Q,3)"));

    }
}
