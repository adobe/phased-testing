/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.dp;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;


@Test
@PhasedTest(canShuffle = true)
public class PhasedSeries_L_ShuffledNoArgs {
    
    public void step1() {
        PhasedTestManager.produceInStep("A");
    }

    public void step2() {
       String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
       
       assertEquals(l_fetchedValue, "A");
    }

}
