/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.events;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencies;
import com.adobe.campaign.tests.integro.phased.permutational.StepDependencies;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@PhasedTest
@Test(dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
public abstract class PhasedParent {


    public void shuffled(String phaseGroup) {

        Class l_executingClass = this.getClass();


        Map<String, ScenarioStepDependencies> l_scenarioDependencies = PhasedTestManager.getStepDependencies();
        //List<StepDependencies> stepOrder = l_scenarioDependencies.fetchExecutionOrderList();

        List<StepDependencies> l_orderList = Phases.getCurrentPhase().equals(Phases.PERMUTATIONAL) ? l_scenarioDependencies.get(l_executingClass.getTypeName()).fetchScenarioPermutations().get(phaseGroup) : l_scenarioDependencies.get(l_executingClass.getTypeName()).fetchExecutionOrderList();


        var nrOfSteps = Phases.getCurrentPhase().hasSplittingEvent() ? PhasedTestManager.fetchStepsBeforePhase(phaseGroup) : l_orderList.size();
        System.out.println(nrOfSteps);
        //for (Method stepMethod : l_executingClass.getDeclaredMethods()) {
        //for (StepDependencies stepOrdering : stepOrder) {


        for (int i = 0; i < nrOfSteps; i++) {
            try {
                //String lt_currentStepName = stepOrder.get(i).getStepName();
                //Method stepMethod = Arrays.stream(l_executingClass.getMethods()).filter(m -> m.getName().equals(lt_currentStepName)).findFirst().get();
                String stepName = l_orderList.get(i).getStepName();
                Method stepMethod = Arrays.stream(l_executingClass.getDeclaredMethods()).filter(dm -> dm.getName().equals(stepName)).findFirst().get();

                PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(stepMethod), phaseGroup);


                Object ourInstance = l_executingClass.getDeclaredConstructor().newInstance();
                stepMethod.invoke(ourInstance, phaseGroup);
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
