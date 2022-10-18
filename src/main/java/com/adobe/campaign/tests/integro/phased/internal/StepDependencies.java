package com.adobe.campaign.tests.integro.phased.internal;

import java.util.HashSet;
import java.util.Set;

public class StepDependencies {

    private int stepPointer;

    public enum Relations{DEPENDS_ON, INDEPENDANT, DEPENDED_ON_BY, INTERDEPENDANT};
    private Set<String> produceSet;
    private String stepName;

    private Set<String> consumeSet;

    public StepDependencies(String in_stepName) {
        stepName = in_stepName;
        produceSet = new HashSet<>();
        consumeSet = new HashSet<>();
    }

    public Set<String> getConsumeSet() {
        return consumeSet;
    }

    public Set<String> getProduceSet() {
        return produceSet;
    }

    public String getStepName() {
        return stepName;
    }

    public int getStepPointer() {
        return stepPointer;
    }

    public void setStepPointer(int stepPointer) {
        this.stepPointer = stepPointer;
    }


    /**
     * Stores the given key in the set of produced resources
     *
     * @param in_key
     */
    public void produce(String in_key) {
        produceSet.add(in_key);
    }

    /**
     * Stores the given key in the set of consumed resources
     *
     * @param in_key
     */
    public void consume(String in_key) {
        consumeSet.add(in_key);
    }

    /**
     * This method checks the relationship between two steps. We have the following use cases:
     * <p/>
     * <ul>
     *     <li><bold>DEPENDED_ON_BY</bold>: when the given step depends on our current step.</li>
     *     <li><bold>DEPENDS_ON</bold>: when the given step depends on our current step.</li>
     *     <li><bold>INDEPENDANT</bold>: when there is no dependency between this step and the provided one.</li>
     *     <li><bold>CIRCULAR</bold>: When the two steps are inter-dependant.</li>
     * </ul>
     * @param in_step
     * @return
     */
    public Relations fetchRelation(StepDependencies in_step) {
        boolean l_producesMyConsumes = in_step.getProduceSet().stream().anyMatch(f -> this.getConsumeSet().contains(f));
        boolean l_consumesMyProduces = in_step.getConsumeSet().stream().anyMatch(f -> this.getProduceSet().contains(f));

        if (l_producesMyConsumes && !l_consumesMyProduces) {
            return Relations.DEPENDS_ON;
        }

        if (!l_producesMyConsumes && l_consumesMyProduces) {
            return Relations.DEPENDED_ON_BY;
        }

        if (!l_producesMyConsumes && !l_consumesMyProduces) {
            return Relations.INDEPENDANT;
        }

        return Relations.INTERDEPENDANT;
    }
}
