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
import com.adobe.campaign.tests.integro.phased.BeforePhase;

public class PhasedSeries_M_AfterPhase_onBeforeSuite {
    
    public static int beforeValue = 0;
    
    @BeforePhase
    @AfterSuite
    public void beforePhasedSuite() {
        beforeValue+=13;
        
        Assert.assertEquals(beforeValue, 13);
    }
}
