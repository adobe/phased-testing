/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;

public class MutationalDataProvider {
    public static final String MUTATIONAL = "MUTATIONAL";
    public static final String MUTATIONAL_SINGLE = "MUTATIONAL_SINGLE";

    protected static Logger log = LogManager.getLogger();

    @DataProvider(name = MUTATIONAL)
    public Object[][] shuffleGroups(ITestNGMethod tm) {

        log.info(tm.getTestClass().getRealClass().getTypeName());

        return PhasedTestManager.fetchProvidersShuffled(tm);
    }

    @DataProvider(name = MUTATIONAL_SINGLE)
    public Object[][] singleRunMode(ITestNGMethod tm) {
        return MutationManager.fetchProvidersSingle(tm);
    }
}
