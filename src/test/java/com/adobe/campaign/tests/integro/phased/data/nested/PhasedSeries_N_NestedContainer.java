/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.nested;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class PhasedSeries_N_NestedContainer {
    @Test(groups = "negative")
    @PhasedTest(canShuffle = true)
    public class PhasedSeries_N_ShuffledClassWithError {

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

    @Test(groups = "positive")
    @PhasedTest(canShuffle = true)
    public class PhasedSeries_N_ShuffledClass {

        public void step1(String val) {
            System.out.println("step1 " + val);
            PhasedTestManager.produceInStep("A");
        }

        public void step2(String val) {
            System.out.println("step2 " + val);
            String l_fetchedValue = PhasedTestManager.consumeFromStep("step1");
            PhasedTestManager.produceInStep(l_fetchedValue + "B");
        }

        public void step3(String val) {
            System.out.println("step3 " + val);
            String l_fetchedValue = PhasedTestManager.consumeFromStep("step2");

            assertEquals(l_fetchedValue, "AB");

        }

    }

}
