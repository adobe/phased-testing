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
package com.adobe.campaign.tests.integro.phased;

import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;
import com.adobe.campaign.tests.integro.phased.data.NormalSeries_A;
import com.adobe.campaign.tests.integro.phased.data.PhasedDataBrokerTestImplementation;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_A;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_B_NoInActive;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_F_Shuffle;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClass;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_K_ShuffledClass_noproviders;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_DPDefinitionInexistant;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_PROVIDER;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDP;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDPPrivate;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDPSimple;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDPSimplePrivate;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

public class PhasedTestManagerTests {
    @BeforeClass
    public void cleanCache() {
        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        PhasedTestManager.clearDataBroker();

    }

    @AfterMethod
    public void clearAllData() {
        cleanCache();
    }

    @Test
    public void testStorage() {
        assertThat("We should have correctly constructed the key ", PhasedTestManager.produceInStep("Hello"),
                equalTo("com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testStorage"));

        assertThat("We should have successfully stored the given value", PhasedTestManager.phasedCache
                .containsKey("com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testStorage"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.consumeFromStep("testStorage"), equalTo("Hello"));
    }

    @Test
    public void testStorageMethod() throws NoSuchMethodException, SecurityException {
        final Method l_myTest = PhasedTestManagerTests.class.getMethod("testStorageMethod");

        String l_producedKey = PhasedTestManager.storeTestData(l_myTest, "DP_A", "myValue");
        assertThat("We should have stored a key in the cache",
                PhasedTestManager.getPhasedCache().containsKey(l_producedKey));

        assertThat("We should have stored a value for the key in the cache",
                PhasedTestManager.getPhasedCache().get(l_producedKey), equalTo("myValue"));

    }

    @Test
    public void testProduceOnBehalphOf() {

        String l_myKeyPrefix = this.getClass().getTypeName() + PhasedTestManager.STD_KEY_CLASS_SEPARATOR;

        assertThat("We should have correctly constructed the key ",
                PhasedTestManager.produceWithKey("A", "Hello"), equalTo(l_myKeyPrefix + "A"));

        assertThat("We should have successfully stored the given value",
                PhasedTestManager.phasedCache.containsKey(l_myKeyPrefix + "A"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.phasedCache.get(l_myKeyPrefix + "A"), equalTo("Hello"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.consumeWithKey("A"), equalTo("Hello"));
    }

    @Test
    public void testProduceOnBehalphOf_withContext() {

        final String l_dataProducerValue = "plop";
        PhasedTestManager.phaseContext.put(
                this.getClass().getTypeName() + ".testProduceOnBehalphOf_withContext", l_dataProducerValue);

        String l_myKeyPrefix = this.getClass().getTypeName() + "(" + l_dataProducerValue + ")"
                + PhasedTestManager.STD_KEY_CLASS_SEPARATOR;

        assertThat("We should have correctly constructed the key ",
                PhasedTestManager.produceWithKey("A", "Hello"), equalTo(l_myKeyPrefix + "A"));

        assertThat("We should have successfully stored the given value",
                PhasedTestManager.phasedCache.containsKey(l_myKeyPrefix + "A"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.phasedCache.get(l_myKeyPrefix + "A"), equalTo("Hello"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.consumeWithKey("A"), equalTo("Hello"));
    }

    @Test
    public void testProduce_withContext() {

        final String l_dataProducerValue = "plop";
        PhasedTestManager.phaseContext.put(this.getClass().getTypeName() + ".testProduce_withContext",
                l_dataProducerValue);

        String l_myKeyPrefix = this.getClass().getTypeName() + "(" + l_dataProducerValue + ")"
                + PhasedTestManager.STD_KEY_CLASS_SEPARATOR;

        assertThat("We should have correctly constructed the key ", PhasedTestManager.produce("A", "Hello"),
                equalTo(l_myKeyPrefix + "A"));

        assertThat("We should have successfully stored the given value",
                PhasedTestManager.phasedCache.containsKey(l_myKeyPrefix + "A"));

        assertThat("We should have successfully fetched the correct value",
                PhasedTestManager.phasedCache.get(l_myKeyPrefix + "A"), equalTo("Hello"));

        assertThat("We should have successfully fetched the correct value", PhasedTestManager.consume("A"),
                equalTo("Hello"));
    }

    @Test
    public void testProduceOnBehalphOf_RepetitiveProduce_Negative() {
        PhasedTestManager.produceWithKey("A", "Bye");
        assertThrows(PhasedTestException.class, () -> PhasedTestManager.produceWithKey("A", "Hello"));
    }

    @Test
    public void testProduce_RepetitiveProduce_Negative() {
        PhasedTestManager.produce("A", "Bye");
        assertThrows(PhasedTestException.class, () -> PhasedTestManager.produce("A", "Hello"));
    }

    @Test
    public void testconsumeByKey_NonExistingKey_Negative() {
        assertThrows(PhasedTestException.class, () -> PhasedTestManager.consumeWithKey("A"));
    }

    @Test
    public void testconsume_NonExistingKey_Negative() {
        assertThrows(PhasedTestException.class, () -> PhasedTestManager.consume("A"));
    }

    @Test
    public void testReset() {
        PhasedTestManager.produceInStep("Hello");

        assertThat(PhasedTestManager.phasedCache.size(), Matchers.greaterThan(0));
        PhasedTestManager.clearCache();
        assertThat(PhasedTestManager.phasedCache.size(), equalTo(0));
    }

    @Test
    public void duplicateExceptionWhenStoring() {
        PhasedTestManager.produceInStep("Hello");

        try {
            PhasedTestManager.produceInStep("Bye");
        } catch (Exception e) {
            assertThat("The exception should be an instance of PhasedTestStorageException",
                    e instanceof PhasedTestException);
            return;
        }

        assertThat("We should not reach this line of code", false);
    }

    @Test
    public void missingDataForPhasedTests() {
        assertThrows(PhasedTestException.class, () -> PhasedTestManager.consumeFromStep("something"));
    }

    @Test
    public void exportingData() throws FileNotFoundException, IOException {
        PhasedTestManager.produceInStep("Hello");

        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        assertThat("The file should exist", l_phasedTestFile.exists());
        assertThat("The file should exist", l_phasedTestFile.length(), Matchers.greaterThan(0l));
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(l_phasedTestFile)) {

            // load a properties file
            prop.load(input);
        }

        assertThat("We should find our property", prop.size(), equalTo(1));
        assertThat("We should find our property", prop
                .containsKey("com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.exportingData"));
        assertThat("We should find our property",
                prop.getProperty(
                        "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.exportingData"),
                equalTo("Hello"));

    }

    @Test
    public void exportingDataTwice() throws FileNotFoundException, IOException {

        final String thisMethodFullName = "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.exportingDataTwice";

        PhasedTestManager.produceInStep("Hello");

        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        assertThat("The file should exist", l_phasedTestFile.exists());
        assertThat("The file should exist", l_phasedTestFile.length(), Matchers.greaterThan(0l));
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(l_phasedTestFile)) {

            // load a properties file
            prop.load(input);
        }

        assertThat("We should find our property", prop.size(), equalTo(1));
        assertThat("We should find our property", prop.containsKey(thisMethodFullName));
        assertThat("We should find our property", prop.getProperty(thisMethodFullName), equalTo("Hello"));

        PhasedTestManager.produce("A", "Bye");

        File l_phasedTestFile2 = PhasedTestManager.exportPhaseData();

        assertThat("The file should exist", l_phasedTestFile2.exists());
        assertThat("The file should exist", l_phasedTestFile2.length(), Matchers.greaterThan(0l));
        Properties prop2 = new Properties();

        try (InputStream input = new FileInputStream(l_phasedTestFile2)) {

            // load a properties file
            prop2.load(input);
        }

        assertThat("We should find our property", prop2.size(), equalTo(2));
        final String l_key = this.getClass().getTypeName() + PhasedTestManager.STD_KEY_CLASS_SEPARATOR + "A";
        assertThat("We should find our property", prop2.containsKey(l_key));

        assertThat("We should find our property", prop2.getProperty(l_key), equalTo("Bye"));
    }

    /**
     * Testing that when the property
     * ({@value PhasedTestManager#PROP_PHASED_DATA_PATH} is set, that path is
     * used.
     *
     * Author : gandomi
     *
     * @throws FileNotFoundException
     * @throws IOException
     *
     */
    @Test
    public void exportingData_UsingSystemValues() throws FileNotFoundException, IOException {
        PhasedTestManager.produceInStep("Hello");

        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");
        assertThat("The new file should be empty", !l_newFile.exists());

        System.setProperty(PhasedTestManager.PROP_PHASED_DATA_PATH, l_newFile.getPath());

        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        assertThat("The file should exist", l_phasedTestFile.exists());
        assertThat("The file should exist", l_phasedTestFile.length(), Matchers.greaterThan(0l));
        assertThat("The exported file should be the same is the one we sent", l_phasedTestFile.getPath(),
                Matchers.equalTo(l_newFile.getPath()));

        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(l_phasedTestFile)) {

            // load a properties file
            prop.load(input);
        }

        assertThat("We should find our property", prop.size(), equalTo(1));
        assertThat("We should find our property", prop.containsKey(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.exportingData_UsingSystemValues"));
        assertThat("We should find our property", prop.getProperty(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.exportingData_UsingSystemValues"),
                equalTo("Hello"));

    }

    /**
     * Testing that when the property
     * ({@value PhasedTestManager#PROP_PHASED_DATA_PATH} is set, that path is
     * used.
     *
     * Author : gandomi
     *
     * @throws FileNotFoundException
     * @throws IOException
     *
     */
    @Test
    public void testingTheFetchExportFile() throws FileNotFoundException, IOException {

        File l_newFile = GeneralTestUtils.createEmptyCacheFile(
                GeneralTestUtils.createCacheDirectory("testingTheFetchExportFile"), "newFile.properties");
        assertThat("The new file should be empty", !l_newFile.exists());

        System.setProperty(PhasedTestManager.PROP_PHASED_DATA_PATH, l_newFile.getPath());

        assertThat(PhasedTestManager.fetchExportFile().getAbsolutePath(),
                equalTo(l_newFile.getAbsolutePath()));

    }

    /**
     * Testing that when the property ({@value PhasedTestManager#PRO} is set,
     * that path is used.
     *
     * Author : gandomi
     *
     * @throws FileNotFoundException
     * @throws IOException
     *
     */
    @Test
    public void testingTheFetchExportFileNoPropertySet() throws FileNotFoundException, IOException {
        File l_parentPath = GeneralTestUtils.fetchCacheDirectory(PhasedTestManager.STD_STORE_DIR);

        assertThat("The directories should be the same", PhasedTestManager.fetchExportFile().getParent(),
                equalTo(l_parentPath.getPath()));

        assertThat("The files should b the same", PhasedTestManager.fetchExportFile().getName(),
                equalTo(PhasedTestManager.STD_STORE_FILE));

    }

    @Test
    public void testExportCache_NegativeIOException() {

        File l_phasedTestFile = new File("skjdfhqskdj", "kjhkjhkjh");
        assertThat("The file should not exist", !l_phasedTestFile.exists());

        assertThrows(PhasedTestException.class, () -> PhasedTestManager.exportCache(l_phasedTestFile));
    }

    @Test
    public void testCleanDataBroker() {
        PhasedTestManager.setDataBroker(new PhasedDataBrokerTestImplementation());

        assertThat(PhasedTestManager.getDataBroker(), Matchers.notNullValue());

        PhasedTestManager.clearDataBroker();

        assertThat(PhasedTestManager.getDataBroker(), Matchers.nullValue());

    }

    @Test
    public void exportingData_dataBroker() throws FileNotFoundException, IOException {
        PhasedTestManager.produceInStep("Hello");

        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        PhasedDataBroker l_myDataBroker = new PhasedDataBrokerTestImplementation();
        l_myDataBroker.store(l_phasedTestFile);

        File l_fetchedTestFile = l_myDataBroker.fetch(l_phasedTestFile.getName());
        assertThat("The file should have been successfully fetched", l_fetchedTestFile,
                Matchers.notNullValue());

        assertThat("The fetched file should exist", l_fetchedTestFile.exists());

        assertThat("The two tests should be the same.", GeneralTestUtils.fetchFileContent(l_fetchedTestFile),
                equalTo(GeneralTestUtils.fetchFileContent(l_fetchedTestFile)));
    }

    /**
     * In this test when we export the file should be stored by the broker, and
     * fetched by the broker
     *
     * Author : gandomi
     *
     * @throws FileNotFoundException
     * @throws IOException
     *
     */
    @Test
    public void exportingData_dataBrokerPhasedTestManager() throws FileNotFoundException, IOException {

        assertThat("At first the data broker should be null", PhasedTestManager.getDataBroker(),
                Matchers.nullValue());

        final PhasedDataBrokerTestImplementation l_myDataBroker = new PhasedDataBrokerTestImplementation();
        l_myDataBroker.deleteData(PhasedTestManager.STD_STORE_FILE);
        PhasedTestManager.setDataBroker(l_myDataBroker);

        //Generate data
        final String l_phaseContent = "noHello";
        PhasedTestManager.produceInStep(l_phaseContent);

        //Store data
        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        //Fetch data
        File l_fetchedTestFile = PhasedTestManager.getDataBroker().fetch(l_phasedTestFile.getName());
        assertThat("The fetched file should exist", l_fetchedTestFile.exists());

        assertThat("The two tests should be the same.", GeneralTestUtils.fetchFileContent(l_fetchedTestFile),
                equalTo(GeneralTestUtils.fetchFileContent(l_fetchedTestFile)));

        //delete file and then import it
        l_phasedTestFile.delete();
        PhasedTestManager.clearCache();

        //import cache
        PhasedTestManager.importPhaseData();

        assertThat("We should have successfully fetched the contents of the stored file.",
                PhasedTestManager.consumeFromStep("exportingData_dataBrokerPhasedTestManager"),
                Matchers.equalTo(l_phaseContent));
    }

    /**
     * Tsting the initializing of the databroker
     *
     * Author : gandomi
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws PhasedTestConfigurationException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     *
     */
    @Test
    public void dataBrokerPhasedTestManagerInitializing() throws PhasedTestConfigurationException {

        assertThat("At first the data broker should be null", PhasedTestManager.getDataBroker(),
                Matchers.nullValue());

        final PhasedDataBrokerTestImplementation l_myDataBroker = new PhasedDataBrokerTestImplementation();
        l_myDataBroker.deleteData(PhasedTestManager.STD_STORE_FILE);

        PhasedTestManager.setDataBroker(PhasedDataBrokerTestImplementation.class.getTypeName());

        //Generate data
        final String l_phaseContent = "noHello";
        PhasedTestManager.produceInStep(l_phaseContent);

        //Store data
        File l_phasedTestFile = PhasedTestManager.exportPhaseData();

        //Fetch data
        File l_fetchedTestFile = PhasedTestManager.getDataBroker().fetch(l_phasedTestFile.getName());
        assertThat("The fetched file should exist", l_fetchedTestFile.exists());

        assertThat("The two tests should be the same.", GeneralTestUtils.fetchFileContent(l_fetchedTestFile),
                equalTo(GeneralTestUtils.fetchFileContent(l_fetchedTestFile)));

        //delete file and then import it
        l_phasedTestFile.delete();
        PhasedTestManager.clearCache();

        //import cache
        PhasedTestManager.importPhaseData();

        assertThat("We should have successfully fetched the contents of the stored file.",
                PhasedTestManager.consumeFromStep("dataBrokerPhasedTestManagerInitializing"),
                Matchers.equalTo(l_phaseContent));
    }

    @Test
    public void dataBrokerPhasedTestManagerInitializing_negativeNotInstanceOfDataBroker()
            throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, PhasedTestConfigurationException {

        assertThat("At first the data broker should be null", PhasedTestManager.getDataBroker(),
                Matchers.nullValue());

        final PhasedDataBrokerTestImplementation l_myDataBroker = new PhasedDataBrokerTestImplementation();
        l_myDataBroker.deleteData(PhasedTestManager.STD_STORE_FILE);

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedTestManager.setDataBroker(NormalSeries_A.class.getTypeName()));
    }

    @Test
    public void dataBrokerPhasedTestManagerInitializing_negativeNotAClass()
            throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, PhasedTestConfigurationException {

        assertThat("At first the data broker should be null", PhasedTestManager.getDataBroker(),
                Matchers.nullValue());

        final PhasedDataBrokerTestImplementation l_myDataBroker = new PhasedDataBrokerTestImplementation();
        l_myDataBroker.deleteData(PhasedTestManager.STD_STORE_FILE);

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedTestManager.setDataBroker("a.b.c.d.F"));
    }

    @Test
    public void importingData() throws IOException {
        PhasedTestManager.produceInStep("Hello");
        File l_phasedTestFile = PhasedTestManager.exportPhaseData();
        PhasedTestManager.clearCache();

        Properties l_phasedTestdata = PhasedTestManager.importCache(l_phasedTestFile);

        assertThat("We should find our property", l_phasedTestdata, Matchers.notNullValue());
        assertThat("We should find our property", l_phasedTestdata.size(), equalTo(1));
        assertThat("We should find our property", l_phasedTestdata
                .containsKey("com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingData"));
        assertThat("We should find our property",
                l_phasedTestdata.getProperty(
                        "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingData"),
                equalTo("Hello"));
    }

    @Test
    public void importingData_NegativeBadFile() throws IOException {
        File l_phasedTestFile = new File("skjdfhqskdj", "kjhkjhkjh");
        assertThat("The file should not exist", !l_phasedTestFile.exists());
        PhasedTestManager.clearCache();

        assertThrows(PhasedTestException.class, () -> PhasedTestManager.importCache(l_phasedTestFile));
    }

    @Test
    public void importingDataSTD() throws FileNotFoundException, IOException {
        PhasedTestManager.produceInStep("Hello");
        PhasedTestManager.exportPhaseData();
        PhasedTestManager.clearCache();

        Properties l_phasedTestdata = PhasedTestManager.importPhaseData();

        assertThat("We should find our property", l_phasedTestdata, Matchers.notNullValue());
        assertThat("We should find our property", l_phasedTestdata.size(), equalTo(1));
        assertThat("We should find our property", l_phasedTestdata.containsKey(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingDataSTD"));
        assertThat("We should find our property",
                l_phasedTestdata.getProperty(
                        "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingDataSTD"),
                equalTo("Hello"));
    }

    @Test
    public void importingDataSTD_UsingSystemValues() throws FileNotFoundException, IOException {
        PhasedTestManager.produceInStep("Hello");
        File l_phasedTestData = PhasedTestManager.exportPhaseData();

        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");
        assertThat("moving should succeed", l_phasedTestData.renameTo(l_newFile));
        assertThat("The new file should now exist", l_newFile.exists());
        assertThat("The old file should now be empty", !l_phasedTestData.exists());

        PhasedTestManager.clearCache();
        System.setProperty(PhasedTestManager.PROP_PHASED_DATA_PATH, l_newFile.getPath());
        Properties l_phasedTestdata = PhasedTestManager.importPhaseData();

        assertThat("We should find our property", l_phasedTestdata, Matchers.notNullValue());
        assertThat("We should find our property", l_phasedTestdata.size(), equalTo(1));
        assertThat("We should find our property", l_phasedTestdata.containsKey(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingDataSTD_UsingSystemValues"));
        assertThat("We should find our property", l_phasedTestdata.getProperty(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.importingDataSTD_UsingSystemValues"),
                equalTo("Hello"));
    }

    @Test
    public void testCreateDataProviderData() {
        Phases.PRODUCER.activate();

        Map<Class, List<String>> l_myMap = new HashMap<Class, List<String>>();

        l_myMap.put(PhasedSeries_F_Shuffle.class, Arrays.asList("a", "b", "c"));

        Map<String, MethodMapping> l_result = PhasedTestManager.generatePhasedProviders(l_myMap);

        assertThat("we need to have the expected key", l_result.containsKey("a"));
        assertThat("The first method should have three entries", l_result.get("a").nrOfProviders, equalTo(3));

        assertThat("The first method should have two entries", l_result.get("b").nrOfProviders, equalTo(2));

        assertThat("The first method should have one entry", l_result.get("c").nrOfProviders, equalTo(1));

        assertThat("We should have the same amount of total sizes", l_result.get("a").totalClassMethods,
                equalTo(l_result.get("b").totalClassMethods));
        assertThat("We should have the same amount of total sizes", l_result.get("a").totalClassMethods,
                equalTo(l_result.get("c").totalClassMethods));

        Object[][] l_providerA = PhasedTestManager.fetchProvidersShuffled("a");

        assertThat(l_providerA[0].length, equalTo(1));

        assertThat(l_providerA[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerA[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));
        assertThat(l_providerA[2][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2"));

        Object[][] l_providerB = PhasedTestManager.fetchProvidersShuffled("b");

        assertThat(l_providerB[0].length, equalTo(1));

        assertThat(l_providerB[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerB[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));

        Object[][] l_providerC = PhasedTestManager.fetchProvidersShuffled("c");

        assertThat(l_providerC[0].length, equalTo(1));

        assertThat(l_providerC[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));

    }

    @Test
    public void testCreateDataProviderData_withOwnDataProvider() {
        Phases.PRODUCER.activate();

        Map<Class, List<String>> l_myMap = new HashMap<Class, List<String>>();

        l_myMap.put(PhasedSeries_L_ShuffledDP.class, Arrays.asList("a", "b", "c"));

        Map<String, MethodMapping> l_result = PhasedTestManager.generatePhasedProviders(l_myMap);

        assertThat("we need to have the expected key", l_result.containsKey("a"));
        assertThat("The first method should have three entries", l_result.get("a").nrOfProviders, equalTo(3));

        assertThat("The first method should have two entries", l_result.get("b").nrOfProviders, equalTo(2));

        assertThat("The first method should have one entry", l_result.get("c").nrOfProviders, equalTo(1));

        assertThat("We should have the same amount of total sizes", l_result.get("a").totalClassMethods,
                equalTo(l_result.get("b").totalClassMethods));
        assertThat("We should have the same amount of total sizes", l_result.get("a").totalClassMethods,
                equalTo(l_result.get("c").totalClassMethods));

        Object[][] l_providerA = PhasedTestManager.fetchProvidersShuffled("a");

        assertThat(l_providerA.length, equalTo(6));
        assertThat(l_providerA[0].length, equalTo(2));

        assertThat(l_providerA[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerA[0][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerA[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerA[1][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));
        assertThat(l_providerA[2][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));
        assertThat(l_providerA[2][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerA[3][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));
        assertThat(l_providerA[3][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));
        assertThat(l_providerA[4][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2"));
        assertThat(l_providerA[4][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerA[5][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2"));
        assertThat(l_providerA[5][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));

        Object[][] l_providerB = PhasedTestManager.fetchProvidersShuffled("b");

        assertThat(l_providerB.length, equalTo(4));
        assertThat(l_providerB[0].length, equalTo(2));

        assertThat(l_providerB[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerB[0][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerB[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerB[1][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));
        assertThat(l_providerB[2][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));
        assertThat(l_providerB[2][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerB[3][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));
        assertThat(l_providerB[3][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));

        Object[][] l_providerC = PhasedTestManager.fetchProvidersShuffled("c");

        assertThat(l_providerC.length, equalTo(2));
        assertThat(l_providerC[0].length, equalTo(2));

        assertThat(l_providerC[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerC[0][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat(l_providerC[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0"));
        assertThat(l_providerC[1][1], equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));

    }

    @Test
    public void testCreateDataProviderData_modeConsumer() throws NoSuchMethodException, SecurityException {
        Map<Class, List<String>> l_myMap = new HashMap<Class, List<String>>();

        final Class<PhasedSeries_F_Shuffle> l_myClass = PhasedSeries_F_Shuffle.class;
        l_myMap.put(l_myClass, Arrays.asList("a", "b", "c"));

        Map<String, MethodMapping> l_result = PhasedTestManager.generatePhasedProviders(l_myMap,
                Phases.CONSUMER);

        assertThat("we need to have the expected key", l_result.containsKey("a"));
        assertThat("The first method should have three entries", l_result.get("a").nrOfProviders, equalTo(1));

        assertThat("The first method should have three entries", l_result.get("b").nrOfProviders, equalTo(2));

        assertThat("The first method should have three entries", l_result.get("c").totalClassMethods,
                equalTo(3));

        Object[][] l_providerA = PhasedTestManager.fetchProvidersShuffled("a");

        assertThat(l_providerA[0].length, equalTo(1));

        assertThat(l_providerA[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3"));

        Object[][] l_providerB = PhasedTestManager.fetchProvidersShuffled("b");

        assertThat(l_providerB[0].length, equalTo(1));

        assertThat(l_providerB[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3"));
        assertThat(l_providerB[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2"));

        Object[][] l_providerC = PhasedTestManager.fetchProvidersShuffled("c");

        assertThat(l_providerC[0].length, equalTo(1));

        assertThat(l_providerC[0][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3"));
        assertThat(l_providerC[1][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2"));
        assertThat(l_providerC[2][0], equalTo(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1"));

    }

    @Test
    public void testPhasedManagerContext() {
        Map<Class, List<String>> l_myMap = new HashMap<Class, List<String>>();

        l_myMap.put(this.getClass(), Arrays.asList("a", "b", "c", "testPhasedManagerContext"));

        final String l_phasedGroupId = "phasedGroupShuffled_4";
        PhasedTestManager.storePhasedContext(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testPhasedManagerContext",
                l_phasedGroupId);

        assertThat("We should have stored the correct property", PhasedTestManager.phaseContext.getProperty(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testPhasedManagerContext"),
                equalTo(l_phasedGroupId));

        PhasedTestManager.produceInStep("myVal");

        assertThat("We should have stored the correct key", PhasedTestManager.getPhasedCache().containsKey(
                "com.adobe.campaign.tests.integro.phased.PhasedTestManagerTests.testPhasedManagerContext("
                        + l_phasedGroupId + ")"));

        assertThat(PhasedTestManager.consumeFromStep("testPhasedManagerContext"), equalTo("myVal"));
    }

    /**** Single Executions ****/

    @Test
    public void testIsExecutedProducer_Producer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        assertThat("step1 should be considered as a producer execution",
                PhasedTestManager.isExecutedInProducerMode(l_myMethod));
    }

    @Test
    public void testIsExecutedProducer_ProducerShuffled() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_H_ShuffledClass.class.getMethod("step1", String.class);
        Phases.PRODUCER.activate();
        assertThat("step1 should be considered as a producer execution",
                !PhasedTestManager.isExecutedInProducerMode(l_myMethod));
    }

    @Test
    public void testIsExecutedProducer_ProducerLimit() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step2", String.class);

        assertThat("step1 should be considered as a producer execution. But it is also the limit",
                PhasedTestManager.isExecutedInProducerMode(l_myMethod));
    }

    @Test
    public void testIsExecutedProducer_Consumer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step3", String.class);

        assertThat("step1 should be executed in producer mode",
                !PhasedTestManager.isExecutedInProducerMode(l_myMethod));
    }

    @Test
    public void testFetchProvidersSingle_Producer_Producer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        Phases.PRODUCER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Producer_Consumer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        Phases.CONSUMER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(0));
    }

    @Test
    public void testFetchProvidersSingle_Producer_Inactive() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Producer_InactiveNoExec()
            throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_B_NoInActive.class.getMethod("step1", String.class);

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(0));
    }

    @Test
    public void testFetchProvidersSingle_Limt_Producer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step2", String.class);

        Phases.PRODUCER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Limit_Consumer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step2", String.class);

        Phases.CONSUMER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(0));
    }

    @Test
    public void testFetchProvidersSingle_Limit_Inactive() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step2", String.class);

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Consumer_Producer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step3", String.class);

        Phases.PRODUCER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(0));

    }

    @Test
    public void testFetchProvidersSingle_Consumer_Consumer() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step3", String.class);

        Phases.CONSUMER.activate();

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Consumer_Inactive() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step3", String.class);

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(1));

        assertThat(l_providerStep1[0], equalTo(PhasedTestManager.STD_PHASED_GROUP_SINGLE));
    }

    @Test
    public void testFetchProvidersSingle_Consumer_InactiveNoExec()
            throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_B_NoInActive.class.getMethod("step3", String.class);

        Object[] l_providerStep1 = PhasedTestManager.fetchProvidersSingle(l_myMethod);

        assertThat(l_providerStep1.length, equalTo(0));

    }

    /**
     * Testing issue #33 When we are in Inactive state the Shuffling should not
     * be executed
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testIsInCascadeMode() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_F_Shuffle.class.getMethod("step3", String.class);

        Phases.CONSUMER.activate();
        assertThat("We should be in Shuffled mode", PhasedTestManager.isPhasedTestShuffledMode(l_myMethod));

    }

    /**
     * Testing issue #33 When we are in Inactive state the Shuffled should not
     * happen
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testIsInCascadeMode_Negative() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_F_Shuffle.class.getMethod("step3", String.class);

        assertThat("We should not be in Shuffled mode",
                !PhasedTestManager.isPhasedTestShuffledMode(l_myMethod));

    }

    @Test
    public void testIsInCascadeMode_Negative2() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = NormalSeries_A.class.getMethod("firstTest");

        Phases.CONSUMER.activate();
        assertThat("We should not be in Shuffled mode",
                !PhasedTestManager.isPhasedTestShuffledMode(l_myMethod));

    }

    @Test(description = "Testing with a single mode")
    public void testIsInCascadeMode_Negative3() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        Phases.CONSUMER.activate();
        assertThat("We should not be in Shuffled mode",
                !PhasedTestManager.isPhasedTestShuffledMode(l_myMethod));

    }

    /****** Single mode tests *******/

    @Test(description = "Testing with a single mode")
    public void testIsInSingleMode_STD() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        Phases.CONSUMER.activate();
        assertThat("We should not be in Shuffled mode", PhasedTestManager.isPhasedTestSingleMode(l_myMethod));
    }

    @Test(description = "Testing with a single mode")
    public void testIsInSingleMode_InActiveState() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_A.class.getMethod("step1", String.class);

        assertThat("We should not be in Shuffled mode", PhasedTestManager.isPhasedTestSingleMode(l_myMethod));
    }

    @Test
    public void testIsInSingleMode_TestIsCascadingButTheStateIsInActive()
            throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_F_Shuffle.class.getMethod("step3", String.class);

        assertThat("We should not be in single mode", PhasedTestManager.isPhasedTestSingleMode(l_myMethod));

    }

    @Test
    public void testIsInSingleMode_Negative2() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = NormalSeries_A.class.getMethod("firstTest");

        Phases.CONSUMER.activate();
        assertThat("We should not be in single mode", !PhasedTestManager.isPhasedTestSingleMode(l_myMethod));

    }

    @Test
    public void testIsInSingleMode_Shuffled_Negative3() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_H_ShuffledClass.class.getMethod("step1", String.class);

        Phases.PRODUCER.activate();
        assertThat("We should not be in single mode", !PhasedTestManager.isPhasedTestSingleMode(l_myMethod));

    }

    @Test
    public void testIsInSingleMode() throws NoSuchMethodException, SecurityException {
        final Method l_myMethod = PhasedSeries_F_Shuffle.class.getMethod("step3", String.class);

        Phases.CONSUMER.activate();
        assertThat("We should be in Shuffled mode", PhasedTestManager.isPhasedTestShuffledMode(l_myMethod));

    }

    /****** Key Identity Methods *****/
    @Test
    public void testGenerateStepKeyIdentity() {
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", "B"),
                equalTo("A" + PhasedTestManager.STD_KEY_CLASS_SEPARATOR + "B"));
    }

    @Test
    public void testGenerateStepKeyIdentity2() {
        PhasedTestManager.phaseContext.put("A", "C");
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", "B"),
                equalTo("A(C)" + PhasedTestManager.STD_KEY_CLASS_SEPARATOR + "B"));
    }

    @Test
    public void testGenerateStepKeyIdentity3() {
        PhasedTestManager.phaseContext.put("A", "C");
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", "E", "B"),
                equalTo("E(C)" + PhasedTestManager.STD_KEY_CLASS_SEPARATOR + "B"));
    }

    @Test
    public void testGenerateStepKeyIdentity_nullStorageKey() {
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", null), equalTo("A"));
    }

    @Test
    public void testGenerateStepKeyIdentity_nullStorageKeyWithContext() {
        PhasedTestManager.phaseContext.put("A", "C");
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", null), equalTo("A(C)"));
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", ""), equalTo("A(C)"));
        assertThat(PhasedTestManager.generateStepKeyIdentity("A", " "), equalTo("A(C)"));
    }

    @Test
    public void testSB() {
        String x = "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_E_FullMonty.step1";

        StringBuilder sb = new StringBuilder(x);

        assertThat(x, equalTo(sb.toString()));
    }

    @Test
    public void testIsPhaseLimit() throws NoSuchMethodException, SecurityException {

        final Method l_myMethod = PhasedSeries_A.class.getMethod("step3", String.class);

        assertThat("step3 should be the phase limit", PhasedTestManager.isPhaseLimit(l_myMethod));
    }

    /*******
     * Keeping test context between phases
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     ******/
    @Test
    public void testFetchScenarioID() throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures

        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        assertThat("We should have the correct full name", PhasedTestManager.fetchScenarioName(l_itr),
                equalTo("com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError(Q)"));
    }
    
    /**
     * <table>
     * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td><td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td><td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td><td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case 1
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void phaseScenrioStates_case1_intra_PASSED() throws NoSuchMethodException, SecurityException {
        //Define previous step
        final Method l_myTestBeforeWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step1",
                String.class);
        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        
        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "0_1" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestBeforeWithOneArg);
        
        String l_scenarioName = PhasedTestManager.fetchScenarioName(l_itr);

        assertThat("The context should have been stored",
                !PhasedTestManager.phasedCache.containsKey(l_scenarioName));

        assertThat("While we are currently executing this test, we should continue",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));
        
        //Store state
        PhasedTestManager.scenarioStateStore(l_itr);        
        
        assertThat("The context should have been stored",
                PhasedTestManager.phasedCache.containsKey(l_scenarioName));

        assertThat("We should have the correct value", PhasedTestManager.phasedCache.get(l_scenarioName),
                equalTo("true"));

        assertThat("While we are currently executing this test, we should continue",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));
    }

    
    
    /**
     * <table>
     * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td><td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td><td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td><td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case 2
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void phaseScenrioStates_case2_intra_FAILED() throws NoSuchMethodException, SecurityException {
        //Define previous step
        final Method l_myTestBeforeWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);
        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        
        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.FAILURE);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestBeforeWithOneArg);
        
        PhasedTestManager.scenarioStateStore(l_itr);        
        String l_scenarioName = PhasedTestManager.fetchScenarioName(l_itr);

        assertThat("The context should have been stored",
                PhasedTestManager.phasedCache.containsKey(l_scenarioName));

        assertThat("We should have the correct value", PhasedTestManager.phasedCache.get(l_scenarioName),
                equalTo(ClassPathParser.fetchFullName(l_itr)));

        assertThat("While we are currently executing this test, we should continue",
                PhasedTestManager.scenarioStateDecision(l_itr),equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        //Define current step
        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);
        
        final Method l_myTestAfterWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);
        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_myTestAfterWithOneArg);
        
        String l_scenarioName2 = PhasedTestManager.fetchScenarioName(l_itr2);
        
        PhasedTestManager.scenarioStateStore(l_itr2);        

        assertThat("The two tests should be of the same scenario",
                l_scenarioName2, equalTo(l_scenarioName));

        assertThat("The context should be containing the previous step not the current one", PhasedTestManager.phasedCache.get(l_scenarioName),
                Matchers.not(equalTo(ClassPathParser.fetchFullName(l_itr2))));

        assertThat("We should not be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr2),equalTo(PhasedTestManager.ScenarioState.SKIP_PREVIOUS_FAILURE));

    }

    /**
     * <table>
     * <th><td>CASE</td><td>Phase</td><td>Current step Nr</td><td>Previous Step Result</td><td>Expected result</td><td>MERGED RESULT</td></th>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td>          <td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td>          <td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td>          <td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td>          <td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case 3
     * 
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void phaseScenrioStates_case3_intra_PASSED() throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures
        
        //Step2
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "1_2" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);
        
        assertThat("Before storing the context the value is true if we are not in consumer",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        PhasedTestManager.scenarioStateStore(l_itr);

        String l_scenarioName = PhasedTestManager.fetchScenarioName(l_itr);
        assertThat("The context should have been stored", PhasedTestManager.phasedCache.containsKey(l_scenarioName));
        assertThat("We should havee the correct value", PhasedTestManager.phasedCache.get(l_scenarioName),
                equalTo(Boolean.TRUE.toString()));

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));
        
        //Step 3
        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);
        
        final Method l_myTestAfterWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);
        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "1_2" });
        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_myTestAfterWithOneArg);

        PhasedTestManager.clearCache();

        Phases.PRODUCER.activate();

        assertThat("In producer mode We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));
    }
    
    
    /**
     * Issue #43 : What happens when there is no context.
     * 
     * <table>
     * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td><td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td><td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td><td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case 4
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void phaseScenrioStates_case4_extra_NoneExecuted()
            throws NoSuchMethodException, SecurityException {

        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step1",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_6" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        Phases.CONSUMER.activate();

        assertThat("We should continue with the phase group if there is no result for this phase group",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));

    }

    /**
    * <table>
    * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
    * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
    * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>4</td><td>Consumer</td>          <td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>5</td><td>Consumer</td>          <td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>6</td><td>Consumer</td>          <td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
    * <tr><td>7</td><td>Consumer</td>          <td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
    * </table>
    * 
    * This is case 5
    * 
    * Author : gandomi
    *
    * @throws NoSuchMethodException
    * @throws SecurityException
    *
    */
    @Test
    public void phaseScenrioStates_case5_extra_PASSED()
            throws NoSuchMethodException, SecurityException {
        
        //Step 2
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        assertThat("Before storing the context the value is true if we are not in consumer",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        PhasedTestManager.scenarioStateStore(l_itr);

        Phases.CONSUMER.activate();
        
        //Step 3
        final Method l_myTestWithOneArg2 = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);        
        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_myTestWithOneArg2);

        assertThat("In consumer mode We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr2), equalTo(PhasedTestManager.ScenarioState.CONTINUE));

    }
    
    /**
    * <table>
    * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
    * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
    * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>4</td><td>Consumer</td>          <td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>5</td><td>Consumer</td>          <td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
    * <tr><td>6</td><td>Consumer</td>          <td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
    * <tr><td>7</td><td>Consumer</td>          <td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
    * </table>
    * 
    * This is case 6
    * 
    * Author : gandomi
    *
    * @throws NoSuchMethodException
    * @throws SecurityException
    *
    */
    @Test
    public void phaseScenrioStates_case6_extra_FAILED()
            throws NoSuchMethodException, SecurityException {
        
        //Step 2
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.FAILURE);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        assertThat("Before storing the context the value is true if we are not in consumer",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        PhasedTestManager.scenarioStateStore(l_itr);

        Phases.CONSUMER.activate();
        
        //Step 3
        final Method l_myTestWithOneArg2 = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);        
        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "2_1" });
        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_myTestWithOneArg2);

        assertThat("In consumer mode We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr2), equalTo(PhasedTestManager.ScenarioState.SKIP_PREVIOUS_FAILURE));

    }

    /**
     * Issue #43 : What happens when there is no context.
     *
     * <table>
     * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td>          <td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td>          <td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td>          <td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td>          <td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case 7
     * 
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testDoNotContinueIfNoTestsExistBeforeConsumer_doNotContinue()
            throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        //Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        Phases.CONSUMER.activate();

        assertThat("We should not continue with the phase group if there is no result for this phase group",
                PhasedTestManager.scenarioStateDecision(l_itr), equalTo(PhasedTestManager.ScenarioState.SKIP_NORESULT));

    }

    



    /**
     * <table>
     * <tr><td>CASE</th><th>Phase</th><th>Current stepthr</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULLT</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>> 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>> 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>4</td><td>Consumer</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>5</td><td>Consumer</td><td>>1</td><td>PASSED</td><td>Continue</td><td>PASSED</td></tr>
     * <tr><td>6</td><td>Consumer</td><td>>1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td></tr>
     * <tr><td>7</td><td>Consumer</td><td>>1</td><td>N/A</td><td>SKIP</td><td>SKIP</td></tr>
     * </table>
     * 
     * This is case ???
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testStateIstKeptBetweenPhases_NegativeSkipped()
            throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SKIP);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        PhasedTestManager.scenarioStateStore(l_itr);

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr),
                equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        String l_name = PhasedTestManager.fetchScenarioName(l_itr);
        assertThat("We should have the correct value", PhasedTestManager.phasedCache.get(l_name),
                equalTo(ClassPathParser.fetchFullName(l_itr)));

    }


    @Test
    public void testStateIstKeptBetweenPhases_NegativeStaysNegativeUnlessSameTest()
            throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.FAILURE);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        PhasedTestManager.scenarioStateStore(l_itr);

        assertThat(
                "We should be able to continue with the phase group, iff we are reeexecuting the same test",
                PhasedTestManager.scenarioStateDecision(l_itr),equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);

        PhasedTestManager.scenarioStateStore(l_itr);

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr),equalTo(PhasedTestManager.ScenarioState.CONTINUE));

    }

    @Test
    public void testStateIstKeptBetweenPhases_SuccessCanTurnNegative()
            throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step1",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "1_3" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        PhasedTestManager.scenarioStateStore(l_itr);

        final Method l_myTestWithOneArg2 = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step2",
                String.class);

        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itr2.getStatus()).thenReturn(ITestResult.FAILURE);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "1_3" });
        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_myTestWithOneArg2);

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr2),equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        PhasedTestManager.scenarioStateStore(l_itr2);

        assertThat("We should  be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr2),equalTo(PhasedTestManager.ScenarioState.CONTINUE));

        final Method l_myTestWithOneArg3 = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr3 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod3 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com3 = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr3.getMethod()).thenReturn(l_itrMethod3);
        Mockito.when(l_itr3.getParameters()).thenReturn(new Object[] { "1_3" });
        Mockito.when(l_itrMethod3.getConstructorOrMethod()).thenReturn(l_com3);
        Mockito.when(l_com3.getMethod()).thenReturn(l_myTestWithOneArg3);

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr3),equalTo(PhasedTestManager.ScenarioState.SKIP_PREVIOUS_FAILURE));

        PhasedTestManager.scenarioStateStore(l_itr);

        assertThat("We should no longer be able to continue with the phase group",
                PhasedTestManager.scenarioStateDecision(l_itr),equalTo(PhasedTestManager.ScenarioState.SKIP_PREVIOUS_FAILURE));
    }


    @Test
    public void testStandardReportName_default() throws NoSuchMethodException, SecurityException {

        assertThat(PhasedSeries_H_ShuffledClassWithError.class.getSimpleName(),
                equalTo("PhasedSeries_H_ShuffledClassWithError"));

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr), equalTo("Q"));

        Phases.CONSUMER.activate();

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr), equalTo("Q"));

    }

    @Test
    public void testConfigureReports() {
        assertThat("We should not have values for the prefix",
                PhasedTestManager.MergedReportData.prefix.isEmpty());
        assertThat("We should not have values for the suffix",
                PhasedTestManager.MergedReportData.suffix.isEmpty());

        PhasedTestManager.MergedReportData.configureMergedReportName(
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.SCENARIO_NAME)), new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.PHASE, PhasedReportElements.PHASE_GROUP)));

        assertThat("The prefix should have been set", PhasedTestManager.MergedReportData.prefix,
                Matchers.hasItems(PhasedReportElements.SCENARIO_NAME));
        assertThat("The suffix should have been set", PhasedTestManager.MergedReportData.suffix,
                Matchers.hasItems(PhasedReportElements.PHASE));

        PhasedTestManager.MergedReportData.resetReport();

        assertThat("We should not have values for the prefix",
                PhasedTestManager.MergedReportData.prefix.isEmpty());
        assertThat("We should not have values for the suffix",
                PhasedTestManager.MergedReportData.suffix.isEmpty());

    }

    @Test
    public void testPhasedReportElements_StandardReportName_ScenarioName()
            throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should get the correct value for the SCENARIO_NAME",
                PhasedReportElements.SCENARIO_NAME.fetchElement(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError"));
    }

    @Test
    public void testPhasedReportElements_StandardReportName_Phase()
            throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should get the correct value for the SCENARIO_NAME",
                PhasedReportElements.PHASE.fetchElement(l_itr), equalTo(Phases.getCurrentPhase().toString()));
    }

    @Test
    public void testPhasedReportElements_StandardReportName_PhaseGROUP()
            throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should get the correct value for the SCENARIO_NAME",
                PhasedReportElements.PHASE_GROUP.fetchElement(l_itr), equalTo("Q"));
    }

    @Test
    public void testPhasedReportElements_StandardReportName_DataProviders()
            throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q", "A", "T" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should get the correct value for the SCENARIO_NAME",
                PhasedReportElements.DATA_PROVIDERS.fetchElement(l_itr), equalTo("A_T"));
    }

    @Test
    public void testStandardReportName_configured() throws NoSuchMethodException, SecurityException {

        PhasedTestManager.MergedReportData.configureMergedReportName(
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.SCENARIO_NAME)),
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.PHASE)));

        assertThat(PhasedSeries_H_ShuffledClassWithError.class.getSimpleName(),
                equalTo("PhasedSeries_H_ShuffledClassWithError"));

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError__Q__" + Phases.getCurrentPhase().toString()));

        Phases.CONSUMER.activate();

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError__Q__" + Phases.CONSUMER.toString()));

    }

    @Test
    public void testStandardReportName_configured2() throws NoSuchMethodException, SecurityException {

        PhasedTestManager.MergedReportData.configureMergedReportName(
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.SCENARIO_NAME)), new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));

        assertThat(PhasedSeries_H_ShuffledClassWithError.class.getSimpleName(),
                equalTo("PhasedSeries_H_ShuffledClassWithError"));

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q", "uo", "Vadis" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        Phases.CONSUMER.activate();

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError__Q__uo_Vadis__" + Phases.CONSUMER.toString()));
    }

    @Test
    public void testStandardReportName_DP_configured() throws NoSuchMethodException, SecurityException {

        PhasedTestManager.MergedReportData.configureMergedReportName(
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.SCENARIO_NAME)),
                new LinkedHashSet<>(Arrays.asList(PhasedReportElements.PHASE)));

        assertThat(PhasedSeries_H_ShuffledClassWithError.class.getSimpleName(),
                equalTo("PhasedSeries_H_ShuffledClassWithError"));

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "Q", "A", "T" });
        Mockito.when(l_itrMethod.getQualifiedName()).thenReturn(
                "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError.step3");

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError__Q__" + Phases.getCurrentPhase().toString()));

        Phases.CONSUMER.activate();

        assertThat("We should be able to continue with the phase group",
                PhasedTestManager.fetchTestNameForReport(l_itr),
                equalTo("phasedSeries_H_ShuffledClassWithError__Q__" + Phases.CONSUMER.toString()));
    }

    /**
     * Standard test for the merge to see if the duration is correctly summed.
     *
     * Author : gandomi
     *
     *
     */
    @Test
    public void testFetchDurationTime_HW() {

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr1.getParameters()).thenReturn(new Object[] { "Q" });

        Mockito.when(l_itr1.getStartMillis()).thenReturn((long) 3);
        Mockito.when(l_itr1.getEndMillis()).thenReturn((long) 5);

        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "Q" });
        Mockito.when(l_itr2.getStartMillis()).thenReturn((long) 7);
        Mockito.when(l_itr2.getEndMillis()).thenReturn((long) 11);

        List<ITestResult> l_resultList = Arrays.asList(l_itr2, l_itr1);

        assertThat("We should find the correst start millisecond",
                PhasedTestManager.fetchDurationMillis(l_resultList), equalTo(6l));

    }

    /**
     * Testing case where the given list is empty or null
     *
     * Author : gandomi
     *
     *
     */
    @Test
    public void testFetchDurationTime_Negative() {
        //null
        assertThrows(IllegalArgumentException.class, () -> PhasedTestManager.fetchDurationMillis(null));

        //Empty
        List<ITestResult> l_resultList = Arrays.asList();

        assertThrows(IllegalArgumentException.class,
                () -> PhasedTestManager.fetchDurationMillis(l_resultList));

    }

    /**
     * Testing caase where list is from different phase groups
     *
     * Author : gandomi
     *
     *
     */
    @Test
    public void testFetchDurationTime_Negative2() {

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr1.getParameters()).thenReturn(new Object[] { "Q" });

        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "U" });

        List<ITestResult> l_resultList = Arrays.asList(l_itr2, l_itr1);

        assertThrows(IllegalArgumentException.class,
                () -> PhasedTestManager.fetchDurationMillis(l_resultList));

    }

    /**
     * Testing caase where the list is from different phased scenarios
     *
     * Author : gandomi
     *
     *
     */
    @Test
    public void testFetchDurationTime_Negative3() {

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod1 = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod1);
        Mockito.when(l_itrMethod1.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClassWithError.class);
        Mockito.when(l_itr1.getParameters()).thenReturn(new Object[] { "Q" });

        ITestResult l_itr2 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        Mockito.when(l_itr2.getMethod()).thenReturn(l_itrMethod2);
        Mockito.when(l_itrMethod2.getRealClass()).thenReturn(PhasedSeries_H_ShuffledClass.class);
        Mockito.when(l_itr2.getParameters()).thenReturn(new Object[] { "Q" });

        List<ITestResult> l_resultList = Arrays.asList(l_itr2, l_itr1);

        assertThrows(IllegalArgumentException.class,
                () -> PhasedTestManager.fetchDurationMillis(l_resultList));

    }

    @Test
    public void testStepName()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);

        Mockito.when(l_itr1.getName()).thenReturn("myTest");
        Mockito.when(l_itr1.getParameters()).thenReturn(new Object[] { "Q" });

        assertThat("We should have the correct name", PhasedTestManager.fetchPhasedStepName(l_itr1),
                equalTo("Q_myTest"));
    }

    @Test
    public void testStepName_DP()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);

        Mockito.when(l_itr1.getName()).thenReturn("myTest");
        Mockito.when(l_itr1.getParameters()).thenReturn(new Object[] { "Q", "A" });

        assertThat("We should have the correct name", PhasedTestManager.fetchPhasedStepName(l_itr1),
                equalTo("Q__A_myTest"));
    }

    @Test
    public void testStepName_Negative() throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException {

        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] {});

        assertThrows(IllegalArgumentException.class, () -> PhasedTestManager.fetchPhasedStepName(l_itr));
    }

    @Test
    public void testCreateNewException() {

        final String l_originalMessage = "Original Message";
        AssertionError l_originalAssertionError = new AssertionError(l_originalMessage);

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod1 = Mockito.mock(ITestNGMethod.class);
        Mockito.when(l_itr1.getStatus()).thenReturn(ITestResult.FAILURE);
        final String l_renamedMethod = "Q_myTest";

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod1);
        Mockito.when(l_itrMethod1.getMethodName()).thenReturn(l_renamedMethod);

        Mockito.when(l_itr1.getThrowable()).thenReturn(l_originalAssertionError);

        PhasedTestManager.generateStepFailure(l_itr1);
        Throwable l_newThrowable = l_itr1.getThrowable();

        assertThat("We should have the correct exception", l_newThrowable.getMessage(),
                Matchers.startsWith(l_originalMessage));
        assertThat("The message should end with the original message", l_newThrowable.getMessage(),
                Matchers.endsWith(Phases.getCurrentPhase().toString() + "]"));

        assertThat("We should have the step name in the message", l_newThrowable.getMessage(),
                Matchers.containsString(l_renamedMethod));

        assertThat("The caused by should be the same exception as before", l_newThrowable.getCause(),

                equalTo(l_originalAssertionError.getCause()));

    }

    @Test
    public void testCreateNewExceptionNoMessage() {

        NullPointerException l_nullptre = new NullPointerException();

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod1 = Mockito.mock(ITestNGMethod.class);
        Mockito.when(l_itr1.getStatus()).thenReturn(ITestResult.FAILURE);
        final String l_renamedMethod = "Q_myTest";

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod1);
        Mockito.when(l_itrMethod1.getMethodName()).thenReturn(l_renamedMethod);

        Mockito.when(l_itr1.getThrowable()).thenReturn(l_nullptre);

        PhasedTestManager.generateStepFailure(l_itr1);
        Throwable l_newThrowable = l_itr1.getThrowable();

        assertThat("We should have the correct exception", l_newThrowable.getMessage(),
                Matchers.startsWith("["));
        assertThat("The message should end with the original message", l_newThrowable.getMessage(),
                Matchers.endsWith(Phases.getCurrentPhase().toString() + "]"));
        assertThat("We should have the step name in the message", l_newThrowable.getMessage(),
                Matchers.containsString(l_renamedMethod));

        assertThat("The caused by should be the same exception as before", l_newThrowable.getCause(),
                equalTo(l_nullptre.getCause()));

    }

