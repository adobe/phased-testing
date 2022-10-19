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

import com.adobe.campaign.tests.integro.phased.data.permutational.MultipleProducerConsumer;
import com.adobe.campaign.tests.integro.phased.data.permutational.SimpleProducerConsumer;
import com.adobe.campaign.tests.integro.phased.data.permutational.SimpleProducerConsumerStaticImport;
import org.testng.annotations.Test;

import java.io.IOException;
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

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepPointer(),
                greaterThan(dependencies.getStep("bbbbb").getStepPointer()));
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

        assertThat("The pointer of aaaa should be after that of bbbbb", dependencies.getStep("aaaa").getStepPointer(),
                greaterThan(dependencies.getStep("bbbbb").getStepPointer()));
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

        assertThat("The pointer of aaaaa should be after that of bbbbb", dependencies.getStep("aaaaa").getStepPointer(),
                greaterThan(dependencies.getStep("bbbbb").getStepPointer()));

        assertThat("The pointer of bbbbb should be after that of ccccc", dependencies.getStep("bbbbb").getStepPointer(),
                greaterThan(dependencies.getStep("ccccc").getStepPointer()));
    }

}


