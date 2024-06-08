/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.permutational;

import java.util.HashSet;
import java.util.Set;

public class StepDependencies {

    protected static final int DEFAULT_LINE_LOCATION = -113;
    private boolean configMethod = false;
    private int stepLine = DEFAULT_LINE_LOCATION;
    private Set<String> produceSet;

    ;
    private String stepName;
    private Set<String> consumeSet;

    public StepDependencies(String in_stepName) {
        stepName = in_stepName;
        produceSet = new HashSet<>();
        consumeSet = new HashSet<>();
    }

    public StepDependencies(String in_stepName, int in_stepLine) {
        this(in_stepName);
        setStepLine(in_stepLine);
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

    public int getStepLine() {
        return stepLine;
    }

    public void setStepLine(int stepLine) {
        this.stepLine = stepLine;
    }

    public boolean isConfigMethod() {
        return configMethod;
    }

    public void setConfigMethod(boolean configMethod) {
        this.configMethod = configMethod;
    }

    /**
     * Stores the given key in the set of produced resources
     *
     * @param in_key The key being produced
     */
    public void produce(String in_key) {
        produceConsume(produceSet, in_key);
    }

    /**
     * Stores the given key in the set of consumed resources
     *
     * @param in_key The key being consumed
     */
    public void consume(String in_key) {
        produceConsume(consumeSet, in_key);
    }

    private void produceConsume(Set<String> in_produceConsume, String in_key) {
        if (!in_produceConsume.contains(in_key)) {
            in_produceConsume.add(in_key);
            setStepLine(getStepLine() + 1);
        }
    }

    /**
     * This method checks the relationship between two steps. We have the following use cases:
     * <p>
     * <ul>
     *     <li><span class="strong">DEPENDED_ON_BY</span>: when the given step depends on our current step.</li>
     *     <li><span class="strong">DEPENDS_ON</span>: when the given step depends on our current step.</li>
     *     <li><span class="strong">INDEPENDANT</span>: when there is no dependency between this step and the provided one.</li>
     *     <li><span class="strong">CIRCULAR</span>: When the two steps are inter-dependant.</li>
     * </ul>
     *
     * @param in_step The step with which we should compare our step
     * @return The relationship of type ${{@link Relations}}
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

    /**
     * Returns the category of the step
     *
     * @return a Category
     */
    Categories getCategory() {
        if (this.getConsumeSet().isEmpty() && this.getProduceSet().isEmpty()) {
            //INDEPENDANT
            return Categories.INDEPENDANT;

        } else if (this.getConsumeSet().isEmpty()) {
            //PRODUCER_ONLY
            return Categories.PRODUCER_ONLY;
        } else if (this.getProduceSet().isEmpty()) {
            //CONSUMER_ONLY
            return Categories.CONSUMER_ONLY;
        } else {
            //PRODUCER_CONSUMER
            return Categories.PRODUCER_CONSUMER;
        }
    }

    /**
     * Returns the short name of the step. This involves concatenating the first and last character of the stap name
     *
     * @return a shortname for the step
     */
    public String getShortName() {

        return getStepName().charAt(0) + ((getStepName().length() > 1) ? getStepName().substring(
                getStepName().length() - 1) : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StepDependencies that = (StepDependencies) o;

        if (getStepLine() != that.getStepLine()) {
            return false;
        }
        return getStepName().equals(that.getStepName());
    }

    @Override
    public int hashCode() {
        int result = getStepLine();
        result = 31 * result + getStepName().hashCode();
        return result;
    }

    public enum Relations {DEPENDS_ON, INDEPENDANT, DEPENDED_ON_BY, INTERDEPENDANT}

    public enum Categories {INDEPENDANT, PRODUCER_ONLY, PRODUCER_CONSUMER, CONSUMER_ONLY}
}
