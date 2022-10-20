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

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestStepDependencies {
    @Test
    public void testScenarioDependencies() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(), equalTo(
                "a.b.c.D"));

    }

    @Test
    public void testStepDependencies() {
        StepDependencies sd = new StepDependencies("walther");
        assertThat("line location is by default 0", sd.getStepPointer(), equalTo(0));
        assertThat("We should now have a step with the name Walther", sd.getStepName(), equalTo("walther"));
        assertThat("We should have a produces set", sd.getProduceSet(), instanceOf(Set.class));
        assertThat("We should have a produces set", sd.getConsumeSet(), instanceOf(Set.class));

        assertThat("At this stage, we should now have no entries for produce", sd.getProduceSet().size(), equalTo(0));
        sd.produce("a");

        assertThat("We should now have an entry for produce", sd.getProduceSet().size(), equalTo(1));
        sd.produce("a");
        assertThat("We should still have one entry for produce", sd.getProduceSet().size(), equalTo(1));
        sd.produce("b");
        assertThat("We should have have two entries for produce", sd.getProduceSet().size(), equalTo(2));
        sd.produce("c");
        assertThat("We should have the correct values", sd.getProduceSet(), Matchers.containsInAnyOrder("a", "b", "c"));

        assertThat("At this stage, we should now have no entries for consume", sd.getConsumeSet().size(), equalTo(0));
        sd.consume("z");
        assertThat("We should now have an entry for produce", sd.getConsumeSet().size(), equalTo(1));
        sd.consume("f");
        assertThat("We should have two entrie for produce", sd.getConsumeSet().size(), equalTo(2));

        assertThat("We should have the correct values", sd.getConsumeSet(), Matchers.containsInAnyOrder("f", "z"));

    }

    @Test
    public void testScenarioDependenciesContinued() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");

        dependencies.putProduce("step2", "a2");

        assertThat("We should have two step dependencies registered for our scenario",
                dependencies.getStepDependencies().size(), equalTo(2));

        dependencies.putConsume("step3", "a2");

        assertThat("We should have a third step dependencies registered for our scenario",
                dependencies.getStepDependencies().size(), equalTo(3));

        assertThat("We should have a third step dependencies registered for our scenario",
                dependencies.getStepDependencies().get("step3").getConsumeSet(), containsInAnyOrder("a2"));

        dependencies.putConsume("step3", "a1");

        assertThat("We should have a third step dependencies registered for our scenario",
                dependencies.getStepDependencies().get("step3").getConsumeSet(), containsInAnyOrder("a1", "a2"));

        assertThat("We should have the correct helper", dependencies.getStep("step3"),
                equalTo(dependencies.getStepDependencies().get("step3")));

        dependencies.addStep("step4");

        assertThat("We should now have four step dependencies registered for our scenario",
                dependencies.getStepDependencies().size(), equalTo(4));

        assertThat("Our new dependency should have empty produces and consumes",
                dependencies.getStep("step4"), notNullValue());

        assertThat("Our new dependency should have empty produces and consumes",
                dependencies.getStep("step4").getProduceSet(), emptyCollectionOf(String.class));

        assertThat("Our new dependency should have empty produces and consumes",
                dependencies.getStep("step4").getConsumeSet(), emptyCollectionOf(String.class));
    }

    @Test
    public void testScenarioDependenciesContinued2() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");

        assertThat("we should have correctly set the line number",
                dependencies.getStep("step1").getStepPointer(), equalTo(0));

        dependencies.putProduce("step1", "a2", 5);

        assertThat("we should have correctly set the line number",
                dependencies.getStep("step1").getStepPointer(), equalTo(5));
    }


        @Test
    public void testOrderStack() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.getStep("step1").setStepPointer(1);

        dependencies.putConsume("step2", "a1");
        dependencies.getStep("step2").setStepPointer(3);

        List<StepDependencies> executionList = dependencies.fetchExecutionOrderList();

        assertThat("We should have two entries one per step", executionList.size(), equalTo(2));
       // assertThat("The first entry should be step 1", executionList.get(0).size(), equalTo(1));
       // assertThat("The first entry should be step 1", executionList.get(0).containsKey("a.b.c.D.step1"),
       //         equalTo(dependencies.stepDependencies.get("step1")));
    }

    @Test
    public void testOrderStackLevel2() {

        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.putConsume("step2", "a1");

        List<StepDependencies> executionList = dependencies.fetchExecutionOrderList();

        assertThat("We should have two entries one per step", executionList.size(), equalTo(2));
       // assertThat("The first entry should be step 1", executionList.get(0).size(), equalTo(1));
       // assertThat("The first entry should be step 1", executionList.get(0).containsKey("a.b.c.D.step1"),
       //         equalTo(dependencies.stepDependencies.get("step1")));
    }

    //Planned for #101
    @Test(enabled = false)
    public void testOrderStackLevel3() {

        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.putConsume("step2", "a1");

        List<StepDependencies> executionList = dependencies.fetchExecutionOrderList(
                dependencies.getStepDependencies().values(), new ArrayList<>());

        assertThat("We should have two entries one per step", executionList.size(), equalTo(2));
        assertThat("The first entry should be step 1", executionList.get(0),
                equalTo(dependencies.getStepDependencies().get("step1")));
    }

    @Test
    public void testOrderStackLevel4_stepRelationsHW() {

        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.putConsume("step2", "a1");

        assertThat("step1 should depend on step2",
                dependencies.getStep("step1").fetchRelation(dependencies.getStep("step2")),
                equalTo(StepDependencies.Relations.DEPENDED_ON_BY));

        assertThat("step2 should depend on step1",
                dependencies.getStep("step2").fetchRelation(dependencies.getStep("step1")),
                equalTo(StepDependencies.Relations.DEPENDS_ON));
    }

    @Test
    public void testOrderStackLevel4_stepRelationsIndependant() {

        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.putProduce("step2", "a2");

        assertThat("step1 & step2 are independant",
                dependencies.getStep("step1").fetchRelation(dependencies.getStep("step2")),
                equalTo(StepDependencies.Relations.INDEPENDANT));

        assertThat("step2 & step1 are independant",
                dependencies.getStep("step2").fetchRelation(dependencies.getStep("step1")),
                equalTo(StepDependencies.Relations.INDEPENDANT));

    }

    @Test
    public void testOrderStackLevel4_stepRelationsInterdependant() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");

        dependencies.putProduce("step1", "a1");
        dependencies.putConsume("step1", "a2");
        dependencies.putProduce("step2", "a2");
        dependencies.putConsume("step2", "a1");

        assertThat("step1 & step2 are independant",
                dependencies.getStep("step1").fetchRelation(dependencies.getStep("step2")),
                equalTo(StepDependencies.Relations.INTERDEPENDANT));

        assertThat("step2 & step1 are independant",
                dependencies.getStep("step2").fetchRelation(dependencies.getStep("step1")),
                equalTo(StepDependencies.Relations.INTERDEPENDANT));

    }

    @Test
    public void testOrderStackLevel4_stepRelationsEmptySets() {
        ScenarioStepDependencies dependencies = new ScenarioStepDependencies("a.b.c.D");
        dependencies.addStep("step1");
        dependencies.addStep("step2");

        assertThat("step1 & step2 are independant",
                dependencies.getStep("step1").fetchRelation(dependencies.getStep("step2")),
                equalTo(StepDependencies.Relations.INDEPENDANT));

        assertThat("step2 & step1 are independant",
                dependencies.getStep("step2").fetchRelation(dependencies.getStep("step1")),
                equalTo(StepDependencies.Relations.INDEPENDANT));

    }
}
