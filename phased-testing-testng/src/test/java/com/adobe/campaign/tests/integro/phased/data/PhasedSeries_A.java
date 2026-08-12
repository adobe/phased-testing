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
import com.adobe.campaign.tests.integro.phased.PhasedTest;

@PhasedTest(canShuffle = false)
public class PhasedSeries_A {
    
 
    @Test
    public void step1(String data) {
    }
    
    @Test
    //@BeforePhaseEvent
    public void step2(String data) {
        
    }
    
    @Test
    @PhaseEvent
    public void step3(String data) {
        
    }

}
