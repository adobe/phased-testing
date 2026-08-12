/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.spi;

import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * A pluggable strategy describing one of the possible ways a phased scenario's execution can mutate
 * (e.g. interruptive-event shuffling, step permutation). {@link com.adobe.campaign.tests.integro.phased.PhasedTestManager}
 * delegates to whichever registered mode applies to a given method/class/result, without knowing the
 * concrete mode implementations itself.
 */
public interface MutationMode {

    /**
     * Tells us if the given method is governed by this mutation mode.
     *
     * @param in_method a candidate test method
     * @return true if this mode applies to the given method
     */
    boolean appliesTo(Method in_method);

    /**
     * Tells us if the given class is governed by this mutation mode.
     *
     * @param in_class a candidate test class
     * @return true if this mode applies to the given class
     */
    boolean appliesTo(Class<?> in_class);

    /**
     * Tells us if the given test result is governed by this mutation mode.
     *
     * @param in_testResult a TestNG test result
     * @return true if this mode applies to the given test result
     */
    boolean appliesTo(ITestResult in_testResult);

    /**
     * Tells us if, under this mode, the given class is to be executed consequently in two phases.
     *
     * @param in_class a candidate test class
     * @return true if the test class is a SingleRun scenario under this mode
     */
    boolean isSingleMode(Class<?> in_class);

    /**
     * Tells us if, under this mode, the given class is to be executed in a shuffled manner.
     *
     * @param in_class a candidate test class
     * @return true if the test class is a Shuffled scenario under this mode
     */
    boolean isShuffleMode(Class<?> in_class);

    /**
     * Provides an ID for the scenario given the ITestResult, under this mode.
     *
     * @param in_testResult a TestNG test result
     * @return the identity of the scenario
     */
    String fetchScenarioName(ITestResult in_testResult);
}
