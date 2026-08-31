/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.nested;

import com.adobe.campaign.tests.integro.phased.BeforePhase;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

public class PhasedSeries_N_BeforePhase_BeforeSuite {
    
    public static int beforeValue = 0;
    
    @BeforePhase
    @BeforeSuite
    public void beforePhasedSuite() {
        beforeValue+=13;
        
        Assert.assertEquals(beforeValue, 13);
    }
}
