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

import com.adobe.campaign.tests.integro.phased.bugs.BeforeAfterPhases;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * This is for reproducing issues
 */
public class IssueValidationsTests {
    @BeforeMethod
    public void cleanUp() {
        TestPhased tp = new TestPhased();
        tp.resetVariables();
    }

    /**
     * Following issue 89 : AfterPhase(CONSUMER) - AfterClass seems to be executing on Producer
     * We need to see if the afterPhase(concerns CONSUMER) is not executed during PRODUCER
     */
    @Test
    public void issueWithAfterPhaseAfterClass_notBeingCorrectlyRestrictive_89_A(){
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing : Issue #89");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests Issue #89");

        final Class<BeforeAfterPhases.Issue89> l_testClass = BeforeAfterPhases.Issue89.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test

        Phases.PRODUCER.activate();
        myTestNG.run();

        assertThat("No configuration method should have been executed ", tla.getConfigurationFailures().size(),
                Matchers.equalTo(0));

        assertThat("No configuration method should have been executed ", tla.getTestContexts().get(0).getPassedConfigurations().size(),
                Matchers.equalTo(0));

        // Rampup #2
        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing : Issue #89");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Shuffled Phased Tests Issue #89");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.CONSUMER.activate();
        myTestNG2.run();

        assertThat("A configuration method should have been executed ", tla2.getConfigurationFailures().size(),
                Matchers.equalTo(1));

        assertThat("No configuration method should have been executed ", tla2.getTestContexts().get(0).getPassedConfigurations().size(),
                Matchers.equalTo(0));

        // Rampup #2
        TestNG myTestNG3 = TestTools.createTestNG();
        TestListenerAdapter tla3 = TestTools.fetchTestResultsHandler(myTestNG3);

        // Define suites
        XmlSuite mySuite3 = TestTools.addSuitToTestNGTest(myTestNG3, "Automated Suite Phased Testing : Issue #89");

        // Add listeners
        mySuite3.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest3 = TestTools.attachTestToSuite(mySuite3, "Test Shuffled Phased Tests Issue #89");

        myTest3.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.NON_PHASED.activate();
        myTestNG3.run();

        assertThat("A configuration method should have been executed ", tla3.getConfigurationFailures().size(),
                Matchers.equalTo(1));

        assertThat("No configuration method should have been executed ", tla3.getTestContexts().get(0).getPassedConfigurations().size(),
                Matchers.equalTo(0));

    }
}
