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

import java.util.Arrays;
import java.util.Iterator;
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
            StringBuilder sb = new StringBuilder(l_className.substring(0, 1).toLowerCase());
            sb.append(l_className.substring(1));
            return sb.toString();
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

            return in_testResult.getParameters()[0].toString();
        }
    },
    /**
     * Creates a concatenation of the user-defined data providers 
     */
    DATA_PROVIDERS {
        @Override
        public String fetchElement(ITestResult in_testResult) {

            Iterator<Object> l_paramsIterator = Arrays.asList(in_testResult.getParameters()).iterator();

            l_paramsIterator.next();

            StringBuilder sb = new StringBuilder(l_paramsIterator.hasNext() ? l_paramsIterator.next().toString() : "");

            while (l_paramsIterator.hasNext()) {
                sb.append("_");
                sb.append(l_paramsIterator.next().toString());
            }

            return sb.toString();
        }
    };

    public abstract String fetchElement(ITestResult in_testResult);

}
