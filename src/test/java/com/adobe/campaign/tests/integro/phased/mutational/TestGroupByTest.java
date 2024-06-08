/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.MutationListener;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.mutational.data.permutational.MultipleProducerConsumer;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild1;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild2;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TestGroupByTest {

    @Test
    public void testNewOrder() {
        //PRODUCER
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PRODUCER.name());

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

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PRODUCER.name());

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
}
