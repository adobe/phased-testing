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

import com.adobe.campaign.tests.integro.phased.AfterPhase;

public class PhasedSeries_M_BeforePhase_onAfterSuite {
    
    public static int beforeValue = 0;
    
    @AfterPhase
    @BeforeSuite
    public void beforePhasedSuite() {
        beforeValue+=13;
        
        Assert.assertEquals(beforeValue, 13);
    }
}
