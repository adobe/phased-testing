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
package com.adobe.campaign.tests.integro.phased.events.data;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import org.testng.Assert;
import org.testng.annotations.Test;

//@Test(groups = "aaa", dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
@PhasedTest
@Test(groups = "aaa")
public class PhasedChild2 extends PhasedParent {

    public void step1(String phaseGroup) {
        System.out.println("PG2 : Executing step1 with"+phaseGroup);
        Assert.assertEquals(1,1);
    }

    public void step2(String phaseGroup) {
        System.out.println("PG2 : Executing step2 with"+phaseGroup);

        Assert.assertEquals(1,1);
    }

    public void step3(String phaseGroup) {
        System.out.println("PG2 : Executing step3 with"+phaseGroup);

        Assert.assertEquals(1,1);
    }
}
