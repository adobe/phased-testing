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


/**
 * This PhasedTest Should not be executed when inactive
 *
 *
 * Author : gandomi
 *
 */
@PhasedTest(executeInactive = false,canShuffle = false)
public class PhasedSeries_B_NoInActive {
    
    @Test
    public void step1(String data) {
        
    }
    
    @Test
    @PhaseEvent
    public void step2(String data) {
        
    }
    
    @Test
    public void step3(String data) {
        
    }

}
