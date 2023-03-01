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
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@PhasedTest(canShuffle = true)
@Test
public class TestOrderedShuffled_eventPassedAsExecutionVariable {
    

    public void stepZZZ(String val) {
        System.out.println("step1 " + val);
        PhasedTestManager.produce("step1","A");
    }

    public void stepMMM(String val) {
        System.out.println("step2 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step1");
        PhasedTestManager.produce("step2",l_fetchedValue + "B");
    }

    public void stepAAA(String val) {
        System.out.println("step3 " + val);
        String l_fetchedValue = PhasedTestManager.consume("step2");

        assertEquals(l_fetchedValue, "AB");
    }

}