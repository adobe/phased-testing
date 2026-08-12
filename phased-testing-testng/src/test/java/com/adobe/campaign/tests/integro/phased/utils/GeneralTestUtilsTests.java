/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestException;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

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
    public void testDeleteFileCreation() {
        
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = GeneralTestUtils.createEmptyCacheFile(l_cacheDir, TEST_CACHE_File);
        Properties props = new Properties();
        props.put("A", "B");
        
        try (FileWriter fw = new FileWriter(l_file)) {

            props.store(fw, null);

        } catch (IOException e) {
            throw new PhasedTestException("Error when creating file " + l_file + ".", e);
        }
        
        assertThat("The file should exist",l_file.exists());
        
        GeneralTestUtils.deleteFile(l_file);
        
        assertThat("The file should no longer exist",!l_file.exists());
    }
    
    @Test
    public void testDeleteFileCreation_Negative() {
        File l_mockedFile = Mockito.mock(File.class);
        Mockito.when(l_mockedFile.exists()).thenReturn(true);
        Mockito.when(l_mockedFile.delete()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> GeneralTestUtils.deleteFile(l_mockedFile));
    }

    @Test
    public void testFetchingFileLines() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = new File(l_cacheDir, TEST_CACHE_File);
        
        
        //Create test data
        Properties l_testProperties = new Properties();
        l_testProperties.put("A", "B");
        l_testProperties.put("C", "D");
           
        try (FileWriter fw = new FileWriter(l_file)) {

            l_testProperties.store(fw, null);            

        } catch (IOException e) {
            throw new PhasedTestException("Error when creating file " + l_file + ".", e);
        }

        List<String> l_fileLines = GeneralTestUtils.fetchFileContentLines(l_file);
        assertThat("We should have the correct number of tests", l_fileLines.size(),
                Matchers.is(Matchers.equalTo(3)));

    }

    @Test
    public void testFetchingFileLines_NonCommented() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = new File(l_cacheDir, TEST_CACHE_File);
        
        
        //Create test data
        Properties l_testProperties = new Properties();
        l_testProperties.put("A", "B");
        l_testProperties.put("C", "D");
           
        try (FileWriter fw = new FileWriter(l_file)) {

            l_testProperties.store(fw, null);            

        } catch (IOException e) {
            throw new PhasedTestException("Error when creating file " + l_file + ".", e);
        }

        assertThat("We should have the correct number of lines", GeneralTestUtils.fetchFileContentLines(l_file).size(),
                Matchers.is(Matchers.equalTo(3)));
        assertThat("We should have the correct number of tests", GeneralTestUtils.fetchFileContentDataLines(l_file).size(),
                Matchers.is(Matchers.equalTo(2)));
    }
    
    @Test
    public void testFetchingFileDataLines_NegativeNonExistant() {
        File l_cacheDir = GeneralTestUtils.createCacheDirectory(TEST_CACHE_DIR);
        File l_file = new File(l_cacheDir, TEST_CACHE_File);

        List<String> l_fileLines = GeneralTestUtils.fetchFileContentDataLines(l_file);
        assertThat("We should have the correct number of tests", l_fileLines.size(),
                Matchers.is(Matchers.equalTo(0)));

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

}
