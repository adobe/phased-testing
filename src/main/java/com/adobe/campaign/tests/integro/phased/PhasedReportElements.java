/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.ITestResult;

/**
 * Provides you with the possible report elements that help constitute a Phased Test
 *
 *
 * Author : gandomi
 *
 */
public enum PhasedReportElements {
    /**
     * Fetches the scenario name, aka class name. It turns the first character to a lower case
     */
    SCENARIO_NAME {
        public String fetchElement(ITestResult in_testResult) {
            final String l_className = in_testResult.getMethod().getRealClass().getSimpleName();
            return l_className.substring(0, 1).toLowerCase() + l_className.substring(1);
        }
    },
    /**
     * Fetches the current phase
     */
    PHASE {
        public String fetchElement(ITestResult in_testResult) {
            return Phases.getCurrentPhase().toString();
        }
    },
    /**
     * Fetches the phase group used for executing the test
     */
    PHASE_GROUP {
        @Override
        public String fetchElement(ITestResult in_testResult) {
            Object[] params = in_testResult.getParameters();
            if (params.length == 0) {
                return "";
            }
            return in_testResult.getParameters()[0].toString();
        }
    },
    /**
     * Creates a concatenation of the user-defined data providers 
     */
    DATA_PROVIDERS {
        @Override
        public String fetchElement(ITestResult in_testResult) {
            return Stream.of(in_testResult.getParameters())
                .skip(1)
                .map(Object::toString)
                .collect(Collectors.joining("_"));
        }
    };

    public abstract String fetchElement(ITestResult in_testResult);

}
