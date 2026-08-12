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

import static com.adobe.campaign.tests.integro.phased.PhasedTestManager.consume;
import static com.adobe.campaign.tests.integro.phased.PhasedTestManager.produce;

public class SimpleProducerConsumerStaticImport {
    public void bbbbb(Object param) {
        produce("bbbbkey","bbbbValue");
    }

    public void aaaa(Object param) {
        consume("bbbbkey");
    }
}
