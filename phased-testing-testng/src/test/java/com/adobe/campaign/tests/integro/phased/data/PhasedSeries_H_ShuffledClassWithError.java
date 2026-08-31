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
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
@PhasedTest(canShuffle = true)
public class PhasedSeries_H_ShuffledClassWithError {

    public void step1(String val) {
        System.out.println("step1 " + val);
        PhasedTestManager.produceInStep("A");
    }

    public void step2(String val) {
        System.out.println("step2 " + val);
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
        assertFalse(true);
        PhasedTestManager.produceInStep(l_fetchedValue + "B");
        
    }

    public void step3(String val) {
        System.out.println("step3 " + val);
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step2");

        assertEquals(l_fetchedValue, "AB");

    }

}
