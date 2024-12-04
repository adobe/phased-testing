/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.data.events.MyNonInterruptiveEvent;
import com.adobe.campaign.tests.integro.phased.data.events.NIEMutationalSynchronousEvent;
import com.adobe.campaign.tests.integro.phased.data.events.NIEMutationalSynchronousEventWithException;
import com.adobe.campaign.tests.integro.phased.data.mutational.TestMutationalNIE_Synchroneous;
import com.adobe.campaign.tests.integro.phased.mutational.data.erroneous.IE_Shuffled_ErrorAssertion1;
import com.adobe.campaign.tests.integro.phased.mutational.data.erroneous.IE_Shuffled_ErrorOtherNonAssertive1;
import com.adobe.campaign.tests.integro.phased.mutational.data.ie.MutationalTestSingleRun;
import com.adobe.campaign.tests.integro.phased.mutational.data.nie.TestMutationalShuffled_eventPassedAsExecutionVariable;
import com.adobe.campaign.tests.integro.phased.mutational.data.permutational.MultipleProducerConsumer;
import com.adobe.campaign.tests.integro.phased.mutational.data.permutational.ShoppingCartDemo;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild1;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild2;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MutationalTests {
    @BeforeClass
    @AfterMethod
    public void resetVariables() {

        ConfigValueHandlerPhased.resetAllValues();

        PhasedEventManager.resetEvents();

        PhasedTestManager.clearCache();

        PhasedTestManager.deactivateTestSelectionByProducerMode();

        PhasedTestManager.MergedReportData.resetReport();

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

        //PhasedEventManager.stopEventManager();
    }

    @Test
    public void testDefault() {
        //PRODUCER
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final XmlPackage l_testPkg = new XmlPackage("com.adobe.campaign.tests.integro.phased.mutational.data.simple1");
        myTest.setPackages(Collections.singletonList(l_testPkg));

        myTest.addIncludedGroup("aaa");

        // Add package to test

        myTestNG.run();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(2)));

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild1.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(1));

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild2.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(1));
    }

    @Test
    public void testDefaultWithAssertionError() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        var l_testClass = IE_Shuffled_ErrorAssertion1.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have no successful methods of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 1 failed method of phased Tests",
                (int) tla.getFailedTests().size(),
                is(equalTo(1)));

        assertThat("We should have no skipped methods of phased Tests",
                (int) tla.getSkippedTests().size(),
                is(equalTo(0)));

        assertThat("We should have no executions for the phased group 0",
                tla.getFailedTests().stream().filter(m -> m.getInstanceName().equals(l_testClass.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(1));

        assertThat("The exception should be an assertion exception", tla.getFailedTests().get(0).getThrowable(),
                Matchers.instanceOf(AssertionError.class));

    }

    @Test
    public void testDefaultWithException() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        var l_testClass = IE_Shuffled_ErrorOtherNonAssertive1.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have no successful methods of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(0)));

        assertThat("We should have 1 failed method of phased Tests",
                (int) tla.getFailedTests().size(),
                is(equalTo(1)));

        assertThat("We should have no skipped methods of phased Tests",
                (int) tla.getSkippedTests().size(),
                is(equalTo(0)));

        assertThat("We should have no executions for the phased group 0",
                tla.getFailedTests().stream().filter(m -> m.getInstanceName().equals(l_testClass.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(1));

        assertThat("The exception should be an assertion exception", tla.getFailedTests().get(0).getThrowable(),
                Matchers.instanceOf(IllegalArgumentException.class));

    }

    @Test
    public void testInterruptiveEvent() {
        //PRODUCER
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ExecutionMode.INTERRUPTIVE.activate("PRODUCER");

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final XmlPackage l_testPkg = new XmlPackage("com.adobe.campaign.tests.integro.phased.mutational.data.simple1");
        myTest.setPackages(Collections.singletonList(l_testPkg));

        myTest.addIncludedGroup("aaa");

        // Add package to test

        myTestNG.run();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(5)));

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild1.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(2));

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild2.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(3));

        //Add with consumer

        // Rampup
        TestNG myTestNGC = TestTools.createTestNG();
        TestListenerAdapter tlaC = TestTools.fetchTestResultsHandler(myTestNGC);

        ExecutionMode.INTERRUPTIVE.activate("CONSUMER");

        // Define suites
        XmlSuite mySuiteC = TestTools.addSuitToTestNGTest(myTestNGC, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuiteC.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTestC = TestTools.attachTestToSuite(mySuiteC, "Test Repetetive Phased Tests Consumer");

        final XmlPackage l_testPkgC = new XmlPackage("com.adobe.campaign.tests.integro.phased.mutational.data.simple1");
        myTestC.setPackages(Collections.singletonList(l_testPkgC));

        myTestC.addIncludedGroup("aaa");

        // Add package to test

        myTestNGC.run();

        Map<String, PhasedTestManager.ScenarioContextData> x = PhasedTestManager.getScenarioContext();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tlaC.getPassedTests().size(),
                is(equalTo(5)));

        assertThat("We should have no executions for the phased group 0",
                tlaC.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild1.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(2));

        assertThat("We should have no executions for the phased group 0",
                tlaC.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild2.class.getTypeName()))
                        .collect(Collectors.toList()).size(),
                Matchers.equalTo(3));
    }

    @Test
    public void testInterruptiveSingleRunEvent() {

        //  ***** PRODUCER ****
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased Tests");

        Class<MutationalTestSingleRun> l_testClass = MutationalTestSingleRun.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.PRODUCER.activate();
        myTestNG.run();

        assertThat("We should have 2 successful methods of phased Tests",
                (int) tla.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

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
        mySuite2.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Phased Tests");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 1 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream()
                        .filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

        assertThat("We should have no unsuccesful methods of phased Tests",
                tla.getFailedTests().size() + tla.getSkippedTests().size(), is(equalTo(0)));

    }

    @Test
    public void testPermutational() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ExecutionMode.PERMUTATIONAL.activate();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Permutational Tests ");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(MultipleProducerConsumer.class)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(2)));

    }

    @Test
    public void testPermutationalDemo() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ExecutionMode.PERMUTATIONAL.activate();

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuiteC.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Permutational Tests ");

        myTest.setXmlClasses(Collections.singletonList(new XmlClass(ShoppingCartDemo.class)));

        // Add package to test

        myTestNG.run();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(3)));
    }

    /**
     * This is a test for non-intyerruptive events in shuffled classes
     */
    @Test
    public void testNonInterruptive_ParellelConfiguredAsExecutionVariable_Shuffled_Ordered() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestMutationalShuffled_eventPassedAsExecutionVariable> l_testClass = TestMutationalShuffled_eventPassedAsExecutionVariable.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        ExecutionMode.NON_INTERRUPTIVE.activate("33");
        ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());
        ConfigValueHandlerPhased.PHASED_TEST_DETECT_ORDER.activate("true");

        myTestNG.run();

        // assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 3 successful executions of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (2 x phase groups)",
                PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }


    /**
     * This is a test for non-intyerruptive events in shuffled classes. Using the legacy annotations
     */
    @Test
    public void testNonInterruptive_23_Targeted() {
        NIEMutationalSynchronousEvent.START_STEP_VALUE = 2;
        NIEMutationalSynchronousEvent.WTF_STEP_VALUE = 7;
        NIEMutationalSynchronousEvent.TDE_STEP_VALUE = 23;

        //Reset
        TestMutationalNIE_Synchroneous.testElement = 3;

        //The WTF should be finished before the start of step 2
        TestMutationalNIE_Synchroneous.expectedStep2Value = NIEMutationalSynchronousEvent.WTF_STEP_VALUE;

        TestMutationalNIE_Synchroneous.expectedStep3Value = NIEMutationalSynchronousEvent.TDE_STEP_VALUE;
        TestMutationalNIE_Synchroneous.expectedStep3EndResult = NIEMutationalSynchronousEvent.WTF_STEP_VALUE + TestMutationalNIE_Synchroneous.testElement;


        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestMutationalNIE_Synchroneous > l_testClass = TestMutationalNIE_Synchroneous.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        ExecutionMode.NON_INTERRUPTIVE.activate("23");
        ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.activate(NIEMutationalSynchronousEvent.class.getTypeName());
        ConfigValueHandlerPhased.EVENT_TARGET.activate(l_testClass.getTypeName() + "#step2");

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled",
                !PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        tla.getFailedTests().forEach(t -> System.out.println(t.getThrowable().getMessage()));

        assertThat("We should have 1 successful scenarios of mutational Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)",
                PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
    }

    /**
     * This is a test for non-interruptive events in shuffled classes. Using the legacy annotations
     */
    @Test
    public void testNonInterruptive_33_Targetted() {
        NIEMutationalSynchronousEvent.START_STEP_VALUE = 2;
        NIEMutationalSynchronousEvent.WTF_STEP_VALUE = 7;
        NIEMutationalSynchronousEvent.TDE_STEP_VALUE = 23;

        //Reset
        TestMutationalNIE_Synchroneous.testElement = 3;

        //The WTF should be finished before the start of step 2
        TestMutationalNIE_Synchroneous.expectedStep2Value = NIEMutationalSynchronousEvent.START_STEP_VALUE;

        TestMutationalNIE_Synchroneous.expectedStep3Value = NIEMutationalSynchronousEvent.TDE_STEP_VALUE;
        TestMutationalNIE_Synchroneous.expectedStep3EndResult = TestMutationalNIE_Synchroneous.expectedStep2Value + TestMutationalNIE_Synchroneous.testElement;

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestMutationalNIE_Synchroneous> l_testClass = TestMutationalNIE_Synchroneous.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        ExecutionMode.NON_INTERRUPTIVE.activate("33");
        ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.activate(NIEMutationalSynchronousEvent.class.getTypeName());
        ConfigValueHandlerPhased.EVENT_TARGET.activate(TestMutationalNIE_Synchroneous.class.getTypeName() + "#step2");

        myTestNG.run();

        assertThat("We should not be in non-interruptive mode shuffled",
                !PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        tla.getFailedTests().forEach(t -> System.out.println(t.getThrowable().getMessage()));

        assertThat("We should have 1 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)",
                PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
    }


    /**
     * This is a test for non-interruptive events in shuffled classes. Using the legacy annotations
     */

    @Test
    public void testNonInterruptive_33_Targetted_ExceptionInStartEvent() {
        NIEMutationalSynchronousEventWithException.START_STEP_VALUE = 2;
        NIEMutationalSynchronousEventWithException.WTF_STEP_VALUE = 7;
        NIEMutationalSynchronousEventWithException.TDE_STEP_VALUE = 23;
        NIEMutationalSynchronousEventWithException.exceptionPlace = 1;

        //Reset
        TestMutationalNIE_Synchroneous.testElement = 3;

        //The WTF should be finished before the start of step 2
        TestMutationalNIE_Synchroneous.expectedStep2Value = NIEMutationalSynchronousEventWithException.START_STEP_VALUE;

        TestMutationalNIE_Synchroneous.expectedStep3Value = NIEMutationalSynchronousEventWithException.TDE_STEP_VALUE;
        TestMutationalNIE_Synchroneous.expectedStep3EndResult = TestMutationalNIE_Synchroneous.expectedStep2Value + TestMutationalNIE_Synchroneous.testElement;

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestMutationalNIE_Synchroneous> l_testClass = TestMutationalNIE_Synchroneous.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        ExecutionMode.NON_INTERRUPTIVE.activate("33");
        ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.activate(NIEMutationalSynchronousEventWithException.class.getTypeName());
        ConfigValueHandlerPhased.EVENT_TARGET.activate(TestMutationalNIE_Synchroneous.class.getTypeName() + "#step2");

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled",
                !PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        tla.getFailedTests().forEach(t -> System.out.println(t.getThrowable().getMessage()));

        assertThat("Only step 1 should have succeeded",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(1)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)",
                PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
    }



}
