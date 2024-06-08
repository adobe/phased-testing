/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhaseEvent;
import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;


@PhasedTest(canShuffle = false)
public class PhasedSeries_G_DefaultProvider {
    
 
    @Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
    public void step1(String data) {
    }
    
    @Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
    public void step2(String data) {
        
    }
    
    @PhaseEvent
    
    @Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
    public void step3(String data) {
        
    }

}
