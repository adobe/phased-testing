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

import com.adobe.campaign.tests.integro.phased.demo.DemoShuffled;
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

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

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

}
