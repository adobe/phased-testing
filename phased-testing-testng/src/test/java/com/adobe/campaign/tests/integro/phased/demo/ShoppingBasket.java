/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.demo;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import org.testng.annotations.Test;

    @Test
    @PhasedTest(canShuffle = true)
    public class ShoppingBasket {
    
        public void step1_searchForProduct(String val) {
          //searchForProduct()
        }
    
        public void step2_addToShoppingBasket(String val) {
            //addToShoppingBasket()   
        }
    
        public void step3_payForProduct(String val) {
          //payForProduct()
        }
    }






