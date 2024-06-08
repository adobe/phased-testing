/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import org.testng.annotations.Test;

public class NestClassContainer {

    @Test
    public static class TestClass1 {

        public void testC1_m1() {
            System.out.println("Should not execute");
        }
    }

    @Test
    public static class TestClass2 {

        public void testC2_m1() {
            System.out.println("Should execute");
        }
    }

}
