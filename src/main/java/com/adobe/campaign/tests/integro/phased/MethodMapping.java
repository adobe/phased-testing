/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

class MethodMapping  {
    
    Class<?> declaredClass;
    int nrOfProviders;
    int totalClassMethods;
    int methodOrderInExecution;
    
    MethodMapping(Class<?> in_declaredClass, int in_nrOfProviders, int in_nrOfStepsInTest, int in_executionOrder) {
        nrOfProviders=in_nrOfProviders;
        totalClassMethods=in_nrOfStepsInTest;
        declaredClass=in_declaredClass;
        methodOrderInExecution=in_executionOrder;
    }

}