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

import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;

public class PhasedDataProvider implements FrameworkDataProvider {
    public static final String SHUFFLED = "phased-data-provider-shuffled";
    public static final String SINGLE = PhasedTestManager.STD_PHASED_SINGLE_PROVIDER;
    public static final String DEFAULT = PhasedTestManager.STD_PHASED_DEFAULT;

    @DataProvider(name = SHUFFLED)
    public Object[][] shuffledMode(Method m) {
        return PhasedTestManager.fetchProvidersShuffled(m);
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
