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
package com.adobe.campaign.tests.integro.phased.demo;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;









@Test
@PhasedTest(canShuffle = true)
public class ShoppingBasket2 {
    
    
    @BeforeSuite
    public void bef() {
        System.out.println("in local before suite");
    }

    public void step1_searchForProduct(String val) {
        System.out.println(val+" -  Search for product");
        PhasedTestManager.produce("searchForProduct","book A");
    }

    public void step2_addToShoppingBasket(String val) {
        System.out.println(val + " - Add to Shopping Basket");
        
        PhasedTestManager.produce("Basket","1");
    }

    public void step3_checkout(String val) {
        System.out.println(val+" - Checkout");
        

    }

}






