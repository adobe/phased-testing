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
package com.adobe.campaign.tests.integro.phased.internal;

import java.util.*;

public class ScenarioStepDependencies {
    private Map<String, StepDependencies> stepDependencies;
    private String scenarioName;

    public ScenarioStepDependencies(String in_scenarioName) {
        this.scenarioName = in_scenarioName;
        stepDependencies = new HashMap<>();
    }

    public Map<String, StepDependencies> getStepDependencies() {
        return stepDependencies;
    }

    public void setStepDependencies(
            Map<String, StepDependencies> stepDependencies) {
        this.stepDependencies = stepDependencies;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    /**
     * This method adds a "produce" dependency entry for a given step
     *
     * @param stepName The name of the step
     * @param in_key   The key that is produced
     */
    public void putProduce(String stepName, String in_key) {
        initializeIfNeeded(stepName);

        stepDependencies.get(stepName).produce(in_key);
    }

    /**
     * This method adds a "produce" dependency entry for a given step
     *
     * @param stepName The name of the step
     * @param in_key   The key that is produced
     * @param in_lineNr The line number of the occurence
     */
    public void putProduce(String stepName, String in_key, int in_lineNr) {
        initializeIfNeeded(stepName);

        stepDependencies.get(stepName).produce(in_key);
        stepDependencies.get(stepName).setStepPointer(in_lineNr);
    }

    /**
     * Initializes a step entry when the step is new
     *
     * @param stepName
     */
    private void initializeIfNeeded(String stepName) {
        if (!stepDependencies.containsKey(stepName)) {
            stepDependencies.put(stepName, new StepDependencies(stepName));
        }
    }

    /**
     * This method adds a "consume" dependency entry for a given step
     *
     * @param stepName The name of the step
     * @param in_key   The key that is consumed
     */
    public void putConsume(String stepName, String in_key) {
        initializeIfNeeded(stepName);

        stepDependencies.get(stepName).consume(in_key);
    }

    /**
     * This method adds a "consume" dependency entry for a given step
     *
     * @param stepName The name of the step
     * @param in_key   The key that is consumed
     * @param in_lineNr The line number of the occurence
     */
    public void putConsume(String stepName, String in_key, int in_lineNr) {
        initializeIfNeeded(stepName);

        stepDependencies.get(stepName).consume(in_key);
        stepDependencies.get(stepName).setStepPointer(in_lineNr);
    }

    /**
     * Returns a list of step dependencies ordered by line numbers
     *
     * @return A list of dependencies sorted by their line numbers
     */
    public List<StepDependencies> fetchExecutionOrderList() {


        List<StepDependencies> lr_orderedSteps = new ArrayList<>();
        lr_orderedSteps.addAll(stepDependencies.values());

        lr_orderedSteps.sort(Comparator.comparing(StepDependencies::getStepPointer));

        return lr_orderedSteps;
    }

    public List<StepDependencies> fetchExecutionOrderList(Collection<StepDependencies> values,
            ArrayList<String> es) {
        List<StepDependencies> lr_orderedList = new ArrayList<>();

        return lr_orderedList;
    }

    /**
     * Given a step name it returns the step dependency information related to that step.
     *
     * @param in_stepName The name of the step
     * @return The step dependency information related to that step.
     */
    public StepDependencies getStep(String in_stepName) {
        return this.stepDependencies.get(in_stepName);
    }

    /**
     * Given a step name, a step is added to the scenario dependencies. This step will have empty produces and consumes.
     * If the step exists it is over-written
     *
     * @param in_stepName The name of the step
     */
    public void addStep(String in_stepName) {
        this.stepDependencies.put(in_stepName, new StepDependencies(in_stepName));
    }


}
