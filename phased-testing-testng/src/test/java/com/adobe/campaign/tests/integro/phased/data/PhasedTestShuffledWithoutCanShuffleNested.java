/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import org.testng.annotations.Test;

public class PhasedTestShuffledWithoutCanShuffleNested {
    @PhasedTest
    @Test
    public class PhasedTestShuffledWithoutCanShuffleNestedInner {
        public void step1(String a) {

        }

        public void step2(String a) {

        }

        public void step3(String a) {

        }
    }
}
