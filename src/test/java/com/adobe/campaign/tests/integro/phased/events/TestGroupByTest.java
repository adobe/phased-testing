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
