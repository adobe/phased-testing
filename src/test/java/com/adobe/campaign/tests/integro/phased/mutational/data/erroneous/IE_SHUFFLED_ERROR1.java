/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational.data.erroneous;

import com.adobe.campaign.tests.integro.phased.Mutational;
import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Test
@PhasedTest
public class IE_SHUFFLED_ERROR1 extends Mutational {

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
