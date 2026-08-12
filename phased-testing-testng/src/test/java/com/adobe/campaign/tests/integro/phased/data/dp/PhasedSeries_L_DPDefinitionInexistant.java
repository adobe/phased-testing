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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;


@Test(dataProvider = "create4")
@PhasedTest(canShuffle = true)
public class PhasedSeries_L_DPDefinitionInexistant {
    
    @DataProvider(name = "create5")
    public Object[][] createData() {
        return new Object[][] {{"Z"},{"M"}};
    }
    
    public void step1(String x) {
        PhasedTestManager.produceInStep("A");
    }

    public void step2(String x) {
       String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
       
       assertEquals(l_fetchedValue, "A");
    }

}
