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
package com.adobe.campaign.tests.integro.phased.events;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencies;
import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencyFactory;
import com.adobe.campaign.tests.integro.phased.permutational.StepDependencies;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@PhasedTest
@Test(dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
public abstract class PhasedParent {


    public void shuffled(String phaseGroup) {

        Class l_executingClass = this.getClass();


        Map<String, ScenarioStepDependencies> l_scenarioDependencies = PhasedTestManager.getStepDependencie();
        //List<StepDependencies> stepOrder = l_scenarioDependencies.fetchExecutionOrderList();
        List<StepDependencies> l_orderList = l_scenarioDependencies.get(l_executingClass.getTypeName()).fetchExecutionOrderList();
        //Assert.assertEquals(l_executingClass.getTypeName(), "com.adobe.campaign.tests.integro.phased.events.data.PhasedChild");
        //Arrays.stream(itx.getAllTestMethods()).

        var nrOfSteps = PhasedTestManager.fetchStepsBeforePhase(phaseGroup);
        System.out.println(nrOfSteps);
        //for (Method stepMethod : l_executingClass.getDeclaredMethods()) {
        //for (StepDependencies stepOrdering : stepOrder) {


        for (int i = 0; i < nrOfSteps; i++) {
            try {
                //String lt_currentStepName = stepOrder.get(i).getStepName();
                //Method stepMethod = Arrays.stream(l_executingClass.getMethods()).filter(m -> m.getName().equals(lt_currentStepName)).findFirst().get();
                String stepName = l_orderList.get(i).getStepName();
                Method stepMethod = Arrays.stream(l_executingClass.getDeclaredMethods()).filter(dm -> dm.getName().equals(stepName)).findFirst().get();
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
