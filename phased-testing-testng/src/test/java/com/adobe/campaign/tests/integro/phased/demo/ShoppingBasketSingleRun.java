/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.demo;

import com.adobe.campaign.tests.integro.phased.PhaseEvent;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

    @Test
    @PhasedTest(canShuffle = false)
    public class ShoppingBasketSingleRun {
    
        public void step1_searchForProduct(String val) {
            //Perform actions
        }
    
        public void step2_addToShoppingBasket(String val) {
            //Perform Actions
    
            //Perform Assertions
        }
        
        @PhaseEvent
    
        public void step3_payForProduct(String val) {
            //Perform Actions
    
            //Perform Assertions
        }
    }

    
    





