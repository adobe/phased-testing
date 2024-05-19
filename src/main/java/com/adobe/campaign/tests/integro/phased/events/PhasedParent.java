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


       // ScenarioStepDependencies l_scenarioDependencies = ScenarioStepDependencyFactory.listMethodCalls(l_executingClass);
        //List<StepDependencies> stepOrder = l_scenarioDependencies.fetchExecutionOrderList();

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
                Method stepMethod = l_executingClass.getDeclaredMethods()[i];
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
