/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

public class RunValues {
    private ExecutionMode executionMode;
    private String behavior;

    RunValues(ExecutionMode executionMode, String behavior) {
        this.executionMode = executionMode;
        this.behavior = behavior;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public String getBehavior() {
        return behavior;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RunValues runValues = (RunValues) o;

        if (getExecutionMode() != runValues.getExecutionMode()) {
            return false;
        }
        return getBehavior().equals(runValues.getBehavior());
    }

    @Override
    public String toString() {
        return executionMode.name() + (!getBehavior().isEmpty() ? "(" + behavior + ")" : "");
    }

}
