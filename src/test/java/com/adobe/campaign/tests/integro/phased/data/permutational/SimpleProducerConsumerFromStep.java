/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

public class SimpleProducerConsumerFromStep {
    public void bbbbb(Object param) {
        PhasedTestManager.produceInStep("bbbbValue");
    }

    public void aaaa(Object param) {
        PhasedTestManager.consumeFromStep("bbbbb");
    }
}
