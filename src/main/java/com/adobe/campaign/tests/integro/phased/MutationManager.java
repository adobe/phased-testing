/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

public class MutationManager {

    /**
     * This method provides an ID for the scenario given the ITestNGResult. This is assembled using the Classname + the
     * PhaseGroup
     * <p>
     * Author : gandomi
     *
     * @param in_testNGResult A TestNG Test Result object
     * @return The identity of the scenario
     */
    public static String fetchScenarioName(ITestResult in_testNGResult) {
        return in_testNGResult.getInstanceName() + ClassPathParser.fetchParameterValues(in_testNGResult);
    }

    /**
     * This method tells us if the ITestResult is mutational
     *
     * @param in_testResult The test result
     * @return true if it is a mutational test
     */
    public static boolean isMutationalTest(ITestResult in_testResult) {
        return in_testResult.getMethod().getRealClass().getTypeName().equals(Mutational.class.getTypeName());
    }

    /**
     * Lets us know if the given class is a mutational class. This means that it is a sub-class of Mutational
     * @param in_class a candidate class
     * @return true if the class inherits from Mutational
     */
    public static boolean isMutationalTest(Class in_class) {

        return in_class.getSuperclass() != null ? in_class.getSuperclass().equals(Mutational.class) : false;
    }

    /**
     * Lets us know if the given method is part of a mutational test. This means that it is a sub-class of Mutational
     * @param in_method a candidate class
     * @return true if the method is part of a class that inherits from Mutational
     */
    public static boolean isMutationalTest(Method in_method) {
        return isMutationalTest(in_method.getDeclaringClass());
    }

    public static String fetchScenarioName(String in_classFullName, String in_shuffleGroup) {
        return in_classFullName + ClassPathParser.fetchParameterValues(in_shuffleGroup);
    }

    /**
     * Return the execution index for a given scenario
     *
     * @param in_className  The name of the scenario
     * @param in_phaseGroup The phase group in which we are in
     * @param in_phase      The phase in which we are in
     * @return An array of two entries. The first entry is the start index and the second entry is the end index
     */
    public static Integer[] fetchExecutionIndex(String in_className, String in_phaseGroup, Phases in_phase) {
        Integer[] lr_result = new Integer[2];

        //FetchNr Of Steps
        int l_nrOfMethods = PhasedTestManager.getMethodMap().keySet().stream().filter(m -> m.startsWith(in_className))
                .collect(Collectors.toList()).size();

        Integer[] l_boundaries = in_phase.hasSplittingEvent() ? PhasedTestManager.fetchShuffledStepCount(
                in_phaseGroup) : new Integer[] {
                0, l_nrOfMethods };

        switch (in_phase) {
        case PRODUCER:
            lr_result[0] = 0;
            lr_result[1] = l_boundaries[0];
            break;
        case CONSUMER:
            lr_result[0] = l_boundaries[0];
            lr_result[1] = l_nrOfMethods;
            break;
        default:
            lr_result[0] = 0;
            lr_result[1] = l_nrOfMethods;
        }

        return lr_result;
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed in a Shuffled manner. For a test with 3
     * steps the test will be executed 6 times in total
     *
     * @param in_class A test class/scenario
     * @return True if the given test scenario is a Shuffled Phased Test scenario
     */
    public static boolean isShuffleMode(Class<?> in_class) {
        return !PhasedTestManager.isPhasedTestWithEvent(in_class) && !PhasedTestManager.isPhasedTestTargetOfEvent(in_class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed consequently in two phases
     *
     * @param in_class Any class that contains tests
     * @return True if the test class is a SingleRun Phase Test scenario
     */
    public static boolean isSingleMode(Class<?> in_class) {
        return isMutationalTest(in_class) && (PhasedTestManager.isPhasedTestWithEvent(in_class) || PhasedTestManager.isPhasedTestTargetOfEvent(in_class));

    }
}
