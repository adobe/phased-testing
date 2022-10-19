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
