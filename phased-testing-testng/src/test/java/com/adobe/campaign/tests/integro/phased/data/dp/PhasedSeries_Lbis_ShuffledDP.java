/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.dp;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(dataProvider = "create", dataProviderClass = PhasedSeries_L_PROVIDER.class)
@PhasedTest(canShuffle = true)
public class PhasedSeries_Lbis_ShuffledDP {
    
    public void step1(String val,String otherVal) {
         System.out.println("step1 " + val+ " user dp : "+otherVal);
        PhasedTestManager.produceInStep("A");
    }

    public void step2(String val,String otherVal) {
       System.out.println("step2 " + val+ " user dp : "+otherVal);
       String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
       
       assertEquals(l_fetchedValue, "A");
    }

    public void step3(String val,String otherVal) {
        System.out.println("step3 " + val+ " user dp : "+otherVal);
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");

        assertEquals(l_fetchedValue, "A");
    }

}
