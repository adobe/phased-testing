/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.data.NormalSeries_A;
import com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError;
import com.adobe.campaign.tests.integro.phased.mutational.data.nested.MutationalTestParent;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild1;
import com.adobe.campaign.tests.integro.phased.mutational.data.simple1.PhasedChild2;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.MockTestTools;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MutationManagerTests {
    private static ITestResult createMutationalMock(String expected, String argument) throws NoSuchMethodException {
        final Method l_myTestWithOneArg = Mutational.class.getMethod("scenario",
                String.class);
        Object[] l_argumentObjects = { argument };

        ITestResult l_itr = MockTestTools.generateTestResultMock(l_myTestWithOneArg, l_argumentObjects);
        Mockito.when(l_itr.getInstanceName()).thenReturn(expected);
        return l_itr;
    }

    @BeforeClass
    public void cleanCache() {
        PhasedTestManager.clearCache();
        ConfigValueHandlerPhased.resetAllValues();

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        PhasedTestManager.clearDataBroker();

    }

    @AfterMethod
    public void clearAllData() {
        cleanCache();
    }

    @Test
    public void testFetchScenarioID_Mutational() throws NoSuchMethodException, SecurityException {
        //On Test End we need to add context of test. The context is the state of the scenario

        //If a test fails, the test state should be logged in the context
        //This logging should be separate from the produce data Class + dataprovider
        //We should have a log
        //Do we only log failures

        var expected = "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError";

        ITestResult l_itr = createMutationalMock(expected, "Q");

        assertThat("We should have the correct full name", PhasedTestManager.fetchScenarioName(l_itr),
                equalTo(expected+"(Q)"));
    }

    @Test
    public void testFetchScenarioID_Mutational2() throws NoSuchMethodException, SecurityException {

        var expected = "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError";

        assertThat("We should have the correct full name", MutationManager.fetchScenarioName(expected, "Q"),
                equalTo(expected+"(Q)"));
    }

    @Test
    public void testIsMutational() throws NoSuchMethodException, SecurityException {
        var expected = "com.adobe.campaign.tests.integro.phased.data.PhasedSeries_H_ShuffledClassWithError";

        ITestResult l_itr = createMutationalMock(expected, "Q");

        assertThat("This should be a mutational test", MutationManager.isMutationalTest(l_itr));
    }

    @Test
    public void testIsMutational_negativeNonMutational() throws NoSuchMethodException, SecurityException {

        final Method l_myTestWithOneArg = PhasedSeries_H_ShuffledClassWithError.class.getMethod("step3",
                String.class);
        Object[] l_argumentObjects = { "Q" };

        ITestResult l_itr = MockTestTools.generateTestResultMock(l_myTestWithOneArg, l_argumentObjects);

        assertThat("We should have the correct full name", !MutationManager.isMutationalTest(l_itr));
    }

    @Test
    public void testExecutionIndex_InterruptiveProducer() {
        //PRODUCER
        //Three steps
        //PG 2_1

        Class testClass = PhasedChild2.class;
        PhasedTestManager.getMethodMap().put(testClass.getTypeName()+".a", new MethodMapping(testClass, 1, 3, 1));
        PhasedTestManager.getMethodMap().put(testClass.getTypeName()+".b", new MethodMapping(testClass, 1, 3, 2));
        PhasedTestManager.getMethodMap().put(testClass.getTypeName()+".c", new MethodMapping(testClass, 1, 3, 3));


        String l_phaseGroup = PhasedTestManager.STD_PHASED_GROUP_PREFIX + "2_1";

        //MutationManager.
        //String l_scenarioName = MutationManager.fetchScenarioName(testClass.getTypeName(), l_phaseGroup);

        assertThat("We should have two steps to execute in Producer", MutationManager.fetchExecutionIndex(testClass.getTypeName(), l_phaseGroup, Phases.PRODUCER),
                Matchers.arrayContaining(0, 2));

        assertThat("We should have one steps to executed in Consumer", MutationManager.fetchExecutionIndex(testClass.getTypeName(), l_phaseGroup, Phases.CONSUMER),
                Matchers.arrayContaining(2, 3));

        assertThat("We should have one steps to executed by default", MutationManager.fetchExecutionIndex(testClass.getTypeName(), l_phaseGroup, Phases.NON_PHASED),
                Matchers.arrayContaining(0, 3));

        assertThat("We should have one steps to executed in Asynchronous", MutationManager.fetchExecutionIndex(testClass.getTypeName(), l_phaseGroup, Phases.ASYNCHRONOUS),
                Matchers.arrayContaining(0, 3));

        assertThat("We should have one steps to executed in permutational", MutationManager.fetchExecutionIndex(testClass.getTypeName(), l_phaseGroup, Phases.PERMUTATIONAL),
                Matchers.arrayContaining(0, 3));
    }

    @Test
    public void testIfTestIsMutationalSimple() {
        Class l_testClass = PhasedChild1.class;

        assertThat("This should be a mutational test", MutationManager.isMutationalTest(l_testClass));

        assertThat("A mutational test is a phasedTest", PhasedTestManager.isPhasedTest(l_testClass));

        Class l_nonMutationalClass = NormalSeries_A.class;
        assertThat("This should not be a mutational test", !MutationManager.isMutationalTest(l_nonMutationalClass));



    }

    @Test
    public void testIfTestIsMutationalNested() {
        Class l_testClass = MutationalTestParent.MutationalChildTest1.class;

        assertThat("This should be a mutational test", MutationManager.isMutationalTest(l_testClass));

        assertThat("TA mutational test is a phasedTest", PhasedTestManager.isPhasedTest(l_testClass));

    }


    @Test
    public void testIfTestIsMutationalMethod() throws NoSuchMethodException {
        Method l_testMethod = PhasedChild1.class.getMethod("step1", String.class);

        assertThat("This should be a mutational test", MutationManager.isMutationalTest(l_testMethod));

        assertThat("A mutational test is a phasedTest", PhasedTestManager.isPhasedTest(l_testMethod));


        Method l_nonMutationalMethod = NormalSeries_A.class.getMethod("firstTest");
        assertThat("This should not be a mutational test", !MutationManager.isMutationalTest(l_nonMutationalMethod));

        Method l_nonMutationalMethod2 = Object.class.getMethod("wait");
        assertThat("This should not be a mutational test", !MutationManager.isMutationalTest(l_nonMutationalMethod));

    }

    @Test
    public void testIsShuffled_Mutational() throws SecurityException {
        Class l_myClass = PhasedChild1.class;

        assertThat("We should be in Shuffled mode", PhasedTestManager.isPhasedTestShuffledMode(l_myClass));
        assertThat("We should not be in Single mode", !PhasedTestManager.isPhasedTestSingleMode(l_myClass));

        //Activate target event
        ConfigValueHandlerPhased.EVENT_TARGET.activate(l_myClass.getTypeName()+".step1");
        assertThat("We should be in Shuffled mode", !PhasedTestManager.isPhasedTestShuffledMode(l_myClass));
        assertThat("We should  be in Single mode", PhasedTestManager.isPhasedTestSingleMode(l_myClass));
    }


}
