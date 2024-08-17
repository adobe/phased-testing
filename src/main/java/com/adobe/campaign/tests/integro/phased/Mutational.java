/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencies;
import com.adobe.campaign.tests.integro.phased.permutational.StepDependencies;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@PhasedTest
@Test(dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
public abstract class Mutational {

    public void shuffled(String phaseGroup) {

        String l_thisScneario = this.getClass().getTypeName()+"("+phaseGroup+")";

        PhasedTestManager.ScenarioContextData x = PhasedTestManager.getScenarioContext().get(l_thisScneario);

        Class l_executingClass = this.getClass();

        Map<String, ScenarioStepDependencies> l_scenarioDependencies = PhasedTestManager.getStepDependencies();


        List<StepDependencies> l_orderList = Phases.getCurrentPhase()
                .equals(Phases.PERMUTATIONAL) ? l_scenarioDependencies.get(l_executingClass.getTypeName())
                .fetchScenarioPermutations().get(phaseGroup) : l_scenarioDependencies.get(
                l_executingClass.getTypeName()).fetchExecutionOrderList();

      // var nrOfSteps = Phases.getCurrentPhase().hasSplittingEvent() ? PhasedTestManager.fetchShuffledStepCount(
       //         phaseGroup)[0] : l_orderList.size();

        Integer[] l_boundaries = MutationManager.fetchExecutionIndex(l_executingClass.getTypeName(), phaseGroup, Phases.getCurrentPhase() );
       // System.out.println(nrOfSteps + " - " + phaseGroup);
        //for (Method stepMethod : l_executingClass.getDeclaredMethods()) {
        //for (StepDependencies stepOrdering : stepOrder) {

        for (int i = l_boundaries[0]; i < l_boundaries[1]; i++) {
            try {
                //String lt_currentStepName = stepOrder.get(i).getStepName();
                //Method stepMethod = Arrays.stream(l_executingClass.getMethods()).filter(m -> m.getName().equals(lt_currentStepName)).findFirst().get();
                String stepName = l_orderList.get(i).getStepName();
                //System.out.println("Executing - " + stepName);

                Method stepMethod = Arrays.stream(l_executingClass.getDeclaredMethods())
                        .filter(dm -> dm.getName().equals(stepName)).findFirst().get();

                PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(stepMethod), phaseGroup);

                if (Phases.ASYNCHRONOUS.isSelected()) {

                    //Check if there is an event declared
                    String lt_event = PhasedEventManager.fetchEvent(stepMethod, phaseGroup);
                    if (lt_event != null) {
                        //TODO use PhasedTestManager for fetching full name instead
                        PhasedEventManager.startEvent(lt_event, l_thisScneario);
                    }
                }

                Object ourInstance = l_executingClass.getDeclaredConstructor().newInstance();
                long l_start = System.currentTimeMillis();
                stepMethod.invoke(ourInstance, phaseGroup);
                long l_end = System.currentTimeMillis();

                if (Phases.ASYNCHRONOUS.isSelected()) {
                    //Check if there is an event declared
                    String lt_event = PhasedEventManager.fetchEvent(stepMethod, phaseGroup);
                    if (lt_event != null) {
                        //TODO use PhasedTestManager for fetching full name instead
                        PhasedEventManager.finishEvent(lt_event, l_thisScneario);
                    }
                }


                PhasedTestManager.scenarioStateStore(PhasedTestManager.fetchScenarioName(stepMethod, phaseGroup),
                        ClassPathParser.fetchFullName(stepMethod), TestResult.SUCCESS,l_start,l_end);



            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
