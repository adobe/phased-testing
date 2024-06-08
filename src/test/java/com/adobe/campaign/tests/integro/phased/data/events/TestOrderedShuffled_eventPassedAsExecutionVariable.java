/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.events;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@PhasedTest(canShuffle = true)
@Test
public class TestOrderedShuffled_eventPassedAsExecutionVariable {
    

    public void stepZZZ(String val) {
        System.out.println("step1 " + val);
        PhasedTestManager.produce("step1","A");
    }

    public void stepMMM(String val) {
        System.out.println("step2 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step1");
        PhasedTestManager.produce("step2",l_fetchedValue + "B");
    }

    public void stepAAA(String val) {
        System.out.println("step3 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step2");

        assertEquals(l_fetchedValue, "AB");
    }

}
