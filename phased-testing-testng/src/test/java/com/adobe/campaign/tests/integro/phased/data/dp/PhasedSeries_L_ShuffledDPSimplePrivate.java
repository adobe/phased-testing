/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.dp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedTest;

@Test(dataProvider = "create2")
@PhasedTest(canShuffle = true)
public class PhasedSeries_L_ShuffledDPSimplePrivate {
    
    @DataProvider(name = "create2")
    public Object[][] createData() {
        return new Object[][] {{"Z"},{"M"}};
    }
    
    public void step1(String val) {
        // System.out.println("step1 " + val);
         //PhasedTestManager.produceInStep("A");
     }

     public void step2(String val) {
        // System.out.println("step2 " + val);
        // String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
        // PhasedTestManager.produceInStep(l_fetchedValue + "B");
     }

}
