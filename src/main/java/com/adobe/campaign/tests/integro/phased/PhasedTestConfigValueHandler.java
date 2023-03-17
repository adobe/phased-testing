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

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Phases;

import java.util.Arrays;

public enum PhasedTestConfigValueHandler {
    PROP_SELECTED_PHASE("PHASED.TESTS.PHASE", Phases.NON_PHASED.name(), false),
    EVENTS_NONINTERRUPTIVE("PHASED.EVENTS.NONINTERRUPTIVE",null, false),
    PROP_PHASED_TEST_DATABROKER("PHASED.TESTS.DATABROKER", null, false),
    PROP_PHASED_DATA_PATH("PHASED.TESTS.STORAGE.PATH", null, false),
    PROP_OUTPUT_DIR("PHASED.TESTS.OUTPUT.DIR", PhasedTestManager.DEFAULT_CACHE_DIR,false),
    PROP_DISABLE_RETRY("PHASED.TESTS.RETRY.DISABLED", "true", false),
    PROP_MERGE_STEP_RESULTS("PHASED.TESTS.REPORT.BY.PHASE_GROUP","NOTSET", false),
    PHASED_TEST_SOURCE_LOCATION("PHASED.TESTS.CODE.ROOT","/src/test/java", false),
    PHASED_TEST_DETECT_ORDER("PHASED.TESTS.DETECT.ORDER", "false", false),
    PHASED_TEST_NONPHASED_LEGACY( "PHASED.TESTS.NONPHASED.LEGACY", "false", false ),
    PROP_SCENARIO_EXPORTED_PREFIX("PHASED.TESTS.STORAGE.SCENARIO.PREFIX", "[TC]", false);

    public final String systemName;
    public final String defaultValue;
    public final boolean requiredValue;

    PhasedTestConfigValueHandler(String in_propertyName, String in_defaultValue, boolean in_requiredValue) {
        systemName =in_propertyName;
        defaultValue=in_defaultValue;
        requiredValue=in_requiredValue;
    }

    /**
     * Returns the value for our config element. If not in system, we return the default value.
     * @return The string value of the given property
     */
    public String fetchValue() {
        return System.getProperty(this.systemName, this.defaultValue);
    }

    /**
     * Sets the given value to our property
     * @param in_value
     */
    public void activate(String in_value) {
        System.setProperty(this.systemName, in_value);
    }

    /**
     * removed the given value from the system
     */
    public void reset() {
        System.clearProperty(this.systemName);
    }

    /**
     * Resets all of the values
     */
    public static void resetAllValues() {
        Arrays.stream(values()).forEach(PhasedTestConfigValueHandler::reset);
    }

    /**
     * Checks if this config value is set
     * @return true if the value for our config item is in the system
     */
    public boolean isSet() {
        return System.getProperties().containsKey(this.systemName);
    }

    /**
     * Compares the value using equalsIgnoreCase
     * @param in_value
     * @return true if the given value is the same as the set one.
     */
    public boolean is(String in_value) {
        return this.fetchValue().equalsIgnoreCase(in_value);
    }
}
