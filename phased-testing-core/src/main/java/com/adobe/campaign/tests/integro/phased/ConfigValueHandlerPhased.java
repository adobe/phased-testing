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
    PROP_SELECTED_PHASE("PHASED.TESTS.PHASE", Phases.NON_PHASED.name(), false,
            "Sets the current phase (PRODUCER, CONSUMER, ASYNCHRONOUS, NON_PHASED).",
            true),
    EVENTS_NONINTERRUPTIVE("MUTATIONAL.EVENTS.NONINTERRUPTIVE", null, false, "PHASED.EVENTS.NONINTERRUPTIVE",
            "Specifies a non-interruptive event to wrap around test execution at run time."),
    PROP_PHASED_TEST_DATABROKER("MUTATIONAL.TESTS.DATABROKER", null, false, "PHASED.TESTS.DATABROKER",
            "The DataBroker implementation class to use for storing/fetching Phased Test data."),
    PROP_PHASED_DATA_PATH("MUTATIONAL.TESTS.STORAGE.PATH", null, false,
            "PHASED.TESTS.STORAGE.PATH",
            "The path where Phased Test data is stored and fetched from."),
    PROP_OUTPUT_DIR("MUTATIONAL.TESTS.OUTPUT.DIR", PhasedTestManager.DEFAULT_CACHE_DIR, false,
            "PHASED.TESTS.OUTPUT.DIR",
            "Overrides the output directory where Phased Test data is stored."),
    PROP_DISABLE_RETRY("MUTATIONAL.TESTS.RETRY.DISABLED", "true", false,
            "PHASED.TESTS.RETRY.DISABLED",
            "Disables the TestNG retry analyzer for Phased Tests (enabled by default)."),
    PROP_MERGE_STEP_RESULTS("MUTATIONAL.TESTS.REPORT.BY.PHASE_GROUP", "NOTSET", false,
            "PHASED.TESTS.REPORT.BY.PHASE_GROUP",
            "Activates 'Report By Phase Group' reporting, grouping step results by phase group."),
    PHASED_TEST_SOURCE_LOCATION("MUTATIONAL.TESTS.CODE.ROOT", "/src/test/java", false,
            "PHASED.TESTS.CODE.ROOT",
            "The root source directory used to detect code-based step execution order."),
    PHASED_TEST_DETECT_ORDER("MUTATIONAL.TESTS.DETECT.ORDER", "false", false,
            "PHASED.TESTS.DETECT.ORDER",
            "Activates code-based detection of step execution order within a scenario."),
    PHASED_TEST_NONPHASED_LEGACY("PHASED.TESTS.NONPHASED.LEGACY", "false", false,
            "Keeps the pre-8.0.0 default execution mode ('phased-data-provider-single') for backward "
                    + "compatibility.",
            true),
    PROP_SCENARIO_EXPORTED_PREFIX("MUTATIONAL.TESTS.STORAGE.SCENARIO.PREFIX", "[TC]", false,
            "PHASED.TESTS.STORAGE.SCENARIO.PREFIX",
            "The prefix used for exported/stored scenario names."),
    EVENT_TARGET("MUTATIONAL.EVENTS.TARGET", null, false, "PHASED.EVENTS.TARGET",
            "Targets an event to a specific step of a scenario, given as a method reference."),
    PROP_EXECUTION_MODE("MUTATIONAL.EXECUTION.MODE", "DEFAULT", false,
            "Sets the Mutational Test execution mode (STANDARD, INTERRUPTIVE(PRODUCER/CONSUMER), "
                    + "NON-INTERRUPTIVE, PERMUATIONAL).");

    private static final Logger log = LogManager.getLogger();

    public final String systemName;
    public final String defaultValue;
    public final boolean requiredValue;
    public final String legacySystemName;
    public final String description;
    public final boolean deprecated;

    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue,
            String in_description) {
        this(in_propertyName, in_defaultValue, in_requiredValue, null, in_description, false);
    }

    /**
     * @param in_legacyPropertyName A previously used system property name for this
     *                              config item, kept for
     *                              backward compatibility. Used as a fallback when
     *                              the current
     *                              {@code systemName} is not set.
     */
    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue,
            String in_legacyPropertyName, String in_description) {
        this(in_propertyName, in_defaultValue, in_requiredValue, in_legacyPropertyName, in_description, false);
    }

    /**
     * @param in_description A description of what this config item does. When
     *                        {@code in_deprecated} is {@code true}, this should
     *                        explain why the property is deprecated (as opposed to
     *                        renamed).
     * @param in_deprecated   Whether this config item's usage itself is deprecated,
     *                        as opposed to having been renamed (see
     *                        {@link #legacySystemName}).
     */
    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue,
            String in_description, boolean in_deprecated) {
        this(in_propertyName, in_defaultValue, in_requiredValue, null, in_description, in_deprecated);
    }

    ConfigValueHandlerPhased(String in_propertyName, String in_defaultValue, boolean in_requiredValue,
            String in_legacyPropertyName, String in_description, boolean in_deprecated) {
        systemName = in_propertyName;
        defaultValue = in_defaultValue;
        requiredValue = in_requiredValue;
        legacySystemName = in_legacyPropertyName;
        description = in_description;
        deprecated = in_deprecated;
    }

    /**
     * Returns the value for our config element. If not in system, we return the
     * default value.
     * 
     * @return The string value of the given property
     */
    public String fetchValue() {
        if (legacySystemName != null && System.getProperties().containsKey(legacySystemName)
                && !System.getProperties().containsKey(systemName)) {
            log.warn("IMPORTANT: The property {} is DEPRECATED. Please use the property {} henceforth.",
                    legacySystemName, systemName);
            return System.getProperty(legacySystemName, defaultValue);
        }
        return System.getProperty(this.systemName, this.defaultValue);
    }

    /**
     * Sets the given value to our property
     * 
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
        if (legacySystemName != null) {
            System.clearProperty(legacySystemName);
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
     * 
     * @return true if the value for our config item is in the system
     */
    public boolean isSet() {
        return System.getProperties().containsKey(this.systemName)
                || (legacySystemName != null && System.getProperties().containsKey(legacySystemName));
    }

    /**
     * Scans all config values and logs a warning for each one whose legacy system
     * property name is
     * currently set, so that legacy usage is surfaced up front rather than only
     * when the value is fetched.
     */
    public static void warnIfLegacyNamesAreUsed() {
        Arrays.stream(values())
                .filter(v -> v.legacySystemName != null && System.getProperties().containsKey(v.legacySystemName))
                .forEach(v -> log.warn("IMPORTANT: The property {} is DEPRECATED. Please use {} henceforth.",
                        v.legacySystemName, v.systemName));
    }

    /**
     * Scans all config values and logs a warning for each one that is marked deprecated and currently set,
     * so that usage of a property that is going away (as opposed to renamed) is surfaced up front.
     */
    public static void warnIfDeprecatedPropertiesAreUsed() {
        Arrays.stream(values())
                .filter(v -> v.deprecated && v.isSet())
                .forEach(v -> log.warn("IMPORTANT: The property {} is DEPRECATED. {}", v.systemName, v.description));
    }

    /**
     * Compares the value using equalsIgnoreCase
     * 
     * @param in_value A value to compare the current one to
     * @return true if the given value is the same as the set one.
     */
    public boolean is(String in_value) {
        return this.fetchValue().equalsIgnoreCase(in_value);
    }
}
