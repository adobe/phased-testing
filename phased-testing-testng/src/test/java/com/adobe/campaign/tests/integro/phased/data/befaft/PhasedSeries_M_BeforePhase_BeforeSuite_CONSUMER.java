/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.befaft;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import com.adobe.campaign.tests.integro.phased.BeforePhase;
import com.adobe.campaign.tests.integro.phased.Phases;

public class PhasedSeries_M_BeforePhase_BeforeSuite_CONSUMER {
    
    public static int beforeValue = 0;
    
    @BeforePhase(appliesToPhases = {Phases.CONSUMER})
    @BeforeSuite
    public void beforePhasedSuiteConsumer() {
        beforeValue+=13;
        
        Assert.assertEquals(beforeValue, 13);
    }
}
