/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational.data.nie;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Mutational;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@PhasedTest
@Test
public class TestMutationalShuffled_eventPassedAsExecutionVariable extends Mutational {

    public void step1(String val) {
        System.out.println("step1 " + val);
        PhasedTestManager.produce("step1","A");
    }

    public void step2(String val) {
        System.out.println("step2 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step1");
        PhasedTestManager.produce("step2",l_fetchedValue + "B");
    }

    public void step3(String val) {
        System.out.println("step3 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step2");

        assertEquals(l_fetchedValue, "AB");
    }

}
