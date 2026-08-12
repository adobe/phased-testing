/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;


/** Shangrila */
@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class, groups = {"X","Y","Z"},description = "specifically for Test Book")
@PhasedTest(canShuffle = true)
public class PhasedSeries_I_ShuffledProduceKey {
    
    
    public void step1(String val) {
        System.out.println("step1 " + val);
        PhasedTestManager.produce("step1Val","A");
    }

    
    public void step2(String val) {
        System.out.println("step2 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step1Val");
        PhasedTestManager.produce("step2Val",l_fetchedValue + "B");
    }

    
    public void step3(String val) {
        System.out.println("step3 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step2Val");

        assertEquals(l_fetchedValue, "AB");

    }

}
