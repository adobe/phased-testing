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
