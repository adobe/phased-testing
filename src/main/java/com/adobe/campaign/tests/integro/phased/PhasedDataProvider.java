/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;

public class PhasedDataProvider {
    public static final String SHUFFLED = "phased-data-provider-shuffled";
    public static final String SINGLE = "phased-data-provider-single";
    public static final String DEFAULT = "phased-default";
    public static final String MUTATIONAL = "MUTATIONAL";

    @DataProvider(name = SHUFFLED)
    public Object[][] shuffledMode(Method m) {
        return PhasedTestManager.fetchProvidersShuffled(m);
    }
    protected static Logger log = LogManager.getLogger();

    @DataProvider(name = "MUTATIONAL")
    public Object[][] shuffleGroups(ITestNGMethod tm) {

        //PhasedTestManager.fetchProvidersShuffled(m);

        log.info(tm.getTestClass().getRealClass().getTypeName());

        return PhasedTestManager.fetchProvidersShuffled(tm);
    }
    
    @DataProvider(name = SINGLE)
    public Object[] singleRunMode(Method m) {
        return PhasedTestManager.fetchProvidersSingle(m);
    }
    
    @DataProvider(name = PhasedDataProvider.DEFAULT)
    public Object[] defaultDP(Method m) {
        return PhasedTestManager.fetchProvidersStandard(m);
    }

}
