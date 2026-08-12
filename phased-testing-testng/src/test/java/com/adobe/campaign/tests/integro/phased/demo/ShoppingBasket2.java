/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
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




