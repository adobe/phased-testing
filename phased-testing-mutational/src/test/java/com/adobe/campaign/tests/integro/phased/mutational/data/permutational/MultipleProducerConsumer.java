/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Mutational;
import org.testng.annotations.Test;

@PhasedTest
@Test(groups = "aaa")
public class MultipleProducerConsumer extends Mutational {
    public void ccccc(String param) {
        PhasedTestManager.produce("keyA","bbbbValue");
    }

    public void bbbbb(String param) {
        PhasedTestManager.produce("keyB","bbbbValue");
    }

    public void aaaaa(String param) {
        PhasedTestManager.consume("keyB");

        PhasedTestManager.consume("keyA");
    }
}
