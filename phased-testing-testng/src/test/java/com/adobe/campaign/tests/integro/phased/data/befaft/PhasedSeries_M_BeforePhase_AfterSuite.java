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
import org.testng.annotations.AfterSuite;

import com.adobe.campaign.tests.integro.phased.AfterPhase;

public class PhasedSeries_M_BeforePhase_AfterSuite {
    
    public static int afterValue = 0;
    
    @AfterPhase
    @AfterSuite
    public void afterPhasedSuite() {
        afterValue+=17;
        
        Assert.assertEquals(afterValue, 24);
    }
}
