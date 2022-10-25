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
package com.adobe.campaign.tests.integro.phased.permutational;

import com.adobe.campaign.tests.integro.phased.data.permutational.*;
import org.mockito.Mockito;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestExtractingDependencies {

    @Test
    public void testFetchExtractingProduceConsume()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<SimpleProducerConsumer> l_testClass = SimpleProducerConsumer.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("Our object should have a map od StpDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));
        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(SimpleProducerConsumer.class.getTypeName()));
        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaa"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbkey"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbkey"));

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));
    }

    @Test
    public void testFetchExtractingProduceConsumeStaticImports()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<SimpleProducerConsumerStaticImport> l_testClass = SimpleProducerConsumerStaticImport.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("Our object should have a map od StpDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));

        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(SimpleProducerConsumerStaticImport.class.getTypeName()));

        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaa"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbkey"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbkey"));

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));
    }

    @Test
    public void testFetchExtractingMultipleProduceConsumeStaticImports()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<MultipleProducerConsumer> l_testClass = MultipleProducerConsumer.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(3));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaaa", "ccccc"));

        assertThat("we should set the correct consume", dependencies.getStep("ccccc").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("ccccc").getProduceSet(),
                hasItems("keyA"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("keyB"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaaa").getConsumeSet(),
                hasItems("keyA","keyB"));

        assertThat("The pointer of aaaaa should be after that of bbbbb", dependencies.getStep("aaaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));

        assertThat("The pointer of bbbbb should be after that of ccccc", dependencies.getStep("bbbbb").getStepLine(),
                greaterThan(dependencies.getStep("ccccc").getStepLine()));
    }


    @Test
    public void testFetchExtracting_noProduceOrConsume()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<NegativeEmptyTest> l_testClass = NegativeEmptyTest.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

    }


    @Test
    public void testFetchMixingEmptyAndProduceAndConsume()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<MixingEmptyAndProduceTests> l_testClass = MixingEmptyAndProduceTests.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(3));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaaa", "ccccc"));

        assertThat("we should set the correct consume", dependencies.getStep("ccccc").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("ccccc").getProduceSet(), hasSize(0));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("keyA"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaaa").getConsumeSet(),
                hasItems("keyA"));

        assertThat("The pointer of aaaaa should be after that of bbbbb", dependencies.getStep("aaaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));

        assertThat("The pointer of bbbbb should be after that of ccccc", dependencies.getStep("bbbbb").getStepLine(),
                greaterThan(dependencies.getStep("ccccc").getStepLine()));
    }


    @Test
    public void testExtractingClasses() throws NoSuchMethodException {
        final Method l_firstMethod = SimplePermutationTest.class.getMethod("zzzz",
                String.class);
        ITestNGMethod l_itrMethod = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itrMethod.getConstructorOrMethod()).thenReturn(l_com);
        Mockito.when(l_com.getMethod()).thenReturn(l_firstMethod);

        final Method l_secondMethod2 = SimplePermutationTest.class.getMethod("yyyyy",
                String.class);
        ITestNGMethod l_itrMethod2 = Mockito.mock(ITestNGMethod.class);
        ConstructorOrMethod l_com2 = Mockito.mock(ConstructorOrMethod.class);

        Mockito.when(l_itrMethod2.getConstructorOrMethod()).thenReturn(l_com2);
        Mockito.when(l_com2.getMethod()).thenReturn(l_secondMethod2);

        //IMethodInstance imi = new MethodInstance();

    }

    @Test
    public void testFetchExtractingProduceConsumeNested()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested> l_testClass = SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("Our object should have a map od StpDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));

        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(SimpleProducerConsumerNestedContainer.SimpleProducerConsumerNested.class.getTypeName()));

        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaa"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbkey"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbkey"));

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));
    }

    @Test
    public void testFetchExtractingProduceConsumeWithBeforeMethod()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<ProducerConsumerWithBeforeClass> l_testClass = ProducerConsumerWithBeforeClass.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("Our object should have a map of StepDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));
        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(l_testClass.getTypeName()));
        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(3));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("runMeBefore", "bbbbb", "aaaa"));

        assertThat("we should set the correct consume for our config method", dependencies.getStep("runMeBefore").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume got our config method", dependencies.getStep("runMeBefore").getProduceSet(), hasSize(0));
        assertThat("This test should be a config method", dependencies.getStep("runMeBefore").isConfigMethod());

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbkey"));
        assertThat("This test should be a test", !dependencies.getStep("bbbbb").isConfigMethod());

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbkey"));
        assertThat("This test should be a test", !dependencies.getStep("aaaa").isConfigMethod());

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepLine(),
                greaterThan(dependencies.getStep("bbbbb").getStepLine()));
    }

}


