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

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

@SuppressWarnings("unused")

    @Test
    @PhasedTest(canShuffle = true)
    public class ShoppingBasket2 {
    
        public void step1_searchForProduct(String val) {
            //Search for product
            
            //Store value with key
            PhasedTestManager.produce("FoundProduct", "book A");
        }
    
        public void step2_addToShoppingBasket(String val) {
            if (val.equals("phased-shuffledGroup_1_2") || val.equals("phased-shuffledGroup_2_1"))
                Assert.assertFalse(true, "Failure during shopping basket storage.");
            System.out.println(val);
            // Fetch searched product
            String searchedProduct = PhasedTestManager.consume("FoundProduct");
           
            //add searchedProduct to basket
            
            //Store basket ID 
            PhasedTestManager.produce("BasketID", "1");
        }
    
        public void step3_checkout(String val) {
            //Fetch basket ID using key
            String basketId = PhasedTestManager.consume("BasketID");
      
            
            //Fetch searched product        
            String searchedProduct = PhasedTestManager.consume("FoundProduct");
            
            //Assertion
            //Check that the product is in the basket
            
            //Checkout
        }
    }




