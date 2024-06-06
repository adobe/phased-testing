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

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.adobe.campaign.tests.integro.phased.data.permutational.*;
import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestExtractingDependencies {
    @BeforeMethod
    @AfterMethod
    public void prepareEnvironment() {
        ConfigValueHandlerPhased.resetAllValues();
    }

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
    public void testFetchExtractingProduceConsumeFromStep()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<SimpleProducerConsumerFromStep> l_testClass = SimpleProducerConsumerFromStep.class;

        ScenarioStepDependencies dependencies = ScenarioStepDependencyFactory.listMethodCalls(l_testClass);

        assertThat("Our object should have a map od StpDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));
        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(SimpleProducerConsumerFromStep.class.getTypeName()));
        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaa"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbb"));

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbb"));

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
                hasItems("keyA", "keyB"));

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

        assertThat("we should set the correct consume for our config method",
                dependencies.getStep("runMeBefore").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume got our config method",
                dependencies.getStep("runMeBefore").getProduceSet(), hasSize(0));
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

        assertThat("The ordered set should include only tests",
                dependencies.fetchExecutionOrderList().stream().map(f -> f.getStepName()).collect(
                        Collectors.toList()), contains("bbbbb", "aaaa"));
    }

    @Test
    public void testListMethodCalls_negativeFileNotFound()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<ProducerConsumerWithBeforeClass> l_testClass = ProducerConsumerWithBeforeClass.class;
        ConfigValueHandlerPhased.PHASED_TEST_SOURCE_LOCATION.activate("/nonExistingDirectory");

        Assert.assertThrows(PhasedTestConfigurationException.class,
                () -> ScenarioStepDependencyFactory.listMethodCalls(l_testClass));
    }

    @Test
    public void testGroupingOfDependencies() {
        //Simple
        ScenarioStepDependencies dependenciesSimple = ScenarioStepDependencyFactory.listMethodCalls(
                MixingEmptyAndProduceTests.class);

        Map<StepDependencies.Categories, List<StepDependencies>> l_groupedDependencies = dependenciesSimple.fetchCategorizations();

        assertThat("We should have a value", l_groupedDependencies, Matchers.notNullValue());
        assertThat("We should have two categories", l_groupedDependencies.keySet(),
                containsInAnyOrder(StepDependencies.Categories.INDEPENDANT,
                        StepDependencies.Categories.PRODUCER_ONLY,
                        StepDependencies.Categories.CONSUMER_ONLY));

        ScenarioStepDependencies dependencies2 = ScenarioStepDependencyFactory.listMethodCalls(
                SimplePermutationTest.class);

        assertThat("Should now have ProducerConsumer", dependencies2.fetchCategorizations().keySet(),
                containsInAnyOrder(
                        StepDependencies.Categories.PRODUCER_ONLY,
                        StepDependencies.Categories.CONSUMER_ONLY, StepDependencies.Categories.PRODUCER_CONSUMER));

    }

    @Test
    public void testCreatingPermutations_simple() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");
        List<StepDependencies> l_steps = new ArrayList<>();
        l_steps.add(new StepDependencies("z"));

        l_steps.add(new StepDependencies("y"));
        List<List<StepDependencies>> allPermutations = GeneralTestUtils.generatePermutations(l_steps, 0);
        assertThat("We should have 2 permutations", allPermutations.size(), equalTo(2));
        int l_locationOfZ = allPermutations.get(0).indexOf(new StepDependencies("z"));
        assertThat("The location of Z in the second list should not be the same",
                allPermutations.get(1).indexOf(new StepDependencies("z")),
                not(equalTo(l_locationOfZ)));
    }

    @Test
    public void testCreatingPermutations_single() {
        List<StepDependencies> l_steps = new ArrayList<>();
        l_steps.add(new StepDependencies("z"));

        List<List<StepDependencies>> allPermutations = GeneralTestUtils.generatePermutations(l_steps, 0);
        assertThat("We should have only 1 permutation", allPermutations.size(), equalTo(1));
        assertThat("We should have only 1 permutation", allPermutations.get(0).size(), equalTo(1));

        assertThat("We should only have our entry", allPermutations.get(0).get(0),
                Matchers.equalTo(new StepDependencies("z")));
    }

    @Test
    public void testCreatingPermutations_empty() {
        List<StepDependencies> l_steps = new ArrayList<>();

        List<List<StepDependencies>> allPermutations = GeneralTestUtils.generatePermutations(l_steps, 0);
        assertThat("We should have only 1 permutation", allPermutations.size(), equalTo(0));

        List<List<StepDependencies>> allPermutationsNull = GeneralTestUtils.generatePermutations(null, 0);
        assertThat("We should have only 1 permutation", allPermutations.size(), equalTo(0));
    }

    @Test
    public void testCreatingSimplePermutations() {
        ScenarioStepDependencies l_scenarioSteps = new ScenarioStepDependencies("MyScenario");
        l_scenarioSteps.addStep("a");
        l_scenarioSteps.addStep("b");

        l_scenarioSteps.getStepDependencies().get("a").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("a").setStepLine(10);
        l_scenarioSteps.getStepDependencies().get("a").produce("k1");

        l_scenarioSteps.getStepDependencies().get("b").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("b").setStepLine(15);
        l_scenarioSteps.getStepDependencies().get("b").produce("k2");

        var stepCombinations = l_scenarioSteps.fetchPermutations();
        assertThat("we should have a value", stepCombinations, Matchers.notNullValue());
        assertThat("We should have two permutations", stepCombinations.keySet(), hasSize(2));
        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(), Matchers.containsInAnyOrder("ab_1-2", "ba_2-2"));
        assertThat("The value for ab should be correct", stepCombinations.get("ab_1-2"), Matchers.equalTo(
                Arrays.asList(l_scenarioSteps.getStepDependencies().get("a"),
                        l_scenarioSteps.getStepDependencies().get("b"))));
        assertThat("The value for ab should be correct", stepCombinations.get("ba_2-2"), Matchers.equalTo(
                Arrays.asList(l_scenarioSteps.getStepDependencies().get("b"),
                        l_scenarioSteps.getStepDependencies().get("a"))));
    }

    @Test
    public void testCreatingSimplePermutationsProducerConsumer() {
        ScenarioStepDependencies l_scenarioSteps = new ScenarioStepDependencies("MyScenario");
        l_scenarioSteps.addStep("a");
        l_scenarioSteps.addStep("b");
        l_scenarioSteps.addStep("c");
        l_scenarioSteps.addStep("d");

        l_scenarioSteps.getStepDependencies().get("a").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("a").setStepLine(10);
        l_scenarioSteps.getStepDependencies().get("a").produce("k1");

        l_scenarioSteps.getStepDependencies().get("b").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("b").setStepLine(15);
        l_scenarioSteps.getStepDependencies().get("b").produce("k2");

        l_scenarioSteps.getStepDependencies().get("c").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("c").setStepLine(25);
        l_scenarioSteps.getStepDependencies().get("c").consume("k1");

        l_scenarioSteps.getStepDependencies().get("d").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("d").setStepLine(35);
        l_scenarioSteps.getStepDependencies().get("d").consume("k2");

        var stepCombinations = l_scenarioSteps.fetchPermutations();

        assertThat("We should have two permutations", stepCombinations.keySet(), hasSize(4));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.startsWith("abcd"), Matchers.startsWith("abdc"),
                        Matchers.startsWith("bacd"), Matchers.startsWith("badc")));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.endsWith("_1-4"), Matchers.endsWith("_2-4"),
                        Matchers.endsWith("_3-4"), Matchers.endsWith("_4-4")));

        String l_key = stepCombinations.keySet().stream().filter(f -> f.startsWith("badc")).findFirst().get();

        assertThat("The value for ab should be correct", stepCombinations.get(l_key), Matchers.equalTo(
                Arrays.asList(l_scenarioSteps.getStepDependencies().get("b"),
                        l_scenarioSteps.getStepDependencies().get("a"),
                        l_scenarioSteps.getStepDependencies().get("d"),
                        l_scenarioSteps.getStepDependencies().get("c"))));

    }

    @Test
    public void testCreatingSimplePermutationsConsumer() {
        ScenarioStepDependencies l_scenarioSteps = new ScenarioStepDependencies("MyScenario");
        l_scenarioSteps.addStep("a");
        l_scenarioSteps.addStep("b");
        l_scenarioSteps.addStep("c");

        l_scenarioSteps.getStepDependencies().get("a").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("a").setStepLine(10);
        l_scenarioSteps.getStepDependencies().get("a").produce("k1");

        l_scenarioSteps.getStepDependencies().get("b").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("b").setStepLine(15);
        l_scenarioSteps.getStepDependencies().get("b").consume("k2");

        l_scenarioSteps.getStepDependencies().get("c").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("c").setStepLine(25);
        l_scenarioSteps.getStepDependencies().get("c").consume("k1");

        var stepCombinations = l_scenarioSteps.fetchPermutations();

        assertThat("We should have two permutations", stepCombinations.keySet(), hasSize(2));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.startsWith("abc"), Matchers.startsWith("acb")));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.endsWith("_1-2"), Matchers.endsWith("_2-2")));

        String l_key = stepCombinations.keySet().stream().filter(f -> f.startsWith("acb")).findFirst().get();

        assertThat("The value for ab should be correct", stepCombinations.get(l_key), Matchers.equalTo(
                Arrays.asList(l_scenarioSteps.getStepDependencies().get("a"),
                        l_scenarioSteps.getStepDependencies().get("c"),
                        l_scenarioSteps.getStepDependencies().get("b"))));
    }

    @Test
    public void testCreatingPermutations_indies() {
        ScenarioStepDependencies l_scenarioSteps = new ScenarioStepDependencies("MyScenario");
        l_scenarioSteps.addStep("a");
        l_scenarioSteps.addStep("b");
        l_scenarioSteps.addStep("c");
        l_scenarioSteps.addStep("d");

        l_scenarioSteps.getStepDependencies().get("a").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("a").setStepLine(10);
        l_scenarioSteps.getStepDependencies().get("a").produce("k1");

        l_scenarioSteps.getStepDependencies().get("b").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("b").setStepLine(15);
        l_scenarioSteps.getStepDependencies().get("b");

        l_scenarioSteps.getStepDependencies().get("c").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("c").setStepLine(25);
        l_scenarioSteps.getStepDependencies().get("c").consume("k1");

        l_scenarioSteps.getStepDependencies().get("d").setConfigMethod(false);
        l_scenarioSteps.getStepDependencies().get("d").setStepLine(35);
        l_scenarioSteps.getStepDependencies().get("d").consume("k1");

        var stepCombinations = l_scenarioSteps.fetchPermutations();

        assertThat("We should have two permutations", stepCombinations.keySet(), hasSize(2));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.startsWith("abdc"), Matchers.startsWith("abcd")));

        assertThat("We should have the correct keys for permutations",
                stepCombinations.keySet(),
                Matchers.containsInAnyOrder(Matchers.endsWith("_1-2"), Matchers.endsWith("_2-2")));

        String l_key = stepCombinations.keySet().stream().filter(f -> f.startsWith("abdc")).findFirst().get();

        assertThat("The value for ab should be correct", stepCombinations.get(l_key), Matchers.equalTo(
                Arrays.asList(l_scenarioSteps.getStepDependencies().get("a"),
                        l_scenarioSteps.getStepDependencies().get("b"),
                        l_scenarioSteps.getStepDependencies().get("d"),
                        l_scenarioSteps.getStepDependencies().get("c"))));
    }

    @Test
    public void testOuterJoinListOfLists() {
        List<List<String>> l_list1 = new ArrayList<>();
        l_list1.add(Arrays.asList("a", "b"));
        l_list1.add(Arrays.asList("c", "d"));

        List<List<String>> l_list2 = new ArrayList<>();
        l_list2.add(Arrays.asList("1", "2"));
        l_list2.add(Arrays.asList("3", "4"));

        List<List<String>> l_result = GeneralTestUtils.outerJoinListOfLists(l_list1, l_list2);

        assertThat("We should have 4 entries", l_result, hasSize(4));
        assertThat("We should have the correct entries", l_result, containsInAnyOrder(
                Arrays.asList("a", "b", "1", "2"),
                Arrays.asList("a", "b", "3", "4"),
                Arrays.asList("c", "d", "1", "2"),
                Arrays.asList("c", "d", "3", "4")
        ));
    }

    @Test
    public void testOuterJoinListOfListsOneIsEmpty() {
        List<List<String>> l_list1 = new ArrayList<>();
        l_list1.add(Arrays.asList("a", "b"));
        l_list1.add(Arrays.asList("c", "d"));

        List<List<String>> l_list2 = new ArrayList<>();

        List<List<String>> l_result = GeneralTestUtils.outerJoinListOfLists(l_list1, l_list2);

        assertThat("We should have 4 entries", l_result, hasSize(2));
        assertThat("We should have the correct entries", l_result, containsInAnyOrder(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
        ));

        List<List<String>> l_result2 = GeneralTestUtils.outerJoinListOfLists(l_list2, l_list1);

        assertThat("We should have 4 entries", l_result2, hasSize(2));
        assertThat("We should have the correct entries", l_result2, containsInAnyOrder(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
        ));

        List<List<String>> l_result3 = GeneralTestUtils.outerJoinListOfLists(l_list2, l_list2);

        assertThat("We should have 4 entries", l_result3, hasSize(0));
    }

    @Test
    public void testOuterJoinListOfLists_negativeNull() {

        Assert.assertThrows(IllegalArgumentException.class,
                () -> GeneralTestUtils.outerJoinListOfLists(null, new ArrayList<>()));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> GeneralTestUtils.outerJoinListOfLists(new ArrayList<>(), null));
        Assert.assertThrows(IllegalArgumentException.class, () -> GeneralTestUtils.outerJoinListOfLists(null, null));
    }

}


