package com.adobe.campaign.tests.integro.phased.events;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@PhasedTest
@Test
public abstract class PhasedParent {


    public void shuffled(String phaseGroup) {

        Class l_executingClass = this.getClass();
        //Assert.assertEquals(l_executingClass.getTypeName(), "com.adobe.campaign.tests.integro.phased.events.data.PhasedChild");
        //Arrays.stream(itx.getAllTestMethods()).
        for (Method m : l_executingClass.getDeclaredMethods()) {
            try {
                Object ourInstance = l_executingClass.getDeclaredConstructor().newInstance();
                m.invoke(ourInstance, phaseGroup);
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
