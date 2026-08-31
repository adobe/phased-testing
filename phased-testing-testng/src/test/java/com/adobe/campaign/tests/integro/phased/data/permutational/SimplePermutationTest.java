/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
@PhasedTest(canShuffle = true)
public class SimplePermutationTest {

    public void zzzz(String val) {
        System.err.println("zzzz "+val);
        PhasedTestManager.produce("step1Key", "A");
    }

    public void yyyyy(String val) {
        System.err.println("yyyyy "+val);
        String l_fetchedValue = PhasedTestManager.consume("step1Key");
        PhasedTestManager.produce("step2Key", l_fetchedValue + "B");
    }

    public void xxxxx(String val) {
        System.err.println("xxxxx "+val);
        String l_fetchedValue = PhasedTestManager.consume("step2Key");

        assertEquals(l_fetchedValue, "AB");

    }

}
