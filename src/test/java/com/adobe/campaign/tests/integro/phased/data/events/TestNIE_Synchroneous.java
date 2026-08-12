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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@PhasedTest
@Test
public class TestNIE_Synchroneous {
    public static int testElement = 3;
    private static final Logger log = LogManager.getLogger();
    public static int expectedStep2Value = 3;
    public static int expectedStep3Value = 3;
    public static int expectedStep3EndResult = 3;

    public void step1(String val) {
        log.info("step1 " + val);
        PhasedTestManager.produceInStep(String.valueOf(testElement));
    }

    public void step2(String val) {
        assertEquals(testElement, expectedStep2Value, "Step 2 assertion");
        log.info("step2 " + val+"  - synchronous value is "+ testElement);
        int l_fetchedValue = Integer.valueOf(PhasedTestManager.consumeFromStep("step1"));
        PhasedTestManager.produceInStep(String.valueOf(l_fetchedValue + testElement));
    }

    public void step3(String val) {
        assertEquals(testElement, expectedStep3Value, "Step 3 assertion");
        log.info("step3 " + val+"  - synchronous value is "+ testElement);
        String l_fetchedValue = PhasedTestManager.consumeFromStep("step2");

        assertEquals(Integer.valueOf(l_fetchedValue), expectedStep3EndResult, "Step 3 assertion FULL");
    }

}
