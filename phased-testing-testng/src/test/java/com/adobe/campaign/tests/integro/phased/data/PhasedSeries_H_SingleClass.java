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

import com.adobe.campaign.tests.integro.phased.PhaseEvent;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

@Test(groups = { "UPGRADE", "PROPERTIES_SELECT" }, description = "This is an archetypical test for the phased tests")
@PhasedTest(canShuffle = false)
public class PhasedSeries_H_SingleClass {

    /** The first step */

    public void step1(String data) {

        PhasedTestManager.produceInStep("A");

    }

    public void step2(String data) {
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
        PhasedTestManager.produceInStep(l_fetchedValue + "B");
    }

    @PhaseEvent
    public void step3(String data) {
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step2");
        System.out.println(data);

        assertEquals(l_fetchedValue, "AB");

    }

}
