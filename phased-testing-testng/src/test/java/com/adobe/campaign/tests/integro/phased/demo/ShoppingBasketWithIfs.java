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
import com.adobe.campaign.tests.integro.phased.Phases;
import org.testng.annotations.Test;

    public class ShoppingBasketWithIfs {
    
        //The normal test (0_3 and 3_0)
        @Test
        public void standardTest() {
            //searchForProduct()
            //addToShoppingBasket()
            //payForProduct()
        }
    
        @Test
        public void ShoppingBasket1_2() {
            if (Phases.PRODUCER.isSelected()) {
                //searchForProduct()
            } else {
                //addToShoppingBasket()
                //payForProduct()
            }
        }
    
        @Test
        public void ShoppingBasket2_1() {
            if (Phases.PRODUCER.isSelected()) {
                //searchForProduct()
                //addToShoppingBasket()
            } else {
                //payForProduct()
            }
        }
    }

    







