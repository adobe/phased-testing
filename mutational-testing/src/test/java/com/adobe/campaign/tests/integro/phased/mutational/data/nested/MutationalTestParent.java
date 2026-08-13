/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational.data.nested;

import com.adobe.campaign.tests.integro.phased.Mutational;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

public class MutationalTestParent {
    @Test(groups = "nested")
    public class MutationalChildTest1 extends Mutational {

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
    }
}
