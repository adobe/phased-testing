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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertThrows;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_SingleClass;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_AfterPhase_onBeforeSuite;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_BeforePhase_AfterSuite;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_BeforePhase_BeforeSuite;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_BeforePhase_BeforeSuite_CONSUMER;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_BeforePhase_onAfterSuite;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_ShuffledClass_changesAter;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_ShuffledClass_noproviders;
import com.adobe.campaign.tests.integro.phased.data.befaft.PhasedSeries_M_Simple_BeforeSuite;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;

public class TestPhased_BEFORE_AFTER {
    @BeforeMethod
    public void resetVariables() {

        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue = 0;
        PhasedSeries_M_BeforePhase_BeforeSuite_CONSUMER.beforeValue = 0;
        PhasedSeries_M_Simple_BeforeSuite.beforeValue = 0;
        PhasedSeries_M_BeforePhase_AfterSuite.afterValue = 0;

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(),
                new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));
    }

    /************************* BeforePhase ****************************/
    @Test
    public void testBEFORE_PHASE_testSimpleSuiteTests_Producer() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing invocation - PRODUCER");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite.class),
                new XmlClass(PhasedSeries_M_Simple_BeforeSuite.class)));

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        assertThat("Thhe before suite shouldd have been invoked only once",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(13));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(3));
        assertThat("We should have one passsed test", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have one passsed test", l_context.getFailedTests().size(), equalTo(0));
        assertThat("We should have executed the before suite once",
                l_context.getPassedConfigurations().size(), equalTo(2));
        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("simpleBeforeSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));

    }

    @Test
    public void testBEFORE_PHASE_testSimpleSuiteTests_Consumer()
            throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing invocation - CONSUMER");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite.class),
                new XmlClass(PhasedSeries_M_Simple_BeforeSuite.class)));

        PhasedTestManager.activateMergedReports();

        Phases.CONSUMER.activate();

        PhasedTestManager.activateMergedReports();

        final Method l_myTest1 = PhasedSeries_M_ShuffledClass_noproviders.class.getMethod("step1",
                String.class);
        final Method l_myTest2 = PhasedSeries_M_ShuffledClass_noproviders.class.getMethod("step2",
                String.class);

        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "A");

        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "AB");
        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "AB");
        
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", true);
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", true);
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", true);


        myTestNG.run();

        assertThat("Thhe before suite shouldd have been invoked only once",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(13));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(3));
        assertThat("We should have one passsed test", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have one passsed test", l_context.getFailedTests().size(), equalTo(0));
        assertThat("We should have executed the before suite thrice",
                l_context.getPassedConfigurations().size(), Matchers.greaterThanOrEqualTo(2));
        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("simpleBeforeSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));

    }

    @Test
    public void testBEFORE_PHASE_testSimpleSuiteTests_failBeforePhase() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing invocation - Failure at BeforePhase");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite.class),
                new XmlClass(PhasedSeries_M_Simple_BeforeSuite.class)));

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue = 1;

        myTestNG.run();

        assertThat("Thhe before suite shouldd have been invoked only once",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(14));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(0));
        assertThat("We should have one passsed test", l_context.getSkippedTests().size(), equalTo(3));
        assertThat("We should have one passsed test", l_context.getFailedTests().size(), equalTo(0));
        assertThat("We should have executed the before suite once",
                l_context.getPassedConfigurations().size(), equalTo(0));
        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(1));
        assertThat("We should have executed the before suite once",
                l_context.getSkippedConfigurations().size(), equalTo(1));
        assertThat("Our beforesuite sshould have been invoke one", l_context.getFailedConfigurations()
                .getAllResults().stream().allMatch(m -> m.getName().equals("beforePhasedSuite")));

    }

    /**
     * By default when not in phase the before/after phase should not be
     * activated
     *
     * Author : gandomi
     *
     *
     */

    @Test
    public void testBEFORE_PHASE_testSimpleSuiteTests_NoPhase() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - NoPhase By defaultskipping beforePhase");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods skipping beforePhase");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite.class),
                new XmlClass(PhasedSeries_M_Simple_BeforeSuite.class)));

        PhasedTestManager.activateMergedReports();

        assertThat("Reset must have worked", PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        myTestNG.run();

        assertThat("The before suite should not have been invoked",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(1));
        assertThat("We should have no skipped tests", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have no failed tests", l_context.getFailedTests().size(), equalTo(0));

        assertThat("We should have executed a before suite once with success",
                l_context.getPassedConfigurations().size(), equalTo(1));
        assertThat("We should have no failed configurations", l_context.getFailedConfigurations().size(),
                equalTo(0));
        assertThat("Our Phased beforesuite should not have been invoked",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("simpleBeforeSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));

    }

    /**
     * In this case the BeforePhase is for CONSUMER. We execute our test as
     * PRODUCER. In this case the BeforePhase will NOT be invoked
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testBEFORE_PHASE_OnlyConsumer_testSimpleSuiteTests_Producer() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "BeforePhase for CONSUMER Only - executing as PRODUCER");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_Simple_BeforeSuite.class),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite_CONSUMER.class)));

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);
        myTestNG.run();

        assertThat("The before suite should not have been invoked",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have three passsed tests", l_context.getPassedTests().size(), equalTo(3));
        assertThat("We should have no skipped tests", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have no failed tests", l_context.getFailedTests().size(), equalTo(0));

        assertThat("We should have executed the before suite once",
                l_context.getPassedConfigurations().size(), equalTo(1));

        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(0));

        assertThat("Our before suite should have not been invoked",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuiteConsumer"))
                        .collect(Collectors.toList()).size(),
                equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("simpleBeforeSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));

    }

    /**
     * In this case the BeforePhase is for CONSUMER. We execute our test as
     * COSUMER. In this case the BeforePhase will be invoked
     *
     * Author : gandomi
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    @Test
    public void testBEFORE_PHASE_OnlyConsumer_testSimpleSuiteTests_Consumer()
            throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "BeforePhase for CONSUMER Only - executing as CONSUMER");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_noproviders> l_testClass = PhasedSeries_M_ShuffledClass_noproviders.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_BeforeSuite_CONSUMER.class)));

        Phases.CONSUMER.activate();

        PhasedTestManager.activateMergedReports();

        final Method l_myTest1 = PhasedSeries_M_ShuffledClass_noproviders.class.getMethod("step1",
                String.class);
        final Method l_myTest2 = PhasedSeries_M_ShuffledClass_noproviders.class.getMethod("step2",
                String.class);

        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "A");
        PhasedTestManager.storeTestData(l_myTest1, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", "A");

        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", "AB");
        PhasedTestManager.storeTestData(l_myTest2, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", "AB");
        
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0", true);
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1", true);
        PhasedTestManager.storeTestData(PhasedSeries_M_ShuffledClass_noproviders.class, PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2", true);

        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);
        myTestNG.run();

        assertThat("The before suite should not have been invoked",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have three passsed tests", l_context.getPassedTests().size(), equalTo(3));
        assertThat("We should have no skipped tests", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have no failed tests", l_context.getFailedTests().size(), equalTo(0));

        assertThat("We should have executed the before suite once",
                l_context.getPassedConfigurations().size(), equalTo(1));

        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(0));

        assertThat("Our before suite should have not been invoked",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuiteConsumer"))
                        .collect(Collectors.toList()).size(),
                equalTo(1));

    }

    @Test
    public void testAfter_PHASE_testSimpleSuiteTests_Producer() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing invocation AfterPhase - PRODUCER");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods");

        final Class<PhasedSeries_M_ShuffledClass_changesAter> l_testClass = PhasedSeries_M_ShuffledClass_changesAter.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_AfterSuite.class)));

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        assertThat("Thhe before suite shouldd have been invoked only once",
                PhasedSeries_M_BeforePhase_AfterSuite.afterValue, equalTo(24));
        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(3));
        assertThat("We should have one passsed test", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have one passsed test", l_context.getFailedTests().size(), equalTo(0));
        assertThat("We should have executed the before suite once",
                l_context.getPassedConfigurations().size(), equalTo(1));
        assertThat("We should have executed the before suite once",
                l_context.getFailedConfigurations().size(), equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("afterPhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(1));

    }

    /**
     * By default when not in phase the before/after phase should not be
     * activated
     *
     * Author : gandomi
     *
     *
     */

    @Test
    public void testAfter_PHASE_testSimpleSuiteTests_NoPhase() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - NoPhase By defaultskipping beforePhase");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods skipping beforePhase");

        final Class<PhasedSeries_M_ShuffledClass_changesAter> l_testClass = PhasedSeries_M_ShuffledClass_changesAter.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_AfterSuite.class)));

        PhasedTestManager.activateMergedReports();

        assertThat("Reset must have worked", PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        myTestNG.run();

        assertThat("The before suite should not have been invoked",
                PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        ITestContext l_context = tla.getTestContexts().get(0);
        assertThat("We should have one passsed test", l_context.getPassedTests().size(), equalTo(1));
        assertThat("We should have no skipped tests", l_context.getSkippedTests().size(), equalTo(0));
        assertThat("We should have no failed tests", l_context.getFailedTests().size(), equalTo(0));

        assertThat("We should not have executed an after suite", l_context.getPassedConfigurations().size(),
                equalTo(0));
        assertThat("We should have no failed configurations", l_context.getFailedConfigurations().size(),
                equalTo(0));
        assertThat("Our Phased beforesuite should not have been invoked",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("beforePhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(0));
        assertThat("Our beforesuite should have been invoked once",
                l_context.getPassedConfigurations().getAllResults().stream()
                        .filter(m -> m.getName().equals("afterPhasedSuite")).collect(Collectors.toList())
                        .size(),
                equalTo(0));

    }

    @Test
    public void testBeforePhaseAfterSuite_Negative() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - BeforePhase set on an AfterSuite - NEGATIVE");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods skipping beforePhase");

        final Class<PhasedSeries_M_ShuffledClass_changesAter> l_testClass = PhasedSeries_M_ShuffledClass_changesAter.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_BeforePhase_onAfterSuite.class)));

        PhasedTestManager.activateMergedReports();

        assertThat("Reset must have worked", PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        assertThrows(PhasedTestConfigurationException.class, () -> myTestNG.run());
    }

    @Test
    public void testAfterPhaseBeforeSuite_Negative() {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG,
                "Automated Suite Phased Testing - BeforePhase set on an AfterSuite - NEGATIVE");

        // Add listeners
        mySuite.addListener(PhasedTestListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test config methods skipping beforePhase");

        final Class<PhasedSeries_M_ShuffledClass_changesAter> l_testClass = PhasedSeries_M_ShuffledClass_changesAter.class;
        myTest.setXmlClasses(Arrays.asList(new XmlClass(l_testClass),
                new XmlClass(PhasedSeries_M_AfterPhase_onBeforeSuite.class)));

        PhasedTestManager.activateMergedReports();

        assertThat("Reset must have worked", PhasedSeries_M_BeforePhase_BeforeSuite.beforeValue, equalTo(0));

        assertThrows(PhasedTestConfigurationException.class, () -> myTestNG.run());
    }

}
