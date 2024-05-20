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
package com.adobe.campaign.tests.integro.phased.events;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.MutationListener;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TestGroupByTest {

    @Test
    public void testNewOrder() {

        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PRODUCER.name());

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        //mySuite.addListener(EventInjectorListener.class.getTypeName());
        mySuite.addListener(MutationListener.class.getTypeName());

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Repetetive Phased Tests Producer");

        final XmlPackage l_testPkg = new XmlPackage("com.adobe.campaign.tests.integro.phased.events.data");
        myTest.setPackages(Collections.singletonList(l_testPkg));

        myTest.addIncludedGroup("aaa");

        // Add package to test

        myTestNG.run();

        assertThat("We should have 2 successful method of phased Tests",
                (int) tla.getPassedTests().size(),
                is(equalTo(5)));
    }
}
