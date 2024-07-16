/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

import java.util.*;
import java.util.stream.Collectors;

public class ScenarioStepDependencies {
    private Map<String, StepDependencies> stepDependencies;
    private String scenarioName;

    public ScenarioStepDependencies(String in_scenarioName) {
        this.setScenarioName(in_scenarioName);
        this.setStepDependencies(new HashMap<>());
    }

    // Copy constructor
    public ScenarioStepDependencies(ScenarioStepDependencies original) {
        this.scenarioName = original.scenarioName;
        this.stepDependencies = new HashMap<>();
        for (Map.Entry<String, StepDependencies> entry : original.stepDependencies.entrySet()) {
            this.stepDependencies.put(entry.getKey(), new StepDependencies(entry.getValue()));
        }
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
        initializeStep(stepName);

        stepDependencies.get(stepName).produce(in_key);
    }

    /**
     * This method adds a "produce" dependency entry for a given step
     *
     * @param stepName  The name of the step
     * @param in_key    The key that is produced
     * @param in_lineNr The line number of the occurence
     */
    public void putProduce(String stepName, String in_key, int in_lineNr) {
        initializeStep(stepName);

        stepDependencies.get(stepName).produce(in_key);
        stepDependencies.get(stepName).setStepLine(in_lineNr);
    }

    /**
     * Initializes a step entry when the step is new
     *
     * @param stepName A name of a step
     */
    private void initializeStep(String stepName) {
        int l_lastStep = this.fetchLastStepPosition();
        if (!stepDependencies.containsKey(stepName)) {
            stepDependencies.put(stepName, new StepDependencies(stepName, l_lastStep + 1));
        }
    }

    /**
     * This method adds a "consume" dependency entry for a given step
     *
     * @param stepName The name of the step
     * @param in_key   The key that is consumed
     */
    public void putConsume(String stepName, String in_key) {
        initializeStep(stepName);

        stepDependencies.get(stepName).consume(in_key);
    }

    /**
     * This method adds a "consume" dependency entry for a given step
     *
     * @param stepName  The name of the step
     * @param in_key    The key that is consumed
     * @param in_lineNr The line number of the occurence
     */
    public void putConsume(String stepName, String in_key, int in_lineNr) {
        initializeStep(stepName);

        stepDependencies.get(stepName).consume(in_key);
        stepDependencies.get(stepName).setStepLine(in_lineNr);
    }

    /**
     * Returns a list of step dependencies ordered by line numbers. In this method we only consider the tests
     *
     * @return A list of dependencies sorted by their line numbers
     */
    public List<StepDependencies> fetchExecutionOrderList() {

        List<StepDependencies> lr_orderedSteps = new ArrayList<>();
        lr_orderedSteps.addAll(
                stepDependencies.values().stream().filter(f -> !f.isConfigMethod()).collect(Collectors.toSet()));

        lr_orderedSteps.sort(Comparator.comparing(StepDependencies::getStepLine));

        return lr_orderedSteps;
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
        initializeStep(in_stepName);
        //this.stepDependencies.put(in_stepName, new StepDependencies(in_stepName));
    }


    /**
     * Returns the step with the largest line number
     *
     * @return the step with the largest line number. Null if empty
     */
    public StepDependencies fetchLastStep() {
        if (getStepDependencies().isEmpty()) {
            return null;
        }
        return Collections.max(getStepDependencies().values(),
                Comparator.comparing(StepDependencies::getStepLine));
    }

    /**
     * Returns the location of the last step in the file.
     *
     * @return the line number of the last step
     */
    public int fetchLastStepPosition() {
        if (getStepDependencies().isEmpty()) {
            return StepDependencies.DEFAULT_LINE_LOCATION;
        }
        return fetchLastStep().getStepLine();
    }


    private static Map<String, List<StepDependencies>> generatePermutationsMap(
            List<List<StepDependencies>> l_scenarioPermutations) {
        Map<String, List<StepDependencies>> lr_permutations = new HashMap<>();

        l_scenarioPermutations.stream().collect(Collectors.toMap(l -> String.join("", l.stream().map(StepDependencies::getShortName).collect(
                Collectors.toList())), l -> l));

        for (int i = 0; i < l_scenarioPermutations.size(); i++) {
            String lt_key = PhasedTestManager.STD_PHASED_PERMUTATIONAL_PREFIX
                    +String.join("", l_scenarioPermutations.get(i).stream().map(StepDependencies::getShortName).collect(
                    Collectors.toList())) + "_" + (i + 1) + "-" + l_scenarioPermutations.size();

            lr_permutations.put(lt_key, l_scenarioPermutations.get(i));
        }
        return lr_permutations;
    }

    /**
     * This method calculated the possible permutations this scenario can have
     *
     * @return a map containing the permutations and their order
     */
    public Map<String, List<StepDependencies>> fetchScenarioPermutations() {
        var lr_permutationsMap = new HashMap<String, StepDependencies>();

        //Fetch the permutations
        List<List<StepDependencies>> l_scenarioPermutations = new ArrayList<>();
        fetchScenarioPermutations(new HashSet<>(), new ArrayList<StepDependencies>(), new ArrayList<StepDependencies>(this.stepDependencies.values()), l_scenarioPermutations);

        return generatePermutationsMap(l_scenarioPermutations);
    }

    protected void fetchScenarioPermutations(HashSet<String> in_dependencies, List<StepDependencies> in_currentScenario, List<StepDependencies> in_baseScenario, List<List<StepDependencies>> in_permutations) {
        //Fetch the steps that can run with the current dependencies
        Set<StepDependencies> l_honorSet = in_baseScenario.stream().filter(f -> f.canRunWithDependencies(in_dependencies))
                .collect(Collectors.toSet());

        //If there are no steps that can run with the current dependencies we add the scenario to the list
        if (l_honorSet.isEmpty()) {
            //in_currentScenario.setScenarioName(in_currentScenario.fe);
            in_permutations.add(in_currentScenario);
        } else {
            //For each step that can run with the current dependencies we create a new scenario
            for (StepDependencies l_step : l_honorSet) {
                List<StepDependencies> l_oldScenario = new ArrayList<>(in_baseScenario);

                List<StepDependencies> l_newScenario = new ArrayList<StepDependencies>(in_currentScenario);
                var newStep = l_oldScenario.remove(l_step);
                l_newScenario.add(l_step);

                HashSet<String> l_newDependencies = new HashSet<>(in_dependencies);
                l_newDependencies.addAll(l_step.getProduceSet());
                fetchScenarioPermutations(l_newDependencies, l_newScenario, l_oldScenario, in_permutations);
            }
        }
    }

    /**
     * This method creates a mapping based on the categorizations of the steps
     *
     * @return a map containing the categories and the steps that belong to them
     */
    public Map<StepDependencies.Categories, List<StepDependencies>> fetchCategorizations() {
        Map<StepDependencies.Categories, List<StepDependencies>> lr_categorizations = new HashMap<>();
        this.getStepDependencies().values().stream().filter(f -> !f.isConfigMethod()).forEach(f -> {
            lr_categorizations.computeIfAbsent(f.getCategory(), k -> new ArrayList<>()).add(f);
        });

        return lr_categorizations;
    }


}
