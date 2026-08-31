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

@PhasedTest(canShuffle = true)
@Test
public class MixingEmptyAndProduceTests {
    public void ccccc(Object param) {


    }
    public void bbbbb(Object param) {
        PhasedTestManager.produce("keyA","v");
    }

    public void aaaaa(Object param) {
        PhasedTestManager.consume("keyA");
    }
}
