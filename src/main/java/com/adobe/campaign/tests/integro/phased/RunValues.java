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

}
