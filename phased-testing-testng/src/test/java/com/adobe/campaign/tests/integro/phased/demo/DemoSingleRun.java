/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.demo;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhaseEvent;
import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;

@Test
@PhasedTest(canShuffle = false)
public class DemoSingleRun {

    public void step1(String val) {
        //Perform actions
    }
    
    @PhaseEvent

    public void step2(String val) {
        //Perform Actions
        
        //Perform Assertions
    }
}
