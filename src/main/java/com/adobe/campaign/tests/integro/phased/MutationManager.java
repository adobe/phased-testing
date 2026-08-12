/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.spi.MutationMode;
import com.adobe.campaign.tests.integro.phased.stepdependencies.StepDependencies;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MutationManager {

    /**
     * The {@link MutationMode} implementation for permutational ("Mutational") scenarios, delegating to this
     * class's existing static methods. Discovered by {@link PhasedTestManager} via {@link java.util.ServiceLoader}
     * (see {@code META-INF/services/com.adobe.campaign.tests.integro.phased.spi.MutationMode}) rather than through
     * a compile-time reference, so it must be a public class with a public no-arg constructor.
     */
    public static final class MutationManagerMode implements MutationMode {

        @Override
        public boolean appliesTo(Method in_method) {
            return isMutationalTest(in_method);
        }

        @Override
        public boolean appliesTo(Class<?> in_class) {
            return isMutationalTest(in_class);
        }

        @Override
        public boolean appliesTo(ITestResult in_testResult) {
            return isMutationalTest(in_testResult);
        }

        @Override
        public boolean isSingleMode(Class<?> in_class) {
            return MutationManager.isSingleMode(in_class);
        }

        @Override
        public boolean isShuffleMode(Class<?> in_class) {
            return MutationManager.isShuffleMode(in_class);
        }

        @Override
        public String fetchScenarioName(ITestResult in_testResult) {
            return MutationManager.fetchScenarioName(in_testResult);
        }

        @Override
        public boolean ownsDataProviderClass(Class<?> in_dataProviderClass) {
            return MutationalDataProvider.class.equals(in_dataProviderClass);
        }
    }

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
     * @param in_runValues      The set execution mode and behavior
     * @return An array of two entries. The first entry is the start index and the second entry is the end index
     */
    public static Integer[] fetchExecutionIndex(String in_className, String in_phaseGroup, RunValues in_runValues) {
        Integer[] lr_result = new Integer[2];

        //FetchNr Of Steps
        var l_relevantMethodMaps = PhasedTestManager.getMethodMap().keySet().stream().filter(m -> m.startsWith(in_className))
                .collect(Collectors.toList());
        int l_nrOfMethods = l_relevantMethodMaps.size();

        //By default run everything
        Integer[] l_boundaries = new Integer[] {0, l_nrOfMethods };

        if (in_runValues.getExecutionMode().equals(ExecutionMode.INTERRUPTIVE)) {
            if (in_phaseGroup.equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)) {
                l_boundaries = new Integer[] { l_relevantMethodMaps.stream()
                        .filter(m -> Arrays.stream(PhasedTestManager.methodMap.get(m).annotations)
                                .anyMatch(a -> a.annotationType().equals(PhaseEvent.class))).findFirst()
                        .map(mf -> PhasedTestManager.methodMap.get(mf).methodOrderInExecution).orElse(l_nrOfMethods)
                        - 1, l_nrOfMethods };
            } else {
                l_boundaries = PhasedTestManager.fetchShuffledStepCount(in_phaseGroup);
            }
        }

        switch (in_runValues.getBehavior()) {
        case "PRODUCER":
            lr_result[0] = 0;
            lr_result[1] = l_boundaries[0];
            break;
        case "CONSUMER":
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

    /**
     * Returns the data provider for a single phase
     * <p>
     * Author : gandomi
     *
     * @param in_method The step/method for which we want to fond out the data provider
     * @return An array containing the data providers for the method. Otherwise an empty array
     */
    public static Object[][] fetchProvidersSingle(ITestNGMethod in_method) {
        return new Object[][] {{ PhasedTestManager.STD_PHASED_GROUP_SINGLE }};
    }

    /**
     * Returns the provider for shuffling tests, in the context of Mutational tests.
     * <p>
     * Author : gandomi
     *
     * @param in_method The step/method for which we want to fond out the data provider
     * @return A two-dimensional array of all the data providers attached to the current step/method
     */
    public static Object[][] fetchProvidersShuffled(ITestNGMethod in_method) {
        String l_candidateMethod = PhasedTestManager
                .fetchMappingKeyWithMaxProviders(in_method.getTestClass().getRealClass().getTypeName(),
                        PhasedTestManager.getMethodMap());
        return fetchProvidersShuffled(l_candidateMethod, ExecutionMode.getCurrentMode().fetchRunValues());
    }

    /**
     * Returns the provider for shuffling tests. For the {@link ExecutionMode#PERMUTATIONAL} execution mode, the
     * values are the possible permutations of the scenario's steps. For any other execution mode, this delegates
     * to {@link PhasedTestManager#fetchProvidersShuffled(String, RunValues)}.
     * <p>
     * Author : gandomi
     *
     * @param in_methodFullName The full name of the method used for identifying it in the phase context
     * @param in_runMode        The mode in which the mutational tests are executed
     * @return A two-dimensional array of all the data providers attached to the current step/method
     */
    public static Object[][] fetchProvidersShuffled(String in_methodFullName, RunValues in_runMode) {
        if (!in_runMode.getExecutionMode().equals(ExecutionMode.PERMUTATIONAL)) {
            return PhasedTestManager.fetchProvidersShuffled(in_methodFullName, in_runMode);
        }

        final MethodMapping l_methodMapping = PhasedTestManager.getMethodMap().get(in_methodFullName);

        Map<String, List<StepDependencies>> l_permutations = PhasedTestManager.getStepDependencies()
                .get(l_methodMapping.declaredClass.getTypeName()).fetchScenarioPermutations();
        Object[][] l_objectArrayPhased = new Object[l_permutations.size()][1];
        int i = 0;
        for (Entry<String, List<StepDependencies>> entry : l_permutations.entrySet()) {
            l_objectArrayPhased[i][0] = entry.getKey();
            i++;
        }

        Object[][] l_userDefinedDataProviders = PhasedTestManager.fetchDataProviderValues(l_methodMapping.declaredClass);

        return PhasedTestManager.dataProvidersCrossJoin(l_objectArrayPhased, l_userDefinedDataProviders);
    }
}
