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

import java.util.HashSet;
import java.util.Set;

public class StepDependencies {

    protected static final int DEFAULT_LINE_LOCATION = -113;
    private boolean configMethod = false;
    private int stepLine = DEFAULT_LINE_LOCATION;

    @Override
    public int hashCode() {
        int result = getStepLine();
        result = 31 * result + getStepName().hashCode();
        return result;
    }

    public enum Relations{DEPENDS_ON, INDEPENDANT, DEPENDED_ON_BY, INTERDEPENDANT};
    private Set<String> produceSet;
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
        produceConsume(produceSet,in_key);
    }

    /**
     * Stores the given key in the set of consumed resources
     *
     * @param in_key The key being consumed
     */
    public void consume(String in_key) {
        produceConsume(consumeSet,in_key);
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

    public enum Categories {INDEPENDANT, PRODUCER_ONLY, PRODUCER_CONSUMER, CONSUMER_ONLY}

    /**
     * Returns the category of the step
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
}
