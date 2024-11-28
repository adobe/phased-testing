/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
