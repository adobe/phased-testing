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

import com.adobe.campaign.tests.integro.phased.data.permutational.SimplePermutationTest;
import com.adobe.campaign.tests.integro.phased.utils.ConfigValueHandler;
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
        ConfigValueHandler.resetAllValues();

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

    @Test
    public void testPhasedProducer() {
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
        //final Class<DebugSimplePermutationTest> l_testClass = DebugSimplePermutationTest.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test
        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful method of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

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

    @Test
    public void testPhasedFullMonty() {
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
        //final Class<DebugSimplePermutationTest> l_testClass = DebugSimplePermutationTest.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test
        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 6 successful method of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

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

        // Rampup Consumer
        TestNG myTestNGC = TestTools.createTestNG();
        TestListenerAdapter tlaC = TestTools.fetchTestResultsHandler(myTestNGC);

        // Define suites
        XmlSuite mySuiteC = TestTools.addSuitToTestNGTest(myTestNGC, "Automated Suite Phased Testing - Ordering steps - Consumer");

        // Add listeners
        mySuiteC.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTestC = TestTools.attachTestToSuite(mySuiteC, "Test Phased : ordering tests - Consumer");

         //final Class<DebugSimplePermutationTest> l_testClass = DebugSimplePermutationTest.class;
        myTestC.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        // Add package to test
        Phases.CONSUMER.activate();

        myTestNGC.run();

        assertThat("We should have 6 successful method of phased Tests",
                (int) tlaC.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tlaC.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tlaC.getSkippedTests().size(), equalTo(0));

        ITestContext contextC = tlaC.getTestContexts().get(0);

        assertThat("The Report NOW only have one passed test",
                contextC.getPassedTests().getAllResults().size(), is(equalTo(3)));

        assertThat("The Report should also include the same value as the Skipped",
                contextC.getSkippedTests().getAllResults().size(), is(equalTo(tlaC.getSkippedTests().size())));

        assertThat("The Report should also include the same value as the Failed",
                contextC.getFailedTests().getAllResults().size(), is(equalTo(tlaC.getFailedTests().size())));

    }

}
