/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.demo.DemoShuffled;
import com.adobe.campaign.tests.integro.phased.demo.ShoppingBasket2;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

public class TestPhased_Demos {
    @BeforeMethod
    public void resetVariables() {

        PhasedTestManager.clearCache();
        ConfigValueHandlerPhased.resetAllValues();

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.MergedReportData.resetReport();

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.SCENARIO_NAME)),
                new LinkedHashSet<>());
    }

    @Test
    public void runDemoCode() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Demo Phased Suite - Producer");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Demo Phased Test - Producer");

        final Class<DemoShuffled> l_testClass = DemoShuffled.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        // ******** CONSUMER ********

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Demo Phased Suite - Consumer");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2,
                "Demo Phased Test - Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new

                XmlClass(l_testClass)));

        myTestNG2.run();

    }

    @Test
    public void runDemoCodeShoppingBasket() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Demo Phased Suite - Producer");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Demo Phased Test - Producer");

        final Class<ShoppingBasket2> l_testClass = ShoppingBasket2.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();

        PhasedTestManager.activateMergedReports();

        myTestNG.run();

        // ******** CONSUMER ********

        //Clear data
        PhasedTestManager.clearCache();
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Demo Phased Suite - Consumer");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2,
                "Demo Phased Test - Shopping Basket - Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new
                XmlClass(l_testClass)));

        myTestNG2.run();
        System.out.println("Finished");
    }

}
