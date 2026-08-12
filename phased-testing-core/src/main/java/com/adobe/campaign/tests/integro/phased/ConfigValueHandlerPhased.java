/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public enum ConfigValueHandlerPhased {
    PROP_SELECTED_PHASE("PHASED.TESTS.PHASE", Phases.NON_PHASED.name(), false),
    EVENTS_NONINTERRUPTIVE("MUTATIONAL.EVENTS.NONINTERRUPTIVE", null, false, "PHASED.EVENTS.NONINTERRUPTIVE"),
    PROP_PHASED_TEST_DATABROKER("PHASED.TESTS.DATABROKER", null, false),
    PROP_PHASED_DATA_PATH("PHASED.TESTS.STORAGE.PATH", null, false),
    PROP_OUTPUT_DIR("PHASED.TESTS.OUTPUT.DIR", PhasedTestManager.DEFAULT_CACHE_DIR,false),
    PROP_DISABLE_RETRY("PHASED.TESTS.RETRY.DISABLED", "true", false),
    PROP_MERGE_STEP_RESULTS("PHASED.TESTS.REPORT.BY.PHASE_GROUP","NOTSET", false),
    PHASED_TEST_SOURCE_LOCATION("PHASED.TESTS.CODE.ROOT","/src/test/java", false),
    PHASED_TEST_DETECT_ORDER("PHASED.TESTS.DETECT.ORDER", "false", false),
    PHASED_TEST_NONPHASED_LEGACY( "PHASED.TESTS.NONPHASED.LEGACY", "false", false ),
    PROP_SCENARIO_EXPORTED_PREFIX("PHASED.TESTS.STORAGE.SCENARIO.PREFIX", "[TC]", false),
    EVENT_TARGET("MUTATIONAL.EVENTS.TARGET", null, false, "PHASED.EVENTS.TARGET"),
    PROP_EXECUTION_MODE("MUTATIONAL.EXECUTION.MODE", "DEFAULT", false);

    private static final Logger log = LogManager.getLogger();

    public final String systemName;
    public final String defaultValue;
    public final boolean requiredValue;
    public final String deprecatedSystemName;

    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue) {
        this(in_propertyName, in_defaultValue, in_requiredValue, null);
    }

    /**
     * @param in_deprecatedPropertyName A previously used system property name for this config item, kept for
     *                                   backward compatibility. Used as a fallback when the current
     *                                   {@code systemName} is not set.
     */
    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue,
            String in_deprecatedPropertyName) {
        systemName = in_propertyName;
        defaultValue = in_defaultValue;
        requiredValue = in_requiredValue;
        deprecatedSystemName = in_deprecatedPropertyName;
    }

    /**
     * Returns the value for our config element. If not in system, we return the default value.
     * @return The string value of the given property
     */
    public String fetchValue() {
        if (deprecatedSystemName != null && System.getProperties().containsKey(deprecatedSystemName)
                && !System.getProperties().containsKey(systemName)) {
            log.warn("IMPORTANT: The property {} is DEPRECATED. Please use the property {} henceforth.",
                    deprecatedSystemName, systemName);
            return System.getProperty(deprecatedSystemName, defaultValue);
        }
        return System.getProperty(this.systemName, this.defaultValue);
    }

    /**
     * Sets the given value to our property
     * @param in_value set the value for the current config to this value
     */
    public void activate(String in_value) {
        System.setProperty(this.systemName, in_value);
    }

    /**
     * removed the given value from the system
     */
    public void reset() {
        System.clearProperty(this.systemName);
        if (deprecatedSystemName != null) {
            System.clearProperty(deprecatedSystemName);
        }
    }

    /**
     * Resets all of the values
     */
    public static void resetAllValues() {
        Arrays.stream(values()).forEach(ConfigValueHandlerPhased::reset);
    }

    /**
     * Checks if this config value is set
     * @return true if the value for our config item is in the system
     */
    public boolean isSet() {
        return System.getProperties().containsKey(this.systemName)
                || (deprecatedSystemName != null && System.getProperties().containsKey(deprecatedSystemName));
    }

    /**
     * Compares the value using equalsIgnoreCase
     * @param in_value A value to compare the current one to
     * @return true if the given value is the same as the set one.
     */
    public boolean is(String in_value) {
        return this.fetchValue().equalsIgnoreCase(in_value);
    }
}
