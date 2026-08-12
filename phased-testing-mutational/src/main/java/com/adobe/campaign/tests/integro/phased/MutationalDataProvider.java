/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.spi.FrameworkDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;

public class MutationalDataProvider implements FrameworkDataProvider {
    public static final String MUTATIONAL = "MUTATIONAL";
    public static final String MUTATIONAL_SINGLE = "MUTATIONAL_SINGLE";
    public static final String SINGLE = PhasedTestManager.STD_PHASED_SINGLE_PROVIDER;
    public static final String DEFAULT = PhasedTestManager.STD_PHASED_DEFAULT;

    protected static Logger log = LogManager.getLogger();

    @DataProvider(name = MUTATIONAL)
    public Object[][] shuffleGroups(ITestNGMethod tm) {

        log.info(tm.getTestClass().getRealClass().getTypeName());

        return MutationManager.fetchProvidersShuffled(tm);
    }

    @DataProvider(name = MUTATIONAL_SINGLE)
    public Object[][] singleRunMode(ITestNGMethod tm) {
        return MutationManager.fetchProvidersSingle(tm);
    }

    /**
     * Fallback provider used when the whole framework is running in {@link ExecutionMode#STANDARD} mode —
     * matches {@code PhasedDataProvider.SINGLE} in the phased-testing-testng module, since a Mutational
     * test run outside of any phased/mutational execution mode behaves like a plain single-run test.
     */
    @DataProvider(name = SINGLE)
    public Object[] singleRunMode(Method m) {
        return PhasedTestManager.fetchProvidersSingle(m);
    }

    /**
     * Fallback provider used when the whole framework is running in {@link ExecutionMode#STANDARD} mode —
     * matches {@code PhasedDataProvider.DEFAULT} in the phased-testing-testng module.
     */
    @DataProvider(name = DEFAULT)
    public Object[] defaultDP(Method m) {
        return PhasedTestManager.fetchProvidersStandard(m);
    }
}
