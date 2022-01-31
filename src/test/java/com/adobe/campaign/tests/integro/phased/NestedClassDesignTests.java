package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClass;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_RecipientClass;
import com.adobe.campaign.tests.integro.phased.data.nested.PhasedSeries_N_NestedContainer;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
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

public class NestedClassDesignTests {
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

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(),
                new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));
    }

    @Test
    public void testSHUFFLED_Nested_HelloWorld() {
        // ******** PRODUCER ********
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing - NESTED");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Nested Phased Tests Producer");

        final Class<PhasedSeries_RecipientClass.PhasedSeries_J_ShuffledClassInAClass> l_testClass = PhasedSeries_RecipientClass.PhasedSeries_J_ShuffledClassInAClass.class;
        myTest.setXmlClasses(
                Arrays.asList(new XmlClass(l_testClass), new XmlClass(PhasedSeries_H_ShuffledClass.class)));

        // Add package to test

        Phases.PRODUCER.activate();

        myTestNG.run();

        assertThat("We should have 12 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(12)));

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(6)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        // ******** CONSUMER ********
        Phases.CONSUMER.activate();

        TestNG myTestNG2 = TestTools.createTestNG();
        TestListenerAdapter tla2 = TestTools.fetchTestResultsHandler(myTestNG2);

        // Define suites
        XmlSuite mySuite2 = TestTools.addSuitToTestNGTest(myTestNG2, "Automated Suite Phased Testing");

        // Add listeners
        mySuite2.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest2 = TestTools.attachTestToSuite(mySuite2, "Test Nested Phased Tests Consumer");

        myTest2.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG2.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla2.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass))
                        .count(),
                is(equalTo(6)));

        //STEP 1
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step1 with the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should NOT have executed step1 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step1 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step1")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 2

        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should NOT have executed step2 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step2 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step2 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step2")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //STEP 3
        assertThat("We should have no executions for the phased group 0",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).noneMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "3_0")));

        assertThat("We should have executed step3 with the phased group 1",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1")));

        assertThat("We should have executed step3 with the phased group 2",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "1_2")));

        assertThat("We should have executed step3 with the phased group 3",
                tla2.getPassedTests().stream().filter(m -> m.getName().equals("step3")).anyMatch(
                        m -> m.getParameters()[0].equals(PhasedTestManager.STD_PHASED_GROUP_PREFIX + "0_3")));

        //Global
        assertThat("We should have no failed tests", tla2.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla2.getSkippedTests().size(), equalTo(0));

    }

    /**
     * In this case we try to run a class with many children
     */
    @Test
    public void testSHUFFLED_Nested_HelloWorld_MultiNestedClasses() {
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing - NESTED");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Nested Phased Tests Producer");

        myTest.setXmlClasses(Collections.singletonList(
                new XmlClass(PhasedSeries_N_NestedContainer.class)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        myTestNG.run();

        //Global
        assertThat("We should have 9 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(9)));

        assertThat("We should have 2 failed tests", tla.getFailedTests().size(), equalTo(2));

        assertThat("We should have one skipped test", tla.getSkippedTests().size(), equalTo(1));

        //Merged
        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should only have one Passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(4)));

        assertThat("The Report should not contain Skipped tests",
                context.getSkippedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should contain 2 tests marked as Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(2)));

    }

    /**
     * In this case we try to run a class with many children. And we only execute only one of them
     */
    @Test
    public void testSHUFFLED_Nested_HelloWorld_Negative_MultiNestedClasses() {
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing - NESTED");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Nested Phased Tests Producer");

        myTest.setXmlClasses(Collections.singletonList(
                new XmlClass(PhasedSeries_N_NestedContainer.PhasedSeries_N_ShuffledClassWithError.class)));

        // Add package to test

        Phases.PRODUCER.activate();
        System.setProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "true");

        myTestNG.run();

        //Global
        assertThat("We should have 3 successful methods of phased Tests", tla.getPassedTests().size(),
                is(equalTo(3)));

        assertThat("We should have 2 failed tests", tla.getFailedTests().size(), equalTo(2));

        assertThat("We should have one skipped test", tla.getSkippedTests().size(), equalTo(1));

        //Merged
        ITestContext context = tla.getTestContexts().get(0);

        assertThat("The Report should only have one Passed test",
                context.getPassedTests().getAllResults().size(), is(equalTo(1)));

        assertThat("The Report should not contain Skipped tests",
                context.getSkippedTests().getAllResults().size(), is(equalTo(0)));

        assertThat("The Report should contain 2 tests marked as Failed",
                context.getFailedTests().getAllResults().size(), is(equalTo(2)));

    }

}
