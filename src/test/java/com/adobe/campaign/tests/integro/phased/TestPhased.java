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

import com.adobe.campaign.tests.integro.phased.data.*;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_RecipientClass.PhasedSeries_J_ShuffledClassInAClass;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDP;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDPSimple;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledNoArgs;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledWrongArgs;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertThrows;

public class TestPhased {
    @BeforeMethod
    public void resetVariables() {

        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.deactivateTestSelectionByProducerMode();

        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        //Delete standard cache file
        File l_importCacheFile = new File(
                GeneralTestUtils.fetchCacheDirectory(PhasedTestManager.STD_STORE_DIR),
                PhasedTestManager.STD_STORE_FILE);

        if (l_importCacheFile.exists()) {
            l_importCacheFile.delete();
        }

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(),
                new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));
    }

    @Test
    public void testProducer_ThatTheTestsOnlyExecuteUptoTheLimit() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(
                Arrays.asList(new XmlClass(PhasedSeries_A.class), new XmlClass(NormalSeries_A.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_A.class)).count(),
                is(equalTo(2)));

        assertThat("We should have 1 successful methods of normal tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(NormalSeries_A.class)).count(),
                is(equalTo(1)));

    }

    /**
     * In this case if no shuffle is possible, and we have not set a Phase Event we should execute it as a normal test
     * <p>
     * Author : gandomi
     */
    @Test
    public void testSINGLE_whereNoPhaseEventHasBeenSet() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Arrays.asList(new XmlClass(PhasedSeries_D_SingleNoPhase.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("All Tests shuld have been executed",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_D_SingleNoPhase.class)).count(),
                is(equalTo(3)));
    }

    /**
     * In this case when we have not set any value regarding the Phase state we are in the state IANACTIVE. In this case
     * the execution of not is defined by the Class Annotation
     * <p>
     * Author : gandomi
     */
    @Test
    public void testProducer_ThatTheTestsOnlyExecuteUptoTheLimit_NoValueSet() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        // Add classes to test
        myTest.setXmlClasses(
                Arrays.asList(new XmlClass(PhasedSeries_A.class), new XmlClass(NormalSeries_A.class)));

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_A.class)).count(),
                is(equalTo(3)));

        assertThat("We should have 1 successful methods of normal tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(NormalSeries_A.class)).count(),
                is(equalTo(1)));

    }

    /**
     * In this case when we have not set any value regarding the Phase state we are in the state IANACTIVE. In this case
     * the execution of not is defined by the Class Annotation
     * <p>
     * Author : gandomi
     */
    @Test
    public void testInActiveNoExecutions() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_B_NoInActive.class)));

        myTestNG.run();

        assertThat("We should have no executed methods of phased Tests",
                (int) tla.getFailedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_B_NoInActive.class)).count(),
                is(equalTo(0)));

        assertThat("We should have no executed methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_B_NoInActive.class)).count(),
                is(equalTo(0)));

    }

    @Test
    public void testConsumer_theTestsStartFromTheCorrectPlace() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        // Add class
        myTest.setXmlClasses(Arrays.asList(new XmlClass(PhasedSeries_H_SingleClass.class),
                new XmlClass(NormalSeries_A.class)));

        System.setProperty(PhasedTestManager.PROP_SELECTED_PHASE, "conSumer");
        Properties phasedCache = PhasedTestManager.phasedCache;
        phasedCache.put("com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass.step2("
                + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")", "AB");

        PhasedTestManager.storeTestData(PhasedSeries_H_SingleClass.class,
                PhasedTestManager.STD_PHASED_GROUP_SINGLE, "true");

        myTestNG.run();

        assertThat("We should have 1 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(1)));

        assertThat("We should have 1 successful methods of normal tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(NormalSeries_A.class)).count(),
                is(equalTo(1)));

    }

    /**
     * Related to issue #43 Skip consumer tests if the producer has not been executed.
     * <p>
     * Author : gandomi
     */
    @Test
    public void testConsumerShuffled_noProducerHasRun() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        // Add class
        myTest.setXmlClasses(Arrays.asList(new XmlClass(PhasedSeries_H_ShuffledClass.class),
                new XmlClass(NormalSeries_A.class)));

        Phases.CONSUMER.activate();

        //Adding otherwise we can have an exception if there is no phasedTest.properties file
        PhasedTestManager.produceInStep("just for testing");

        myTestNG.run();

        //This is because the Phase group 0-3 should still be executed
        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_ShuffledClass.class)).count(),
                is(equalTo(3)));

        //This is because the Phase groups 1-3 and 2-3 should not be executed
        assertThat("We should have no failed methods of phased Tests",
                (int) tla.getFailedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_ShuffledClass.class)).count(),
                is(equalTo(0)));

        //This is because phase groups 1-3 and 2-3 should not have the necessary context to run 
        assertThat("We should have 3 skipped methods of phased Tests",
                (int) tla.getSkippedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_ShuffledClass.class)).count(),
                is(equalTo(3)));

        assertThat("We should have 1 successful methods of normal tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(NormalSeries_A.class)).count(),
                is(equalTo(1)));

    }

    /**
     * Related to issue #43 Skip consumer tests if the producer has not been executed.
     * <p>
     * Author : gandomi
     */
    @Test
    public void testConsumerSingle_noProducerHasRun() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        // Add class
        final Class<PhasedSeries_H_SingleClass> l_targetTestClass = PhasedSeries_H_SingleClass.class;
        myTest.setXmlClasses(
                Arrays.asList(new XmlClass(l_targetTestClass), new XmlClass(NormalSeries_A.class)));

        Phases.CONSUMER.activate();

        //Adding otherwise we can have an exception if there is no phasedTest.properties file
        PhasedTestManager.produceInStep("just for testing");

        myTestNG.run();

        //This is because the Phase group 0-3 should still be executed
        assertThat("We should have 0 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_targetTestClass)).count(),
                is(equalTo(0)));

        //This is because the Phase groups 1-3 and 2-3 should not be executed
        assertThat("We should have no failed methods of phased Tests",
                (int) tla.getFailedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_targetTestClass)).count(),
                is(equalTo(0)));

        //This is because phase groups 1-3 and 2-3 should not have the necessary context to run 
        assertThat("We should have 1 skipped methods of phased Tests",
                (int) tla.getSkippedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_targetTestClass)).count(),
                is(equalTo(1)));

        assertThat("We should have 1 successful methods of normal tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(NormalSeries_A.class)).count(),
                is(equalTo(1)));

    }

    @Test
    public void testFullMonty_SingleRun() {
        //  ***** PRODUCER ****
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_E_FullMonty.class)));

        Phases.PRODUCER.activate();
        myTestNG.run();

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(2)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(2)));

        assertThat("We should have no unsuccesful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

        // ***** COSNUMER ****

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Phased Tests");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_E_FullMonty.class)));

        myTestNG2.run();

        assertThat("We should have 1 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(1)));

        assertThat("We should have no unsuccesful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

    }

    // ***** Tests for DataDependencies ******

    /**
     * Here we check that the consuming a producing is taken into account
     * <p>
     * Author : gandomi
     */
    @Test
    public void testDataDependencyCheck() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_C_NonAnnotatedDependencies.class)));

        myTestNG.run();

        assertThat("We should have no successful methods of phased Tests", (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_C_NonAnnotatedDependencies.class)).count(),
                is(equalTo(3)));

        assertThat("We should have no successful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

    }

    /****** SHUFFLED ******/

    @Test
    public void testProducer_SHUFFLED() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "false");

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(tla.getPassedTests().size())));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        //STEP 1
        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should  have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 2

        assertThat("We should have executed step2 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step2 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step2 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 3
        assertThat("We should have executed step3 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step3 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step3 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //Context tests
        List<String> l_passedTestNamesContext = context.getPassedTests().getAllResults().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        List<String> l_passsedTestNamesNormal = tla.getPassedTests().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        assertThat("Both lists should have the same values",
                l_passedTestNamesContext.containsAll(l_passsedTestNamesNormal));
    }

    @Test
    public void testProducer_MERGED_SHUFFLED() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        //STEP 1
        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should  have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 2

        assertThat("We should have executed step2 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step2 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step2 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 3
        assertThat("We should have executed step3 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step3 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step3 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //TRIM
        List<String> l_testNames = context.getPassedTests().getAllResults().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        assertThat("The names should have changed", l_testNames,
                Matchers.containsInAnyOrder(
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2" + "__"
                                + Phases.getCurrentPhase().toString(),
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1" + "__"
                                + Phases.getCurrentPhase().toString(),
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0" + "__"
                                + Phases.getCurrentPhase().toString()));
    }

    //TRIM add assertions duplicate
    @Test
    public void testConsumer_SHUFFLED() throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.CONSUMER.activate();

        //Fill the cache
        final Method l_myTest1 = PhasedSeries_F_Shuffle.class.getMethod("step1", String.class);
        final Method l_myTest2 = PhasedSeries_F_Shuffle.class.getMethod("step2", String.class);

        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "A");

        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "AB");
        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "AB");

        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "true");
        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "true");
        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "true");

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(tla.getPassedTests().size())));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Context tests
        List<String> l_passedTestNamesContext = context.getPassedTests().getAllResults().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        List<String> l_passsedTestNamesNormal = tla.getPassedTests().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        assertThat("Both lists should have the same values",
                l_passedTestNamesContext.containsAll(l_passsedTestNamesNormal));
    }

    //TRIM add assertions duplicate
    @Test
    public void testConsumer_MERGED_SHUFFLED() throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.CONSUMER.activate();

        PhasedTestManager.activateMergedReports();

        //Fill the cache
        final Method l_myTest1 = PhasedSeries_F_Shuffle.class.getMethod("step1", String.class);
        final Method l_myTest2 = PhasedSeries_F_Shuffle.class.getMethod("step2", String.class);

        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "A");

        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "AB");
        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "AB");

        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "true");
        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "true");
        PhasedTestManager.storeTestData(PhasedSeries_F_Shuffle.class,
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "true");

        //Add the test context

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should not include the same value as the Passed",
                context.getPassedTests().getAllResults().size(), not(equalTo(tla.getPassedTests().size())));

        assertThat("The Report should have 3 passed entries", context.getPassedTests().getAllResults().size(),
                is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Context tests
        List<String> l_passedTestNamesContext = context.getPassedTests().getAllResults().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        assertThat("The names should have changed", l_passedTestNamesContext, Matchers.containsInAnyOrder(

                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2" + "__"
                        + Phases.getCurrentPhase().toString(),
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1" + "__"
                        + Phases.getCurrentPhase().toString(),
                PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3" + "__"
                        + Phases.getCurrentPhase().toString()));
    }

    @Test
    public void testSHUFFLED_FullMonty() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "false");

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(tla.getPassedTests().size())));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        // ******** CONSUMER ********

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

        ITestContext contextConsumer = tla2.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                contextConsumer.getPassedTests().getAllResults().size(),
                is(equalTo(tla2.getPassedTests().size())));

        assertThat("The Report should also include the same value as the Skipped",
                contextConsumer.getSkippedTests().getAllResults().size(),
                is(equalTo(tla2.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                contextConsumer.getFailedTests().getAllResults().size(),
                is(equalTo(tla2.getFailedTests().size())));

    }

    @Test
    public void testSHUFFLED_MERGED_FullMonty() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Merged Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Merged Phased Tests Producer");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

        // ******** CONSUMER ********

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Merged Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2,
                "Test Repetetive Phased Merged Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

        ITestContext contextConsumer = tla2.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passed",
                contextConsumer.getPassedTests().getAllResults().size(), is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                contextConsumer.getSkippedTests().getAllResults().size(),
                is(equalTo(tla2.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                contextConsumer.getFailedTests().getAllResults().size(),
                is(equalTo(tla2.getFailedTests().size())));

    }

    @Test
    public void testInactive_SHUFFLED() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test
        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //STEP 1
        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)));

        assertThat("We should have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)));

        assertThat("We should  have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

    }

    @Test
    public void testProducer_DefaultDataProvider() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_G_DefaultProvider.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_G_DefaultProvider.class)).count(),
                is(equalTo(2)));

        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)));

    }

    @Test
    public void testProducer_DefaultDataProvider_withoutListener() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_G_DefaultProvider.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_G_DefaultProvider.class)).count(),
                is(equalTo(3)));

        assertThat("We should have executed step1 with the phased default group ",
                tla.getPassedTests().stream().filter(m -> m.getName().equals("step1"))
                        .anyMatch(m -> m.getParameters()[0].equals(PhasedDataProvider.DEFAULT)));

    }

    @Test
    public void testProducer_SingleRun_DataBrokerParameter() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");
        Map<String, String> l_myparameters = new HashMap<>();
        l_myparameters.put(PhasedTestManager.PROP_PHASED_TEST_DATABROKER,
                PhasedDataBrokerTestImplementation.class.getTypeName());
        mySuite.setParameters(l_myparameters);

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_E_FullMonty.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(2)));

        //Check that file was retreived
        File l_storedFile = PhasedTestManager.getDataBroker().fetch(PhasedTestManager.STD_STORE_FILE);
        assertThat("The file should exist", l_storedFile.exists());

        //Since we also store the test status context in the cache we also have the result of the scenario after the phase.
        //I.e. 2 step cache data + 1 scenario state
        //There is a comment line in the beginning
        assertThat("We should have three +1 lines",
                GeneralTestUtils.fetchFileContentDataLines(l_storedFile).size(), Matchers.equalTo(3));

    }

    /**
     * in this example we check the precedence of the Runtime properties over the configuration files
     * <p>
     * Author : gandomi
     */
    @Test
    public void testProducer_SingleRun_DataBrokerSystemProperty() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");
        Map<String, String> l_myparameters = new HashMap<>();
        l_myparameters.put(PhasedTestManager.PROP_PHASED_TEST_DATABROKER, NormalSeries_A.class.getTypeName());
        mySuite.setParameters(l_myparameters);

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_E_FullMonty.class)));

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER,
                PhasedDataBrokerTestImplementation.class.getTypeName());

        myTestNG.run();

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(2)));

        //Check that file was retreived
        File l_storedFile = PhasedTestManager.getDataBroker().fetch(PhasedTestManager.STD_STORE_FILE);
        assertThat("The file should exist", l_storedFile.exists());

        //Since we also store the test status context in the cache we also have the result of the scenario after the phase.
        //I.e. 2 step cache data + 1 scenario state     
        //+ 1 line for the comment
        assertThat("We should have three +1  lines",
                GeneralTestUtils.fetchFileContentDataLines(l_storedFile).size(), Matchers.equalTo(2 + 1));

    }

    /**
     * in this example we check the precedence of the Runtime properties over the configuration files
     * <p>
     * Author : gandomi
     */
    @Test
    public void testProducer_SingleRun_DataBroker_Negative() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");
        Map<String, String> l_myparameters = new HashMap<>();
        l_myparameters.put(PhasedTestManager.PROP_PHASED_TEST_DATABROKER, NormalSeries_A.class.getTypeName());
        mySuite.setParameters(l_myparameters);

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_E_FullMonty.class)));

        assertThrows(TestNGException.class, myTestNG::run);

        assertThat("We should have no successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(0)));

        assertThat("We should have no failed methods of phased Tests",
                (int) tla.getFailedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(0)));

        assertThat("We should have no skipped methods of phased Tests",
                (int) tla.getSkippedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_E_FullMonty.class)).count(),
                is(equalTo(0)));

    }

    /************ Class Level Tests **************/

    @Test
    public void testProducer_classLevel() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_H_SingleClass.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(2)));
    }

    @Test
    public void testProducer_classLevel_MERGED() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_H_SingleClass.class)));

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(2)));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the passed",
                context.getPassedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(0)));
    }

    @Test
    public void testSINGLE_ClassLevelFullMonty() {
        // ***** PRODUCER ****
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        final Class<PhasedSeries_H_SingleClass> l_testClass = PhasedSeries_H_SingleClass.class;

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.PRODUCER.activate();
        myTestNG.run();

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(2)));

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(2)));

        assertThat("We should have no unsuccesful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

        // ***** COSNUMER ****

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Phased Tests");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 1 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(1)));

        assertThat("We should have no unsuccesful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

    }

    @Test
    public void testSHUFFLED_ClassLevelFullMonty() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_H_ShuffledClass> l_testClass = PhasedSeries_H_ShuffledClass.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        // ******** CONSUMER ********

        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

    }

    @Test(description = "Shuffling with an error")
    public void testSHUFFLED_ClassLevelFullMonty_Negative() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing -  Producer");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_H_ShuffledClassWithError> l_testClass = PhasedSeries_H_ShuffledClassWithError.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 3 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(3)));

        //Global
        assertThat("We should have 3 failed tests", tla.getFailedTests().size(), equalTo(2));

        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(1));

        //STEP 1
        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should  have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 2

        assertThat("We should have executed step2 with the phased group 0",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step2 with the phased group 1",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step2 with the phased group 2",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 3
        assertThat("We should have executed step3 with the phased group 0",
                tla.getSkippedTests().stream().filter(m -> m.getName().startsWith("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        // ******** CONSUMER ********

        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2,
                "Automated Suite Phased Testing - Consumer");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        //Global

        assertThat("We should have 1 successful methods of phased Tests", tla2.getPassedTests().size(),
                is(equalTo(1)));

        assertThat("We should have 2  failed tests", tla2.getFailedTests().size(), equalTo(2));
        assertThat("We should have three skipped tests", tla2.getSkippedTests().size(), equalTo(3));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getFailedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getFailedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have NO executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

    }

    @Test(description = "Shuffling with an error")
    public void testSHUFFLED_MERGED_ClassLevelFullMonty_Negative() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - With Failures -  Merged - Producer");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_H_ShuffledClassWithError> l_testClass = PhasedSeries_H_ShuffledClassWithError.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        myTestNG.run();

        assertThat("We should have 3 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(3)));

        //Global
        assertThat("We should have 2 failed tests", tla.getFailedTests().size(), equalTo(2));
        assertThat("We should have one skipped tests", tla.getSkippedTests().size(), equalTo(1));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should only have one Passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("The Report should not contain Skipped tests",
                context.getSkippedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should contain 2 tests marked as Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(2)));

        //Check duration
        // assertThat("The duration should be the sum of all the tests", assertion);

        //STEP 1
        assertThat("We should have executed step1 with the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step1 with the phased group 1",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should  have executed step1 with the phased group 2",
                tla.getPassedTests().stream().filter(m -> m.getName().startsWith("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 2

        assertThat("We should have executed step2 with the phased group 0",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step2 with the phased group 1",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step2 with the phased group 2",
                tla.getFailedTests().stream().filter(m -> m.getName().startsWith("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        //STEP 3
        assertThat("We should have executed step3 with the phased group 0",
                tla.getSkippedTests().stream().filter(m -> m.getName().startsWith("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        // ******** CONSUMER ********

        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2,
                "Automated Suite Phased Testing - Consumer");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        //Global

        assertThat("We should have 1 successful methods of phased Tests", tla2.getPassedTests().size(),
                is(equalTo(1)));

        assertThat("We should have 2  failed tests", tla2.getFailedTests().size(), equalTo(2));
        assertThat("We should have three skipped tests", tla2.getSkippedTests().size(), equalTo(3));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getFailedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getFailedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have NO executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getSkippedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        ITestContext contextConsumer = tla2.getTestContexts().get(0);

        assertThat("The Report should have no Passed tests",
                contextConsumer.getPassedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should have 1 Skipped test",
                contextConsumer.getSkippedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("The Report should contain 2 tests marked as Failed",
                contextConsumer.getFailedTests().getAllResults().size(), is(equalTo(2)));

    }

    @Test
    public void test_WithoutDataProvider() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_K_ShuffledClass_noproviders> l_testClass = PhasedSeries_K_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        // ******** CONSUMER ********

        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

    }

    @Test
    public void testNonPhased_WithoutDataProvider() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_K_ShuffledClass_noproviders> l_testClass = PhasedSeries_K_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should also include the same value as the Passsed",
                context.getPassedTests().getAllResults().size(), is(equalTo(tla.getPassedTests().size())));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

    }

    @Test
    public void testNonPhased_MERGED() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_K_ShuffledClass_noproviders> l_testClass = PhasedSeries_K_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have 3 successful method of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report NOW only have one passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

    }

    @Test
    public void testNonPhasedWithFailure_MERGED() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_H_ShuffledClassWithError> l_testClass = PhasedSeries_H_ShuffledClassWithError.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have 3 successful method of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(1));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(1));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report NOW only have one passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should have no tests marked as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should test marked as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("We should have the correct message suffix",
                context.getFailedTests().getAllResults().iterator().next().getThrowable().getMessage(),
                Matchers.endsWith(Phases.getCurrentPhase().toString() + "]"));

    }

    /******** GROUP Testing ********/

    @Test
    public void testProducer_classGroupLevel() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - Groups ");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests - Groups");

        //Define packages
        List<XmlPackage> l_packages = new ArrayList<>();
        l_packages.add(new XmlPackage("com.adobe.campaign.tests.integro.phased.data"));
        myTest.setXmlPackages(l_packages);

        myTest.addIncludedGroup("UPGRADE");

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 2 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(2)));
    }

    @Test
    public void testProducer_produceWithKey() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing Produce");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Simple Phased Tests - produce");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(PhasedSeries_I_SingleClassProduceTest.class)));

        Phases.PRODUCER.activate();

        myTestNG.run();

        final Properties phaseContext = PhasedTestManager.importPhaseData();
        assertThat("The phase cache should have stored the correct value",
                phaseContext.containsKey(PhasedSeries_I_SingleClassProduceTest.class.getTypeName() + "("
                        + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")"
                        + PhasedTestManager.STD_KEY_CLASS_SEPARATOR + "MyVal"));

        assertThat("We should have no failed methods of phased Tests", tla.getFailedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 1 successful method of phased Tests", (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_I_SingleClassProduceTest.class)).count(),
                is(equalTo(1)));
    }

    @Test
    public void testProducer_produceWithKeyCascaded() {

        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - Produce with Key");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite,
                "Test Repetetive Phased Tests Producer - Produce with Key");

        final Class<PhasedSeries_I_ShuffledProduceKey> l_testClass = PhasedSeries_I_ShuffledProduceKey.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

    }

    /*   ************* Test The listener **************/

    /*
     * Testing the AppendShuffledGroupMethod
     * <p>
     * Author : gandomi
     */
    @Test
    public void testAppendShuffledGroupName() throws NoSuchMethodException, SecurityException {
        final Method l_myTestNoArgs = PhasedSeries_H_SingleClass.class.getMethod("step2", String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        //TODO replace, since this is invalid in later versions of Mockito
        //Mockito.when(l_itr.getMethod()).thenThrow(NoSuchFieldException.class);
        Mockito.when(l_itr.getMethod()).thenAnswer(invocation -> {
            throw new NoSuchFieldException("Mocked Exception");
        });
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "A" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestNoArgs);

        PhasedTestListener ptl = new PhasedTestListener();
        assertThrows(PhasedTestException.class, () -> ptl.appendShuffleGroupToName(l_itr));

    }

    @Test
    public void testAppendShuffledGroupName2() throws NoSuchMethodException, SecurityException {
        final Method l_myTestNoArgs = PhasedSeries_H_SingleClass.class.getMethod("step2", String.class);

        ITestResult l_itr = Mockito.mock(ITestResult.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        //TODO replace, since this is invalid in later versions of Mockito
        //Mockito.when(l_itr.getMethod()).thenThrow(IllegalAccessException.class);
        Mockito.when(l_itr.getMethod()).thenAnswer(invocation -> {
            throw new IllegalAccessException("Mocked Exception");
        });
        Mockito.when(l_itr.getParameters()).thenReturn(new Object[] { "A" });
        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_myTestNoArgs);

        PhasedTestListener ptl = new PhasedTestListener();
        assertThrows(PhasedTestException.class, () -> ptl.appendShuffleGroupToName(l_itr));

    }

    @Test
    public void testSHUFFLED_ClassInAClass() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_J_ShuffledClassInAClass> l_testClass = PhasedSeries_J_ShuffledClassInAClass.class;
        myTest.setXmlClasses(
                Arrays.asList(new XmlClass(l_testClass), new XmlClass(PhasedSeries_H_ShuffledClass.class)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(12)));

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        // ******** CONSUMER ********
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Repetetive Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

    }

    /************* Multi Data Provider ******************/
    @Test
    public void testMultiDataProvider() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing multi dp");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_L_ShuffledDP> l_testClass = PhasedSeries_L_ShuffledDP.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));
    }

    @Test
    public void testMultiDataProviderInClass() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing multi dp");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_L_ShuffledDPSimple> l_testClass = PhasedSeries_L_ShuffledDPSimple.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));
    }

    @Test
    public void testMultiDataProviderInClass_MERGED() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing multi dp");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_L_ShuffledDPSimple> l_testClass = PhasedSeries_L_ShuffledDPSimple.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        myTestNG.run();

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("We should have 6 successful methods of phased Tests",
                (int) context.getPassedTests().getAllResults().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(4)));

        //Global
        assertThat("We should have no failed tests", context.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", context.getSkippedTests().size(), equalTo(0));

        List<String> l_passedTestNamesContext = context.getPassedTests().getAllResults().stream()
                .map(m -> m.getMethod().getMethodName()).collect(Collectors.toList());

        assertThat("The names should have changed", l_passedTestNamesContext,

                Matchers.containsInAnyOrder(
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_0" + "__" + "M" + "__"
                                + Phases.getCurrentPhase().toString(),
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_0" + "__" + "Z" + "__"
                                + Phases.getCurrentPhase().toString(),
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_1" + "__" + "M" + "__"
                                + Phases.getCurrentPhase().toString(),
                        PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_1" + "__" + "Z" + "__"
                                + Phases.getCurrentPhase().toString()));
    }

    //Related to issue #27 errors when we have no arguments
    @Test
    public void testInsufficientArguments_Negative() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing multi dp");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_L_ShuffledNoArgs> l_testClass = PhasedSeries_L_ShuffledNoArgs.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        assertThrows(PhasedTestConfigurationException.class, myTestNG::run);

    }

    //Related to issue #28 errors when we have the wrong number of arguments
    @Test
    public void testInsufficientArguments_NegativeWrongArgs() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing multi dp");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final Class<PhasedSeries_L_ShuffledWrongArgs> l_testClass = PhasedSeries_L_ShuffledWrongArgs.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        assertThrows(PhasedTestConfigurationException.class, myTestNG::run);

    }

    /************** Testing issue #9 ******************/

    @Test
    public void testConsumer_selectionBasedOnProducer_Deactivated() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        Phases.CONSUMER.activate();
        Properties phasedCache = PhasedTestManager.phasedCache;
        phasedCache.put(PhasedSeries_H_SingleClass.class.getTypeName() + ".step2("
                + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")", "AB");

        PhasedTestManager
                .storeTestData(PhasedSeries_H_SingleClass.class, PhasedTestManager.STD_PHASED_GROUP_SINGLE, "true");

        assertThat("We should not be in the SELECT_BY_PRODUCER mode",
                !PhasedTestManager.isTestsSelectedByProducerMode());

        myTestNG.run();

        assertThat("We should not be in the SELECT_BY_PRODUCER mode",
                !PhasedTestManager.isTestsSelectedByProducerMode());

        assertThat(
                "Since we have not passed the test group PHASED_PRODUCED_TESTS, we should have no successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(0)));
    }

    /**
     * Starting from here we use he properties file as a test selector.
     * <p>
     * Author : gandomi
     */
    @Test
    public void testConsumer_selectionBasedOnProducerFile() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        //This activates the selection
        myTest.addIncludedGroup(PhasedTestManager.STD_GROUP_SELECT_TESTS_BY_PRODUCER);

        Phases.CONSUMER.activate();
        Properties phasedCache = PhasedTestManager.phasedCache;
        phasedCache.put(PhasedSeries_H_SingleClass.class.getTypeName() + ".step2("
                + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")", "AB");

        PhasedTestManager
                .storeTestData(PhasedSeries_H_SingleClass.class, PhasedTestManager.STD_PHASED_GROUP_SINGLE, "true");

        assertThat("We should not be in the SELECT_BY_PRODUCER mode",
                !PhasedTestManager.isTestsSelectedByProducerMode());

        myTestNG.run();

        assertThat("We should be in the SELECT_BY_PRODUCER mode", PhasedTestManager.isTestsSelectedByProducerMode());

        assertThat("The number of tests should not have changed",
                tla.getTestContexts().get(0).getSuite().getXmlSuite().getTests().size(), equalTo(1));

        assertThat("We should have 1 successful methods of phased Tests", (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(1)));

    }

    /**
     * In this test the same phased test is select by direct group selection and by selection through producer what we
     * want is that this test should only be executed once
     */
    @Test
    public void testSelectedByProducer_groupSelected() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        //Define packages
        List<XmlPackage> l_packages = new ArrayList<>();
        l_packages.add(new XmlPackage("com.adobe.campaign.tests.integro.phased.data"));
        myTest.setXmlPackages(l_packages);

        myTest.addIncludedGroup("PROPERTIES_SELECT");

        Phases.CONSUMER.activate();

        //Fill producer data
        Properties phasedCache = PhasedTestManager.phasedCache;
        phasedCache.put(PhasedSeries_H_SingleClass.class.getTypeName() + ".step2("
                + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")", "AB");

        PhasedTestManager
                .storeTestData(PhasedSeries_H_SingleClass.class, PhasedTestManager.STD_PHASED_GROUP_SINGLE, "true");

        myTestNG.run();

        assertThat("We should have 1 successful methods of phased Tests", (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(1)));
    }

    /**
     * Testing how the feature of selection by properties works if we have many tests. In this example we have two
     * tests. The first one executes a normal test. The second one has the execute by producer activated
     */
    @Test
    public void testSelectByProperties_multiTest() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Test1 - include a simple test
        XmlTest myTest1 = TestTools.attachTestToSuite(mySuite, "Two tests in suite #1. Normal test");

        //Define packages
        List<XmlPackage> l_packages = new ArrayList<>();
        l_packages.add(new XmlPackage("com.adobe.campaign.tests.integro.phased.data"));
        myTest1.setXmlPackages(l_packages);

        myTest1.addIncludedGroup("PROPERTIES_TEST2");

        //Test 2 - Include the phased test to be selected from producer

        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite, "Two tests in suite. Contains phased test.");

        //This activates the selection for selection by Producerr
        myTest2.addIncludedGroup(PhasedTestManager.STD_GROUP_SELECT_TESTS_BY_PRODUCER);

        Phases.CONSUMER.activate();

        //Fill producer data
        Properties phasedCache = PhasedTestManager.phasedCache;
        phasedCache.put(PhasedSeries_H_SingleClass.class.getTypeName() + ".step2("
                + PhasedTestManager.STD_PHASED_GROUP_SINGLE + ")", "AB");

        PhasedTestManager
                .storeTestData(PhasedSeries_H_SingleClass.class, PhasedTestManager.STD_PHASED_GROUP_SINGLE, "true");

        myTestNG.run();

        assertThat("We should have a total of 2 successful methods", tla.getPassedTests().size(), is(equalTo(2)));

        assertThat("We should have 1 successful methods of phased Tests", (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(PhasedSeries_H_SingleClass.class)).count(),
                is(equalTo(1)));
    }
}