    @Test
    public void testCreateNewException_Negative1() {

        final String l_originalMessage = "Original Message";
        AssertionError l_originalAssertionError = new AssertionError(l_originalMessage);

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod1 = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr1.getStatus()).thenReturn(ITestResult.SUCCESS);

        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod1);

        Mockito.when(l_itrMethod1.getMethodName()).thenReturn("Q_myTest");

        Mockito.when(l_itr1.getThrowable()).thenReturn(l_originalAssertionError);

        assertThrows(IllegalArgumentException.class, () -> PhasedTestManager.generateStepFailure(l_itr1));

    }

    //in relation to combining phased with data providers #26  
    @Test
    public void testCrossJoinDataProviders() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = new Object[][] { { "Z" }, { "M" } };

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(4));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(2));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[0][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][1], equalTo("M"));
        assertThat("We should have the correct values in the lines", result[2][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[2][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[3][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[3][1], equalTo("M"));

    }

    @Test
    public void testCrossJoinDataProviders_multiple() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = new Object[][] { { "Z", "D" }, { "M", "F" } };

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(4));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(3));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[0][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[0][2], equalTo("D"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][1], equalTo("M"));
        assertThat("We should have the correct values in the lines", result[1][2], equalTo("F"));
        assertThat("We should have the correct values in the lines", result[2][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[2][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[2][2], equalTo("D"));
        assertThat("We should have the correct values in the lines", result[3][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[3][1], equalTo("M"));
        assertThat("We should have the correct values in the lines", result[3][2], equalTo("F"));

    }

    @Test
    public void testCrossJoinDataProviders_multipleDifferentLineNrs() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = new Object[][] { { "Z" }, { "M" }, { "K" } };

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(6));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(2));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[0][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][1], equalTo("M"));
        assertThat("We should have the correct values in the lines", result[2][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[2][1], equalTo("K"));
        assertThat("We should have the correct values in the lines", result[3][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[3][1], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[4][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[4][1], equalTo("M"));
        assertThat("We should have the correct values in the lines", result[5][0], equalTo("B"));
        assertThat("We should have the correct values in the lines", result[5][1], equalTo("K"));

    }

    @Test
    public void testCrossJoinDataProviders_Empty1() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = new Object[0][0];

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(2));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(1));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("B"));

    }

    @Test
    public void testCrossJoinDataProviders_EmptyColumn() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = new Object[][] { {} };

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(2));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(1));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("B"));
    }

    @Test
    public void testCreateNewException_Negative2() {

        final String l_originalMessage = "Original Message";
        AssertionError l_originalAssertionError = new AssertionError(l_originalMessage);

        ITestResult l_itr1 = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod1 = Mockito.mock(ITestNGMethod.class);

        Mockito.when(l_itr1.getStatus()).thenReturn(ITestResult.SKIP);
        Mockito.when(l_itr1.getMethod()).thenReturn(l_itrMethod1);

        Mockito.when(l_itrMethod1.getMethodName()).thenReturn("Q_myTest");

        Mockito.when(l_itr1.getThrowable()).thenReturn(l_originalAssertionError);

        assertThrows(IllegalArgumentException.class, () -> PhasedTestManager.generateStepFailure(l_itr1));

    }

    @Test
    public void testCrossJoinDataProviders_null() {

        //System.out.println(0 % 0);
        Object[][] providerSeriess1 = new Object[][] { { "A" }, { "B" } };

        Object[][] providerSeriess2 = null;

        Object[][] result = PhasedTestManager.dataProvidersCrossJoin(providerSeriess1, providerSeriess2);

        assertThat("The dimensions should be correct in side - nr of lines", result.length, equalTo(2));

        assertThat("The dimensions should be correct in side - nr of columns", result[0].length, equalTo(1));

        assertThat("We should have the correct values in the lines", result[0][0], equalTo("A"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("B"));

    }

    /**************** Fetching Data providers *******************/
    @Test
    public void testFetchingDataProvidersInDPClass() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException {
        Class<PhasedSeries_L_ShuffledDP> l_classLevelDP = PhasedSeries_L_ShuffledDP.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_classLevelDP);

        assertThat("We should have two entries", result.length, equalTo(2));
        assertThat("We should have the correct values in the lines", result[0][0],
                equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat("We should have the correct values in the lines", result[1][0],
                equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));
    }

    @Test
    public void testFetchingDataProvidersInDPClass_Negative_Private() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class<PhasedSeries_L_ShuffledDPPrivate> l_classLevelDP = PhasedSeries_L_ShuffledDPPrivate.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_classLevelDP);

        assertThat("We should have two entries", result.length, equalTo(2));
        assertThat("We should have the correct values in the lines", result[0][0],
                equalTo(PhasedSeries_L_PROVIDER.PROVIDER_A));
        assertThat("We should have the correct values in the lines", result[1][0],
                equalTo(PhasedSeries_L_PROVIDER.PROVIDER_B));
    }

    @Test
    public void testFetchingDataProvidersInTestClass() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class<PhasedSeries_L_ShuffledDPSimple> l_class = PhasedSeries_L_ShuffledDPSimple.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_class);

        assertThat("We should have two entries", result.length, equalTo(2));
        assertThat("We should have the correct values in the lines", result[0][0], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("M"));

    }

    @Test
    public void testFetchingDataProvidersInTestClass_NegativePrivat() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class<PhasedSeries_L_ShuffledDPSimplePrivate> l_class = PhasedSeries_L_ShuffledDPSimplePrivate.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_class);

        assertThat("We should have two entries", result.length, equalTo(2));
        assertThat("We should have the correct values in the lines", result[0][0], equalTo("Z"));
        assertThat("We should have the correct values in the lines", result[1][0], equalTo("M"));

    }

    /**
     * In this case we do not have a data provider defined at any level. We
     * should simply return an empty array
     *
     * Author : gandomi
     */
    @Test
    public void testFetchingDataProvidersInTestClass_NegativeNoTestOnClass() {
        Class<PhasedSeries_A> l_class = PhasedSeries_A.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_class);

        assertThat("We should be getting an empty array", result.length, equalTo(0));
    }

    /**
     * In this case we do not have a data provider defined at any level. We
     * should simply return an empty array
     *
     * Author : gandomi
     */
    @Test
    public void testFetchingDataProvidersInTestClass_NegativeNoDataProvider() {
        Class<PhasedSeries_K_ShuffledClass_noproviders> l_class = PhasedSeries_K_ShuffledClass_noproviders.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_class);

        assertThat("We should be getting an empty array", result.length, equalTo(0));
    }

    /**
     * In this case we have a badly declared detaprovider. The data provider does not exist in this case
     *
     * Author : gandomi
     */
    @Test
    public void testFetchingDataProvidersInTestClass_NegativeDataProviderNotExists() {
        Class<PhasedSeries_L_DPDefinitionInexistant> l_class = PhasedSeries_L_DPDefinitionInexistant.class;

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedTestManager.fetchDataProviderValues(l_class));

    }

    /**
     * In this case the user sets the phased data provider on the class.
     *
     * Author : gandomi
     */
    @Test
    public void testFetchingDataProvidersInTestClass_UsingPhasedDataProviders() {
        Class<PhasedSeries_H_ShuffledClass> l_class = PhasedSeries_H_ShuffledClass.class;

        Object[][] result = PhasedTestManager.fetchDataProviderValues(l_class);

        assertThat("We should be getting an empty array", result.length, equalTo(0));
    }

    @Test
    public void generatePhaseGroupID() {
        assertThat("The id should be a concatenation of the parameters",
                PhasedTestManager.concatenateParameterArray(new Object[] { "A" }), equalTo("A"));

        assertThat("The id should be a concatenation of the parameters",
                PhasedTestManager.concatenateParameterArray(new Object[] { "A", "B" }), equalTo("A__B"));

        assertThat("The id should be a concatenation of the parameters",
                PhasedTestManager.concatenateParameterArray(new Object[0]), equalTo(""));

        assertThat("The id should be a concatenation of the parameters",
                PhasedTestManager.concatenateParameterArray(new Object[] { "A", new Integer(5) }),
                equalTo("A__5"));

    }

    @Test
    public void testMergeActivation() {
        assertThat("By default we should not have merged reports",
                !PhasedTestManager.isMergedReportsActivated());

        PhasedTestManager.activateMergedReports();

        assertThat("By default we should not have merged reports",
                PhasedTestManager.isMergedReportsActivated());

        PhasedTestManager.deactivateMergedReports();

        assertThat("By default we should not have merged reports",
                !PhasedTestManager.isMergedReportsActivated());
    }

    @Test
    public void testChangeException() {
        final String l_originalMessage = "Message Before";
        PhasedTestConfigurationException l_ptce = new PhasedTestConfigurationException(l_originalMessage);

        assertThat("We should have the expected message", l_ptce.getMessage(), equalTo(l_originalMessage));

        final String l_newMessage = "New Message";
        PhasedTestManager.changeExceptionMessage(l_ptce, l_newMessage);

        assertThat("The message must have changed", l_ptce.getMessage(),
                Matchers.not(equalTo(l_originalMessage)));

        assertThat("The message must have the correct value", l_ptce.getMessage(), equalTo(l_newMessage));
    }

    @Test
    public void testChangeExceptio_NullPointer() {
        final String l_originalMessage = "Message Before";
        NullPointerException l_nullptre = new NullPointerException();

        assertThat("We should have the expected message", l_nullptre.getMessage(), Matchers.nullValue());

        final String l_newMessage = "New Message";
        PhasedTestManager.changeExceptionMessage(l_nullptre, l_newMessage);

        assertThat("The message must have changed", l_nullptre.getMessage(),
                Matchers.not(equalTo(l_originalMessage)));

        assertThat("The message must have the correct value", l_nullptre.getMessage(), equalTo(l_newMessage));
    }

    @Test
    public void testSystemProperties() {
        Assert.assertFalse(
                new Boolean(System.getProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "FALSE")));

        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        Assert.assertTrue(
                new Boolean(System.getProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "FALSE")));

        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "FALSE");

        Assert.assertFalse(
                new Boolean(System.getProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "FALSE")));
    }

    @Test
    public void testDoesTesthaveStepsInProducer() throws NoSuchMethodException, SecurityException {
        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        //Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_6" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestWithOneArg);

        //Testing as consumer
        Phases.CONSUMER.activate();

        assertThat("This method and phase group should not have steps in the producer",
                !PhasedTestManager.hasStepsExecutedInProducer(l_itr, Phases.CONSUMER));

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_3" });

        assertThat("This method and phase group should  have steps in the producer",
                PhasedTestManager.hasStepsExecutedInProducer(l_itr));

        //Testing as producer

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_3" });

        assertThat("This method and phase group should not have steps in the producer",
                !PhasedTestManager.hasStepsExecutedInProducer(l_itr, Phases.PRODUCER));

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3" });

        assertThat(
                "This method and phase group should not have steps in the producer since we are in Producer",
                !PhasedTestManager.hasStepsExecutedInProducer(l_itr, Phases.PRODUCER));

        //Testing non-phased

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_3" });

        assertThat("This method and phase group should not have steps in the producer",
                !PhasedTestManager.hasStepsExecutedInProducer(l_itr, Phases.NON_PHASED));

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3" });

        assertThat(
                "This method and phase group should not have steps in the producer since we are in Producer",
                !PhasedTestManager.hasStepsExecutedInProducer(l_itr, Phases.NON_PHASED));

    }

    @Test
    public void tesFetchFromPhaseGroup() throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        //Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_6" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod())
                .thenReturn(PhasedSeries_H_ShuffledClass.class.getMethod("step2", String.class));

        Phases.CONSUMER.activate();
        assertThat("We should have 0 steps before", PhasedTestManager.fetchNrOfStepsBeforePhaseChange(l_itr),
                equalTo(0));

        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_6" });

        assertThat("We should have 2 steps before", PhasedTestManager.fetchNrOfStepsBeforePhaseChange(l_itr),
                equalTo(2));

        Phases.NON_PHASED.activate();
        Mockito.when(l_itr.getParameters())
                .thenReturn(new Object[] { PhasedTestManager.STD_PHASED_GROUP_SINGLE });

        assertThat("We should have 1 step before", PhasedTestManager.fetchNrOfStepsBeforePhaseChange(l_itr),
                equalTo(1));
    }

    @Test
    public void tesFetchFromPhaseGroup_negative() throws NoSuchMethodException, SecurityException {

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itr.getMethod()).thenReturn(l_itrMethod);
        //Mockito.when(l_itr.getStatus()).thenReturn(ITestResult.SUCCESS);
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "0_6" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod())
                .thenReturn(PhasedSeries_H_ShuffledClass.class.getMethod("step2", String.class));

        Phases.CONSUMER.activate();
        assertThrows(PhasedTestException.class,
                () -> PhasedTestManager.fetchNrOfStepsBeforePhaseChange(l_itr));

    }

}
