/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.MutationListener;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.data.events.MyNonInterruptiveEvent;
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
import org.testng.annotations.BeforeMethod;
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
    public void testNewOrder() {
        //PRODUCER
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        Phases.PRODUCER.activate();

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
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild1.class.getTypeName())).collect(Collectors.toList()).size(),
                Matchers.equalTo(2));

        assertThat("We should have no executions for the phased group 0",
                tla.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild2.class.getTypeName())).collect(Collectors.toList()).size(),
                Matchers.equalTo(3));

        //Add with consumer

        // Rampup
        TestNG myTestNGC = TestTools.createTestNG();
        TestListenerAdapter tlaC = TestTools.fetchTestResultsHandler(myTestNGC);

        Phases.CONSUMER.activate();

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

        Map<String, PhasedTestManager.ScenarioContextData> x =PhasedTestManager.getScenarioContext();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tlaC.getPassedTests().size(),
                is(equalTo(5)));

        assertThat("We should have no executions for the phased group 0",
                tlaC.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild1.class.getTypeName())).collect(Collectors.toList()).size(),
                Matchers.equalTo(2));

        assertThat("We should have no executions for the phased group 0",
                tlaC.getPassedTests().stream().filter(m -> m.getInstanceName().equals(PhasedChild2.class.getTypeName())).collect(Collectors.toList()).size(),
                Matchers.equalTo(3));
    }


    @Test
    public void testPermutational() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PERMUTATIONAL.name());

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

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PERMUTATIONAL.name());

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

        Phases.ASYNCHRONOUS.activate();
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

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }
}
