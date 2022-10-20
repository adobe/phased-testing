package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.data.permutational.SimplePermutationTest;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class OrderingStepsTests {
    @BeforeMethod
    @AfterMethod
    public void resetVariables() {

        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);
        System.clearProperty("PHASED.TESTS.DETECT.ORDER");

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
    public void testNonPhased() {
        System.setProperty("PHASED.TESTS.DETECT.ORDER","true");
        //Activate Merge
        PhasedTestManager.activateMergedReports();

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing - Ordering steps");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased : ordering tests");

        final Class<SimplePermutationTest> l_testClass = SimplePermutationTest.class;
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

    @Test(enabled = false)
    public void testPhasedProducer() {
        //Activate Merge
        PhasedTestManager.activateMergedReports();


        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing - Ordering steps");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Phased : ordering tests");

        final Class<SimplePermutationTest> l_testClass = SimplePermutationTest.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test
        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 3 successful method of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report NOW only have one passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                context.getSkippedTests().getAllResults().size(), is(equalTo(tla.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(tla.getFailedTests().size())));

    }

}
